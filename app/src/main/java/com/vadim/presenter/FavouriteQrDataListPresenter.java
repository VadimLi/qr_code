package com.vadim.presenter;

import android.content.Context;
import android.util.Log;

import com.vadim.model.QrData;
import com.vadim.presenter.json.JsonUtil;
import com.vadim.ui.UtilsUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FavouriteQrDataListPresenter implements PresenterView {
    private static final String TAG = FavouriteQrDataListPresenter.class.getSimpleName();

    private FavouriteQrDataListView favouriteQrDataListView;
    private final Context context;
    private final List<QrData> qrDataList = new ArrayList<>();

    public FavouriteQrDataListPresenter(final FavouriteQrDataListView favouriteQrDataListView, Context context) {
        this.favouriteQrDataListView = favouriteQrDataListView;
        this.context = context;
    }

    public Disposable fetchFavouriteQrDataList() {
        JsonUtil jsonUtil = new JsonUtil(context);
        return Observable.create((ObservableOnSubscribe<QrData>) emitter -> {
            qrDataList.clear();
            List<QrData> jsonQrDataList = jsonUtil.readJsonListFile(JsonUtil.NAME_OF_FAVOURITE_FILE);
            for (QrData qrData : jsonQrDataList) {
               emitter.onNext(qrData);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(qrData -> {
                    File file = new File(qrData.getOriginalPhoto());
                    return file.exists();
                })
                .doOnNext(qrDataList::add)
                .doOnComplete(() -> {
                    Log.d(TAG, "Finished thread");
                    jsonUtil.writeJsonToListFile(qrDataList, JsonUtil.NAME_OF_FAVOURITE_FILE);
                    if (favouriteQrDataListView != null) {
                        favouriteQrDataListView.displayFavouriteQrDataList(qrDataList);
                    }
                }).subscribe();
    }

    @Override
    public void detachView() {
        favouriteQrDataListView = null;
    }

    public interface FavouriteQrDataListView extends UtilsUI.UtilsFragment {
        void displayFavouriteQrDataList(List<QrData> qrDataList);
    }

}
