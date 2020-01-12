package me.nakukibo.projectfoodpicker;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends ThemedAppCompatActivity {

    private List<HashMap<String, String>> previouslyAccessed;

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

    private List<HashMap<String, String>> getHistory(){
        Set tempSet = getApplicationSharedPreferences().getStringSet(getString(R.string.sp_previously_accessed), null);
        List<HashMap<String, String>> tempList;
        if (tempSet == null) {
            tempList = new ArrayList<>();
        } else {
            tempList = new ArrayList<>(tempSet);
        }

        return tempList;
    }
}

