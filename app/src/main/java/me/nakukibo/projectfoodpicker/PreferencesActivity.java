package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.Locale;
import java.util.Set;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PreferencesActivity extends CustomAppCompatActivity {

    private static final String TAG = PreferencesActivity.class.getSimpleName();

    // variables used to pass data between PreferencesActivity and RestaurantCardFinderActivity
    public static final int PREF_ANY_INT_REP = 0;
    public static final String PREF_ANY_STR_REP = "Any";

    public static final String PREF_INTENT_FOOD_TYPE = "food_type";
    public static final String PREF_INTENT_RATING = "rating";
    public static final String PREF_INTENT_DISTANCE = "distance";
    public static final String PREF_INTENT_PRICING = "pricing";
    public static final String PREF_INTENT_OPEN_NOW = "open_now";

    // views from layout
    private Spinner spinFoodType;
    private Spinner spinRating;
    private Spinner spinPricing;
    private SeekBar sbrDistance;
    private Switch toggleOpenNow;

    // values for views
    private static String[] foodTypes = {PREF_ANY_STR_REP, "American", "Asian",
            "Barbecue", "Boba", "Dessert", "European", "Hamburger", "Mediterranean",
            "Mexican", "Pizza", "Seafood", "Steak"};
    private static int[] minRatings = {0, 2, 3, 4};
    private static int[] priceRanges = {0, 2, 3, 4};
    private static float[] distances = {.5f, 1f, 5f, 10f, 20f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        resetDaily();
        requestLocationPermission();
        initPrefViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetDaily();
        activateActivityButtons();
    }

    /**
     * ask user for location permission if not already enabled
     * */
    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    PreferencesActivity.this, new String[]{ACCESS_FINE_LOCATION},
                    1
            );
        }
    }

    /**
     * initializes the view globals and the values
     */
    private void initPrefViews() {

        initFoodTypesView();
        initRatingsView();
        initDistancesView();
        initPriceRangesView();
        initIsOpenNowView();
        activateActivityButtons();
    }

    private void initFoodTypesView() {

        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodTypes);

        spinFoodType = findViewById(R.id.spin_foodtype);
        spinFoodType.setAdapter(adapterFood);
        spinFoodType.setSelection(0);
    }

    private void initRatingsView() {

        ArrayAdapter<String> adapterRating;
        String[] ratingString = new String[minRatings.length];

        for (int i = 0; i < minRatings.length; i++) {

            final int rating = minRatings[i];

            if (rating == PREF_ANY_INT_REP) {
                ratingString[i] = PREF_ANY_STR_REP;
            } else {
                ratingString[i] = String.format(Locale.US, "%d stars", rating);
            }
        }

        adapterRating = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ratingString);

        spinRating = findViewById(R.id.spin_rating);
        spinRating.setAdapter(adapterRating);
        spinRating.setSelection(0);
    }

    private void initIsOpenNowView() {
        toggleOpenNow = findViewById(R.id.toggle_open_now);
        toggleOpenNow.setChecked(true);
    }

    private void initPriceRangesView() {

        ArrayAdapter<String> adapterPricing;
        String[] pricingString = new String[priceRanges.length];

        for (int i = 0; i < priceRanges.length; i++) {

            final int pricing = priceRanges[i];

            if (pricing == PREF_ANY_INT_REP) {
                pricingString[i] = PREF_ANY_STR_REP;
            } else {
                pricingString[i] = getPricingText(pricing);
            }
        }

        adapterPricing = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, pricingString);

        spinPricing = findViewById(R.id.spin_pricing);
        spinPricing.setAdapter(adapterPricing);
        spinPricing.setSelection(0);
    }

    private void initDistancesView() {

        final TextView distanceVal = findViewById(R.id.txtvw_distance_progress);
        sbrDistance = findViewById(R.id.sbr_distance);

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

    public void changeSettings(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        deactivateActivityButtons();
        startActivity(intent);
    }

    public void openHistory(View view) {
        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        deactivateActivityButtons();
        startActivity(intent);
    }

    /**
     * gets the preference values and opens the results activity (RestaurantCardFinderActivity.java)
     */
    public void submitPref(View view) {

        // if not search button then wrong view
        if (view.getId() != R.id.btn_search) return;

        // check if user is connected to the internet
        if (!checkNetworkConnection()) {
            // notify user error message
            Toast toast = Toast.makeText(getApplicationContext(), "There is no internet connection. Please try again later.",
                    Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (hasLocationPermission()) {
            startRestaurantCardFinder();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "You cannot search with location off.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void startRestaurantCardFinder() {

        final Intent switchIntent = new Intent(this, RestaurantCardFinderActivity.class);
        final int errorMargin = getApplicationSharedPreferences()
                .getInt(getString(R.string.sp_distance_margin), SettingsActivity.MARGIN_MULTIPLIER);

        // retrieve value from preferences
        final String foodType = spinFoodType.getSelectedItem().toString();
        final int rating = minRatings[spinRating.getSelectedItemPosition()];
        final int pricing = priceRanges[spinPricing.getSelectedItemPosition()];
        final int distMeters = milesToMeters(getMaxDistance(distances[sbrDistance.getProgress()], errorMargin));
        final boolean mustBeOpen = toggleOpenNow.isChecked();

        deactivateActivityButtons();

        // log the values
        Log.d(TAG,
                "submitPref: Attempting to submit preferences:" +
                        "foodType=" + foodType + ", " +
                        "distMeters=" + distMeters + ", " +
                        "pricing=" + pricing + ", " +
                        "mustBeOpen=" + mustBeOpen
        );

        // go to RestaurantCardFinderActivity.java and pass along values
        switchIntent.putExtra(PREF_INTENT_FOOD_TYPE, foodType);
        switchIntent.putExtra(PREF_INTENT_RATING, rating);
        switchIntent.putExtra(PREF_INTENT_PRICING, pricing);
        switchIntent.putExtra(PREF_INTENT_DISTANCE, distMeters);
        switchIntent.putExtra(PREF_INTENT_OPEN_NOW, mustBeOpen);

        startActivity(switchIntent);
    }

    private void activateActivityButtons() {
        findViewById(R.id.btn_search).setClickable(true);
        findViewById(R.id.btn_setting).setClickable(true);
        findViewById(R.id.btn_get_history).setClickable(true);
    }

    private void deactivateActivityButtons() {
        findViewById(R.id.btn_search).setClickable(false);
        findViewById(R.id.btn_setting).setClickable(false);
        findViewById(R.id.btn_get_history).setClickable(false);
    }

    public static Double metersToMiles(Float meters) {
        if (meters == null) return null;
        return meters * 0.000621371;
    }

    private static float getMaxDistance(float distance, int errorMargin) {
        return distance + errorMargin * distance / 100.0f;
    }

    public static String getPricingText(int pricing) {
        if (pricing <= 0) return "free";
        return "$$$$".substring(0, pricing);
    }

    /**
     * converts miles to meters
     * @param miles miles value
     * @return int rounded up value of meters
     * */
    private static int milesToMeters(double miles){
        return (int) Math.ceil(miles*1609.34);
    }

    /**
     * returns the distance as a string: "[miles of the index] miles"
     * @param index  index of the distances array
     * @return String string representation of the value to be displayed in view
     * */
    private static String getDistance(int index) {
        float distance = distances[index];
        return String.format(Locale.US, "%2.1f miles", distance);
    }

    public static String getDefaultFoodType(){
        return foodTypes[0];
    }

    public static int getDefaultRating(){
        return minRatings[0];
    }

    public static int getDefaultPriceRange(){
        return priceRanges[0];
    }

    public static int getDefaultDistanceMeters(int errorMargin){
        return milesToMeters(getMaxDistance(distances[0], errorMargin));
    }

    public static boolean getDefaultIsOpen(){
        return false;
    }
}
