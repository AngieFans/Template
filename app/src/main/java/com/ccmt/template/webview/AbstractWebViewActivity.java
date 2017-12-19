package com.ccmt.template.webview;

import android.os.Bundle;

import com.ccmt.template.activity.AbstractActivity;

/**
 * @author myx
 *         by 2017-08-10
 */
public abstract class AbstractWebViewActivity extends AbstractActivity {

    protected WebViewManager mWebViewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebViewManager = WebViewManager.getInstance();
//        mWebViewManager.onCreate((WebView) findViewById(R.id.wv), false);
        String url = getUrl();
        if (url != null) {
            mWebViewManager.loadUrlFromUrl(url);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebViewManager.onDestroy();
        mWebViewManager = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebViewManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebViewManager.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mWebViewManager.canGoBack()) {
            mWebViewManager.goBack();
        }
        super.onBackPressed();
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
    protected int obtainLayoutResID() {
        return 0;
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
    protected boolean getActivityHasSetting() {
        return false;
    }

    protected abstract String getUrl();

}
