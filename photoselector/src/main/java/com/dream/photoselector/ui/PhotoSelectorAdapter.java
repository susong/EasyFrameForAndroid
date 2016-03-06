package com.dream.photoselector.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;

import com.dream.photoselector.R;
import com.dream.photoselector.model.PhotoModel;


/**
 * @author Aizaz AZ
 */


public class PhotoSelectorAdapter extends PsBaseAdapter<PhotoModel> {

    private int itemWidth;
    private int horizontalNum = 3;
    private PhotoItem.onPhotoItemCheckedListener mOnPhotoItemCheckedListener;
    private LayoutParams itemLayoutParams;
    private PhotoItem.onItemClickListener mOnItemClickListener;
    private OnClickListener mCameraOnClickListener;

    private PhotoSelectorAdapter(Context context) {
        super(context, null);
    }

    public PhotoSelectorAdapter(Context context, int screenWidth,
                                PhotoItem.onPhotoItemCheckedListener onPhotoItemCheckedListener,
                                PhotoItem.onItemClickListener onItemClickListener,
                                OnClickListener cameraOnClickListener) {
        this(context);
        setItemWidth(screenWidth);
        this.mOnPhotoItemCheckedListener = onPhotoItemCheckedListener;
        this.mOnItemClickListener = onItemClickListener;
        this.mCameraOnClickListener = cameraOnClickListener;
    }

    /**
     * 设置每一个Item的宽高
     */
    public void setItemWidth(int screenWidth) {
        int horizontalSpace = mContext.getResources().getDimensionPixelSize(R.dimen.ps_sticky_item_horizontalSpacing);
        this.itemWidth = (screenWidth - (horizontalSpace * (horizontalNum - 1))) / horizontalNum;
        this.itemLayoutParams = new LayoutParams(itemWidth, itemWidth);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoItem item = null;
        if (convertView == null || !(convertView instanceof PhotoItem)) {
            item = new PhotoItem(mContext, mOnPhotoItemCheckedListener);
            item.setLayoutParams(itemLayoutParams);
            convertView = item;
        } else {
            item = (PhotoItem) convertView;
        }
        item.setImageDrawable(mList.get(position));
        item.setSelected(mList.get(position).isChecked());
        item.setOnClickListener(mOnItemClickListener, position);
        return convertView;
    }
}
