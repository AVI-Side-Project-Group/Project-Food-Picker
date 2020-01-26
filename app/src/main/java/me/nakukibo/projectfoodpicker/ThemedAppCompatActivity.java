package me.nakukibo.projectfoodpicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public abstract class ThemedAppCompatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String[] themes = {"Light", "Dark", "Purple"};
    private int[] themeIDs = {R.style.Light, R.style.Dark, R.style.Purple};
    private int currentTheme;

    private static final String TAG = ThemedAppCompatActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initSharedPreferences();
        initTheme();
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private void initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void initTheme() {
        currentTheme = sharedPreferences.getInt(getString(R.string.sp_theme), themeIDs[0]);
        setTheme(currentTheme);
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
