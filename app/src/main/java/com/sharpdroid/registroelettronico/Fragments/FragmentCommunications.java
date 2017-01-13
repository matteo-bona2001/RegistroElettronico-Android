package com.sharpdroid.registroelettronico.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharpdroid.registroelettronico.API.SpiaggiariApiClient;
import com.sharpdroid.registroelettronico.Adapters.CommunicationAdapter;
import com.sharpdroid.registroelettronico.Databases.CommunicationsDB;
import com.sharpdroid.registroelettronico.Interfaces.API.Communication;
import com.sharpdroid.registroelettronico.R;
import com.sharpdroid.registroelettronico.Tasks.CacheListObservable;
import com.sharpdroid.registroelettronico.Tasks.CacheListTask;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.sharpdroid.registroelettronico.Utils.Metodi.isNetworkAvailable;

public class FragmentCommunications extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final private String TAG = FragmentCommunications.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private CommunicationAdapter mRVAdapter;
    private Context mContext;
    private CommunicationsDB db;

    public FragmentCommunications() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();
        View layout = inflater.inflate(R.layout.coordinator_swipe_recycler, container, false);
        db = CommunicationsDB.from(mContext);
        ButterKnife.bind(this, layout);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.bluematerial,
                R.color.redmaterial,
                R.color.greenmaterial,
                R.color.orangematerial);

        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext).marginResId(R.dimen.activity_horizontal_margin, R.dimen.activity_horizontal_margin).color(Color.parseColor("#11000000")).size(1).build());
        mRecyclerView.setItemAnimator(null);

        mRVAdapter = new CommunicationAdapter(mContext, mCoordinatorLayout, db);
        mRecyclerView.setAdapter(mRVAdapter);

        bindCommunicationsCache();
        UpdateCommunications();

        return layout;
    }

    private void addCommunications(List<Communication> communications, boolean docache) {
        if (!communications.isEmpty()) {
            mRVAdapter.clear();
            mRVAdapter.addAll(communications);

            if (docache) {
                // Update cache
                new CacheListTask(mContext.getCacheDir(), TAG).execute((List) communications);
            }
        }
    }

    private void bindCommunicationsCache() {
        new CacheListObservable(new File(mContext.getCacheDir(), TAG))
                .getCachedList(Communication.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(communications -> {
                    addCommunications(communications, false);
                    Log.d(TAG, "Restored cache");
                });
    }

    public void onRefresh() {
        UpdateCommunications();
    }

    private void UpdateCommunications() {
        mSwipeRefreshLayout.setRefreshing(true);
        new SpiaggiariApiClient(mContext)
                .getCommunications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(communications -> {
                    addCommunications(communications, true);
                    mSwipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    if (!isNetworkAvailable(mContext)) {
                        Snackbar.make(mCoordinatorLayout, R.string.nointernet, Snackbar.LENGTH_LONG).show();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }
}