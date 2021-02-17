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

import com.vadim.adapter.SearcherQrDataListRecyclerAdapter;
import com.vadim.model.QrData;
import com.vadim.presenter.SearcherQrDataListPresenter;
import com.vadim.qr_code.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearcherQrDataListFragment extends Fragment
        implements SearcherQrDataListPresenter.SearcherQrDataListView {
    private static final String TAG = SearcherQrDataListFragment.class.getSimpleName();

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.swipeSearcherContainer)
    public SwipeRefreshLayout swipeSearcherContainer;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progressBarSearcherQrDataList)
    ProgressBar progressBarSearcherQrDataList;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.searcherOfQrDataRecyclerView)
    RecyclerView searcherOfQrDataRecyclerView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.emptySearcherQrDataText)
    TextView emptySearcherQrDataText;

    private Context context;
    private SearcherQrDataListPresenter searcherQrDataListPresenter;
    private SearcherQrDataListRecyclerAdapter searcherQrDataListRecyclerAdapter;

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
        View view = inflater.inflate(R.layout.fragment_searcher_qr_data_list, container, false);
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
            progressBarSearcherQrDataList.setVisibility(View.VISIBLE);
            searcherQrDataListPresenter = new SearcherQrDataListPresenter(this, context);
            searcherQrDataListPresenter.searchQrDataList();
        } else {
            Boolean loading = (Boolean) savedInstanceState.get("loading");
            if (loading != null && loading) {
                progressBarSearcherQrDataList.setVisibility(View.VISIBLE);
            } else if (!swipeSearcherContainer.isRefreshing()) {
                QrData[] qrDataArray = (QrData[]) savedInstanceState.get("qrDataArray");
                displaySearcherQrDataList(Arrays.asList(qrDataArray));
            }
        }
    }

    private void addDividerForRecyclerView() {
        DividerItemDecoration itemDecorator = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.divider)));
        searcherOfQrDataRecyclerView.addItemDecoration(itemDecorator);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener() {
        swipeSearcherContainer.setOnRefreshListener(() -> {
            if (progressBarSearcherQrDataList.getVisibility() == View.VISIBLE) {
                swipeSearcherContainer.setRefreshing(false);
            } else {
                searcherQrDataListPresenter.searchQrDataList();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarSearcherQrDataList.getVisibility() == View.VISIBLE) {
            outState.putBoolean("loading", true);
        }
        outState.putParcelableArray("qrDataArray", searcherQrDataListRecyclerAdapter.getSearcherQrDataArray());
        searcherQrDataListPresenter.searchQrDataList().dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searcherQrDataListPresenter.detachView();
    }

    @Override
    public void displaySearcherQrDataList(List<QrData> qrDataList) {
        if (qrDataList.isEmpty()) {
            displayEmptyListView();
        } else {
            setQrDataRecyclerView(qrDataList);
        }
        stopAllLoaders();
    }

    private void setQrDataRecyclerView(List<QrData> qrDataList) {
        searcherOfQrDataRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        searcherQrDataListRecyclerAdapter = new SearcherQrDataListRecyclerAdapter(context, qrDataList);
        searcherOfQrDataRecyclerView.setAdapter(searcherQrDataListRecyclerAdapter);
    }

    private void stopAllLoaders() {
        progressBarSearcherQrDataList.setVisibility(View.GONE);
        swipeSearcherContainer.setRefreshing(false);
    }

    @Override
    public void filter(String text, Filter.FilterListener filterListener) {
        searcherQrDataListRecyclerAdapter.getFilter().filter(text, filterListener);
    }

    @Override
    public void displayEmptyListView() {
        searcherOfQrDataRecyclerView.setVisibility(View.GONE);
        emptySearcherQrDataText.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayListView() {
        searcherOfQrDataRecyclerView.setVisibility(View.VISIBLE);
        emptySearcherQrDataText.setVisibility(View.GONE);
    }

}