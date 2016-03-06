package com.dream.photoselector.ui;

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

    private Context mContext;
    private ImageView ivAlbum, ivIndex;
    private TextView tvName, tvCount;

    public AlbumItem(Context context) {
        this(context, null);
    }

    public AlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.item_album, this, true);

        ivAlbum = (ImageView) findViewById(R.id.iv_album_ps);
        ivIndex = (ImageView) findViewById(R.id.iv_index_ps);
        tvName = (TextView) findViewById(R.id.tv_name_ps);
        tvCount = (TextView) findViewById(R.id.tv_count_ps);
    }

    public AlbumItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void setAlbumImage(String path) {
        ImageLoader.getInstance().displayImage("file://" + path, ivAlbum);
    }

    public void update(AlbumModel album) {
        setAlbumImage(album.getRecent());
        setName(album.getName());
        setCount(album.getCount());
        isCheck(album.isCheck());
    }

    public void setName(CharSequence title) {
        tvName.setText(title);
    }

    public void setCount(int count) {
        tvCount.setHint(count + "张");
    }

    public void isCheck(boolean isCheck) {
        if (isCheck)
            ivIndex.setVisibility(View.VISIBLE);
        else
            ivIndex.setVisibility(View.GONE);
    }

}
