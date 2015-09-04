package com.supermarcus.jraklib.protocol.raklib.acknowledge;

import com.supermarcus.jraklib.protocol.raklib.PacketInfo;

public class NACK extends AcknowledgePacket {
    public NACK() {
        super(PacketInfo.NACK);
    }
}
