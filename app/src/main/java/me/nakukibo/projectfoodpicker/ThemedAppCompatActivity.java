package me.nakukibo.projectfoodpicker;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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
