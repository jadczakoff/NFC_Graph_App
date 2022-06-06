package com.jd.nfc_graph_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GraphActivity extends AppCompatActivity implements RecyclerViewInterface {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    static String Key_word_Temp = "Plot_Temp";
    static String Key_word_Humidity = "Plot_Humidity";
    static String Key_word_Pressure = "Plot_Pressure";
    static String Key_word_MagnetoX = "Plot_MagnetoX";
    static String Key_word_MagnetoY = "Plot_MagnetoY";
    static String Key_word_MagnetoZ = "Plot_MagnetoZ";
    static String Key_word_AccelX = "Plot_AccelX";
    static String Key_word_AccelY = "Plot_AccelY";
    static String Key_word_AccelZ = "Plot_AccelZ";
    static String Key_word_GyroX = "Plot_GyroX";
    static String Key_word_GyroY = "Plot_GyroY";
    static String Key_word_GyroZ = "Plot_GyroZ";

    static String Prefix_Temp = "Temp:";
    static String Prefix_Humidity = "Humidity:";
    static String Prefix_Pressure = "Pressure:";
    static String Prefix_MagnetoX = "MagnetoX:";
    static String Prefix_MagnetoY = "MagnetoY:";
    static String Prefix_MagnetoZ = "MagnetoZ:";
    static String Prefix_AccelX = "AccelX:";
    static String Prefix_AccelY = "AccelY:";
    static String Prefix_AccelZ = "AccelZ:";
    static String Prefix_GyroX = "GyroX:";
    static String Prefix_GyroY = "GyroY:";
    static String Prefix_GyroZ = "GyroZ:";


    RecyclerView recyclerView;
    String types_params_holder[];
    GraphView graph;

    NFC_Communication nfc_comm;
    PendingIntent pendingIntent;
    IntentFilter[] writeTagFilters;
    Tag myTag;
    String receivedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = (GraphView) findViewById(R.id.graph);

        recyclerView = findViewById(R.id.mRecyclerView);
        types_params_holder = getResources().getStringArray(R.array.TypeOfParametersGraph);
        MyAdapter myAdapter = new MyAdapter(this, types_params_holder, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        nfc_comm = new NFC_Communication(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};

    }

    protected int SendNFCText(String text) {
        try {
            if (myTag == null) {
                Toast.makeText(getApplicationContext(), ERROR_DETECTED, Toast.LENGTH_LONG).show();
                return -1;
            } else {
                nfc_comm.write(text, myTag);
                Toast.makeText(getApplicationContext(), WRITE_SUCCESS, Toast.LENGTH_LONG).show();
                return 1;
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), WRITE_ERROR, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (FormatException e) {
            Toast.makeText(getApplicationContext(), WRITE_ERROR, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        receivedData = nfc_comm.readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        nfc_comm.setWriteModeOff();
        nfc_comm.nfcAdapter.disableForegroundDispatch(this); // Wylaczamy priorytet obslugi modulu NFC dla tej aktywnosci
    }

    @Override
    public void onResume() {
        super.onResume();
        nfc_comm.setWriteModeOn();
        nfc_comm.nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null); // Nadajemy priorytet obslugi modulu NFC dla tej aktywnosci
    }


    private void DrawChart(String keyWord, String prefix) {
        if (SendNFCText(keyWord) == 1) {
            

            System.out.println(receivedData);
            if (receivedData != null) { // Sprawdzamy czy udalo sie poprawnie odebrac dane
                if (receivedData.contains(prefix)) { // Sprawdzamy czy odebrane dane zawieraja dany prefix
                    if (receivedData.startsWith(prefix)) { // Sprawdzamy czy string z danymi zaczyna sie od danego prefixu
                        String tempReceivedData = receivedData.substring(receivedData.indexOf(";") + 1); // Pomijamy pierwsza wartosc odebranych danych, ze wzgledu na mozliwy blad (niekompletna dana, np. '.40' lub '40' lub ';' zamiast '302.40')
                        receivedData = tempReceivedData; // Nadpisujemy pelne odebrane dane, danymi bez pierwszej wartosci

                        graph.removeAllSeries(); // Czyscimy wykres

                        double t = 0.0d; // Definiujemy zmienna osi czasu wykresu
                        String[] dane = receivedData.split(";"); // Dzielimy odebrane dane na poszczegolne wartosci
                        ArrayList<DataPoint> temp = new ArrayList<DataPoint>(); // Przygotowujemy tymczasowa liste punktow wykresu
                        DataPoint[] series = new DataPoint[dane.length - 11]; // Pomijamy pierwsze 10 odebranych danych (ze wzgledu na bufor kolowy STMa w module NFC, dane te sa prawdopodobnie z innego czujnika niz chcemy, gdyz nie zdazyly zostac nadpisane)
                        for (int i = 10; i < (dane.length - 1); i++) {
                            temp.add(new DataPoint(t, Double.parseDouble(dane[i]))); // Tworzymy punkty wykresy, na podstawie zakresu odebranych danych (10..n-1), gdyz ostatnia wartosc w odebranych danych moze byc niekompletna ('10.' zamiast '10.38', etc.)
                            t += 0.5d; // Inkrementujemy zmienna osi czasu wykresu
                        }

                        for (int i = 0; i < series.length; i++) {
                            series[i] = temp.get(i); // Przepisujemy punkty wykresu z ArrayList<DataPoint> do tablicy statycznej DataPoint[], gdyz wykres do wizualizacji potrzebuje danych w takiej postaci
                        }

                        graph.addSeries(new LineGraphSeries(series)); // Rysujemy wykres
                        receivedData = "";
                    } else {
                        Toast.makeText(getApplicationContext(), String.format("receivedData nie zaczyna się od prefixu \"%s\"", prefix), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), String.format("receivedData nie zawiera prefixu \"%s\"", prefix), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "nie udalo sie - receivedData = null", Toast.LENGTH_LONG).show();
            }

        }

    }

    /*
    Temperature = 0
    Humidity = 1
    Pressure = 2
    MagnetometerX = 3
    MagnetometerY = 4
    MagnetometerZ = 5
    AccelerometerX = 6
    AccelerometerY = 7
    AccelerometerZ = 8
    GyroscopeX = 9
    GyroscopeY = 10
    GyroscopeZ = 11
 */

    @Override
    public void onItemClick(int position) {
        // W zaleznosci od klikniecie danego przycisku, zostaje generowany wykres
        switch (position) {
            case 0:
                DrawChart(Key_word_Temp, Prefix_Temp);
                break;
            case 1:
                DrawChart(Key_word_Humidity, Prefix_Humidity);
                break;
            case 2:
                DrawChart(Key_word_Pressure, Prefix_Pressure);
                break;
            case 3:
                DrawChart(Key_word_MagnetoX, Prefix_MagnetoX);
                break;
            case 4:
                DrawChart(Key_word_MagnetoY, Prefix_MagnetoY);
                break;
            case 5:
                DrawChart(Key_word_MagnetoZ, Prefix_MagnetoZ);
                break;
            case 6:
                DrawChart(Key_word_AccelX, Prefix_AccelX);
                break;
            case 7:
                DrawChart(Key_word_AccelY, Prefix_AccelY);
                break;
            case 8:
                DrawChart(Key_word_AccelZ, Prefix_AccelZ);
                break;
            case 9:
                DrawChart(Key_word_GyroX, Prefix_GyroX);
                break;
            case 10:
                DrawChart(Key_word_GyroY, Prefix_GyroY);
                break;
            case 11:
                DrawChart(Key_word_GyroZ, Prefix_GyroZ);
                break;

            default:

                break;

        }


    }
}