package com.supermarcus.test;

import com.supermarcus.jraklib.MessageHandler;
import com.supermarcus.jraklib.PacketHandler;
import com.supermarcus.jraklib.RakLibServerInstance;
import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.lang.message.RakLibMessage;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;
import com.supermarcus.test.protocol.BatchPacket;
import com.supermarcus.test.protocol.StrangePacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RakLibServerTest extends RakLibServerInstance {
    public static long TEST_TIMEOUT = 10 * 1000;

    public static void main(String args[]){
        new RakLibServerTest();
    }

    public RakLibServerTest(){
        try {
            this.addMessageHandler(new MessageHandler() {
                @Override
                public void onMessage(RakLibMessage message) {
                    System.out.println(message);
                }
            });
            this.getSessionManager().setPacketHandler(new PacketHandler() {
                @Override
                public void onRawPacket(RawPacket packet) {
                    System.out.println("New Raw Packet #" + (packet.getData()[0] & 0xff));
                }

                @Override
                public void onACKNotification(ACKNotification notification) {
                    System.out.println("New ACK Notification");
                }

                @Override
                public void onEncapsulated(Session session, EncapsulatedPacket packet, int flags) {
                    System.out.println("New Encapsulated packet #" + (packet.getBuffer()[0] & 0xff) + " from " + session.getAddress());
                    for(byte b : packet.getBuffer()){
                        System.out.print((b & 0xff) + " ");
                    }
                    System.out.println();
                    if ((packet.getBuffer()[0] & 0xff) == BatchPacket.NETWORK_ID) {
                        BatchPacket dataPacket = BatchPacket.fromBinary(packet.getBuffer());
                        StrangePacket dp = new StrangePacket();
                        try {
                            dp.setDestination(new InetSocketAddress(InetAddress.getByName("spleef.lbsg.net"), 19132));
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        EncapsulatedPacket reply = new EncapsulatedPacket();
                        reply.setBuffer(dp);
                        reply.setReliability(EncapsulatedPacket.RELIABLE_ORDERED);
                        reply.setOrderIndex(0);
                        reply.setOrderChannel(1);

                        session.getReliableManager().addEncapsulatedToQueue(reply, SendPriority.IMMEDIATE);
                    }
                }
            });
            this.getSessionManager().addInterface(new InetSocketAddress("0.0.0.0", 19132));

            System.out.println("Test is running...");
            long startMillis = System.currentTimeMillis();
            while((System.currentTimeMillis() - startMillis) < TEST_TIMEOUT){
                System.out.println("Test is running for " + ((System.currentTimeMillis() - startMillis) / 1000) + "sec");
                Thread.sleep(10 * 1000);
            }
            System.out.println("Shutting down...");
            this.getSessionManager().shutdown();
            System.out.println("Finish test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
