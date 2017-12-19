package com.ccmt.template.net;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ccmt.library.util.ObjectUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class JsonObjectPostRequest extends JsonObjectRequest {

    public JsonObjectPostRequest(String url, Map<String, Object> map, Listener<JSONObject> listener,
                                 ErrorListener errorListener) throws JSONException {
        super(Request.Method.POST, url, new JSONObject(ObjectUtil.obtainJsonManager().toJson(map)), listener,
                errorListener);
    }

}