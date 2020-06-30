package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminateBroadcast implements Broadcast {
    private String diaryFileName;
    private String inventoryFileName;

    public void setDiaryFileName(String fileName){this.diaryFileName = fileName;}
    public void setInventoryFileName(String fileName){this.inventoryFileName = fileName;}
    public String getDiaryFileName(){return this.diaryFileName;}
    public String getInventoryFileName(){return this.inventoryFileName;}
}
