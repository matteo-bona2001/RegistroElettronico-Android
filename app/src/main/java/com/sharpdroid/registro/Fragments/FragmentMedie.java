package com.sharpdroid.registro.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharpdroid.registro.API.SpiaggiariApiClient;
import com.sharpdroid.registro.Adapters.MedieAdapter;
import com.sharpdroid.registro.Databases.SubjectsDB;
import com.sharpdroid.registro.Interfaces.API.MarkSubject;
import com.sharpdroid.registro.R;
import com.sharpdroid.registro.Tasks.CacheListTask;
import com.sharpdroid.registro.Utils.ItemOffsetDecoration;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.sharpdroid.registro.Utils.Metodi.getOverallAverage;
import static com.sharpdroid.registro.Utils.Metodi.isNetworkAvailable;

public class FragmentMedie extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final private String TAG = FragmentMedie.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    SubjectsDB subjectsDB;
    Snackbar snackbar;
    private MedieAdapter mRVAdapter;
    private Context mContext;

    public FragmentMedie() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();
        View layout = inflater.inflate(R.layout.coordinator_swipe_recycler, container, false);

        ButterKnife.bind(this, layout);

        subjectsDB = SubjectsDB.from(mContext);

        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.bluematerial,
                R.color.redmaterial,
                R.color.greenmaterial,
                R.color.orangematerial);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        mRecyclerView.addItemDecoration(new ItemOffsetDecoration(mContext, R.dimen.cards_margin));

        mRVAdapter = new MedieAdapter(mContext, new CopyOnWriteArrayList<>(), subjectsDB);
        mRecyclerView.setAdapter(mRVAdapter);

        UpdateMedie();

        return layout;
    }

    @Override
    public void onResume() {
        bindMarksCache();
        super.onResume();
    }

    private void addSubjects(List<MarkSubject> markSubjects, boolean docache) {
        if (!markSubjects.isEmpty()) {
            mRVAdapter.clear();
            mRVAdapter.addAll(markSubjects);


            if (docache) {
                // Update cache
                new CacheListTask(mContext.getCacheDir(), TAG).execute((List) markSubjects);
            }
        }
    }

    private void bindMarksCache() {
        ObjectInputStream objectInputStream = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(mContext.getCacheDir(), TAG));
            objectInputStream = new ObjectInputStream(fileInputStream);
            List<MarkSubject> cachedData = new LinkedList<>();
            MarkSubject temp;
            while ((temp = (MarkSubject) objectInputStream.readObject()) != null) {
                cachedData.add(temp);
            }
            addSubjects(cachedData, false);
            Log.d(TAG, "Restored cache");
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Cache not found.");
        } catch (EOFException e) {
            Log.e(TAG, "Error while reading cache! (EOF) ");
        } catch (StreamCorruptedException e) {
            Log.e(TAG, "Corrupted cache!");
        } catch (IOException e) {
            Log.e(TAG, "Error while reading cache!");
        } catch (ClassCastException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onRefresh() {
        UpdateMedie();
    }

    private void UpdateMedie() {
        mSwipeRefreshLayout.setRefreshing(true);
        new SpiaggiariApiClient(mContext).getMarks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(marks -> {
                    addSubjects(marks, true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    snackbar = Snackbar.make(mCoordinatorLayout, "Media totale: " + String.format(Locale.getDefault(), "%.2f", getOverallAverage(marks)), Snackbar.LENGTH_LONG);
                    snackbar.show();
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
        subjectsDB.close();
    }
}
