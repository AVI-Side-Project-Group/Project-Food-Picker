package me.nakukibo.projectfoodpicker;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinFoodtype;
    private Spinner spinRating;
    private SeekBar sbrDistance;
    private RadioGroup rdgroupPricing;
    private String[] foodTypes = {"Any", "American", "African", "Asian", "European", "Mediterranean",
                                    "Mexican"};
    private String[] ratings = {"Any", "2 star", "3 star", "4 star"};
    private Float[] distances = {.5f, 1f, 5f, 10f, 20f};

    private static final String TAG = MainActivity.class.getSimpleName();
    private static PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        requestLocationPermission();
        initPrefWidgets();
        initPlacesAPI();
    }

    public void submitPref(View view){
        Log.d(TAG, "submitPref: Attempting to submit preferences");
        Log.d(TAG, "submitPref: " + String.format("Food: %s",
                spinFoodtype.getSelectedItem().toString()));
        Log.d(TAG, "submitPref: " + String.format("Rating: %s",
                spinRating.getSelectedItem().toString()));
        Log.d(TAG, "submitPref:  " + String.format("Distance: %s",
                getDistance(sbrDistance.getProgress())));
        Log.d(TAG, "submitPref:  " + String.format("Pricing: %s",
                ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId())).getText().toString()));
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    private void initPrefWidgets() {
        spinFoodtype = findViewById(R.id.spin_foodtype);
        spinRating = findViewById(R.id.spin_rating);
        sbrDistance = findViewById(R.id.sbr_distance);
        rdgroupPricing = findViewById(R.id.rdgroup_pricing);

        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodTypes);
        spinFoodtype.setAdapter(adapterFood);
        spinFoodtype.setSelection(0);

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

    private void initPlacesAPI() {
        Places.initialize(getApplicationContext(), "AIzaSyDlyvqIWa52WgnfWn3OCb_vq8aaY4lu5z0");
        placesClient = Places.createClient(this);
    }

    private String getDistance(int index){
        return String.format(Locale.US, "%2.1f miles", distances[index]);
    }
}
