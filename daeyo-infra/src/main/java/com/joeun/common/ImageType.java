package com.joeun.common;

public enum ImageType {
    ITEM("daeyeo-item"),
    UNIT("daeyeo-item-unit");

    private final String type;

    ImageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

