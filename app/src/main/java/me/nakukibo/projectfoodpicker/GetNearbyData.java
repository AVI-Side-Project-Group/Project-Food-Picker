package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyData extends AsyncTask<Object, String, String> {

    /**
     * fetches the restaurants from Google Places and then parses the data and sends it back
     * to the ReceiveNearbyData instance that was passed as argument
     */

    private String googlePlacesData;
    private ReceiveNearbyData receiveNearbyData;
    private Location userLocation;
    private int maxDistance;
    private int pricingRange;
    private int minRating;

    GetNearbyData(ReceiveNearbyData receiveNearbyData) {
        this.receiveNearbyData = receiveNearbyData;
    }

    private static final String TAG = GetNearbyData.class.getSimpleName();

    @Override
    protected String doInBackground(Object... objects){
        String url = (String) objects[0];
        Log.d(TAG, "doInBackground: url to search=" + url);

        // set variables to values passed
        userLocation = (Location) objects[1];
        maxDistance = (int) objects[2];
        pricingRange = (int) objects[3];
        minRating = (int) objects[4];

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
        try {
            nearbyPlaceList = parser.parse(s, userLocation, maxDistance, pricingRange, minRating);
            nextPageToken = parser.getNextPageToken();
        }catch (RuntimeException e){
            nearbyPlaceList = null;
            nextPageToken = null;
        }
        // send data to RestaurantCardFinder
        receiveNearbyData.sendData(nearbyPlaceList, nextPageToken);
    }
}
