package com.supermarcus.jraklib.protocol.raklib.acknowledge;

import com.supermarcus.jraklib.protocol.raklib.PacketInfo;

public class ACK extends AcknowledgePacket {
    public ACK() {
        super(PacketInfo.ACK);
    }
}
