package it.uniba.di.nitwx.progettoMobile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;
import io.jsonwebtoken.Jwts;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /**Controllo se nelle shared preferences c'è l'immagine del profilo. Se si, la imposta**/
        ImageView profileImage = (ImageView) findViewById(R.id.profileImageView);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);

        String imagePath =sharedPreferences.getString(Constants.USER_IMAGE_PROFILE,null);
        if(imagePath!=null){
            ((ImageView) findViewById(R.id.profileImageView)).setImageURI(Uri.fromFile(new File(imagePath)));
            CircleImageView toolbarImage = (CircleImageView) findViewById(R.id.profile_image);
            toolbarImage.setImageURI(Uri.fromFile(new File(imagePath)));
        }
        profileImage.setOnClickListener(loadImageListener);

        TextView emailProfile = (TextView) findViewById(R.id.emailProfileTextView);
        emailProfile.setText(HttpController.userClaims.get("email").toString());

        TextView genderProfile = (TextView) findViewById(R.id.genderProfileTextView);
        genderProfile.setText(HttpController.userClaims.get("gender").toString());

        TextView bDayProfile = (TextView) findViewById(R.id.bDayProfileTextView);
        bDayProfile.setText(HttpController.userClaims.get("birth_date").toString());

        Button changePassword = (Button) findViewById(R.id.pwProfileChangePassword);
        changePassword.setOnClickListener(changePasswordListener);
    }

    View.OnClickListener loadImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.SELECT_PICTURE_REQUEST);

        }
    };

    View.OnClickListener changePasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.change_password_dialog_fragment);
            dialog.show();

            Button sendForm = (Button) findViewById(R.id.sendFormChangePwBtn);
            sendForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText oldPw = (EditText) findViewById(R.id.oldPwEditText);
                    String oldPwString = oldPw.getText().toString();
                    /**Implementare il controllo password**/
                }
            });
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.SELECT_PICTURE_REQUEST) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Set the image in ImageView
                    ((ImageView) findViewById(R.id.profileImageView)).setImageURI(selectedImageUri);
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.PACKAGE_NAME+Constants.REFRESH_TOKEN, Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString(Constants.USER_IMAGE_PROFILE, selectedImageUri.getPath().toString()).apply();
                }
            }
        }
    }
}
