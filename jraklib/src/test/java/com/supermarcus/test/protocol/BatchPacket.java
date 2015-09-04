package com.supermarcus.test.protocol;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.BinaryUtils;

import java.nio.ByteBuffer;

public class BatchPacket implements BinaryConvertible {
    public static int NETWORK_ID = 0xb1;

    private byte[] payload;

    public static BatchPacket fromBinary(byte[] buffer){
        BatchPacket packet = new BatchPacket();

        ByteBuffer buf = ByteBuffer.wrap(buffer);
        BinaryUtils utils = new BinaryUtils(buf);

        buf.get();
        packet.payload = utils.getBytes(buf.getInt());

        return packet;
    }

    public byte[] getPayload(){
        return payload;
    }

    @Override
    public byte[] toBinary() {
        return ByteBuffer.allocate(5 + this.payload.length).put((byte) NETWORK_ID).putInt(this.payload.length).put(this.payload).array();
    }
}
