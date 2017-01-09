package com.sharpdroid.registro.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharpdroid.registro.Adapters.AllLessonsAdapter;
import com.sharpdroid.registro.Interfaces.API.Lesson;
import com.sharpdroid.registro.Listeners.OnScrollLessonsListener;
import com.sharpdroid.registro.R;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllLessonsActivity extends AppCompatActivity {
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    AllLessonsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_lessons);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Lezioni");

        ArrayList<Lesson> lessons = null;
        try {
            lessons = new Gson().getAdapter(new TypeToken<ArrayList<Lesson>>() {
            }).fromJson(getIntent().getStringExtra("data"));
        } catch (IOException e) {
            Toast.makeText(this, "Errore", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        adapter = new AllLessonsAdapter(this);
        adapter.addAll(lessons);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.addOnScrollListener(new OnScrollLessonsListener());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
