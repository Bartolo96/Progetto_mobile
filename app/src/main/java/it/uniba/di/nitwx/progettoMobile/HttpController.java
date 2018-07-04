package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.RsaSignatureValidator;


public class HttpController {

    /**
     * This method generalizes and customizes Volley's post hhtp request method.
     *
     **/
    static HashMap<String,String> authorizationHeader = new HashMap<>();
    private final static byte[] salt ={
                (byte)0x16 , (byte)0x13 , (byte)0x60 , (byte)0xdb ,
                (byte)0x17 , (byte)0xcf , (byte)0x98 , (byte)0xc0 ,
                (byte)0x8e , (byte)0x85 , (byte)0xde , (byte)0x8c ,
                (byte)0x12 , (byte)0xe7 , (byte)0xbb , (byte)0x6f
    };

    private final static String aesPsawword = "t16imhkowz7s712k";
    private final static String transformation = "AES/ECB/PKCS5Padding";

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

    static void refreshAccessToken(Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{

        String url=Constants.URL_REFRESH_ACCESS_TOKEN;
        //HashMap<String,String> headers = new HashMap<>();
        http_request(Request.Method.GET,context,url,authorizationHeader,null,responseHandler,errorHandler);
    }
    static void login (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url=Constants.URL_AUTH_USER;
        HashMap<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE,Constants.CONTENT_TYPE_APPLICATION_JSON);
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }
    static void thirdPartyLogin (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url=Constants.URL_AUTH_THIRD_PARTY_USER;
        HashMap<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE,Constants.CONTENT_TYPE_APPLICATION_JSON);
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }

    static void getProducts (Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{

        String url=Constants.URL_PRODUCTS;
        //HashMap<String,String> headers = new HashMap<>();
        http_request(Request.Method.GET,context,url,authorizationHeader,null,responseHandler,errorHandler);
    }
    static void addUser (JSONObject body,Response.Listener<String> responseHandler,Response.ErrorListener errorHandler, Context context) throws JSONException{
        String url = Constants.URL_ADD_USER;
        HashMap<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE,Constants.CONTENT_TYPE_APPLICATION_JSON);
        http_request(Request.Method.POST,context,url,headers,body,responseHandler,errorHandler);
    }





    static void saveToken(String string,Context c){

        try {
            String encryptedToken = AESCrypt.encrypt(aesPsawword, string);
            SharedPreferences sharedPref = c.getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.REFRESH_TOKEN, encryptedToken);
            editor.commit();
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }

    }

    static String getToken(Context c) {
        String token="";
        try {
            c.getSharedPreferences(Constants.REFRESH_TOKEN,Context.MODE_PRIVATE);
            SharedPreferences sharedPref = c.getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN,Context.MODE_PRIVATE);
            String encryptedToken = sharedPref.getString(Constants.REFRESH_TOKEN,"Failed");
            token = encryptedToken!= null? AESCrypt.decrypt(aesPsawword,encryptedToken) :null;
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }
        return token;

    }

    static PublicKey getKey(){
        try{
            byte[] byteKey = Base64.decode(Constants.PUBLIC_KEY.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static RsaSignatureValidator v =new RsaSignatureValidator(SignatureAlgorithm.RS256, getKey()) {
        @Override
        protected boolean doVerify(Signature sig, PublicKey pk, byte[] data, byte[] signature) throws InvalidKeyException, java.security.SignatureException {
            throw new InvalidKeyException("prova");
        }
    };


    static String get_SHA_512_SecurePassword(String passwordToHash){
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
