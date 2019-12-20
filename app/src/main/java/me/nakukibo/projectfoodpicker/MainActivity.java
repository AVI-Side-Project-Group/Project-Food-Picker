package me.nakukibo.projectfoodpicker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: figure out a secure way to store API keys
        // Initialize the SDK
        Places.initialize(getApplicationContext(), "AIzaSyDlyvqIWa52WgnfWn3OCb_vq8aaY4lu5z0");

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);

    }

}
