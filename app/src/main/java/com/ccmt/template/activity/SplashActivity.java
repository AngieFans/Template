package com.ccmt.template.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.ccmt.template.util.MyAnimationDrawable;
import com.wifi.boost.clean.R;

import java.lang.ref.WeakReference;

public class SplashActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
//        overridePendingTransition(R.anim.stand,R.anim.splash);
        startAnimation();
    }

    private void startAnimation() {
        Handler handler = new Handler();
        ImageView layout = (ImageView) findViewById(R.id.img_bg);
        handler.postDelayed(() -> MyAnimationDrawable.getInstance().animateRawManuallyFromXML(R.drawable.activity_splash,
                layout, null, new InnerRunnable(SplashActivity.this)), 500);
    }

    private static class InnerRunnable implements Runnable {
        private final WeakReference<SplashActivity> mActivity;

        InnerRunnable(SplashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            MyAnimationDrawable.getInstance().release();
            SplashActivity activity = mActivity.get();
            if (activity != null) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.activity_splash;
    }

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected boolean getActivityHasBack() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 启动界面，屏蔽Back键
        // super.onBackPressed();
    }

}
