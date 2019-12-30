package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    // variables used to pass data between MainActivity and RestaurantCardFinder
    public static final String PREF_INTENT_FOOD_TYPE = "food_type";
    public static final String PREF_INTENT_RATING = "rating";
    public static final String PREF_INTENT_DISTANCE = "distance";
    public static final String PREF_INTENT_PRICING = "pricing";

    // tag for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    // flag for checking whether the user allowed for location data
    private boolean isLocationOn;

    // views from layout
    private Spinner spinFoodType;
    private Spinner spinRating;
    private SeekBar sbrDistance;
    private RadioGroup rdgroupPricing;

    // values for views
    private String[] foodTypes = {"Any", "American", "African", "Asian",
            "Barbecue", "European", "Hamburger", "Mediterranean",
            "Mexican", "Pizza", "Seafood", "Steak"};
    private int[] minRatings = {0, 1, 2, 3, 4};
    private Float[] distances = {.5f, 1f, 5f, 10f, 20f};

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
            int distMeters = milesToMeters(distances[sbrDistance.getProgress()]);
            String pricing = ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId()))
                    .getText()
                    .toString();

            // log the values
            Log.d(TAG, "submitPref: Attempting to submit preferences");
            logValues(TAG, "submitPref", foodType, String.valueOf(rating),
                    String.valueOf(distMeters), pricing);

            // go to RestaurantCardFinder.java and pass along values
            Intent switchIntent = new Intent(this, RestaurantCardFinder.class);
            switchIntent.putExtra(PREF_INTENT_FOOD_TYPE, foodType);
            switchIntent.putExtra(PREF_INTENT_RATING, rating);
            switchIntent.putExtra(PREF_INTENT_DISTANCE, distMeters);
            switchIntent.putExtra(PREF_INTENT_PRICING, pricing);
            startActivity(switchIntent);
        } else {
            // tell user error message
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
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, 1);
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
        spinFoodType = findViewById(R.id.spin_foodtype);
        spinRating = findViewById(R.id.spin_rating);
        sbrDistance = findViewById(R.id.sbr_distance);
        rdgroupPricing = findViewById(R.id.rdgroup_pricing);

        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodTypes);
        spinFoodType.setAdapter(adapterFood);
        spinFoodType.setSelection(0);

        String[] ratingString = new String[minRatings.length];
        for (int i = 0; i < minRatings.length; i++) {
            int rating = minRatings[i];
            if (rating == 0) {
                ratingString[i] = "Any";
            } else {
                ratingString[i] = String.format(Locale.US, "%d stars", rating);
            }
        }
        ArrayAdapter<String> adapterRating = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ratingString);
        spinRating.setAdapter(adapterRating);
        spinRating.setSelection(0);

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
        Intent switchIntent = new Intent(this, Settings.class);
        startActivity(switchIntent);
    }
}
