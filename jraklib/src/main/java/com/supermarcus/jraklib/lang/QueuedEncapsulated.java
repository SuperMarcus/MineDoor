package com.supermarcus.jraklib.lang;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;

public class QueuedEncapsulated {
    private EncapsulatedPacket packet;

    private int flags;

    private Session session;

    public QueuedEncapsulated(Session session, EncapsulatedPacket packet, int flags){
        this.packet = packet;
        this.flags = flags;
        this.session = session;
    }

    public EncapsulatedPacket getPacket() {
        return packet;
    }

    public int getFlags() {
        return flags;
    }

    public Session getSession() {
        return session;
    }
}
