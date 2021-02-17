package com.vadim.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.vadim.model.QrData;
import com.vadim.presenter.json.JsonUtil;
import com.vadim.ui.UtilsUI;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearcherQrDataListPresenter implements PresenterView {
    private static final String TAG = SearcherQrDataListPresenter.class.getSimpleName();

    private final List<Path> paths = new ArrayList<>();
    private final List<QrData> qrDataList = new ArrayList<>();
    private final Context context;
    private final UtilsUI utilsUI;
    private final JsonUtil jsonUtil;
    private SearcherQrDataListView searcherQrDataListView;
    private BarcodeDetector qrCodeDetector;

    public SearcherQrDataListPresenter(final SearcherQrDataListView searcherQrDataListView,
                                       final Context context) {
        this.searcherQrDataListView = searcherQrDataListView;
        this.context = context;
        utilsUI = new UtilsUI(context);
        jsonUtil = new JsonUtil(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Disposable searchQrDataList() {
        int threadCt = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCt);
        qrCodeDetector = new BarcodeDetector.Builder(context)
                            .setBarcodeFormats(Barcode.QR_CODE)
                            .build();
        addToQrDataList();
        return Observable.create((ObservableOnSubscribe<Path>) emitter -> {
            List<Path> photoLinkedQueue = findPhotoList();
            for (Path path : photoLinkedQueue) {
                emitter.onNext(path);
            }
            emitter.onComplete();
        }).flatMap(path -> Observable.just(path)
                .doOnNext(this::addToQrDataList)
                .subscribeOn(Schedulers.from(executor)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    Log.d(TAG, "Finished");
                    List<QrData> responseQrDataList = findSortedList();
                    savePhotoList(responseQrDataList);
                    jsonUtil.writeJsonToListFile(responseQrDataList, JsonUtil.NAME_OF_QR_DATA_FILE);
                    if (searcherQrDataListView != null) {
                        searcherQrDataListView.displaySearcherQrDataList(responseQrDataList);
                    }
                }).subscribe();
    }

    private void addToQrDataList() {
        qrDataList.clear();
        jsonUtil.readJsonListFile(JsonUtil.NAME_OF_QR_DATA_FILE)
                .stream()
                .filter(qrData -> {
                    File file = new File(qrData.getOriginalPhoto());
                    return file.exists();
                })
                .forEach(qrDataList::add);
    }

    private List<QrData> findSortedList() {
        qrDataList.sort(Collections.reverseOrder());
        Set<QrData> qrDataSet = new HashSet<>(qrDataList);
        return new ArrayList<>(qrDataSet);
    }

    private void savePhotoList(List<QrData> qrDataList) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean loadingPhotos = prefs.getBoolean("load_photos", false);
        if (loadingPhotos) {
            File qrFileDirectory = findFileDirectory();
            for (QrData qrData : qrDataList) {
                File inputFile = new File(qrData.getOriginalPhoto());
                File savedFile = new File(qrFileDirectory, inputFile.getName());
                savePhoto(inputFile, savedFile);
            }
        }
    }

    private File findFileDirectory() {
        File qrListDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QrList");
        if (!qrListDirectory.exists()) {
            qrListDirectory.mkdirs();
        }
        return qrListDirectory;
    }

    private void savePhoto(File inputFile, File savedFile) {
        if (!inputFile.equals(savedFile)) {
            try {
                FileOutputStream out = new FileOutputStream(savedFile);
                Bitmap photo = utilsUI.convertFileToBitmap(inputFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<Path> findPhotoList() {
        paths.clear();
        Map<String, String> indexDirs = jsonUtil.readJsonMapFile(JsonUtil.NAME_OF_INDEX_FILE);
        for (Directory directory : Directory.values()) {
            File dir = directory.getDirectory();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String lastModifiedDate = sdf.format(dir.lastModified());
            String jsonLastModifiedDate = indexDirs.get(dir.getName());
            if (!lastModifiedDate.equals(jsonLastModifiedDate)) {
                indexDirs.put(dir.getName(), lastModifiedDate);
                searchFiles(directory.getDirectory());
            }
        }
        jsonUtil.writeJsonToMapFile(indexDirs, JsonUtil.NAME_OF_INDEX_FILE);
        return paths;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void searchFiles(File rootFile) {
        try {
            Files.walk(Paths.get(rootFile.getAbsolutePath()))
                    .parallel()
                    .filter(Files::isRegularFile)
                    .filter(this::isFormatByExtension)
                    .forEach(paths::add);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addToQrDataList(Path path) {
        final File file = path.toFile();
        Bitmap bitmap = utilsUI.convertFileToBitmap(file);
        if (qrCodeDetector.isOperational() && bitmap != null) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Barcode> barcodes = qrCodeDetector.detect(frame);
            setQrDataForEachImage(barcodes, file);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setQrDataForEachImage(SparseArray<Barcode> barcodes, File file) {
        if (barcodes.size() != 0) {
            final QrData qrData = new QrData();
            for (int index = 0; index < barcodes.size(); index++) {
                addToQrData(barcodes, qrData, index, file);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addToQrData(SparseArray<Barcode> barcodes,
                             QrData qrData, int index, File file) {
        Barcode code = barcodes.valueAt(index);
        final String photoDate = findPhotoDate(file);
        qrData.setName(code.displayValue);
        qrData.setDate(photoDate);
        qrData.setContent(code.displayValue);
        qrData.setOriginalPhoto(file.getAbsolutePath());
        qrDataList.add(qrData);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String findPhotoDate(File file) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return sdf.format(file.lastModified());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isFormatByExtension(Path path) {
        File file = path.toFile();
        String fileExtension = FilenameUtils.getExtension(file.getName());
        return fileExtension.equals(FormatListing.JPG.getFormat())
                || fileExtension.equals(FormatListing.PNG.getFormat());
    }

    @Override
    public void detachView() {
        searcherQrDataListView = null;
    }

    public interface SearcherQrDataListView extends UtilsUI.UtilsFragment {
        void displaySearcherQrDataList(List<QrData> qrDataList);
    }

}
