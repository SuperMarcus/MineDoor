package com.supermarcus.minedoor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileAlreadyExistsException;
import java.util.Properties;

public class ServerProperties extends Properties {
    private ArgumentDispatcher dispatcher;

    public ServerProperties(){
        super(DefaultProperties.getDefault());
    }

    public void loadArgumentDispatcher(ArgumentDispatcher dispatcher){
        this.dispatcher = dispatcher;
        this.putAll(dispatcher.getOverrideProperties());
    }

    public long getUpdateFrequency(){
        return Math.max(1, Long.parseLong(this.getProperty("monitor.update-frequency")));
    }

    public InetSocketAddress getInterfaceAddress(){
        return new InetSocketAddress(this.getProperty("server.address"), Short.parseShort(this.getProperty("server.port")));
    }

    public void save() throws IOException {
        File store = this.dispatcher.getPropertiesFile();
        if(store.isDirectory()){
            throw new FileAlreadyExistsException(store.getAbsolutePath(), "", "The config file already exists and is a directory");
        }else{
            if(!store.exists()){
                if(this.dispatcher.doNotCreateFile()){
                    throw new IOException("No new file creation");
                }
                //noinspection ResultOfMethodCallIgnored
                store.createNewFile();
            }
            if(store.canWrite()){
                FileWriter writer = new FileWriter(store);
                this.store(writer, "MineDoor Configuration file");
                writer.close();
            }else{
                throw new IOException("Configuration: '" + store.getAbsolutePath() + "' is not writable.");
            }
        }
    }
}
