package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

public class OPEN_CONNECTION_REQUEST_1 extends Packet {
    private int protocol = PacketInfo.PROTOCOL;

    private int mtuSize = -1;

    public OPEN_CONNECTION_REQUEST_1() {
        super(PacketInfo.OPEN_CONNECTION_REQUEST_1);
    }

    @Override
    public void encode() {
        this.getUtils().putMagic();
        this.getBuffer().put((byte) this.getProtocol());
        this.getUtils().putRepeatedBytes((byte) 0x00, (this.getMtuSize() - 18));
    }

    @Override
    public void decode() {
        this.getUtils().getMagic();
        this.setProtocol(this.getBuffer().get());
        this.setMtuSize(this.getBuffer().remaining() + 18);
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }
}
