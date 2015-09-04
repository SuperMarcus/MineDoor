package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

public class TimeWarningMessage extends ServerMessage {
    public TimeWarningMessage(RakLibInterface rakLibInterface){
        super(MessageLevel.WARN, rakLibInterface);
    }
}
