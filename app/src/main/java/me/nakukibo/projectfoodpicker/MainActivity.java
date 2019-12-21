package me.nakukibo.projectfoodpicker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // TODO: make a neutral option for some of the preferences and maybe switch out of using seek bars
    // TODO: figure out a secure way to store API keys
    private Spinner spinFoodtype;
    private Spinner spinRating;
    private SeekBar sbrDistance;
    private RadioGroup rdgroupPricing;
    private String[] foodTypes = {"Any", "American", "African", "Asian", "European", "Mediterranean",
                                    "Mexican"};
    private String[] ratings = {"Any", "2 star", "3 star", "4 star"};
    private Float[] distances = {.5f, 1f, 5f, 10f, 20f};

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
        System.out.println("Food: " + spinFoodtype.getSelectedItem().toString());
        System.out.println("Rating: " + spinRating.getSelectedItem().toString());
        System.out.println("Distance: " + getDistance(sbrDistance.getProgress()));
        System.out.println("Pricing: " + ((RadioButton) findViewById(rdgroupPricing.getCheckedRadioButtonId())).getText().toString());
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

    private String getDistance(int index){
        return String.format(Locale.US, "%2.1f miles", distances[index]);
    }
}
