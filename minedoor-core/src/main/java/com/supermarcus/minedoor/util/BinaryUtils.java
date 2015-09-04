package com.supermarcus.minedoor.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class BinaryUtils {
    public static final byte[] MAGIC = {0, -1, -1, 0, -2, -2, -2, -2, -3, -3, -3, -3, 18, 52, 86, 120};

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private ByteBuffer buffer;

    public BinaryUtils(ByteBuffer buffer){
        this.buffer = Objects.requireNonNull(buffer);
    }

    public void putMagic(){
        this.getBuffer().put(com.supermarcus.jraklib.protocol.BinaryUtils.MAGIC);
    }

    public void putLTriad(int value){
        this.getBuffer().put((byte)(value & 0xFF));
        this.getBuffer().put((byte)((value >> 8) & 0xFF));
        this.getBuffer().put((byte) ((value >> 16) & 0xFF));
    }

    public void putString(String value){
        this.putString(value, com.supermarcus.jraklib.protocol.BinaryUtils.DEFAULT_CHARSET);
    }

    public void putString(String value, Charset charset){
        byte[] bytes = value.getBytes(charset);
        this.getBuffer().putShort((short) bytes.length);
        this.getBuffer().put(bytes);
    }

    public void putBool(boolean value){
        this.getBuffer().put((byte) (value ? 1 : 0));
    }

    public void putAddress(InetSocketAddress address){
        this.getBuffer().put((byte)(address.getAddress() instanceof Inet6Address ? 6 : 4));
        byte[] addressBytes = address.getAddress().getAddress();
        for(int i = 0; i < addressBytes.length; ++i){
            addressBytes[i] = (byte) ~addressBytes[i];
        }
        this.getBuffer().put(addressBytes);
        this.getBuffer().putShort((short) address.getPort());
    }

    public void putRepeatedBytes(byte value, int length){
        while ((--length) >= 0){
            this.getBuffer().put(value);
        }
    }

    public void getMagic(){
        this.getBytes(16);
    }

    public String getString(){
        return this.getString(this.getBuffer().getShort());
    }

    public String getString(int length){
        return this.getString(length, com.supermarcus.jraklib.protocol.BinaryUtils.DEFAULT_CHARSET);
    }

    public String getString(int length, Charset charset){
        return new String(this.getBytes(length), charset);
    }

    public boolean getBool(){
        return (this.getBuffer().get() > 0);
    }

    public boolean checkMagic(){
        return Arrays.equals(this.getBytes(16), com.supermarcus.jraklib.protocol.BinaryUtils.MAGIC);
    }

    public int getLTriad(){
        return (this.getBuffer().get() & 0xFF) | ((this.getBuffer().get() & 0xFF) << 8) | ((this.getBuffer().get() & 0x0F) << 16);
    }

    public InetSocketAddress getAddress(){
        try {
            byte[] addressBytes = this.getBytes((this.getBuffer().get() == 16) ? 16 : 4);
            for(int i = 0; i < addressBytes.length; ++i){
                addressBytes[i] = (byte) ~addressBytes[i];
            }
            return new InetSocketAddress(InetAddress.getByAddress(addressBytes), this.getBuffer().getShort());
        } catch (UnknownHostException ignore) {}
        return null;
    }

    public byte[] getBytes(int length){
        byte[] buffer = new byte[Math.min(length, this.getBuffer().remaining())];
        this.getBuffer().get(buffer);
        return buffer;
    }

    public byte[] getRemainingBytes(){
        return this.getBytes(this.getBuffer().remaining());
    }

    private ByteBuffer getBuffer(){
        return this.buffer;
    }
}
