package com.dream.photoselector.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aizaz
 */


public class MBaseAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> mList;

    public MBaseAdapter(Context context, List<T> list) {
        this.mContext = context;
        if (list == null) {
            this.mList = new ArrayList<T>();
        } else {
            this.mList = list;
        }
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * 更新数据
     */
    public void updateAdapter(List<T> models) {
        if (models == null) {
            return;
        }
        this.mList.clear();
        for (T t : models) {
            this.mList.add(t);
        }
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return mList;
    }

}
