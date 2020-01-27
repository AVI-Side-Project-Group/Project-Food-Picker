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
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class CustomAppCompatActivity extends AppCompatActivity {

    private static final String TAG = CustomAppCompatActivity.class.getSimpleName();

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

    void resetDaily() {

        SharedPreferences.Editor editor = getApplicationSharedPreferences().edit();
        String newDate = getNewDate();

        if(newDate != null){
            editor.remove(getString(R.string.sp_previously_accessed_json));
            editor.apply();

            editor.remove(getString(R.string.sp_remained_rerolls));
            editor.apply();

            editor.putString(getString(R.string.sp_date), newDate);
            editor.apply();
        }
    }

    String getNewDate(){

        SharedPreferences sharedPreferences = getApplicationSharedPreferences();
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String date = getDateFormat(day, month, year);
        String lastDate = sharedPreferences.getString(getString(R.string.sp_date), getDefaultDate());

        Log.d(TAG, "getNewDate: current date=" + date + ", last date=" + lastDate);

        return lastDate.equals(date) ? null : date;
    }

    private static String getDateFormat(int day, int month, int year){
        return String.format(Locale.US, "%02d%02d%04d", day, month, year);
    }

    private static String getDefaultDate(){
        return getDateFormat(0, 0, 0);
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
