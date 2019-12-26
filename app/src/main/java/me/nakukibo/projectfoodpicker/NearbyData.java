package me.nakukibo.projectfoodpicker;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class NearbyData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private ReceiveData receiveData;

    NearbyData(ReceiveData receiveData) {
        this.receiveData = receiveData;
    }

    @Override
    protected String doInBackground(Object... objects){
        String url = (String) objects[0];
        DownloadUrl downloadURL = new DownloadUrl();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){
        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);

        // send data to RestaurantCardFinder
        receiveData.sendData(nearbyPlaceList);
    }
}
