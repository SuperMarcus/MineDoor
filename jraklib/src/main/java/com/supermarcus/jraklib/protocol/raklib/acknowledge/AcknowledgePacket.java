package com.supermarcus.jraklib.protocol.raklib.acknowledge;

import com.supermarcus.jraklib.protocol.BinaryUtils;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.PacketInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class AcknowledgePacket extends Packet {
    private ArrayList<Integer> packets = new ArrayList<>();

    public AcknowledgePacket(PacketInfo identifier) {
        super(identifier);
    }

    public void encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        BinaryUtils utils = new BinaryUtils(buffer);
        int records = 0;

        if(this.getPacketCount() > 0){
            Integer[] packets = this.getPackets();

            int pointer = 0;
            int start = packets[0];
            int last = packets[0];

            while(pointer + 1 < this.getPacketCount()){
                int current = packets[pointer++];
                int diff = current - last;
                if(diff == 1){
                    last = current;
                } else if(diff > 1){
                    if(start == last){
                        utils.putBool(true);
                        utils.putLTriad(start);
                        start = last = current;
                    } else {
                        utils.putBool(false);
                        utils.putLTriad(start);
                        utils.putLTriad(last);
                        start = last = current;
                    }
                    records++;
                }
            }

            if(start == last){
                utils.putBool(true);
                utils.putLTriad(start);
            } else {
                utils.putBool(false);
                utils.putLTriad(start);
                utils.putLTriad(last);
            }
            records++;
        }
        this.getBuffer().putShort((short) records);
        this.getBuffer().put(buffer.array(), 0, buffer.position());
    }

    public void decode(){
        this.clearPacketBuffer();
        int count = this.getBuffer().getShort();
        int cnt = 0;

        for(int i = 0; ((i < count) && (this.getBuffer().remaining() > 0) && (cnt < 4096)); i++){
            if(this.getUtils().getBool()){
                this.addPacket(this.getUtils().getLTriad());
            } else {
                int start = this.getUtils().getLTriad();
                int end = this.getUtils().getLTriad();

                if((end - start) > 512){
                    end = start + 512;
                }
                for(int p = start; p <= end; p++){
                    cnt++;
                    this.addPacket(p);
                }
            }
        }
    }

    public void addPackets(Collection<Integer> packets){
        this.packets.addAll(packets);
    }

    public void addPacket(int packetIdentifier){
        this.packets.add(packetIdentifier);
    }

    public Integer[] getPackets(){
        Collections.sort(this.packets);
        return this.packets.toArray(new Integer[this.packets.size()]);
    }

    public int getPacketCount(){
        return this.packets.size();
    }

    public void clearPacketBuffer(){
        this.packets.clear();
    }
}
