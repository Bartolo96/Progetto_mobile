package it.uniba.di.nitwx.progettoMobile;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicol on 25/06/2018.
 */

public class HttpController {
    private JSONObject jsonResponse;

    public JSONObject post(String url,String ContentType,Map<String,String> customHeaders,JSONObject body){
        final Map<String,String> tmpHeaders=customHeaders;
        final String requestBody = body.toString();

        JSONObject returnObj=new JSONObject();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        jsonResponse=response;

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    //headers.put("Content-Type", "application/json");
                    for (String cazzo: headers.keySet()) {
                         headers.put(cazzo,tmpHeaders.get(cazzo));
                    }
                    return headers;
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
        //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        return returnObj;
    }
}
