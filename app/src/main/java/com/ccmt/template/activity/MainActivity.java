package com.ccmt.template.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.ccmt.template.R;

public class MainActivity extends AbstractActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MyLog", "onCreate()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MyLog", "onDestroy()");
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected String getActivityTitle() {
        return "主界面";
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return false;
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
        Log.i("MyLog", "onResume()");
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        LogUtil.i("JJBoostMainActivity onWindowFocusChanged()");
//        LinearLayout vgJJIcon = (LinearLayout) findViewById(R.id.vgJJIcon);
//        vgJJIcon.setOnClickListener(v -> LogUtil.i("ha"));
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) vgJJIcon.getLayoutParams();
//        ValueAnimator top = ValueAnimator.ofInt(layoutParams.topMargin, layoutParams.topMargin - 600);
//        top.setDuration(1000);
//        top.addUpdateListener(animation -> {
//            layoutParams.topMargin = (int) animation.getAnimatedValue();
//            vgJJIcon.setLayoutParams(layoutParams);
//        });
//        top.start();
////        AnimatorSet animatorSet = new AnimatorSet();
////        animatorSet.setDuration(1000);
////        animatorSet.playTogether(top, bottom);
//////        animatorSet.addListener(new AnimatorListenerAdapter() {
//////            @Override
//////            public void onAnimationEnd(Animator animation) {
//////                super.onAnimationEnd(animation);
//////                vgJJIcon.requestLayout();
//////            }
//////        });
////        animatorSet.start();
//        LogUtil.i("Arrays.toString(top.getValues()) -> " + Arrays.toString(top.getValues()));
//        LogUtil.i("top.getAnimatedValue() -> " + top.getAnimatedValue());
//        top.setFloatValues(200, 300);
//        top.setDuration(2000);
//        LogUtil.i("Arrays.toString(top.getValues()) -> " + Arrays.toString(top.getValues()));
//        LogUtil.i("top.getAnimatedValue() -> " + top.getAnimatedValue());
//    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MyLog", "onPause()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("MyLog", "onKeyDown()");
//        if (keyCode==KeyEvent.KEYCODE_BACK) {
//            onBackPressed();
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }

}