package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;


public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar homeToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(homeToolbar);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        /**Inserimento drawerLayout + set Listener per la Navigation View**/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, homeToolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int points = Integer.valueOf((String) HttpController.userClaims.get("points"));

        TextView emailProfile = findViewById(R.id.emailProfileTextView);
        emailProfile.setText((String)HttpController.userClaims.get("email"));

        TextView pointsProfile = findViewById(R.id.pointsProfileTextView);
        pointsProfile.setText(getResources().getString(R.string.youHavePoints,points));

        TextView genderProfile = findViewById(R.id.genderTextView);
        if(HttpController.userClaims.get("gender")!= null && !((String)HttpController.userClaims.get("gender")).equals("M"))
            genderProfile.setText(getString(R.string.Female));
        else
            genderProfile.setText(getString(R.string.Male));

        Long longDate;
        Date date;
        TextView bDayProfile = findViewById(R.id.bDayTextView);
        if (HttpController.userClaims.get("birth_date",String.class) != null) {
            longDate = Long.valueOf(HttpController.userClaims.get("birth_date", String.class)) * 1000;
            date = new Date(longDate);
            String displayDate = String.format("%02d/%02d/%d", date.getDay(), date.getMonth() + 1, date.getYear() + 1900);
            bDayProfile.setText(displayDate);
        }

        Button changePassword = findViewById(R.id.changePwButton);
        String type = HttpController.userClaims.get(Constants.USER_TYPE, String.class);
        if(Integer.valueOf(type)==Constants.REGISTERD_USER)
            changePassword.setOnClickListener(changePasswordListener);
        else
            changePassword.setEnabled(false);

    }



    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            try {
                JSONObject temp = new JSONObject(response);
                boolean ok= temp.getBoolean("password_update");

                if(ok){
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.goodOperation), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.newPwConfirmNoMatch), Toast.LENGTH_SHORT);
        }
    };


    View.OnClickListener changePasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.change_password_dialog_fragment);
            dialog.show();


            Button sendForm = dialog.findViewById(R.id.sendFormChangePwBtn);
            sendForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        EditText oldPw = dialog.findViewById(R.id.oldPwEditText);
                        EditText newPw = dialog.findViewById(R.id.newPwEditText);
                        EditText newPwConfirm = dialog.findViewById(R.id.confirmNewPsEditText);
                        String oldPwString = oldPw.getText().toString();
                        String newPwString = newPw.getText().toString();
                        String newPwConfirmString = newPwConfirm.getText().toString();

                        if (newPwString.equals(newPwConfirmString)) {

                            JSONObject body = new JSONObject();
                            String hashedOldPassword = HttpController.get_SHA_512_SecurePassword(oldPwString);
                            String hashedNewPassword = HttpController.get_SHA_512_SecurePassword(newPwConfirmString);
                            try {
                                body.put("old_password", hashedOldPassword );
                                body.put("new_password", hashedNewPassword);
                                HttpController.changePw(body, responseListener, responseErrorListener, ProfileActivity.this);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.newPwConfirmNoMatch), Toast.LENGTH_SHORT);
                        }
                    }

            });
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
                Intent goToProductsActivityIntent = new Intent(ProfileActivity.this, ProductListActivity.class);
                startActivity(goToProductsActivityIntent);
                break;

            case R.id.offersItemMenu:
                Intent goToOffersActivityIntent = new Intent(ProfileActivity.this, OfferListActivity.class);
                startActivity(goToOffersActivityIntent);
                break;
            case R.id.myProfile:
                Intent goToProfileActivityIntent = new Intent(ProfileActivity.this,ProfileActivity.class);
                startActivity(goToProfileActivityIntent);
                break;
            case R.id.gameItemMenu:
                long lastTimePlayed = Long.valueOf(HttpController.userClaims.get("last_time_played",String.class));
                long curTime = Calendar.getInstance().getTimeInMillis()/1000;

                if( lastTimePlayed < (curTime -(24*60*60))) {
                    Intent goToGameIntent = new Intent(ProfileActivity.this, GameActivity.class);
                    startActivityForResult(goToGameIntent, Constants.GAME_COMPLETED_CODE);
                }else {
                    long reaminingTime = lastTimePlayed - ( curTime -(24*60*60));
                    String waitToPlay = getString(R.string.waitToPlay)+ " " + String.format("%02dh:%02dm",reaminingTime/3600,(reaminingTime%3600)/60);
                    Toast.makeText(ProfileActivity.this,waitToPlay,Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.help:

                Intent goToHelpActivityIntent = new Intent(ProfileActivity.this, HelpActivity.class);
                startActivity(goToHelpActivityIntent);
                break;

            case R.id.logOut:
                functionLogOut();
                Intent intent=new Intent(ProfileActivity.this,LogIn.class);
                startActivity(intent);
                finish();
                break;
            case R.id.storesItemMenu:
                Intent goToStoresActivityIntent = new Intent(ProfileActivity.this,StoresActivity.class);
                startActivity(goToStoresActivityIntent);
                break;
            case R.id.homenavigation:
                Intent goToHomeActivityIntent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(goToHomeActivityIntent);
                finish();
        }
        return false;
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent=new Intent(ProfileActivity.this,LogIn.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
    public void functionLogOut(){
        String tmp=(String) HttpController.userClaims.get(Constants.USER_TYPE);
        switch (new Integer(tmp)){
            case Constants.REGISTERD_USER:
                break;
            case Constants.FACEBOOK_USER:
                LoginManager.getInstance().logOut();
                break;
            case Constants.GOOGLE_USER:
                signOut();
                break;
        }

        SharedPreferences sharedPref = ProfileActivity.this.getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(Constants.REFRESH_TOKEN);
        editor.apply();
    }
    Response.Listener<String> updatePointsResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                HttpController.userClaims = Jwts.parser().setSigningKey(HttpController.getKey()).
                        parseClaimsJws(jsonResponse.getString(Constants.AUTH_TOKEN)).
                        getBody();
                String token_type = jsonResponse.getString(Constants.TOKEN_TYPE);
                if (token_type != null && token_type.equals(Constants.TOKEN_TYPE_BEARER))
                    HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, token_type + " " + jsonResponse.getString(Constants.AUTH_TOKEN));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Response.ErrorListener updatePointsErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Toast.makeText(ProfileActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.GAME_COMPLETED_CODE){
            if(resultCode == Activity.RESULT_OK){
                if(data.getBooleanExtra("completed",false)){
                    Toast.makeText(this,getString(R.string.updatingPoints),Toast.LENGTH_SHORT).show();
                    HttpController.updatePoints(updatePointsResponseHandler,updatePointsErrorHandler,ProfileActivity.this);
                }
            }
        }
    }

}
