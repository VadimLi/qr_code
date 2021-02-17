package com.vadim.qr_code;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FavouriteQrDataListPresenterTest {

    @Test
    public void readPdfFile() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.vadim.qr_code", appContext.getPackageName());
    }

}
