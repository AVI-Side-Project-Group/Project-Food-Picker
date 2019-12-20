package me.nakukibo.projectfoodpicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class MainActivity extends AppCompatActivity {
    // TODO: make a neutral option for some of the preferences and maybe switch out of using seek bars
    // TODO: figure out a secure way to store API keys
    private EditText edtxtFoodType;
    private SeekBar sbrRating;
    private SeekBar sbrDistance;
    private RadioGroup rdgroupPricing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        initPrefWidgets();

        // Initialize the SDK
        Places.initialize(getApplicationContext(), "AIzaSyDlyvqIWa52WgnfWn3OCb_vq8aaY4lu5z0");
        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);
    }

    public void submitPref(View view){
        System.out.println("Food: " + edtxtFoodType.getText().toString());
        System.out.println("Rating: " + sbrRating.getProgress());
        System.out.println("Distance: " + sbrDistance.getProgress());
        System.out.println("Pricing: " + ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId())).getText().toString());
    }

    private void initPrefWidgets() {
        edtxtFoodType = findViewById(R.id.edtxt_foodtype);
        sbrRating = findViewById(R.id.sbr_rating);
        sbrDistance = findViewById(R.id.sbr_distance);
        rdgroupPricing = findViewById(R.id.rdgroup_pricing);

        final TextView ratingVal = findViewById(R.id.txtvw_rating_progress);
        final TextView distanceVal = findViewById(R.id.txtvw_distance_progress);

        ratingVal.setText(String.valueOf(sbrRating.getProgress()));
        distanceVal.setText(String.valueOf(sbrDistance.getProgress()));
        sbrRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ratingVal.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbrDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceVal.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
