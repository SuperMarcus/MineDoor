package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class PING_DataPacket extends Packet {
    private long pingID = 0L;

    public PING_DataPacket(){
        super(PacketInfo.PING_DataPacket);
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getPingID());
    }

    @Override
    public void decode() {
        this.setPingID(this.getBuffer().getLong());
    }

    public long getPingID() {
        return pingID;
    }

    public void setPingID(long pingID) {
        this.pingID = pingID;
    }
}
