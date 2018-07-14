package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    GoogleSignInClient  mGoogleSignInClient;
    GoogleSignInOptions gso;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
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
            Toast.makeText(HomeActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode == Constants.GAME_COMPLETED_CODE){
           if(resultCode == Activity.RESULT_OK){
               if(data.getBooleanExtra("completed",false)){
                   Toast.makeText(this,getString(R.string.updatingPoints),Toast.LENGTH_SHORT).show();
                   HttpController.updatePoints(updatePointsResponseHandler,updatePointsErrorHandler,HomeActivity.this);
               }
           }
       }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Inserimento toolbar
        Toolbar homeToolbar = findViewById(R.id.toolbar);
        homeToolbar.inflateMenu(R.menu.right_menu_home);
        setSupportActionBar(homeToolbar);

        //Inserimento drawerLayout + set Listener per la Navigation View
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, homeToolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView loggedAsName = navigationView.getHeaderView(0).findViewById(R.id.loggedAsEmailTextView);
        loggedAsName.setText((String)HttpController.userClaims.get("email"));

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        CardView goToProductsBtn = findViewById(R.id.btnProducts);
        CardView stores = findViewById(R.id.storesBtn);
        CardView offerBtn = findViewById(R.id.btnOffer);
        CardView gameBtn = findViewById(R.id.playGameBtn);

        stores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,StoresActivity.class);
                startActivity(intent);
            }
        });
        offerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,OfferListActivity.class);
                startActivity(intent);
            }
        });

        goToProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProductsIntent = new Intent(HomeActivity.this,ProductListActivity.class);
                startActivity(goToProductsIntent);
            }
        });

        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long lastTimePlayed = Long.valueOf(HttpController.userClaims.get("last_time_played",String.class));
                long curTime = Calendar.getInstance().getTimeInMillis()/1000;

                if( lastTimePlayed < (curTime -(24*60*60))) {
                    Intent goToGameIntent = new Intent(HomeActivity.this, GameActivity.class);
                    startActivityForResult(goToGameIntent, Constants.GAME_COMPLETED_CODE);
                }else {
                    long reaminingTime = lastTimePlayed - ( curTime -(24*60*60));
                    String waitToPlay = getString(R.string.waitToPlay)+ " " + String.format("%02dh:%02dm",reaminingTime/3600,(reaminingTime%3600)/60);
                    Toast.makeText(HomeActivity.this,waitToPlay,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent=new Intent(HomeActivity.this,LogIn.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
                Intent goToProductsActivityIntent = new Intent(HomeActivity.this, ProductListActivity.class);
                startActivity(goToProductsActivityIntent);
                break;

            case R.id.offersItemMenu:
                Intent goToOffersActivityIntent = new Intent(HomeActivity.this, OfferListActivity.class);
                startActivity(goToOffersActivityIntent);
                break;
            case R.id.myProfile:
                Intent goToProfileActivityIntent = new Intent(HomeActivity.this,ProfileActivity.class);
                startActivity(goToProfileActivityIntent);
                break;
            case R.id.settings:

                Intent goToSettingsActivityIntent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(goToSettingsActivityIntent);

                break;
            case R.id.help:

                Intent goToHelpActivityIntent = new Intent(HomeActivity.this, HelpActivity.class);
                startActivity(goToHelpActivityIntent);
                break;

            case R.id.logOut:
                functionLogOut();
                Intent intent =new Intent(HomeActivity.this,LogIn.class);
                startActivity(intent);
                finish();
                break;
            case R.id.homenavigation:
                DrawerLayout mDrawerLayout;
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawers();
                break;
            case R.id.gameItemMenu:
                long lastTimePlayed = Long.valueOf(HttpController.userClaims.get("last_time_played",String.class));
                long curTime = Calendar.getInstance().getTimeInMillis()/1000;

                if( lastTimePlayed < (curTime -(24*60*60))) {
                    Intent goToGameIntent = new Intent(HomeActivity.this, GameActivity.class);
                    startActivityForResult(goToGameIntent, Constants.GAME_COMPLETED_CODE);
                }else {
                    long reaminingTime = lastTimePlayed - ( curTime -(24*60*60));
                    String waitToPlay = getString(R.string.waitToPlay)+ " " + String.format("%02dh:%02dm",reaminingTime/3600,(reaminingTime%3600)/60);
                    Toast.makeText(HomeActivity.this,waitToPlay,Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.storesItemMenu:
                Intent goToStoresActivityIntent = new Intent(HomeActivity.this,StoresActivity.class);
                startActivity(goToStoresActivityIntent);
                break;
        }
        return false;
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

        SharedPreferences sharedPref = HomeActivity.this.getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(Constants.REFRESH_TOKEN);
        editor.apply();
    }
}
