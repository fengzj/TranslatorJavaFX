package com.maple;

public enum Languages {
    ENGLISH("en"), CHINESE_SIMPLE("zh-CN"), AUTO("auto");

    private final String value;

    Languages(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return this.value;
    }
}
