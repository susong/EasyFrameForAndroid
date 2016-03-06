package com.dream.photoselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.photoselector.R;
import com.dream.photoselector.domain.PhotoSelectorDomain;
import com.dream.photoselector.model.AlbumModel;
import com.dream.photoselector.model.PhotoModel;
import com.dream.photoselector.util.PsAnimationUtils;
import com.dream.photoselector.util.PsCommonUtils;
import com.dream.photoselector.util.PsConstants;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.util.ArrayList;
import java.util.List;

public class PhotoSelectorActivity extends Activity {


    public static final int SINGLE_IMAGE = 1;
    public static final String KEY_MAX_SIZE = "key_max_size";
    public static final String KEY_CURRENT_SIZE = "key_current_size";
    public static final String KEY_PHOTO_LIST = "key_photo_list";
    public static final String KEY_PHOTO_SELECTOR_LIST = "key_photo_selector_list";
    public static final String KEY_IS_SELECTOR_PREVIEW = "key_is_selector_preview";
    public static final String KEY_POSITION = "key_position";

    public static String mRecentPhotoStr = null;

    public static boolean mIsFull;
    public static int mMaxSize = 0;
    private int mCurrentSize = 0;
    private String sure;

    private GridView gvPhotos;
    private ListView lvAlbum;
    private Button btnOk;
    private TextView tvAlbum, tvPreview, tvTitle;
    private PhotoSelectorDomain mPhotoSelectorDomain;
    private PhotoSelectorAdapter mPhotoSelectorAdapter;
    private AlbumAdapter mAlbumAdapter;
    private RelativeLayout layoutAlbum;
    public static List<PhotoModel> mPhotoModelList = new ArrayList<PhotoModel>();
    public static List<PhotoModel> mPhotoModelSelectorList = new ArrayList<PhotoModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_photo_selector);

        initImageLoader();
        initIntent();
        initView();
        initListener();
        initAdapter();
        initData();
    }

    private void initView() {
        layoutAlbum = (RelativeLayout) findViewById(R.id.layout_album_ar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
        lvAlbum = (ListView) findViewById(R.id.lv_ablum_ar);
        btnOk = (Button) findViewById(R.id.btn_right_lh);
        tvAlbum = (TextView) findViewById(R.id.tv_album_ar);
        tvPreview = (TextView) findViewById(R.id.tv_preview_ar);

        mRecentPhotoStr = getResources().getString(R.string.recent_photos);
        sure = getResources().getString(R.string.sure);

        int currentSize = mCurrentSize + mPhotoModelSelectorList.size();
        btnOk.setText(sure + "(" + currentSize + "/" + mMaxSize + ")");
    }

    private void initListener() {
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ok(); // 选完照片
            }
        });
        tvAlbum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                album();
            }
        });
        tvPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 预览照片
                 */
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_MAX_SIZE, mMaxSize);
                bundle.putInt(KEY_CURRENT_SIZE, mCurrentSize);
                bundle.putBoolean(KEY_IS_SELECTOR_PREVIEW, true);
