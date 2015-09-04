package com.supermarcus.test.protocol;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.BinaryUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class StrangePacket implements BinaryConvertible {
    private InetSocketAddress destination = null;

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        BinaryUtils utils = new BinaryUtils(buffer);
        buffer.put((byte) 0x1b);
        utils.putAddress(this.getDestination());
        byte[] binary = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, binary, 0, buffer.position());
        return binary;
    }

    public InetSocketAddress getDestination() {
        return destination;
    }

    public void setDestination(InetSocketAddress destination) {
        this.destination = destination;
    }
}
