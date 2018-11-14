package com.ccmt.template.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ccmt.library.util.LogUtil;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.CcmtApplication;
import com.ccmt.template.R;
import com.ccmt.template.dynamicpermissions.activity.ProgressbarActivity;
import com.ccmt.template.fragment.AbstractFragment;
import com.ccmt.template.statusbar.StatusBarCompat;
import com.ccmt.template.util.NavigationBarManager;
import com.ccmt.template.dynamicpermissions.util.ObjectUtil;
import com.ccmt.template.view.TitleView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractActivity extends AppCompatActivity implements TitleView.OnTitleClickListener {

    public static AbstractActivity sActivity;
    //    protected FragmentManager mFragmentManager;
    protected Resources mResources;
    public TitleView mTitleLayout;
    protected boolean mIsLoadData;
    @SuppressWarnings("unused")
    protected boolean mIsNavigationBarShow;
    protected Map<String, WeakReference<Fragment>> mFragments = new HashMap<>();

    /**
     * 用户是否显示或隐藏了导航栏
     */
    public boolean mIsSwitch;

    protected boolean mIsSaveInstanceState;

    @SuppressWarnings({"deprecation", "StatementWithEmptyBody"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.i(getClass().getName() + " onCreate()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);

//        MobclickAgent.setDebugMode(true);
//        MobclickAgent.openActivityDurationTrack(false);
//        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
//        MobclickAgent.setCatchUncaughtExceptions(true);

        mResources = getResources();

        // 完全透明的Activity,可以点击后面的Activity的控件.必须在setContentView()方法之前调用,
        // 否则沉浸式状态栏不生效.也可以在setTheme()方法之后调用.
        int customStyleResourceId = getCustomStyleResourceId();
        if (this instanceof ProgressbarActivity) {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            if (customStyleResourceId == R.style.custom_not_touch_modal_activity) {
                // 可以点击后面的窗体,点击返回键不生效.
//                lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

                // 可以点击后面的窗体,点击返回键生效.
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

//                lp.type = WindowManager.LayoutParams.TYPE_PHONE;

//                lp.format = PixelFormat.TRANSPARENT;
//                lp.alpha = 0.6F;

                window.setAttributes(lp);
            } else if (customStyleResourceId == R.style.custom_progressbar_activity) {
                // 透明转圈对话框界面全屏
//                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else if (customStyleResourceId == R.style.custom_permissions_activity) {
                // 透明不转圈对话框界面不全屏
//                lp.format = PixelFormat.TRANSPARENT;
//                window.setAttributes(lp);
            }
        }

        if (customStyleResourceId > 0) {
            setTheme(customStyleResourceId);
        }

        int layoutResID = obtainLayoutResID();
        if (layoutResID > 0) {
            setContentView(layoutResID);
            StatusBarCompat.setStatusBarColor(this, mResources.getColor(R.color.title_bg), true);
//            setForNavigationBarBottom(this);
        }

//        JJBoostApplication.application.addActivity(this);

//        mFragmentManager = getSupportFragmentManager();

//        initTitle();

//        if (isContentNavigationFragment()) {
//            NavigationBarManager navigationBarManager = NavigationBarManager.getInstance();
//            boolean navigationBarShow = navigationBarManager.isNavigationBarShow(this, null);
//            mIsNavigationBarShow = navigationBarShow;
//            if (savedInstanceState == null) {
//                AbstractFragment mainFragment = getFragment(navigationBarShow, getHasNoNavigationFragmentClass(),
//                        getHasNavigationFragmentClass());
////                mPresenter = new JJBoostMainPresenter();
////                JJBoostMainPresenter.sMainFragment = mainFragment;
//                initFragment(mainFragment);
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                fragmentManager.beginTransaction().add(R.id.fragmentContent, mainFragment,
//                        mainFragment.getClass().getSimpleName()).commit();
////            fragmentManager.executePendingTransactions();
//            } else {
//                // 橫竖屏后走这里
//                initFragment((AbstractFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContent));
////                mPresenter = new JJBoostMainPresenter();
//            }
//            navigationBarManager.add(findViewById(R.id.fragmentContent), navigationBarShow,
//                    isNavigationBarShow -> switchFragment(getHasNoNavigationFragmentClass(),
//                            getHasNavigationFragmentClass(), isNavigationBarShow));
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LogUtil.i(getClass().getName() + " onDestroy()");

//        JJBoostApplication.application.removeActivity(this);

        if (isContentNavigationFragment()) {
            NavigationBarManager.getInstance().remove();
        }

        if (Build.VERSION.SDK_INT >= 16) {
            CcmtApplication.getRefWatcher().watch(this);
        }
    }

    protected abstract int obtainLeftIcon();

    protected abstract int obtainRightIcon();

    protected abstract int obtainLayoutResID();

    protected abstract String getActivityTitle();

    protected abstract boolean getActivityHasBack();

    protected abstract boolean getActivityHasSetting();

    protected boolean isShowTitle() {
        return true;
    }

    protected int getCustomStyleResourceId() {
        return 0;
    }

    /**
     * 是否用Fragment来显示,如果返回true,那么对应的ViewGroup的id必须为fragmentContent,专门处理用户显示或隐藏导航栏时切换不同的布局.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public boolean isContentNavigationFragment() {
        return false;
    }

    public Class<? extends AbstractFragment> getHasNoNavigationFragmentClass() {
        return null;
    }

    public Class<? extends AbstractFragment> getHasNavigationFragmentClass() {
        return null;
    }

    /**
     * 如果是用Fragment来显示,Activity的onCreate()方法调用后,会调用该方法.
     *
     * @param fragment
     */
    @SuppressWarnings({"JavaDoc", "UnusedParameters", "unused"})
    protected void initFragment(AbstractFragment fragment) {

    }

    //    /**
//     * 获取任务栈的第1个Activity的Class对象,也就是主界面了,点击返回键会退出程序的那个Activity的Class对象.
//     *
//     * @return
//     */
//    @SuppressWarnings({"JavaDoc", "unused"})
//    private static Class<? extends AbstractActivity> getFirstActivity() {
//        return AcceMainActivity.class;
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(getClass().getName() + " onActivityResult()");
        LogUtil.i("requestCode -> " + requestCode);
        LogUtil.i("resultCode -> " + resultCode);
        LogUtil.i("data -> " + data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ObjectUtil.obtainDynamicPermissionManager().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void initTitle() {
        LogUtil.i("initTitle()");
        if (!isShowTitle()) {
            return;
        }
//        if (mTitleLayout == null) {
//            mTitleLayout = (TitleView) findViewById(R.id.title_root_layout);
//        }
        if (mTitleLayout == null) {
            return;
        }
        mTitleLayout.setOnTitleClickListener(this);
        mTitleLayout.setTitle(getActivityTitle());
        if (obtainLeftIcon() != 0) {
            mTitleLayout.setBackBtnResource(obtainLeftIcon());
        } else {
            mTitleLayout.setBackBtnResource(R.drawable.title_back);
        }
        if (!getActivityHasBack()) {
            mTitleLayout.setBackBtnVisibility(View.GONE);
        }
        if (obtainRightIcon() != 0) {
            mTitleLayout.setSettingBtnResource(obtainRightIcon());
        } else {
            mTitleLayout.setSettingBtnResource(R.drawable.settings);
        }
        if (!getActivityHasSetting()) {
            mTitleLayout.setSettingBtnVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    public void setActivityTitle(final String title) {
        if (mTitleLayout != null) {
            mTitleLayout.setTitle(title);
        }
    }

    @Override
    public void onTitleBack() {
        if (getActivityHasBack()) {
            exit();
        }
    }

    /**
     * 如果onSaveInstanceState()方法已经调用,那么手动调用onBackPressed()方法会报错,所以要做专门处理.
     */
    protected void exit() {
        if (!mIsSaveInstanceState) {
            onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    public void onTitleSetting() {
//        startActivity(JJBoostSettingActivity.class);
    }

    /**
     * 向服务端调接口或本地操作获取数据
     */
    @SuppressWarnings("unused")
    protected void loadData() {
        if (ThreadManager.isOnMainThread()) {
            mIsLoadData = true;
        } else {
            runOnUiThread(() -> mIsLoadData = true);
        }
    }

    @SuppressWarnings("unused")
    protected void setSettingBtnResource(int resid) {
        mTitleLayout.setSettingBtnResource(resid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i(getClass().getName() + " onPause()");
//        MobclickAgent.onPageEnd(getClass().getName());
//        MobclickAgent.onPause(this);
//        if (getClass() == getFirstActivity()) {
        sActivity = null;
//        }
    }

    @SuppressWarnings("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(getClass().getName() + " onResume()");

        sActivity = this;

//        MobclickAgent.onPageStart(getClass().getName());
//        MobclickAgent.onResume(this);
        LogUtil.i("mIsLoadData -> " + mIsLoadData);

        // 如果是以Dialog形式,就不能有这个判断语句.
//        if (mIsLoadData) {
//            // 加载数据时弹出转圈对话框,数据加载完,对话框关闭,会再调用onResume()方法,如果是这种情况就直接返回.
//            mIsLoadData = false;
//            return;
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i(getClass().getName() + " onStart()");
    }

    protected void onStop() {
        super.onStop();
        LogUtil.i(getClass().getName() + " onStop()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtil.i(getClass().getName() + " onSaveInstanceState()");
        LogUtil.i("outState -> " + outState);
//        if (outState != null) {
//            outState.remove("android:support:fragments");
//            outState.remove("android:fragments");
//        }
        if (!mIsSaveInstanceState) {
            mIsSaveInstanceState = true;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LogUtil.i(getClass().getName() + " onRestoreInstanceState()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        LogUtil.i(getClass().getName() + " onResumeFragments()");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LogUtil.i(getClass().getName() + " onPostResume()");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(getClass().getName() + " onNewIntent()");
    }

    public void startActivity(Class<? extends Activity> cla) {
        Intent intent = new Intent();
        intent.setClass(this, cla);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    public AbstractFragment getFragment(boolean navigationBarShow,
                                        Class<? extends AbstractFragment> hasNoNavigationMainFragmentClass,
                                        Class<? extends AbstractFragment> hasNavigationMainFragmentClass) {
        return getFragment(navigationBarShow, hasNoNavigationMainFragmentClass, hasNavigationMainFragmentClass, null);
    }

    public AbstractFragment getFragment(boolean navigationBarShow,
                                        Class<? extends AbstractFragment> hasNoNavigationMainFragmentClass,
                                        Class<? extends AbstractFragment> hasNavigationMainFragmentClass,
                                        Bundle args) {
//        AbstractMainFragment fragment = (AbstractMainFragment) fragmentManager.findFragmentById(R.id.fragmentContent);
        AbstractFragment fragment;
//        if (fragment == null) {
        if (!navigationBarShow) {
            fragment = (AbstractFragment) getFragment(hasNoNavigationMainFragmentClass, args);
        } else {
            fragment = (AbstractFragment) getFragment(hasNavigationMainFragmentClass, args);
        }
//        }
        return fragment;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    protected Fragment getFragment(Class<? extends AbstractFragment> cla, Bundle args) {
        String simpleName = cla.getSimpleName();
        Fragment fragment = null;
        WeakReference<Fragment> weakReference = mFragments.get(simpleName);
        if (weakReference == null) {
            try {
                fragment = cla.newInstance();
                if (args != null) {
                    fragment.setArguments(args);
                }
                mFragments.put(simpleName, new WeakReference<>(fragment));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            fragment = weakReference.get();
            if (fragment == null) {
                try {
                    fragment = cla.newInstance();
                    if (args != null) {
                        fragment.setArguments(args);
                    }
                    mFragments.put(simpleName, new WeakReference<>(fragment));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                if (args != null) {
                    fragment.setArguments(args);
                }
            }
        }
        return fragment;
    }

    @SuppressWarnings("unused")
    public void switchFragment(Class<? extends AbstractFragment> hasNoNavigationMainFragmentClass,
                               Class<? extends AbstractFragment> hasNavigationMainFragmentClass,
                               boolean isNavigationBarShow, Bundle args) {
        mIsSwitch = true;
//        Fragment newFragment = getFragment(isNavigationBarShow, hasNoNavigationMainFragmentClass, hasNavigationMainFragmentClass);
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragmentContent);
////        JJBoostMainPresenter presenter = null;
//        if (oldFragment != null) {
////            presenter = oldFragment.mPresenter;
////            if (presenter != null) {
////                presenter.onPause();
////                presenter.onDestory();
////                oldFragment.mPresenter = null;
////            }
//            fragmentTransaction.remove(oldFragment);
//        }
////        if (presenter != null) {
//////            presenter = new JJBoostMainPresenter(newFragment);
////            presenter.mainView = newFragment;
////        }
////        newFragment.mPresenter = presenter;
//        fragmentTransaction.add(R.id.fragmentContent, newFragment, newFragment.getClass().getSimpleName()).commit();
////        mCurrentFragment = newFragment;
////        fragmentManager.executePendingTransactions();
    }

    @SuppressWarnings("unused")
    public void switchFragment(Class<? extends AbstractFragment> hasNoNavigationMainFragmentClass,
                               Class<? extends AbstractFragment> hasNavigationMainFragmentClass,
                               boolean isNavigationBarShow) {
        switchFragment(hasNoNavigationMainFragmentClass,
                hasNavigationMainFragmentClass, isNavigationBarShow, null);
    }

}
