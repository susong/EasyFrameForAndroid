package com.dream.photoselector.util;

/**
 * Author:      SuSong
 * Email:       751971697@qq.com | susong0618@163.com
 * GitHub:      https://github.com/susong0618
 * Date:        16/3/6 下午7:58
 * Description: EasyFrameForAndroid
 */
public interface PsConstants {

    // 请求相机
    int REQUEST_CODE_GET_PHOTO_BY_CAMERA = 100;
    // 请求相册
    int REQUEST_CODE_PHOTO_SELECTOR_ACTIVITY = 101;
    // 预览已经选择的照片
    int REQUEST_CODE_PHOTO_SELECTED_PREVIEW_ACTIVITY = 102;
    // 预览要选择的照片
    int REQUEST_CODE_PHOTO_PREVIEW_ACTIVITY = 103;
    // 预览界面点击确定按钮
    int RESULT_CODE_PHOTO_PREVIEW_ACTIVITY_CONFIRM = 104;
    // 预览界面点击返回按钮
    int RESULT_CODE_PHOTO_PREVIEW_ACTIVITY_BACK = 105;

    // 添加图标标识
    String KEY_ADD_PHOTO_PS = "KEY_ADD_PHOTO_PS";
    // 最多选择多少张
    String KEY_MAX_SIZE_PS = "KEY_MAX_SIZE_PS";
    // 当前已经选择了多少张
    String KEY_CURRENT_SIZE_PS = "KEY_CURRENT_SIZE_PS";
    // 点击的位置
    String KEY_POSITION = "KEY_POSITION";
    // 预览已经选中的照片
    String KEY_IS_SELECTED_PREVIEW = "KEY_IS_SELECTED_PREVIEW";
    // 照片集合
    String KEY_PHOTO_MODEL_LIST = "KEY_PHOTO_MODEL_LIST";
    // 照片路径集合
    String KEY_PHOTO_PATH_LIST = "KEY_PHOTO_PATH_LIST";

}
