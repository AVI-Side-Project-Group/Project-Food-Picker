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

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static PlacesClient placesClient;
    // TODO: get rid of unnecessary stuff from tutorial
    // TODO: remove stuff as needed from permissions and figure out what each of them do
    // TODO: deal with "next page" in json readings
    private Spinner spinFoodtype;
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

        initPlacesAPI();
        requestLocationPermission();
        initPrefWidgets();
    }

    public void submitPref(View view) {
        if (isLocationOn) {
            Log.d(TAG, "submitPref: Attempting to submit preferences");
            Log.d(TAG, "submitPref: " + String.format("Food: %s",
                    spinFoodtype.getSelectedItem().toString()));
            Log.d(TAG, "submitPref: " + String.format("Rating: %s",
                    spinRating.getSelectedItem().toString()));
            Log.d(TAG, "submitPref:  " + String.format("Distance: %s",
                    getDistance(sbrDistance.getProgress())));
            Log.d(TAG, "submitPref:  " + String.format("Pricing: %s",
                    ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId())).getText().toString()));

            // go to MapsActivity.java
            Intent switchIntent = new Intent(this, MapsActivity.class);
            startActivity(switchIntent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "You cannot search with location off.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{ACCESS_FINE_LOCATION},
                1);

        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            isLocationOn = false;
        } else {
            isLocationOn = true;
        }
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

    private String getDistance(int index) {
        return String.format(Locale.US, "%2.1f miles", distances[index]);
    }
}
