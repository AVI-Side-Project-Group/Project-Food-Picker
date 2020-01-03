package me.nakukibo.projectfoodpicker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class DetailData extends AsyncTask<Object, String, String> {

    /**
     * takes in the HashMap<String, String> selectedRestaurant and fills it with the detailed info
     * the reference will be sent back to the ReceiveDetailData instance that was passed as argument
     */

    private static final String TAG = NearbyData.class.getSimpleName();

    private HashMap<String, String> selectedRestaurant;
    private ReceiveDetailData receiveDetailData;

    private String googlePlacesData;

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