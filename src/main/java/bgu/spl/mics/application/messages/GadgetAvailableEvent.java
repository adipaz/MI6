package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import javafx.util.Pair;

public class GadgetAvailableEvent implements Event<Pair<Integer,Boolean>> {
    private String gadget;

    public String getGadget(){return this.gadget;}

    public GadgetAvailableEvent(String gadget)
    {
        this.gadget = gadget;
    }
}
