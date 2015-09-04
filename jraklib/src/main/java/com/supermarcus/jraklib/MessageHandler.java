package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.message.RakLibMessage;

public interface MessageHandler {
    void onMessage(RakLibMessage message);
}