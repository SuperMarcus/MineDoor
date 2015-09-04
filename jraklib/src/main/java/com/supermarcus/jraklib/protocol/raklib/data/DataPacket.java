package com.supermarcus.jraklib.protocol.raklib.data;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;
import com.supermarcus.jraklib.protocol.raklib.PacketInfo;

import java.util.ArrayList;

abstract public class DataPacket extends Packet {
    private ArrayList<BinaryConvertible> packets = new ArrayList<>();

    private int seqNumber = 0;

    public DataPacket(PacketInfo identifier) {
        super(identifier);
    }

    public void encode(){
        this.getUtils().putLTriad(this.getSeqNumber());
        for(BinaryConvertible packet : this.getPackets()){
            if(packet instanceof EncapsulatedPacket){
                ((EncapsulatedPacket) packet).writeToBuffer(this.getBuffer());
            }else{
                this.getBuffer().put(packet.toBinary());
            }
        }
    }

    public void decode(){
        this.setSeqNumber(this.getUtils().getLTriad());
        while(this.getBuffer().hasRemaining()){
            EncapsulatedPacket packet = new EncapsulatedPacket(this.getBuffer());
            if(packet.getBuffer().length > 0){
                this.addPacket(packet);
            }else{
                break;
            }
        }
    }

    public int getLength(){
        int length = 4;
        for(BinaryConvertible packet : this.getPackets()){
            if(packet instanceof EncapsulatedPacket){
                length += ((EncapsulatedPacket) packet).getTotalLength();
            }else{
                length += packet.toBinary().length;
            }
        }
        return length;
    }

    public void addPacket(BinaryConvertible packet){
        this.packets.add(packet);
    }

    public void clearPackets(){
        this.packets.clear();
    }

    public int countPackets(){
        return this.packets.size();
    }

    public BinaryConvertible[] getPackets(){
        return this.packets.toArray(new BinaryConvertible[this.packets.size()]);
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }
}
