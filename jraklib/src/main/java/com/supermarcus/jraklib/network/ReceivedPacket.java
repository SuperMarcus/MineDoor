package com.supermarcus.jraklib.network;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class ReceivedPacket {
    private byte[] rawPacket;

    private InetSocketAddress fromAddress;

    public ReceivedPacket(DatagramPacket dPacket){
        this(null, new InetSocketAddress(dPacket.getAddress(), dPacket.getPort()));
        byte[] data = new byte[dPacket.getLength()];
        System.arraycopy(dPacket.getData(), 0, data, 0, dPacket.getLength());
        this.rawPacket = data;
    }
    
    public ReceivedPacket(byte[] buffer, InetSocketAddress address){
        this.fromAddress = address;
        this.rawPacket = buffer;
    }

    public InetSocketAddress getSendAddress(){
        return this.fromAddress;
    }

    public byte[] getRawData(){
        return this.rawPacket;
    }
}
