package com.supermarcus.jraklib.lang.message;

abstract public class RakLibMessage {
    private MessageLevel level;

    public RakLibMessage(MessageLevel level){
        this.level = level;
    }

    public boolean equals(Object s){
        return s.getClass().getName().equals(this.getClass().getName());
    }

    public String toString(){
        return "[" + this.getClass().getSimpleName() + ": " + this.getLevel() + "]";
    }

    public MessageLevel getLevel(){
        return this.level;
    }
}
