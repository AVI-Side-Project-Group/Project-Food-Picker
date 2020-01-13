package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends ThemedAppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private Spinner spinTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        initTheme();
        checkSharedPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initTheme() {
        spinTheme = findViewById(R.id.spin_theme);
        ArrayAdapter<String> adapterTheme = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getThemes());
        spinTheme.setAdapter(adapterTheme);
        spinTheme.setSelection(getThemeIDPosition(getCurrentTheme()));
    }

    private void checkSharedPreference() {
        int pos = getThemeIDPosition(getCurrentTheme());
        spinTheme.setSelection(pos);

        Log.d(TAG, "checkSharedPreference: " + getCurrentTheme());
    }

    private int findThemeIDByName(String theme){
        String[] themes = getThemes();
        for(int i = 0; i < themes.length; i++){
            if (theme.equals(themes[i])) return getThemeIDs()[i];
        }
        return -1;
    }

    private int getThemeIDPosition(int id){
        String[] themes = getThemes();
        for(int i = 0; i < themes.length; i++){
            if (id == getThemeIDs()[i]) return i;
        }
        return -1;
    }

    public void applySettings(View view){
        int selectedTheme;
        SharedPreferences.Editor editor = getApplicationSharedPreferences().edit();

        selectedTheme = findThemeIDByName(spinTheme.getSelectedItem().toString());
        Log.d(TAG, "applySettings: selectedTheme=" + selectedTheme);
        setTheme(selectedTheme);

        editor.putInt(getString(R.string.sp_theme), selectedTheme);
        editor.apply();

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
