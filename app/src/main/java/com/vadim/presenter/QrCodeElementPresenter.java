package com.vadim.presenter;

import android.content.Context;

import com.vadim.model.QrData;
import com.vadim.presenter.favourite_state.AddingFavouriteState;
import com.vadim.presenter.favourite_state.FavouriteState;
import com.vadim.presenter.json.JsonUtil;
import com.vadim.ui.UtilsUI;

import java.util.ArrayList;
import java.util.List;

public class QrCodeElementPresenter {
    private static final String TAG = QrCodeElementPresenter.class.getSimpleName();

    private QrDataElementViewHolder qrDataElementViewHolder;
    private QrDataElementFromFavouriteViewHolder qrCodeElementFromFavouriteView;
    private UtilsUI.UtilsFragment utilsFragment;
    private FavouriteState favouriteState;

    private final JsonUtil jsonUtil;
    private List<QrData> qrDataList;

    public QrCodeElementPresenter(final Context context) {
        jsonUtil = new JsonUtil(context);
        if (qrDataList == null) {
            qrDataList = new ArrayList<>();
        }
        favouriteState = new AddingFavouriteState(jsonUtil);
    }

    public void attachSearcherView(QrDataElementViewHolder qrDataElementViewHolder) {
        this.qrDataElementViewHolder = qrDataElementViewHolder;
    }

    public void attachFavouriteView(QrDataElementFromFavouriteViewHolder qrCodeElementFromFavouriteView) {
        this.qrCodeElementFromFavouriteView = qrCodeElementFromFavouriteView;
    }

    public void attachFavouriteFragment(UtilsUI.UtilsFragment utilsFragment) {
        this.utilsFragment = utilsFragment;
    }

    public void setFavouriteState(FavouriteState favouriteState) {
        this.favouriteState = favouriteState;
    }

    public boolean getStateFavouriteIcon(QrData qrData) {
        qrDataList = jsonUtil.readJsonListFile(JsonUtil.NAME_OF_FAVOURITE_FILE);
        return qrDataList.contains(qrData);
    }

    public boolean addOrDeleteQrDataJson(QrData qrData) {
        return favouriteState.addOrDeleteQrDataJson(this, qrData);
    }

    public void changeTextOfSearcherName(QrData qrData) {
        changeTextOfNameForConcreteFile(JsonUtil.NAME_OF_FAVOURITE_FILE, qrData);
        changeTextOfNameForConcreteFile(JsonUtil.NAME_OF_QR_DATA_FILE, qrData);
        qrDataElementViewHolder.notifyQrData(qrDataList);
    }

    public void changeTextOfFavouriteName(QrData qrData) {
        changeTextOfNameForConcreteFile(JsonUtil.NAME_OF_QR_DATA_FILE, qrData);
        changeTextOfNameForConcreteFile(JsonUtil.NAME_OF_FAVOURITE_FILE, qrData);
        qrCodeElementFromFavouriteView.notifyQrData(qrDataList);
    }

    private void changeTextOfNameForConcreteFile(String fileName, QrData qrData) {
        this.qrDataList.clear();
        List<QrData> qrDataList = jsonUtil.readJsonListFile(fileName);
        for (QrData data : qrDataList) {
            if (qrData.equals(data)) {
                data.setName(qrData.getName());
            }
            this.qrDataList.add(data);
        }
        jsonUtil.writeJsonToListFile(this.qrDataList, fileName);
    }

    public void deleteQrCodeElementFromFavouriteView(QrData qrData) {
        qrDataList = jsonUtil.readJsonListFile(JsonUtil.NAME_OF_FAVOURITE_FILE);
        qrDataList.remove(qrData);
        jsonUtil.writeJsonToListFile(qrDataList, JsonUtil.NAME_OF_FAVOURITE_FILE);
        if (qrDataList.isEmpty()) {
            utilsFragment.displayEmptyListView();
        }
        qrCodeElementFromFavouriteView.deleteElement(qrData);
    }

    public interface QrDataElementViewHolder {
        void notifyQrData(List<QrData> qrDataList);
    }

    public interface QrDataElementFromFavouriteViewHolder extends QrDataElementViewHolder{
        void deleteElement(QrData qrData);
    }

}
