package com.dream.photoselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dream.photoselector.R;
import com.dream.photoselector.util.PsAnimationUtils;
import com.dream.photoselector.util.PsConstants;
import com.dream.photoselector.util.PsLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/3/12 下午5:27
 * Description: EasyFrameForAndroid
 */
public class PhotoSelectedPreviewActivity extends Activity {

    private ViewPager mViewPager;
    private ImageButton mIbBack;
    private TextView mTvPercent;
    private ImageButton mIbDelete;
    private RelativeLayout mPhotoPreviewToolbar;
    private boolean mToolbarIsShowing = true;
    private int mCurrentPosition = 0;
    private List<String> mPhotoPathList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.ps_activity_selected_photo_preview);

        mPhotoPreviewToolbar = (RelativeLayout) findViewById(R.id.photo_preview_toolbar_ps);
        mViewPager = (ViewPager) findViewById(R.id.view_pager_ps);
        mIbBack = (ImageButton) findViewById(R.id.ib_back_ps);
        mTvPercent = (TextView) findViewById(R.id.tv_percent_ps);
        mIbDelete = (ImageButton) findViewById(R.id.ib_delete_ps);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ArrayList<String> list = extras.getStringArrayList(PsConstants.KEY_PHOTO_PATH_LIST);
            if (list != null) {
                mPhotoPathList.addAll(list);
            }
            mCurrentPosition = extras.getInt(PsConstants.KEY_POSITION, 0);
            mTvPercent.setText((mCurrentPosition + 1) + "/" + mPhotoPathList.size());
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(mCurrentPosition);
            mViewPager.setOffscreenPageLimit(mPhotoPathList.size());
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mTvPercent.setText((mCurrentPosition + 1) + "/" + mPhotoPathList.size());
                PsLog.d(mPhotoPathList.get(mCurrentPosition));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mIbDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoPathList.size() > 1) {
                    mPhotoPathList.remove(mCurrentPosition);
                    if (mCurrentPosition >= mPhotoPathList.size()) {
                        mCurrentPosition = mPhotoPathList.size() - 1;
                    }
                    mTvPercent.setText((mCurrentPosition + 1) + "/" + mPhotoPathList.size());
                    mPagerAdapter.notifyDataSetChanged();
                } else {
                    mPhotoPathList.remove(mCurrentPosition);
                    setResult();
                    finish();
                }
            }
        });

        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult();
                finish();
            }
        });

        // 渐入效果
        overridePendingTransition(R.anim.ps_anim_activity_alpha_action_in, 0);
    }

    private void setResult() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(PsConstants.KEY_PHOTO_PATH_LIST, (ArrayList<String>) mPhotoPathList);
        setResult(RESULT_OK, intent);
    }

    /**
     * 图片点击事件回调
     */
    private View.OnClickListener photoItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mToolbarIsShowing) {
                full(true);
                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_toolbar_translate_hidde)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewToolbar);

                mToolbarIsShowing = false;
            } else {
                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_toolbar_translate_show)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewToolbar);

                mToolbarIsShowing = true;
                full(false);
            }
        }
    };

    /**
     * android 动态控制状态栏显示和隐藏的方法实例
     *
     * @param enable 是否全屏
     */
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (mPhotoPathList == null) {
                return 0;
            } else {
                return mPhotoPathList.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(getApplicationContext());
            container.addView(photoPreview);
            String path = mPhotoPathList.get(position);
            photoPreview.loadImage("file://" + path);
            photoPreview.setOnClickListener(photoItemClickListener);
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // 解决Viewpager在调用notifyDataSetChanged()时，界面无刷新
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    };
}
