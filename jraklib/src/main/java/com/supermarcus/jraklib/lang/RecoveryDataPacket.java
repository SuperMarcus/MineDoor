package com.supermarcus.jraklib.lang;

import com.supermarcus.jraklib.protocol.raklib.data.DataPacket;

public class RecoveryDataPacket {
    private long sendTime;

    private DataPacket packet;

    public RecoveryDataPacket(DataPacket packet, long sendTime){
        this.packet = packet;
        this.sendTime = sendTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public DataPacket getPacket() {
        return packet;
    }

    public Integer getSeqNumber(){
        return getPacket().getSeqNumber();
    }
}
