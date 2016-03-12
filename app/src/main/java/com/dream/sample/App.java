package com.dream.sample;

import android.app.Application;

import com.dream.android_universal_image_loader.ImageLoaderHelper;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/3/4 上午9:23
 * Description: EasyFrameForAndroid
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "900022242", false);
        ImageLoaderHelper.init(this);
    }
}
