package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;

/**
 * A fragment representing a single Offer detail screen.
 * This fragment is either contained in a {@link OfferListActivity}
 * in two-pane mode (on tablets) or a {@link OfferDetailActivity}
 * on handsets.
 */
public class OfferDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private OfferContent.Offer mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OfferDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = OfferContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

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
        View rootView = inflater.inflate(R.layout.activity_offer_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.txtNameOfferDetail)).setText(mItem.name);
            ((TextView)rootView.findViewById(R.id.txtPointDetail)).setText(String.valueOf(mItem.point));
            ((TextView)rootView.findViewById(R.id.txtPriceDetail)).setText(String.valueOf(mItem.offerPrice));
            ((TextView)rootView.findViewById(R.id.txtOfferDescriptionDetail)).setText(mItem.products.toString());
            ((Button)rootView.findViewById(R.id.btnRedeem)).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //da implementare
                        }
                    }
            );
        }

        return rootView;
    }
}
