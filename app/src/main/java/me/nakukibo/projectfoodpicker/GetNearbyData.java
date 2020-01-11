package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class GetNearbyData extends AsyncTask<Object, String, String> {

    /**
     * fetches the restaurants from Google Places and then parses the data and sends it back
     * to the ReceiveNearbyData instance that was passed as argument
     **/

    private static final String DEFAULT_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private String apiKey;
    private String googlePlacesData;
    private ReceiveNearbyData receiveNearbyData;
    private Location userLocation;
    private int maxDistance;
    private int pricingRange;
    private int minRating;
    private List<Restaurant> restaurants;

    GetNearbyData(String apiKey, ReceiveNearbyData receiveNearbyData, List<Restaurant> listToPopulate) {
        this.apiKey = apiKey;
        this.receiveNearbyData = receiveNearbyData;

        this.restaurants = listToPopulate;
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
        List<Restaurant> nearbyPlaceList;
        DataParser parser = new DataParser();
        String nextPageToken;
        try {
            nearbyPlaceList = parser.parse(s, userLocation, maxDistance, pricingRange, minRating);
            restaurants.addAll(nearbyPlaceList);
            nextPageToken = parser.getNextPageToken();
        }catch (RuntimeException e){
            nearbyPlaceList = null;
            nextPageToken = null;
        }

        if(nextPageToken != null){
            final String url = getUrlNextPage(nextPageToken);

            new Handler().postDelayed(() -> {
                new GetNearbyData(apiKey, receiveNearbyData, restaurants).execute(url, userLocation, maxDistance, pricingRange, minRating);
            }, 2000);
        } else {
            receiveNearbyData.onFinishNearbyFetch();
        }
    }

    private String getUrlNextPage(String nextPageToken) {
        String googlePlaceUrl = DEFAULT_PLACES_SEARCH_URL;
        googlePlaceUrl += "pagetoken=" + nextPageToken;
        googlePlaceUrl += "&key=" + apiKey;
        return googlePlaceUrl;
    }

    public static interface ReceiveNearbyData {

        void onFinishNearbyFetch();
    }
}
