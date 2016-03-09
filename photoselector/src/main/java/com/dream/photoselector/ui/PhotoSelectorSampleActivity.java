package com.dream.photoselector.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dream.photoselector.R;

import java.util.List;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/2/29 下午9:26
 * Description: EasyFrameForAndroid
 */
public class PhotoSelectorSampleActivity extends AppCompatActivity {

    private ViewPhotoSelector mViewPhotoSelector;
    private static final int MAX_PIC_NUMBER = 9;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ps_activity_photo_selector_sample);
        mViewPhotoSelector = new ViewPhotoSelector(this, MAX_PIC_NUMBER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (mViewPhotoSelector.onActivityResult(requestCode, resultCode, intent)) {
            List<String> data = mViewPhotoSelector.getData();
        }
    }
}
