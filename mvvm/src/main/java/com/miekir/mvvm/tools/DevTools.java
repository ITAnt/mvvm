package com.miekir.mvvm.tools;

import android.content.pm.ApplicationInfo;

import com.miekir.mvvm.context.GlobalContext;

/**
 * @author : 詹子聪
 * 系统工具
 * @date : 2021-6-3 22:21
 */
public final class DevTools {
    private DevTools() {}

    /**
     * @return 是否是debug模式
     */
    public static boolean isDebug() {
        if (GlobalContext.getContext() == null) {
            return false;
        }
        if (isSpace(GlobalContext.getContext().getPackageName())) {
            return false;
        } else {
            ApplicationInfo ai = GlobalContext.getContext().getApplicationInfo();
            return ai != null && (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
    }

    private static boolean isSpace(String s) {
        if (s != null) {
            int i = 0;
            for (int len = s.length(); i < len; ++i) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
