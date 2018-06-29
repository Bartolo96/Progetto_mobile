package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

/**
 * A fragment representing a single Product detail screen.
 * This fragment is either contained in a {@link ProductListActivity}
 * in two-pane mode (on tablets) or a {@link ProductDetailActivity}
 * on handsets.
 */
public class ProductDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ProductContent.ProductItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProductDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ProductContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.product_detail, container, false);

        // Show the dummy name as text in a TextView.
        if (mItem != null) {

            /*Ottengo i riferimenti a immagine prodotto, nome, prezzo, codice e descrizione e li
                mostro all'interno del ProductDetailFragment
             */
            int drawableId = -1;

            drawableId=getResources().getIdentifier("productimage"+mItem.code,"drawable",this.getClass().getPackage().getName());


            if(drawableId==-1) {
                ((ImageView) rootView.findViewById(R.id.productImage)).setImageDrawable(getResources().getDrawable(R.drawable.questionmark));
            }
            else{
                ((ImageView) rootView.findViewById(R.id.productImage)).setImageDrawable(getResources().getDrawable(drawableId));
            }

            ((TextView) rootView.findViewById(R.id.productDescription)).setText(mItem.description);
            String stringArrayProduct[] = getResources().getStringArray(R.array.productAttributes);

            String toSetText= stringArrayProduct[1]+": "+mItem.name+"\n"+
                                stringArrayProduct[3]+": €"+mItem.price+"\n"+
                                stringArrayProduct[4]+": "+mItem.code;

            ((TextView) rootView.findViewById(R.id.productName)).setText(toSetText);
        }

        return rootView;
    }
}
