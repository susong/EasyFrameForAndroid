package com.dream.photoselector.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dream.photoselector.model.AlbumModel;


public class AlbumAdapter extends PsBaseAdapter<AlbumModel> {

    public AlbumAdapter(Context context) {
        super(context, null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumItem albumItem = null;
        if (convertView == null) {
            albumItem = new AlbumItem(mContext);
            convertView = albumItem;
        } else {
            albumItem = (AlbumItem) convertView;
        }
        albumItem.update(mList.get(position));
        return convertView;
    }

}
