package com.ccmt.template.net;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.NetUtil;
import com.wifi.boost.clean.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class HttpManager {

    private static final String TAG = "VolleyPatterns";
    @SuppressWarnings("unused")
    private static final String HOST;
    @SuppressWarnings("unused")
    public static final String API_KEY = "Supseruser201690449a1531d98f682168";
    private volatile RequestQueue mRequestQueue;
    private Context mContext;

    static {
        if (BuildConfig.IS_CHINA) {
            HOST = "http://api-cn.supersuer.com:8000";
        } else {
            HOST = "http://api.supersuer.com";
        }

        // 测试自升级
//        HOST = "http://ec2-54-222-216-155.cn-north-1.compute.amazonaws.com.cn:8000";
    }

    @SuppressWarnings("WeakerAccess")
    public interface INetworkError {
        void networkError();
    }

    private HttpManager(Context context) {
        this.mContext = context;

        Class<DefaultRetryPolicy> cla = DefaultRetryPolicy.class;
        Field defaultTimeoutMs;
        Field defaultMaxRetries;
        try {
            defaultTimeoutMs = cla.getDeclaredField("DEFAULT_TIMEOUT_MS");
            defaultMaxRetries = cla.getDeclaredField("DEFAULT_MAX_RETRIES");
            defaultTimeoutMs.setAccessible(true);
            defaultMaxRetries.setAccessible(true);

            try {
                defaultTimeoutMs.set(null, 6000);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            try {
                defaultMaxRetries.set(null, 0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static HttpManager getInstance(Context context) {
        LruMap lruMap = LruMap.getInstance();
        String name = HttpManager.class.getName();
        HttpManager httpManager = (HttpManager) lruMap.get(name);
        if (httpManager == null) {
            httpManager = new HttpManager(context.getApplicationContext());
            lruMap.put(name, httpManager);
        }
        return httpManager;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (this) {
                if (mRequestQueue == null) {
                    mRequestQueue = Volley.newRequestQueue(mContext);
                }
            }
        }
        return mRequestQueue;
    }

    @SuppressWarnings("unused")
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    private <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    @SuppressWarnings("unused")
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    /**
     * 通用post请求接口
     *
     * @param url
     * @param param
     * @param listener
     * @param errorListener
     * @param networkError  没有网络连接的回调
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public void postJSONObject(String url, Map<String, Object> param,
                               Listener<JSONObject> listener,
                               ErrorListener errorListener,
                               INetworkError networkError) {
        if (NetUtil.checkNet(mContext)) {
            postJsonObject(url, param, listener, errorListener);
        } else {
//            ToastUtil.showLong(mContext, mContext.getString(R.string.update_no_network));
            if (networkError != null) {
                networkError.networkError();
            }
        }
    }

    /**
     * @param url
     * @param param
     * @param listener
     * @param errorListener
     * @param networkError  没有网络连接的回调
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public void postJSONArray(String url, Map<String, Object> param,
                              Listener<JSONArray> listener,
                              ErrorListener errorListener,
                              INetworkError networkError) {
        if (NetUtil.checkNet(mContext)) {
            postJsonArray(url, param, listener, errorListener);
        } else {
            if (networkError != null) {
                networkError.networkError();
            }
        }
    }

    private void postJsonObject(String url, Map<String, Object> param,
                                Listener<JSONObject> listener,
                                ErrorListener errorListener) {
        try {
//            LogUtil.i("ObjectUtil.obtainJsonManager().toJson(param) -> " + ObjectUtil.obtainJsonManager().toJson(param));
            addToRequestQueue(new JsonObjectPostRequest(url, param, listener, errorListener));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void postJsonArray(String url, Map<String, Object> param,
                               Listener<JSONArray> listener,
                               ErrorListener errorListener) {
        addToRequestQueue(new JsonArrayPostRequest(url, param, listener, errorListener));
    }

    @SuppressWarnings({"unchecked", "unused"})
    private <T> void updateFile(String url, Map<String, String> param,
                                String fileName, File file, Listener<T> listener,
                                ErrorListener errorListener) {
        addToRequestQueue(new GoodMultipartRequest(url, errorListener,
                listener, fileName, file, param));
    }

    @SuppressWarnings({"unchecked", "unused"})
    private <T> void updateMultiFile(String url, Map<String, String> param,
                                     String fileName, List<File> files, Listener<T> listener,
                                     ErrorListener errorListener) {
        addToRequestQueue(new GoodMultipartRequest(url, errorListener,
                listener, fileName, files, param));
    }

}
