package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class UNCONNECTED_PONG extends Packet {
    private long pingID = 0L;

    private long serverID = 0L;

    private String serverName = "";

    protected UNCONNECTED_PONG(PacketInfo identifier){
        super(identifier);
    }

    public UNCONNECTED_PONG() {
        this(PacketInfo.UNCONNECTED_PONG);
    }

    public long getPingID(){
        return this.pingID;
    }

    public void setPingID(long pingID){
        this.pingID = pingID;
    }

    public long getServerID(){
        return this.serverID;
    }

    public void setServerID(long serverID){
        this.serverID = serverID;
    }

    public String getServerName(){
        return this.serverName;
    }

    public void setServerName(String serverName){
        this.serverName = serverName;
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getPingID());
        this.getBuffer().putLong(this.getServerID());
        this.getUtils().putMagic();
        this.getUtils().putString(this.getServerName());
    }

    @Override
    public void decode() {
        this.setPingID(this.getBuffer().getLong());
        this.setServerID(this.getBuffer().getLong());
        this.getUtils().getMagic();
        this.setServerName(this.getUtils().getString());
    }
}
