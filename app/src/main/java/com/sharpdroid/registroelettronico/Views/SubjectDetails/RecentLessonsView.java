package com.sharpdroid.registroelettronico.Views.SubjectDetails;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.Button;

import com.google.gson.Gson;
import com.sharpdroid.registroelettronico.Activities.AllLessonsActivity;
import com.sharpdroid.registroelettronico.Adapters.LessonsAdapter;
import com.sharpdroid.registroelettronico.Interfaces.API.Lesson;
import com.sharpdroid.registroelettronico.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentLessonsView extends CardView {
    Context mContext;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_more)
    Button showAllButton;

    LessonsAdapter adapter;

    public RecentLessonsView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public RecentLessonsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public RecentLessonsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.view_recent_lessons, this);
        ButterKnife.bind(this);

        adapter = new LessonsAdapter(mContext);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext).marginResId(R.dimen.activity_vertical_margin, R.dimen.activity_vertical_margin).size(1).build());
        mRecyclerView.setAdapter(adapter);
    }

    public void addAll(Collection<Lesson> list) {
        adapter.addAll(list);
        showAllButton.setOnClickListener(view -> mContext.startActivity(new Intent(mContext, AllLessonsActivity.class).putExtra("data", new Gson().toJson(list))));
    }

    public void clear() {
        adapter.clear();
    }
}