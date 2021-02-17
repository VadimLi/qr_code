package com.vadim.presenter;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class GalleryQrDataPresenter implements PresenterView {
    private static final String TAG = GalleryQrDataPresenter.class.getSimpleName();

    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;

    private GalleryView galleryView;

    public GalleryQrDataPresenter(GalleryView galleryView) {
        this.galleryView = galleryView;
    }

    public void generateQrCodeFromContent(String content) {
        Bitmap photoOfQrCode = encodeAsBitmap(content);
        galleryView.displayQrData(photoOfQrCode);
    }

    private Bitmap encodeAsBitmap(String content) {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = fillPixes(width, height, result);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, width, height);
        return bitmap;
    }

    private int[] fillPixes(int width, int height, BitMatrix result) {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        return pixels;
    }

    @Override
    public void detachView() {
        galleryView = null;
    }

    public interface GalleryView {
        void displayQrData(Bitmap photoOfQrCode);
    }

}
