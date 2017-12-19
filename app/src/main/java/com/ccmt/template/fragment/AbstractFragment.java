package com.ccmt.template.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;

import com.ccmt.library.util.ReflectUtils;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.activity.AbstractActivity;

public abstract class AbstractFragment extends Fragment {

    protected static AbstractFragment sFragment;
    private AbstractActivity mContext;
    protected boolean mIsLoadData;
    protected boolean mIsDestroy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(getClass().getName() + " onCreate()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(getClass().getName() + " onDestroy()");
//        doOnDestroy();
//        JJBoostApplication.getRefWatcher().watch(this);
        AbstractActivity activity = getContextNew();
        if (activity == null) {
            return;
        }
        if (activity != getActivity()) {
            return;
        }
        if (!activity.mIsSwitch) {
            mIsDestroy = true;

            // 如果用户没有显示或隐藏导航栏导致Fragment被销毁,而是点返回键正常退出,
            // 需要把Fragment的所有静态变量设置为初始状态,避免内存泄露.
            ReflectUtils.setStaticFieldValues(getRecycleStaticFieldClass(), null);
            sFragment = null;
        }
    }

//    protected abstract void doOnDestroy();

    /**
     * 由于getActivity()方法不能重写,就写个新方法了.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public AbstractActivity getContextNew(Class<? extends AbstractActivity> cla) {
        if (mContext == null) {
            // 用户显示或隐藏导航栏时,如果是以remove和add方式替换的Fragment,
            // 那么被隐藏的Fragment已经没在视图树中了,此时调用getActivity()方法也会返回null.
            AbstractActivity activity = (AbstractActivity) getActivity();
            if (activity != null) {
                return activity;
            }
            if (AbstractActivity.sActivity != null) {
                if (cla != null) {
                    if (AbstractActivity.sActivity.getClass() == cla) {
                        return AbstractActivity.sActivity;
                    }
                    LogUtil.i("AbstractActivity.sActivity不是当前Fragment对象所依附的Activity");
                } else {
                    return AbstractActivity.sActivity;
                }
            }
        }
        return mContext;
    }

    public AbstractActivity getContextNew() {
        return getContextNew(null);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        LogUtil.i(getClass().getName() + " onAttachFragment()");
        LogUtil.i("childFragment -> " + childFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LogUtil.i(getClass().getName() + " onAttach()");
        LogUtil.i("context -> " + context);
        mContext = (AbstractActivity) context;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        LogUtil.i(getClass().getName() + " onCreateAnimation()");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.i(getClass().getName() + " onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        AbstractActivity activity = getContextNew();
        if (activity == null) {
            return null;
        }
        if (activity != getActivity()) {
            return null;
        }
        View view = null;
        if (activity.isContentNavigationFragment()) {
            Class<? extends AbstractFragment> hasNoNavigationMainFragmentClass = activity.getHasNoNavigationFragmentClass();
            Class<? extends AbstractFragment> hasNavigationMainFragmentClass = activity.getHasNavigationFragmentClass();
            if (getClass() == hasNoNavigationMainFragmentClass) {
                view = FragmentManager.getInstance().getView(hasNoNavigationMainFragmentClass,
                        inflater, container, getHasNoNavigationFragmentLayoutResourceId(), false);
            } else if (getClass() == hasNavigationMainFragmentClass) {
                view = FragmentManager.getInstance().getView(hasNavigationMainFragmentClass,
                        inflater, container, getHasNavigationFragmentLayoutResourceId(), false);
            }
        } else {
            view = doOnCreateView(inflater, container, savedInstanceState);
        }
        if (view != null) {
            Boolean tag = (Boolean) view.getTag();
            if (tag != null && tag) {
                LogUtil.i("新创建的View对象");
                View viewTemp = view;
                viewTemp.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        changeView(viewTemp);
                        ViewUtil.removeOnGlobalLayoutListener(viewTemp, this);
                    }
                });
            } else {
                LogUtil.i("从缓存中获取的View对象");
            }
            initView(view);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtil.i(getClass().getName() + " onViewCreated()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);
        AbstractActivity activity = getContextNew();
//        if (activity == null) {
//            return;
//        }
//        if (activity != getActivity()) {
//            return;
//        }
        if (activity.mIsSwitch) {
            restoreState(view, savedInstanceState);
        } else {
            doOnViewCreated(view, savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.i(getClass().getName() + " onActivityCreated()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        LogUtil.i(getClass().getName() + " onViewStateRestored()");
        LogUtil.i("savedInstanceState -> " + savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.i(getClass().getName() + " onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        AbstractActivity context = getContextNew();
        if (context == null) {
            return;
        }
        if (context.mIsSwitch) {
            if (!isSwitchDoOnResume()) {
                context.mIsSwitch = false;
                return;
            }
        }
        LogUtil.i(getClass().getName() + " onResume()");
        onResumeBefore();
        if (context.mIsSwitch) {
            context.mIsSwitch = false;
        }
        onResumeAfter();
    }

    /**
     * 不管用户有没有显示或隐藏导航栏,mIsSwitch标记被修改前会调用该方法.
     */
    protected abstract void onResumeBefore();

