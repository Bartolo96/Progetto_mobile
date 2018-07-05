package it.uniba.di.nitwx.progettoMobile;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.LoginResult;
import com.facebook.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;


public class LogIn extends AppCompatActivity {
    CallbackManager callbackManager;
    GoogleSignInClient mGoogleSignInClient;
    EditText username;
    EditText password;
    TextView register;
    Switch remember;
    SignInButton googleBtn;
    GoogleSignInOptions gso;
    RelativeLayout progressBar;
    PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private final int REQUEST_FINE_ACCESS = 1;

    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {

                if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);
                Log.d("Provaaaaa", response);
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has(Constants.AUTH_TOKEN)) {

                    JSONObject jsonAccessToken = jsonResponse.getJSONObject(Constants.AUTH_TOKEN);
                    HttpController.userClaims = Jwts.parser().setSigningKey(HttpController.getKey()).
                            parseClaimsJws(jsonAccessToken.getString(Constants.AUTH_TOKEN)).
                            getBody();
                    Log.d("Prova", jsonAccessToken.toString());
                    String token_type = jsonAccessToken.getString(Constants.TOKEN_TYPE);
                    if (token_type != null && token_type.equals(Constants.TOKEN_TYPE_BEARER))
                        HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, token_type + " " + jsonAccessToken.getString(Constants.AUTH_TOKEN));
                    if (jsonResponse.has(Constants.REFRESH_TOKEN)) {
                        JSONObject jsonRefreshToken = jsonResponse.getJSONObject(Constants.REFRESH_TOKEN);
                        Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(jsonRefreshToken.getString(Constants.REFRESH_TOKEN));
                        Log.d("Token Salvato", jsonRefreshToken.getString(Constants.REFRESH_TOKEN));
                        HttpController.saveRefreshToken(jsonRefreshToken.getString(Constants.REFRESH_TOKEN), LogIn.this);
                    }
                    Intent goToHomeIntent = new Intent(LogIn.this, HomeActivity.class);
                    startActivity(goToHomeIntent);

                } else {
                    Toast.makeText(LogIn.this, "Da mettere stringa login", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
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

        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    protected void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            JSONObject body = new JSONObject();
            body.put("token", idToken);
            body.put("user_type", Constants.GOOGLE_USER);

            HttpController.thirdPartyLogin(body, logInResponseHandler, logInErrorHandler, this);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public View.OnClickListener googleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            signIn();
        }
    };

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(Task<GoogleSignInAccount> task) {
                handleSignInResult(task);
            }

        });

        String token = HttpController.getRefreshToken(LogIn.this);
        if (token != null) {
            Log.d("Token Caricato", token);
            try {
                Jwts.parser().require(Constants.USER_TYPE, Integer.toString(Constants.REGISTERD_USER)).
                        setSigningKey(HttpController.getKey()).
                        parseClaimsJws(token);
                HttpController.authorizationHeader = new HashMap<>();
                HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, Constants.TOKEN_TYPE_BEARER + " " + token);
                HttpController.refreshAccessToken(logInResponseHandler, logInErrorHandler, LogIn.this);
            } catch (MissingClaimException | IncorrectClaimException | JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        createNotificationChannel();
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        progressBar = findViewById(R.id.progress_bar);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        //GOOGLE SIGN IN
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleBtn = (SignInButton) findViewById(R.id.sign_in_button_Google);
        googleBtn.setOnClickListener(googleSignIn);


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            LoginManager.getInstance().logInWithReadPermissions(LogIn.this, Arrays.asList("public_profile", "email"));
        }
        //FACEBOOK SIGN IN
        callbackManager = CallbackManager.Factory.create();
        LoginButton btn = (LoginButton) findViewById(R.id.btnLogInFacebook);
        btn.setReadPermissions("email");
        btn.setOnClickListener(facebookSignIn);
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        try {
                            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                            JSONObject body = new JSONObject();
                            body.put("token", AccessToken.getCurrentAccessToken().getToken());
                            body.put("user_type", Constants.FACEBOOK_USER);
                            HttpController.thirdPartyLogin(body, logInResponseHandler, logInErrorHandler, getApplicationContext());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancel() {
                        Log.d("porco", "porcoDio");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        exception.printStackTrace();
                    }
                });


        Button logInBtn = (Button) findViewById(R.id.btnLogIn);
        logInBtn.setOnClickListener(logInListener);

        username = findViewById(R.id.txtUsername);
        password = findViewById(R.id.txtPwd);
        remember = findViewById(R.id.swtRemember);
        register = (TextView) findViewById(R.id.txtTapHere);
        register.setOnClickListener(goToRegisterPage);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_ACCESS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(Constants.PACKAGE_NAME+"Prima Geofence")
                    .setCircularRegion(
                            41.3149307,
                            16.2623632,
                            4000000
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Geofence","Geofenses added");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                        }
                    });

            Log.d("Geofence","Yay");
            Intent intent = new Intent(LogIn.this, GeofenceService.class);
            startService(intent);
        }

        if(isMyServiceRunning(GeofenceService.class)){
            Log.d("Geofence","Service running");

        }
        else
            Log.d("...","Porco jesus");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = "Test developing apllication";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.PACKAGE_NAME, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public View.OnClickListener facebookSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LoginManager.getInstance().logInWithReadPermissions(LogIn.this, Arrays.asList("public_profile", "email"));

        }
    };
    public View.OnClickListener logInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                JSONObject body = new JSONObject();
                body.put("email", username.getText().toString());
                body.put("password", HttpController.get_SHA_512_SecurePassword(password.getText().toString()));
                body.put("remember_me", remember.isChecked());
                body.put("user_type", Constants.REGISTERD_USER);
                HttpController.login(body, logInResponseHandler, logInErrorHandler, LogIn.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public View.OnClickListener goToRegisterPage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LogIn.this, RegisterActivity.class);
            startActivity(intent);
        }
    };
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}


