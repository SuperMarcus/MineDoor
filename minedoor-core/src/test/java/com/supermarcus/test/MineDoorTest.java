package com.supermarcus.test;

import com.supermarcus.minedoor.MineDoor;

public class MineDoorTest {
    public MineDoorTest(){
        MineDoor.main(new String[0]);
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
