package com.supermarcus.jraklib.lang;

import com.supermarcus.jraklib.Session;

public class ACKNotification {
    private Session session;

    private Integer identifier;

    public ACKNotification(Session session, Integer identifier){
        this.session = session;
        this.identifier = identifier;
    }

    public Session getSession() {
        return session;
    }

    public Integer getIdentifier() {
        return identifier;
    }
}
