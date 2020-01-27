package me.nakukibo.projectfoodpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class DefaultLoadingScreen extends ConstraintLayout {


    public DefaultLoadingScreen(Context context) {
        this(context, null);
    }

    public DefaultLoadingScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultLoadingScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.loading_screen, this);
    }

    public void setLoadingText(String loadingText){
        final TextView TXTVW_LOADING_TEXT = findViewById(R.id.loading_txt);
        TXTVW_LOADING_TEXT.setText(loadingText);
    }
}
