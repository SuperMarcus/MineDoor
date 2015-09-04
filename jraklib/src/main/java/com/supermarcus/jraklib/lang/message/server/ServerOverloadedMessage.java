package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

public class ServerOverloadedMessage extends ServerMessage {
    private long takes;

    public ServerOverloadedMessage(long tickTakes, RakLibInterface sourceInterface) {
        super(MessageLevel.WARN, sourceInterface);
        this.takes = tickTakes;
    }

    public long getTickMillis(){
        return this.takes;
    }
}
