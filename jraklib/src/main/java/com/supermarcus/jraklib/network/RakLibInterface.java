package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.SessionManager;
import com.supermarcus.jraklib.lang.message.server.*;
import com.supermarcus.jraklib.lang.message.session.SessionCloseMessage;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.protocol.raklib.PacketInfo;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PING;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PONG;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Thread interface
 */
public class RakLibInterface extends Thread{
    public static final int NETWORK_CALCULATE_PERIOD = 32;

    public static final long NORMAL_TICK = 50;

    public static final int MAX_PACKET_PER_TICK = 500;

    private ProtocolSocket socket;

    private SessionManager sessionManager;

    private boolean running = false;

    private boolean isShutdown = true;

    private long startTime = 0L;

    private int serverId;

    private long tickCounter = 0L;

    private ChildNetworkManager networkManager;

    public RakLibInterface(InetSocketAddress serverAddress, SessionManager manager, int serverId) throws SocketException {
        this.networkManager = new ChildNetworkManager(manager.getNetworkManager(), this);
        this.socket = new ProtocolSocket(serverAddress, this.getNetworkManager());
        this.sessionManager = manager;
        this.serverId = serverId;
        this.setName("RakLib - Interface Thread");
        this.start();
    }

    public void run(){
        this.running = true;
        this.isShutdown = false;
        synchronized (this){
            this.startTime = System.currentTimeMillis();
        }
        this.getSessionManager().queueMessage(new InterfaceStartMessage(this.getStartTimeMillis(), this));
        try{
            while(!this.isShutdown){
                long tickStart = System.currentTimeMillis();

                ++this.tickCounter;

                if(tickStart < this.startTime){//what???
                    synchronized (this){
                        this.startTime = tickStart;
                    }
                }

                this.onTick();

                try{
                    Thread.sleep(tickStart + RakLibInterface.NORMAL_TICK - System.currentTimeMillis());
                }catch (Exception ignore){}
            }
            this.getSocket().close();
        }catch (Throwable t){
            this.getSessionManager().queueMessage(new InterfaceInterruptMessage(t, this));
        }
        this.getSessionManager().queueMessage(new InterfaceShutdownMessage(System.currentTimeMillis(), this));
        this.running = false;
    }

    public boolean receivePacket(){
        ReceivedPacket packet = this.getSocket().readPacket();
        if(packet != null){
            byte[] buffer = packet.getRawData();
            PacketInfo identifier = PacketInfo.getById(buffer[0]);
            if(identifier != null){
                try{
                    Packet wrappedPacket = identifier.wrap(buffer);
                    wrappedPacket.decode();
                    if(identifier == PacketInfo.UNCONNECTED_PING){//No need to pass to a session
                        UNCONNECTED_PONG pong = new UNCONNECTED_PONG();
                        pong.setServerName(getSessionManager().getServerName());
                        pong.setServerID(getSessionManager().getServerId());
                        pong.setPingID(((UNCONNECTED_PING) wrappedPacket).getPingID());
                        pong.encode();
                        this.getSocket().writePacket(pong, packet.getSendAddress());
                    }else{
                        this.getSessionManager().getSessionMap().getSession(packet.getSendAddress(), this).handlePacket(wrappedPacket);
                    }
                }catch (Exception e){
                    e.printStackTrace();//TODO
                }
            }else{
                this.getSessionManager().queueRaw(new RawPacket(packet.getRawData(), packet.getSendAddress(), this));
            }
            return true;
        }
        return false;
    }

    /**
     * Main tick here
     */
    public void onTick() {
        int max = RakLibInterface.MAX_PACKET_PER_TICK;
        long startMillis = System.currentTimeMillis();

        while((max > 0) && this.receivePacket()){
            this.getSocket().flush();
            --max;
        }
        this.getSocket().flush();
        this.getSessionManager().getSessionMap().update(this, startMillis);

        if((this.getTick() % RakLibInterface.NETWORK_CALCULATE_PERIOD) == 0){
            this.getNetworkManager().doUpdate(System.currentTimeMillis());
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onAddressBlocked(InetAddress address, long millis){
        this.getSessionManager().queueMessage(new NetworkBlockedMessage(address, millis, this));
        for(Session s : this.getSessionManager().getSessionMap().findSessions(this)){
            if(s.getAddress().getAddress().equals(address)){
                s.close(SessionCloseMessage.Reason.NETWOEK_BLOCKED);
            }
        }
    }

    public boolean isRunning(){
        return this.running;
    }

    public void shutdown(){
        this.isShutdown = true;
    }

    public int getServerId(){
        return serverId;
    }

    public SessionManager getSessionManager(){
        return this.sessionManager;
    }

    public ProtocolSocket getSocket(){
        return this.socket;
    }

    public boolean isTerminated(){
        return !(this.getSocket().isAlive() && this.isAlive());
    }

    public long getStartTimeMillis(){
        return this.startTime;
    }

    public boolean equals(Object object){
        return (object instanceof RakLibInterface) && (((RakLibInterface) object).getServerId() == this.getServerId());
    }

    public long getTick(){
        return this.tickCounter;
    }

    public void finalize(){
        try {
            super.finalize();
            this.getSocket().close();
            this.interrupt();
        } catch (Throwable ignore) {}
    }

    public ChildNetworkManager getNetworkManager() {
        return networkManager;
    }
}
