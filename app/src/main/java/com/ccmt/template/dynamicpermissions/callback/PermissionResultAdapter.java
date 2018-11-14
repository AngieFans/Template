package com.ccmt.template.dynamicpermissions.callback;

import com.ccmt.library.util.LogUtil;
import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.template.dynamicpermissions.PermissionInfo;

import java.util.Arrays;
import java.util.List;

/**
 * 支持任意重写方法,而无需重写所有的方法
 */
public abstract class PermissionResultAdapter implements PermissionResultCallBack {

    @Override
    public void onHasPermissionRational(List<PermissionInfo> mPermissionListNeedReq) {
        LogUtil.i("onHasPermissionRational()");
    }

    @Override
    public void onHasPermissionDenied(List<PermissionInfo> mPermissionListDenied) {
        LogUtil.i("onHasPermissionDenied()");
    }

    @Override
    public void onPermissionGranted() {
        LogUtil.i("onPermissionGranted()");
//        if (DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck != null
//                && DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck) {
//            DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck = null;
////            DynamicPermissionManager.sIsShouldGoToAppSetting = null;
//        }
    }

    @Override
    public void onPermissionGranted(String... permissions) {
        LogUtil.i("onPermissionGranted()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
    }

    @Override
    public void onPermissionDenied(String... permissions) {
        LogUtil.i("onPermissionDenied()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
        DynamicPermissionManager.sIsShouldGoToAppSetting = null;
    }

    @Override
    public void onRationalShow(String... permissions) {
        LogUtil.i("onRationalShow()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
//        if (DynamicPermissionManager.sType != null
//                && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            DynamicPermissionManager.sIsShouldGoToAppSetting = true;
//        } else {
//            DynamicPermissionManager.sIsShouldGoToAppSetting = null;
//        }
        DynamicPermissionManager.sIsShouldGoToAppSetting = null;
    }

}
