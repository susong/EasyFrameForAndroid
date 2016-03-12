package com.dream.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dream.photoselector.ui.PhotoSelectorSampleActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_photo_selector)
    Button mBtnPhotoSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({
            R.id.btn_photo_selector
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_photo_selector:
                Intent intent = new Intent(this, PhotoSelectorSampleActivity.class);
                startActivity(intent);
                break;
        }
    }
}
