package com.vadim.presenter.favourite_state;

import com.vadim.model.QrData;
import com.vadim.presenter.QrCodeElementPresenter;
import com.vadim.presenter.json.JsonUtil;

import java.util.Collections;
import java.util.List;

public class AddingFavouriteState implements FavouriteState {
    private final JsonUtil jsonUtil;

    public AddingFavouriteState(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public boolean addOrDeleteQrDataJson(QrCodeElementPresenter qrCodeElementPresenter, QrData qrData) {
        List<QrData> qrDataList = jsonUtil.readJsonListFile(JsonUtil.NAME_OF_FAVOURITE_FILE);
        if (qrDataList == null) {
            qrDataList = Collections.singletonList(qrData);
        } else if (qrDataList.contains(qrData)) {
            qrCodeElementPresenter.setFavouriteState(new DeletingFavouriteState(jsonUtil));
            return qrCodeElementPresenter.addOrDeleteQrDataJson(qrData);
        }
        qrDataList.add(qrData);
        jsonUtil.writeJsonToListFile(qrDataList, JsonUtil.NAME_OF_FAVOURITE_FILE);
        return true;
    }

}
