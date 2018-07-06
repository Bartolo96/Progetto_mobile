package it.uniba.di.nitwx.progettoMobile;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailProfile = (TextView) findViewById(R.id.emailProfileTextView);
        emailProfile.setText(HttpController.userClaims.get("email").toString());

        TextView genderProfile = (TextView) findViewById(R.id.genderProfileTextView);
        genderProfile.setText(HttpController.userClaims.get("gender").toString());

        TextView bDayProfile = (TextView) findViewById(R.id.bDayProfileTextView);
        bDayProfile.setText(HttpController.userClaims.get("birth_date").toString());

        Button changePassword = (Button) findViewById(R.id.pwProfileChangePassword);
        changePassword.setOnClickListener(changePasswordListener);
    }

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

}
