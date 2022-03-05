package nachos.vm;
import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.sql.rowset.spi.TransactionalWriter;

import nachos.machine.TranslationEntry;

class VirtualPage{
    public int processID, vpn;
    VirtualPage(int processID, int vpn){
        this.processID = processID;
        this.vpn = vpn;
    }

    @Override    
    public boolean equals(Object o) {
        VirtualPage b = (VirtualPage)o;
        return processID == b.processID && vpn == b.vpn;
    }

    @Override
	public int hashCode() {
        return processID*100000 + vpn;
    }
}

public class MMU {
    public Hashtable<VirtualPage, TranslationEntry> pageTable;
    public LinkedList<Integer> availablePages;
    public int counter = 0;
    public int tlbSize;
    public SwapSpace swapSpace;
    public int next = 0;
    public long[] accessTimes;
    public int[] pageFaults;
    int numPhysPages;

    MMU(){
        pageTable = new Hashtable<VirtualPage, TranslationEntry>();
        availablePages = new LinkedList<>();
        numPhysPages = Machine.processor().getNumPhysPages();
        pageFaults = new int[numPhysPages];        
        for(int i = 0; i < numPhysPages; i++){
            availablePages.add(i);
            pageFaults[i] = 0;
        }
        tlbSize = Machine.processor().getTLBSize();
        swapSpace = new SwapSpace();
        accessTimes=new long[Machine.processor().getNumPhysPages()];
        for(int i=0;i<accessTimes.length;i++)
        {
            accessTimes[i]=-1;
        }
    }

    public void deAllocate(VMProcess process){
        ArrayList<VirtualPage> list = new ArrayList<>();
        for(VirtualPage vPage : pageTable.keySet()){
            if(vPage.processID == process.processID){
                list.add(vPage);
            }
        }
        for(VirtualPage vPage : list){
            TranslationEntry entry = pageTable.get(vPage);            
            availablePages.add(entry.ppn);
            pageTable.remove(vPage);        
        }
        swapSpace.deAllocate(process);        
    }

    public int allocateNewPage(int vpn, VMProcess process){
        int val=0;
        Stats.numPageFaults++;
        if(availablePages.isEmpty() == false){
            val = availablePages.getFirst();
            availablePages.removeFirst();
        }else{
            long min=922334645;
            for(int i=0;i<accessTimes.length;i++)
            {
                if(accessTimes[i]<0)
                {
                    val=i;
                    break;
                }
                else if(accessTimes[i]<min)
                {
                    min=accessTimes[i];
                    val=i;

                }
            }
            for(VirtualPage vPage : pageTable.keySet()){
                TranslationEntry en2 = pageTable.get(vPage);
                if(en2.ppn == val){
                    boolean alreadyswapped=false;
                    if(en2.dirty)
                    {
                        alreadyswapped=true;
                        en2.dirty=false;
                        swapSpace.moveToSwap(en2, vPage.processID);
                    }
                    Processor processor = Machine.processor();
                    for(int i = 0; i < processor.getTLBSize(); i++){
                        TranslationEntry entry = processor.readTLBEntry(i);
                        if(entry.ppn == en2.ppn && entry.valid){
                            if(entry.dirty&&!alreadyswapped)
                            {
                                en2.dirty=false;
                                swapSpace.moveToSwap(en2, vPage.processID);
                            }
                            entry.valid = false;
                            processor.writeTLBEntry(i, entry);
                        }
                    }
                    pageTable.remove(vPage);
                    break;
                }
            }
        }
        TranslationEntry entry = new TranslationEntry(vpn, val, true, false, false, false);
        pageTable.put(new VirtualPage(process.processID, vpn), entry);
        pageFaults[entry.ppn]++;
        return val;
    }

    public TranslationEntry translate(int vpn, VMProcess process){
        VirtualPage vPage = new VirtualPage(process.processID, vpn);
        if(!pageTable.containsKey(vPage)){
            loadPage(vpn, process);
        }
        accessTimes[pageTable.get(vPage).ppn]=Machine.timer().getTime();
        return pageTable.get(vPage);
    }
    public boolean setDirtyBit(int vpn, VMProcess process,boolean val){
        VirtualPage vPage = new VirtualPage(process.processID, vpn);
        for(int i = 0; i < Machine.processor().getTLBSize(); i++){
            TranslationEntry entry = Machine.processor().readTLBEntry(i);
            if(entry.vpn == vpn && entry.valid){
                entry.dirty = val;
                Machine.processor().writeTLBEntry(i, entry);
            }
        }
        if(pageTable.get(vPage)!=null)
        {
            pageTable.get(vPage).dirty=val;
            return true;
        }
        return false;
        
    }

    public void loadPage(int vPageNo, VMProcess process){
        VirtualPage vPage = new VirtualPage(process.processID, vPageNo);
        if(!pageTable.containsKey(vPage)){
            if(swapSpace.existInSwap(vPage)){
                int ppn = allocateNewPage(vPageNo, process);
                accessTimes[ppn]=Machine.timer().getTime();
                swapSpace.getFromSwap(vPage, Machine.processor().getMemory(), 
                    ppn*swapSpace.pageSize);
            }
            else if(process.toLoadCoff.containsKey(vPageNo)){
                CoffPage coffPage = process.toLoadCoff.get(vPageNo);
                int ppn = allocateNewPage(vPageNo, process);
                accessTimes[ppn]=Machine.timer().getTime();
                CoffSection section = process.getCoff().getSection(coffPage.sectionNo);
                section.loadPage(coffPage.segmentPage, ppn);    
            }
            else{
                int ppn = allocateNewPage(vPageNo, process);
                accessTimes[ppn]=Machine.timer().getTime();
            }
        }
        
        if(pageTable.containsKey(vPage)){
            TranslationEntry entry = pageTable.get(vPage);
            entry.valid = true;
            if(Machine.processor().readTLBEntry(counter).valid)
            {
                pageTable.get(new VirtualPage(process.processID, Machine.processor().readTLBEntry(counter).vpn)).dirty=Machine.processor().readTLBEntry(counter).dirty;
            }
            Machine.processor().writeTLBEntry(counter, entry);
            counter = (counter+1)%tlbSize;
        }
    }

    public void printPageFaults(){
        System.out.println("Page faults: ");
        for(int i = 0; i < numPhysPages; i++ ){
            System.out.println("Page No: " + i + ": " + pageFaults[i]);
        }
    }

}
