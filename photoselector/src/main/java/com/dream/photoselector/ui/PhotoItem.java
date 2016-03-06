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
    private ImageView mIvPhoto;
    private CheckBox mCbPhoto;
    private onPhotoItemCheckedListener mOnPhotoItemCheckedListener;
    private PhotoModel mPhotoModel;
    private boolean mIsCheckAll;
    private onItemClickListener mOnItemClickListener;
    private int mPosition;

    private PhotoItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public PhotoItem(Context context, onPhotoItemCheckedListener onPhotoItemCheckedListener) {
        this(context);
        LayoutInflater.from(context).inflate(R.layout.ps_item_photo, this, true);

        mIvPhoto = (ImageView) findViewById(R.id.iv_photo_ps);
        mCbPhoto = (CheckBox) findViewById(R.id.cb_photo_ps);

        this.mOnPhotoItemCheckedListener = onPhotoItemCheckedListener;

        setOnClickListener(this);
        setOnLongClickListener(this);
        mCbPhoto.setOnCheckedChangeListener(this); // CheckBox选中状态改变监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!mIsCheckAll) {
            if (isChecked && PhotoSelectorActivity.mIsFull) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.ps_max_img_limit_reached), PhotoSelectorActivity.mMaxSize), Toast.LENGTH_SHORT).show();
                mCbPhoto.setChecked(false);
                return;
            }
            mOnPhotoItemCheckedListener.onCheckedChanged(mPhotoModel, buttonView, isChecked); // 调用主界面回调函数
        }
        // 让图片变暗或者变亮
        if (isChecked) {
            mIvPhoto.setDrawingCacheEnabled(true);
            mIvPhoto.buildDrawingCache();
            mIvPhoto.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            mIvPhoto.clearColorFilter();
        }
        mPhotoModel.setChecked(isChecked);
    }

    /**
     * 设置路径下的图片对应的缩略图
     */
    public void setImageDrawable(final PhotoModel photo) {
        this.mPhotoModel = photo;
        // You may need this setting form some custom ROM(s)
        /*
         * new Handler().postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { ImageLoader.getInstance().displayImage(
		 * "file://" + mPhotoModel.getOriginalPath(), mIvPhoto); } }, new
		 * Random().nextInt(10));
		 */

        ImageLoader.getInstance().displayImage("file://" + photo.getOriginalPath(), mIvPhoto);
    }

    @Override
    public void setSelected(boolean selected) {
        if (mPhotoModel == null) {
            return;
        }
        mIsCheckAll = true;
        mCbPhoto.setChecked(selected);
        mIsCheckAll = false;
    }

    public void setOnClickListener(onItemClickListener l, int position) {
        this.mOnItemClickListener = l;
        this.mPosition = position;
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
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(mPosition);
        }
    }

    /**
     * 长按
     */
    @Override
    public boolean onLongClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(mPosition);
        }
        return true;
    }

}
