package com.supermarcus.minedoor;

import com.supermarcus.jraklib.PacketHandler;
import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;

public class RakLibPacketHandler implements PacketHandler {
    private MineDoor mineDoor;

    public RakLibPacketHandler(MineDoor mineDoor){
        this.mineDoor = mineDoor;
    }

    @Override
    public void onRawPacket(RawPacket packet) {

    }

    @Override
    public void onACKNotification(ACKNotification notification) {

    }

    @Override
    public void onEncapsulated(Session session, EncapsulatedPacket packet, int flags) {
        this.mineDoor.onPlayerJoin(session);
    }
}
