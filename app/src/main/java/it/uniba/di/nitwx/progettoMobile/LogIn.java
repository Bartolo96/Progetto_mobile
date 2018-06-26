package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.LoginResult;
import com.facebook.*;

import java.util.Arrays;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class LogIn extends AppCompatActivity {
    CallbackManager callbackManager;
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
    GoogleSignInClient  mGoogleSignInClient;
    TextView email;
    EditText username;
    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            email.setText(response);
        }
    };
    Response.ErrorListener logInErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            email.setText("test");
        }
    };
    private static final int RC_SIGN_IN = 9001;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount user=task.getResult();
            String userEmail=user.getEmail();
            email.setText(userEmail);
        }
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        email = (TextView) findViewById(R.id.testMail);
        //sign in google
        mGoogleSignInClient= GoogleSignIn.getClient(this, gso);
        SignInButton googleBtn = (SignInButton) findViewById(R.id.sign_in_button_Google);
        googleBtn.setOnClickListener(googleSignIn);
        //facebook sign in
         callbackManager = CallbackManager.Factory.create();
        //get info about user.app interaction.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        LoginButton btn = (LoginButton) findViewById(R.id.btnLogInFacebook);
        btn.setReadPermissions( Arrays.asList("public_profile"));
        Button logInBtn=(Button) findViewById(R.id.btnLogIn);
        logInBtn.setOnClickListener(logInListener);
        username = findViewById(R.id.txtUsername);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        AccessToken accessToken = loginResult.getAccessToken();
                        if (accessToken != null) {
                            Toast.makeText(LogIn.this, "successo", Toast.LENGTH_LONG);
                        }
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
    }
    public View.OnClickListener googleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            signIn();
        }
    };
    public View.OnClickListener logInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                HttpController.login(logInResponseHandler, logInErrorHandler, LogIn.this);
            }
            catch (JSONException e){
                email.setText(e.getMessage());
            }
        }
    };
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

}
