package com.supermarcus.minedoor.transfer;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PING;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PONG;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

public class TransferServer {
    private InetSocketAddress address;

    private ServerMonitor monitor;

    private int lastPingID = -1;

    private long[] pingTimeTable = new long[1024];

    private long[] replyTimeTable = new long[1024];

    private long sendedPing = 0;

    private long receivedPong = 0;

    private String name = "";

    private long serverId = 0L;

    private ReentrantLock lock = new ReentrantLock();

    private double delay = 0;

    private int protocol = -1;

    private int playerCount = 0;

    private int maxPlayer = 0;

    private String version = "";

    private boolean isUp = false;

    public TransferServer(InetSocketAddress address, ServerMonitor monitor){
        this.address = address;
        this.monitor = monitor;

        for(int i = 0; i < 1024; ++i){
            pingTimeTable[i] = -1;
            replyTimeTable[i] = -1;
        }
    }

    public void onPacket(Packet packet){
        this.isUp = true;
        if(packet instanceof UNCONNECTED_PONG){
            this.lock.lock();
            String name = ((UNCONNECTED_PONG) packet).getServerName();
            if(name.startsWith("MCPE")){
                try{
                    name = name.replaceAll("\\\\;", "%{replaced_semicolon}");
                    String[] splits = name.split(";");
                    this.name = splits[1].replaceAll("%\\{replaced_semicolon}", ";");
                    this.protocol = Integer.parseInt(splits[2]);
                    this.version = splits[3].replaceAll("%\\{replaced_semicolon}", ";");
                    this.playerCount = Integer.parseInt(splits[4]);
                    this.maxPlayer = Integer.parseInt(splits[5]);

                    this.getMonitor().getLogger().info("Server '" + this.getAddress() + "' is online. Player count: " + this.getPlayerCount() + "/" + this.getMaxPlayer() + ", version: " + this.getVersion() + ", delay: " + this.getDelay());
                }catch (Exception e){
                    this.name = name;
                }
            }else{
                this.name = name;
            }
            this.serverId = ((UNCONNECTED_PONG) packet).getServerID();
            if(((UNCONNECTED_PONG) packet).getPingID() >= 0 && ((UNCONNECTED_PONG) packet).getPingID() < 128){
                if(this.replyTimeTable[((int) ((UNCONNECTED_PONG) packet).getPingID())] == -1){
                    ++this.receivedPong;
                    this.replyTimeTable[((int) ((UNCONNECTED_PONG) packet).getPingID())] = System.currentTimeMillis();
                }
            }
            this.lock.unlock();
        }
    }

    public void onUpdate(long millis){
        this.lock.lock();

        this.isUp = false;
        this.lastPingID = (++this.lastPingID) % 1024;

        UNCONNECTED_PING ping = new UNCONNECTED_PING();
        ping.setPingID(this.lastPingID);
        this.sendPacket(ping);

        ++this.sendedPing;
        this.pingTimeTable[this.lastPingID] = millis;
        this.replyTimeTable[this.lastPingID] = -1;

        long lag = 0;
        int calculated = 0;
        for(int i = 0; (i < 128) && (this.replyTimeTable[i] != -1); ++i){
            lag += (this.replyTimeTable[i] - this.pingTimeTable[i]);
            ++calculated;
        }
        this.delay = ((double) lag) / ((double) calculated);

        this.lock.unlock();
    }

    public void sendPacket(Packet packet){
        packet.encode();
        this.sendPacket((BinaryConvertible) packet);
    }

    public void sendPacket(BinaryConvertible data){
        byte[] dataBytes = data.toBinary();
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, this.getAddress());
        try {
            this.getMonitor().getSocket().send(packet);
        } catch (IOException e) {
            this.getMonitor().getLogger().error("Unable to send packet to server: " + this.getAddress(), e);
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public ServerMonitor getMonitor() {
        return monitor;
    }

    public double getDelay() {
        return delay;
    }

    public String getName() {
        return name;
    }

    public long getServerId() {
        return serverId;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public String getVersion() {
        return version;
    }

    public double getLoosePercentage(){
        return ((double)(this.sendedPing - this.receivedPong) / (double) this.sendedPing);
    }

    public long getTransferPoint(){
        return this.isUp ? (long)(((double)(getMaxPlayer() - getPlayerCount()) / (double) getMaxPlayer()) * 13000D) + (long)(5000 - Math.min(getDelay(), 5000D)) + (long)((1D - getLoosePercentage()) * 5000D) : 1;
    }
}
