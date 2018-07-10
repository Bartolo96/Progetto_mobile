package it.uniba.di.nitwx.progettoMobile;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailProfile = (TextView) findViewById(R.id.emailTextView);
        emailProfile.setText((String)HttpController.userClaims.get("email"));

        TextView genderProfile = (TextView) findViewById(R.id.genderTextView);
        genderProfile.setText((String)HttpController.userClaims.get("gender"));

        TextView bDayProfile = (TextView) findViewById(R.id.bDayTextView);
        bDayProfile.setText((String) HttpController.userClaims.get("birth_date"));

        /*Button changePassword = (Button) findViewById(R.id.pwProfileChangePassword);
        changePassword.setOnClickListener(changePasswordListener);*/
    }

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

                    if(newPwString.equals(newPwConfirmString)){

                    }
                    else{
                        Toast.makeText(ProfileActivity.this, getResources().getString(R.string.newPwConfirmNoMatch), Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    };

}
