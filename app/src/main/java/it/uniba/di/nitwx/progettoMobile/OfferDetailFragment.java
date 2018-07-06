package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
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
        final View rootView = inflater.inflate(R.layout.activity_offer_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

            ((TextView)rootView.findViewById(R.id.txtPoint)).setText((HttpController.userClaims.get("points")).toString());
            ((TextView) rootView.findViewById(R.id.txtNameOfferDetail)).setText(mItem.name);
            ((TextView)rootView.findViewById(R.id.txtPointDetail)).setText(String.valueOf(mItem.points_cost));
            ((TextView)rootView.findViewById(R.id.txtPriceDetail)).setText(String.valueOf(mItem.offerPrice));
            ((TextView)rootView.findViewById(R.id.txtOfferDescriptionDetail)).setText(mItem.product_list.toString());
            ((Button)rootView.findViewById(R.id.btnRedeem)).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //da implementare
                        }
                    }
            );
        }
        Button btnRedeem = (Button) rootView.findViewById(R.id.btnRedeem);
        btnRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int point =Integer.valueOf((String) HttpController.userClaims.get("points"));
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.redeem_layout_fragment);
                TextView confirmMessage = (TextView) dialog.findViewById(R.id.txtRedeemConfirm);
                Resources res = getResources();
                String message = res.getQuantityString(R.plurals.pointToUse,point,point);
                confirmMessage.setText(message);
                int dialogHeight= dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
                int dialogWidth= dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();

                dialog.show();
                if(!true) {
                    JSONObject body = new JSONObject();
                    String idOffer = mItem.id;
                    try {
                        body.put("id", idOffer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button buyNow = (Button) rootView.findViewById(R.id.btnBuyNowOfferDetail);
        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**Creazione QrCode: Json composto da: token utente + prodotto**/
                try {

                    JSONObject json = new JSONObject();
                    Jwts.parser();
                    json.put("token",HttpController.authorizationHeader.get(Constants.AUTH_TOKEN));
                    json.put("id", mItem.id);
                    json.put("timestamp_qrCode_created",System.currentTimeMillis());

                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.qrcode_dialog_fragment);
                    int dialogHeight= dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
                    int dialogWidth= dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
                    Bitmap mImage = QRCode.from(json.toString()).withSize(dialogWidth*2,dialogHeight*2).bitmap();
                    BitmapDrawable qrCode = new BitmapDrawable(getResources(), mImage);
                    TextView qrCodeText = dialog.findViewById(R.id.qrCodeTextView);
                    qrCodeText.setText(R.string.qrCodeDialogTitle);

                    ImageView qrCodeImage = dialog.findViewById(R.id.qrCodeImageView);
                    qrCodeImage.setImageDrawable(qrCode);
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }});


        return rootView;
    }
}
