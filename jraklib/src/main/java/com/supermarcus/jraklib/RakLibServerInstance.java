package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;

import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * RakLib Server Instance
 *
 * - Extend this class in order to use JRakLib
 */
abstract public class RakLibServerInstance {
    private SessionManager manager = new SessionManager();

    protected SessionManager getSessionManager(){
        return manager;
    }

    /**
     * Add a interface
     *
     * @param address The address binding on
     * @throws SocketException
     * @throws InterfaceOutOfPoolSizeException
     */
    protected void addInterface(InetSocketAddress address) throws SocketException, InterfaceOutOfPoolSizeException {
        this.getSessionManager().addInterface(address);
    }

    /**
     * Add a handler which handle the messages triggered by JRakLib
     *
     * @param handler The handler which will be add to message handler list
     */
    protected void addMessageHandler(MessageHandler handler){
        this.getSessionManager().addMessageHandler(handler);
    }

    /**
     * Set the handler which will handle all packets and encapsulated packets
     *
     * @param handler The handler which will be add to packet handler list
     */
    protected void setPacketHandler(PacketHandler handler){
        this.getSessionManager().setPacketHandler(handler);
    }
}
