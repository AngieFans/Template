package com.ccmt.template.dynamicpermissions.callback;

import com.ccmt.template.dynamicpermissions.PermissionInfo;

import java.util.List;

public interface PermissionResultCallBack {

    /**
     * 弹出了授权对话框
     *
     * @param mPermissionListNeedReq
     */
    @SuppressWarnings("JavaDoc")
    void onHasPermissionRational(List<PermissionInfo> mPermissionListNeedReq);

    /**
     * 有权限被拒绝,且被勾选了不再提醒选项.
     *
     * @param mPermissionListDenied
     */
    @SuppressWarnings("JavaDoc")
    void onHasPermissionDenied(List<PermissionInfo> mPermissionListDenied);

    /**
     * 当所有权限的申请被用户同意之后,该方法会被调用
     */
    void onPermissionGranted();

    /**
     * 返回此次申请中通过的权限列表
     *
     * @param permissions
     */
    @SuppressWarnings("JavaDoc")
    void onPermissionGranted(String... permissions);

    /**
     * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,并勾选了不再提醒选项时（权限的申请窗口不能再弹出，
     * 没有办法再次申请）,该方法将会被调用。该方法调用时机在onRationalShow之前.onDenied和onRationalShow
     * 有可能都会被触发.
     *
     * @param permissions
     */
    @SuppressWarnings("JavaDoc")
    void onPermissionDenied(String... permissions);

    /**
     * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,但没有勾选不再提醒选项时（权限申请窗口还能再次申请弹出）
     * 该方法将会被调用.这个方法会在onPermissionDenied之后调用,当申请权限为多个时,onDenied和onRationalShow
     * 有可能都会被触发.
     *
     * @param permissions
     */
    @SuppressWarnings("JavaDoc")
    void onRationalShow(String... permissions);

}
