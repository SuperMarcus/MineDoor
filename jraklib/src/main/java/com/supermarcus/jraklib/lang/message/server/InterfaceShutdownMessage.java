package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

public class InterfaceShutdownMessage extends ServerMessage {
    private long millis;

    public InterfaceShutdownMessage(long millis, RakLibInterface sourceInterface) {
        super(MessageLevel.INFO, sourceInterface);
        this.millis = millis;
    }

    public long getTimeMillis(){
        return this.millis;
    }
}
