package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RestaurantCardFinder extends AppCompatActivity implements ReceiveData {

    private final static int ERROR_PASSED_VALUE = -1;
    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private String foodType;
    private int distance;
    private String pricing;
    private int rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_card_finder);

        retrievePassedValues();
        fetchLocation();
    }

    @Override
    public void sendData(List<HashMap<String, String>> nearbyPlaceList) {
        // get random restaurant passed back
        int index = (int) (Math.random() * (nearbyPlaceList.size() + 1));
        HashMap<String, String> selectedRestaurant = nearbyPlaceList.get(index);

        // set view values depending on the restaurant values
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
     * fetch user current location and find the nearby restaurants based on preferences
     */
    private void fetchLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // get locational information
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Object[] dataTransfer = new Object[2];

                            // find restaurants
                            NearbyData getNearbyPlacesData = new NearbyData(RestaurantCardFinder.this);
                            String url = getUrl(latitude, longitude);
                            dataTransfer[0] = url;
                            getNearbyPlacesData.execute(dataTransfer);
                        }
                    }
                });
    }

    /**
     * get the google place url based on the values passed
     */
    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(distance);
        if (!foodType.equals("any")) googlePlaceUrl.append("&query=").append(foodType);
        googlePlaceUrl.append("&type=restaurant");
        //if (!foodType.equals("any")) googlePlaceUrl.append("&keyword=").append(foodType);
        googlePlaceUrl.append("&sensor=true"); //take out?
        googlePlaceUrl.append("&field=formatted_address,name,permanently_closed,photos,place_id," +
                "price_level,rating,user_ratings_total");
        googlePlaceUrl.append("&key=AIzaSyCd9Q5wxR59XOi1ugwZzH4l8fa2_BnBvOI");

        Log.d(TAG, "getUrl: " + googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    /**
     * restore values passed in from MainActivity.java
     */
    private void retrievePassedValues() {
        foodType = getIntent().getStringExtra(MainActivity.PREF_INTENT_FOOD_TYPE);
        rating = getIntent().getIntExtra(MainActivity.PREF_INTENT_RATING, ERROR_PASSED_VALUE);
        distance = getIntent().getIntExtra(MainActivity.PREF_INTENT_DISTANCE, ERROR_PASSED_VALUE);
        pricing = getIntent().getStringExtra(MainActivity.PREF_INTENT_PRICING);
    }
}
