package com.yuanfen.common.utils;

import com.yuanfen.common.CommonAppContext;

/**
 * Created by cxf on 2017/8/9.
 * dp转px工具类
 */

public class DpUtil {

    private static float scale;

    static {
        scale = CommonAppContext.getInstance().getResources().getDisplayMetrics().density;
    }

    public static int dp2px(int dpVal) {
        return (int) (scale * dpVal + 0.5f);
    }
}
