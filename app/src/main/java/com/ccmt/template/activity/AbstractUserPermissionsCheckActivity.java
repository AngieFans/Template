package com.ccmt.template.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.R;
import com.ccmt.template.appopspermissions.PermissionsUtil;
import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.template.dynamicpermissions.callback.PermissionResultAdapter;
import com.ccmt.template.util.DialogFractory;
import com.ccmt.template.util.ObjectUtil;
import com.ccmt.template.view.CustomAlertDialog;

public abstract class AbstractUserPermissionsCheckActivity extends AbstractActivity {

    /**
     * 是否让用户授权android.permission.PACKAGE_USAGE_STATS权限
     */
    public static Boolean sIsAuthorization;

    private static Boolean sIsShowPackageUsageStatsDialoged;

    protected View mShowView;
    protected View mHideView;
    public boolean mIsClickButton;
    private boolean mIsShowHasNoNonDynamicPermissionsContented;
    private boolean mIsShowHasNoDynamicPermissionsContented;
    private boolean mIsShowHasDynamicPermissionsContented;
    private boolean mIsOnPermissionDeniedInvoke;
    public String[] mGrantedPermissionsees;
    public boolean mIsHasNonDynamicPermissions = true;
    public boolean mIsAllGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mIsSwitch) {
            sIsAuthorization = null;
        }
    }

    @SuppressWarnings("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

//        if (this instanceof ProgressbarActivity) {
////            if (DynamicPermissionManager.sType != null
////                    && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            doOnResume();
//            return;
////            }
//        }

        mIsAllGranted = false;

        LogUtil.i("sIsAuthorization -> " + sIsAuthorization);
//        LogUtil.i("sIsShowDynamicPermissionDialoged -> " + sIsShowDynamicPermissionDialoged);
        LogUtil.i("DynamicPermissionManager.sIsShouldGoToAppSetting -> " + DynamicPermissionManager.sIsShouldGoToAppSetting);

        if (DynamicPermissionManager.sIsShouldGoToAppSetting == null) {
            // 用户拒绝授权动态权限,同时点击了不再提示,会再调用onResume()方法,就会弹系统动态权限列表界面,
            // 再点返回键到应用的当前界面时,会再弹系统动态权限列表界面,如果是这种情况就直接返回.
            DynamicPermissionManager.sIsShouldGoToAppSetting = true;

            mIsShowHasDynamicPermissionsContented = false;

            if (!mIsShowHasNoDynamicPermissionsContented) {
                showHasNoDynamicPermissionsContent();
            }
            return;
        }

        boolean hasNonDynamicPermissions = isHasNonDynamicPermissions(this);
        mIsHasNonDynamicPermissions = hasNonDynamicPermissions;
        boolean checkNonDynamicPermissions = isCheckNonDynamicPermissions();
        if (checkNonDynamicPermissions) {
            if (sIsAuthorization != null) {
                // 如果用户没有授权android.permission.PACKAGE_USAGE_STATS权限,也能返回到主界面.
                if (!hasNonDynamicPermissions) {
                    if (!mIsShowHasNoNonDynamicPermissionsContented) {
                        showHasNoNonDynamicPermissionsContent();
                    }
                    if (isDoOnResumeAfterRequestNonDynamicPermissionsError()) {
                        doOnResume();
                    }

                    // 如果非动态权限都没有被用户允许,就不应该再检测动态权限,直接返回.
                    return;
                }
                sIsAuthorization = null;
            }
            if (hasNonDynamicPermissions) {
//                if (mIsShowHasNoNonDynamicPermissionsContented) {
                mIsShowHasNoNonDynamicPermissionsContented = false;
                mShowView = initHasNonDynamicPerssmissionsShowView();
                mHideView = initHasNonDynamicPerssmissionsHideView();
                showContent();
//                }
            } else {
                if (!mIsShowHasNoNonDynamicPermissionsContented) {
                    showHasNoNonDynamicPermissionsContent();
                    showPackageUsageStatsDialog(this);
                } else {
                    if (isDoOnResumeAfterRequestNonDynamicPermissionsError()) {
                        doOnResume();
                    }
                }
                return;
            }
        }

        // 重构后不需要以下代码
//        if (sIsShowDynamicPermissionDialoged != null) {
//            // 弹出授权对话框后,用户允许或拒绝都会再调用onResume()方法,如果是这种情况就直接返回.
//            sIsShowDynamicPermissionDialoged = null;
//            return;
//        }

//        if (DynamicPermissionManager.sIsShouldGoToAppSetting == null) {
//            // 用户拒绝授权动态权限,同时点击了不再提示,会再调用onResume()方法,就会弹系统动态权限列表界面,
//            // 再点返回键到应用的当前界面时,会再弹系统动态权限列表界面,如果是这种情况就直接返回.
//            DynamicPermissionManager.sIsShouldGoToAppSetting = true;
//
//            mIsShowHasDynamicPermissionsContented = false;
//
//            if (!mIsShowHasNoDynamicPermissionsContented) {
//                showHasNoDynamicPermissionsContent();
//            }
//            return;
//        }
        boolean checkPermissions = checkPermissions(this);
        LruMap lruMap = LruMap.getInstance();
        Boolean isGoToAppSetting = (Boolean) lruMap.get("isGoToAppSetting");
        if (isGoToAppSetting != null) {
            lruMap.remove("isGoToAppSetting", false);
            if (!checkPermissions) {
                return;
            }
        }
        if (checkPermissions) {
            mIsAllGranted = true;

            mGrantedPermissionsees = initDynamicPermissionses();

            mIsShowHasNoDynamicPermissionsContented = false;

            if (!mIsShowHasDynamicPermissionsContented) {
                mIsShowHasDynamicPermissionsContented = true;
                mShowView = initHasDynamicPerssmissionsShowView();
                mHideView = initHasDynamicPerssmissionsHideView();
                showContent();
            }

            boolean showDialog = isShowDialog();
            boolean doOnResumeAfterRequestDynamicPermissions = isDoOnResumeAfterLoadDataDone();
            if (showDialog) {
//                DialogFractory.showProgressDialog(mContext, true);
                DialogFractory.showFullScreenProgressDialog(this);
            }
            ThreadManager.executeAsyncTask(() -> {
                loadData();
                ThreadManager.post(() -> {
                    initRequestDynamicPermissionsFinishView();
                    if (showDialog) {
//                        DialogFractory.closeProgressDialog(mContext);
                        DialogFractory.closeFullScreenProgressDialog();
                    }
                    if (doOnResumeAfterRequestDynamicPermissions) {
                        doOnResume();
//                        startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                    }
                });
            });
            if (!doOnResumeAfterRequestDynamicPermissions) {
                doOnResume();
//                startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
            }
        } else {
            // Activity方式
            requestDynamicPermissions(false);
        }

        // 动态权限用的是非Activity方式
//        if (DynamicPermissionManager.sType != null
//                && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            DialogFractory.closeProgressDialog(this);
//        }

//        if (checkNonDynamicPermissions) {
//            // 检测android.permission.PACKAGE_USAGE_STATS权限
//            if (!hasNonDynamicPermissions) {
////            mShowView = initHasNoDynamicPerssmissionsShowView();
////            mHideView = initHasNoPerssmissionsHideView();
////            showContent();
//
////            LogUtil.i("让用户去授权");
////            sIsAuthorization = true;
////            requestReadNetworkStats(this);
//
//                showPackageUsageStatsDialog(this);
//                return;
//            }
//        }

        // 检测动态权限,暂时保留.
//        String[] dynamicPermissionses = initDynamicPermissionses();
//        if (dynamicPermissionses != null && dynamicPermissionses.length > 0) {
//            if (Build.VERSION.SDK_INT >= 23) {
//                boolean hasDynamicPermission = true;
//
//                for (String dynamicPermissionse : dynamicPermissionses) {
//                    hasDynamicPermission = ContextCompat.checkSelfPermission(this,
//                            dynamicPermissionse) == PackageManager.PERMISSION_GRANTED;
//                    if (!hasDynamicPermission) {
//                        hasDynamicPermission = false;
//                        break;
//                    }
//                }
//                LogUtil.i("hasDynamicPermission -> " + hasDynamicPermission);
////                hasDynamicPermission = NewTrafficManager.hasPermission(this, AppOpsManager.OPSTR_GET_USAGE_STATS);
////                LogUtil.i("hasDynamicPermission -> " + hasDynamicPermission);
//
//                if (!hasDynamicPermission) {
//                    mShowView = initHasNoDynamicPerssmissionsShowView();
//                    mHideView = initHasNoPerssmissionsHideView();
//                    showContent();
//                    return;
//                }
//            }
//        }

//        // Activity方式
//        requestDynamicPermissions(false);
    }

    public static void showPackageUsageStatsDialog(AbstractUserPermissionsCheckActivity userPermissionsCheckActivity) {
        if (sIsShowPackageUsageStatsDialoged != null) {
            return;
        }
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                LogUtil.i("让用户去授权");
                sIsAuthorization = true;
                PermissionsUtil.requestReadNetworkStats(userPermissionsCheckActivity);
            } else {
                userPermissionsCheckActivity.showHasNoNonDynamicPermissionsContent();
                if (userPermissionsCheckActivity.isDoOnResumeAfterRequestNonDynamicPermissionsError()) {
                    userPermissionsCheckActivity.doOnResume();
                }
            }
        };
        try {
            PackageManager packageManager = userPermissionsCheckActivity.getPackageManager();
            CharSequence label = packageManager.getApplicationInfo(userPermissionsCheckActivity.getPackageName(),
                    0).loadLabel(packageManager);
            CustomAlertDialog dialog = new CustomAlertDialog.Builder(userPermissionsCheckActivity)
                    .setTitle(userPermissionsCheckActivity.getString(R.string.traffic_single_package_usage_stats_title))
                    .setMessage("“" + label.toString() + "”" + userPermissionsCheckActivity.getString(R.string
                            .traffic_single_package_usage_stats_message))
                    .setPositiveButton(R.string.traffic_single_allow, onClickListener)
                    .setNegativeButton(R.string.traffic_single_deny, onClickListener)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setOnDismissListener(dialog1 -> sIsShowPackageUsageStatsDialoged = null)
                    .create();
            dialog.setOnShowListener(dialog12 -> sIsShowPackageUsageStatsDialoged = true);
            dialog.show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void requestDynamicPermissions(boolean isClickButton) {
//        ITrafficManager trafficManager = TrafficManagerFactory.createTrafficManager(this);
//        LogUtil.i("mTrafficManager -> " + mTrafficManager);
//        if (mTrafficManager == null) {
//            return;
//        }

        mIsClickButton = isClickButton;

        boolean showDialog = isShowDialog();
        boolean doOnResumeAfterLoadDataDone = isDoOnResumeAfterLoadDataDone();

        // Activity方式
        String[] dynamicPermissionses = initDynamicPermissionses();
        if (Build.VERSION.SDK_INT >= 23) {
            ObjectUtil.obtainDynamicPermissionManager().request(this,
                    dynamicPermissionses,
                    new PermissionResultAdapter() {
                        @Override
                        public void onPermissionGranted() {
                            super.onPermissionGranted();
                            mIsAllGranted = true;
                            if (showDialog) {
//                                    DialogFractory.showProgressDialog(mContext, true);
                                DialogFractory.showFullScreenProgressDialog(AbstractUserPermissionsCheckActivity.this);
                            }
                            ThreadManager.executeAsyncTask(() -> {
                                loadData();
                                ThreadManager.post(() -> {
                                    initRequestDynamicPermissionsFinishView();
                                    if (showDialog) {
//                                            DialogFractory.closeProgressDialog(mContext);
                                        DialogFractory.closeFullScreenProgressDialog();
                                    }
                                    if (doOnResumeAfterLoadDataDone) {
                                        doOnResume();
//                                            startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                                    }
                                });
                            });
                            if (!doOnResumeAfterLoadDataDone) {
                                doOnResume();
//                                    startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                            }
                        }

                        @Override
                        public void onPermissionGranted(String... permissions) {
                            super.onPermissionGranted(permissions);
                            mGrantedPermissionsees = permissions;
//                            if (Arrays.asList(dynamicPermissionses).equals(Arrays.asList(permissions))) {
//                                if (showDialog) {
////                                    DialogFractory.showProgressDialog(mContext, true);
//                                    DialogFractory.showFullScreenProgressDialog(AbstractUserPermissionsCheckActivity.this);
//                                }
//                                ThreadManager.executeAsyncTask(() -> {
//                                    loadData();
//                                    runOnUiThread(() -> {
//                                        initRequestDynamicPermissionsFinishView();
//                                        if (showDialog) {
////                                            DialogFractory.closeProgressDialog(mContext);
//                                            DialogFractory.closeFullScreenProgressDialog();
//                                        }
//                                        if (doOnResumeAfterRequestDynamicPermissions) {
//                                            doOnResume();
////                                            startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
//                                        }
//                                    });
//                                });
//                                if (!doOnResumeAfterRequestDynamicPermissions) {
//                                    doOnResume();
////                                    startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
//                                }
//                            }
                        }

                        @Override
                        public void onPermissionDenied(String... permissions) {
                            super.onPermissionDenied(permissions);
                            mIsOnPermissionDeniedInvoke = true;
                            doOnResume();
                        }

                        @Override
                        public void onRationalShow(String... permissions) {
                            super.onRationalShow(permissions);
                            if (mIsOnPermissionDeniedInvoke) {
                                mIsOnPermissionDeniedInvoke = false;
                            } else {
                                doOnResume();
                            }
                        }
                    });
        } else {
            if (showDialog) {
//                DialogFractory.showProgressDialog(mContext, true);
                DialogFractory.showFullScreenProgressDialog(this);
            }
            ThreadManager.executeAsyncTask(() -> {
                loadData();
                ThreadManager.post(() -> {
                    initRequestDynamicPermissionsFinishView();
                    if (showDialog) {
//                        DialogFractory.closeProgressDialog(mContext);
                        DialogFractory.closeFullScreenProgressDialog();
                    }
                    if (doOnResumeAfterLoadDataDone) {
                        doOnResume();
//                        startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                    }
                });
            });
            if (!doOnResumeAfterLoadDataDone) {
                doOnResume();
//                startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
            }
        }
    }

    protected abstract void initRequestDynamicPermissionsFinishView();

    /**
     * 申请动态权限成功后,加载数据的过程中是否弹出转圈对话框.
     *
     * @return 为true代表要弹出转圈对话框.否则不弹出转圈对话框.
     */
    protected abstract boolean isShowDialog();

    /**
     * 是否检测非动态权限,例如android.permission.PACKAGE_USAGE_STATS权限,如果不检测,则只检测动态权限申请.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    protected boolean isCheckNonDynamicPermissions() {
        return false;
    }

    /**
     * 子类是否在申请动态权限成功且在子线程运行完loadData()方法后,然后在主线程调用doOnResume()方法.
     *
     * @return 为true代表子类在申请动态权限成功且在子线程运行完loadData()方法后,
     * 然后在主线程调用doOnResume()方法.否则代表子类在申请动态权限成功后,不用等子线程运行完loadData()方法,
     * 直接在主线程调用doOnResume()方法.
     * 如果申请动态权限失败,不会在子线程调用loadData()方法,直接在主线程调用doOnResume()方法.
     */
    protected abstract boolean isDoOnResumeAfterLoadDataDone();

    /**
     * 请求非动态权限时,例如android.permission.PACKAGE_USAGE_STATS权限,如果请求失败,是否仍然调用onResume()方法.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    protected abstract boolean isDoOnResumeAfterRequestNonDynamicPermissionsError();

//    /**
//     * 请求非动态权限时,例如android.permission.PACKAGE_USAGE_STATS权限,如果请求失败,是否直接返回,不再往下运行.
//     *
//     * @return
//     */
//    @SuppressWarnings("JavaDoc")
//    protected abstract boolean isReturnAfterRequestNonDynamicPermissionsError();

    protected void showContent() {
        if (mShowView != null && mHideView != null) {
            ViewUtil.setVisibility(mShowView, View.VISIBLE);
            ViewUtil.setVisibility(mHideView, View.GONE);
        }
    }

    public void showHasNoNonDynamicPermissionsContent() {
        mIsShowHasNoNonDynamicPermissionsContented = true;
        mShowView = initHasNoNonDynamicPerssmissionsShowView();
        mHideView = initHasNoNonDynamicPerssmissionsHideView();
        showContent();
    }

    public void showHasNoDynamicPermissionsContent() {
        mIsShowHasNoDynamicPermissionsContented = true;
        mShowView = initHasNoDynamicPerssmissionsShowView();
        mHideView = initHasNoDynamicPerssmissionsHideView();
        showContent();
    }

    protected boolean checkPermissions(Context context) {
        String[] dynamicPermissionses = initDynamicPermissionses();
        if (dynamicPermissionses == null || dynamicPermissionses.length == 0) {
            throw new RuntimeException("子类重写initDynamicPermissionses()方法返回的权限不能为空且数量必须大于0");
        }
        boolean hasDynamicPermissions = true;
        for (String dynamicPermissionse : dynamicPermissionses) {
            hasDynamicPermissions = ContextCompat.checkSelfPermission(context,
                    dynamicPermissionse) == PackageManager.PERMISSION_GRANTED;
            if (!hasDynamicPermissions) {
                break;
            }
        }
        return hasDynamicPermissions;
    }

    /**
     * 是否有非动态权限被用户允许
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "UnusedParameters"})
    protected boolean isHasNonDynamicPermissions(Context context) {
        return true;
    }

    /**
     * 如果子类每次onResume()方法被调用时需要申请的动态权限请在子类重写该方法
     */
    protected abstract String[] initDynamicPermissionses();

    protected abstract View initHasNonDynamicPerssmissionsShowView();

    protected abstract View initHasNonDynamicPerssmissionsHideView();

    protected abstract View initHasNoNonDynamicPerssmissionsShowView();

    protected abstract View initHasNoNonDynamicPerssmissionsHideView();

    protected abstract View initHasDynamicPerssmissionsShowView();

    protected abstract View initHasDynamicPerssmissionsHideView();

    protected abstract View initHasNoDynamicPerssmissionsShowView();

    protected abstract View initHasNoDynamicPerssmissionsHideView();

    protected abstract void doOnResume();

}
