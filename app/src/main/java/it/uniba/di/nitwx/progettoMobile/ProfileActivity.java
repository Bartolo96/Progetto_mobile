package it.uniba.di.nitwx.progettoMobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        TextView nameProfile = (TextView) findViewById(R.id.nameProfileTextView);
        TextView emailProfile = (TextView) findViewById(R.id.emailProfileTextView);
        TextView bDayProfile = (TextView) findViewById(R.id.bDayProfileTextView);


    }
}
