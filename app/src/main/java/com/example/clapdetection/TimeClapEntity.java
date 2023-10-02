package com.example.clapdetection;

public class TimeClapEntity {
    private long time1;
    private long time2;
    private long  time3;
    private long time4;


    public TimeClapEntity() {
        time1 = time2 = time3 = time4 = 0L;
    }

    public boolean isDoubleClap(long clapDetectedNumber ){
        time4 = time3;
        time3 = time2;
        time2 = time1;
        time1 = clapDetectedNumber;
        if(time1 - time4 == 2 || time1 - time3 == 2) return true;
        return false;
    }
}
