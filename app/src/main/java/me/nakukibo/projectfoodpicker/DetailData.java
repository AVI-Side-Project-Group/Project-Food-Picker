package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DetailData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private HashMap<String, String> selectedRestaurant;
    private ReceiveDetailData receiveDetailData;

    private static final String TAG = NearbyData.class.getSimpleName();

    DetailData(HashMap<String, String> selectedRestaurant, ReceiveDetailData receiveDetailData) {
        this.selectedRestaurant = selectedRestaurant;
        this.receiveDetailData = receiveDetailData;
    }

    @Override
    protected String doInBackground(Object... objects) {
        String url = (String) objects[0];
        Log.d(TAG, "doInBackground: url to search=" + url);

        DownloadUrl downloadURL = new DownloadUrl();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        DataParser parser = new DataParser();
        parser.parseDetails(s, selectedRestaurant);
        receiveDetailData.sendDetailData(selectedRestaurant);
    }
}