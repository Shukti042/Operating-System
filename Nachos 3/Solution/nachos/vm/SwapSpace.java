package nachos.vm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import nachos.security.*;
import nachos.vm.VMKernel;
import nachos.vm.VMProcess;
import nachos.machine.*;

public class SwapSpace {
    Hashtable<VirtualPage, Integer> swapTable;
    OpenFile swapFile;
    LinkedList<Integer> available;
    int startAt;
    int pageSize;
    public SwapSpace(){
        swapTable = new Hashtable<VirtualPage, Integer>();
        available = new LinkedList<Integer>();
        startAt = 0;
        swapFile = VMKernel.fileSystem.open("swap.txt", true);
        pageSize = Processor.pageSize;
    }
    boolean moveToSwap(TranslationEntry entry, int pid){
        VirtualPage vPage = new VirtualPage(pid, entry.vpn);
        int address = 0;
        if(swapTable.containsKey(vPage)){
            address = swapTable.get(vPage);
        }else if(available.isEmpty() == false ){
            address = available.getFirst();
            available.removeFirst();
            swapTable.put(vPage, address);
        }else{
            address = startAt;
            startAt++;
            swapTable.put(vPage, address);
        }
        int ppn = entry.ppn;
        byte[] memory = Machine.processor().getMemory();
        swapFile.write(address*pageSize ,memory, ppn*pageSize, pageSize);
        return true;
    }

    public void deAllocate(VMProcess process){
        ArrayList<VirtualPage> list = new ArrayList<>();        
        for(VirtualPage vPage : swapTable.keySet()){
            if(vPage.processID == process.processID){
                list.add(vPage);
            }
        }
        for(VirtualPage vPage : list){
            int address = swapTable.get(vPage);
            available.add(address);
            swapTable.remove(vPage);
        }
    }

    boolean existInSwap(VirtualPage vPage){
        return swapTable.containsKey(vPage);
    }

    void closeSwap(){
        swapFile.close();
        VMKernel.fileSystem.remove("swap.txt");
    }

    void getFromSwap(VirtualPage vPage, byte[] buf, int offset){
        int address = swapTable.get(vPage);
        swapFile.read(address*pageSize, buf, offset, pageSize);
    }
}
