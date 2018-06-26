package it.uniba.di.nitwx.progettoMobile;

import android.content.Context;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicol on 25/06/2018.
 */

public class HttpController {
    private JSONObject jsonResponse;
    /**
     * This method generalizes and customizes Volley's post hhtp request method.
     *
     **/
    private static void post(Context context,String url, Map<String,String> customHeaders,
                             JSONObject body, Response.Listener<String> responseHandler, Response.ErrorListener errorHandler ){
        final Map<String,String> tmpHeaders=customHeaders;
        final String requestBody = body.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,responseHandler,errorHandler){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    //headers.put("Content-Type", "application/json");
                    for (String headerName: headers.keySet()) {
                         headers.put(headerName,tmpHeaders.get(headerName));
                    }
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public byte[] getBody(){
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                    }
                catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };


        // Access the RequestQueue through your singleton class.
        requestQueue.add(stringRequest);
    }
    /**
     * This method authenticates the user and recieves as response authentication tokens
     **/
    public static void login (Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url="http://nitwx.000webhostapp.com/auth/authenticate_user";
        HashMap<String,String> headers = new HashMap<>();
        JSONObject body= new JSONObject();
        body.put("email","test");
        body.put("password","test");
        body.put("remember_me",true);
        post(context,url,headers,body,responseHandler,errorHandler);
    }
}
