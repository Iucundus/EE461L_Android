package com.example.zachary.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String API_KEY = "&key=AIzaSyDuo9-DNqIY8LAVvIVCfRicNHRfmMMxM5I";
    public static final String base_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr = ((EditText)findViewById(R.id.addressBox)).getText().toString();
                String[] addrComps = addr.split(",");
                String url_str = base_URL;
                for(int i = 0; i < addrComps.length; i++){
                    addrComps[i] = addrComps[i].replace(" ", "+");
                    url_str += addrComps[i];
                    if(i != addrComps.length - 1){
                        url_str += ",";
                    }
                }
                url_str += API_KEY;

            }
        });
    }
}