//                bundle.putParcelableArrayList(KEY_PHOTO_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
//                bundle.putParcelableArrayList(KEY_PHOTO_SELECTOR_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
                PsCommonUtils.launchActivityForResult(PhotoSelectorActivity.this, PhotoPreviewActivity.class, PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_PREVIEW, bundle);
            }
        });
        /**
         * 相册列表点击事件
         */
        lvAlbum.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
                for (int i = 0; i < parent.getCount(); i++) {
                    AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
                    if (i == position)
                        album.setCheck(true);
                    else
                        album.setCheck(false);
                }
                mAlbumAdapter.notifyDataSetChanged();
                hideAlbum();
                tvAlbum.setText(current.getName());
                // tvTitle.setText(current.getName());

                // 更新照片列表
                if (current.getName().equals(mRecentPhotoStr)) {
                    getCurrentPhotoList();
                } else {
                    getPhotoListByAlbum(current.getName());
                }
            }
        });

        // 返回
        findViewById(R.id.ll_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                clear();
            }
        });
    }

    private void initIntent() {
        if (getIntent().getExtras() != null) {
            mMaxSize = getIntent().getIntExtra(KEY_MAX_SIZE, 0);
            mCurrentSize = getIntent().getIntExtra(KEY_CURRENT_SIZE, 0);
        }
        if (mMaxSize == 0) {
            mIsFull = true;
        } else {
            mIsFull = false;
        }
    }

    private void initAdapter() {

        mPhotoSelectorAdapter = new PhotoSelectorAdapter(getApplicationContext(), PsCommonUtils.getWidthPixels(this),
                /**
                 * 照片选中状态改变之后
                 */
                new PhotoItem.onPhotoItemCheckedListener() {
                    @Override
                    public void onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (!mPhotoModelSelectorList.contains(photoModel)) {
                                mPhotoModelSelectorList.add(photoModel);
                            }
                            tvPreview.setEnabled(true);
                        } else {
                            mPhotoModelSelectorList.remove(photoModel);
                        }

                        int currentSize = mCurrentSize + mPhotoModelSelectorList.size();
                        btnOk.setText(sure + "(" + currentSize + "/" + mMaxSize + ")");
                        if (currentSize >= mMaxSize) {
                            mIsFull = true;
                        } else {
                            mIsFull = false;
                        }

                        if (mPhotoModelSelectorList.isEmpty()) {
                            tvPreview.setEnabled(false);
                            tvPreview.setText(getString(R.string.preview));
                        }
                    }
                },
                /**
                 * 点击查看照片
                 */
                new PhotoItem.onItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Bundle bundle = new Bundle();
                        if (tvAlbum.getText().toString().equals(mRecentPhotoStr)) {
                            bundle.putInt(KEY_POSITION, position);
                        } else {
                            bundle.putInt(KEY_POSITION, position);
                        }
                        bundle.putInt(KEY_MAX_SIZE, mMaxSize);
                        bundle.putInt(KEY_CURRENT_SIZE, mCurrentSize);
                        bundle.putBoolean(KEY_IS_SELECTOR_PREVIEW, false);
