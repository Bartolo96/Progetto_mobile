package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText confPassword;
    DatePicker bday;
    Button register;
    RadioButton male;
    RadioButton female;
    RadioGroup gender;
    String sex;   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register = (Button)findViewById(R.id.btnRegister);
        register.setOnClickListener(registerListener);

    }

    View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            email = (EditText) findViewById(R.id.edTextEmailReg);
            password=(EditText) findViewById(R.id.edTextPwdReg);
            confPassword=(EditText)findViewById(R.id.edTextPwdConfReg);
            bday = (DatePicker) findViewById(R.id.datePickerBday);
            gender =(RadioGroup) findViewById(R.id.radioGroupGender);
            male=(RadioButton)findViewById(R.id.radioBtnMale);
            female=(RadioButton)findViewById(R.id.radioBtnFemale);
            Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
            gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if(i==R.id.radioBtnMale){
                        sex="M";
                   }
                   else {
                        sex="F";
                    }
                }
            });
            if(password.getText().toString().equals(confPassword.getText().toString())) {
                Toast.makeText(RegisterActivity.this,"gay",Toast.LENGTH_SHORT).show();
                writeInDb(email.getText().toString(), password.getText().toString(), sex, "" + bday.getDayOfMonth()
                        + bday.getMonth() + bday.getYear());
                startActivity(intent);
            }
            else
                Toast.makeText(RegisterActivity.this,"La password e la sua conferma non sono uguali",Toast.LENGTH_SHORT).show();
        }
    };
    public void writeInDb(String email,String password, String gender, String bday){
        //do something
    }

}


