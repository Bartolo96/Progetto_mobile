package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Date;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText confPassword;
    DatePicker bday;
    Button register;
    RadioButton male;
    RadioButton female;
    RadioGroup gender;
    String sex;

    Response.Listener<String> addUserResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response){
            try {
                JSONObject temp = new JSONObject(response);

                Log.d("prova",response);
                if(temp.has(Constants.REGISTER_RESPONSE) && temp.getBoolean(Constants.REGISTER_RESPONSE)){
                    Toast.makeText(RegisterActivity.this,getString(R.string.RegistrationOk),Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this,LogIn.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(RegisterActivity.this,"QUALCOSA è ANDATO STORTO",Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    };



    Response.ErrorListener addUserErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(RegisterActivity.this,"Errore",Toast.LENGTH_SHORT).show();
        }
    };


    @Override
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
                boolean emailIsVAlid= isValidEmail(email.getText().toString());
                if(!emailIsVAlid){
                    Toast.makeText(RegisterActivity.this,getString(R.string.WrongMail),Toast.LENGTH_SHORT).show();
                }
                else {
                    String hashedPwd = HttpController.get_SHA_512_SecurePassword(password.getText().toString());
                    JSONObject body = new JSONObject();
                    try {
                        body.put("email", email.getText().toString());
                        body.put("password", hashedPwd);
                        body.put("gender", sex);
                        body.put("bday", new Date(bday.getYear(), bday.getMonth(), bday.getDayOfMonth()).getTime());
                        HttpController.addUser(body, addUserResponseHandler, addUserErrorHandler, RegisterActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
            else
                Toast.makeText(RegisterActivity.this,"La password e la sua conferma non sono uguali",Toast.LENGTH_SHORT).show();
        }
    };

    public static boolean isValidEmail(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}


