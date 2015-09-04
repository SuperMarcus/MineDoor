package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetSocketAddress;

public class OPEN_CONNECTION_REQUEST_2 extends Packet {
    private long clientID = 0L;

    private InetSocketAddress serverAddress = null;

    private int mtuSize = 0;

    public OPEN_CONNECTION_REQUEST_2() {
        super(PacketInfo.OPEN_CONNECTION_REQUEST_2);
    }

    @Override
    public void encode() {
        this.getUtils().putMagic();
        this.getUtils().putAddress(this.getServerAddress());
        this.getBuffer().putShort((short) this.getMtuSize());
        this.getBuffer().putLong(this.getClientID());
    }

    @Override
    public void decode() {
        this.getUtils().getMagic();
        this.setServerAddress(this.getUtils().getAddress());
        this.setMtuSize(this.getBuffer().getShort());
        this.setClientID(this.getBuffer().getLong());
    }

    public long getClientID() {
        return clientID;
    }

    public void setClientID(long clientID) {
        this.clientID = clientID;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }
}
