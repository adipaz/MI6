package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import java.util.List;

public class ReleaseAgentsEvent implements Event {

    private List<String> serial;

    public ReleaseAgentsEvent(List<String> s)
    {
        this.serial = s;
    }

    public List<String> getSerial(){return this.serial;}
}
