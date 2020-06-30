package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Agent;
import javafx.util.Pair;

import java.util.List;

public class AgentsAvailableEvent implements Event<Pair<Pair<Integer,Boolean>, List<String>>> {
    private List<String> serial;

    public List<String> getSerial(){return this.serial;}

    public AgentsAvailableEvent(List<String> serial)
    {
        this.serial = serial;
    }
}
