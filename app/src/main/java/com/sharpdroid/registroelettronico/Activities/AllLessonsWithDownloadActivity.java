package com.sharpdroid.registroelettronico.Activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sharpdroid.registroelettronico.API.SpiaggiariApiClient;
import com.sharpdroid.registroelettronico.Adapters.AllLessonsAdapter;
import com.sharpdroid.registroelettronico.Databases.RegistroDB;
import com.sharpdroid.registroelettronico.Interfaces.API.Lesson;
import com.sharpdroid.registroelettronico.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.sharpdroid.registroelettronico.Utils.Metodi.getProfessorOfThisSubject;
import static com.sharpdroid.registroelettronico.Utils.Metodi.getSubjectName;
import static com.sharpdroid.registroelettronico.Utils.Metodi.isNetworkAvailable;

public class AllLessonsWithDownloadActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = "Lessons";
    static int code;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    AllLessonsAdapter mRVAdapter;

    RegistroDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_refresh_scrollbar);
        ButterKnife.bind(this);

        code = getIntent().getIntExtra("code", -1);
        db = RegistroDB.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(getSubjectName(db.getSubject(code)));

        mRVAdapter = new AllLessonsAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRVAdapter);

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.bluematerial,
                R.color.redmaterial,
                R.color.greenmaterial,
                R.color.orangematerial);
        bindLessonsCache();

        UpdateLessons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    private void addLessons(List<Lesson> lessons) {
        if (!lessons.isEmpty()) {
            mRVAdapter.clear();
            mRVAdapter.addAll(lessons);
        }
    }

    private void bindLessonsCache() {
        addLessons(db.getLessons(code));
    }

    public void onRefresh() {
        UpdateLessons();
    }

    private void UpdateLessons() {
        if (!isNetworkAvailable(this)) {
            Snackbar.make(mCoordinatorLayout, R.string.nointernet, Snackbar.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            for (Integer prof : db.getProfessorCodes(code)) {
                new SpiaggiariApiClient(this)
                        .getLessons(code, String.valueOf(prof))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(lessons -> {
                            //update db
                            db.removeLessons(code);
                            db.addLessons(code, prof, lessons);
                            db.addProfessor(code, prof, getProfessorOfThisSubject(lessons));
                            bindLessonsCache();

                            mSwipeRefreshLayout.setRefreshing(false);
                        }, error -> {
                            error.printStackTrace();
                            mSwipeRefreshLayout.setRefreshing(false);
                        });
            }
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
