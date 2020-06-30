package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Squad;
import javafx.util.Pair;

import java.util.List;
import java.util.logging.SimpleFormatter;

/**
 * Only this type of Subscriber can access the squad.
 * There are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	private int serialNumber;

	public Moneypenny(int serialNumber) {
		super("MoneyPenny");
		this.serialNumber = serialNumber;
	}

	@Override
	protected void initialize() {
		Callback<TerminateBroadcast> callbackTerminate = e -> {
			// whatever you want to happen when SomeEvent is received
			terminate();
			MessageBrokerImpl.getInstance().unregister(this);
		};
		this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);

		// anonymous class, the method ‘call’ is overriden
		Callback<AgentsAvailableEvent> callbackAvailable = new Callback<AgentsAvailableEvent>(){
			@Override
			public void call(AgentsAvailableEvent e) {
				// whatever you want to happen when SomeEvent is received
				boolean result = Squad.getInstance().getAgents(e.getSerial());
				if(result)
				{
					List<String> agentsNameList = Squad.getInstance().getAgentsNames(e.getSerial());
					Pair<Pair<Integer,Boolean>,List<String>> pair = new Pair<>(new Pair<>(serialNumber,result), agentsNameList);
					complete(e,pair);
				}
				else{
					Pair<Pair<Integer,Boolean>,List<String>> pair = new Pair<>(new Pair<>(serialNumber,result), null);
					complete(e,pair);
				}

			}
		};

		Callback<SendAgentsEvent> callbackSendAgents = new Callback<SendAgentsEvent>(){
			@Override
			public void call(SendAgentsEvent e) {
				// whatever you want to happen when SomeEvent is received
				Squad.getInstance().sendAgents(e.getSerial(),e.getDuration());
			}
		};

		Callback<ReleaseAgentsEvent> callbackReleaseAgents = new Callback<ReleaseAgentsEvent>(){
			@Override
			public void call(ReleaseAgentsEvent e) {
				// whatever you want to happen when SomeEvent is received
				Squad.getInstance().releaseAgents(e.getSerial());
			}
		};

		if(this.serialNumber % 2 == 0) // to distinguish between the moneypenny for avoiding deadlock
		{
			this.subscribeEvent(ReleaseAgentsEvent.class, callbackReleaseAgents);
			this.subscribeEvent(SendAgentsEvent.class, callbackSendAgents);
		}
		else
			this.subscribeEvent(AgentsAvailableEvent.class, callbackAvailable);
	}

}
