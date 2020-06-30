package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the diary where all reports are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Diary {
	private List<Report> reportList;
	private AtomicInteger total;

	private static class DiarySingletonHolder {
		private static Diary instance = new Diary();
	}
	private Diary() {
		this.reportList = new LinkedList<>();
		this.total = new AtomicInteger(0);
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Diary getInstance() {
		return DiarySingletonHolder.instance;
	}


	/**
	 * adds a report to the diary
	 * @param reportToAdd - the report to add
	 */
	public void addReport(Report reportToAdd){
		this.reportList.add(reportToAdd);
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object List<Report> which is a
	 * List of all the reports in the diary.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printToFile(String filename){
		try(FileWriter file = new FileWriter(filename))
		{
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(this, file);
		}catch(IOException ex)
		{

		}

	}

	/**
	 * Gets the total number of received missions (executed / aborted) be all the M-instances.
	 * @return the total number of received missions (executed / aborted) be all the M-instances.
	 */
	public int getTotal(){
		return this.total.get();
	}

	/**
	 * Increments the total number of received missions by 1
	 */
	public void incrementTotal(){

		int val;
		do {
			val = this.total.get();
		}while (!total.compareAndSet(val, val + 1));
	}
}
