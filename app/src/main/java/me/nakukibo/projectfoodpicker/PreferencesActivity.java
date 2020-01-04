package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PreferencesActivity extends AppCompatActivity {

    // variables used to pass data between PreferencesActivity and RestaurantCardFinder
    public static final String PREF_INTENT_FOOD_TYPE = "food_type";
    public static final String PREF_ANY_STR_REP = "Any";
    public static final String PREF_INTENT_RATING = "rating";
    public static final String PREF_INTENT_DISTANCE = "distance";
    public static final String PREF_INTENT_PRICING = "pricing";
    public static final int PREF_ANY_INT_REP = 0;
    // tag for logging
    private static final String TAG = PreferencesActivity.class.getSimpleName();

    // flag for checking whether the user allowed for location data
    private boolean isLocationOn;

    // views from layout
    private Spinner spinFoodType;
    private Spinner spinRating;
    private Spinner spinPricing;
    private SeekBar sbrDistance;

    // values for views
    private String[] foodTypes = {PREF_ANY_STR_REP, "American", "Asian",
            "Barbecue", "Dessert", "European", "Hamburger", "Mediterranean",
            "Mexican", "Pizza", "Seafood", "Steak"};
    private int[] minRatings = {0, 1, 2, 3, 4};
    private int[] priceRanges = {0, 1, 2, 3, 4};
    private float[] distances = {.5f, 1f, 5f, 10f, 20f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        requestLocationPermission();
        initPrefViews();
    }

    /**
     * logs all values as debug
     *
     * @param tag:      the tag for the messages
     * @param funcName: function name where this function is called from
     * @param values:   values to be logged
     */
    public static void logValues(String tag, String funcName, String... values) {
        for (String value : values) {
            Log.d(tag, funcName + ": " + value);
        }
    }

    /**
     * gets the preference values and opens the results activity (RestaurantCardFinder.java)
     *
     * @param view the view to be interfaced
     */
    public void submitPref(View view) {
        // if not search button then wrong view
        if (view.getId() != R.id.btn_search) return;

        if (isLocationOn) {
            // retrieve value from preferences
            String foodType = spinFoodType.getSelectedItem().toString();
            int rating = spinRating.getSelectedItemPosition();
            int pricing = spinPricing.getSelectedItemPosition();
            int distMeters = milesToMeters(distances[sbrDistance.getProgress()]);

            // log the values
            Log.d(TAG, "submitPref: Attempting to submit preferences:");
            logValues(TAG, "submitPref", foodType, String.valueOf(rating),
                    String.valueOf(distMeters), String.valueOf(pricing));

            // go to RestaurantCardFinder.java and pass along values
            Intent switchIntent = new Intent(this, RestaurantCardFinder.class);
            switchIntent.putExtra(PREF_INTENT_FOOD_TYPE, foodType);
            switchIntent.putExtra(PREF_INTENT_RATING, rating);
            switchIntent.putExtra(PREF_INTENT_PRICING, pricing);
            switchIntent.putExtra(PREF_INTENT_DISTANCE, distMeters);
            startActivity(switchIntent);
        } else {
            // notify user error message
            Toast toast = Toast.makeText(getApplicationContext(), "You cannot search with location off.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * ask user for location permission and sets global isLocationOn
     * */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PreferencesActivity.this, new String[]{ACCESS_FINE_LOCATION}, 1);
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationOn = false;
            }
        }
        isLocationOn = true;
    }

    /**
     * initializes the view globals and the values
     */
    private void initPrefViews() {
        initFoodTypesView();
        initRatingsView();
        initDistancesView();
        initPriceRangesView();
    }

    private void initFoodTypesView() {
        spinFoodType = findViewById(R.id.spin_foodtype);
        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodTypes);
        spinFoodType.setAdapter(adapterFood);
        spinFoodType.setSelection(0);
    }

    private void initRatingsView() {
        spinRating = findViewById(R.id.spin_rating);
        String[] ratingString = new String[minRatings.length];
        for (int i = 0; i < minRatings.length; i++) {
            int rating = minRatings[i];
            if (rating == PREF_ANY_INT_REP) {
                ratingString[i] = PREF_ANY_STR_REP;
            } else {
                ratingString[i] = String.format(Locale.US, "%d stars", rating);
            }
        }
        ArrayAdapter<String> adapterRating = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ratingString);
        spinRating.setAdapter(adapterRating);
        spinRating.setSelection(0);
    }

    private void initPriceRangesView() {
        spinPricing = findViewById(R.id.spin_pricing);
        String[] pricingString = new String[priceRanges.length];
        for (int i = 0; i < priceRanges.length; i++) {
            int pricing = priceRanges[i];
            if (pricing == PREF_ANY_INT_REP) {
                pricingString[i] = PREF_ANY_STR_REP;
            } else {
                pricingString[i] = "$$$$".substring(0, pricing);
            }
        }
        ArrayAdapter<String> adapterPricing = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, pricingString);
        spinPricing.setAdapter(adapterPricing);
        spinPricing.setSelection(0);
    }

    private void initDistancesView() {
        sbrDistance = findViewById(R.id.sbr_distance);
        final TextView distanceVal = findViewById(R.id.txtvw_distance_progress);
        distanceVal.setText(getDistance(sbrDistance.getProgress()));
        sbrDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceVal.setText(getDistance(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    /**
     * converts miles to meters
     * @param miles miles value
     * @return int rounded up value of meters
     * */
    private int milesToMeters(double miles){
        return (int) Math.ceil(miles*1609.34);
    }

    /**
     * returns the distance as a string: "[miles of the index] miles"
     * @param index  index of the distances array
     * @return String string representation of the value to be displayed in view
     * */
    private String getDistance(int index) {
        return String.format(Locale.US, "%2.1f miles", distances[index]);
    }

    public void changeSettings(View view) {
        Intent switchIntent = new Intent(this, SettingsActivity.class);
        startActivity(switchIntent);
    }
}
