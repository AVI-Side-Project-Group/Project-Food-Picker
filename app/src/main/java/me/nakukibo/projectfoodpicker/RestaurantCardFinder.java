package me.nakukibo.projectfoodpicker;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RestaurantCardFinder extends AppCompatActivity implements ReceiveData {

    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private static final int ERROR_PASSED_VALUE = -1;
    private static final String DEFAULT_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final int DEFAULT_WAIT_MS = 500;

    // accumulates the list over several calls
    private static List<HashMap<String, String>> nearbyPlaceListCombined;

    // data passed from PreferencesActivity.java
    private String foodType;
    private int distance;
    private int pricing;
    private int rating;

    // previous pageToken for multiple calls
    private String previousPageToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_card_finder);

        nearbyPlaceListCombined = null;
        retrievePassedValues();
        fetchLocation(null);
    }

    @Override
    public void sendData(List<HashMap<String, String>> nearbyPlaceList, String nextPageToken) {
        if(nearbyPlaceList == null){
            // if null then that means the nextPageToken request failed so search again
            requestNextPageSearch(previousPageToken);
            return;
        } else {
            previousPageToken = nextPageToken;
        }

        if(nearbyPlaceListCombined == null) {
            nearbyPlaceListCombined = nearbyPlaceList;
        } else {
            nearbyPlaceListCombined.addAll(nearbyPlaceList);
            Log.d(TAG, "sendData: combined list has new size of " + nearbyPlaceListCombined.size());
        }

        if(nextPageToken != null){
            requestNextPageSearch(nextPageToken);
            return;
        }

        Log.d(TAG, "sendData: logging the combined list");
        logAllPlacesList(nearbyPlaceListCombined);

        int index = new Random().nextInt(nearbyPlaceListCombined.size());
        HashMap<String, String> selectedRestaurant = nearbyPlaceListCombined.get(index);
        setViewValues(selectedRestaurant);
    }

    /**
     * set values of views to values in HashMap<String, String>
     */
    private void setViewValues(HashMap<String, String> selectedRestaurant) {
        TextView txtvwName = findViewById(R.id.txtvw_name);
        txtvwName.setText(selectedRestaurant.get(DataParser.DATA_KEY_NAME));

        ImageView restPhoto = findViewById(R.id.imgvw_restaurant);
        restPhoto.setImageResource(R.drawable.ic_launcher_background);

        TextView txtvwRating = findViewById(R.id.txtvw_rating);
        txtvwRating.setText(String.format(Locale.US, "%s stars (%s)",
                selectedRestaurant.get(DataParser.DATA_KEY_RATING), selectedRestaurant.get(DataParser.DATA_KEY_TOT_RATING)));

        TextView txtvwPricing = findViewById(R.id.txtvw_price_level);
        txtvwPricing.setText(String.format(Locale.US, "Pricing Level: %s",
                selectedRestaurant.get(DataParser.DATA_KEY_PRICE_LEVEL)));

        TextView txtvwAddress = findViewById(R.id.txtvw_address);
        txtvwAddress.setText(selectedRestaurant.get(DataParser.DATA_KEY_ADDRESS));

        TextView txtvwPhoneNumber = findViewById(R.id.txtvw_phone_number);
        txtvwPhoneNumber.setText(selectedRestaurant.get(DataParser.DATA_KEY_PHONE_NUMBER));

        TextView txtvwWebsite = findViewById(R.id.txtvw_website);
        txtvwWebsite.setText(selectedRestaurant.get(DataParser.DATA_KEY_WEBSITE));

        TextView txtvwHours = findViewById(R.id.txtvw_hours_values);
        txtvwHours.setText(selectedRestaurant.get(DataParser.DATA_KEY_HOURS));
    }

    /**
     * send search request with nextPageToken
     */
    private void requestNextPageSearch(String nextPageToken) {
        SystemClock.sleep(DEFAULT_WAIT_MS);
        String nextPage = getUrlNextPage(nextPageToken);
        Log.d(TAG, "sendData: search with url=" + nextPage);
        fetchLocation(nextPage);
    }

    /**
     * fetch user current location and find the nearby restaurants based on preferences
     */
    private void fetchLocation(String customUrl) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // get locational information
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Object[] dataTransfer = new Object[5];

                        // find restaurants
                        NearbyData getNearbyPlacesData = new NearbyData(RestaurantCardFinder.this);
                        String url = getUrl(latitude, longitude);
                        dataTransfer[0] = customUrl == null ? url : customUrl;
                        dataTransfer[1] = location;
                        dataTransfer[2] = distance;
                        dataTransfer[3] = pricing;
                        dataTransfer[4] = rating;
                        getNearbyPlacesData.execute(dataTransfer);
                    }
                });
    }

    /**
     * get the google place url based on nextPageToken only
     */
    private String getUrlNextPage(String nextPageToken){
        String googlePlaceUrl = DEFAULT_PLACES_SEARCH_URL;
        googlePlaceUrl += "pagetoken=" + nextPageToken;
        googlePlaceUrl += "&key=" + getResources().getString(R.string.google_maps_key);
        return googlePlaceUrl;
    }

    /**
     * get the google place url based on the values passed
     */
    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlaceUrl = new StringBuilder(DEFAULT_PLACES_SEARCH_URL);

        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(distance);

        if (!foodType.equals("any")) googlePlaceUrl.append("&query=").append(foodType);
        googlePlaceUrl.append("&type=restaurant");

        googlePlaceUrl.append("&field=formatted_address,name,permanently_closed,photos,place_id," +
                "price_level,rating,user_ratings_total");

        googlePlaceUrl.append("&key=").append(getResources().getString(R.string.google_maps_key));

        Log.d(TAG, "getUrl: " + googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    /**
     * logs all of the nearbyPlacesList's HashMaps' name fields for debugging purposes
     * */
    private void logAllPlacesList(List<HashMap<String, String>> nearbyPlaceList){
        Log.d(TAG, "logAllPlacesList: printing nearbyPlacesList---------------------");
        for(HashMap<String, String> placesInfo : nearbyPlaceList){
            Log.d(TAG, "logAllPlacesList: restaurant name=" + placesInfo.get(DataParser.DATA_KEY_NAME));
        }
        Log.d(TAG, "logAllPlacesList: -----------------------------------------------");
    }

    /**
     * restore values passed in from PreferencesActivity.java
     */
    private void retrievePassedValues() {
        foodType = getIntent().getStringExtra(PreferencesActivity.PREF_INTENT_FOOD_TYPE);
        rating = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_RATING, ERROR_PASSED_VALUE);
        distance = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_DISTANCE, ERROR_PASSED_VALUE);
        pricing = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_PRICING, ERROR_PASSED_VALUE);
    }
}
