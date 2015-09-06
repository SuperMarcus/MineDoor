package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    public static final long CLIENT_PACKET_LIMIT = 500;

    public static final int CALCULATE_MAX_QUEUE = 64;

    private long sendBytes = 0L;

    private long receivedBytes = 0L;

    private long lastSendBytes = 0L;

    private long lastReceivedBytes = 0L;

    private long lastCalculated = 0L;

    private double receiveSpeed = 0;

    private double sendSpeed = 0;

    private ConcurrentLinkedQueue<Double> sendCalculateQueue = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<Double> receiveCalculateQueue = new ConcurrentLinkedQueue<>();

    private ConcurrentHashMap<InetAddress, Long> blockAddresses = new ConcurrentHashMap<>();

    public void blockAddress(InetAddress address, long millis){
        this.blockAddressTill(address, (System.currentTimeMillis() + millis));
    }

    public void blockAddressTill(InetAddress address, long millis){
        this.blockAddresses.put(address, millis);
    }

    public void unblockAddress(InetAddress address){
        this.blockAddresses.remove(address);
    }

    public boolean isAddressBlocked(InetAddress address){
        return this.blockAddresses.containsKey(address);
    }

    public Map<InetAddress, Long> getBlockedAddresses(){
        return this.blockAddresses;
    }

    public void doUpdate(long millis){
        synchronized (this){
            long timeSpend = millis - this.lastCalculated;

            if(timeSpend > 0){
                double lastReceivedBytesPerSec = ((double) (this.lastReceivedBytes * 1000)) / ((double) timeSpend);
                double lastSendBytesPerSec = ((double) (this.lastSendBytes * 1000)) / ((double) timeSpend);

                this.sendCalculateQueue.offer(lastSendBytesPerSec);
                this.receiveCalculateQueue.offer(lastReceivedBytesPerSec);

                if(this.sendCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE || this.receiveCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                    while(this.sendCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                        this.sendCalculateQueue.poll();
                    }
                    while(this.receiveCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                        this.receiveCalculateQueue.poll();
                    }
                }

                double bytesSum = 0;
                for(Double cSendBytes : this.sendCalculateQueue){
                    bytesSum += cSendBytes;
                }
                this.sendSpeed = bytesSum / (double) this.sendCalculateQueue.size();

                bytesSum = 0;
                for(Double cReceiveBytes : this.receiveCalculateQueue){
                    bytesSum += cReceiveBytes;
                }
                this.receiveSpeed = bytesSum / (double) this.receiveCalculateQueue.size();

                this.lastReceivedBytes = 0;
                this.lastSendBytes = 0;

                if((this.sendBytes > (Long.MAX_VALUE - (Packet.MAX_SIZE * 16))) || (this.receivedBytes > (Long.MAX_VALUE - (Packet.MAX_SIZE * 16)))){
                    this.clearCounter0();
                }

                HashSet<InetAddress> needToUnblock = new HashSet<>();
                for(Map.Entry<InetAddress, Long> entry : this.getBlockedAddresses().entrySet()){
                    if(entry.getValue() != -1 && entry.getValue() <= millis){
                        needToUnblock.add(entry.getKey());
                    }
                }
                for(InetAddress unblockAddress : needToUnblock){
                    this.unblockAddress(unblockAddress);
                }
            }

            this.lastCalculated = millis;
        }
    }

    protected void addSendBytes(long bytes){
        synchronized (this){
            this.sendBytes += bytes;
            this.lastSendBytes += bytes;
        }
    }

    protected void addReceivedBytes(long bytes){
        synchronized (this){
            this.receivedBytes += bytes;
            this.lastReceivedBytes += bytes;
        }
    }

    public void clearCounter(){
        synchronized (this){
            this.clearCounter0();
        }
    }

    public long getSendedBytes() {
        return sendBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public double getSendSpeed(){
        return sendSpeed;
    }

    public double getReceiveSpeed(){
        return receiveSpeed;
    }

    /**
     * While holding lock
     */
    private void clearCounter0(){
        this.receiveCalculateQueue.clear();
        this.sendCalculateQueue.clear();
        this.sendBytes = 0;
        this.receivedBytes = 0;
        this.lastSendBytes = 0;
        this.lastReceivedBytes = 0;
        this.sendSpeed = 0;
        this.receiveSpeed = 0;
    }
}
