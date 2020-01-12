package me.nakukibo.projectfoodpicker;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends ThemedAppCompatActivity {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private List<Restaurant> previouslyAccessed;

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
        Gson gson = new Gson();
        Set jsonSet = getApplicationSharedPreferences().getStringSet(getString(R.string.sp_previously_accessed_json), null);

        ArrayList<String> jsonList = new ArrayList<String>(jsonSet);

        if(jsonSet != null){
            for(int i = 0; i < jsonList.size(); i++) {
                restaurantList.add(gson.fromJson(jsonList.get(i), Restaurant.class));
            }
        }

        Log.d(TAG, "getPreviouslyAccessed: " + restaurantList);

        return restaurantList;
    }
}

