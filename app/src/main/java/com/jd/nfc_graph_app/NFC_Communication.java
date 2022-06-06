package com.jd.nfc_graph_app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class NFC_Communication {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    public NfcAdapter nfcAdapter;

    boolean writeMode;
    Tag myTag;
    Context context;


    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    public String readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // Odebranie surowych danych po NFC
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            // Sprawdzenie czy sa jakies dane oraz przepisanie danych do kolejnego bufora
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            return buildTagViews(msgs);
        }
        return "0";
    }

    public String buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return "";

        String text = "";
        // Kodowanie surowych danych w kodowaniu "UTF-8" lub "UTF-16"
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"

        try {
            // Konwersja danych na String
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        return text;
    }


    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/
    public void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        // Pobieranie Tagu NFC.
        Ndef ndef = Ndef.get(tag);
        // Polaczenie sie z NFC tag
        ndef.connect();
        // Napisanie wiadomosci po NFC
        ndef.writeNdefMessage(message);
        // Rozlaczenie sie z NFC tag
        ndef.close();
    }

    public NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        // Konwersja surowych danych w format taki jak przyjmuje NFC
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];


        payload[0] = (byte) langLength;

        
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return recordNFC;
    }

    public NFC_Communication(Context context) {
        this.context = context;
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);

    }

    public void setWriteModeOff() {
        writeMode = false;
    }

    public void setWriteModeOn() {
        writeMode = true;
    }

}
