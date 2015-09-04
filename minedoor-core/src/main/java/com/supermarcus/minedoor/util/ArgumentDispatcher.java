package com.supermarcus.minedoor.util;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Properties;

public class ArgumentDispatcher {
    private Properties overrideProperties = new Properties();

    private File propertiesFile = new File("server.properties");

    private File serverList = new File("servers.txt");

    public ArgumentDispatcher(String[] arguments){
        for (int argIndex = 0; argIndex < arguments.length; ++argIndex) {
            try {
                argIndex += this.dispatch(arguments[argIndex].trim(), (argIndex == arguments.length - 1 ? null : arguments[argIndex + 1].trim()));
            }catch (Exception e){
                LogManager.getLogger().warn("Illegal argument: " + arguments[argIndex].trim(), e);
            }
        }
    }

    private int dispatch(String key, String value) throws Exception{
        if(key.startsWith("-P")){//Override properties
            this.getOverrideProperties().setProperty(key.substring(2), value);
            return 1;
        }else if(key.toLowerCase().equals("--config") || key.toLowerCase().equals("-c")){
            File newConfig = new File(value);
            if(!newConfig.isDirectory()){
                if(!newConfig.exists()){
                    if(!newConfig.createNewFile()){
                        throw new IOException("Unable to create config file");
                    }
                }else if(!newConfig.canRead()){
                    throw new IOException("Configuration '" + newConfig.getAbsolutePath() + "' is not readable");
                }
                this.propertiesFile = newConfig;
                return 1;
            }else{
                throw new FileAlreadyExistsException(newConfig.getAbsolutePath(), "", "The config file pointed in arguments already exists and is a directory");
            }
        }else if(key.toLowerCase().equals("--server") || key.toLowerCase().equals("-s")) {
            File list = new File(value);
            if(!list.isDirectory()){
                if(!list.exists()){
                    if(!list.createNewFile()){
                        throw new IOException("Unable to create server list file");
                    }
                }else if(!list.canRead()){
                    throw new IOException("Server list '" + list.getAbsolutePath() + "' is not readable");
                }
                this.serverList = list;
                return 1;
            }else{
                throw new FileAlreadyExistsException(list.getAbsolutePath(), "", "The server list pointed in arguments already exists and is a directory");
            }
        }
        return 0;
    }

    public File getPropertiesFile(){
        return this.propertiesFile;
    }

    public Properties getOverrideProperties(){
        return this.overrideProperties;
    }

    public File getServerList() {
        return serverList;
    }
}
