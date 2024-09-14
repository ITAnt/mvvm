package com.miekir.mt.utils;

import android.content.Context;

import com.miekir.mvvm.context.GlobalContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author Miekir
 * @date 2020/7/11 21:59
 * Description: 字符串相关工具
 */
public class FormatUtils {
    private FormatUtils() {
    }

    private static final DecimalFormat FORMAT_TWO = new DecimalFormat("#.00");

    public static String getShowMoney(double number) {
        return FORMAT_TWO.format(number);
    }

    /**
     * 分转元，保留2位小数
     */
    public static String longCent2Yuan(long cent) {
        BigDecimal centDecimal = BigDecimal.valueOf(cent);
        BigDecimal exchangeNum = BigDecimal.valueOf(100);
        BigDecimal yuanDecimal = centDecimal.divide(exchangeNum, 2, RoundingMode.UNNECESSARY);
        return yuanDecimal.toPlainString();
    }

    public static String getString(int stringId) {
        Context context = GlobalContext.getContext();
        if (context == null) {
            return "";
        }
        return context.getString(stringId);
    }
}
