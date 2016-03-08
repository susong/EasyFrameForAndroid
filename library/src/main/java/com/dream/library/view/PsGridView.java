package com.dream.library.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        15/10/8 下午3:57
 * Description: 解决ScrollView下嵌套ListView、GridView显示不全的问题(冲突)
 * http://blog.csdn.net/cs_li1126/article/details/12906203
 */
public class PsGridView extends GridView {
    public PsGridView(Context context) {
        super(context);
    }

    public PsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PsGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PsGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 解决ScrollView中嵌套GridView显示不正常的问题
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
