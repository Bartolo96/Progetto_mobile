package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import io.jsonwebtoken.Jwts;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView profileImage = (ImageView) findViewById(R.id.profileImageView);
        profileImage.setOnClickListener(loadImageListener);
        TextView nameProfile = (TextView) findViewById(R.id.nameProfileTextView);
        TextView emailProfile = (TextView) findViewById(R.id.emailProfileTextView);
        TextView bDayProfile = (TextView) findViewById(R.id.bDayProfileTextView);

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.SELECT_PICTURE_REQUEST) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Set the image in ImageView
                    ((ImageView) findViewById(R.id.profileImageView)).setImageURI(selectedImageUri);
                }
            }
        }
    }
}
