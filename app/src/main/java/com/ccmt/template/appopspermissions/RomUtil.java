package com.ccmt.template.appopspermissions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author myx
 *         by 2017-08-18
 */
class RomUtil {

    @SuppressWarnings("unused")
    public static boolean isIntentAvailable(Intent intent, Context context) {
        return intent != null && context.getPackageManager()
                .queryIntentActivities(intent, 0).size() > 0;
    }

//    private boolean isIntentAvailable2(Intent intent, Context context) {
//        return intent != null && context.getPackageManager()
//                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
//    }

    /**
     * 获取 emui 版本号
     *
     * @return
     */
    @SuppressWarnings({"ConstantConditions", "JavaDoc", "unused"})
    public static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            return Double.parseDouble(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }

    static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    @SuppressWarnings("unused")
    public static boolean isHuaweiRom() {
//        return Build.MANUFACTURER.contains("HUAWEI");
        return "huawei".equals(Build.MANUFACTURER.toLowerCase());
    }

    /**
     * check if is miui ROM
     */
    static boolean isMiuiRom() {
//        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
        return "xiaomi".equals(Build.MANUFACTURER.toLowerCase());
    }

    static boolean isMeizuRom() {
//        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
//        if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
//            return false;
//        } else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")) {
//            return true;
//        } else {
//            return false;
//        }
        return "meizu".equals(Build.MANUFACTURER.toLowerCase());
    }

    /**
     * check if is 360 ROM
     */
    @SuppressWarnings("unused")
    public static boolean is360Rom() {
//        return Build.MANUFACTURER.contains("QiKU");
        return "qiku".equals(Build.MANUFACTURER.toLowerCase());
    }

}
