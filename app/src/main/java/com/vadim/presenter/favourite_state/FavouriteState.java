package com.vadim.presenter.favourite_state;

import com.vadim.model.QrData;
import com.vadim.presenter.QrCodeElementPresenter;

public interface FavouriteState {
    boolean addOrDeleteQrDataJson(QrCodeElementPresenter qrCodeElementPresenter, QrData qrData);
}
