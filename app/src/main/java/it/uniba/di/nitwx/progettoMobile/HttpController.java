package it.uniba.di.nitwx.progettoMobile;

import android.content.Context;
import android.util.Log;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class HttpController {

    private static HashMap<String,String> customHeaders = null;
    /**
     * This method generalizes and customizes Volley's post hhtp request method.
     *
     **/
    private static void http_request(int requestType, Context context,String url, Map<String,String> customHeaders,
                             JSONObject body, Response.Listener<String> responseHandler, Response.ErrorListener errorHandler ){
        final Map<String,String> tmpHeaders=customHeaders;
        final String requestBody;
        if(body!=null)
            requestBody = body.toString();
        else
            requestBody=null;

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(requestType, url,responseHandler,errorHandler){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    if (tmpHeaders!=null){
                        //headers.put("Content-Type", "application/json");
                        for (String headerName : tmpHeaders.keySet()) {
                            headers.put(headerName, tmpHeaders.get(headerName));
                        }
                    }
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    return Constants.CONTENT_TYPE_APPLICATION_JSON;
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

    public static void setCustomHeaders (JSONObject jsonHeaders) throws JSONException{
        customHeaders = new HashMap<>();
        if(jsonHeaders.has(Constants.AUTH_TOKEN)){
            customHeaders.put(Constants.AUTH_TOKEN,jsonHeaders.getString(Constants.AUTH_TOKEN));

        }
        if(jsonHeaders.has(Constants.REFRESH_TOKEN)){
            customHeaders.put(Constants.REFRESH_TOKEN,jsonHeaders.getString(Constants.REFRESH_TOKEN));
        }

    }
    public static void login (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url=Constants.URL_AUTH_USER;
        HashMap<String,String> headers = new HashMap<>();
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }
    public static void thirdPartyLogin (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url=Constants.URL_AUTH_THIRD_PARTY_USER;
        HashMap<String,String> headers = new HashMap<>();
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }

    public static void getProducts (Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{

        String url=Constants.URL_PRODUCTS;

        http_request(Request.Method.GET,context,url,customHeaders,null,responseHandler,errorHandler);
    }
    public static void addUser (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url = Constants.URL_ADD_USER;
        HashMap<String,String> headers = new HashMap<>();
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }
    public static String get_SHA_512_SecurePassword(String passwordToHash){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }

}
