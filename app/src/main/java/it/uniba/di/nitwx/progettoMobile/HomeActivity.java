package it.uniba.di.nitwx.progettoMobile;

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

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    GoogleSignInClient  mGoogleSignInClient;
    GoogleSignInOptions gso;
    GoogleSignInAccount mGoogleSignInAccount;
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /**Inserimento toolbar**/
        Toolbar homeToolbar = (Toolbar) findViewById(R.id.toolbar);
        homeToolbar.inflateMenu(R.menu.right_menu_home);
        setSupportActionBar(homeToolbar);
        /**Inserimento drawerLayout + set Listener per la Navigation View**/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, homeToolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView loggedAsName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loggedAsEmailTextView);
        loggedAsName.setText((String)HttpController.userClaims.get("email"));

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        CardView goToProductsBtn = (CardView) findViewById(R.id.btnProducts);
        CardView stores = (CardView) findViewById(R.id.storesBtn);
        CardView offerBtn = (CardView) findViewById(R.id.btnOffer);
        CardView gameBtn = (CardView) findViewById(R.id.playGameBtn);

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
                Intent goToGameIntent = new Intent(HomeActivity.this,GameActivity.class);
                startActivity(goToGameIntent);
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
    View.OnClickListener logOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            functionLogOut();
            Intent intent=new Intent(HomeActivity.this,LogIn.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
                Intent goToProductsActivityIntent = new Intent(HomeActivity.this, ProductListActivity.class);
                startActivity(goToProductsActivityIntent);
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
                Intent intent=new Intent(HomeActivity.this,LogIn.class);
                startActivity(intent);
                finish();
                break;
            case R.id.homenavigation:
                Intent goToHomeActivityIntent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(goToHomeActivityIntent);
                finish();
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
