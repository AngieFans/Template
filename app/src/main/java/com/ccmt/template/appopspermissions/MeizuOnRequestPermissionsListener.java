package com.ccmt.template.appopspermissions;

import android.content.Context;
import android.content.Intent;

/**
 * @author myx
 *         by 2017-08-18
 */
class MeizuOnRequestPermissionsListener extends AbstractOnRequestPermissionsListener {

    @Override
    public void requestPermissionsSystemAlertWindow(Context context) {
        applyPermission(context);
    }

    /**
     * 去魅族权限申请页面
     */
    private static void applyPermission(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        intent.putExtra("packageName", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
