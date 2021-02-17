package com.vadim.presenter.json;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vadim.model.QrData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final String TAG = JsonUtil.class.getSimpleName();
    public static final String NAME_OF_FAVOURITE_FILE = "favourite.json";
    public static final String NAME_OF_INDEX_FILE = "index.json";
    public static final String NAME_OF_QR_DATA_FILE = "qr_data.json";

    private final Context context;

    public JsonUtil(Context context) {
        this.context = context;
    }

    @SuppressLint("SetWorldWritable")
    public static void createJsonFile(Context context, String fileName) {
        File rootFolder = context.getCacheDir();
        File jsonFile = new File(rootFolder, fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public List<QrData> readJsonListFile(String fileName) {
        Gson gson = new Gson();
        File rootFolder = context.getCacheDir();
        File jsonFile = new File(rootFolder, fileName);
        try (Reader reader = new FileReader(jsonFile.getAbsolutePath())) {
            Type listType = new TypeToken<List<QrData>>() {}.getType();
            List<QrData> dataList = gson.fromJson(reader, listType);
            if (dataList == null) {
                dataList = new ArrayList<>();
            }
            return dataList;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }

    public void writeJsonToListFile(List<QrData> dataList, String fileName) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        File rootFolder = context.getCacheDir();
        File jsonFile = new File(rootFolder, fileName);
        try (FileWriter fileWriter = new FileWriter(jsonFile.getAbsolutePath())) {
            gson.toJson(dataList, fileWriter);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Map<String, String> readJsonMapFile(String fileName) {
        Gson gson = new Gson();
        File rootFolder = context.getCacheDir();
        File jsonFile = new File(rootFolder, fileName);
        try (Reader reader = new FileReader(jsonFile.getAbsolutePath())) {
            Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
            Map<String, String> map = gson.fromJson(reader, mapType);
            if (map == null) {
                map = new HashMap<>();
            }
            return map;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return new HashMap<>();
        }
    }

    public void writeJsonToMapFile(Map<String, String> map, String fileName) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        File rootFolder = context.getCacheDir();
        File jsonFile = new File(rootFolder, fileName);
        try (FileWriter fileWriter = new FileWriter(jsonFile.getAbsolutePath())) {
            gson.toJson(map, fileWriter);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
