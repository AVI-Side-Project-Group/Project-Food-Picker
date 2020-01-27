package me.nakukibo.projectfoodpicker;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends CustomAppCompatActivity {

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
        String jsonArrayStr = getApplicationSharedPreferences().getString(getString(R.string.sp_previously_accessed_json), null);

        Log.d(TAG, "getHistory: restoring jsonArray=" + jsonArrayStr);

        if(jsonArrayStr != null){
            JSONArray jsonArray = null;

            try {
                JSONObject jsonObject = new JSONObject(jsonArrayStr);
                jsonArray = jsonObject.getJSONArray(getString(R.string.sp_previously_accessed_json));
            } catch(JSONException e){
                e.printStackTrace();
            }

            for(int i = 0; i < jsonArray.length(); i++) {
                try {
                    Restaurant restaurant = new Restaurant(jsonArray.getString(i));
                    restaurantList.add(0, restaurant);
                    Log.d(TAG, "getHistory: " + restaurantList.get(i).getName());
                } catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }

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

