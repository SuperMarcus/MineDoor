package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.lang.message.RakLibMessage;
import com.supermarcus.jraklib.network.RakLibInterface;

import java.lang.ref.WeakReference;
import java.util.Objects;

abstract public class ServerMessage extends RakLibMessage {
    WeakReference<RakLibInterface> source;

    public ServerMessage(MessageLevel level, RakLibInterface sourceInterface) {
        super(level);
        Objects.requireNonNull(sourceInterface);
        this.source = new WeakReference<>(sourceInterface);
    }

    public RakLibInterface getSource(){
        return source.get();
    }
}
