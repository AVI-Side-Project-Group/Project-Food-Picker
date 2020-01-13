package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends ThemedAppCompatActivity {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private List<Restaurant> previouslyAccessed;

    private PopupWindow popupWindow;

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
        View historyCardView = inflater.inflate(R.layout.history_card,null);
        popupWindow = new PopupWindow(historyCardView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);
    }

    public void closePopUpWindown(View view){
        popupWindow.dismiss();
    }
}

