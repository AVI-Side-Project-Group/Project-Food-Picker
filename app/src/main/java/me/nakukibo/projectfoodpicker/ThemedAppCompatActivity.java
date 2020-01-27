package me.nakukibo.projectfoodpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class ThemedAppCompatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String[] themes = {"Light", "Dark", "Purple"};
    private int[] themeIDs = {R.style.Light, R.style.Dark, R.style.Purple};
    private int currentTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        initSharedPreferences();
        initTheme();

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return true;
    }

    /**
     * initializes sharedPreferences for the activity using application context
     */
    private void initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     * initializes currentTheme field to the theme key to the value stored in sharedPreferences
     * defaults to themeIDs[0] if not available
     */
    private void initTheme() {
        currentTheme = sharedPreferences.getInt(getString(R.string.sp_theme), themeIDs[0]);
        setTheme(currentTheme);
    }

    /**
     * checks to see if has location permission
     *
     * @return true: location permission enabled
     * false: location permission disabled
     */
    boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * checks to see if the user is connected to the internet
     *
     * @return true if connected
     * false if not connected or failed to initialize the needed variables
     */
    boolean checkNetworkConnection() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT < 23) {
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null) {
                return (ni.isConnected() &&
                        (ni.getType() == ConnectivityManager.TYPE_WIFI ||
                                ni.getType() == ConnectivityManager.TYPE_MOBILE));
            }

            return false;
        } else {
            final Network n = connectivityManager.getActiveNetwork();

            if (n != null) {
                final NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(n);

                if (nc == null) return false;

                return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }

        return false;
    }

    /**
     * will be null if called before onCreate
     */
    SharedPreferences getApplicationSharedPreferences() {
        return sharedPreferences;
    }

    String[] getThemes() {
        return themes;
    }

    int[] getThemeIDs() {
        return themeIDs;
    }

    int getCurrentTheme() {
        return currentTheme;
    }
}
