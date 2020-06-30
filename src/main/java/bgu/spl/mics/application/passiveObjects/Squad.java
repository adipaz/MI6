package bgu.spl.mics.application.passiveObjects;
import java.util.*;
import java.util.stream.Collectors;

import bgu.spl.mics.application.passiveObjects.Agent;
/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

	private Map<String, Agent> agents;


	private static class SquadHolder {
		private static Squad instance = new Squad();
	}
	private Squad() {
		this.agents=new HashMap<String,Agent>();
	}


	/**
	 * Retrieves the single instance of this class.
	 */
	public static Squad getInstance() {
		return SquadHolder.instance;
	}



	/**
	 * Initializes the squad. This method adds all the agents to the squad.
	 * <p>
	 * @param agents 	Data structure containing all data necessary for initialization
	 * 						of the squad.
	 */
	public void load (Agent[] agents) {
		for (Agent a: agents) {
			this.agents.put(a.getSerialNumber(),a);
		}
		this.agents = this.agents.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, HashMap::new));
	}

	/**
	 * Releases agents.
	 */
	public void releaseAgents(List<String> serials){
		for (String num:serials) {
				Agent agent = this.agents.get(num);
			if(agent != null) {
				synchronized (agent)
				{

						agent.release();
						agent.notifyAll();
				}
			}
		}
	}

	/**
	 * simulates executing a mission by calling sleep.
	 * @param time   time ticks to sleep
	 */
	public void sendAgents(List<String> serials, int time){
		try {
			Thread.sleep(time*100);
			this.releaseAgents(serials);
		} catch (InterruptedException e) {

		}
	}

	/**
	 * acquires an agent, i.e. holds the agent until the caller is done with it
	 * @param serials   the serial numbers of the agents
	 * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
	 */
	public boolean getAgents(List<String> serials){

		for (String num: serials) {
			if(!this.agents.containsKey(num)) //there is a serial number but there isn't such agent in the list
				return false;
			Agent agent = this.agents.get(num);
			if(agent !=null) {
				synchronized (agent) {
						while (!agent.isAvailable()) {
							try {
								agent.wait();
							} catch (InterruptedException e) { }
						}
						this.agents.get(num).acquire();
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

    /**
     * gets the agents names
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials){
        List<String> names=new LinkedList<>();
		for (String num: serials)
		{
			names.add(this.agents.get(num).getName());
		}

		return names;
    }

}
