package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String SETTING_THEME = "theme";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Spinner spinTheme;

    private String[] themes = {"Light", "Purple"};

    private String currentTheme;

    private boolean schduledRestart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        setContentView(R.layout.settings_activity);

        initTheme();
        checkSharedPreference();
        changeTheme(currentTheme);

        super.onCreate(savedInstanceState);

        /*getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        /*sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        checkSharedPreference();*/

        //String selectedTheme = spinTheme.getSelectedItem().toString();
       // changeTheme(selectedTheme);
        /*if(currentTheme == selectedTheme) {
            editor.putString(getString(R.string.sp_theme), currentTheme);
            editor.commit();
        } else {
            editor.putString(getString(R.string.sp_theme), selectedTheme);
            editor.commit();
        }*/
    }

    private void initTheme() {
        spinTheme = findViewById(R.id.spin_theme);
        ArrayAdapter<String> adapterTheme = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, themes);
        spinTheme.setAdapter(adapterTheme);
        spinTheme.setSelection(findThemePosition(currentTheme));
    }

    /*public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if(schduledRestart){
            schduledRestart = false;
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private void checkSharedPreference() {
        currentTheme = sharedPreferences.getString(getString(R.string.sp_theme), "Light");
        int pos = findThemePosition(currentTheme);
        spinTheme.setSelection(pos);
    }

    private int findThemePosition(String theme) {
        for(int i = 0; i < themes.length; i++) {
            if(themes[i] == theme) return i;
        }
        return -1;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences spref, String key) {
        if(key.equals(getString(R.string.sp_theme)) && !spref.getString(getString(R.string.sp_theme), "Light").equals(currentTheme))
        {
            changeTheme(spref.getString(getString(R.string.sp_theme), "Light"));
            schduledRestart = true;
        }
    }

    public void changeTheme(String theme) {
        int pos = findThemePosition(theme);
        switch(pos){
            case 0:
                setTheme(R.style.Light);
                break;
            case 1:
                setTheme(R.style.Purple);
                break;
            //case 2:
            case -1:
                break;
        }
    }

    public void returnPreferenceActivity(View view){
        Intent switchIntent = new Intent(this, PreferencesActivity.class);
        startActivity(switchIntent);
    }
}
