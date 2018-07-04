package it.uniba.di.nitwx.progettoMobile;

import android.Manifest;
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
import android.provider.SyncStateContract;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


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
    private List<Geofence> mGeofenceList;
    private final int REQUEST_FINE_ACCESS = 1;

    Response.Listener<String> logInResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {

                if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);
                Log.d("Provaaaaa",response);
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has(Constants.AUTH_TOKEN)) {
                    JSONObject jsonAccessToken = jsonResponse.getJSONObject(Constants.AUTH_TOKEN);
                    Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(jsonAccessToken.getString(Constants.AUTH_TOKEN));
                    String token_type = jsonAccessToken.getString(Constants.TOKEN_TYPE);
                    if (token_type != null && token_type.equals(Constants.TOKEN_TYPE_BEARER))
                        HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, token_type + " " + jsonAccessToken.getString(Constants.AUTH_TOKEN));
                    if (jsonResponse.has(Constants.REFRESH_TOKEN)) {
                        JSONObject jsonRefreshToken = jsonResponse.getJSONObject(Constants.REFRESH_TOKEN);
                        Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(jsonRefreshToken.getString(Constants.REFRESH_TOKEN));
                        Log.d("Token Salvato", jsonRefreshToken.getString(Constants.REFRESH_TOKEN));
                        HttpController.saveToken(jsonRefreshToken.getString(Constants.REFRESH_TOKEN), LogIn.this);
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

        String token;
        try {
            if ((token = HttpController.getToken(LogIn.this)) != null) {
                Log.d("Token Caricato", token);
                Jwts.parser().require(Constants.USER_TYPE, Constants.REGISTERD_USER).require(Constants.TOKEN_TYPE, Constants.TOKEN_TYPE_BEARER);
                HttpController.authorizationHeader = new HashMap<>();
                HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, Constants.TOKEN_TYPE_BEARER + " " + token);
                HttpController.refreshAccessToken(logInResponseHandler, logInErrorHandler, LogIn.this);
            }
        } catch (MissingClaimException | IncorrectClaimException e) {

            // we get here if the required claim is not present
            // we get here if the required claim has the wrong value
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        createNotificationChannel();


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
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
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
        remember= findViewById(R.id.swtRemember);
        register = (TextView)findViewById(R.id.txtTapHere);
        register.setOnClickListener(goToRegisterPage);
        GeofencingTask geoTask = new GeofencingTask();
        geoTask.execute();
    }


    public View.OnClickListener facebookSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LoginManager.getInstance().logInWithReadPermissions(LogIn.this, Arrays.asList("public_profile","email"));

        }
    };
    public View.OnClickListener logInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if(progressBar!=null)progressBar.setVisibility(View.VISIBLE);
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


    private class GeofencingTask extends AsyncTask<String,Void,String> implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        @Override
        protected void onPreExecute() {


        }


        @Override
        protected String doInBackground(String... params) {


            mGeofencingClient = LocationServices.getGeofencingClient(LogIn.this);
            mGeofenceList = new ArrayList<>();
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId("PrimaGeofence")

                    .setCircularRegion(
                            41.315376,
                            16.261924,
                            1000
                    )
                    .setExpirationDuration(60 * 60 * 1000 )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());



            return null;

        };

        protected void onPostExecute(String s) {
            if (s == null) {
                return;

            }
        }

        @Override
        public void onConnected(Bundle bundle) {

            Log.i("Prova", "Connected to GoogleApiClient");
            SharedPreferences sharedPrefs = LogIn.this.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
            String geofencesExist = sharedPrefs.getString("Geofences added", null);

            if (geofencesExist == null) {
                if (ActivityCompat.checkSelfPermission(LogIn.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(LogIn.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(LogIn.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_FINE_ACCESS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                else {
                    mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener(LogIn.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Pollo","Geofences added");
                                }
                            })
                            .addOnFailureListener(LogIn.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }

            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(LogIn.this,
                            Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("Prova", "Exception while resolving connection error.", e);
                }
            } else {
                int errorCode = connectionResult.getErrorCode();
                Log.e("Prova", "Connection to Google Play services failed with error code " + errorCode);
            }
        }

    }
}
