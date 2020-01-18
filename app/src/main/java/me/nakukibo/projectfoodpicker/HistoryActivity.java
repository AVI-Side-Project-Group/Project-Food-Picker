package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryActivity extends ThemedAppCompatActivity {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private List<Restaurant> previouslyAccessed;

    private PopupWindow popupWindow;
    private int currentImage = 0;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        previouslyAccessed = getHistory();
        initRecycler();
    }

    private void initRecycler(){
        recyclerView = findViewById(R.id.recycle_history);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RestaurantAdapter(previouslyAccessed);
        recyclerView.setAdapter(adapter);
    }

    private List<Restaurant> getHistory(){
        List<Restaurant> restaurantList = new ArrayList<>();
        Set jsonSet = getApplicationSharedPreferences().getStringSet(getString(R.string.sp_previously_accessed_json), null);

        if(jsonSet != null){
            ArrayList<String> jsonList = new ArrayList<String>(jsonSet);
            for(int i = 0; i < jsonList.size(); i++) {
                Restaurant restaurant = new Restaurant(jsonList.get(i));
                restaurantList.add(restaurant);
                Log.d(TAG, "getHistory: " + restaurantList.get(i).getName());
            }
        }

        Log.d(TAG, "getHistory: " + restaurantList);

        return restaurantList;
    }

    public void getInfo(View view){
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.history_card,null);
        popupWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        CardView cardView = layout.findViewById(R.id.his_cardView);

        if(Build.VERSION.SDK_INT >= 21){
            popupWindow.setElevation(5.0f);
        }
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

        setValues(view, layout);
        TypedArray typedArray = this.getTheme().obtainStyledAttributes(R.styleable.ViewStyle);
        cardView.setCardBackgroundColor(typedArray.getColor(R.styleable.ViewStyle_colorPrimary, Color.BLACK));
    }

    public void closePopUpWindow(View view){
        popupWindow.dismiss();
    }

    private void setValues(View view, View card){
        Restaurant restaurant = previouslyAccessed.get(recyclerView.getChildLayoutPosition(view));


        Log.d(TAG, "setValues: " + restaurant.getJsonFromRestaurant());

        Log.d(TAG, "setValues: " + restaurant.getPhotos());

        RestaurantCard restaurantCard = card.findViewById(R.id.history_restcard);
        restaurantCard.setValues(restaurant);
        restaurantCard.setPopupMode(true);
    }
}

