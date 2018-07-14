package it.uniba.di.nitwx.progettoMobile;


import android.accounts.AccountManager;
import android.app.DatePickerDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    EditText email;
    EditText password;
    EditText confPassword;
    Button bday;
    Button register;
    RadioButton male;
    RadioButton female;
    RadioGroup gender;
    String sex;
    TextView datePickedText;
    Integer bdayDay;
    Integer bdayMonth;
    Integer bdayYear;
    DatePickerDialog datePickerDialog;
    private AccountManager accountManager;
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

        email = findViewById(R.id.edTextEmailReg);
        password = findViewById(R.id.edTextPwdReg);
        confPassword = findViewById(R.id.edTextPwdConfReg);
        bday = findViewById(R.id.btnPickDate);
        register = findViewById(R.id.btnRegister);
        gender = findViewById(R.id.radioGroupGender);
        male = findViewById(R.id.radioBtnMale);
        female = findViewById(R.id.radioBtnFemale);
        register.setOnClickListener(registerListener);
        datePickedText = findViewById(R.id.datePickedText);
        final Calendar c = Calendar.getInstance();
        bdayYear = c.get(Calendar.YEAR);
        bdayMonth = c.get(Calendar.MONTH);
        bdayDay = c.get(Calendar.DAY_OF_MONTH);
        accountManager = AccountManager.get(RegisterActivity.this);
        datePickerDialog = new DatePickerDialog(
                this, RegisterActivity.this, bdayYear, bdayMonth, bdayDay);
        bday = findViewById(R.id.btnPickDate);
        bday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

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

    }

    View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (password != null && confPassword !=null && bdayDay != null && bdayMonth != null && bdayYear != null && sex != null && email != null) {
                if (password.getText().toString().equals(confPassword.getText().toString())) {
                    if (password.length() > 8) {
                        boolean emailIsValid = isValidEmail(email.getText().toString());
                        if (!emailIsValid) {
                            Toast.makeText(RegisterActivity.this, getString(R.string.WrongMail), Toast.LENGTH_SHORT).show();
                        } else {
                            String hashedPwd = HttpController.get_SHA_512_SecurePassword(password.getText().toString());
                            JSONObject body = new JSONObject();
                            try {
                                body.put("email", email.getText().toString());
                                body.put("password", hashedPwd);
                                body.put("gender", sex);
                                Calendar test = Calendar.getInstance();
                                test.set(bdayYear, bdayMonth, bdayDay);

                                body.put("birth_date", test.getTimeInMillis()/1000);
                                Log.d("gender", sex);
                                HttpController.addUser(body, addUserResponseHandler, addUserErrorHandler, RegisterActivity.this);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else
                        Toast.makeText(RegisterActivity.this, "La password è troppo corta", Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(RegisterActivity.this, "La password e la sua conferma non sono uguali", Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(RegisterActivity.this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();

        }};

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        bdayDay=i2;
        bdayMonth=i1;
        bdayYear=i;
        String displayDate = getString(R.string.dateShowText) + String.format("%02d/%02d/%d",bdayDay,bdayMonth,bdayYear);
        datePickedText.setText(displayDate);
    }
}





