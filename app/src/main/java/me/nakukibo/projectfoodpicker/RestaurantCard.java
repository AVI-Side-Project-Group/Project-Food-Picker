package me.nakukibo.projectfoodpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class RestaurantCard extends CardView {

    public RestaurantCard(@NonNull Context context) {
        this(context, null);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitializeCard(context, attrs);
    }

    private void InitializeCard(@NonNull Context context, @Nullable AttributeSet attrs){
        inflate(context, R.layout.restaurant_card, this);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RestaurantCard);
        attributes.recycle();
    }
}
