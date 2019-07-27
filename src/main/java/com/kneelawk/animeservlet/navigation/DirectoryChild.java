package com.kneelawk.animeservlet.navigation;

/**
 * Created by Kneelawk on 7/27/19.
 */
public class DirectoryChild {
    private final String url;
    private final String name;

    public DirectoryChild(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}
