package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetSocketAddress;

public class CLIENT_HANDSHAKE_DataPacket extends Packet {
    private InetSocketAddress address = null;

    private InetSocketAddress[] systemAddresses = new InetSocketAddress[10];

    private long sendPing = 0L;

    private long sendPong = 0L;

    public CLIENT_HANDSHAKE_DataPacket() {
        super(PacketInfo.CLIENT_HANDSHAKE_DataPacket);
    }

    @Override
    public void encode() {
        //TODO: Not needed yet
    }

    @Override
    public void decode() {
        this.setAddress(this.getUtils().getAddress());
        InetSocketAddress[] systemAddresses = new InetSocketAddress[10];
        for(int i = 0; i < 10; ++i){
            systemAddresses[i] = this.getUtils().getAddress();
        }
        this.setSystemAddresses(systemAddresses);
        this.setSendPing(this.getBuffer().getLong());
        this.setSendPong(this.getBuffer().getLong());
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
