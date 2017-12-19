package com.ccmt.template.util;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ReflectUtils;
import com.ccmt.library.util.ScreenUtils;
import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.activity.AbstractActivity;

public class NavigationBarManager {

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;
    private View mViewObserved;
    @SuppressWarnings("unused")
    private int mUsableHeightPrevious;
    @SuppressWarnings("unused")
    private ViewGroup.LayoutParams mFrameLayoutParams;
    private boolean mIsNavigationBarShow;

    private NavigationBarManager() {

    }

    public static NavigationBarManager getInstance() {
        LruMap lruMap = LruMap.getInstance();
        String name = NavigationBarManager.class.getName();
        NavigationBarManager navigationBarManager = (NavigationBarManager) lruMap.get(name);
        if (navigationBarManager == null) {
            navigationBarManager = new NavigationBarManager();
            lruMap.put(name, navigationBarManager);
        }
        return navigationBarManager;
    }

    /**
     * 关联要监听的视图
     *
     * @param viewObserving
     * @param isNavigationBarShow
     */
    @SuppressWarnings("JavaDoc")
    public void add(View viewObserving, boolean isNavigationBarShow, OnNavigationBarChangeListener onNavigationBarChangeListener) {
        mIsNavigationBarShow = isNavigationBarShow;

        if (mViewObserved != null) {
            remove();
        }
        mViewObserved = viewObserving;

        mFrameLayoutParams = mViewObserved.getLayoutParams();

        // 给View添加全局的布局监听器
        mOnGlobalLayoutListener = () -> resetLayoutByUsableHeight(onNavigationBarChangeListener);
        mViewObserved.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    @SuppressWarnings("WeakerAccess")
    public interface OnNavigationBarChangeListener {
        void onNavigationBarChange(boolean isNavigationBarShow);
    }

    public void remove() {
        if (mViewObserved == null) {
            return;
        }
        ViewUtil.removeOnGlobalLayoutListener(mViewObserved, mOnGlobalLayoutListener);
        ViewGroup parent = (ViewGroup) mViewObserved.getParent();
        if (parent != null) {
            parent.removeView(mViewObserved);
        }
        mViewObserved = null;
        mFrameLayoutParams = null;
        mOnGlobalLayoutListener = null;
        mIsNavigationBarShow = false;
    }

    @SuppressWarnings("deprecation")
    private void resetLayoutByUsableHeight(OnNavigationBarChangeListener onNavigationBarChangeListener) {
        // 比较布局变化前后的View的可用高度
//        LogUtil.showScreenInfo((Activity) mViewObserved.getContext());
//        isNavigationBarShow((Activity) mViewObserved.getContext(), null);
//        int screenHeightReal = ScreenUtils.getScreenHeightReal((Activity) mViewObserved.getContext());
//        int usableHeightNow = mViewObserved.getHeight();
//        View childAt = ScreenUtils.getContentView((Activity) mViewObserved.getContext()).getChildAt(0);
//        LogUtil.i("(childAt==mViewObserved) -> " + (childAt == mViewObserved));
//        LogUtil.i("mUsableHeightPrevious -> " + mUsableHeightPrevious);
//        LogUtil.i("usableHeightNow -> " + usableHeightNow);

//        if (usableHeightNow > 0 && usableHeightNow != mUsableHeightPrevious) {
//            // 如果两次高度不一致
//            // 将当前的View的可用高度设置成View的实际高度
////            if (usableHeightNow != screenHeightReal) {
//            mFrameLayoutParams.height = usableHeightNow;
//            mUsableHeightPrevious = usableHeightNow;
//            mViewObserved.setLayoutParams(mFrameLayoutParams);
////            mViewObserved.requestLayout();
//            mViewObserved.invalidate();
////            ScreenUtils.getContentView((Activity) mViewObserved.getContext()).requestLayout();
////            ScreenUtils.getContentView((Activity) mViewObserved.getContext()).invalidate();
////            }
//        }
        if (mViewObserved.getHeight() < 0) {
            return;
        }
        AbstractActivity activity;
        boolean isNavigationBarShow;
        activity = (AbstractActivity) mViewObserved.getContext();
        isNavigationBarShow = isNavigationBarShow(activity, null);
        if (mIsNavigationBarShow == isNavigationBarShow) {
            return;
        }
        mIsNavigationBarShow = isNavigationBarShow;

//        int navHeight = getNavigationHeightFromResource(activity);
//        if (isNavigationBarShow) {
////            activity.setForNavigationBarBottom(activity);
//
////            FrameLayout content = ((FrameLayout) activity.findViewById(android.R.id.content));
//////            ViewGroup content = (ViewGroup) mViewObserved;
////            int childCount = content.getChildCount();
////            FrameLayout.LayoutParams layoutParams;
////            for (int i = 0; i < childCount; i++) {
////                View childView = content.getChildAt(i);
////
//////                childView.setPadding(0, 0, 0, navHeight);
////                layoutParams = (FrameLayout.LayoutParams) childView.getLayoutParams();
////                layoutParams.topMargin = -navHeight;
//////                childView.setLayoutParams(layoutParams);
////            }
//            View view1 = mViewObserved.findViewById(R.id.vgJJIcon);
//            View view2 = mViewObserved.findViewById(R.id.vgToSpeedUpTime);
//            View view3 = mViewObserved.findViewById(R.id.vgToSpeedUp);
//            ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams) view1.getLayoutParams();
//            ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) view2.getLayoutParams();
//            ViewGroup.MarginLayoutParams layoutParams3 = (ViewGroup.MarginLayoutParams) view3.getLayoutParams();
//            layoutParams1.topMargin -= navHeight;
//            layoutParams2.topMargin -= navHeight;
//            layoutParams3.topMargin -= navHeight;
////            view3.setLayoutParams(layoutParams3);
////            view2.setLayoutParams(layoutParams2);
////            view1.setLayoutParams(layoutParams1);
////            mViewObserved.requestLayout();
////            mViewObserved.invalidate();
//            mViewObserved.setLayoutParams(mViewObserved.getLayoutParams());
//
////            FrameLayout.LayoutParams navBarLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
////                    navHeight, Gravity.BOTTOM);
////            View navBar = new View(activity);
////            navBar.setBackgroundColor(activity.getResources().getColor(R.color.title_bg));
////            content.addView(navBar, navBarLayoutParams);
////            content.requestLayout();
////            content.invalidate();
//        } else {
//            View view1 = mViewObserved.findViewById(R.id.vgJJIcon);
//            View view2 = mViewObserved.findViewById(R.id.vgToSpeedUpTime);
//            View view3 = mViewObserved.findViewById(R.id.vgToSpeedUp);
//            ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams) view1.getLayoutParams();
//            ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) view2.getLayoutParams();
//            ViewGroup.MarginLayoutParams layoutParams3 = (ViewGroup.MarginLayoutParams) view3.getLayoutParams();
//            layoutParams1.topMargin += navHeight;
//            layoutParams2.topMargin += navHeight;
//            layoutParams3.topMargin += navHeight;
////            view3.setLayoutParams(layoutParams3);
////            view2.setLayoutParams(layoutParams2);
////            view1.setLayoutParams(layoutParams1);
////            mViewObserved.requestLayout();
////            mViewObserved.invalidate();
//            mViewObserved.setLayoutParams(mViewObserved.getLayoutParams());
//        }

        LogUtil.i("切换布局");
        LogUtil.showScreenInfo(activity);

//        if (!isNavigationBarShow) {
//            activity.setContentView(R.layout.fragment_main_no_navigation);
//        } else {
//            activity.setContentView(R.layout.fragment_main_navigation);
//        }
//        activity.onCreate(null);
//        ((JJBoostMainActivity) activity).doOnResume();

//        activity.mIsSwitchLayout = true;
//        activity.onBackPressed();

//        activity.switchFragment();
        if (onNavigationBarChangeListener != null) {
            onNavigationBarChangeListener.onNavigationBarChange(isNavigationBarShow);
        }
    }

//    /**
//     * 计算视图可视高度
//     *
//     * @return
//     */
//    @SuppressWarnings("JavaDoc")
//    private int computeUsableHeight() {
//        Rect r = new Rect();
//        mViewObserved.getWindowVisibleDisplayFrame(r);
//        return (r.bottom - r.top);
//    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    private static boolean isNavigationBarShow(Activity activity, Display display) {
//        boolean result;
//        if (Build.VERSION.SDK_INT >= 17) {
//            if (display == null) {
//                display = activity.getWindowManager().getDefaultDisplay();
//            }
//            Point size = new Point();
//            Point realSize = new Point();
//            display.getSize(size);
//            display.getRealSize(realSize);
////            LogUtil.i("size -> " + size);
////            LogUtil.i("realSize -> " + realSize);
//            result = realSize.y != size.y;
//        } else {
//            result = !(ViewConfiguration.get(activity).hasPermanentMenuKey() || KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK));
//        }
//        if (result) {
////            LogUtil.i("导航栏显示");
//        } else {
////            LogUtil.i("导航栏隐藏");
//        }
//        return result;
//    }

    /**
     * 可以调用{@link ScreenUtils#getScreenWeightAndHeightFull(Activity, Display)}方法获取屏幕的宽度和高度信息,
     * 间接获取导航栏高度.
     * 竖屏: 屏幕完整高度 - 屏幕普通高度 = 导航栏高度.
     * 橫屏: 屏幕完整宽度 - 屏幕普通宽度 = 导航栏宽度.
     *
     * @param activity
     * @param display
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public boolean isNavigationBarShow(Activity activity, Display display) {
        final boolean[] result = {false};
        if (Build.VERSION.SDK_INT >= 17) {
            if (display == null) {
                display = activity.getWindowManager().getDefaultDisplay();
            }

//            Point size = new Point();
//            Point realSize = new Point();
//            display.getSize(size);
//            display.getRealSize(realSize);
//            LogUtil.i("size -> " + size);
//            LogUtil.i("realSize -> " + realSize);
//            result[0] = realSize.y != size.y;
            DisplayMetrics size = new DisplayMetrics();
            DisplayMetrics realSize = new DisplayMetrics();
            display.getMetrics(size);
            display.getRealMetrics(realSize);
            LogUtil.i("size -> " + size);
            LogUtil.i("realSize -> " + realSize);
            if (ScreenUtils.isScreenOriatationPortrait(activity)) {
                result[0] = realSize.heightPixels != size.heightPixels;
            } else {
                result[0] = realSize.widthPixels != size.widthPixels;
            }
        } else {
            if (display == null) {
                display = activity.getWindowManager().getDefaultDisplay();
            }

//            Point size = new Point();
//            Point realSize = new Point();
//            display.getSize(size);
//            Display displayTemp = display;
//            final boolean[] flag = {true};
//            ReflectUtils.invokeNonStaticMethod(display, "getRealSize",
//                    new Class[]{Point.class},
//                    () -> {
//                        Object obj = ReflectUtils.invokeNonStaticMethod(displayTemp, "getRawHeight", null,
//                                () -> flag[0] = false);
//                        if (obj != null) {
//                            realSize.y = (int) obj;
//                        }
//                    }, realSize);
//            LogUtil.i("size -> " + size);
//            LogUtil.i("realSize -> " + realSize);
            DisplayMetrics size = new DisplayMetrics();
            DisplayMetrics realSize = new DisplayMetrics();
            display.getMetrics(size);
            Display displayTemp = display;
            final boolean[] flag = {true};
            ReflectUtils.invokeNonStaticMethod(display, "getRealMetrics", new Class[]{DisplayMetrics.class},
                    () -> {
                        Point realSize2 = new Point();
                        final boolean[] flag2 = {true};
                        ReflectUtils.invokeNonStaticMethod(displayTemp, "getRealSize",
                                new Class[]{Point.class},
                                () -> {
                                    flag2[0] = false;
                                    if (ScreenUtils.isScreenOriatationPortrait(activity)) {
                                        Object obj = ReflectUtils.invokeNonStaticMethod(displayTemp, "getRawHeight", null,
                                                () -> flag[0] = false);
                                        if (obj != null) {
                                            realSize.heightPixels = (int) obj;
                                        }
                                    } else {
                                        Object obj = ReflectUtils.invokeNonStaticMethod(displayTemp, "getRawWidth", null,
                                                () -> flag[0] = false);
                                        if (obj != null) {
                                            realSize.widthPixels = (int) obj;
                                        }
                                    }
                                }, realSize2);
                        if (flag2[0]) {
                            if (ScreenUtils.isScreenOriatationPortrait(activity)) {
                                realSize.heightPixels = realSize2.y;
                            } else {
                                realSize.widthPixels = realSize2.x;
                            }
                        }
                    }, realSize);
            LogUtil.i("size -> " + size);
            LogUtil.i("realSize -> " + realSize);

            if (flag[0]) {
                if (ScreenUtils.isScreenOriatationPortrait(activity)) {
                    if (realSize.heightPixels >= size.heightPixels) {
                        result[0] = realSize.heightPixels != size.heightPixels;
                    } else {
                        handlerNavigationBar(activity, result);
                    }
                } else {
                    if (realSize.widthPixels >= size.widthPixels) {
                        result[0] = realSize.widthPixels != size.widthPixels;
                    } else {
                        handlerNavigationBar(activity, result);
                    }
                }
            } else {
                handlerNavigationBar(activity, result);
            }
        }
        if (result[0]) {
            com.ccmt.library.util.LogUtil.i("导航栏显示");
        } else {
            com.ccmt.library.util.LogUtil.i("导航栏隐藏");
        }
        return result[0];
    }

    private static void handlerNavigationBar(Activity activity, boolean[] result) {
        if (Build.VERSION.SDK_INT >= 14) {
            result[0] = !(ViewConfiguration.get(activity).hasPermanentMenuKey()
                    || KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK));
        } else {
            Object obj = ReflectUtils.obtainNonStaticFieldValue(ViewConfiguration.get(activity), "sHasPermanentMenuKey");
            if (obj != null) {
                result[0] = !((boolean) obj || KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK));
            } else {
                result[0] = !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            }
        }
    }

}
