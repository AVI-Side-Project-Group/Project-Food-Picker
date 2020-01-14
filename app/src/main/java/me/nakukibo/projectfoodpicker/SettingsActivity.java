package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class SettingsActivity extends ThemedAppCompatActivity {

    static final int MARGIN_MULTIPLIER = 5;
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Spinner spinTheme;
    private Switch allowProminent;
    private SeekBar sbrDistanceMargin;
    private TextView txtvwMarginValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        initViewVariables();
        initTheme();
        checkSharedPreference();
    }

    private void initViewVariables() {
        spinTheme = findViewById(R.id.spin_theme);
        allowProminent = findViewById(R.id.toggle_allow_prominent);
        sbrDistanceMargin = findViewById(R.id.sbr_distance_margin);
        txtvwMarginValue = findViewById(R.id.txtvw_margin_value);

        sbrDistanceMargin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setMarginValue(getMarginFromSeekBarValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initTheme() {
        ArrayAdapter<String> adapterTheme = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getThemes());
        spinTheme.setAdapter(adapterTheme);
        spinTheme.setSelection(getThemeIDPosition(getCurrentTheme()));
    }

    private void checkSharedPreference() {
        int pos = getThemeIDPosition(getCurrentTheme());
        spinTheme.setSelection(pos);
        Log.d(TAG, "checkSharedPreference: " + getCurrentTheme());

        boolean prominentAllowed = getApplicationSharedPreferences().
                getBoolean(getResources().getString(R.string.sp_allow_prominent), false);
        allowProminent.setChecked(prominentAllowed);

        int margin = getApplicationSharedPreferences()
                .getInt(getResources().getString(R.string.sp_distance_margin), MARGIN_MULTIPLIER);
        sbrDistanceMargin.setProgress(getSeekBarValueFromMargin(margin));
        setMarginValue(margin);
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
        SharedPreferences.Editor editor = getApplicationSharedPreferences().edit();
        int selectedTheme = findThemeIDByName(spinTheme.getSelectedItem().toString());
        boolean prominentAllowed = allowProminent.isChecked();
        int margin = getMarginFromSeekBarValue();

        setTheme(selectedTheme);

        Log.d(TAG, "applySettings: selectedTheme=" + selectedTheme);
        editor.putInt(getString(R.string.sp_theme), selectedTheme);
        editor.apply();

        Log.d(TAG, "applySettings: prominentAllowed=" + prominentAllowed);
        editor.putBoolean(getResources().getString(R.string.sp_allow_prominent), prominentAllowed);
        editor.apply();

        Log.d(TAG, "applySettings: margin=" + margin);
        editor.putInt(getResources().getString(R.string.sp_distance_margin), margin);
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

    private void setMarginValue(int margin){
        txtvwMarginValue.setText(String.format(Locale.US, "%2d%%", margin));
    }

    private int getMarginFromSeekBarValue(){
        return sbrDistanceMargin.getProgress() * MARGIN_MULTIPLIER;
    }

    private int getSeekBarValueFromMargin(int margin){
        return margin / MARGIN_MULTIPLIER;
    }
}
