package it.uniba.di.nitwx.progettoMobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent srcIntent = getIntent();
        final String name = srcIntent.getStringExtra("name");
        Button goToProductsBtn = (Button) findViewById(R.id.btnProducts);
        goToProductsBtn.setText(name);

        goToProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProductsIntent = new Intent(HomeActivity.this,ProductListActivity.class);
                Log.d("pollichevolano", "listenerBattolo");
                startActivity(goToProductsIntent);
            }
        });
    }
}
