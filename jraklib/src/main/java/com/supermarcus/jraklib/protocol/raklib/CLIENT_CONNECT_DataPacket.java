package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class CLIENT_CONNECT_DataPacket extends Packet {
    private boolean security = false;

    private long clientID = 0L;

    private long sendPing = 0L;

    public CLIENT_CONNECT_DataPacket() {
        super(PacketInfo.CLIENT_CONNECT_DataPacket);
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getClientID());
        this.getBuffer().putLong(this.getSendPing());
        this.getUtils().putBool(this.isSecurity());
    }

    @Override
    public void decode() {
        this.setClientID(this.getBuffer().getLong());
        this.setSendPing(this.getBuffer().getLong());
        this.setSecurity(this.getUtils().getBool());
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public long getClientID() {
        return clientID;
    }

    public void setClientID(long clientID) {
        this.clientID = clientID;
    }

    public long getSendPing() {
        return sendPing;
    }

    public void setSendPing(long sendPing) {
        this.sendPing = sendPing;
    }
}
