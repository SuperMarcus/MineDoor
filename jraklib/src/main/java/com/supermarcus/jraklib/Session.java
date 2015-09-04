package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.lang.message.session.SessionCloseMessage;
import com.supermarcus.jraklib.lang.message.session.SessionCreateMessage;
import com.supermarcus.jraklib.lang.message.session.SessionOpenMessage;
import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.network.ReliableManager;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.*;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.AcknowledgePacket;
import com.supermarcus.jraklib.protocol.raklib.data.DataPacket;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class Session {
    public static final long UPDATE_TIMEOUT = 10 * 1000;

    public static final int MAX_MTU_SIZE = 1464;

    private InetSocketAddress address;

    private SessionManager manager;

    private boolean isActive = false;

    private WeakReference<RakLibInterface> ownedInterface;

    private State state = State.UNCONNECTED;

    private HashSet<EncapsulatedPacket> prejoinQueue = new HashSet<>();

    private long lastUpdate = System.currentTimeMillis();

    private long clientID = 0L;

    private int mtuSize = 548; //Min size

    private ReliableManager reliableManager;

    public Session(SessionManager manager, InetSocketAddress address, RakLibInterface ownedInterface){
        this.address = address;
        this.manager = manager;
        this.ownedInterface = new WeakReference<>(ownedInterface);
        this.reliableManager = new ReliableManager(this, manager);
        manager.queueMessage(new SessionCreateMessage(this));
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public RakLibInterface getOwnedInterface(){
        return this.ownedInterface.get();
    }

    public void handleEncapsulatedPacketRoute(EncapsulatedPacket packet){
        if(packet.hasSplit()){
            if(this.state == State.CONNECTED){
                this.getReliableManager().onSplit(packet);
            }
            return;
        }

        int id = packet.getBuffer()[0] & 0xff;
        if(id < 0x80){
            PacketInfo info = PacketInfo.getById(packet.getBuffer()[0]);
            if(info != null){
                if(this.state == State.CONNECTING_2){
                    EncapsulatedPacket reply;
                    switch (info){
                        case CLIENT_CONNECT_DataPacket:
                            CLIENT_CONNECT_DataPacket connectPacket = (CLIENT_CONNECT_DataPacket) info.wrap(packet.getBuffer());
                            connectPacket.decode();

                            SERVER_HANDSHAKE_DataPacket replyHandshake = new SERVER_HANDSHAKE_DataPacket();
                            replyHandshake.setAddress(this.getAddress());
                            replyHandshake.setSendPing(connectPacket.getSendPing() + 1000L);
                            replyHandshake.encode();

                            reply = new EncapsulatedPacket();
                            reply.setReliability(EncapsulatedPacket.UNRELIABLE);
                            reply.setBuffer(replyHandshake);

                            this.getReliableManager().addToQueue(reply, SendPriority.IMMEDIATE);
                            break;
                        case CLIENT_HANDSHAKE_DataPacket:
                            CLIENT_HANDSHAKE_DataPacket handshake = (CLIENT_HANDSHAKE_DataPacket) info.wrap(packet.getBuffer());
                            handshake.decode();

                            if(handshake.getAddress().getPort() == this.getAddress().getPort() || !this.manager.isPortChecking()){
                                this.state = State.CONNECTED;
                                this.manager.queueMessage(new SessionOpenMessage(this));
                                for(EncapsulatedPacket preEncapsulated : this.prejoinQueue){
                                    this.manager.queueEncapsulated(this, preEncapsulated);
                                }
                                this.prejoinQueue.clear();
                            }
                            break;
                    }
                }else if (info == PacketInfo.CLIENT_DISCONNECT_DataPacket){
                    this.close(SessionCloseMessage.Reason.CLIENT_DISCONNECT);
                }else if (info == PacketInfo.PING_DataPacket){
                    PING_DataPacket ping = (PING_DataPacket) info.wrap(packet.getBuffer());
                    ping.decode();

                    PONG_DataPacket pong = new PONG_DataPacket();
                    pong.setPingID(ping.getPingID());
                    pong.encode();

                    EncapsulatedPacket reply = new EncapsulatedPacket();
                    reply.setReliability(EncapsulatedPacket.UNRELIABLE);
                    reply.setBuffer(pong);

                    this.getReliableManager().addToQueue(reply, SendPriority.NORMAL);
                }
            }
        }else if(this.state == State.CONNECTED){
            this.manager.queueEncapsulated(this, packet);
        }else{
            this.prejoinQueue.add(packet);
        }
    }

    public void handlePacket(Packet packet){
        this.isActive = true;
        this.lastUpdate = System.currentTimeMillis();
        if((this.state == State.CONNECTED) || (this.state == State.CONNECTING_2)){
            if(packet instanceof DataPacket){
                this.getReliableManager().onDataPacket((DataPacket) packet);
            }else if(packet instanceof AcknowledgePacket){
                this.getReliableManager().onAcknowledgement((AcknowledgePacket) packet);
            }
        }

        if(packet.getNetworkID() > 0x00){
            if(this.state == State.UNCONNECTED && packet instanceof OPEN_CONNECTION_REQUEST_1){
                OPEN_CONNECTION_REPLY_1 reply = new OPEN_CONNECTION_REPLY_1();
                reply.setMtuSize(((OPEN_CONNECTION_REQUEST_1) packet).getMtuSize());
                reply.setServerID(this.manager.getServerId());
                this.sendPacket(reply);
                this.state = State.CONNECTING_1;
            }else if(this.state == State.CONNECTING_1 && packet instanceof OPEN_CONNECTION_REQUEST_2){
                this.clientID = ((OPEN_CONNECTION_REQUEST_2) packet).getClientID();
                if((((OPEN_CONNECTION_REQUEST_2) packet).getServerAddress().getPort() == this.getOwnedInterface().getSocket().getPort()) || !this.manager.isPortChecking()){
                    this.setMtuSize(Math.min(Math.abs(((OPEN_CONNECTION_REQUEST_2) packet).getMtuSize()), Session.MAX_MTU_SIZE));
                    OPEN_CONNECTION_REPLY_2 reply = new OPEN_CONNECTION_REPLY_2();
                    reply.setMtuSize(this.getMtuSize());
                    reply.setServerID(this.manager.getServerId());
                    reply.setClientAddress(this.getAddress());
                    this.sendPacket(reply);
                    this.state = State.CONNECTING_2;
                }
            }
        }
    }

    public void update(long millis){
        try{
            RakLibInterface rakLibInterface;

            try{
                rakLibInterface = this.getOwnedInterface();

                if(rakLibInterface == null){
                    this.close();
                    return;
                }
            }catch (Exception ignore){}

            if(!this.isActive && ((this.lastUpdate + Session.UPDATE_TIMEOUT) < millis)){
                this.close(SessionCloseMessage.Reason.TIMEOUT);
            }

            this.isActive = false;

            this.getReliableManager().onUpdate(millis);
        }catch (Exception e){
            e.printStackTrace();//TODO
        }//TODO: Add a message or something?
    }

    public void close(){
        this.manager.getSessionMap().removeSession(this.getAddress());
    }

    public void close(SessionCloseMessage.Reason reason){
        this.manager.queueMessage(new SessionCloseMessage(reason, this));
        this.manager.getSessionMap().removeSession(this.getAddress());
    }

    public void sendPacket(Packet pk){
        this.sendPacket(pk, SendPriority.NORMAL);
    }

    public void sendPacket(Packet pk, SendPriority priority){
        pk.encode();
        this.sendPacket((BinaryConvertible) pk, priority);
    }

    public void sendPacket(BinaryConvertible pk, SendPriority priority){
        this.getOwnedInterface().getSocket().writePacket(pk, this.getAddress(), priority);
    }

    public long getClientID() {
        return clientID;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }

    public ReliableManager getReliableManager() {
        return reliableManager;
    }

    public enum State {
        UNCONNECTED(0),
        CONNECTING_1(1),
        CONNECTING_2(2),
        CONNECTED(3);

        private int value;

        State(int value){
            this.value = value;
        }

        public int getState(){
            return this.value;
        }
    }
}
