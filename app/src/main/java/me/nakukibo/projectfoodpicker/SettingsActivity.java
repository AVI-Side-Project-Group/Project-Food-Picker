package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Spinner spinTheme;
    private String[] themes = {"Light", "Dark", "Purple"};
    private int[] themeIDs = {R.style.Light, R.style.Dark, R.style.Purple};
    private int currentTheme;
    private int selectedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = FoodPicker.getSharedPreferences();
        editor = FoodPicker.getEditor();
        currentTheme = sharedPreferences.getInt(getString(R.string.sp_theme), themeIDs[0]);

        setTheme(currentTheme);
        setContentView(R.layout.settings_activity);

        initTheme();
        checkSharedPreference();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initTheme() {
        spinTheme = findViewById(R.id.spin_theme);
        ArrayAdapter<String> adapterTheme = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, themes);
        spinTheme.setAdapter(adapterTheme);
        spinTheme.setSelection(getThemeIDPosition(currentTheme));
    }

    private void checkSharedPreference() {
        int pos = getThemeIDPosition(currentTheme);
        spinTheme.setSelection(pos);

        Log.d(TAG, "checkSharedPreference: " + currentTheme);
    }

    private int findThemeIDByName(String theme){
        for(int i = 0; i < themes.length; i++){
            if(theme.equals(themes[i])) return themeIDs[i];
        }
        return -1;
    }

    private int getThemeIDPosition(int id){
        for(int i = 0; i < themes.length; i++){
            if(id == themeIDs[i]) return i;
        }
        return -1;
    }

    public void applySettings(View view){
        selectedTheme = findThemeIDByName(spinTheme.getSelectedItem().toString());
        Log.d(TAG, "applySettings: " + selectedTheme);
        setTheme(selectedTheme);
        editor.putInt(getString(R.string.sp_theme), selectedTheme);
        editor.commit();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void finishSettings(View view){
        finish();
        Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
