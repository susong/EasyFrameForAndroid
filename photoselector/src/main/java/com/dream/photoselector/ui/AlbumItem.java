package com.dream.photoselector.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dream.photoselector.R;
import com.dream.photoselector.model.AlbumModel;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AlbumItem extends LinearLayout {

    private ImageView mIvAlbum, mIvIndex;
    private TextView mTvName, mTvCount;

    public AlbumItem(Context context) {
        this(context, null);
    }

    public AlbumItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.ps_item_album, this, true);
        mIvAlbum = (ImageView) findViewById(R.id.iv_album_ps);
        mIvIndex = (ImageView) findViewById(R.id.iv_index_ps);
        mTvName = (TextView) findViewById(R.id.tv_name_ps);
        mTvCount = (TextView) findViewById(R.id.tv_count_ps);
    }

    public void update(AlbumModel album) {
        setAlbumImage(album.getRecent());
        setName(album.getName());
        setCount(album.getCount());
        isCheck(album.isCheck());
    }

    public void setAlbumImage(String path) {
        ImageLoader.getInstance().displayImage("file://" + path, mIvAlbum);
    }

    public void setName(CharSequence title) {
        mTvName.setText(title);
    }

    @SuppressLint("SetTextI18n")
    public void setCount(int count) {
        mTvCount.setText(count + getResources().getString(R.string.ps_size));
    }

    public void isCheck(boolean isCheck) {
        if (isCheck) {
            mIvIndex.setVisibility(View.VISIBLE);
        } else {
            mIvIndex.setVisibility(View.GONE);
        }
    }

}
