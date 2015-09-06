package com.supermarcus.jraklib.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChildNetworkManager extends NetworkManager {
    private NetworkManager parent;

    private RakLibInterface owner;

    private ConcurrentHashMap<InetSocketAddress, Long> trafficMonitor = new ConcurrentHashMap<>();

    private long lastTrafficMeasure = 0;

    public ChildNetworkManager(NetworkManager parent, RakLibInterface owner){
        this.parent = parent;
        this.owner = owner;
    }

    public void onSocketSend(long sendBytes){
        this.addSendBytes(sendBytes);
    }

    public void onSocketRead(long readBytes, InetSocketAddress from){
        this.addReceivedBytes(readBytes);

        if(this.trafficMonitor.containsKey(from)){
            this.trafficMonitor.put(from, this.trafficMonitor.get(from) + 1);
        }else{
            this.trafficMonitor.put(from, readBytes);
        }
    }

    public void doUpdate(long millis){
        super.doUpdate(millis);

        synchronized (this){
            if(this.lastTrafficMeasure == 0){
                this.lastTrafficMeasure = millis;
                return;
            }

            long duration = millis - this.lastTrafficMeasure;
            if(duration > 0){
                for(Map.Entry<InetSocketAddress, Long> e : this.trafficMonitor.entrySet()){
                    if(e.getValue() >= NetworkManager.CLIENT_PACKET_LIMIT){
                        long blockMillis = (long)(((double) e.getValue() / (double) NetworkManager.CLIENT_PACKET_LIMIT) * 3000D);
                        this.blockAddress(e.getKey().getAddress(), blockMillis);
                        this.getOwner().onAddressBlocked(e.getKey().getAddress(), blockMillis);
                        if(blockMillis >= (NetworkManager.CLIENT_PACKET_LIMIT * 4)){
                            this.getParent().blockAddress(e.getKey().getAddress(), blockMillis);
                        }
                    }
                }
                this.trafficMonitor.clear();
            }

            this.lastTrafficMeasure = millis;
        }
    }

    public boolean isAddressBlocked(InetAddress address){
        return super.isAddressBlocked(address) || this.getParent().isAddressBlocked(address);
    }

    protected void addSendBytes(long bytes){
        super.addSendBytes(bytes);
        this.getParent().addSendBytes(bytes);
    }

    protected void addReceivedBytes(long bytes){
        super.addReceivedBytes(bytes);
        this.getParent().addReceivedBytes(bytes);
    }

    public NetworkManager getParent() {
        return parent;
    }

    public RakLibInterface getOwner() {
        return owner;
    }
}
