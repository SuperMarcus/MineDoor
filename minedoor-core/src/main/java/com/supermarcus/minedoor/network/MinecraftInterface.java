package com.supermarcus.minedoor.network;

import com.supermarcus.jraklib.RakLibServerInstance;
import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;
import com.supermarcus.minedoor.MineDoor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketException;

public class MinecraftInterface extends RakLibServerInstance {
    private MineDoor mineDoor;

    private Logger logger = LogManager.getLogger();

    public MinecraftInterface(MineDoor mineDoor){
        this.mineDoor = mineDoor;
    }

    public void start(InetSocketAddress bindAddress){
        try {
            this.setPacketHandler(this.mineDoor.getPacketHandler());
            this.addInterface(bindAddress);
        } catch (SocketException e) {
            logger.fatal("Unable to start interface", e);
        } catch (InterfaceOutOfPoolSizeException e) {
            e.printStackTrace();
        }
    }
}
