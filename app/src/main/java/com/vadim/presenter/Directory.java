package com.vadim.presenter;

import android.os.Environment;

import java.io.File;

enum Directory {
    DCIM(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)),
    PICTURE(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)),
    DOWNLOAD(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

    private final File directory;

    Directory(final File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }
}

