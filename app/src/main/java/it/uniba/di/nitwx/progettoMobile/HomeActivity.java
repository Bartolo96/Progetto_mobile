package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
        setSupportActionBar(homeToolbar);
        /**Inserimento drawerLayout + set Listener per la Navigation View**/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, homeToolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent srcIntent = getIntent();
        final String name = srcIntent.getStringExtra("name");
        Button goToProductsBtn = (Button) findViewById(R.id.btnProducts);
        goToProductsBtn.setText(name);
        Button logOut=(Button) findViewById(R.id.button_sign_out);
        logOut.setOnClickListener(logOutListener);

        goToProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProductsIntent = new Intent(HomeActivity.this,ProductListActivity.class);
                startActivity(goToProductsIntent);
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
            switch (view.getId()) {
                case R.id.button_sign_out:
                    signOut();
                    break;
                // ...
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.right_menu_home,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
            Intent goToProductsActivityIntent = new Intent(HomeActivity.this, ProductListActivity.class);
            startActivity(goToProductsActivityIntent);
                break;
            case R.id.settings:
            /*
                Codice di gestione della voce MENU_2
             */
        }
        return super.onOptionsItemSelected(item);
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
            case R.id.settings:
            /*
                Codice di gestione della voce MENU_2
             */
            case R.id.myProfile:
                Intent goToProfileActivityIntent = new Intent(HomeActivity.this,ProfileActivity.class);
                startActivity(goToProfileActivityIntent);
        }
        return false;
    }
}
