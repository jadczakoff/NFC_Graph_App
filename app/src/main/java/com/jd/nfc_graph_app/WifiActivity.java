package com.jd.nfc_graph_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class WifiActivity extends AppCompatActivity {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    TextView wifi_ssid_message;
    TextView wifi_password_message;
    Button btnWrite;

    NFC_Communication nfc_comm;
    PendingIntent pendingIntent;
    IntentFilter[] writeTagFilters;
    Tag myTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        wifi_ssid_message = (TextView) findViewById(R.id.Wifi_ssid_id);
        wifi_password_message = (TextView) findViewById(R.id.Wifi_password_id);
        btnWrite = (Button) findViewById(R.id.Send_NFC_Button);

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (myTag == null) {
                        // Jesli nie wykrylo NFC taga to wyswietla komunikat
                        Toast.makeText(getApplicationContext(), ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {
                        // Parsowanie danych do poprawnego formatu ktory mikrokontroler stm32 odczyta
                        String wifi_ssid = ";Login>" + wifi_ssid_message.getText().toString() + ";";
                        String wifi_password = ";Password>" + wifi_password_message.getText().toString() + ";";
                        String passy = wifi_ssid + wifi_password;
                        // Wysylanie danych po NFC
                        nfc_comm.write(passy, myTag);
                        Toast.makeText(getApplicationContext(), WRITE_SUCCESS, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    // Jesli nie udalo sie napisac do NFC tag to wyswietl komunikat
                    Toast.makeText(getApplicationContext(), WRITE_ERROR, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    // Jesli nie udalo sie napisac do NFC tag to wyswietl komunikat
                    Toast.makeText(getApplicationContext(), WRITE_ERROR, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        nfc_comm = new NFC_Communication(this);
        // Wywolywanie Intentu w przypadku gdy NFC tag zostanie znaleziony
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Odebranie danych po NFC
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        nfc_comm.setWriteModeOff();
        nfc_comm.nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        nfc_comm.setWriteModeOn();
        nfc_comm.nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }


}