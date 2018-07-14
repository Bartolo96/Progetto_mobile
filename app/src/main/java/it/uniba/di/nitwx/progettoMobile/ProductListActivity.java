package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static it.uniba.di.nitwx.progettoMobile.Constants.UNAUTHORIZED_STATUS_CODE;

/**
 * An activity representing a list of Products. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProductDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProductListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private View recyclerView;
    private List<ProductContent.Product> productsList;
    AppDatabase db ;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;


    private class InsertProductAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Perform pre-adding operation here.
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.productDao().insertProductsList(productsList );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //To after addition operation here.
        }
    }
    private class SelectProductAsync extends AsyncTask<Void, Void, List<ProductContent.Product>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Perform pre-adding operation here.
        }

        protected List<ProductContent.Product> doInBackground(Void... voids) {
            return db.productDao().loadAllProducts();

        }

        @Override
        protected void onPostExecute(List<ProductContent.Product> lista) {
            super.onPostExecute(lista);
            try {
                ProductContent.populate(lista);

                if(ProductContent.ITEMS.isEmpty()){
                    HttpController.getProducts(productsResponseHandler,productsErrorHandler,getApplicationContext());
                }else {
                    Log.d("Prova", ":D");
                    recyclerView = findViewById(R.id.product_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    Response.Listener<String> productsResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                productsList = ProductContent.populate(new JSONArray(response));
                new InsertProductAsync().execute();
                recyclerView = findViewById(R.id.product_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    Response.ErrorListener refreshErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("erroreRefresh","Bohhh");
            SharedPreferences sharedPref = getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(Constants.REFRESH_TOKEN);
            editor.apply();


            Intent backToLogin =  new Intent(ProductListActivity.this, LogIn.class);
            startActivity(backToLogin);


        }
    };
    Response.Listener<String> refreshAuthHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                Log.d("Response",response);
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jsonAccessToken = jsonResponse.getJSONObject(Constants.AUTH_TOKEN);
                HttpController.userClaims = Jwts.parser().setSigningKey(HttpController.getKey()).
                        parseClaimsJws(jsonAccessToken.getString(Constants.AUTH_TOKEN)).
                        getBody();
                String token_type = jsonAccessToken.getString(Constants.TOKEN_TYPE);
                if (token_type != null && token_type.equals(Constants.TOKEN_TYPE_BEARER))
                    HttpController.authorizationHeader = new HashMap<>();
                    HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, token_type + " " + jsonAccessToken.getString(Constants.AUTH_TOKEN));
                if (jsonResponse.has(Constants.REFRESH_TOKEN)) {
                    JSONObject jsonRefreshToken = jsonResponse.getJSONObject(Constants.REFRESH_TOKEN);
                    Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(jsonRefreshToken.getString(Constants.REFRESH_TOKEN));
                    HttpController.saveRefreshToken(jsonRefreshToken.getString(Constants.REFRESH_TOKEN), ProductListActivity.this);
                }
                HttpController.getProducts(productsResponseHandler,productsErrorHandler,ProductListActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Response.ErrorListener productsErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error.networkResponse.statusCode == UNAUTHORIZED_STATUS_CODE){
                String token = HttpController.getRefreshToken(ProductListActivity.this);
                if (token != null) {
                    Log.d("Token IN PRODUCT LIST", token);
                    try {
                        HttpController.authorizationHeader = new HashMap<>();
                        HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, Constants.TOKEN_TYPE_BEARER + " " + token);
                        HttpController.refreshAccessToken(refreshAuthHandler, refreshErrorHandler, ProductListActivity.this);
                    } catch (MissingClaimException | IncorrectClaimException | JSONException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Intent backToLogin =  new Intent(ProductListActivity.this, LogIn.class);
                    startActivity(backToLogin);
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        db = AppDatabase.getDatabase(ProductListActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        /**Inserimento drawerLayout + set Listener per la Navigation View**/
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView loggedAsName = navigationView.getHeaderView(0).findViewById(R.id.loggedAsEmailTextView);
        loggedAsName.setText((String)HttpController.userClaims.get("email"));

        if (findViewById(R.id.product_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }


        if (ProductContent.ITEMS.isEmpty()){
            new SelectProductAsync().execute();

        }
        else{
            recyclerView = findViewById(R.id.product_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, ProductContent.ITEMS, mTwoPane));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ProductListActivity mParentActivity;
        private final List<ProductContent.Product> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductContent.Product item = (ProductContent.Product) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ProductDetailFragment.ARG_ITEM_ID, item.id);
                    ProductDetailFragment fragment = new ProductDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.product_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ProductListActivity parent,
                                      List<ProductContent.Product> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_list_content, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            /*Una volta inserite tutte le immagini utilizzare questa riga mValues.get(position).content*/
            int drawableId = -1;

            //Field c= Drawable.class.getDeclaredField("productimage"+mValues.get(position).code);
            drawableId=getResources().getIdentifier("productimage"+mValues.get(position).code,"drawable",getPackageName());

            if((position%2)==0){
                holder.itemView.setBackgroundResource(R.drawable.list_item_shade1);
            }
            else{
                holder.itemView.setBackgroundResource(R.drawable.list_item_shade2);
            }

            if(drawableId==-1) {
                holder.mIdView.setImageDrawable(getResources().getDrawable(R.drawable.questionmark));
            }
            else{
                holder.mIdView.setImageDrawable(getResources().getDrawable(drawableId));
            }
            holder.mIdView.setMaxHeight(50);
            holder.mIdView.setMaxWidth(50);
            holder.mContentView.setText(mValues.get(position).name);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
                DrawerLayout mDrawerLayout;
                mDrawerLayout = findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawers();
                break;

            case R.id.offersItemMenu:
                Intent goToOffersActivityIntent = new Intent(ProductListActivity.this, OfferListActivity.class);
                startActivity(goToOffersActivityIntent);
                break;
            case R.id.myProfile:
                Intent goToProfileActivityIntent = new Intent(ProductListActivity.this,ProfileActivity.class);
                startActivity(goToProfileActivityIntent);
                break;
            case R.id.settings:

                Intent goToSettingsActivityIntent = new Intent(ProductListActivity.this, SettingsActivity.class);
                startActivity(goToSettingsActivityIntent);

                break;
            case R.id.help:

                Intent goToHelpActivityIntent = new Intent(ProductListActivity.this, HelpActivity.class);
                startActivity(goToHelpActivityIntent);
                break;

            case R.id.logOut:
                functionLogOut();
                Intent intent=new Intent(ProductListActivity.this,LogIn.class);
                startActivity(intent);
                finish();
                break;
            case R.id.homenavigation:
                Intent goToHomeActivityIntent = new Intent(ProductListActivity.this, HomeActivity.class);
                startActivity(goToHomeActivityIntent);
                finish();
                break;
            case R.id.gameItemMenu:
                long lastTimePlayed = Long.valueOf(HttpController.userClaims.get("last_time_played",String.class));
                long curTime = Calendar.getInstance().getTimeInMillis()/1000;

                if( lastTimePlayed < (curTime -(24*60*60))) {
                    Intent goToGameIntent = new Intent(ProductListActivity.this, GameActivity.class);
                    startActivityForResult(goToGameIntent, Constants.GAME_COMPLETED_CODE);
                }else {
                    long reaminingTime = lastTimePlayed - ( curTime -(24*60*60));
                    String waitToPlay = getString(R.string.waitToPlay)+ " " + String.format("%02dh:%02dm",reaminingTime/3600,(reaminingTime%3600)/60);
                    Toast.makeText(ProductListActivity.this,waitToPlay,Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.storesItemMenu:
                Intent goToStoresActivityIntent = new Intent(ProductListActivity.this,StoresActivity.class);
                startActivity(goToStoresActivityIntent);
                break;
        }
        return false;
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent=new Intent(ProductListActivity.this,LogIn.class);
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

        SharedPreferences sharedPref = ProductListActivity.this.getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
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
            Toast.makeText(ProductListActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.GAME_COMPLETED_CODE){
            if(resultCode == Activity.RESULT_OK){
                if(data.getBooleanExtra("completed",false)){
                    Toast.makeText(this,getString(R.string.updatingPoints),Toast.LENGTH_SHORT).show();
                    HttpController.updatePoints(updatePointsResponseHandler,updatePointsErrorHandler,ProductListActivity.this);
                }
            }
        }
    }
}
