package bgu.spl.mics;

import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {

	private Map<Subscriber, BlockingDeque<Message>> subscriberQueueMap;
	private Map<Class<? extends Event>, BlockingDeque<Subscriber>> eventQueueMap;
	private Map<Class<? extends Broadcast>, BlockingDeque<Subscriber>> broadcastQueueMap;
	private Map<Event,Future> eventFutureMap;

	private static class MessageBrokerImplHolder {
		private static MessageBrokerImpl instance = new MessageBrokerImpl();
	}

	private MessageBrokerImpl() {
		this.subscriberQueueMap=new ConcurrentHashMap<>();
		this.eventQueueMap=new ConcurrentHashMap<>();
		this.broadcastQueueMap=new ConcurrentHashMap<>();
		this.eventFutureMap=new ConcurrentHashMap<>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static MessageBroker getInstance() {
		return MessageBrokerImplHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
		if(this.eventQueueMap.get(type)==null) {
			this.eventQueueMap.put(type, new LinkedBlockingDeque<>());
		}
		try {
			this.eventQueueMap.get(type).put(m);
		} catch (InterruptedException e) { }
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
		this.broadcastQueueMap.computeIfAbsent(type, k -> new LinkedBlockingDeque<>());
		try {
			this.broadcastQueueMap.get(type).put(m);
		} catch (InterruptedException e) { }
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		this.eventFutureMap.get(e).resolve(result);

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if(this.broadcastQueueMap.containsKey(b.getClass()))
		{
			for (Subscriber sub: this.broadcastQueueMap.get(b.getClass())) {
				BlockingDeque<Message> queue = this.subscriberQueueMap.get(sub);

					synchronized (queue)
					{
						try {
							if(b.getClass().equals(TerminateBroadcast.class))
								queue.putFirst(b);
							else
								queue.put(b);
						} catch (InterruptedException e) { }
						queue.notifyAll();
					}

			}
		}
	}

	//takes the first sub in round-robbin and returns new Future object
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if (this.eventQueueMap.get(e.getClass()) == null) //if there isn't such event
			return null;
		Subscriber sub = this.eventQueueMap.get(e.getClass()).peek();
		if (sub == null)//if there are no subscribers listed to this type of event
			return null;
		BlockingDeque<Message> queue = this.subscriberQueueMap.get(sub);
		if (queue != null) {
			synchronized (queue) {
				sub = this.eventQueueMap.get(e.getClass()).remove();
				try {
					this.eventQueueMap.get(e.getClass()).put(sub);
					queue.put(e);
					this.eventFutureMap.put(e, new Future());
					if (Thread.holdsLock(queue))
						queue.notifyAll();
				} catch (InterruptedException ex) {
				}
				return this.eventFutureMap.get(e);
			}
		}
		return this.eventFutureMap.get(e);
	}

	@Override
	public void register(Subscriber m) {
		if(this.subscriberQueueMap.get(m)==null)
			this.subscriberQueueMap.put(m,new LinkedBlockingDeque<>());
	}

	@Override
	public void unregister(Subscriber m) {
		this.subscriberQueueMap.remove(m);
		for (Map.Entry<Class<? extends Event>, BlockingDeque<Subscriber>> pair : this.eventQueueMap.entrySet()) {
			if (pair.getValue().contains(m)) {
				pair.getValue().remove(m);
			}
		}
		for (Map.Entry<Class<? extends Broadcast>, BlockingDeque<Subscriber>> pair : this.broadcastQueueMap.entrySet())
			pair.getValue().remove(m);
}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		BlockingQueue subQueue = this.subscriberQueueMap.get(m);
		Message msg = null;
		synchronized (subQueue)
		{
			while(subQueue.isEmpty()) {
				subQueue.wait();
			}
			for (Message message: this.subscriberQueueMap.get(m)) {
				if(message.getClass().equals(TerminateBroadcast.class)) {
					msg = message;
					this.subscriberQueueMap.get(m).remove(msg);
				}
			}
			if(msg == null)
				msg = this.subscriberQueueMap.get(m).remove();
		}
		return msg;

	}
}
