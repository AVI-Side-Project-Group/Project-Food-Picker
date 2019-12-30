package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.PurpleOrange);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    public void returnMainPage (View view) {
        Intent switchIntent = new Intent(this, MainActivity.class);
        startActivity(switchIntent);
    }
}
