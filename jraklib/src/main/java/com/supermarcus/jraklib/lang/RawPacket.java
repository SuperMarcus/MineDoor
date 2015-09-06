package com.supermarcus.jraklib.lang;

import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.network.ReceivedPacket;

import java.net.InetSocketAddress;

public class RawPacket {
    private byte[] data;

    private InetSocketAddress address;

    private RakLibInterface raklib;

    public RawPacket(ReceivedPacket packet, RakLibInterface raklib){
        this(packet.getRawData(), packet.getSendAddress(), raklib);
    }

    public RawPacket(byte[] data, InetSocketAddress target, RakLibInterface raklib) {
        this.data = data;
        this.address = target;
        this.raklib = raklib;
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public byte[] getData(){
        return this.data;
    }

    public RakLibInterface getInterface() {
        return raklib;
    }
}
