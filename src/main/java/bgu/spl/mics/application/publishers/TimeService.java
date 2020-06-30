package bgu.spl.mics.application.publishers;

import bgu.spl.mics.Publisher;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.subscribers.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link //Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
	private int Tick;
	private int terminateTick;
	private SimplePublisher simplePublisher;
	private String diaryFileName;
	private String inventoryFileName;

	public void setDiaryFileName(String fileName){this.diaryFileName = fileName;}
	public void setInventoryFileName(String fileName){this.inventoryFileName = fileName;}
	public long getTick()
	{
		return this.Tick;
	}
	public void setTerminateTick(int tickToSet) {this.terminateTick = tickToSet;}

	public TimeService() {
		super("Time Service");
		this.simplePublisher = new SimplePublisher();
	}

	@Override
	protected void initialize() {
		this.Tick = 0;
	}

	@Override
	public void run() {
		initialize();
		Timer timer = new Timer("Timer");
		TimerTask repeatedTask = new TimerTask() {
			public void run() {
				if(Tick == terminateTick)
				{
					TerminateBroadcast b = new TerminateBroadcast();
					b.setDiaryFileName(diaryFileName);
					b.setInventoryFileName(inventoryFileName);
					simplePublisher.sendBroadcast(b);
					timer.cancel();
				}
				else
				{
					simplePublisher.sendBroadcast(new TickBroadcast(Tick,terminateTick));
					Tick++;
				}

			}
		};


		long period  = TimeUnit.MILLISECONDS.toMillis(100);
		timer.scheduleAtFixedRate(repeatedTask, 0, period);

	}

}
