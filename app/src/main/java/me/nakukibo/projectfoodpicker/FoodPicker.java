package me.nakukibo.projectfoodpicker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class FoodPicker extends Application {
    private static Context app;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static PlacesClient placesClient;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        // Initialize the SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        // Create a new Places client instance
        placesClient = Places.createClient(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        editor = sharedPreferences.edit();
    }

    public static Context getApp(){
        return app;
    }

    public static SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditor(){
        return editor;
    }

    public static PlacesClient getPlacesClient() {return placesClient;}
}
