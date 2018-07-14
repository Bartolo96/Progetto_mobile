package it.uniba.di.nitwx.progettoMobile;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        int points = Integer.valueOf((String) HttpController.userClaims.get("points"));

        TextView emailProfile = (TextView) findViewById(R.id.emailProfileTextView);
        emailProfile.setText((String)HttpController.userClaims.get("email"));

        TextView pointsProfile = (TextView) findViewById(R.id.pointsProfileTextView);
        pointsProfile.setText(getResources().getString(R.string.youHavePoints,points));

        TextView genderProfile = (TextView) findViewById(R.id.genderTextView);
        if(!((String)HttpController.userClaims.get("gender")).equals("M"))
            genderProfile.setText(getString(R.string.Female));
        else
            genderProfile.setText(getString(R.string.Male));


        TextView bDayProfile = (TextView) findViewById(R.id.bDayTextView);
        java.util.Date date = new java.util.Date(Long.parseLong((String) HttpController.userClaims.get("birth_date")));
        bDayProfile.setText(date.toString());

        Button changePassword = (Button) findViewById(R.id.changePwButton);
        String type = HttpController.userClaims.get(Constants.USER_TYPE, String.class);
        if(Integer.valueOf(type)==Constants.REGISTERD_USER)
            changePassword.setOnClickListener(changePasswordListener);
        else
            changePassword.setEnabled(false);

    }

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            try {
                JSONObject temp = new JSONObject(response);
                boolean ok= temp.getBoolean("password_updated");

                if(ok){
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.goodOperation), Toast.LENGTH_SHORT);
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Error From Server", Toast.LENGTH_SHORT);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.newPwConfirmNoMatch), Toast.LENGTH_SHORT);
        }
    };


    View.OnClickListener changePasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.change_password_dialog_fragment);
            dialog.show();


            Button sendForm = (Button) dialog.findViewById(R.id.sendFormChangePwBtn);
            sendForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                        EditText oldPw = (EditText) dialog.findViewById(R.id.oldPwEditText);
                        EditText newPw = (EditText) dialog.findViewById(R.id.newPwEditText);
                        EditText newPwConfirm = (EditText) dialog.findViewById(R.id.confirmNewPsEditText);
                        String oldPwString = oldPw.getText().toString();
                        String newPwString = newPw.getText().toString();
                        String newPwConfirmString = newPwConfirm.getText().toString();

                        if (newPwString.equals(newPwConfirmString)) {

                            JSONObject body = new JSONObject();
                            try {
                                body.put("old_password", HttpController.get_SHA_512_SecurePassword(oldPwString));
                                body.put("new_password", HttpController.get_SHA_512_SecurePassword(newPwConfirmString));
                                HttpController.changePw(body, responseListener, responseErrorListener, ProfileActivity.this);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.newPwConfirmNoMatch), Toast.LENGTH_SHORT);
                        }
                    }

            });
        }
    };

}
