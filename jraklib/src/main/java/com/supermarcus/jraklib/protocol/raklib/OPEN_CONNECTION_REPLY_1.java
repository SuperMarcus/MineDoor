package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class OPEN_CONNECTION_REPLY_1 extends Packet {
    private long serverID = 0L;

    private int mtuSize = 0;

    private boolean security = false;

    public OPEN_CONNECTION_REPLY_1() {
        super(PacketInfo.OPEN_CONNECTION_REPLY_1);
    }

    @Override
    public void encode() {
        this.getUtils().putMagic();
        this.getBuffer().putLong(this.getServerID());
        this.getUtils().putBool(this.isSecurity());
        this.getBuffer().putShort((short) this.getMtuSize());
    }

    @Override
    public void decode() {
        this.getUtils().getMagic();
        this.setServerID(this.getBuffer().getLong());
        this.setSecurity(this.getUtils().getBool());
        this.setMtuSize(this.getBuffer().getShort());
    }

    public long getServerID() {
        return serverID;
    }

    public void setServerID(long serverID) {
        this.serverID = serverID;
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
