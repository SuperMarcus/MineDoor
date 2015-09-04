package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

public class InterfaceInterruptMessage extends ServerMessage {
    private Throwable exception;

    public InterfaceInterruptMessage(Throwable t, RakLibInterface sourceInterface) {
        super(MessageLevel.FATAL, sourceInterface);
        this.exception = t;
    }

    public Throwable getException(){
        return this.exception;
    }

    public String toString(){
        return super.toString() + " " + this.getException();
    }
}