    /**
     * 不管用户有没有显示或隐藏导航栏,mIsSwitch标记被修改后会调用该方法.
     */
    protected abstract void onResumeAfter();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtil.i(getClass().getName() + " onSaveInstanceState()");
        LogUtil.i("outState -> " + outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.i(getClass().getName() + " onConfigurationChanged()");
        LogUtil.i("newConfig -> " + newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i(getClass().getName() + " onPause()");
        AbstractActivity activity = getContextNew();
        if (activity == null) {
            return;
        }
        if (activity != getActivity()) {
            return;
        }
        if (activity.mIsSwitch) {
            saveState(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.i(getClass().getName() + " onStop()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtil.i(getClass().getName() + " onLowMemory()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.i(getClass().getName() + " onDestroyView()");
    }

    @SuppressWarnings({"TryWithIdenticalCatches", "EmptyFinallyBlock"})
    @Override
    public void onDetach() {
        super.onDetach();
        LogUtil.i(getClass().getName() + " onDetach()");
        ReflectUtils.setNonStaticFieldValue(Fragment.class, this, "mChildFragmentManager", null);
        mContext = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.i(getClass().getName() + " onHiddenChanged()");
        LogUtil.i("hidden -> " + hidden);
    }

    /**
     * 不考虑用户没有显示或隐藏导航栏,Activity对象的isContentFragment()方法返回true时走这里.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @SuppressWarnings("JavaDoc")
    protected abstract View doOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected abstract void changeView(View root);

    /**
     * 用户没有显示或隐藏导航栏,走正常流程.
     *
     * @param view
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    protected abstract void doOnViewCreated(View view, @Nullable Bundle savedInstanceState);

    /**
     * 用户显示或隐藏导航栏时,被显示的Fragment如果有数据需要恢复在该方法进行恢复状态处理.
     *
     * @param view
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    protected void restoreState(View view, @Nullable Bundle savedInstanceState) {
        mIsLoadData = sFragment.mIsLoadData;
    }

    /**
     * 用户显示或隐藏导航栏时,被隐藏的Fragment如果有数据需要保存在该方法进行保存状态处理.
     *
     * @param fragment
     */
    @SuppressWarnings("JavaDoc")
    protected void saveState(AbstractFragment fragment) {
        sFragment = fragment;
    }

    protected abstract int getHasNoNavigationFragmentLayoutResourceId();

    protected abstract int getHasNavigationFragmentLayoutResourceId();

    protected abstract void initView(View view);

    /**
     * 获取在用户点返回键时,需要将静态变量置空的Fragment的Class对象.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    protected abstract Class<?> getRecycleStaticFieldClass();

    /**
     * 如果用户显示或隐藏导航栏,是否走OnResume流程.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    protected boolean isSwitchDoOnResume() {
        return true;
    }

    /**
     * 向服务端调接口或本地操作获取数据
     */
    @SuppressWarnings("unused")
    protected void loadData() {
        if (ThreadManager.isOnMainThread()) {
            mIsLoadData = true;
        } else {
            getActivity().runOnUiThread(() -> mIsLoadData = true);
        }
    }

}
