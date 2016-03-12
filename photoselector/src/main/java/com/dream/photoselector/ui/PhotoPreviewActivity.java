package com.dream.photoselector.ui;
/**
 * @author Aizaz AZ
 */

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.photoselector.R;
import com.dream.photoselector.model.PhotoModel;
import com.dream.photoselector.util.PsAnimationUtils;
import com.dream.photoselector.util.PsConstants;
import com.dream.photoselector.util.PsLog;

import java.util.ArrayList;
import java.util.List;


public class PhotoPreviewActivity extends Activity {

    private ViewPager mViewPager;
    private RelativeLayout mPhotoPreviewToolbar;
    private RelativeLayout mPhotoPreviewBottomToolbar;
    private ImageButton mIbBack;
    private TextView mTvPercent;
    private CheckBox mCbCheck;
    private Button mBtnConfirm;
    private int mCurrentPosition = 0;
    private boolean mIsToolbarShow = true;
    private String mConfirmStr;

    private int mMaxSize = 0;
    private int mCurrentSize = 0;
    private List<PhotoModel> mPhotoModelList = new ArrayList<PhotoModel>();
    private List<PhotoModel> mPhotoModelSelectorList = new ArrayList<PhotoModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.ps_activity_photo_preview);
        mPhotoPreviewToolbar = (RelativeLayout) findViewById(R.id.photo_preview_toolbar_ps);
        mPhotoPreviewBottomToolbar = (RelativeLayout) findViewById(R.id.photo_preview_bottom_toolbar_ps);
        mViewPager = (ViewPager) findViewById(R.id.view_pager_ps);
        mIbBack = (ImageButton) findViewById(R.id.ib_back_ps);
        mTvPercent = (TextView) findViewById(R.id.tv_percent_ps);
        mCbCheck = (CheckBox) findViewById(R.id.cb_check_ps);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm_ps);


        mConfirmStr = getResources().getString(R.string.ps_confirm);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMaxSize = getIntent().getIntExtra(PhotoSelectorActivity.KEY_MAX_SIZE, 0);
            mCurrentSize = getIntent().getIntExtra(PhotoSelectorActivity.KEY_CURRENT_SIZE, 0);
            mCurrentPosition = extras.getInt(PsConstants.KEY_POSITION, 0);
//            mPhotoModelList = extras.getParcelableArrayList(PhotoSelectorActivity.KEY_PHOTO_LIST);
//            mPhotoModelSelectorList = extras.getParcelableArrayList(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST);
            boolean is_selected_preview = extras.getBoolean(PhotoSelectorActivity.KEY_IS_SELECTED_PREVIEW, false);
            if (is_selected_preview) {
                mPhotoModelList.addAll(PhotoSelectorActivity.mPhotoModelSelectorList);
            } else {
                mPhotoModelList.addAll(PhotoSelectorActivity.mPhotoModelList);
            }
            mPhotoModelSelectorList.addAll(PhotoSelectorActivity.mPhotoModelSelectorList);

            mTvPercent.setText((mCurrentPosition + 1) + "/" + mPhotoModelList.size());

            mCbCheck.setChecked(mPhotoModelList.get(mCurrentPosition).isChecked());

            int size = mCurrentSize + mPhotoModelSelectorList.size();
            mBtnConfirm.setText(mConfirmStr + "(" + size + "/" + mMaxSize + ")");

            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(mCurrentPosition);
            mViewPager.setOffscreenPageLimit(mMaxSize);
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mTvPercent.setText((mCurrentPosition + 1) + "/" + mPhotoModelList.size());
                PsLog.d(mPhotoModelList.get(mCurrentPosition).toString());
                mCbCheck.setChecked(mPhotoModelList.get(mCurrentPosition).isChecked());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
                setResult(PsConstants.PHOTO_PREVIEW_RESULT_CODE_BACK, intent);
                finish();
            }
        });

        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
                setResult(PsConstants.PHOTO_PREVIEW_RESULT_CODE_CONFIRM, intent);
                finish();
            }
        });

        mCbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PhotoModel photoModel = mPhotoModelList.get(mCurrentPosition);
                photoModel.setChecked(isChecked);
                if (isChecked) {
                    if (!mPhotoModelSelectorList.contains(photoModel)) {
                        if (mCurrentSize + mPhotoModelSelectorList.size() < mMaxSize) {
                            mPhotoModelSelectorList.add(photoModel);
                        } else {
                            Toast.makeText(PhotoPreviewActivity.this, String.format(getString(R.string.ps_max_img_limit_reached), mMaxSize), Toast.LENGTH_SHORT).show();
                            photoModel.setChecked(false);
                            mCbCheck.setChecked(false);
                        }
                    }
                    int size = mCurrentSize + mPhotoModelSelectorList.size();
                    mBtnConfirm.setText(mConfirmStr + "(" + size + "/" + mMaxSize + ")");
                } else {
                    if (mPhotoModelSelectorList.contains(photoModel)) {
                        mPhotoModelSelectorList.remove(photoModel);
                    }
                    int size = mCurrentSize + mPhotoModelSelectorList.size();
                    mBtnConfirm.setText(mConfirmStr + "(" + size + "/" + mMaxSize + ")");
                }
                PsLog.d(photoModel.toString());
            }
        });

        overridePendingTransition(R.anim.ps_anim_activity_alpha_action_in, 0); // 渐入效果
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(PhotoSelectorActivity.KEY_PHOTO_SELECTOR_LIST, (ArrayList<PhotoModel>) mPhotoModelSelectorList);
        setResult(PsConstants.PHOTO_PREVIEW_RESULT_CODE_BACK, intent);
        finish();
    }

    /**
     * 图片点击事件回调
     */
    private View.OnClickListener photoItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsToolbarShow) {
                full(true);
                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_toolbar_translate_hidde)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewToolbar);

                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_bottom_toolbar_translate_hidde)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewBottomToolbar);
                mIsToolbarShow = false;
            } else {
                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_toolbar_translate_show)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewToolbar);

                new PsAnimationUtils(getApplicationContext(), R.anim.ps_anim_preview_bottom_toolbar_translate_show)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(mPhotoPreviewBottomToolbar);
                mIsToolbarShow = true;
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
            if (mPhotoModelList == null) {
                return 0;
            } else {
                return mPhotoModelList.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(getApplicationContext());
            container.addView(photoPreview);
            PhotoModel photoModel = mPhotoModelList.get(position);
            photoPreview.loadImage(photoModel);
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

    };

}
