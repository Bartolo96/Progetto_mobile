package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.app.Dialog;
import android.arch.persistence.room.Transaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
    public AppDatabase db;
    private boolean isTransactionValid = false;
    private String token;
    private String accesToken;
    private Button btnRedeem;
    private Dialog dialog;
    private String transactionToken;
    private TextView txtPoints;

    Response.Listener<String> checkAvailibilityHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                Log.d("onRespone","entrato");
                Log.d("response",response);
                JSONObject jsonResponse = new JSONObject(response);
                token = (String) jsonResponse.get(Constants.TRANSACTION_TOKEN);
                transactionToken=token;
                Claims transactionClaims = Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(token).getBody();
                UserTransaction userTransaction = new UserTransaction((String) transactionClaims.get("id"),token,HttpController.userClaims.getId(),mItem.id);
                new insertTransactionAsync().execute(userTransaction);
                btnRedeem.setText("Mostra codice Qr");
                Toast.makeText(getContext(),"Hai già riscattato questa offerta. Mostra il QrCode in cassa!",Toast.LENGTH_LONG);
                btnRedeem.setOnClickListener(btnShowPointsQrListener);
            } catch (JSONException|ExpiredJwtException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener checkAvailibilityErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error.networkResponse.statusCode==Constants.NO_TRANSACTION_FOUND) {
                Log.d("onError","entrato");
                btnRedeem.setOnClickListener(btnRedeemListener);
                error.printStackTrace();
            }
        }
    };
    Response.Listener<String> redeemResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                token = (String) jsonResponse.get("transaction_token");

                JSONObject jsonAccessToken = jsonResponse.getJSONObject(Constants.AUTH_TOKEN);
                HttpController.userClaims = Jwts.parser().setSigningKey(HttpController.getKey()).
                        parseClaimsJws(jsonAccessToken.getString(Constants.AUTH_TOKEN)).
                        getBody();
                String token_type = jsonAccessToken.getString(Constants.TOKEN_TYPE);
                if (token_type != null && token_type.equals(Constants.TOKEN_TYPE_BEARER)) {
                    HttpController.authorizationHeader.put(Constants.AUTHORIZATON_HEADER, token_type + " " + jsonAccessToken.getString(Constants.AUTH_TOKEN));
                }
                txtPoints.setText((HttpController.userClaims.get("points")).toString());
                Claims transactionClaims = Jwts.parser().setSigningKey(HttpController.getKey()).parseClaimsJws(token).getBody();
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.qrcode_dialog_fragment);
                int dialogHeight = dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
                int dialogWidth = dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
                Bitmap mImage = QRCode.from(jsonResponse.toString()).withSize(dialogWidth * 2, dialogHeight * 2).bitmap();
                BitmapDrawable qrCode = new BitmapDrawable(getResources(), mImage);
                TextView qrCodeText = dialog.findViewById(R.id.qrCodeTextView);
                qrCodeText.setText(R.string.qrCodeDialogTitle);

                ImageView qrCodeImage = dialog.findViewById(R.id.qrCodeImageView);
                qrCodeImage.setImageDrawable(qrCode);
                dialog.show();
                btnRedeem.setText("Mostra Codice Qr");
                btnRedeem.setOnClickListener(btnShowQrListener);
                new insertTransactionAsync().execute(new UserTransaction((String) transactionClaims.get("id"),token,(String)HttpController.userClaims.get("id"), mItem.id));

            } catch (JSONException|ExpiredJwtException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener redeemErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OfferDetailFragment() {
    }

    private class selectTransactionAsync extends AsyncTask<Void, Void, List<UserTransaction>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<UserTransaction> doInBackground(Void... voids) {
            return db.transactionDao().loadAllTransaction(mItem.id, HttpController.userClaims.get("id",String.class));
        }

        @Override
        protected void onPostExecute(List<UserTransaction> list) {
            super.onPostExecute(list);
            if (list.isEmpty()) {
                JSONObject body = new JSONObject();
                try {
                    body.put("offer_id",mItem.id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpController.checkTransactionAvailability(body,checkAvailibilityHandler,checkAvailibilityErrorHandler,getContext());
               //fai ciò che facevi prima,
            }else{
                transactionToken = list.get(0).token;
                btnRedeem.setText("Mostra Codice Qr");
                btnRedeem.setOnClickListener(btnShowPointsQrListener);

            }
        }

    }

    private class insertTransactionAsync extends AsyncTask<UserTransaction, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Perform pre-adding operation here.
        }

        protected Void doInBackground(UserTransaction...transaction) {
            db.transactionDao().isertIntoTransaction(transaction[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = OfferContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));


        }
        db = AppDatabase.getDatabase(getContext());
        new selectTransactionAsync().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.offer_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

            ((TextView) rootView.findViewById(R.id.txtNameOfferDetail)).setText(mItem.name);
            ((TextView) rootView.findViewById(R.id.pointPriceTextView)).setText(getResources().getString(R.string.redeemFor,mItem.points_cost));
            ((TextView) rootView.findViewById(R.id.offerPriceTextView)).setText(getResources().getString(R.string.buyFor)+mItem.offerPrice);
            ((TextView) rootView.findViewById(R.id.OfferDescriptionText)).setText(mItem.description);
            btnRedeem= (Button) rootView.findViewById(R.id.btnRedeem);

        }

        Button buyNow = (Button) rootView.findViewById(R.id.btnBuyNowOfferDetail);
        //BUY NOW LISTENER
        buyNow.setOnClickListener(btnShowQrListener);

        return rootView;
    }

    View.OnClickListener btnRedeemListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int point = Integer.valueOf((String) HttpController.userClaims.get("points"));
            int pointCost = mItem.points_cost;
            if(point<pointCost){
                Toast.makeText(getContext(),"Non hai punti a sufficienza! Corri a giocare!",Toast.LENGTH_SHORT).show();
            }
            else {
                dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.redeem_layout_fragment);
                TextView confirmMessage = (TextView) dialog.findViewById(R.id.txtRedeemConfirm);
                Resources res = getResources();
                String message = res.getQuantityString(R.plurals.pointToUse, pointCost, pointCost);
                confirmMessage.setText(message);
                dialog.show();

                Button btnYes = dialog.findViewById(R.id.btnYesRedeem);
                btnYes.setOnClickListener(btnYesRedeemListener);

                Button btnNo = dialog.findViewById(R.id.btnNoRedeem);
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }

        }
    };

    View.OnClickListener btnYesRedeemListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            JSONObject body = new JSONObject();
            try {
                body.put("offer_id",mItem.id);
                HttpController.redeemOffer(body,redeemResponseHandler,redeemErrorHandler,getContext());
                dialog.cancel();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnClickListener btnShowQrListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                JSONObject json = new JSONObject();
                json.put("token", HttpController.authorizationHeader.get(Constants.AUTH_TOKEN));
                json.put("id", mItem.id);
                json.put("timestamp_qrCode_created", System.currentTimeMillis()/1000);

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.qrcode_dialog_fragment);
                int dialogHeight = dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
                int dialogWidth = dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();

                Bitmap mImage = QRCode.from(json.toString()).withSize(dialogWidth * 2, dialogHeight * 2).bitmap();
                BitmapDrawable qrCode = new BitmapDrawable(getResources(), mImage);

                TextView qrCodeText = dialog.findViewById(R.id.qrCodeTextView);
                qrCodeText.setText(R.string.qrCodeDialogTitle);
                ImageView qrCodeImage = dialog.findViewById(R.id.qrCodeImageView);

                qrCodeImage.setImageDrawable(qrCode);
                dialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    View.OnClickListener btnShowPointsQrListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Dialog dialog = new Dialog(getContext());
            JSONObject jsonTransactionToken= new JSONObject();
            try {
                jsonTransactionToken.put(Constants.TRANSACTION_TOKEN,transactionToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.setContentView(R.layout.qrcode_dialog_fragment);
            int dialogHeight = dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
            int dialogWidth = dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
            //Log.d("Prova",transactionToken);
            Bitmap mImage = QRCode.from(jsonTransactionToken.toString()).withSize(dialogWidth * 2, dialogHeight * 2).bitmap();
            BitmapDrawable qrCode = new BitmapDrawable(getResources(), mImage);
            TextView qrCodeText = dialog.findViewById(R.id.qrCodeTextView);
            qrCodeText.setText(R.string.qrCodeDialogTitle);

            ImageView qrCodeImage = dialog.findViewById(R.id.qrCodeImageView);
            qrCodeImage.setImageDrawable(qrCode);
            dialog.show();
        }
    };


}
