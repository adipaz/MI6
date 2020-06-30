package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {

	List<MissionInfo> missionInfoList;
	SimplePublisher simplePublisher;

	public Intelligence(List<MissionInfo> list) {
		super("Intelligence");
		this.missionInfoList = list;
		simplePublisher = new SimplePublisher();
	}

	@Override
	protected void initialize() {
		// anonymous class, the method ‘call’ is overriden
		Callback<TerminateBroadcast> callbackTerminate = e -> {
			// whatever you want to happen when SomeEvent is received
			terminate();
			MessageBrokerImpl.getInstance().unregister(this);
		};
		this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);
		Callback<TickBroadcast> callbackName = new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast e) {
				// whatever you want to happen when SomeEvent is received
				MissionInfo missionInfo = findMissionInfoByCurrentTick(e.getTick());
				if(missionInfo != null) {
					simplePublisher.sendEvent(new MissionReceivedEvent(missionInfo));
				}
			}
		};
		this.subscribeBroadcast(TickBroadcast.class, callbackName);



	}

	private MissionInfo findMissionInfoByCurrentTick(int tick)
	{
		for (MissionInfo m :this.missionInfoList) {
			if(m.getTimeIssued() == tick)
				return m;
		}
		return null;
	}
}
