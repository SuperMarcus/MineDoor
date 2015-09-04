package com.supermarcus.minedoor.util;

import java.util.Properties;

public class DefaultProperties extends Properties {
    private DefaultProperties(){
        this.setProperty("server.address", "0.0.0.0");
        this.setProperty("server.port", "19132");
        this.setProperty("server.name", "MinecraftServer");
        this.setProperty("server.maximum-people", "auto");
        this.setProperty("monitor.update-frequency", "15000");
    }

    public static DefaultProperties getDefault(){
        return new DefaultProperties();
    }
}
