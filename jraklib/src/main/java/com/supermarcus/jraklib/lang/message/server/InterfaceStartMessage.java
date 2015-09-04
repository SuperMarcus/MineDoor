package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

public class InterfaceStartMessage extends ServerMessage {
    private long millis;

    public InterfaceStartMessage(long millis, RakLibInterface sourceInterface) {
        super(MessageLevel.INFO, sourceInterface);
        this.millis = millis;
    }

    public long getTimeMillis(){
        return this.millis;
    }
}
