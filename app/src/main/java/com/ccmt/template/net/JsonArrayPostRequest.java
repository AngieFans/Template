package com.ccmt.template.net;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONArray;

import java.util.Map;

public class JsonArrayPostRequest extends JsonArrayRequest {

    public JsonArrayPostRequest(String url, Map<String, Object> map, Listener<JSONArray> listener,
                                ErrorListener errorListener) {
        super(url, map, listener, errorListener);
    }

}
