package com.ccmt.template.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * @author myx
 *         by 2017-07-21
 */
public class TelephonyUtil {

    /**
     * 获取手机SIM卡运营商
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static String getSimOperator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null ? telephonyManager.getSimOperatorName() : "";
    }

}
