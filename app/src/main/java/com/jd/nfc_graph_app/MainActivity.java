package com.jd.nfc_graph_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // Tworzymy nowe obiekty dla klasy Button
    Button MoveScreenWifiButton;
    Button MoveScreenGraphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Przypisujemy do obiektow dany przycisk z layout'u
        MoveScreenWifiButton = (Button) findViewById(R.id.WifiScreenButton);
        MoveScreenGraphButton = (Button) findViewById(R.id.GraphScreenButton);


        MoveScreenWifiButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                // Na klikniecie przypisanego przycisku przechodzimy do klasy WifiActivity
                Intent intent = new Intent(getApplicationContext(), WifiActivity.class);
                startActivity(intent);

            }

        });

        MoveScreenGraphButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                // Na klikniecie przypisanego przycisku przechodzimy do klasy GraphActivity
                Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                startActivity(intent);


            }

        });


    }
}