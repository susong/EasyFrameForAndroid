package com.dream.library.view;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dream.library.R;
import com.dream.library.adapter.CommonAdapter;
import com.dream.library.adapter.CommonAdapterHelper;
import com.dream.library.utils.AbListViewAndGridViewUtils;
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
 * Date:        16/2/29 下午9:26
 * Description: EasyFrameForAndroid
 */
public class PhotoSelectorSampleActivity extends AppCompatActivity {

    private ScrollView mScrollView;
    private XGridView mXGridView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selector_sample);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mXGridView = (XGridView) findViewById(R.id.gridView);

        mAdapter.add(ADD_IMG);
        mXGridView.setAdapter(mAdapter);
    }


    private CommonAdapter<String> mAdapter = new CommonAdapter<String>(PhotoSelectorSampleActivity.this, R.layout.item_publish_grid) {
        @Override
        public void convert(final CommonAdapterHelper helper, String photoPath) {
            ImageView im = helper.getView(R.id.item_grida_image);
            ImageView im_delete = helper.getView(R.id.im_delete);
            im.setLayoutParams(new RelativeLayout.LayoutParams((int) ((double) mXGridView.getWidth() / 3), (int) ((double) mXGridView.getWidth() / 3)));
            if (photoPath.equals(ADD_IMG)) {
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.add_image, im);
                im.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showSelectAblumPop();
                    }
                });
                im.setBackgroundColor(getResources().getColor(R.color.base_bg));
                im.setScaleType(ImageView.ScaleType.FIT_XY);
                im_delete.setVisibility(View.GONE);
            } else {
                im_delete.setVisibility(View.VISIBLE);
                im.setScaleType(ImageView.ScaleType.CENTER_CROP);
                im.setBackgroundColor(getResources().getColor(R.color.transparent));
                ImageLoader.getInstance().displayImage("file://" + photoPath, im);
                im.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });

                im_delete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mAdapter.remove(helper.getPosition());
                        if (mAdapter.getCount() == MAX_PIC_NUMBER - 1 && !mAdapter.getList().contains(ADD_IMG)) {
                            mAdapter.add(ADD_IMG);
                        }
                    }
                });
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_GET_IMAGE_BY_CAMERA) {
            // 拍照
            if (mAdapter.getCount() < MAX_PIC_NUMBER) {
                mAdapter.add(mAdapter.getCount() - 1, this.camreaPath);
            } else {
                mAdapter.add(mAdapter.getCount() - 1, this.camreaPath);
                mAdapter.remove(MAX_PIC_NUMBER);
            }
            AbListViewAndGridViewUtils.setGridViewHeightBasedOnChildren(mXGridView);

        } else if (requestCode == REQUEST_CODE_GET_IMAGE_BY_ALBUM) {
            // 图片库
            @SuppressWarnings("unchecked")
            List<PhotoModel> models = (List<PhotoModel>) data.getExtras().getSerializable("photos");
            List<String> images = new ArrayList<String>();
            for (PhotoModel photo : models) {
                images.add(photo.getOriginalPath());
            }
            mAdapter.addAll(mAdapter.getCount() - 1, images);
            if (mAdapter.getCount() == MAX_PIC_NUMBER + 1) {
                mAdapter.remove(MAX_PIC_NUMBER);
            }
            AbListViewAndGridViewUtils.setGridViewHeightBasedOnChildren(mXGridView);
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
            pop_select_ablum_view = View.inflate(this, R.layout.pop_select_ablum, null);
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
                    Intent intent = new Intent(PhotoSelectorSampleActivity.this, PhotoSelectorActivity.class);
                    intent.putExtra(PhotoSelectorActivity.KEY_MAX_SIZE, MAX_PIC_NUMBER);
                    intent.putExtra(PhotoSelectorActivity.KEY_CURRENT_SIZE, mAdapter.getCount() - 1);
                    startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_ALBUM);
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
            pop_select_ablum_popupWindow = new PopupWindow(this);
            pop_select_ablum_popupWindow.setAnimationStyle(R.style.anim_popup_dir);
            pop_select_ablum_popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            pop_select_ablum_popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            pop_select_ablum_popupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        pop_select_ablum_popupWindow.setContentView(pop_select_ablum_view);
        pop_select_ablum_popupWindow.showAtLocation(mXGridView, Gravity.BOTTOM, 0, 0);
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
        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_BY_CAMERA);
    }
}
