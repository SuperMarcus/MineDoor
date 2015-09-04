package com.supermarcus.jraklib.network;

/**
 * RakLib Packets send priority
 */
public enum SendPriority {
    /**
     * Normal priority, will be normally queue to send
     */
    NORMAL(0),

    /**
     * Highest priority, will be queue but send first
     */
    IMMEDIATE(1);

    private int value;

    SendPriority(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}