//                        bundle.putParcelableArrayList(KEY_PHOTO_LIST, (ArrayList<PhotoModel>) mPhotoModelList);
//                        bundle.putParcelableArrayList(KEY_PHOTO_SELECTOR_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
                        PsCommonUtils.launchActivityForResult(PhotoSelectorActivity.this, PhotoPreviewActivity.class, PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_PREVIEW, bundle);
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        catchPicture();
                    }
                });
        gvPhotos.setAdapter(mPhotoSelectorAdapter);

        mAlbumAdapter = new AlbumAdapter(getApplicationContext());
        lvAlbum.setAdapter(mAlbumAdapter);
    }

    private void initData() {
        mPhotoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());
        getAlbumList();
        getCurrentPhotoList();
    }

    private void getAlbumList() {
        // 获取相册列表
        mPhotoSelectorDomain.getAlbumsList(new OnAlbumLoadListener() {
            @Override
            public void onAlbumLoaded(List<AlbumModel> albumModelList) {
                mAlbumAdapter.updateAdapter(albumModelList);
            }
        });
    }

    private void getCurrentPhotoList() {
        // 获取最近照片
        mPhotoSelectorDomain.getCurrentPhotoList(new OnPhotoLoadListener() {
            @Override
            public void onPhotoLoaded(List<PhotoModel> photoModelList) {
                dealPhotoModeList(photoModelList);
            }
        });
    }

    private void getPhotoListByAlbum(String AlbumName) {
        mPhotoSelectorDomain.getPhotoListByAlbum(AlbumName, new OnPhotoLoadListener() {
            @Override
            public void onPhotoLoaded(List<PhotoModel> photoModelList) {
                dealPhotoModeList(photoModelList);
            }
        });
    }

    private void dealPhotoModeList(List<PhotoModel> photoModelList) {
        mPhotoModelList = photoModelList;
        for (PhotoModel photoModel : photoModelList) {
            if (mPhotoModelSelectorList.contains(photoModel)) {
                photoModel.setChecked(true);
            }
        }
        mPhotoSelectorAdapter.updateAdapter(photoModelList);
        gvPhotos.smoothScrollToPosition(0); // 滚动到顶端
        // reset(); //--keep mPhotoModelSelectorList mPhotoModelSelectorList
    }

    /**
     * 拍照
     */
    private void catchPicture() {
        PsCommonUtils.launchActivityForResult(this, new Intent(MediaStore.ACTION_IMAGE_CAPTURE), PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_CAMERA) {
            PhotoModel photoModel = new PhotoModel(PsCommonUtils.query(getApplicationContext(), data.getData()));

            if (mPhotoModelSelectorList.size() >= mMaxSize) {
                Toast.makeText(this, String.format(getString(R.string.max_img_limit_reached), mMaxSize), Toast.LENGTH_SHORT).show();
                photoModel.setChecked(false);
                mPhotoSelectorAdapter.notifyDataSetChanged();
            } else {
                if (!mPhotoModelSelectorList.contains(photoModel)) {
                    mPhotoModelSelectorList.add(photoModel);
                }
            }
            ok();
        } else if (resultCode == PsConstants.PHOTO_PREVIEW_RESULT_CODE_CONFIRM && requestCode == PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_PREVIEW) {
            mPhotoModelSelectorList = data.getParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST);

            for (PhotoModel photoModel : mPhotoModelList) {
                if (mPhotoModelSelectorList.contains(photoModel)) {
                    photoModel.setChecked(true);
                } else {
                    photoModel.setChecked(false);
                }
            }
            mPhotoSelectorAdapter.updateAdapter(mPhotoModelList);

            int currentSize = mCurrentSize + mPhotoModelSelectorList.size();
            if (currentSize >= mMaxSize) {
                mIsFull = true;
            } else {
                mIsFull = false;
            }
            btnOk.setText(sure + "(" + currentSize + "/" + mMaxSize + ")");
            ok();
        } else if (resultCode == PsConstants.PHOTO_PREVIEW_RESULT_CODE_BACK && requestCode == PsConstants.PHOTO_SELECTOR_REQUEST_PHOTO_PREVIEW) {
            mPhotoModelSelectorList = data.getParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST);
            for (PhotoModel photoModel : mPhotoModelList) {
                if (mPhotoModelSelectorList.contains(photoModel)) {
                    photoModel.setChecked(true);
                } else {
                    photoModel.setChecked(false);
                }
            }
            mPhotoSelectorAdapter.updateAdapter(mPhotoModelList);

            int currentSize = mCurrentSize + mPhotoModelSelectorList.size();
            if (currentSize >= mMaxSize) {
                mIsFull = true;
            } else {
                mIsFull = false;
            }
            btnOk.setText(sure + "(" + currentSize + "/" + mMaxSize + ")");
        }
    }

    /**
     * 完成
     */
    private void ok() {
        if (mPhotoModelSelectorList.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra(KEY_PHOTO_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
            setResult(RESULT_OK, intent);
        }
        finish();
        clear();
    }

    private void album() {
        if (layoutAlbum.getVisibility() == View.GONE) {
            popAlbum();
        } else {
            hideAlbum();
        }
    }

    /**
     * 弹出相册列表
     */
    private void popAlbum() {
        layoutAlbum.setVisibility(View.VISIBLE);
        new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_album_list_translate_up)
                .setLinearInterpolator().startAnimation(layoutAlbum);
    }

    /**
     * 隐藏相册列表
     */
    private void hideAlbum() {
        new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_album_list_translate_down)
                .setLinearInterpolator().startAnimation(layoutAlbum);
        layoutAlbum.setVisibility(View.GONE);
    }

    /**
     * 清空选中的图片
     */
    private void reset() {
        mPhotoModelSelectorList.clear();
        btnOk.setText(sure + "(" + 0 + "/" + mMaxSize + ")");
        tvPreview.setEnabled(false);
    }

    private void clear() {
        mPhotoModelList.clear();
        mPhotoModelSelectorList.clear();
    }

    @Override
    public void onBackPressed() {
        if (layoutAlbum.getVisibility() == View.VISIBLE) {
            hideAlbum();
        } else
            super.onBackPressed();
    }

    /**
     * 获取本地图库照片回调
     */
    public interface OnPhotoLoadListener {
        void onPhotoLoaded(List<PhotoModel> photos);
    }

    /**
     * 获取本地相册信息回调
     */
    public interface OnAlbumLoadListener {
        void onAlbumLoaded(List<AlbumModel> albums);
    }

    private void initImageLoader() {
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_picture_loading)
                .showImageOnFail(R.drawable.ic_picture_loadfailed)
                .cacheInMemory(true).cacheOnDisk(true)
                .resetViewBeforeLoading(true).considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(400, 400)
                .diskCacheExtraOptions(400, 400, null)
                .threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                .diskCache(new UnlimitedDiskCache(StorageUtils.getCacheDirectory(this, true)))
                .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(this))
                .imageDecoder(new BaseImageDecoder(false))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .defaultDisplayImageOptions(imageOptions).build();
        ImageLoader.getInstance().init(config);
    }
}
