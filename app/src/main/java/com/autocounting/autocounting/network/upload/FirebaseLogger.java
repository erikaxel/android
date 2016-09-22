package com.autocounting.autocounting.network.upload;

import org.apache.commons.lang3.time.StopWatch;

public class FirebaseLogger {

    private StopWatch timer;

    public FirebaseLogger(){
        timer = new StopWatch();
    }

    public void start(){
        timer.start();
    }

    public void onFinish(){
        System.out.println(timer.getTime());
    }
}
