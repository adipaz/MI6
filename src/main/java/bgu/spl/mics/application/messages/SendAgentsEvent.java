package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import java.util.List;

public class SendAgentsEvent implements Event {
    private List<String> serial;
    private int duration;

    public SendAgentsEvent(List<String> serial, int duration)
    {
        this.serial = serial;
        this.duration = duration;
    }

    public List<String> getSerial(){return this.serial;}

    public int getDuration(){return this.duration;}
}
