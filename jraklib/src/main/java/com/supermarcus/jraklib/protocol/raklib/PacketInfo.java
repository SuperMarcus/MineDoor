package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.nio.ByteBuffer;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.*;
import com.supermarcus.jraklib.protocol.raklib.data.*;

/**
 * This class contains all the known packets and their ids
 */
public enum PacketInfo {
    /**
     * Ping/Pong
     */

    PING_DataPacket((byte) 0x00, PING_DataPacket.class),

    PONG_DataPacket((byte) 0x03, PONG_DataPacket.class),

    /**
     * Unconnected & connecting packets
     */

    UNCONNECTED_PING((byte) 0x01, UNCONNECTED_PING.class),

    UNCONNECTED_PING_OPEN_CONNECTIONS((byte) 0x02, UNCONNECTED_PING_OPEN_CONNECTIONS.class),

    OPEN_CONNECTION_REQUEST_1((byte) 0x05, OPEN_CONNECTION_REQUEST_1.class),

    OPEN_CONNECTION_REPLY_1((byte) 0x06, OPEN_CONNECTION_REPLY_1.class),

    OPEN_CONNECTION_REQUEST_2((byte) 0x07, OPEN_CONNECTION_REQUEST_2.class),

    OPEN_CONNECTION_REPLY_2((byte) 0x08, OPEN_CONNECTION_REPLY_2.class),

    CLIENT_CONNECT_DataPacket((byte) 0x09, CLIENT_CONNECT_DataPacket.class),

    SERVER_HANDSHAKE_DataPacket((byte) 0x10, SERVER_HANDSHAKE_DataPacket.class),

    CLIENT_HANDSHAKE_DataPacket((byte) 0x13, CLIENT_HANDSHAKE_DataPacket.class),

    CLIENT_DISCONNECT_DataPacket((byte) 0x15, CLIENT_DISCONNECT_DataPacket.class),

    UNCONNECTED_PONG((byte) 0x1c, UNCONNECTED_PONG.class),

    ADVERTISE_SYSTEM((byte) 0x1d, ADVERTISE_SYSTEM.class),

    /**
     * Data packets
     */

    DATA_PACKET_0((byte) 0x80, DATA_PACKET_0.class),

    DATA_PACKET_1((byte) 0x81, DATA_PACKET_1.class),

    DATA_PACKET_2((byte) 0x82, DATA_PACKET_2.class),

    DATA_PACKET_3((byte) 0x83, DATA_PACKET_3.class),

    DATA_PACKET_4((byte) 0x84, DATA_PACKET_4.class),

    DATA_PACKET_5((byte) 0x85, DATA_PACKET_5.class),

    DATA_PACKET_6((byte) 0x86, DATA_PACKET_6.class),

    DATA_PACKET_7((byte) 0x87, DATA_PACKET_7.class),

    DATA_PACKET_8((byte) 0x88, DATA_PACKET_8.class),

    DATA_PACKET_9((byte) 0x89, DATA_PACKET_9.class),

    DATA_PACKET_A((byte) 0x8a, DATA_PACKET_A.class),

    DATA_PACKET_B((byte) 0x8b, DATA_PACKET_B.class),

    DATA_PACKET_C((byte) 0x8c, DATA_PACKET_C.class),

    DATA_PACKET_D((byte) 0x8d, DATA_PACKET_D.class),

    DATA_PACKET_E((byte) 0x8e, DATA_PACKET_E.class),

    DATA_PACKET_F((byte) 0x8f, DATA_PACKET_F.class),

    /**
     * Acknowledge packets
     */

    NACK((byte) 0xa0, NACK.class),

    ACK((byte) 0xc0, ACK.class);

    /**
     * Protocol implemented in JRakLib
     */
    public static final int PROTOCOL = 5;

    /**
     * RakLib version
     */
    public static final String VERSION = "0.7.0";

    private byte id;

    private Class<? extends Packet> packet;

    PacketInfo(byte id, Class<? extends Packet> packet){
        this.id = id;
        this.packet = packet;
    }

    public byte getNetworkId(){
        return this.id;
    }

    public Packet wrap(byte[] buffer){
        Packet instance = null;
        if(buffer[0] == this.getNetworkId()){
            try {
                instance = this.packet.newInstance();
                instance.initBuffer(ByteBuffer.wrap(buffer));
            } catch (Exception e) {
                instance = null;
            }
        }
        return instance;
    }

    public static PacketInfo getById(byte id){
        for(PacketInfo p : PacketInfo.values()){
            if(p.getNetworkId() == id){
                return p;
            }
        }
        return null;
    }
}
