package com.vadim.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vadim.adapter.FavouriteQrDataListRecyclerAdapter;
import com.vadim.model.QrData;
import com.vadim.presenter.FavouriteQrDataListPresenter;
import com.vadim.qr_code.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteQrDataListFragment extends Fragment
        implements FavouriteQrDataListPresenter.FavouriteQrDataListView {
    private static final String TAG = SearcherQrDataListFragment.class.getSimpleName();

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.swipeFavouriteContainer)
    public SwipeRefreshLayout swipeFavouriteContainer;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progressBarFavouriteQrDataList)
    ProgressBar progressBarFavouriteQrDataList;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.favouriteQrDataRecyclerView)
    RecyclerView favouriteQrDataRecyclerView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.emptyFavouriteQrDataText)
    TextView emptyFavouriteQrDataText;

    private Context context;
    private FavouriteQrDataListPresenter favouriteQrDataListPresenter;
    private FavouriteQrDataListRecyclerAdapter favouriteQrDataListRecyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_qr_data_list, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        setQrDataRecyclerView(new ArrayList<>());
        addDividerForRecyclerView();
        addSwipeRefresherListener();
        return view;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            progressBarFavouriteQrDataList.setVisibility(View.VISIBLE);
            favouriteQrDataListPresenter = new FavouriteQrDataListPresenter(this, context);
            favouriteQrDataListPresenter.fetchFavouriteQrDataList();
        } else {
            Boolean loading = (Boolean) savedInstanceState.get("loading");
            if (loading != null && loading) {
                progressBarFavouriteQrDataList.setVisibility(View.VISIBLE);
            } else if (!swipeFavouriteContainer.isRefreshing()) {
                QrData[] qrDataArray = (QrData[]) savedInstanceState.get("qrDataArray");
                displayFavouriteQrDataList(Arrays.asList(qrDataArray));
            }
        }
    }

    private void addDividerForRecyclerView() {
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
        favouriteQrDataRecyclerView.addItemDecoration(itemDecorator);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener() {
        swipeFavouriteContainer.setOnRefreshListener(() -> {
            if (progressBarFavouriteQrDataList.getVisibility() == View.VISIBLE) {
                swipeFavouriteContainer.setRefreshing(false);
            } else {
                favouriteQrDataListPresenter.fetchFavouriteQrDataList();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarFavouriteQrDataList.getVisibility() == View.VISIBLE) {
            outState.putBoolean("loading", true);
        }
        outState.putParcelableArray("qrDataArray", favouriteQrDataListRecyclerAdapter.getFavouriteQrDataArray());
        favouriteQrDataListPresenter.fetchFavouriteQrDataList().dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        favouriteQrDataListPresenter.detachView();
    }

    private void setQrDataRecyclerView(List<QrData> qrDataList) {
        favouriteQrDataRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        favouriteQrDataListRecyclerAdapter = new FavouriteQrDataListRecyclerAdapter(this, context, qrDataList);
        favouriteQrDataRecyclerView.setAdapter(favouriteQrDataListRecyclerAdapter);
    }

    private void stopAllLoaders() {
        progressBarFavouriteQrDataList.setVisibility(View.GONE);
        swipeFavouriteContainer.setRefreshing(false);
    }

    @Override
    public void displayFavouriteQrDataList(List<QrData> qrDataList) {
        if (qrDataList.isEmpty()) {
            displayEmptyListView();
        } else {
            setQrDataRecyclerView(qrDataList);
        }
        stopAllLoaders();
    }

    @Override
    public void filter(String text, Filter.FilterListener filterListener) {
        favouriteQrDataListRecyclerAdapter.getFilter().filter(text, filterListener);
    }

    @Override
    public void displayEmptyListView() {
        favouriteQrDataRecyclerView.setVisibility(View.GONE);
        emptyFavouriteQrDataText.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayListView() {
        favouriteQrDataRecyclerView.setVisibility(View.VISIBLE);
        emptyFavouriteQrDataText.setVisibility(View.GONE);
    }

}