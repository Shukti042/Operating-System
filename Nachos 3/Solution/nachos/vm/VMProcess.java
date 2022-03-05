package nachos.vm;

import java.util.Hashtable;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */

class CoffPage{
    public int sectionNo, segmentPage;
    CoffPage(int sectionNo, int segmentPage){
        this.sectionNo = sectionNo;
        this.segmentPage = segmentPage;
    }
    public boolean equals(Object o) {
        CoffPage b = (CoffPage)o;
        return sectionNo == b.sectionNo && segmentPage == b.segmentPage;
    }
	public int hashCode() {
        return sectionNo + segmentPage*10000;
	}
}

public class VMProcess extends UserProcess {
    /**
     * Allocate a new process.
     */
    public static int counter = 0;
    public Hashtable<Integer, CoffPage> toLoadCoff;
    public static Lock memoryLock = new Lock();
    public static int fin = 0;

    public VMProcess() {
        super();
        toLoadCoff = new Hashtable<Integer, CoffPage>();
    }

    /**
     * Save the state of this process in preparation for a context switch. Called by
     * <tt>UThread.saveState()</tt>.
     */
    public void saveState() {

        for(int i = 0; i < Machine.processor().getTLBSize(); i++){
            TranslationEntry tempEntry = Machine.processor().readTLBEntry(i);
            if(tempEntry.valid)
            {
                tempEntry.valid=false;
                if(VMKernel.mmu.pageTable.get(new VirtualPage(this.processID, tempEntry.vpn))!=null)
                {
                     VMKernel.mmu.pageTable.get(new VirtualPage(this.processID, tempEntry.vpn)).dirty=tempEntry.dirty;
                }

            }
            Machine.processor().writeTLBEntry(i, tempEntry);
        }
       // super.saveState();
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     *
     * @return <tt>true</tt> if successful.
     */

     protected boolean loadSections() {
        Processor processor = Machine.processor();

        int index = 0;
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            Lib.debug(dbgProcess,
                    "\tinitializing " + section.getName() + " section (" + section.getLength() + " pages)");

            for (int i = 0; i < section.getLength(); i++) {
                int vpn = section.getFirstVPN() + i;
                toLoadCoff.put(vpn, new CoffPage(s, i));
            }
        }
        return true;
    }


    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);
        memoryLock.acquire();
        byte[] memory = Machine.processor().getMemory();

        int amount = 0;
        for(int i = 0; i < length && i+offset < data.length ;i++){
            int address = vaddr + i;
            int vPageNo = (address)/pageSize;
            if(vPageNo >= numPages) break;
            int pageOffset = address%pageSize;
            int pPageNo = VMKernel.mmu.translate(vPageNo, this).ppn;
            address = pPageNo*pageSize + pageOffset;
            //if(address >= memory.length) break;
            memory[address] = data[i+offset];
            VMKernel.mmu.setDirtyBit(vPageNo, this, true);
            amount++;
        }
        memoryLock.release();
        return amount;
    }

    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);
        memoryLock.acquire();
        byte[] memory = Machine.processor().getMemory();
        int amount = 0;

        Processor processor = Machine.processor();
        for(int i = 0; i < length && i+offset < data.length ;i++){
            int address = vaddr + i;
            int vPageNo = (address)/pageSize;
            if(vPageNo >= numPages) break;
            int pageOffset = address%pageSize;
            int pPageNo = VMKernel.mmu.translate(vPageNo, this).ppn;
            address = pPageNo*pageSize + pageOffset;
            if(address >= memory.length) break;
            data[i+offset] = memory[address];
            amount++;
        }
        memoryLock.release();
        return amount;
    }

    
    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        VMKernel.mmu.deAllocate(this);
    }

    /**
     * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>.
     * The <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     */

    void handleTLBMiss(){
        Processor processor = Machine.processor();
        memoryLock.acquire();
        int vaddrs = Machine.processor().readRegister(Processor.regBadVAddr);
        int pageNo = vaddrs/pageSize;
        VMKernel.mmu.loadPage(pageNo, this);
        memoryLock.release();
    } 

    public void handleException(int cause) {        
        switch (cause) {
            case Processor.exceptionTLBMiss:
                handleTLBMiss();
                break;
            default:
                super.handleException(cause);
                break;
        }
    }

    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgVM = 'v';
}
