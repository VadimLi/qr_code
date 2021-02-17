package com.vadim.presenter;

import java.io.File;

public class SettingsPresenter implements PresenterView {
    private static final String TAG = SettingsPresenter.class.getSimpleName();

    private SettingsView settingsView;

    public SettingsPresenter(SettingsView settingsView) {
        this.settingsView = settingsView;
    }

    public void deleteDirectory() {
        File pictureDir = Directory.PICTURE.getDirectory();
        File deletedFolder = new File(pictureDir.getAbsolutePath() + "/QrList");
        if (!deletedFolder.exists()) {
            settingsView.notifyAboutNotExisting();
            return;
        }
        String[] files = deletedFolder.list();
        if (files != null) {
            deleteOfPhotos(files, deletedFolder);
            settingsView.notifyAboutDeleting();
        }
    }

    private void deleteOfPhotos(String[] files, File deletedFolder) {
        for (String fileName : files) {
            File currentFile = new File(deletedFolder + "/" + fileName);
            currentFile.delete();
        }
        deletedFolder.delete();
    }

    @Override
    public void detachView() {
        settingsView = null;
    }

    public interface SettingsView {
        void notifyAboutDeleting();
        void notifyAboutNotExisting();
    }

}
