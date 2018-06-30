package it.uniba.di.nitwx.progettoMobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    EditText username;
    EditText password;
    TextView register;
    Switch remember;
    SignInButton googleBtn;
    GoogleSignInOptions gso;
    RelativeLayout progressBar;

    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response){
            try {
                Log.d("Prova",response);
                JSONObject temp = new JSONObject(response);
                if(temp.has(Constants.AUTH_TOKEN)){
                    progressBar.setVisibility(View.VISIBLE);
                    HttpController.setCustomHeaders(new JSONObject(response));
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
            error.printStackTrace();
        }
    };

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
        }

        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode,resultCode,data);

    }

    protected  void handleSignInResult(Task<GoogleSignInAccount> task){
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
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
            progressBar.setVisibility(View.VISIBLE);
            signIn();
        }
    };
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onStop(){
        super.onStop();
        progressBar.setVisibility(View.INVISIBLE);

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        progressBar = findViewById(R.id.progress_bar);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        //SIGN IN GOOGLE
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleBtn = (SignInButton) findViewById(R.id.sign_in_button_Google);
        googleBtn.setOnClickListener(googleSignIn);

        //FACEBOOK SIGN IN
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        LoginButton btn = (LoginButton) findViewById(R.id.btnLogInFacebook);
        btn.setReadPermissions("email");
        //btn.setOnClickListener(facebookSignIn);
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        try {
                            progressBar.setVisibility(View.VISIBLE);
                            JSONObject body = new JSONObject();
                            body.put("token",AccessToken.getCurrentAccessToken().getToken());
                            body.put("user_type",Constants.FACEBOOK_USER);
                            HttpController.thirdPartyLogin(body,logInResponseHandler,logInErrorHandler,getApplicationContext());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        exception.printStackTrace();
                    }
                });


        Button logInBtn=(Button) findViewById(R.id.btnLogIn);
        logInBtn.setOnClickListener(logInListener);


        username = findViewById(R.id.txtUsername);
        password = findViewById(R.id.txtPwd);
        remember= findViewById(R.id.swtRemember);
        register = (TextView)findViewById(R.id.txtTapHere);
        register.setOnClickListener(goToRegisterPage);


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
                progressBar.setVisibility(View.VISIBLE);
                JSONObject body= new JSONObject();
                body.put("email",username.getText().toString());
                body.put("password",HttpController.get_SHA_512_SecurePassword(password.getText().toString()));
                body.put("remember_me",remember.isChecked());
                body.put("user_type",Constants.REGISTERD_USER);
                HttpController.login(body,logInResponseHandler, logInErrorHandler, LogIn.this);
            }
            catch (JSONException e){
                e.printStackTrace();
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
