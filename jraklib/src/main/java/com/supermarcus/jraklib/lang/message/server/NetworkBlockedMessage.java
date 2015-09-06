package com.supermarcus.jraklib.lang.message.server;

import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.network.RakLibInterface;

import java.net.InetAddress;

public class NetworkBlockedMessage extends ServerMessage {
    private InetAddress address;

    private long millis;

    public NetworkBlockedMessage(InetAddress address, long millis, RakLibInterface sourceInterface) {
        super(MessageLevel.INFO, sourceInterface);
        this.address = address;
        this.millis = millis;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getMillis() {
        return millis;
    }
}
