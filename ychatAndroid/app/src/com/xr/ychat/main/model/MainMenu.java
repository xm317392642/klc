package com.xr.ychat.main.model;

import java.io.Serializable;

public class MainMenu implements Serializable {
    private String name;
    private int icon;

    public MainMenu(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
