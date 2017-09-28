package com.sharpdroid.registroelettronico.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharpdroid.registroelettronico.API.V1.SpiaggiariApiClient;
import com.sharpdroid.registroelettronico.Adapters.FolderAdapter;
import com.sharpdroid.registroelettronico.Databases.RegistroDB;
import com.sharpdroid.registroelettronico.Interfaces.API.FileTeacher;
import com.sharpdroid.registroelettronico.Interfaces.API.Folder;
import com.sharpdroid.registroelettronico.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.sharpdroid.registroelettronico.Utils.Metodi.isNetworkAvailable;

public class FragmentFolders extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FolderAdapter.Listener {
    final private String TAG = FragmentFolders.class.getSimpleName();
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    Context mContext;
    FolderAdapter mRVAdapter;
    ActionBar supportActionBar;
    RegistroDB db;

    public FragmentFolders() {
    }

    public void getInstance(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        return inflater.inflate(R.layout.coordinator_swipe_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.bluematerial,
                R.color.redmaterial,
                R.color.greenmaterial,
                R.color.orangematerial);

        db = RegistroDB.getInstance(getContext());

        getActivity().setTitle(getString(R.string.files));
        RecyclerView mRecyclerView = view.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(null);

        mRVAdapter = new FolderAdapter(this);
        mRecyclerView.setAdapter(mRVAdapter);

        load();
        update();

    }

    private void addFiles(List<FileTeacher> result, boolean docache) {
        if (!result.isEmpty()) {
            mRVAdapter.setFileTeachers(result);

            if (docache) {
                // Update cache
                db.addFileTeachers(result);
            }
        }
    }

    public void onRefresh() {
        update();
    }

    private void load() {
        addFiles(db.getFileTeachers(), false);
    }

    private void save(List<FileTeacher> files) {
        db.addFileTeachers(files);
    }

    private void update() {
        mSwipeRefreshLayout.setRefreshing(true);
        new SpiaggiariApiClient(mContext)
                .getFiles()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(files -> {
                    save(files);
                    load();
                    mSwipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    error.printStackTrace();
                    if (!isNetworkAvailable(mContext)) {
                        Snackbar.make(mCoordinatorLayout, R.string.nointernet, Snackbar.LENGTH_LONG).show();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                });
    }

    @Override
    public void onFolderClick(Folder f) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)/*setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)*/.replace(R.id.fragment_container, FragmentFiles.newInstance(f)).addToBackStack(null);
        transaction.commit();
    }
}
