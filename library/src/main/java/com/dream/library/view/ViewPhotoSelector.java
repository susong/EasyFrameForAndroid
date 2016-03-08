package com.dream.library.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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

import com.dream.library.R;
import com.dream.photoselector.model.PhotoModel;
import com.dream.photoselector.ui.PhotoSelectorActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/3/8 上午12:12
 * Description: EasyFrameForAndroid
 */
public class ViewPhotoSelector {

    private Activity mActivity;
    private Context mContext;
    private GridView mGridView;
    private List<String> mList;
    private ViewPhotoSelectorAdapter mAdapter;

    public ViewPhotoSelector(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            throw new ClassCastException("必须是Activity的上下文");
        }
        init();
    }

    private void init() {
        mList = new ArrayList<>();
        mGridView = (GridView) mActivity.getWindow().findViewById(R.id.gridView);
        mList.add(ADD_IMG);
        mAdapter = new ViewPhotoSelectorAdapter(mContext, mList);
        mGridView.setAdapter(mAdapter);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_GET_IMAGE_BY_CAMERA) {
            // 拍照
            if (mAdapter.getCount() < MAX_PIC_NUMBER) {
                mList.add(mAdapter.getCount() - 1, this.camreaPath);
            } else {
                mList.add(mAdapter.getCount() - 1, this.camreaPath);
                mList.remove(MAX_PIC_NUMBER);
            }
            mAdapter.notifyDataSetChanged();

        } else if (requestCode == REQUEST_CODE_GET_IMAGE_BY_ALBUM) {
            // 图片库
            @SuppressWarnings("unchecked")
            List<PhotoModel> models = intent.getParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_LIST);
            List<String> images = new ArrayList<String>();
            for (PhotoModel photo : models) {
                images.add(photo.getOriginalPath());
            }
            mList.addAll(mAdapter.getCount() - 1, images);
            if (mAdapter.getCount() == MAX_PIC_NUMBER + 1) {
                mList.remove(MAX_PIC_NUMBER);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    //==============================================================================================

    private PopupWindow pop_select_ablum_popupWindow;
    private View pop_select_ablum_view;
    private TextView tv_camrea;
    private TextView tv_alubm;
    private TextView tv_cancel;


    /**
     * 弹出选择窗口
     */
    private void showSelectAblumPop() {
        if (pop_select_ablum_view == null) {
            pop_select_ablum_view = View.inflate(mContext, R.layout.pop_select_ablum, null);
            tv_camrea = (TextView) pop_select_ablum_view.findViewById(R.id.tv_camrea);
            tv_alubm = (TextView) pop_select_ablum_view.findViewById(R.id.tv_alubm);
            tv_cancel = (TextView) pop_select_ablum_view.findViewById(R.id.tv_cancel);

            tv_camrea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCamrea();
                    pop_select_ablum_popupWindow.dismiss();
                }
            });
            tv_alubm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PhotoSelectorActivity.class);
                    intent.putExtra(PhotoSelectorActivity.KEY_MAX_SIZE, MAX_PIC_NUMBER);
                    intent.putExtra(PhotoSelectorActivity.KEY_CURRENT_SIZE, mAdapter.getCount() - 1);
                    mActivity.startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_ALBUM);
                    pop_select_ablum_popupWindow.dismiss();
                }
            });
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop_select_ablum_popupWindow.dismiss();
                }
            });
        }

        if (pop_select_ablum_popupWindow == null) {
            pop_select_ablum_popupWindow = new PopupWindow(mContext);
            pop_select_ablum_popupWindow.setAnimationStyle(R.style.anim_popup_dir);
            pop_select_ablum_popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            pop_select_ablum_popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            pop_select_ablum_popupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        pop_select_ablum_popupWindow.setContentView(pop_select_ablum_view);
        //防止虚拟软键盘被弹出菜单遮住
        pop_select_ablum_popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        pop_select_ablum_popupWindow.showAtLocation(mGridView, Gravity.BOTTOM, 0, 0);
        pop_select_ablum_popupWindow.update();
    }


    //==============================================================================================

    /**
     * 请求相册
     */
    public static final int REQUEST_CODE_GET_IMAGE_BY_ALBUM = 0;
    /**
     * 请求相机
     */
    public static final int REQUEST_CODE_GET_IMAGE_BY_CAMERA = 1;
    private static final int MAX_PIC_NUMBER = 9;
    private static final String ADD_IMG = "add_img";


    /**
     * 拍照过后照片的绝对路径
     */
    private String camreaPath;


    /**
     * 启动相机
     */
    private void startCamrea() {
        Intent intent;
        // 判断是否挂载了SD卡
        String savePath = "";
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ksky/Camera/";
            File savedir = new File(savePath);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        }

        // 没有挂载SD卡，无法保存文件
        if (TextUtils.isEmpty(savePath)) {
//            ToastUtils.show(mContext, "无法保存照片，请检查SD卡是否挂载");
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = "ksky" + timeStamp + ".jpg";// 照片命名
        File out = new File(savePath, fileName);
        Uri uri = Uri.fromFile(out);

        camreaPath = savePath + fileName;// 该照片的绝对路径

        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mActivity.startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_CAMERA);
    }


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
                convertView = mInflater.inflate(R.layout.item_publish_grid, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.ivPhoto = (ImageView) convertView.findViewById(R.id.item_grida_image);
                viewHolder.ivDelete = (ImageView) convertView.findViewById(R.id.im_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String photoPath = mList.get(position);
            viewHolder.ivPhoto.setLayoutParams(new RelativeLayout.LayoutParams((int) ((double) mGridView.getWidth() / 3), (int) ((double) mGridView.getWidth() / 3)));
            if (photoPath.equals(ADD_IMG)) {
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.add_image, viewHolder.ivPhoto);
                viewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showSelectAblumPop();
                    }
                });
                viewHolder.ivPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.base_bg));
                viewHolder.ivPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                viewHolder.ivDelete.setVisibility(View.GONE);
            } else {
                viewHolder.ivDelete.setVisibility(View.VISIBLE);
                viewHolder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.ivPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                ImageLoader.getInstance().displayImage("file://" + photoPath, viewHolder.ivPhoto);
                viewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });

                viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mList.remove(position);
                        if (mAdapter.getCount() == MAX_PIC_NUMBER - 1 && !mList.contains(ADD_IMG)) {
                            mList.add(ADD_IMG);
                        }
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
}
