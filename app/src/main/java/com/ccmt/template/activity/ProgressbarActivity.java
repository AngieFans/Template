package com.ccmt.template.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.R;
import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.template.util.DialogFractory;
import com.ccmt.template.util.ObjectUtil;
import com.ccmt.template.view.CustomAlertDialog;
import com.ccmt.template.view.LoadingView;

public class ProgressbarActivity extends AbstractActivity {

    private LoadingView mLoadingView;
    //    private boolean mIsSaveInstanceState;
    private boolean mIsInit;
//    private Boolean mIsNotTouchModal;
//    private Boolean mIsShouldAddFlagActivityNewTask;

//    /**
//     * 是否从当面界面跳转到了其他界面
//     */
//    private boolean mIsStartActivityed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoadingView = (LoadingView) findViewById(R.id.mImageView);

        makeOutsideClose(false);
    }

    private void makeOutsideClose(boolean outsideClose) {
        if (outsideClose) {
            RelativeLayout root = (RelativeLayout) findViewById(R.id.root);

            // 非透明的内容区域
            View popupWindowView = findViewById(R.id.content_layout);
            root.setOnTouchListener((v, event) -> {
                int x = (int) event.getX();
                int y = (int) event.getY();
                Rect rect = new Rect();
                popupWindowView.getGlobalVisibleRect(rect);
                if (!rect.contains(x, y)) {
                    DialogFractory.closeProgressDialog(ProgressbarActivity.this);
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LogUtil.i("ProgressbarActivity onDestroy()");

        LruMap lruMap = LruMap.getInstance();
        if (lruMap.get("requestPermissionsRunnable") != null) {
            LogUtil.i("回收Activity占用的内存");

            lruMap.remove("isReturnDialog", false);
            lruMap.remove("isGoToAppSetting", false);
            lruMap.remove("isShowPermissionsDialog", false);
            lruMap.remove("permissionsDialog", false);
            lruMap.remove("requestPermissionsRunnable", false);

//            if (DynamicPermissionManager.sType != null
//                    && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//                lruMap.put("isRequestPermissionsFinish", true);
//            }

//            ObjectUtil.obtainDynamicPermissionManager().setContext(null);
            ObjectUtil.obtainDynamicPermissionManager().reset();
        } else {
            if (DialogFractory.sType != null && DialogFractory.sType == DynamicPermissionManager.TYPE_ACTIVITY) {
                DialogFractory.sType = null;
            }
//            if (mIsNotTouchModal != null) {
//                Intent intent = new Intent(this, JJBoostMainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
        }
    }

    @Override
    protected int obtainLeftIcon() {
        return 0;
    }

    @Override
    protected int obtainRightIcon() {
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        LogUtil.i("ProgressbarActivity onResume()");
        LogUtil.i("mIsInit -> " + mIsInit);

        LruMap lruMap = LruMap.getInstance();
        Runnable requestPermissionsRunnable = (Runnable) lruMap.get("requestPermissionsRunnable");
        if (lruMap.get("isShow") != null) {
            if (requestPermissionsRunnable == null) {
                Boolean isNotTouchModal = (Boolean) lruMap.get("isNotTouchModal");
                LogUtil.i("isNotTouchModal -> " + isNotTouchModal);
//                mIsNotTouchModal = isNotTouchModal;
                if (isNotTouchModal == null) {
                    Boolean isRotate = (Boolean) LruMap.getInstance().get("isRotate");
                    if (isRotate != null) {
                        LogUtil.i("打开转圈对话框");
                        ViewUtil.setVisibility(mLoadingView, View.VISIBLE);
                    } else {
                        if (!mIsInit) {
                            mIsInit = true;
                            ViewUtil.setVisibility(mLoadingView, View.GONE);
                        } else {
                            return;
                        }
                    }
                    DialogFractory.sType = DynamicPermissionManager.TYPE_ACTIVITY;
                } else {
                    if (!mIsInit) {
                        LogUtil.i("可穿透的透明界面");
                        mIsInit = true;

                        ViewUtil.setVisibility(mLoadingView, View.GONE);

//                        HashMap map = (HashMap) getIntent().getSerializableExtra("param");
//                        LogUtil.i("调用者是否将参数传过来 -> " + (map != null && map.size() > 0));
//                        if (map != null) {
//                            mIsShouldAddFlagActivityNewTask = (Boolean) map.get("isShouldAddFlagActivityNewTask");
//                            mIsStartActivityed = true;
////                            if (mIsShouldAddFlagActivityNewTask != null && !mIsShouldAddFlagActivityNewTask) {
////                                mIsShouldAddFlagActivityNewTask = null;
////                                CommonUtil.showGuide(this, dialog -> CommonUtil.goToApplicationDetailsSettings(ProgressbarActivity.this,
////                                        (String) map.get("packageName"),
////                                        false));
//////                                CommonUtil.showGuide(this, null);
////                            } else {
////                                CommonUtil.goToApplicationDetailsSettings(this, (String) map.get("packageName"), true);
////                            }
//                            CommonUtil.goToApplicationDetailsSettings(this, (String) map.get("packageName"), mIsShouldAddFlagActivityNewTask);
//                        }
//                        CommonUtil.showGuide(this);
                    } else {
//                        boolean isHasParam = getIntent().getBooleanExtra("isHasParam", false);
//                        LogUtil.i("是否跳转到其他界面后再重新显示当前界面 -> " + isHasParam);
//                        if (!isHasParam) {
//                            LogUtil.i("ProgressbarActivity被重复开启");
//                            DialogFractory.closeProgressDialog(this);
//                        }
                        LogUtil.i("ProgressbarActivity被重复开启");
                        DialogFractory.closeProgressDialog(this);
                    }
                }
            } else {
                if (!mIsInit) {
                    mIsInit = true;

                    // 开启新的Activity走动态权限流程
                    ViewUtil.setVisibility(mLoadingView, View.GONE);

                    ObjectUtil.obtainDynamicPermissionManager().setContext(this);

                    requestPermissionsRunnable.run();
                } else {
//                    requestPermissionsRunnable.run();
                    Boolean isShowPermissionsDialog = (Boolean) lruMap.get("isShowPermissionsDialog");
                    if (isShowPermissionsDialog != null) {
                        // 弹出了动态权限对话框
                        Boolean isGoToAppSetting = (Boolean) lruMap.get("isGoToAppSetting");
                        if (isGoToAppSetting != null) {
                            // 弹出了动态权限对话框,且用户点了确认.
                            LogUtil.i("弹出了动态权限对话框,且用户点了确认.");
                            DynamicPermissionManager.sIsShouldGoToAppSetting = true;
                            DialogFractory.closeProgressDialog(this);
                        } else {
                            Boolean isReturnDialog = (Boolean) lruMap.get("isReturnDialog");
                            if (isReturnDialog != null) {
                                // 弹出了动态权限对话框,且用户点了取消.
                                LogUtil.i("弹出了动态权限对话框,且用户点了取消.");
                                DynamicPermissionManager.sIsShouldGoToAppSetting = null;
                                DialogFractory.closeProgressDialog(this);
                            } else {
                                // 只是弹出了动态权限对话框,用户没有点确认或取消.
                                LogUtil.i("只是弹出了动态权限对话框,用户没有点确认或取消.");
                                CustomAlertDialog permissionsDialog = (CustomAlertDialog) lruMap.get("permissionsDialog");
                                if (permissionsDialog != null) {
                                    permissionsDialog.show();
                                } else {
                                    DialogFractory.closeProgressDialog(this);
                                }
                            }
                        }
                    } else {
                        // 没有弹对话框
                        LogUtil.i("没有弹动态权限对话框");
                        DialogFractory.closeProgressDialog(this);
                    }
                }
            }
        } else {
            if (requestPermissionsRunnable == null) {
                LogUtil.i("关闭转圈对话框");

                ViewUtil.setVisibility(mLoadingView, View.GONE);
            }
            exit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LogUtil.i("ProgressbarActivity onPause()");

//        if (LruMap.getInstance().get("uninstallSelf") == null) {
//            if (RootUtil.isModelStartInit()) {
//                LogUtil.i("root授权对话框弹出");
//                mIsStartRoot = true;
//                RootUtil.setStartRootDone(true);
//            }
//        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mIsStartActivityed) {
//            mIsStartActivityed = false;
//            Intent intent = new Intent(this, ProgressbarActivity.class);
//            intent.putExtra("isHasParam", true);
////            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (!mIsSaveInstanceState) {
//            mIsSaveInstanceState = true;
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LruMap.getInstance().get("isNotTouchModal") == null) {
            return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogFractory.closeProgressDialog(this);
        }
        return super.onKeyDown(keyCode, event);
    }

//    private void exit() {
//        if (!mIsSaveInstanceState) {
//            onBackPressed();
//        } else {
//            finish();
//        }
//    }

    @Override
    protected boolean getActivityHasSetting() {
        return false;
    }

    @Override
    protected boolean isShowTitle() {
        return false;
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.activity_dialog_progressbar;
    }

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected int getCustomStyleResourceId() {
        Boolean isRotate = (Boolean) LruMap.getInstance().get("isRotate");
        if (isRotate != null) {
            return R.style.custom_progressbar_activity;
        }
        Boolean isNotTouchModal = (Boolean) LruMap.getInstance().get("isNotTouchModal");
        if (isNotTouchModal != null) {
            return R.style.custom_not_touch_modal_activity;
        }
        return R.style.custom_permissions_activity;
    }

}
