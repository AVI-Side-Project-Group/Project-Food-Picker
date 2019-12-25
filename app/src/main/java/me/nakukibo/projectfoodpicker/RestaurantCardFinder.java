package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;

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

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        retrievePassedValues();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Object[] dataTransfer = new Object[2];
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
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(distance);
        googlePlaceUrl.append("&type=restaurant");
        if (!foodType.equals("any")) googlePlaceUrl.append("&keyword=").append(foodType);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=AIzaSyCd9Q5wxR59XOi1ugwZzH4l8fa2_BnBvOI");

        return googlePlaceUrl.toString();
    }

    private void retrievePassedValues() {
        foodType = getIntent().getStringExtra(MainActivity.PREF_INTENT_FOOD_TYPE);
        rating = getIntent().getIntExtra(MainActivity.PREF_INTENT_RATING, ERROR_PASSED_VALUE);
        distance = getIntent().getIntExtra(MainActivity.PREF_INTENT_DISTANCE, ERROR_PASSED_VALUE);
        pricing = getIntent().getStringExtra(MainActivity.PREF_INTENT_PRICING);
    }

    @Override
    public void sendData(List<HashMap<String, String>> nearbyPlaceList) {
        HashMap<String, String> selectedRestaurant = nearbyPlaceList.get(0);
        TextView txtvwName = findViewById(R.id.txtvw_name);
        txtvwName.setText(selectedRestaurant.get("place_name"));
    }
}
