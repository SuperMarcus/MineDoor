package com.supermarcus.minedoor.network;

import com.supermarcus.jraklib.RakLibServerInstance;
import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;
import com.supermarcus.minedoor.MineDoor;
import com.supermarcus.minedoor.util.ServerProperties;
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
            this.getSessionManager().setDisplayName(this.mineDoor.getProperties().getServerName());
            this.setPacketHandler(this.mineDoor.getPacketHandler());
            this.addInterface(bindAddress);
        } catch (SocketException e) {
            logger.fatal("Unable to start interface", e);
        } catch (InterfaceOutOfPoolSizeException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerCounter(int[] playerCounter){
        ServerProperties properties = this.mineDoor.getProperties();
        int currentPlayers = Math.min(playerCounter[0], (properties.getMaxPlayers() == -1 ? playerCounter[1] : properties.getMaxPlayers()));
        int maxPlayers = properties.getMaxPlayers() == -1 ? playerCounter[1] : properties.getMaxPlayers();
        this.getSessionManager().setPlayerOnline(currentPlayers);
        this.getSessionManager().setMaxPlayer(maxPlayers);
    }
}
