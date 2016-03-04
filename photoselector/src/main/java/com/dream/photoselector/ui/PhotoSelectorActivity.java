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
import com.dream.photoselector.util.AnimationUtil;
import com.dream.photoselector.util.CommonUtils;
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

/**
 * @author Aizaz AZ
 */
public class PhotoSelectorActivity extends Activity implements
        PhotoItem.onItemClickListener,
        PhotoItem.onPhotoItemCheckedListener,
        OnItemClickListener,
        OnClickListener {

    public static final int SINGLE_IMAGE = 1;
    public static final String KEY_MAX_SIZE = "key_max_size";
    public static final String KEY_CURRENT_SIZE = "key_current_size";
    public static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_CAMERA = 1;

    public static String sRecentPhoto = null;

    public static boolean sIsFull;
    public static int sMaxImageSize = 0;
    private int mCurrentSize = 0;
    private String sure;


    private GridView gvPhotos;
    private ListView lvAblum;
    private Button btnOk;
    private TextView tvAlbum, tvPreview, tvTitle;
    private PhotoSelectorDomain mPhotoSelectorDomain;
    private PhotoSelectorAdapter mPhotoSelectorAdapter;
    private AlbumAdapter mAlbumAdapter;
    private RelativeLayout layoutAlbum;
    private ArrayList<PhotoModel> mPhotoModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_photoselector);
        sRecentPhoto = getResources().getString(R.string.recent_photos);
        sure = getResources().getString(R.string.sure);

        if (getIntent().getExtras() != null) {
            sMaxImageSize = getIntent().getIntExtra(KEY_MAX_SIZE, 0);
            mCurrentSize = getIntent().getIntExtra(KEY_CURRENT_SIZE, 0);
        }
        if (sMaxImageSize == 0) {
            sIsFull = true;
        } else {
            sIsFull = false;
        }

        initImageLoader();

        mPhotoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());

        mPhotoModelList = new ArrayList<PhotoModel>();

        tvTitle = (TextView) findViewById(R.id.tv_title_lh);
        gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
        lvAblum = (ListView) findViewById(R.id.lv_ablum_ar);
        btnOk = (Button) findViewById(R.id.btn_right_lh);
        tvAlbum = (TextView) findViewById(R.id.tv_album_ar);
        tvPreview = (TextView) findViewById(R.id.tv_preview_ar);
        layoutAlbum = (RelativeLayout) findViewById(R.id.layout_album_ar);

        btnOk.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        tvPreview.setOnClickListener(this);

        mPhotoSelectorAdapter = new PhotoSelectorAdapter(getApplicationContext(), new ArrayList<PhotoModel>(), CommonUtils.getWidthPixels(this), this, this, this);
        gvPhotos.setAdapter(mPhotoSelectorAdapter);

        mAlbumAdapter = new AlbumAdapter(getApplicationContext(), new ArrayList<AlbumModel>());
        lvAblum.setAdapter(mAlbumAdapter);
        lvAblum.setOnItemClickListener(this);

        findViewById(R.id.bv_back_lh).setOnClickListener(this); // 返回

        mPhotoSelectorDomain.getRecent(recentListener); // 更新最近照片
        mPhotoSelectorDomain.updateAlbum(albumListener); // 跟新相册信息
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_right_lh)
            ok(); // 选完照片
        else if (v.getId() == R.id.tv_album_ar)
            album();
        else if (v.getId() == R.id.tv_preview_ar)
            preview();
        else if (v.getId() == R.id.tv_camera_vc)
            catchPicture();
        else if (v.getId() == R.id.bv_back_lh)
            finish();
    }

    /**
     * 拍照
     */
    private void catchPicture() {
        CommonUtils.launchActivityForResult(this, new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            PhotoModel photoModel = new PhotoModel(CommonUtils.query(getApplicationContext(), data.getData()));

            if (mPhotoModelList.size() >= sMaxImageSize) {
                Toast.makeText(this, String.format(getString(R.string.max_img_limit_reached), sMaxImageSize), Toast.LENGTH_SHORT).show();
                photoModel.setChecked(false);
                mPhotoSelectorAdapter.notifyDataSetChanged();
            } else {
                if (!mPhotoModelList.contains(photoModel)) {
                    mPhotoModelList.add(photoModel);
                }
            }
            ok();
        }
    }

    /**
     * 完成
     */
    private void ok() {
        if (mPhotoModelList.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", mPhotoModelList);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    /**
     * 预览照片
     */
    private void preview() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", mPhotoModelList);
        CommonUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
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
        new AnimationUtil(getApplicationContext(), R.anim.ps_anim_album_list_translate_up)
                .setLinearInterpolator().startAnimation(layoutAlbum);
    }

    /**
     * 隐藏相册列表
     */
    private void hideAlbum() {
        new AnimationUtil(getApplicationContext(), R.anim.ps_anim_album_list_translate_down)
                .setLinearInterpolator().startAnimation(layoutAlbum);
        layoutAlbum.setVisibility(View.GONE);
    }

    /**
     * 清空选中的图片
     */
    private void reset() {
        mPhotoModelList.clear();
        btnOk.setText(sure);
        tvPreview.setEnabled(false);
    }

    /**
     * 点击查看照片
     */
    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        if (tvAlbum.getText().toString().equals(sRecentPhoto)) {
            bundle.putInt("position", position - 1);
        } else {
            bundle.putInt("position", position);
        }
        bundle.putString("album", tvAlbum.getText().toString());
        CommonUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
    }

    /**
     * 照片选中状态改变之后
     */
    @Override
    public void onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (!mPhotoModelList.contains(photoModel)) {
                mPhotoModelList.add(photoModel);
            }
            tvPreview.setEnabled(true);
        } else {
            mPhotoModelList.remove(photoModel);
        }

        int currentSize = mCurrentSize + mPhotoModelList.size();
        if (currentSize == 0) {
            btnOk.setText(sure);
        } else {
            btnOk.setText(sure + "(" + currentSize + "/" + sMaxImageSize + ")");
        }
        if (currentSize >= sMaxImageSize) {
            sIsFull = true;
        } else {
            sIsFull = false;
        }

        if (mPhotoModelList.isEmpty()) {
            tvPreview.setEnabled(false);
            tvPreview.setText(getString(R.string.preview));
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutAlbum.getVisibility() == View.VISIBLE) {
            hideAlbum();
        } else
            super.onBackPressed();
    }

    /**
     * 相册列表点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
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
        if (current.getName().equals(sRecentPhoto))
            mPhotoSelectorDomain.getRecent(recentListener);
        else
            mPhotoSelectorDomain.getAlbum(current.getName(), recentListener); // 获取选中相册的照片
    }

    /**
     * 获取本地图库照片回调
     */
    public interface OnLocalRecentListener {
        void onPhotoLoaded(List<PhotoModel> photos);
    }

    /**
     * 获取本地相册信息回调
     */
    public interface OnLocalAlbumListener {
        void onAlbumLoaded(List<AlbumModel> albums);
    }

    private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
        @Override
        public void onAlbumLoaded(List<AlbumModel> albums) {
            mAlbumAdapter.update(albums);
        }
    };

    private OnLocalRecentListener recentListener = new OnLocalRecentListener() {
        @Override
        public void onPhotoLoaded(List<PhotoModel> photos) {
            for (PhotoModel model : photos) {
                if (mPhotoModelList.contains(model)) {
                    model.setChecked(true);
                }
            }
            mPhotoSelectorAdapter.update(photos);
            gvPhotos.smoothScrollToPosition(0); // 滚动到顶端
            // reset(); //--keep mPhotoModelList photos

        }
    };


    private void initImageLoader() {
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_picture_loading)
                .showImageOnFail(R.drawable.ic_picture_loadfailed)
                .cacheInMemory(true).cacheOnDisk(true)
                .resetViewBeforeLoading(true).considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .memoryCacheExtraOptions(400, 400)
                        // default = device screen dimensions
                .diskCacheExtraOptions(400, 400, null)
                .threadPoolSize(5)
                        // default Thread.NORM_PRIORITY - 1
                .threadPriority(Thread.NORM_PRIORITY)
                        // default FIFO
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                        // default
                .diskCache(
                        new UnlimitedDiskCache(StorageUtils.getCacheDirectory(
                                this, true)))
                        // default
                .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                        // default
                .imageDownloader(new BaseImageDownloader(this))
                        // default
                .imageDecoder(new BaseImageDecoder(false))
                        // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        // default
                .defaultDisplayImageOptions(imageOptions).build();

        ImageLoader.getInstance().init(config);
    }
}
