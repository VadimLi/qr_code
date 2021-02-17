package com.vadim.presenter;

public enum FormatListing {
    JPG("jpg"),
    PNG("png");

    private final String format;

    FormatListing(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

}
