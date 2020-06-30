package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;
import javafx.util.Pair;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	private SimplePublisher simplePublisher;
	private int serialNumber;
	private int currTick;
	private int terminateTick;

	public M(int serialNumber) {
		super("M");
		this.serialNumber = serialNumber;
		this.simplePublisher = new SimplePublisher();
	}

	@Override
	protected void initialize() {
		Callback<TerminateBroadcast> callbackTerminate = e -> {
			// whatever you want to happen when SomeEvent is received
			terminate();
			MessageBrokerImpl.getInstance().unregister(this);
			Diary.getInstance().printToFile(e.getDiaryFileName());
		};
		this.subscribeBroadcast(TerminateBroadcast.class, callbackTerminate);

		// anonymous class, the method ‘call’ is overriden
		Callback<TickBroadcast> callbackTickBroadcast = new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast e) {
				// whatever you want to happen when SomeEvent is received
				currTick = e.getTick();
				terminateTick = e.getTerminateTick();
			}
		};

		Callback<MissionReceivedEvent> callbackMissionReceived = new Callback<MissionReceivedEvent>(){
			@Override
			public void call(MissionReceivedEvent e) {
				// whatever you want to happen when SomeEvent is received
				int timeIssued = currTick;
				MissionInfo missionInfo = e.getMissionInfo();
				Future<Pair<Pair<Integer,Boolean>,List<String>>> areAgentsAvailable = simplePublisher.sendEvent(new AgentsAvailableEvent(missionInfo.getSerialAgentsNumbers()));
				if(areAgentsAvailable != null && areAgentsAvailable.get() != null && areAgentsAvailable.get().getKey() != null && areAgentsAvailable.get().getKey().getValue() && areAgentsAvailable.get().getValue() != null)
				{
					Future<Pair<Integer,Boolean>> areGadgetsAvailable = simplePublisher.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
					if(areGadgetsAvailable != null && areGadgetsAvailable.get() != null && areGadgetsAvailable.get().getValue())
					{
						if(areAgentsAvailable!=null&areGadgetsAvailable!=null) {
							Pair<Pair<Integer,Boolean>, List<String>> pair = areAgentsAvailable.get();
							Pair<Integer,Boolean> gadgetPair = areGadgetsAvailable.get();
							if(pair!=null&gadgetPair!=null)
							{
								Boolean areAgentsAvailableBoolean = pair.getKey().getValue();
								Boolean areGadgetsAvailableBoolean = gadgetPair.getValue();
								if(areAgentsAvailableBoolean & areGadgetsAvailableBoolean &  missionInfo.getTimeExpired() >= currTick) //still have time to complete the mission
								{
									simplePublisher.sendEvent(new SendAgentsEvent(missionInfo.getSerialAgentsNumbers(),missionInfo.getDuration()));
									Report report = new Report();
									report.setTimeCreated(currTick);
									report.setAgentsNames(missionInfo.getSerialAgentsNumbers());
									report.setAgentsNames(pair.getValue());
									report.setGadgetName(missionInfo.getGadget());
									report.setM(serialNumber);
									report.setMissionName(missionInfo.getMissionName());
									report.setMoneypenny(pair.getKey().getKey());
									report.setTimeIssued(timeIssued);
									report.setQTime(gadgetPair.getKey());
									Diary.getInstance().addReport(report);
								}
								else
								{
									simplePublisher.sendEvent(new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers()));
								}
							}
							else
							{
								simplePublisher.sendEvent(new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers()));
							}
						}
						else
						{
							simplePublisher.sendEvent(new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers()));
						}
					}
					else
					{
						simplePublisher.sendEvent(new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers()));
					}
				}
				else
				{
					simplePublisher.sendEvent(new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers()));
				}

				Diary.getInstance().incrementTotal();
			}
		};
		this.subscribeBroadcast(TickBroadcast.class, callbackTickBroadcast);
		this.subscribeEvent(MissionReceivedEvent.class, callbackMissionReceived);
	}

}
