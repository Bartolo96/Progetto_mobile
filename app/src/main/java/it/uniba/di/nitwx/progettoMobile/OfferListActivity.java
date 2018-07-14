package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Transaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static it.uniba.di.nitwx.progettoMobile.Constants.UNAUTHORIZED_STATUS_CODE;

/**
 * An activity representing a list of Offers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link OfferDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class OfferListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private List<OfferContent.Offer> offerList;
    private View recyclerView;
    private AppDatabase db;

    private class InsertOffersAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.offerDao().insertProductsList(OfferContent.ITEMS);
            for(OfferContent.Offer a : OfferContent.ITEMS){
                db.offerDao().updateProductslist(a.product_list, Integer.valueOf(a.id));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //To after addition operation here.
        }
    }
    private class SelectOfferssAsync extends AsyncTask<Void, Void, List<OfferContent.Offer>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Perform pre-adding operation here.
        }

        protected List<OfferContent.Offer> doInBackground(Void... voids) {
            return db.offerDao().loadAllOffers(Calendar.getInstance().getTimeInMillis()/1000);

        }

        @Override
        protected void onPostExecute(List<OfferContent.Offer> lista) {
            super.onPostExecute(lista);
            try {
                OfferContent.populate(lista);

                if(OfferContent.ITEMS.isEmpty()){
                    HttpController.getOffers(offerResponseHandler, offerErrorHandler, OfferListActivity.this);
                }else {
                    Log.d("Prova", ":D");
                    recyclerView = findViewById(R.id.offer_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    Response.ErrorListener refreshErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            SharedPreferences sharedPref = getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(Constants.REFRESH_TOKEN);
            editor.apply();


            Intent backToLogin =  new Intent(OfferListActivity.this, LogIn.class);
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
                    HttpController.saveRefreshToken(jsonRefreshToken.getString(Constants.REFRESH_TOKEN), OfferListActivity.this);
                }
                HttpController.getOffers(offerResponseHandler, offerErrorHandler, OfferListActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Response.Listener<String> offerResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                offerList = OfferContent.populate(new JSONArray(response));
                new InsertOffersAsync().execute();
                recyclerView = findViewById(R.id.offer_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);

            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    Response.ErrorListener offerErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error.networkResponse.statusCode == UNAUTHORIZED_STATUS_CODE){
                String token = HttpController.getRefreshToken(OfferListActivity.this);
                if (token != null) {
                    Log.d("Token IN PRODUCT LIST", token);
                    try {
                        HttpController.authorizationHeader = new HashMap<>();
                        HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, Constants.TOKEN_TYPE_BEARER + " " + token);
                        HttpController.refreshAccessToken(refreshAuthHandler, refreshErrorHandler, OfferListActivity.this);
                    } catch (MissingClaimException | IncorrectClaimException | JSONException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Intent backToLogin =  new Intent(OfferListActivity.this, LogIn.class);
                    startActivity(backToLogin);
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);
        db = AppDatabase.getDatabase(OfferListActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        TextView txtPoints= (TextView) toolbar.findViewById(R.id.points);
        txtPoints.setText("IcePoints: "+(HttpController.userClaims.get("points")).toString());

        /**Inserimento drawerLayout + set Listener per la Navigation View**/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView loggedAsName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loggedAsEmailTextView);
        loggedAsName.setText((String)HttpController.userClaims.get("email"));

        if (findViewById(R.id.offer_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        if(OfferContent.ITEMS.isEmpty()){
            new SelectOfferssAsync().execute();
        }
        View recyclerView = findViewById(R.id.offer_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, OfferContent.ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final OfferListActivity mParentActivity;
        private final List<OfferContent.Offer> mValues;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OfferContent.Offer item = (OfferContent.Offer) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(OfferDetailFragment.ARG_ITEM_ID, item.id);
                    OfferDetailFragment fragment = new OfferDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.offer_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, OfferDetailActivity.class);
                    intent.putExtra(OfferDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(OfferListActivity parent,
                                      List<OfferContent.Offer> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.offer_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            int discount = 0;
            double price=0;
            Log.d("Prova", mValues.get(position).product_list.toString());
            for (OfferDao.ProductInOffer p: mValues.get(position).product_list){
                Log.d("Prova", ""+p.price);
                price  += p.price;
            }
            discount=(int)(mValues.get(position).offerPrice/price*100);
            holder.mIdView.setText(mValues.get(position).name);
            holder.mPriceView.setText("€ "+String.valueOf(mValues.get(position).offerPrice));
            holder.mDiscount.setText(discount+"%");

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mPriceView;
            final TextView mDiscount;

            ViewHolder(View view) {
                super(view);

                mIdView = (TextView) view.findViewById(R.id.txtNameOffer);
                mPriceView = (TextView) view.findViewById(R.id.txtDescriptionOffer);
                mDiscount =(TextView) view.findViewById(R.id.txtDiscount);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.offer_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            try {
                Log.d("refresh","pinna");
                HttpController.getOffers(offerResponseHandler, offerErrorHandler, OfferListActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.productsItemMenu:
                Intent goToProductsActivityIntent = new Intent(OfferListActivity.this, ProductListActivity.class);
                startActivity(goToProductsActivityIntent);
                break;

            case R.id.offersItemMenu:
                DrawerLayout mDrawerLayout;
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawers();
                break;
            case R.id.myProfile:
                Intent goToProfileActivityIntent = new Intent(OfferListActivity.this,ProfileActivity.class);
                startActivity(goToProfileActivityIntent);
                break;
            case R.id.settings:

                Intent goToSettingsActivityIntent = new Intent(OfferListActivity.this, SettingsActivity.class);
                startActivity(goToSettingsActivityIntent);

                break;
            case R.id.help:

                Intent goToHelpActivityIntent = new Intent(OfferListActivity.this, HelpActivity.class);
                startActivity(goToHelpActivityIntent);
                break;

            case R.id.logOut:
                //HomeActivity.functionLogOut();
                Intent intent=new Intent(OfferListActivity.this,LogIn.class);
                startActivity(intent);
                finish();
                break;
            case R.id.homenavigation:
                Intent goToHomeActivityIntent = new Intent(OfferListActivity.this, HomeActivity.class);
                startActivity(goToHomeActivityIntent);
                finish();
                break;
            case R.id.gameItemMenu:
                if(Integer.valueOf(HttpController.userClaims.get("last_time_played",String.class)) < (Calendar.getInstance().getTimeInMillis()/1000)-(24*60*60) ) {
                    Intent goToGameIntent = new Intent(OfferListActivity.this, GameActivity.class);
                    startActivityForResult(goToGameIntent, 1);
                }else {
                    Toast.makeText(OfferListActivity.this,"You have to wait",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.storesItemMenu:
                Intent goToStoresActivityIntent = new Intent(OfferListActivity.this,StoresActivity.class);
                startActivity(goToStoresActivityIntent);
                break;
        }
        return false;
    }
}


