package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetSocketAddress;

public class OPEN_CONNECTION_REPLY_2 extends Packet {
    private long serverID = 0L;

    private InetSocketAddress clientAddress = null;

    private int mtuSize = 0;

    private boolean security = false;

    public OPEN_CONNECTION_REPLY_2() {
        super(PacketInfo.OPEN_CONNECTION_REPLY_2);
    }

    @Override
    public void encode() {
        this.getUtils().putMagic();
        this.getBuffer().putLong(this.getServerID());
        this.getUtils().putAddress(this.getClientAddress());
        this.getBuffer().putShort((short) this.getMtuSize());
        this.getUtils().putBool(this.isSecurity());
    }

    @Override
    public void decode() {
        this.getUtils().getMagic();
        this.setServerID(this.getBuffer().getLong());
        this.setClientAddress(this.getUtils().getAddress());
        this.setMtuSize(this.getBuffer().getShort());
        this.setSecurity(this.getUtils().getBool());
    }

    public long getServerID() {
        return serverID;
    }

    public void setServerID(long serverID) {
        this.serverID = serverID;
    }

    public InetSocketAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }
}
