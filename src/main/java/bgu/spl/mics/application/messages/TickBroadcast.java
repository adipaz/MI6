package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    private int tick;
    private int terminateTick;


    public TickBroadcast(int tick, int terminateTick)
    {
        this.tick = tick;
        this.terminateTick = terminateTick;
    }

    public TickBroadcast(){}

    public void setTick(int tick){this.tick = tick;}
    public int getTick(){return this.tick;}
    public int getTerminateTick(){return this.terminateTick;}
}
