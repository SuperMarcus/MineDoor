package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetSocketAddress;

public class SERVER_HANDSHAKE_DataPacket extends Packet {
    private InetSocketAddress address = null;

    private InetSocketAddress[] systemAddresses = {
            new InetSocketAddress("127.0.0.1", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0),
            new InetSocketAddress("0.0.0.0", 0)
    };

    private long sendPing = 0L;

    private long sendPong = 0L;

    public SERVER_HANDSHAKE_DataPacket(){
        super(PacketInfo.SERVER_HANDSHAKE_DataPacket);
    }

    @Override
    public void encode() {
        this.getUtils().putAddress(this.getAddress());
        this.getBuffer().putShort((short) 0);
        for(int i = 0; i < 10; ++i){
            this.getUtils().putAddress(this.getSystemAddresses()[i]);
        }
        this.getBuffer().putLong(this.getSendPing());
        this.getBuffer().putLong(this.getSendPong());
    }

    @Override
    public void decode() {
        //TODO: Not need yet
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public InetSocketAddress[] getSystemAddresses() {
        return systemAddresses;
    }

    public void setSystemAddresses(InetSocketAddress[] systemAddresses) {
        this.systemAddresses = systemAddresses;
    }

    public long getSendPing() {
        return sendPing;
    }

    public void setSendPing(long sendPing) {
        this.sendPing = sendPing;
    }

    public long getSendPong() {
        return sendPong;
    }

    public void setSendPong(long sendPong) {
        this.sendPong = sendPong;
    }
}
