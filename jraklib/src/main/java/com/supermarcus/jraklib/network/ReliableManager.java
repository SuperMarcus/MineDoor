package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.SessionManager;
import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.lang.RecoveryDataPacket;
import com.supermarcus.jraklib.protocol.BinaryUtils;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.ACK;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.AcknowledgePacket;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.NACK;
import com.supermarcus.jraklib.protocol.raklib.data.DATA_PACKET_0;
import com.supermarcus.jraklib.protocol.raklib.data.DATA_PACKET_4;
import com.supermarcus.jraklib.protocol.raklib.data.DataPacket;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ReliableManager {
    public static int WINDOW_SIZE = 1024 * 2;

    private WeakReference<Session> ownedSession;

    private HashSet<Integer> ACKQueue = new HashSet<>();

    private HashSet<Integer> NACKQueue = new HashSet<>();

    private ConcurrentLinkedQueue<DataPacket> packetToSend = new ConcurrentLinkedQueue<>();

    private DATA_PACKET_4 sendQueue = new DATA_PACKET_4();

    private TreeMap<Integer, RecoveryDataPacket> recoveryQueue = new TreeMap<>();

    private HashMap<Integer, TreeSet<Integer>> needACK = new HashMap<>();

    private ArrayList<Integer> receivedWindow = new ArrayList<>();

    private HashMap<Short, TreeMap<Integer, EncapsulatedPacket>> splitPackets = new HashMap<>();

    private int windowStart = 0;

    private int windowEnd = ReliableManager.WINDOW_SIZE;

    private int reliableWindowStart = 0;

    private int reliableWindowEnd = ReliableManager.WINDOW_SIZE;

    private int lastReliableIndex = -1;

    private int sendSeqNumber = 0;

    private int lastSeqNumber = -1;

    private int messageIndex = 0;

    private int splitIndex = 0;

    private int splitID = 0;

    private int[] channelIndex = new int[32];

    private TreeMap<Integer, EncapsulatedPacket> reliableWindow = new TreeMap<>();

    private SessionManager manager;

    public ReliableManager(Session ownedSession, SessionManager manager){
        this.ownedSession = new WeakReference<>(ownedSession);
        this.manager = manager;

        for(int i = 0; i < 32; ++i){
            this.channelIndex[i] = 0;
        }
    }

    public void addEncapsulatedToQueue(EncapsulatedPacket packet, SendPriority flags){
        if(packet.needACK()){
            this.needACK.put(packet.getIdentifierACK(), new TreeSet<>());
        }

        if(packet.getReliability() == EncapsulatedPacket.RELIABLE ||
                packet.getReliability() == EncapsulatedPacket.RELIABLE_ORDERED ||
                packet.getReliability() == EncapsulatedPacket.RELIABLE_SEQUENCED ||
                packet.getReliability() == EncapsulatedPacket.RELIABLE_WITH_ACK_RECEIPT ||
                packet.getReliability() == EncapsulatedPacket.RELIABLE_ORDERED_WITH_ACK_RECEIPT){
            packet.setMessageIndex(this.messageIndex++);

            if(packet.getReliability() == EncapsulatedPacket.RELIABLE_ORDERED){
                packet.setOrderIndex(this.channelIndex[packet.getOrderChannel()]++);
            }
        }

        if((packet.getTotalLength() + 4) > this.getSession().getMtuSize()){
            int splitID = ++this.splitID % 65536;
            ByteBuffer buffer = ByteBuffer.wrap(packet.getBuffer());
            BinaryUtils utils = new BinaryUtils(buffer);
            ArrayList<byte[]> splits = new ArrayList<>();
            while(buffer.hasRemaining()){
                splits.add(utils.getBytes(this.getSession().getMtuSize() - 34));
            }
            for(int i = 0; i < splits.size(); ++i){
                EncapsulatedPacket splitEncapsulatedPacket = new EncapsulatedPacket();
                splitEncapsulatedPacket.setSplit(true);
                splitEncapsulatedPacket.setSplitID((short) splitID);
                splitEncapsulatedPacket.setReliability(packet.getReliability());
                splitEncapsulatedPacket.setSplitIndex(i);
                splitEncapsulatedPacket.setBuffer(splits.get(i));

                if(i > 0){
                    splitEncapsulatedPacket.setMessageIndex(this.messageIndex++);
                }else{
                    splitEncapsulatedPacket.setMessageIndex(packet.getMessageIndex());
                }

                if(splitEncapsulatedPacket.getReliability() == EncapsulatedPacket.RELIABLE_ORDERED){
                    splitEncapsulatedPacket.setOrderChannel(packet.getOrderChannel());
                    splitEncapsulatedPacket.setOrderIndex(packet.getOrderIndex());
                }

                this.addToQueue(splitEncapsulatedPacket, flags);
            }
        }else {
            this.addToQueue(packet, flags);
        }
    }

    public void addToQueue(EncapsulatedPacket packet, SendPriority priority){
        if(packet.needACK() && packet.getMessageIndex() != null){
            TreeSet<Integer> indexes;
            if(this.needACK.containsKey(packet.getIdentifierACK())){
                indexes = this.needACK.get(packet.getIdentifierACK());
            }else{
                indexes = new TreeSet<>();
                this.needACK.put(packet.getIdentifierACK(), indexes);
            }
            indexes.add(packet.getMessageIndex());
        }
        if(priority == SendPriority.IMMEDIATE){
            DATA_PACKET_0 send = new DATA_PACKET_0();
            send.setSeqNumber(this.sendSeqNumber++);
            this.sendQueue.addPacket(packet.needACK() ? new EncapsulatedPacket(packet) : packet);
            packet.setNeedACK(false);
            this.getSession().sendPacket(send);
            RecoveryDataPacket recoveryDataPacket = new RecoveryDataPacket(send, System.currentTimeMillis());
            this.recoveryQueue.put(recoveryDataPacket.getSeqNumber(), recoveryDataPacket);
            return;
        }
        if(this.sendQueue.getLength() + packet.getTotalLength() > this.getSession().getMtuSize()){
            this.sendQueue();
        }
        this.sendQueue.addPacket(packet.needACK() ? new EncapsulatedPacket(packet) : packet);
    }

    public void onSplit(EncapsulatedPacket packet){
        if(packet.getSplitCount() > 128){
            return;
        }

        TreeMap<Integer, EncapsulatedPacket> splitPackets;
        if(this.splitPackets.containsKey(packet.getSplitID())){
            splitPackets = this.splitPackets.get(packet.getSplitID());
        }else {
            splitPackets = new TreeMap<>();
            this.splitPackets.put(packet.getSplitID(), splitPackets);
        }
        splitPackets.put(packet.getSplitIndex(), packet);

        if(splitPackets.size() >= packet.getSplitCount()){
            ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_SIZE);

            for(EncapsulatedPacket pk : splitPackets.values()){
                buffer.put(pk.getBuffer());
            }

            EncapsulatedPacket pk = new EncapsulatedPacket();
            pk.setBuffer(Arrays.copyOf(buffer.array(), buffer.position()));

            this.getSession().handleEncapsulatedPacketRoute(pk);
        }
    }

    public void onAcknowledgement(AcknowledgePacket packet){
        if(packet instanceof ACK){
            for(Integer seq : packet.getPackets()){
                if(this.recoveryQueue.containsKey(seq)){
                    for(BinaryConvertible binPk : this.recoveryQueue.get(seq).getPacket().getPackets()){
                        if((binPk instanceof EncapsulatedPacket) && (((EncapsulatedPacket) binPk).needACK()) && (null != ((EncapsulatedPacket) binPk).getMessageIndex())){
                            this.needACK.get(((EncapsulatedPacket) binPk).getIdentifierACK()).remove(((EncapsulatedPacket) binPk).getMessageIndex());
                        }
                    }
                    this.recoveryQueue.remove(seq);
                }
            }
        }else if(packet instanceof NACK){
            for(Integer seq : packet.getPackets()){
                if(this.recoveryQueue.containsKey(seq)){
                    DataPacket pk = this.recoveryQueue.get(seq).getPacket();
                    pk.setSeqNumber(this.sendSeqNumber++);
                    this.packetToSend.add(pk);
                    this.recoveryQueue.remove(seq);
                }
            }
        }
    }

    public void onDataPacket(DataPacket packet){
        if((packet.getSeqNumber() < this.windowStart) || (packet.getSeqNumber() > this.windowEnd) || this.receivedWindow.contains(packet.getSeqNumber())){
            return;
        }

        int diff = packet.getSeqNumber() - this.lastSeqNumber;

        this.NACKQueue.remove(packet.getSeqNumber());
        this.ACKQueue.add(packet.getSeqNumber());
        this.receivedWindow.add(packet.getSeqNumber());

        if(diff != 1){
            for(int i = this.lastSeqNumber; i < packet.getSeqNumber(); ++i){
                this.NACKQueue.add(i);
            }
        }

        if(diff >= 1){
            this.lastSeqNumber = packet.getSeqNumber();
            this.windowStart += diff;
            this.windowEnd += diff;
        }

        for (BinaryConvertible encapsulatedPacket : packet.getPackets()){
            if(encapsulatedPacket instanceof EncapsulatedPacket){
                this.onEncapsulatedPacket((EncapsulatedPacket) encapsulatedPacket);
            }
        }
    }

    public void onEncapsulatedPacket(EncapsulatedPacket packet){
        if(packet.getMessageIndex() == null){
            this.getSession().handleEncapsulatedPacketRoute(packet);
        }else{
            if((packet.getMessageIndex() < this.reliableWindowStart) || (packet.getMessageIndex() > this.reliableWindowEnd)){
                return;
            }

            if((packet.getMessageIndex() - this.lastReliableIndex) == 1){
                this.lastReliableIndex++;
                this.reliableWindowStart++;
                this.reliableWindowEnd++;
                this.getSession().handleEncapsulatedPacketRoute(packet);

                if(!this.reliableWindow.isEmpty()){
                    final boolean[] valid = {true};
                    final HashSet<Integer> packetToRemove = new HashSet<>();

                    this.reliableWindow.forEach(new BiConsumer<Integer, EncapsulatedPacket>() {
                        @Override
                        public void accept(Integer index, EncapsulatedPacket packet) {
                            if(valid[0] && (index - lastReliableIndex) == 1){
                                ReliableManager.this.lastReliableIndex++;
                                ReliableManager.this.reliableWindowStart++;
                                ReliableManager.this.reliableWindowEnd++;
                                ReliableManager.this.getSession().handleEncapsulatedPacketRoute(packet);
                                packetToRemove.add(index);
                            }else{
                                valid[0] = false;
                            }
                        }
                    });

                    for(Integer index : packetToRemove){
                        this.reliableWindow.remove(index);
                    }
                }
            }
        }
    }

    public void onUpdate(final long millis){
        if(!this.ACKQueue.isEmpty()){
            ACK pk = new ACK();
            pk.addPackets(this.ACKQueue);
            this.getSession().sendPacket(pk);
            this.ACKQueue.clear();
        }

        if(!this.NACKQueue.isEmpty()){
            NACK pk = new NACK();
            pk.addPackets(this.NACKQueue);
            this.getSession().sendPacket(pk);
            this.NACKQueue.clear();
        }

        if(!this.packetToSend.isEmpty()){
            int limit = 16;
            while(((--limit) >= 0) && !this.packetToSend.isEmpty()){
                DataPacket pk = this.packetToSend.poll();
                RecoveryDataPacket rpk = new RecoveryDataPacket(pk, millis);
                this.recoveryQueue.put(rpk.getSeqNumber(), rpk);
                this.getSession().sendPacket(pk);
            }
            if(this.packetToSend.size() > ReliableManager.WINDOW_SIZE){
                this.packetToSend.clear();
            }
        }

        if(!this.needACK.isEmpty()){
            final HashSet<Integer> needToRemove = new HashSet<>();
            this.needACK.forEach(new BiConsumer<Integer, TreeSet<Integer>>() {
                public void accept(Integer identifier, TreeSet<Integer> indexes) {
                    if(indexes.isEmpty()){
                        needToRemove.add(identifier);
                    }
                }
            });
            for(Integer identifier : needToRemove){
                this.needACK.remove(identifier);
                this.manager.notifyACK(this.getSession(), identifier);
            }
        }

        if(!this.recoveryQueue.isEmpty()){
            final HashSet<RecoveryDataPacket> needToRecovery = new HashSet<>();
            this.recoveryQueue.forEach(new BiConsumer<Integer, RecoveryDataPacket>() {
                @Override
                public void accept(Integer seq, RecoveryDataPacket pk) {
                    if (pk.getSendTime() < (millis - 8000)) {
                        needToRecovery.add(pk);
                    }
                }
            });
            for(RecoveryDataPacket pk : needToRecovery){
                this.recoveryQueue.remove(pk.getSeqNumber());
                this.packetToSend.add(pk.getPacket());
            }
        }

        this.receivedWindow.removeIf(new Predicate<Integer>() {
            @Override
            public boolean test(Integer seq) {
                return (seq < ReliableManager.this.windowStart);
            }
        });

        this.sendQueue();
    }

    public void sendQueue(){
        if(this.sendQueue.countPackets() > 0){
            this.sendQueue.setSeqNumber(this.sendSeqNumber++);
            this.getSession().sendPacket(this.sendQueue);
            this.recoveryQueue.put(this.sendQueue.getSeqNumber(), new RecoveryDataPacket(this.sendQueue, System.currentTimeMillis()));
            this.sendQueue = new DATA_PACKET_4();
        }
    }

    private Session getSession(){
        return ownedSession.get();
    }
}
