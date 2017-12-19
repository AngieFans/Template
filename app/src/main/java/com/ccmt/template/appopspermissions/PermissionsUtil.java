package com.ccmt.template.appopspermissions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author myx
 *         by 2017-08-17
 */
public class PermissionsUtil {

    private static int OP_NONE = -1;

    private static int MODE_ALLOWED = 0;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static int MODE_IGNORED = 1;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static int MODE_ERRORED = 2;

    private static int MODE_DEFAULT = 3;

    static {
        if (Build.VERSION.SDK_INT >= 19) {
            Object opNone = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "OP_NONE");
            if (opNone != null) {
                OP_NONE = (int) opNone;
            }
            Object modeAllowed = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "MODE_ALLOWED");
            if (modeAllowed != null) {
                MODE_ALLOWED = (int) modeAllowed;
            }
            Object modeIgnored = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "MODE_IGNORED");
            if (modeIgnored != null) {
                MODE_IGNORED = (int) modeIgnored;
            }
            Object modeErrored = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "MODE_ERRORED");
            if (modeErrored != null) {
                MODE_ERRORED = (int) modeErrored;
            }
            Object modeDefault = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "MODE_DEFAULT");
            if (modeDefault != null) {
                MODE_DEFAULT = (int) modeDefault;
            }
        }
    }

    private static int checkOpNoThrow(Context context, int op, int uid, String packageName) {
        AppOpsManager appOpsManager;
        if (android.os.Build.VERSION.SDK_INT < 19) {
            return MODE_ALLOWED;
        }
        appOpsManager = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        Method m;
        try {
            m = appOpsManager.getClass().getDeclaredMethod("checkOpNoThrow",
                    int.class, int.class, String.class);
            return (int) m.invoke(appOpsManager, op, uid, packageName);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return OP_NONE;
    }

    @SuppressWarnings("WeakerAccess")
    public static int checkOpNoThrow(Context context, int op, String packageName) {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(
                    packageName, 0);
            return checkOpNoThrow(context, op, applicationInfo.uid,
                    applicationInfo.packageName);
        } catch (PackageManager.NameNotFoundException e3) {
            e3.printStackTrace();
        }
        return OP_NONE;
    }

    private static void setMode(AppOpsManager appOpsManager, int code, int uid, String packageName, int mode) {
        try {
            Method m = appOpsManager.getClass().getDeclaredMethod("setMode",
                    int.class, int.class, String.class, int.class);
            m.setAccessible(true);
            m.invoke(appOpsManager, code, uid, packageName, mode);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void setMode(Context context, int code, String packageName, int mode) {
        AppOpsManager appOpsManager;
        if (android.os.Build.VERSION.SDK_INT < 19) {
            return;
        }
        appOpsManager = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            setMode(appOpsManager, code, applicationInfo.uid, applicationInfo.packageName,
                    mode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void setUidMode(AppOpsManager appOpsManager, int code, int uid, int mode) {
        try {
            Method m = appOpsManager.getClass().getDeclaredMethod("setUidMode",
                    int.class, int.class, int.class);
            m.setAccessible(true);
            m.invoke(appOpsManager, code, uid, mode);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean hasPermission(Context context, String perssmissionsName) {
        if (Build.VERSION.SDK_INT < 19) {
            return true;
        }
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(perssmissionsName,
                android.os.Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    @SuppressWarnings("unused")
    public static boolean hasPermissionToReadNetworkStats(Context context) {
        return Build.VERSION.SDK_INT < 21 || hasPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS);
    }

    /**
     * 打开有权查看使用情况的应用页面
     */
    public static void requestReadNetworkStats(Context context) {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        } else {
            return;
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @SuppressWarnings("unused")
    public static boolean hasPermissionWriteSettings(Context context) {
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (Settings.System.canWrite(context)) {
//                LogUtil.i("有写设置项权限");
//                return true;
//            }
////            LogUtil.i("没有写设置项权限");
////            return false;
//            LogUtil.i("经过6.0的方式检测,没有写设置项权限.");
//        }
        if (Build.VERSION.SDK_INT >= 19) {
            Object obj = ReflectUtil
                    .obtainStaticFieldValue(AppOpsManager.class, "OP_WRITE_SETTINGS");
            int opWriteSettings;
            if (obj != null) {
                opWriteSettings = checkOpNoThrow(context, (int) obj, context.getPackageName());
            } else {
                LogUtil.i("当前sdk版本没有引入该权限");
                return false;
            }
            // code 23
            LogUtil.i("opWriteSettings -> " + opWriteSettings);
//            if (opWriteSettings == MODE_ALLOWED || opWriteSettings == MODE_DEFAULT
//                    || opWriteSettings == MODE_ERRORED) {
//                LogUtil.i("有写设置项权限");
//                return true;
//            }
            if (opWriteSettings == MODE_ALLOWED) {
                LogUtil.i("有写设置项权限");
                return true;
            }
            LogUtil.i("没有写设置项权限");
            return false;
        }
        LogUtil.i("有写设置项权限");
        return true;
    }

    @SuppressWarnings("unused")
    public static void requestWriteSettings(Context context) {
        @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @SuppressWarnings("unused")
    public static void setWriteSettingsAllow(Context context, String packageName) {
        Object obj;
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            obj = ReflectUtil.obtainStaticFieldValue(AppOpsManager.class, "OP_WRITE_SETTINGS");
            if (obj != null) {
                setMode(context, (int) obj, packageName, MODE_ALLOWED);
            } else {
                LogUtil.i("当前sdk版本没有引入该权限");
            }
        }
    }

    @SuppressWarnings("unused")
    public static boolean hasPermissionSystemAlertWindow(Context context) {
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (Settings.canDrawOverlays(context)) {
//                LogUtil.i("有悬浮窗权限");
//                return true;
//            }
////            LogUtil.i("没有悬浮窗权限");
////            return false;
//            LogUtil.i("经过6.0的方式检测,没有悬浮窗权限.");
//        }
        if (Build.VERSION.SDK_INT >= 19) {
            Object obj = ReflectUtil
                    .obtainStaticFieldValue(AppOpsManager.class, "OP_SYSTEM_ALERT_WINDOW");
            int opSystemAlertWindow;
            if (obj != null) {
                opSystemAlertWindow = checkOpNoThrow(context, (int) obj, context.getPackageName());
            } else {
                LogUtil.i("当前sdk版本没有引入该权限");
                return false;
            }
            // code 24
            LogUtil.i("opSystemAlertWindow -> " + opSystemAlertWindow);
            if (opSystemAlertWindow == MODE_ALLOWED || opSystemAlertWindow == MODE_DEFAULT
                    || opSystemAlertWindow == MODE_ERRORED) {
                LogUtil.i("有悬浮窗权限");
                return true;
            }
//            if (opSystemAlertWindow == MODE_ERRORED) {
//                if ("samsung".equals(Build.MANUFACTURER.toLowerCase())) {
////                    if ("sm-j7108".equals(Build.MODEL.toLowerCase())
////                            || "sm-g9200".equals(Build.MODEL.toLowerCase())) {
////                        // SM-J7108和SM-G9200,这两款手机无论用户有没有允许悬浮窗权限都可以正常显示悬浮窗.
////                        LogUtil.i("有悬浮窗权限");
////                        return true;
////                    }
//                    LogUtil.i("有悬浮窗权限");
//                    return true;
//                } else {
//                    if ("huawei".equals(Build.MANUFACTURER.toLowerCase())) {
////                        // HUAWEI
////                        if ("plk-tl01h".equals(Build.MODEL.toLowerCase())) {
////                            // PLK-TL01H
////                            LogUtil.i("有悬浮窗权限");
////                            return true;
////                        }
//                        LogUtil.i("有悬浮窗权限");
//                        return true;
//                    }
//                }
//            }
            LogUtil.i("没有悬浮窗权限");
            return false;
        }
        LogUtil.i("有悬浮窗权限");
        return true;
    }

    @SuppressWarnings("unused")
    public static void requestSystemAlertWindow(Context context) {
        @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
//        if (!(context instanceof Activity)) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
        try {
            if ("meizu".equals(Build.MANUFACTURER.toLowerCase())) {
                LogUtil.i("魅族方式打开悬浮窗授权界面成功");
                AbstractOnRequestPermissionsListener onRequestPermissionsListener = OnRequestPermissionsListenerFactory
                        .createOnRequestPermissionsListener();
                if (onRequestPermissionsListener != null) {
                    onRequestPermissionsListener.requestPermissionsSystemAlertWindow(context);
                }
            } else {
                context.startActivity(intent);
                LogUtil.i("默认方式打开悬浮窗授权界面成功");
            }
//            if (RomUtil.isIntentAvailable(intent, context)) {
//                context.startActivity(intent);
//                LogUtil.i("默认方式打开悬浮窗授权界面成功");
//            } else {
//                LogUtil.i("不存在默认方式悬浮窗授权界面的intent过滤规则");
//                AbstractOnRequestPermissionsListener onRequestPermissionsListener = OnRequestPermissionsListenerFactory
//                        .createOnRequestPermissionsListener();
//                if (onRequestPermissionsListener != null) {
//                    onRequestPermissionsListener.requestPermissionsSystemAlertWindow(context);
//                }
//            }
        } catch (Exception e) {
            LogUtil.i("默认方式打开悬浮窗授权界面失败");
            AbstractOnRequestPermissionsListener onRequestPermissionsListener = OnRequestPermissionsListenerFactory
                    .createOnRequestPermissionsListener();
            if (onRequestPermissionsListener != null) {
                onRequestPermissionsListener.requestPermissionsSystemAlertWindow(context);
            }
        }
    }

}
