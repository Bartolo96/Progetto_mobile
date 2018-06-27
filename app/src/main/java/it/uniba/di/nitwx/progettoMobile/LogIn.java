package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
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
    EditText password;
    TextView register;
    Switch remember;

    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response){
            try {
                JSONObject temp = new JSONObject(response);
                if(temp.has("authtoken")){
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
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount user=task.getResult();
            String userEmail=user.getEmail();
            email.setText(userEmail);
            Intent goToHomeIntent = new Intent(LogIn.this, HomeActivity.class);
            goToHomeIntent.putExtra("name",userEmail);
            startActivity(goToHomeIntent);
        }
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
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

    public View.OnClickListener googleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            signIn();
        }
    };
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
                HttpController.login(username.getText().toString(),password.getText().toString(),remember.isChecked(),logInResponseHandler, logInErrorHandler, LogIn.this);
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
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

}
