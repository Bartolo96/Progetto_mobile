package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.LoginResult;
import com.facebook.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class LogIn extends AppCompatActivity {
    CallbackManager callbackManager;

    GoogleSignInClient  mGoogleSignInClient;
    TextView email;
    EditText username;
    EditText password;
    TextView register;
    Switch remember;
    SignInButton googleBtn;
    GoogleSignInOptions gso;
    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response){
            try {
                JSONObject temp = new JSONObject(response);

                Log.d("prova",response);
                if(temp.has(Constants.AUTH_TOKEN)){
                    HttpController.setCustomHeaders(new JSONObject(response));
                    email.setText(response);
                    Intent goToHomeIntent = new Intent(LogIn.this, HomeActivity.class);
                    startActivity(goToHomeIntent);
                }
                else{
                    Toast.makeText(LogIn.this,"",Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    };



    Response.ErrorListener logInErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            email.setText("Error in Volley Response");
        }
    };

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }

        //callbackManager.onActivityResult(requestCode,resultCode,data);

    }

    protected  void handleSignInResult(Task<GoogleSignInAccount> task){
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.d("prova",idToken);
            JSONObject body = new JSONObject();
            body.put("token",idToken);
            body.put("user_type",Constants.GOOGLE_USER);

            HttpController.thirdPartyLogin(body,logInResponseHandler,logInErrorHandler,this);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public View.OnClickListener googleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            signIn();
        }
    };
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public String computeFingerPrint(final byte[] certRaw) {

        String strResult = "";

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
            md.update(certRaw);
            for (byte b : md.digest()) {
                String strAppend = Integer.toString(b & 0xff, 16);
                if (strAppend.length() == 1)
                    strResult += "0";
                strResult += strAppend;
            }
            strResult = strResult.toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        return strResult;
    }
    @Override
    protected void onStart(){
        super.onStart();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete( Task<GoogleSignInAccount> task) {
                handleSignInResult(task);
            }

        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Tries to do a silent sign in
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                //.requestServerAuthCode(getString(R.string.server_client_id))
                .requestEmail()
                .build();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        email = (TextView) findViewById(R.id.testMail);
        PackageManager pm = this.getPackageManager();
        String packageName =this.getPackageName();

        int flags = PackageManager.GET_SIGNATURES;

        PackageInfo packageInfo = null;

        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
            Signature[] signatures = packageInfo.signatures;

            byte[] cert = signatures[0].toByteArray();

            email.setText(computeFingerPrint(cert));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }



        //SIGN IN GOOGLE
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleBtn = (SignInButton) findViewById(R.id.sign_in_button_Google);
        googleBtn.setOnClickListener(googleSignIn);




        //facebook sign in
         callbackManager = CallbackManager.Factory.create();
        //get info about user.app interaction.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        LoginButton btn = (LoginButton) findViewById(R.id.btnLogInFacebook);
        btn.setOnClickListener(facebookSignIn);

        Button logInBtn=(Button) findViewById(R.id.btnLogIn);
        logInBtn.setOnClickListener(logInListener);


        username = findViewById(R.id.txtUsername);
        password = findViewById(R.id.txtPwd);
        remember= findViewById(R.id.swtRemember);
        register = (TextView)findViewById(R.id.txtTapHere);
        register.setOnClickListener(goToRegisterPage);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.d("Culo", "onSuccess");


                        Profile profile = Profile.getCurrentProfile();
                        Log.d("Culo",  "" + profile.getName());
                        email.setText(profile.getName()+profile.getLastName());
                        Intent goToHomeIntent = new Intent(LogIn.this, HomeActivity.class);
                        goToHomeIntent.putExtra("name",profile.getName());
                        startActivity(goToHomeIntent);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
        AccessToken accessToken= AccessToken.getCurrentAccessToken();

    }


    public View.OnClickListener facebookSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LoginManager.getInstance().logInWithReadPermissions(LogIn.this, Arrays.asList("public_profile"));

        }
    };
    public View.OnClickListener logInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                JSONObject body= new JSONObject();
                body.put("email",username.getText().toString());
                body.put("password",password.getText().toString());
                body.put("remember_me",remember.isChecked());
                body.put("user_type",Constants.REGISTERD_USER);
                HttpController.login(body,logInResponseHandler, logInErrorHandler, LogIn.this);
            }
            catch (JSONException e){
                email.setText(e.getMessage());
            }
        }
    };

    public View.OnClickListener goToRegisterPage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LogIn.this,RegisterActivity.class);
            startActivity(intent);
        }
    };



}
