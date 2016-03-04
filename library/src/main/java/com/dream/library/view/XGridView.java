package com.dream.library.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by susong on 15/11/18.
 */
public class XGridView extends GridView {
    public XGridView(Context context) {
        super(context);
    }

    public XGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public XGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * ListView嵌套GridView显示不全解决方法
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


    //*******************************************************************************************


    /**
     * GridView点击空白地方事件扩展
     */
    private OnTouchInvalidPositionListener mOnTouchInvalidPositionListener;

    public interface OnTouchInvalidPositionListener {
        /**
         * motionEvent 可使用 MotionEvent.ACTION_DOWN 或者 MotionEvent.ACTION_UP等来按需要进行判断
         *
         * @return 是否要终止事件的路由
         */
        boolean onTouchInvalidPosition();
    }

    /**
     * 点击空白区域时的响应和处理接口
     */
    public void setOnTouchInvalidPositionListener(OnTouchInvalidPositionListener listener) {
        mOnTouchInvalidPositionListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mOnTouchInvalidPositionListener == null) {
            return super.onTouchEvent(event);
        }

        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return isClickable() || isLongClickable();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());

            if (motionPosition == INVALID_POSITION) {
                super.onTouchEvent(event);
                return mOnTouchInvalidPositionListener.onTouchInvalidPosition();
            }
        }

        return super.onTouchEvent(event);
    }

}
