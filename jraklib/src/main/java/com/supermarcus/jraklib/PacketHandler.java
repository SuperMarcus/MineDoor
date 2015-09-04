package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;

public interface PacketHandler {
    void onRawPacket(RawPacket packet);

    void onACKNotification(ACKNotification notification);

    void onEncapsulated(Session session, EncapsulatedPacket packet, int flags);
}
