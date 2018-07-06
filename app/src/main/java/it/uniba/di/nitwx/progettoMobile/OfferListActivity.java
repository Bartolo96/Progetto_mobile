package it.uniba.di.nitwx.progettoMobile;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;

import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

import java.util.List;

/**
 * An activity representing a list of Offers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link OfferDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class OfferListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private List<OfferContent.Offer> offerList;
    private View recyclerView;
    private AppDatabase db;
    /*
    private class InsertOffersAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Perform pre-adding operation here.
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.offerDao().insertProductsList(OfferContent.ITEMS);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //To after addition operation here.
        }
    }
    private class SelectProductsAsync extends AsyncTask<Void, Void, List<ProductContent.Product>> {

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
*/

    Response.Listener<String> offerResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                offerList = OfferContent.populate(new JSONArray(response));
                //new ProductListActivity.InsertProductAsync().execute();
                recyclerView = findViewById(R.id.offer_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);
                if(OfferContent.ITEMS.isEmpty()){
                    try {
                        HttpController.getOffers(offerResponseHandler, offerErrorHandler, OfferListActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    Response.ErrorListener offerErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.getStackTrace();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.offer_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        if(OfferContent.ITEMS.isEmpty()){
            try {
                HttpController.getOffers(offerResponseHandler, offerErrorHandler, OfferListActivity.this);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
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
            for (ProductContent.Product p: mValues.get(position).products){
                price+=p.price;
            }
            discount=(int)(mValues.get(position).offerPrice/price*100);
            holder.mIdView.setText(mValues.get(position).name);
            holder.mPriceView.setText(String.valueOf(mValues.get(position).offerPrice));
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
}
