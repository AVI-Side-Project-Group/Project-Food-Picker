package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

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
    private Boolean allowProminent;
    private Boolean openNow;
    private List<Restaurant> restaurants;

    private static final String TAG = GetNearbyData.class.getSimpleName();

    GetNearbyData(String apiKey, ReceiveNearbyData receiveNearbyData, List<Restaurant> listToPopulate) {
        this.apiKey = apiKey;
        this.receiveNearbyData = receiveNearbyData;

        this.restaurants = listToPopulate;
    }

    @Override
    protected String doInBackground(Object... objects){
        String url = (String) objects[0];
        Log.d(TAG, "doInBackground: url to search=" + url);

        // set variables to values passed
        userLocation = (Location) objects[1];
        maxDistance = (int) objects[2];
        pricingRange = (int) objects[3];
        minRating = (int) objects[4];
        allowProminent = (Boolean) objects[5];
        openNow = (Boolean) objects[6];

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

        Log.d(TAG, "onPostExecute: finished executing");

        List<Restaurant> nearbyPlaceList;
        DataParser parser = new DataParser();
        String nextPageToken;
        try {
            nearbyPlaceList = parser.parse(s, userLocation, maxDistance, pricingRange, minRating, allowProminent, openNow);
            restaurants.addAll(nearbyPlaceList);
            nextPageToken = parser.getNextPageToken();
        }catch (RuntimeException e){
            e.printStackTrace();
            nextPageToken = null;
        }

        if(nextPageToken != null){
            final String url = getUrlNextPage(nextPageToken);

            Thread fetchThread = new Thread(() -> {
                for(int i=0; i<20; i++){
                    if(isCancelled()) return;

                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
                new GetNearbyData(apiKey, receiveNearbyData, restaurants)
                        .execute(url, userLocation, maxDistance, pricingRange, minRating, allowProminent, openNow);
            });

            fetchThread.start();
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

    public interface ReceiveNearbyData {

        void onFinishNearbyFetch();
    }
}
