package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class NearbyData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private ReceiveData receiveData;
    private Location userLocation;
    private int maxDistance;

    NearbyData(ReceiveData receiveData) {
        this.receiveData = receiveData;
    }

    private static final String TAG = NearbyData.class.getSimpleName();

    @Override
    protected String doInBackground(Object... objects){
        String url = (String) objects[0];
        Log.d(TAG, "doInBackground: url to search=" + url);
        userLocation = (Location) objects[1];
        maxDistance = (int) objects[2];

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
        String nextPageToken;
        boolean invalidRequest;
        try {
            nearbyPlaceList = parser.parse(s, userLocation, maxDistance);
            nextPageToken = parser.getNextPageToken();
            invalidRequest = false;
        }catch (RuntimeException e){
            nearbyPlaceList = null;
            nextPageToken = null;
            invalidRequest = true;
        }
        // send data to RestaurantCardFinder
        receiveData.sendData(nearbyPlaceList, nextPageToken);
    }
}
