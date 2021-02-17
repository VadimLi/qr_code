package com.vadim.presenter.favourite_state;

import com.vadim.model.QrData;
import com.vadim.presenter.QrCodeElementPresenter;
import com.vadim.presenter.json.JsonUtil;

import java.util.List;

public class DeletingFavouriteState implements FavouriteState {
    private final JsonUtil jsonUtil;

    public DeletingFavouriteState(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public boolean addOrDeleteQrDataJson(QrCodeElementPresenter qrCodeElementPresenter, QrData qrData) {
        List<QrData> qrDataList = jsonUtil.readJsonListFile(JsonUtil.NAME_OF_FAVOURITE_FILE);
        if (!qrDataList.contains(qrData)) {
            qrCodeElementPresenter.setFavouriteState(new AddingFavouriteState(jsonUtil));
            return qrCodeElementPresenter.addOrDeleteQrDataJson(qrData);
        }
        qrDataList.remove(qrData);
        jsonUtil.writeJsonToListFile(qrDataList, JsonUtil.NAME_OF_FAVOURITE_FILE);
        return false;
    }

}
