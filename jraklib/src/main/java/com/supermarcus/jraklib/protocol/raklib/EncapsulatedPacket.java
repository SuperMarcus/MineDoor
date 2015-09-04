package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.BinaryUtils;

import java.nio.ByteBuffer;

public class EncapsulatedPacket implements Cloneable, BinaryConvertible {

    /**
     * RakNet reliability constants
     *
     * Default: 0b010 (RELIABLE) or 0b011 (RELIABLE_ORDERED)
     *
     * @link http://www.jenkinssoftware.com/raknet/manual/reliabilitytypes.html
     */

    public static final int UNRELIABLE = 0;
    public static final int UNRELIABLE_SEQUENCED = 1;
    public static final int RELIABLE = 2;
    public static final int RELIABLE_ORDERED = 3;
    public static final int RELIABLE_SEQUENCED = 4;
    public static final int UNRELIABLE_WITH_ACK_RECEIPT = 5;
    public static final int RELIABLE_WITH_ACK_RECEIPT = 6;
    public static final int RELIABLE_ORDERED_WITH_ACK_RECEIPT = 7;

    private int reliability = 0;

    private boolean split = false;
    private int splitCount = -1;
    private short splitID = -1;
    private int splitIndex = -1;

    private byte[] buffer = new byte[0];

    private boolean needACK = false;

    private Integer identifierACK = null;

    private Integer messageIndex = null;
    private Integer orderIndex = null;
    private Integer orderChannel = null;

    public EncapsulatedPacket(){

    }

    public EncapsulatedPacket(EncapsulatedPacket packet){
        this.reliability = packet.reliability;
        this.split = packet.split;
        this.splitCount = packet.splitCount;
        this.splitID = packet.splitID;
        this.splitIndex = packet.splitIndex;
        this.needACK = packet.needACK;

        this.buffer = new byte[packet.buffer.length];
        System.arraycopy(packet.buffer, 0, this.buffer, 0, packet.buffer.length);

        this.identifierACK = (packet.identifierACK == null ? null : (Integer)((int) packet.identifierACK));
        this.messageIndex = (packet.messageIndex == null ? null : (Integer)((int) packet.messageIndex));
        this.orderIndex = (packet.orderIndex == null ? null : (Integer)((int) packet.orderIndex));
        this.orderChannel = (packet.orderChannel == null ? null : (Integer)((int) packet.orderChannel));
    }

    public EncapsulatedPacket(ByteBuffer buffer){
        this(buffer, false);
    }

    public EncapsulatedPacket(ByteBuffer buffer, boolean internal){
        BinaryUtils utils = new BinaryUtils(buffer);

        byte flags = buffer.get();
        this.setReliability((flags & 0b11100000) >> 5);
        this.setSplit((flags & 0b00010000) > 0);

        int length;
        if(internal){
            length = buffer.getInt();
            this.setIdentifierACK(buffer.getInt());
        }else {
            length = (int) Math.ceil(buffer.getShort() / 8);
            this.setIdentifierACK(null);
        }

        if(this.getReliability() > EncapsulatedPacket.UNRELIABLE){
            if((this.getReliability() >= EncapsulatedPacket.RELIABLE) && (this.getReliability() != EncapsulatedPacket.UNRELIABLE_WITH_ACK_RECEIPT)){
                this.setMessageIndex(utils.getLTriad());
            }

            if((this.getReliability() <= EncapsulatedPacket.RELIABLE_SEQUENCED) && (this.getReliability() != EncapsulatedPacket.RELIABLE)){
                this.setOrderIndex(utils.getLTriad());
                this.setOrderChannel((int) buffer.get());
            }
        }

        if(this.hasSplit()){
            this.setSplitCount(buffer.getInt());
            this.setSplitID(buffer.getShort());
            this.setSplitIndex(buffer.getInt());
        }

        this.setBuffer(utils.getBytes(length));
    }

    public void writeToBuffer(ByteBuffer buffer){
        this.writeToBuffer(buffer, false);
    }

    public void writeToBuffer(ByteBuffer buffer, boolean internal){
        BinaryUtils utils = new BinaryUtils(buffer);

        buffer.put((byte)((this.getReliability() << 5) | (this.hasSplit() ? (0b00010000) : (0))));

        if(internal){
            buffer.putInt(this.getBuffer().length);
            buffer.putInt(this.getIdentifierACK());
        }else {
            buffer.putShort((short) (this.getBuffer().length << 3));
        }

        if(this.getReliability() > EncapsulatedPacket.UNRELIABLE){
            if((this.getReliability() >= EncapsulatedPacket.RELIABLE) && (this.getReliability() != EncapsulatedPacket.UNRELIABLE_WITH_ACK_RECEIPT)){
                utils.putLTriad(this.getMessageIndex());
            }

            if((this.getReliability() <= EncapsulatedPacket.RELIABLE_SEQUENCED) && (this.getReliability() != EncapsulatedPacket.RELIABLE)){
                utils.putLTriad(this.getOrderIndex());
                buffer.put((byte) (int) this.getOrderChannel());
            }
        }

        if(this.hasSplit()) {
            buffer.putInt(this.getSplitCount());
            buffer.putShort(this.getSplitID());
            buffer.putInt(this.getSplitIndex());
        }

        buffer.put(this.getBuffer());
    }

    public byte[] toBinary(){
        ByteBuffer buffer = ByteBuffer.allocate(this.getTotalLength());
        this.writeToBuffer(buffer);
        return buffer.array();
    }

    public int getTotalLength(){
        return (3 + this.getBuffer().length + (this.getMessageIndex() == null ? 0 : 3) + (this.getOrderIndex() == null ? 0 : 4) + (this.hasSplit() ? 10 : 0));
    }

    public EncapsulatedPacket clone(){
        try {
            return (EncapsulatedPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getReliability() {
        return reliability;
    }

    public void setReliability(int reliability) {
        this.reliability = reliability;
    }

    public boolean hasSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public Integer getMessageIndex() {
        return messageIndex;
    }

    public void setMessageIndex(Integer messageIndex) {
        this.messageIndex = messageIndex;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getOrderChannel() {
        return orderChannel;
    }

    public void setOrderChannel(Integer orderChannel) {
        this.orderChannel = orderChannel;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public short getSplitID() {
        return splitID;
    }

    public void setSplitID(short splitID) {
        this.splitID = splitID;
    }

    public int getSplitIndex() {
        return splitIndex;
    }

    public void setSplitIndex(int splitIndex) {
        this.splitIndex = splitIndex;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void setBuffer(BinaryConvertible packet){
        this.setBuffer(packet.toBinary());
    }

    public boolean needACK() {
        return needACK;
    }

    public void setNeedACK(boolean needACK) {
        this.needACK = needACK;
    }

    public Integer getIdentifierACK() {
        return identifierACK;
    }

    public void setIdentifierACK(Integer identifierACK) {
        this.identifierACK = identifierACK;
    }
}
