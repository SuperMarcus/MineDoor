package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class UNCONNECTED_PING extends Packet {

    private long pingID = 0L;

    protected UNCONNECTED_PING(PacketInfo identifier){
        super(identifier);
    }

    public UNCONNECTED_PING() {
        this(PacketInfo.UNCONNECTED_PING);
    }

    public long getPingID(){
        return this.pingID;
    }

    public void setPingID(long pingID){
        this.pingID = pingID;
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getPingID());
        this.getUtils().putMagic();
    }

    @Override
    public void decode() {
        this.setPingID(this.getBuffer().getLong());
        //Magic
    }
}
