package com.supermarcus.jraklib.lang;

import com.supermarcus.jraklib.network.ReceivedPacket;

import java.net.InetSocketAddress;

public class RawPacket {
    private byte[] data;

    private InetSocketAddress address;

    public RawPacket(ReceivedPacket packet){
        this(packet.getRawData(), packet.getSendAddress());
    }

    public RawPacket(byte[] data, InetSocketAddress target) {
        this.data = data;
        this.address = target;
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public byte[] getData(){
        return this.data;
    }
}
