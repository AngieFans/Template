package com.ccmt.template.webview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ccmt.library.lru.LruMap;

import org.apache.http.HttpStatus;

/**
 * @author myx
 *         by 2017-08-08
 */
class WebViewManager {

    private static boolean sIsSetAppCachePath;
    private WebView mWebView;
    private boolean mIsDisableCache;

    private WebViewManager() {

    }

    public static WebViewManager getInstance() {
        LruMap lruMap = LruMap.getInstance();
        String name = WebViewManager.class.getName();
        WebViewManager webViewManager = (WebViewManager) lruMap.get(name);
        if (webViewManager == null) {
            webViewManager = new WebViewManager();
            lruMap.put(name, webViewManager);
        }
        return webViewManager;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(WebView webView, boolean isDisableCache) {
        mWebView = webView;
        mIsDisableCache = isDisableCache;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);

        // 设置自适应屏幕,两者合用.
        // 将图片调整到适合webview的大小
        webSettings.setUseWideViewPort(true);

        // 缩放至屏幕的大小
        webSettings.setLoadWithOverviewMode(true);

        // 缩放操作
        // 支持缩放,默认为true.是下面那个的前提.
        webSettings.setSupportZoom(true);

        // 设置内置的缩放控件.若为false,则该WebView不可缩放.
        webSettings.setBuiltInZoomControls(true);

        // 隐藏原生的缩放控件
        webSettings.setDisplayZoomControls(false);

        // 其他细节操作
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);

        // 支持通过JS打开新窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);

        // 设置编码格式
        webSettings.setDefaultTextEncodingName("utf-8");

        if (isDisableCache) {
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

            // 开启Application Caches功能
            webSettings.setAppCacheEnabled(true);

//             如果需要存储一些简单的用key/value对即可解决的数据,DOM Storage是非常完美的方案.根据作用范围的不同,
//             有Session Storage和Local Storage两种,分别用于会话级别的存储(页面关闭即消失)和本地化存储(除非主动删除,否则数据永远不会过期.).
//             在Android中可以手动开启DOM Storage(setDomStorageEnabled),设置存储路径(setDatabasePath).
//             Android中Webkit会为DOM Storage产生两个文件(my_path/localstorage/http_h5.m.taobao.com_0.localstorage
//             和my_path/localstorage/Databases.db).
            // 开启DOM storage API功能
//            webSettings.setDomStorageEnabled(true);

            // 开启database storage API功能
//            webSettings.setDatabaseEnabled(true);

            // 设置 Application Caches缓存目录
            if (!sIsSetAppCachePath) {
                sIsSetAppCachePath = true;
                String dir = webView.getContext().getFilesDir().getAbsolutePath();
                webSettings.setAppCachePath(dir + "/AppCache");
//                webSettings.setDatabasePath(dir + "/DatabaseCache");
            }
        }

        // 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为,使网页用WebView打开.
        webView.setWebViewClient(new CcmtWebViewClient());

//        webView.setWebChromeClient(new CcmtWebChromeClient());
    }

    public void onDestroy() {
        if (mIsDisableCache) {
            cleardata();
        }

//        mWebView.removeAllViews();
        ((ViewGroup) mWebView.getParent()).removeView(mWebView);
//        ReflectUtils.removeContext(mWebView);

        mWebView.destroy();

        mIsDisableCache = false;
        mWebView = null;
        sIsSetAppCachePath = false;
    }

    void onResume() {
        mWebView.onResume();
    }

    void onPause() {
        mWebView.onPause();
    }

    private static class CcmtWebViewClient extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 返回值是true的时候控制去WebView打开,为false调用系统浏览器或第三方浏览器.
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LogUtil.i("WebViewManager onPageStarted()");
            LogUtil.i("url -> " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            LogUtil.i("WebViewManager onPageFinished()");
            LogUtil.i("url -> " + url);
            if (url.endsWith(".apk")) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                view.getContext().startActivity(intent);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
//            LogUtil.i("WebViewManager onLoadResource()");
//            LogUtil.i("url -> " + url);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            LogUtil.i("WebViewManager onReceivedError()");
            LogUtil.i("errorCode -> " + errorCode);
            LogUtil.i("description -> " + description);
            LogUtil.i("failingUrl -> " + failingUrl);
            switch (errorCode) {
                case HttpStatus.SC_NOT_FOUND:
                    break;
            }
        }

        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            super.onReceivedClientCertRequest(view, request);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            LogUtil.i("WebViewManager onReceivedSslError()");
            LogUtil.i("error -> " + error);

            // 表示等待证书响应
            handler.proceed();

            // 表示挂起连接,为默认方式.
            // handler.cancel();

            // 可做其他处理
            // handler.handleMessage(null);
        }
    }

    @SuppressWarnings("unused")
    private static class CcmtWebChromeClient extends WebChromeClient {
        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            LogUtil.i("WebViewManager onProgressChanged()");
            LogUtil.i("newProgress -> " + newProgress);
            if (newProgress == 100) {
                // 网页加载完成
            } else {
                // 加载中
            }
        }
    }

    @SuppressWarnings("unused")
    public void loadUrlFromAsset(String assetFileName) {
        mWebView.loadUrl("file:///android_asset/" + assetFileName);
    }

    @SuppressWarnings("unused")
    void loadUrlFromUrl(String url) {
        mWebView.loadUrl(url);
    }

    boolean canGoBack() {
        return mWebView.canGoBack();
    }

    void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void cleardata() {
        // 清除网页访问留下的缓存,由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
        mWebView.clearCache(true);

        // 清除当前webview访问的历史记录,只会webview访问历史记录里的所有记录除了当前访问记录.
        mWebView.clearHistory();

        // 这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
        mWebView.clearFormData();
    }

}
