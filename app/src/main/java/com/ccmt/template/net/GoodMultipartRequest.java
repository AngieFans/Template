package com.ccmt.template.net;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class GoodMultipartRequest<T> extends Request<T> {

    private MultipartEntity entity = new MultipartEntity();

    private final Listener<T> mListener;

    private List<File> mFileParts;
    private String mFilePartName;
    private Map<String, String> mParams;

    /**
     * 单个文件
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param file
     * @param params
     */
    @SuppressWarnings("JavaDoc")
    public GoodMultipartRequest(String url,
                                Response.ErrorListener errorListener,
                                Response.Listener<T> listener, String filePartName, File file,
                                Map<String, String> params) {
        super(Method.POST, url, errorListener);

        mFileParts = new ArrayList<>();
        if (file != null) {
            mFileParts.add(file);
        }
        mFilePartName = filePartName;
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }

    /**
     * 单个文件
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param params
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public GoodMultipartRequest(String url,
                                Response.ErrorListener errorListener, Listener<T> listener,
                                Map<String, String> params) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }

    /**
     * 多个文件，对应一个key
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param files
     * @param params
     */
    @SuppressWarnings("JavaDoc")
    public GoodMultipartRequest(String url,
                                Response.ErrorListener errorListener, Listener<T> listener,
                                String filePartName, List<File> files, Map<String, String> params) {
        super(Method.POST, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mFileParts = files;
        mParams = params;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        if (mFileParts != null && mFileParts.size() > 0) {
            for (File file : mFileParts) {
                entity.addPart(mFilePartName, new FileBody(file));
            }
            // long l = entity.getContentLength();
            // Log(mFileParts.size()+"个，长度："+l);
        }

        try {
            if (mParams != null && mParams.size() > 0) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    entity.addPart(
                            entry.getKey(),
                            new StringBody(entry.getValue(), Charset
                                    .forName("UTF-8")));
                }
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }

    @Override
    public String getBodyContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        if (VolleyLog.DEBUG) {
            if (response.headers != null) {
                for (Map.Entry<String, String> entry : response.headers
                        .entrySet()) {
                    VolleyLog.d(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        try {
            return (Response<T>) Response.success(new JSONObject(parsed),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        VolleyLog.d("getHeaders");
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }
        return headers;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

}