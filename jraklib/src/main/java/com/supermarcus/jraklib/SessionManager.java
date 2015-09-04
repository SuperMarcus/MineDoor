package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.lang.QueuedEncapsulated;
import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;
import com.supermarcus.jraklib.lang.message.RakLibMessage;
import com.supermarcus.jraklib.lang.message.major.MainThreadExceptionMessage;
import com.supermarcus.jraklib.lang.message.major.UncaughtMainThreadExceptionMessage;
import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class SessionManager extends Thread {
    public static final int MAX_SERVER_INTERFACES = 25;

    private ReentrantLock threadLock = new ReentrantLock(true);

    private boolean isShutdown = false;

    private ConcurrentLinkedQueue<RakLibMessage> messages = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<RawPacket> rawPackets = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<ACKNotification> ackNotifications = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<QueuedEncapsulated> encapsulatedQueue = new ConcurrentLinkedQueue<>();

    private int runningServer = 0;

    private RakLibInterface[] interfaces = new RakLibInterface[SessionManager.MAX_SERVER_INTERFACES];

    private LinkedList<MessageHandler> messageHandlers = new LinkedList<>();

    private PacketHandler handler = null;

    private SessionMap map = new SessionMap();

    private long serverId = 1L;

    private boolean portChecking = false;

    volatile private String serverName = "MCPE;Minecraft Server;27;0.11.0;0;60";

    public SessionManager(){
        this.setName("RakLib - Main Thread");
        this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                queueMessage(new UncaughtMainThreadExceptionMessage(t, e));
            }
        });
        this.start();
    }

    public void run(){
        while(!this.isShutdown()){
            try{
                this.processMessages();
                this.processRawPacket();
                this.processACKNotification();
                this.processEncapsulated();
            }catch (Throwable t){
                this.queueMessage(new MainThreadExceptionMessage(this, t));
            }
        }
        synchronized (this){
            for(RakLibInterface i : this.getInterfaces()){
                i.shutdown();
                try{
                    i.join(1000 * 5);
                }catch (InterruptedException ignore){}
                if(!i.isTerminated()){
                    i.interrupt();
                }
            }
        }
    }

    public void notifyACK(Session session, Integer identifier){
        this.ackNotifications.add(new ACKNotification(session, identifier));
    }

    public SessionMap getSessionMap(){
        return this.map;
    }

    public void setServerName(String name){
        this.serverName = name;
    }

    public String getServerName(){
        return this.serverName;
    }

    public long getServerId(){
        return this.serverId;
    }

    private void processMessages(){
        RakLibMessage message = messages.poll();
        while(message != null){
            try{
                this.fireMessage(message);
            }catch (Exception ignore){}
            message = messages.poll();
        }
    }

    private void processRawPacket(){
        RawPacket raw = rawPackets.poll();
        while(raw != null){
            try{
                if(this.handler != null){
                    this.handler.onRawPacket(raw);
                }
            }catch (Exception ignore){}
            raw = rawPackets.poll();
        }
    }

    private void processACKNotification(){
        ACKNotification n = ackNotifications.poll();
        while(n != null){
            try{
                if(this.handler != null){
                    this.handler.onACKNotification(n);
                }
            }catch (Exception ignore){}
            n = ackNotifications.poll();
        }
    }

    private void processEncapsulated(){
        QueuedEncapsulated e = encapsulatedQueue.poll();
        while(e != null){
            try{
                if(this.handler != null){
                    this.handler.onEncapsulated(e.getSession(), e.getPacket(), e.getFlags());
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            e = encapsulatedQueue.poll();
        }
    }

    private void fireMessage(RakLibMessage message){
        for(MessageHandler handler : this.messageHandlers){
            handler.onMessage(message);
        }
    }

    public void addMessageHandler(MessageHandler handler){
        Objects.requireNonNull(handler);
        this.messageHandlers.add(handler);
    }

    public RakLibInterface getInterface(int id){
        return this.interfaces[id];
    }

    public RakLibInterface[] getInterfaces(){
        RakLibInterface[] interfaces = new RakLibInterface[this.runningServer];
        int o = 0;
        for (RakLibInterface anInterface : this.interfaces) {
            if (anInterface != null) {
                interfaces[o] = anInterface;
            }
        }
        return interfaces;
    }

    public void queueMessage(RakLibMessage message){
        this.messages.offer(message);
    }

    public void queueRaw(RawPacket pk){
        this.rawPackets.offer(pk);
    }

    public void queueEncapsulated(Session session, EncapsulatedPacket packet){
        this.queueEncapsulated(session, packet, SendPriority.NORMAL.getValue());
    }

    public void queueEncapsulated(Session session, EncapsulatedPacket packet, int flags){
        this.encapsulatedQueue.offer(new QueuedEncapsulated(session, packet, flags));
    }

    public boolean collectInterfaces(boolean force){
        boolean didCollect = false;
        if(force || this.threadLock.tryLock()){
            int running = 0;
            for(int i = 0; i < this.interfaces.length; ++i){
                if(this.interfaces[i] != null){
                    if(this.interfaces[i].isTerminated()){
                        this.interfaces[i] = null;
                        didCollect = true;
                    }else {
                        ++running;
                    }
                }
            }
            this.runningServer = running;
            if(!force)this.threadLock.unlock();
        }
        return didCollect;
    }

    public RakLibInterface addInterface(InetSocketAddress bindAddress) throws SocketException, InterfaceOutOfPoolSizeException {
        this.threadLock.lock();
        this.collectInterfaces(true);
        int id = nextInterfaceId();
        if(id < 0){
            throw new InterfaceOutOfPoolSizeException("try to add interface but pool size is " + this.interfaces.length);
        }
        RakLibInterface server = new RakLibInterface(bindAddress, this, id);
        this.interfaces[id] = server;
        ++this.runningServer;
        this.threadLock.unlock();
        return server;
    }

    public void setPacketHandler(PacketHandler handler){
        this.handler = handler;
    }

    public void shutdown(){
        this.isShutdown = true;
        try {
            this.join();
        } catch (InterruptedException ignore) {}
    }

    public boolean isShutdown(){
        return this.isShutdown;
    }

    private int nextInterfaceId() {
        for(int i = 0; i < this.interfaces.length; ++i){
            if(this.interfaces[i] == null){
                return i;
            }
        }
        return -1;
    }

    public boolean isPortChecking() {
        return portChecking;
    }

    public void setPortChecking(boolean portChecking) {
        this.portChecking = portChecking;
    }

    public class SessionMap extends ConcurrentHashMap<InetSocketAddress, Session> {
        public Session getSession(InetSocketAddress address, RakLibInterface rakLibInterface){
            synchronized (this){
                Session session;
                if(this.containsKey(address)){
                    session = this.get(address);
                }else{
                    session = new Session(SessionManager.this, address, rakLibInterface);
                    this.put(address, session);
                }
                return session;
            }
        }

        public void removeSession(InetSocketAddress address){
            synchronized (this){
                if(this.containsKey(address)){
                    this.remove(address);
                }
            }
        }

        public Session[] findSessions(final RakLibInterface rakLibInterface){
            synchronized (this){
                final ArrayList<Session> sessions = new ArrayList<>();
                this.forEach(new BiConsumer<InetSocketAddress, Session>() {
                    @Override
                    public void accept(InetSocketAddress address, Session session) {
                        if(session.getOwnedInterface().equals(rakLibInterface)){
                            sessions.add(session);
                        }
                    }
                });
                return sessions.toArray(new Session[sessions.size()]);
            }
        }

        public void update(RakLibInterface rakLibInterface, long millis){
            Session[] sessions = this.findSessions(rakLibInterface);
            for(Session session : sessions){
                session.update(millis);
            }
        }
    }
}
