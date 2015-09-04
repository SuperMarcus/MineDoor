package com.supermarcus.jraklib.network;

import java.net.DatagramPacket;

public class QueuePacket implements Comparable {
    private SendPriority priority;

    private DatagramPacket packet;

    public QueuePacket(DatagramPacket packet, SendPriority priority){
        this.packet = packet;
        this.priority = priority;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Object o) {
        if(o instanceof QueuePacket){
            return (((QueuePacket) o).priority.getValue() - this.priority.getValue());
        }
        return 0;
    }

    public DatagramPacket getPacket() {
        return packet;
    }
}
