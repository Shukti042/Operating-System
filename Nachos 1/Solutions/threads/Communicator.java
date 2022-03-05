package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        this.waiting = new Lock();
        this.speak = new Condition2(waiting);
        this.listen = new Condition2(waiting);
        this.currentSpeaker=new Condition2(waiting);
        this.spoke = false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {

        this.waiting.acquire();

        while (spoke)
        {
            this.speak.sleep();
        }
        this.spoke = true;
        this.toTransfer = word;
        System.out.println("Speaker spoke "+word);
        this.listen.wake();
        currentSpeaker.sleep();
        this.waiting.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {

        this.waiting.acquire();
        while (!spoke)
        {
            this.listen.sleep();
        }

        int transferring = this.toTransfer;
        System.out.println("Listener listened "+transferring);
        this.currentSpeaker.wake();
        spoke = false;
        this.speak.wake();
        this.waiting.release();
        return transferring;
    }
    private Lock waiting;
    private Condition2 speak;
    private Condition2 listen;
    private Condition2 currentSpeaker;
    private int toTransfer;
    private boolean spoke;
}
