package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import javafx.util.Pair;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	int Tick ;
	public Q() {
		super("Q");
	}

	@Override
	protected void initialize() {
		Callback<TerminateBroadcast> callbackTerminate = e -> {
			// whatever you want to happen when SomeEvent is received
			terminate();
			MessageBrokerImpl.getInstance().unregister(this);
			Inventory.getInstance().printToFile(e.getInventoryFileName());
		};
		this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);
		this.subscribeBroadcast(TickBroadcast.class,c -> Tick=c.getTick() );
		// anonymous class, the method ‘call’ is overridden
		Callback<GadgetAvailableEvent> callbackName = new Callback<GadgetAvailableEvent>(){
			@Override
			public void call(GadgetAvailableEvent e) {
				// whatever you want to happen when SomeEvent is received

				boolean result = Inventory.getInstance().getItem(e.getGadget());
				complete(e, new Pair<>(Tick, result));

			}
		};
		this.subscribeEvent(GadgetAvailableEvent.class, callbackName);
	}

}
