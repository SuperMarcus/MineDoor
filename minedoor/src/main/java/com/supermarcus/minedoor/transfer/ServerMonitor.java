package com.supermarcus.minedoor.transfer;

import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.PacketInfo;
import com.supermarcus.minedoor.MineDoor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ServerMonitor {
    public static long PING_FREQUENCY;

    private MineDoor mineDoor;

    private DatagramSocket socket;

    private SocketBlockingThread receiveThread = new SocketBlockingThread();

    private ServerMonitoringThread monitoringThread = new ServerMonitoringThread();

    private Logger logger = LogManager.getLogger();

    private ConcurrentHashMap<InetSocketAddress, TransferServer> servers = new ConcurrentHashMap<>();

    public ServerMonitor(MineDoor mineDoor){
        this.mineDoor = mineDoor;
    }

    public void start(){
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            this.getLogger().fatal("Unable to initialize monitor socket", e);
        }
        this.receiveThread.start();
        this.monitoringThread.start();
    }

    public DatagramSocket getSocket(){
        return this.socket;
    }

    public TransferServer getBestServer(){
        TransferServer server = null;
        long highestPoint = 0;
        for(Map.Entry<InetSocketAddress, TransferServer> entry : this.getMonitoringServers().entrySet()){
            if(entry.getValue().getTransferPoint() > highestPoint){
                highestPoint = entry.getValue().getTransferPoint();
                server = entry.getValue();
            }
        }
        return server;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public void addToMonitor(InetSocketAddress target){
        this.getMonitoringServers().put(target, new TransferServer(target, this));
    }

    public ConcurrentHashMap<InetSocketAddress, TransferServer> getMonitoringServers(){
        return this.servers;
    }

    private class ServerMonitoringThread extends Thread {

        public ServerMonitoringThread(){
            this.setName("ServerMonitor - Monitoring thread");
            this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    getLogger().fatal("An uncaught exception has been thrown", e);
                }
            });
        }

        public void run(){
            while(getSocket() != null && getSocket().isBound()){
                final long millis = System.currentTimeMillis();
                getLogger().info("Starting to refresh servers...");
                final int[] playerCounter = new int[]{0, 0};
                getMonitoringServers().forEach(new BiConsumer<InetSocketAddress, TransferServer>() {
                    @Override
                    public void accept(InetSocketAddress address, TransferServer target) {
                        target.onUpdate(millis);
                        playerCounter[0] += target.getPlayerCount();
                        playerCounter[1] += target.getMaxPlayer();
                    }
                });
                mineDoor.getInterface().updatePlayerCounter(playerCounter);
                try {
                    Thread.sleep(PING_FREQUENCY);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    private class SocketBlockingThread extends Thread{
        public static final int MAX_RECEIVE_PACKET_SIZE = 256;

        public SocketBlockingThread(){
            this.setName("ServerMonitor - Socket receive thread");
            this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    getLogger().fatal("An uncaught exception has been thrown", e);
                }
            });
        }

        public void run(){
            while(getSocket() != null && getSocket().isBound()){
                byte[] buffer = new byte[MAX_RECEIVE_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, MAX_RECEIVE_PACKET_SIZE);
                try {
                    getSocket().receive(packet);
                } catch (IOException ignore) {}

                //noinspection SuspiciousMethodCalls
                if(packet.getLength() > 0 && getMonitoringServers().containsKey(packet.getSocketAddress())){
                    PacketInfo info = PacketInfo.getById(buffer[0]);
                    if(info != null){
                        Packet pk = info.wrap(buffer);
                        pk.decode();
                        //noinspection SuspiciousMethodCalls
                        getMonitoringServers().get(packet.getSocketAddress()).onPacket(pk);
                    }
                }
            }
        }
    }
}
