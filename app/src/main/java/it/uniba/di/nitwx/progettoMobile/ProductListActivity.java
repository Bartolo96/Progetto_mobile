package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;
import java.util.List;

/**
 * An activity representing a list of Products. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProductDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProductListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private View recyclerView;
    private List<ProductContent.Product> productsList;
    AppDatabase db ;


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
                if(ProductContent.ITEMS.isEmpty()){
                    try {
                        HttpController.getProducts(productsResponseHandler, productsErrorHandler, ProductListActivity.this);
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
    Response.ErrorListener productsErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.getStackTrace();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        db = AppDatabase.getDatabase(ProductListActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


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
            /*int drawableId= Resources.getSystem().getIdentifier("productimage"+mValues.get(position).code,"drawable",getPackageName());*/

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
                mIdView = (ImageView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
