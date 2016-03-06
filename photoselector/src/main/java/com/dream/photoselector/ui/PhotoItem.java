package com.dream.photoselector.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dream.photoselector.R;
import com.dream.photoselector.model.PhotoModel;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author Aizaz AZ
 */

public class PhotoItem extends LinearLayout implements OnCheckedChangeListener, View.OnLongClickListener, View.OnClickListener {

    private Context mContext;
    private ImageView ivPhoto;
    private CheckBox cbPhoto;
    private onPhotoItemCheckedListener listener;
    private PhotoModel photo;
    private boolean isCheckAll;
    private onItemClickListener l;
    private int position;

    private PhotoItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public PhotoItem(Context context, onPhotoItemCheckedListener listener) {
        this(context);
        LayoutInflater.from(context).inflate(R.layout.ps_item_photo, this, true);

        ivPhoto = (ImageView) findViewById(R.id.iv_photo_ps);
        cbPhoto = (CheckBox) findViewById(R.id.cb_photo_ps);

        this.listener = listener;

        setOnClickListener(this);
        setOnLongClickListener(this);
        cbPhoto.setOnCheckedChangeListener(this); // CheckBox选中状态改变监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isCheckAll) {
            if (isChecked && PhotoSelectorActivity.mIsFull) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.ps_max_img_limit_reached), PhotoSelectorActivity.mMaxSize), Toast.LENGTH_SHORT).show();
                cbPhoto.setChecked(false);
                return;
            }
            listener.onCheckedChanged(photo, buttonView, isChecked); // 调用主界面回调函数
        }
        // 让图片变暗或者变亮
        if (isChecked) {
            setDrawingable();
            ivPhoto.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            ivPhoto.clearColorFilter();
        }
        photo.setChecked(isChecked);
    }

    /**
     * 设置路径下的图片对应的缩略图
     */
    public void setImageDrawable(final PhotoModel photo) {
        this.photo = photo;
        // You may need this setting form some custom ROM(s)
        /*
         * new Handler().postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { ImageLoader.getInstance().displayImage(
		 * "file://" + photo.getOriginalPath(), ivPhoto); } }, new
		 * Random().nextInt(10));
		 */

        ImageLoader.getInstance().displayImage("file://" + photo.getOriginalPath(), ivPhoto);
    }

    private void setDrawingable() {
        ivPhoto.setDrawingCacheEnabled(true);
        ivPhoto.buildDrawingCache();
    }

    @Override
    public void setSelected(boolean selected) {
        if (photo == null) {
            return;
        }
        isCheckAll = true;
        cbPhoto.setChecked(selected);
        isCheckAll = false;
    }

    public void setOnClickListener(onItemClickListener l, int position) {
        this.l = l;
        this.position = position;
    }


    /**
     * 图片Item选中事件监听器
     */
    public interface onPhotoItemCheckedListener {
        void onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked);
    }

    /**
     * 图片点击事件
     */
    public interface onItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 点击
     */
    @Override
    public void onClick(View v) {
        if (l != null) {
            l.onItemClick(position);
        }
    }

    /**
     * 长按
     */
    @Override
    public boolean onLongClick(View v) {
        if (l != null) {
            l.onItemClick(position);
        }
        return true;
    }

}
