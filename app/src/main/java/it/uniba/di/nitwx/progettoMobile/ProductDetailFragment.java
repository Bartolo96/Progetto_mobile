package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
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
    private ProductContent.Product mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();

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
            String toName = stringArrayProduct[1]+": "+mItem.name;
            String toPrice = stringArrayProduct[3]+": €"+mItem.price;
            ((TextView) rootView.findViewById(R.id.productName)).setText(toName);
            ((TextView) rootView.findViewById(R.id.productPrice)).setText(toPrice);

            Button buyNow = (Button) rootView.findViewById(R.id.ButtonBuyNow);
            buyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**Creazione QrCode: Json composto da: token utente + prodotto**/
                    try {

                        JSONObject json = new JSONObject();
                        Jwts.parser();
                        json.put("token",HttpController.authorizationHeader.get(Constants.AUTH_TOKEN));
                        json.put("product", mItem);
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
        }
        return rootView;
    }
}
