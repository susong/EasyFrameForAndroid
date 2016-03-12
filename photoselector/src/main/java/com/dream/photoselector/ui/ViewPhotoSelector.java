package com.dream.photoselector.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.photoselector.R;
import com.dream.photoselector.model.PhotoModel;
import com.dream.photoselector.util.PsCommonUtils;
import com.dream.photoselector.util.PsConstants;
import com.dream.photoselector.util.PsLog;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/3/8 上午12:12
 * Description: EasyFrameForAndroid
 */
public class ViewPhotoSelector {

    // 请求相册
    public static final int REQUEST_CODE_GET_IMAGE_BY_ALBUM = 0;
    // 请求相机
    public static final int REQUEST_CODE_GET_IMAGE_BY_CAMERA = 1;
    // 添加图标标识
    private static final String ADD_IMG = "add_img";
    private Activity mActivity;
    private Context mContext;
    private GridView mGridView;
    private List<String> mList;
    private ViewPhotoSelectorAdapter mAdapter;
    private int mMaxPicNumber = 9;
    private boolean mIsAppendFirst = false;

    public ViewPhotoSelector(Context context, int maxPicNumber) {
        this(context, maxPicNumber, false);
    }

    public ViewPhotoSelector(Context context, int maxPicNumber, boolean isAppendFirst) {
        mContext = context;
        mMaxPicNumber = maxPicNumber;
        mIsAppendFirst = isAppendFirst;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            throw new ClassCastException("必须是Activity的上下文");
        }
        initImageLoader();
        init();
    }

    private void init() {
        mList = new ArrayList<>();
        mGridView = (GridView) mActivity.getWindow().findViewById(R.id.gridView);
        mList.add(ADD_IMG);
        mAdapter = new ViewPhotoSelectorAdapter(mContext, mList);
        mGridView.setAdapter(mAdapter);
    }

    public List<String> getData() {
        List<String> list = new ArrayList<>();
        list.addAll(mList);
        if (list.contains(ADD_IMG)) {
            list.remove(ADD_IMG);
        }
        PsLog.d(list.toString());
        return list;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGE_BY_CAMERA) {
            PsLog.d("from camera : " + this.mCameraPhotoAbsolutePath);
            // 拍照
            if (mIsAppendFirst) {
                mList.add(0, this.mCameraPhotoAbsolutePath);
            } else {
                mList.add(mAdapter.getCount() - 1, this.mCameraPhotoAbsolutePath);
            }
            if (mAdapter.getCount() > mMaxPicNumber) {
                mList.remove(mMaxPicNumber);
            }
            mAdapter.notifyDataSetChanged();
            return true;
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGE_BY_ALBUM) {
            // 图片库
            List<PhotoModel> photoModelList = intent.getParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_LIST);
            List<String> photoPathList = new ArrayList<String>();
            for (PhotoModel photo : photoModelList) {
                photoPathList.add(photo.getOriginalPath());
            }
            if (mIsAppendFirst) {
                mList.addAll(0, photoPathList);
            } else {
                mList.addAll(mAdapter.getCount() - 1, photoPathList);
            }
            if (mAdapter.getCount() > mMaxPicNumber) {
                mList.remove(mMaxPicNumber);
            }
            mAdapter.notifyDataSetChanged();
            return true;
        } else if (resultCode == PsConstants.RESULT_CODE_PHOTO_SELECTED_PREVIEW_ACTIVITY &&
                requestCode == PsConstants.REQUEST_CODE_PHOTO_SELECTED_PREVIEW_ACTIVITY) {
            List<String> photoPathList = intent.getStringArrayListExtra(PsConstants.PHOTO_SELECTED_LIST);
            mList.clear();
            mList.addAll(photoPathList);
            if (mList.size() < mMaxPicNumber) {
                mList.add(ADD_IMG);
            }
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    //==============================================================================================


    // 拍照过后照片的绝对路径
    private String mCameraPhotoAbsolutePath;

    /**
     * 启动相机
     */
    private void startCamera() {
        // 判断是否挂载了SD卡
        String savePath = "";
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
        }
        // 没有挂载SD卡，无法保存文件
        if (TextUtils.isEmpty(savePath)) {
            Toast.makeText(mContext, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = "PhotoSelector" + timeStamp + ".jpg";// 照片命名
        mCameraPhotoAbsolutePath = savePath + fileName;// 该照片的绝对路径
        File out = new File(savePath, fileName);
        Uri uri = Uri.fromFile(out);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mActivity.startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_CAMERA);
    }

    //==============================================================================================

    private PopupWindow mPopSelectAlbumPopupWindow;
    private View mPopSelectAlbumView;
    private TextView mTvCamera;
    private TextView mTvAlbum;
    private TextView mTvCancel;


    /**
     * 弹出选择窗口
     */
    private void showSelectAlbumPop() {
        if (mPopSelectAlbumView == null) {
            mPopSelectAlbumView = View.inflate(mContext, R.layout.ps_pop_selector_album, null);
            mTvCamera = (TextView) mPopSelectAlbumView.findViewById(R.id.tv_camera);
            mTvAlbum = (TextView) mPopSelectAlbumView.findViewById(R.id.tv_album);
            mTvCancel = (TextView) mPopSelectAlbumView.findViewById(R.id.tv_cancel);

            mTvCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCamera();
                    mPopSelectAlbumPopupWindow.dismiss();
                }
            });
            mTvAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PhotoSelectorActivity.class);
                    intent.putExtra(PhotoSelectorActivity.KEY_MAX_SIZE, mMaxPicNumber);
                    intent.putExtra(PhotoSelectorActivity.KEY_CURRENT_SIZE, mAdapter.getCount() - 1);
                    mActivity.startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_ALBUM);
                    mPopSelectAlbumPopupWindow.dismiss();
                }
            });
            mTvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopSelectAlbumPopupWindow.dismiss();
                }
            });
        }

        if (mPopSelectAlbumPopupWindow == null) {
            mPopSelectAlbumPopupWindow = new PopupWindow(mContext);
            mPopSelectAlbumPopupWindow.setAnimationStyle(R.style.PsPopupWindowAnimal);
            mPopSelectAlbumPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopSelectAlbumPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopSelectAlbumPopupWindow.setBackgroundDrawable(new ColorDrawable());
//            mPopSelectAlbumPopupWindow.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.ps_transparent_gray)));
        }
        mPopSelectAlbumPopupWindow.setContentView(mPopSelectAlbumView);
        //防止虚拟软键盘被弹出菜单遮住
        mPopSelectAlbumPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopSelectAlbumPopupWindow.showAtLocation(mGridView, Gravity.BOTTOM, 0, 0);
        mPopSelectAlbumPopupWindow.update();
    }


    //==============================================================================================

    public class ViewPhotoSelectorAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<String> mList;

        public ViewPhotoSelectorAdapter(Context context, List<String> list) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mList = list;
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.ps_item_view_photo_selector, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_photo_ps);
                viewHolder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String photoPath = mList.get(position);

            int columnWidth = getColumnWidth(mGridView);
            viewHolder.ivPhoto.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));
            if (photoPath.equals(ADD_IMG)) {
                viewHolder.ivDelete.setVisibility(View.GONE);
                viewHolder.ivPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                viewHolder.ivPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.ps_base_background));
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.ps_add_photo, viewHolder.ivPhoto);
                viewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showSelectAlbumPop();
                    }
                });
            } else {
                viewHolder.ivDelete.setVisibility(View.VISIBLE);
                viewHolder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.ivPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.ps_transparent));
                ImageLoader.getInstance().displayImage("file://" + photoPath, viewHolder.ivPhoto);
                viewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(PsConstants.KEY_POSITION, position);
                        bundle.putStringArrayList(PsConstants.PHOTO_SELECTED_LIST, (ArrayList<String>) getData());
                        PsCommonUtils.launchActivityForResult(mActivity, PhotoSelectedPreviewActivity.class, PsConstants.REQUEST_CODE_PHOTO_SELECTED_PREVIEW_ACTIVITY, bundle);
                    }
                });

                viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String remove = mList.remove(position);
                        PsLog.d("Remove photo : " + remove);
                        if (mAdapter.getCount() == mMaxPicNumber - 1 && !mList.contains(ADD_IMG)) {
                            mList.add(ADD_IMG);
                        }
                        PsLog.d(mList.toString());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }

        private class ViewHolder {
            ImageView ivPhoto;
            ImageView ivDelete;
        }
    }

    private int getColumnWidth(GridView gridView) {
        int numColumns = gridView.getNumColumns();
        int paddingLeft = gridView.getPaddingLeft();
        int paddingRight = gridView.getPaddingRight();
        int horizontalSpacing = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            horizontalSpacing = gridView.getHorizontalSpacing();
        } else {
            try {
                Field field = gridView.getClass().getDeclaredField("mHorizontalSpacing");
                field.setAccessible(true);
                horizontalSpacing = field.getInt(gridView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (int) (((double) gridView.getWidth() - ((numColumns - 1) * horizontalSpacing) - paddingLeft - paddingRight) / 3);
    }

    private void initImageLoader() {
        if (ImageLoader.getInstance().isInited()) {
            return;
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ps_picture_loading)
                .showImageOnFail(R.drawable.ps_picture_loadfailed)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .memoryCacheExtraOptions(400, 400)
                .diskCacheExtraOptions(400, 400, null)
                .threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                .diskCache(new UnlimitedDiskCache(StorageUtils.getCacheDirectory(mContext, true)))
//                .diskCacheSize(50 * 1024 * 1024)
//                .diskCacheFileCount(100)
//                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(mContext))
                .imageDecoder(new BaseImageDecoder(false))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .defaultDisplayImageOptions(imageOptions).build();
        ImageLoader.getInstance().init(config);
    }
}
