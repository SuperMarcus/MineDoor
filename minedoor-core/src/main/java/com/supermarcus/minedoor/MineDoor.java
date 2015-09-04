package com.supermarcus.minedoor;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;
import com.supermarcus.minedoor.network.MinecraftInterface;
import com.supermarcus.minedoor.protocol.minecraftpe.StrangePacket;
import com.supermarcus.minedoor.transfer.ServerMonitor;
import com.supermarcus.minedoor.util.ArgumentDispatcher;
import com.supermarcus.minedoor.util.ServerProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;

public class MineDoor {
    public static void main(String args[]){
        try{
            LogManager.getLogger().info("Loading...");
            new MineDoor(args);
        }catch (Throwable e){
            LogManager.getLogger().fatal("An uncaught exception has been cached. MineDoor had just stop to protect your server.", e);
        }
    }

    private ArgumentDispatcher arguments;

    private ServerProperties serverProperties;

    private Logger logger = LogManager.getLogger();

    private MinecraftInterface minecraftInterface = new MinecraftInterface(this);

    private RakLibPacketHandler packetHandler = new RakLibPacketHandler(this);

    private ServerMonitor monitor = new ServerMonitor(this);

    private MineDoor(String[] args){
        this.arguments = new ArgumentDispatcher(args);
        this.serverProperties = new ServerProperties();
        this.getProperties().loadArgumentDispatcher(this.getArguments());

        try {
            this.getProperties().save();
        } catch (IOException e) {
            this.getLogger().warn("Unable to save configuration", e);
        }

        this.getLogger().info("Loading servers...");
        File servers = this.getArguments().getServerList();
        try {
            if(!servers.exists()){
                //noinspection ResultOfMethodCallIgnored
                servers.createNewFile();
                PrintWriter writer = new PrintWriter(new FileWriter(servers));
                writer.println("#MineDoor Monitoring server list");
                writer.println("#Servers list in this file will be monitored by MineDoor");
                writer.println("#e.g. 112.15.62.17:19132");
                writer.close();
            }
            BufferedReader reader = new BufferedReader(new FileReader(servers));
            String line;
            while ((line = reader.readLine()) != null){
                line = line.trim();
                if(line.startsWith("#") || line.isEmpty())continue;
                try{
                    String address = line;
                    short port = 19132;
                    if(line.contains(":")){
                        address = line.split(":")[0];
                        port = Short.parseShort(line.split(":")[1]);
                    }
                    this.monitor.addToMonitor(new InetSocketAddress(address, port));
                }catch (Exception e){
                    this.getLogger().info("Unable to parse server list", e);
                }
            }
            reader.close();
        } catch (IOException e) {
            this.getLogger().error("Unable to parse server list", e);
        }

        this.getLogger().info("Starting monitor...");
        ServerMonitor.PING_FREQUENCY = this.getProperties().getUpdateFrequency();
        this.monitor.start();
        this.getLogger().info("Monitor running at " + this.monitor.getSocket().getLocalSocketAddress());

        this.getLogger().info("Starting MineDoor at " + this.getProperties().getInterfaceAddress());
        this.getInterface().start(this.getProperties().getInterfaceAddress());
    }

    public ServerProperties getProperties(){
        return this.serverProperties;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public ArgumentDispatcher getArguments() {
        return this.arguments;
    }

    public MinecraftInterface getInterface() {
        return this.minecraftInterface;
    }

    public void onPlayerJoin(Session session){
        InetSocketAddress address = this.monitor.getBestServer().getAddress();
        this.getLogger().info("Transferring player '" + session.getAddress() + "' to address '" + address + "'");

        StrangePacket packet = new StrangePacket();
        packet.setDestination(address);

        EncapsulatedPacket reply = new EncapsulatedPacket();
        reply.setBuffer(packet);
        reply.setReliability(EncapsulatedPacket.RELIABLE_ORDERED);
        reply.setOrderIndex(0);
        reply.setOrderChannel(1);

        session.getReliableManager().addEncapsulatedToQueue(reply, SendPriority.IMMEDIATE);
    }

    public RakLibPacketHandler getPacketHandler() {
        return packetHandler;
    }
}
