package bgu.spl.mics.application;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Squad;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {

    public static void main(String[] args) {
        List<Thread> threadList = new LinkedList<>();
       String jsonFileNameInput = args[0];
       String jsonFileNameOutputInventory = args[1];
       String jsonFileNameOutputDiary = args[2];
        MessageBrokerImpl.getInstance();
        TimeService timeService = new TimeService();
        timeService.setDiaryFileName(jsonFileNameOutputDiary);
        timeService.setInventoryFileName(jsonFileNameOutputInventory);
        Thread timeServiceThread = new Thread(timeService);
        jsonInputFileParsing(jsonFileNameInput, timeService, threadList);
        Q q = new Q();
        Thread qThread = new Thread(q);
        threadList.add(qThread);
        //run all the subs
        for (Thread t: threadList) {
            t.start();
        }
        timeServiceThread.start();

        try {
            timeServiceThread.join();
            for (Thread t: threadList) {
                t.join();
            }
        } catch (InterruptedException e) { }
    }

    public static void jsonInputFileParsing(String fileName, TimeService timeService, List<Thread> threadList)
    {
        try (Reader reader = new FileReader(fileName)) {
            JsonParser p = new JsonParser();
            JsonElement jsonElement = p.parse(reader);

            //parsing the inventory
            inventoryParsing(jsonElement);

            //parsing the services
            mParsing(jsonElement, timeService, threadList);
            moneypennyParsing(jsonElement, timeService, threadList);
            intelligenceParsing(jsonElement, threadList);
            int terminateTick = jsonElement.getAsJsonObject().getAsJsonObject("services").get("time").getAsInt();
            timeService.setTerminateTick(terminateTick);
            //parsing the squad
            squadParsing(jsonElement, timeService);
        }  catch (Exception e) {System.out.println(e.getCause());}
    }

    public static void inventoryParsing(JsonElement jsonElement)
    {
        JsonArray inventoryListJson = jsonElement.getAsJsonObject().getAsJsonArray("inventory");
        List<String> inventoryList = new LinkedList<>();
        int size = inventoryListJson.size();
        for(int i = 0; i < size; i++)
        {
            inventoryList.add(inventoryListJson.get(i).getAsString());
        }
        Inventory.getInstance().load(inventoryList.toArray(new String[0])); //convert the list to string array
    }

    public static void mParsing(JsonElement jsonElement, TimeService timeService, List<Thread> threadList)
    {
        JsonElement MinstancesJson = jsonElement.getAsJsonObject().getAsJsonObject("services").get("M");
        int Minstances = MinstancesJson.getAsInt();
        for(int i = 0; i < Minstances; i++)
        {
            M m = new M(i);
            Thread thread = new Thread(m);
            threadList.add(thread);
        }
    }

    public static void moneypennyParsing(JsonElement jsonElement, TimeService timeService, List<Thread> threadList)
    {
        JsonElement MoneypennyinstancesJson = jsonElement.getAsJsonObject().getAsJsonObject("services").get("Moneypenny");
        int Moneypennyinstances = MoneypennyinstancesJson.getAsInt();
        for(int i = 0; i < Moneypennyinstances; i++)
        {
            Moneypenny moneypenny = new Moneypenny(i);
            Thread thread = new Thread(moneypenny);
            threadList.add(thread);
        }
    }

    public static void intelligenceParsing(JsonElement jsonElement, List<Thread> threadList)
    {
        JsonElement intelligenceListJson1 = jsonElement.getAsJsonObject().getAsJsonObject("services").get("intelligence");
        JsonArray intelligenceListJson = intelligenceListJson1.getAsJsonArray();
        int size = intelligenceListJson.size();
        for(int i = 0; i < size; i++)
        {
            JsonElement missions1 = intelligenceListJson.get(i).getAsJsonObject().get("missions");
            JsonArray missions = missions1.getAsJsonArray();
            int length = missions.size();
            List<MissionInfo> missionInfoList = new LinkedList<>();
            for(int j = 0; j < length; j++)
            {
                JsonArray serialAgentsNumbersJson = missions.get(j).getAsJsonObject().getAsJsonArray("serialAgentsNumbers");
                int amount = serialAgentsNumbersJson.size();
                List<String> serialAgentsNumbers = new LinkedList<>();
                for(int s = 0; s < amount; s++)
                {
                    serialAgentsNumbers.add(serialAgentsNumbersJson.get(s).getAsString());
                }
                MissionInfo missionInfo = new MissionInfo();
                missionInfo.setSerialAgentsNumbers(serialAgentsNumbers);
                int duration = missions.get(j).getAsJsonObject().get("duration").getAsInt();
                missionInfo.setDuration(duration);
                String gadget = missions.get(j).getAsJsonObject().get("gadget").getAsString();
                missionInfo.setGadget(gadget);
                String missionName = "";
                if(missions.get(j).getAsJsonObject().get("missionName") != null)
                    missionName = missions.get(j).getAsJsonObject().get("missionName").getAsString();
                else
                    missionName = missions.get(j).getAsJsonObject().get("name").getAsString();
                missionInfo.setMissionName(missionName);
                int timeExpired = missions.get(j).getAsJsonObject().get("timeExpired").getAsInt();
                missionInfo.setTimeExpired(timeExpired);
                int timeIssued = missions.get(j).getAsJsonObject().get("timeIssued").getAsInt();
                missionInfo.setTimeIssued(timeIssued);
                missionInfoList.add(missionInfo);
            }
            Intelligence intelligence = new Intelligence(missionInfoList);
            Thread thread = new Thread(intelligence);
            threadList.add(thread);
        }
    }

    public static void squadParsing(JsonElement jsonElement, TimeService timeService)
    {
        JsonArray squadListJson = jsonElement.getAsJsonObject().getAsJsonArray("squad");
        List<Agent> squadList = new LinkedList<>();
        int size = squadListJson.size();
        for(int i = 0; i < size; i++)
        {
            JsonElement agentJson = squadListJson.get(i);
            Agent agent = new Agent();
            agent.setName(agentJson.getAsJsonObject().get("name").getAsString());
            agent.setSerialNumber(agentJson.getAsJsonObject().get("serialNumber").getAsString());
            squadList.add(agent);
        }
        Squad.getInstance().load(squadList.toArray(new Agent[0]));//convert the list to Agent array
    }
}
