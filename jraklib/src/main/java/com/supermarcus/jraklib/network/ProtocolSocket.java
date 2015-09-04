package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.protocol.Packet;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * UDP Socket for Minecraft: Pocket Edition network protocol
 */
public class ProtocolSocket extends DatagramSocket {

    private SocketSendReceiveThread thread = new SocketSendReceiveThread();

    public ProtocolSocket(SocketAddress bindAddress) throws SocketException {
        super(bindAddress);
        this.setSendBufferSize(Packet.MAX_SIZE);
        this.setReceiveBufferSize(Packet.MAX_SIZE);
        this.thread.start();
    }

    /**
     * To queue a packet to send buffer
     *
     * @param packet Packet to send
     */
    public void writePacket(QueuePacket packet){
        this.thread.send(packet);
    }

    /**
     * To send a packet
     *
     * @param packet Packet to send
     * @param target Target address
     * @param priority Send priority
     */
    public void writePacket(BinaryConvertible packet, SocketAddress target, SendPriority priority){
        try{
            byte[] data = packet.toBinary();
            DatagramPacket dPacket = new DatagramPacket(data, data.length, target);
            this.writePacket(new QueuePacket(dPacket, priority));
        }catch (Exception ignore){}
    }

    /**
     * To send a packet use normal priority
     *
     * @param packet Packet to send
     * @param target Target address
     */
    public void writePacket(BinaryConvertible packet, SocketAddress target){
        this.writePacket(packet, target, SendPriority.NORMAL);
    }

    /**
     * To receive a packet
     *
     * @return Packet received
     */
    public ReceivedPacket readPacket(){
        return this.thread.receive();
    }

    public void flush(){
        this.thread.flush();
    }

    /**
     * Real send
     *
     * @param packet Instance of DatagramPacket
     * @throws IOException
     */
    private void writePacket(DatagramPacket packet) throws IOException {
        this.send(packet);
    }

    public boolean isAlive(){
        return !this.isClosed() && this.isBound();
    }

    public void close(){
        this.thread.shutdown();
        super.close();
    }

    private class SocketSendReceiveThread extends Thread{
        private ConcurrentLinkedQueue<ReceivedPacket> receiveBuffer = new ConcurrentLinkedQueue<>();

        private PriorityBlockingQueue<QueuePacket> sendBuffer = new PriorityBlockingQueue<>();

        private boolean running = true;

        public SocketSendReceiveThread(){
            this.setName("RakLib Protocol Socket - " + ProtocolSocket.this.getLocalSocketAddress() + ":" + ProtocolSocket.this.getLocalPort());
        }

        public void flush(){
            try{
                while (!this.sendBuffer.isEmpty()){
                    QueuePacket packet = this.sendBuffer.poll();
                    ProtocolSocket.this.writePacket(packet.getPacket());
                }
            }catch (Exception ignore){}
        }

        public void run(){
            while(this.running){
                try{
                    DatagramPacket dPacket = new DatagramPacket(new byte[Packet.MAX_SIZE], Packet.MAX_SIZE);
                    ProtocolSocket.this.receive(dPacket);
                    if(dPacket.getLength() > 0){
                        this.receiveBuffer.add(new ReceivedPacket(dPacket));
                    }
                }catch (Exception ignore){}
            }
        }

        public ReceivedPacket receive(){
            return receiveBuffer.poll();
        }

        public void send(QueuePacket packet){
            this.sendBuffer.offer(packet);
        }

        public void shutdown(){
            this.running = false;
            try {
                this.notifyAll();
                this.join();
            } catch (InterruptedException ignore) {}
        }
    }
}
