package com.ccmt.template.appopspermissions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * @author myx
 *         by 2017-08-18
 */
class MiUiOnRequestPermissionsListener extends AbstractOnRequestPermissionsListener {

    @Override
    public void requestPermissionsSystemAlertWindow(Context context) {
        applyMiuiPermission(context);
    }

    /**
     * 小米ROM权限申请
     */
    private void applyMiuiPermission(Context context) {
        int versionCode = getMiuiVersion();
        LogUtil.i("小米版本号 -> " + versionCode);
        if (versionCode == 5) {
            goToMiuiPermissionActivityV5(context);
        } else if (versionCode == 6) {
            goToMiuiPermissionActivityV6(context);
        } else if (versionCode == 7) {
            goToMiuiPermissionActivityV7(context);
        } else if (versionCode == 8) {
            goToMiuiPermissionActivityV8(context);
        } else {
            LogUtil.i("小米未知版本号");
        }
    }

    /**
     * 获取小米rom版本号,获取失败返回-1.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private int getMiuiVersion() {
        String version = RomUtil.getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 小米V5版本ROM权限申请
     */
    private void goToMiuiPermissionActivityV5(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (isIntentAvailable(intent, context)) {
        context.startActivity(intent);
//        }

        // 设置页面在应用详情页面
//        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//        PackageInfo pInfo = null;
//        try {
//            pInfo = context.getPackageManager().getPackageInfo
//                    (HostInterfaceManager.getHostInterface().getApp().getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            AVLogUtils.e(TAG, e.getMessage());
//        }
//        intent.setClassName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor");
//        intent.putExtra("extra_package_uid", pInfo.applicationInfo.uid);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (isIntentAvailable(intent, context)) {
//            context.startActivity(intent);
//        } else {
//            AVLogUtils.e(TAG, "Intent is not available!");
//        }
    }

    /**
     * 小米V6版本ROM权限申请
     */
    private void goToMiuiPermissionActivityV6(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        if (isIntentAvailable(intent, context)) {
        context.startActivity(intent);
//        }
    }

    /**
     * 小米V7版本ROM权限申请
     */
    private void goToMiuiPermissionActivityV7(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        if (isIntentAvailable(intent, context)) {
        context.startActivity(intent);
//        }
    }

    /**
     * 小米V8版本ROM权限申请
     */
    private void goToMiuiPermissionActivityV8(Context context) {
//        boolean intentAvailable = isIntentAvailable(new Intent("miui.intent.action.APP_PERM_EDITOR"), context);
//        boolean intentAvailable2 = isIntentAvailable2(new Intent("miui.intent.action.APP_PERM_EDITOR"), context);
//        LogUtil.i("intentAvailable -> " + intentAvailable);
//        LogUtil.i("intentAvailable2 -> " + intentAvailable2);
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR_PRIVATE");
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        if (isIntentAvailable(intent, context)) {
        context.startActivity(intent);
//        }
    }

}
