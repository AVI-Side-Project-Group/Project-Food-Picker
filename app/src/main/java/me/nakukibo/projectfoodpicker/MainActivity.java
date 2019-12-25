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

    // TODO: get rid of unnecessary stuff from tutorial
    // TODO: remove stuff as needed from permissions and figure out what each of them do
    // TODO: deal with "next page" in json readings
    // TODO: change the pricing and other parameters to the appropriate data type
    // TODO: add a back button to go back to preferences

    public static final String PREF_INTENT_FOOD_TYPE = "food_type";
    public static final String PREF_INTENT_RATING = "rating";
    public static final String PREF_INTENT_DISTANCE = "distance";
    public static final String PREF_INTENT_PRICING = "pricing";

    private static final String TAG = MainActivity.class.getSimpleName();

    private Spinner spinFoodType;
    private Spinner spinRating;
    private SeekBar sbrDistance;
    private RadioGroup rdgroupPricing;
    private String[] foodTypes = {"Any", "American", "African", "Asian", "European", "Mediterranean",
            "Mexican"};
    private String[] ratings = {"Any", "2 star", "3 star", "4 star"};
    private Float[] distances = {.5f, 1f, 5f, 10f, 20f};
    private boolean isLocationOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        requestLocationPermission();
        initPrefWidgets();
    }

    public void submitPref(View view) {
        if (isLocationOn) {
            String foodType = spinFoodType.getSelectedItem().toString();
            String rating = spinRating.getSelectedItem().toString();
            int distMeters = milesToMeters(distances[sbrDistance.getProgress()]);
            String pricing = ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId()))
                    .getText()
                    .toString();

            Log.d(TAG, "submitPref: Attempting to submit preferences");
            Log.d(TAG, "submitPref: " + String.format("Food: %s", foodType));
            Log.d(TAG, "submitPref: " + String.format("Rating: %s", rating));
            Log.d(TAG, "submitPref:  " + String.format("Distance: %d meters", distMeters));
            Log.d(TAG, "submitPref:  " + String.format("Pricing: %s", pricing));

            // go to MapsActivity.java
            Intent switchIntent = new Intent(this, MapsActivity.class);
            switchIntent.putExtra(PREF_INTENT_FOOD_TYPE, foodType);
            switchIntent.putExtra(PREF_INTENT_RATING, rating);
            switchIntent.putExtra(PREF_INTENT_DISTANCE, distMeters);
            switchIntent.putExtra(PREF_INTENT_PRICING, pricing);
            startActivity(switchIntent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "You cannot search with location off.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, 1);
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationOn = false;
            }
        }
        isLocationOn = true;
    }

    private void initPrefWidgets() {
        spinFoodType = findViewById(R.id.spin_foodtype);
        spinRating = findViewById(R.id.spin_rating);
        sbrDistance = findViewById(R.id.sbr_distance);
        rdgroupPricing = findViewById(R.id.rdgroup_pricing);

        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodTypes);
        spinFoodType.setAdapter(adapterFood);
        spinFoodType.setSelection(0);

        ArrayAdapter<String> adapterRating = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ratings);
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private int milesToMeters(double miles){
        return (int) Math.ceil(miles*1609.34);
    }

    private String getDistance(int index) {
        return String.format(Locale.US, "%2.1f miles", distances[index]);
    }
}
