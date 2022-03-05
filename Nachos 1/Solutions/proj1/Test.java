package nachos.proj1;
import jdk.dynalink.beans.StaticClass;
import nachos.machine.Machine;
import nachos.threads.*;

import java.util.Random;

public class Test {
    public static void initiateTest()
    {
        new Jointest().performTest();
        new Condition2Test().performTest();
        new AlarmTest().performTest();
    }
}
class Jointest {
    public void performTest() {
        System.out.println("----------------------------");
        System.out.println("Testing for task 1 initiated");
        System.out.println("----------------------------");
        System.out.println("Thread 0 loops for 15 times and Thread 1 and 2 loops for 5 times");

        KThread t0=new KThread(new PingTest(0)).setName(" forked thread 0");
        System.out.println("forked thread 0");
        t0.fork();
        KThread t1=new KThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    System.out.println("*** thread 1 looped "
                            + i + " times");
                    KThread.yield();
                }
                System.out.println("Thread 1 joining thread 0");
                t0.join();
                System.out.println("This should be printed after thread 0 to finishes");
                System.out.println("Thread 1 is finishing");
            }
        }).setName("forked thread 1");
        KThread t2=new KThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    System.out.println("*** thread 2 looped "
                            + i + " times");
                    KThread.yield();
                }
                System.out.println("Thread 2 joining thread 0");
                t0.join();
                System.out.println("This might not wait for thread 0 to finish as result of calling join() a second time on the same thread is undefined");
                System.out.println("Thread 2 is finishing");
            }
        }).setName("forked thread 2");
        System.out.println("forked thread 1");
        t1.fork();
        System.out.println("forked thread 2");
        t2.fork();
        System.out.println("Joined with thread 1");
        t1.join();
        System.out.println("This should be printed after thread 1 finishes");
        System.out.println("----------------------------");
        System.out.println("Testing for task 1 finished");
        System.out.println("----------------------------");

    }

    private static class PingTest implements Runnable {
        PingTest(int which) {
            this.which = which;
        }

        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println("*** thread " + which + " looped "
                        + i + " times");
                KThread.yield();
            }
            System.out.println("Thread "+which+" is finishing");
        }

        private int which;
    }
}

class Condition2Test
{
    Communicator com;
    public Condition2Test()
    {
        com=new Communicator();
    }
    public void performTest()
    {
        System.out.println("-------------------------------");
        System.out.println("Testing for task 2 & 4 initiated");
        System.out.println("-------------------------------");
        KThread l1=new KThread(new Listener(1,com)).setName("Listener Thread 1");
        KThread l2=new KThread(new Listener(2,com)).setName("Listener Thread 2");
        KThread s1=new KThread(new Speaker(1,com)).setName("Speaker Thread 1");
        KThread s2=new KThread(new Speaker(2,com)).setName("Speaker Thread 2");
        KThread s3=new KThread(new Speaker(3,com)).setName("Speaker Thread 3");
        l1.fork();
        l2.fork();
        s1.fork();
        s2.fork();
        s3.fork();

        l1.join();
        l2.join();
        s1.join();
        s2.join();
        s3.join();

        System.out.println("----------------------------");
        System.out.println("Testing for task 2 & 4 finished");
        System.out.println("----------------------------");


    }
    private static class Listener implements Runnable
    {
        private int which;
        private Communicator com;

        public Listener(int which, Communicator com) {
            this.which = which;
            this.com = com;
        }

        @Override
        public void run() {
            for (int i=0;i<3;i++) {
                com.listen();
                //System.out.println("Listener done");
            }
        }
    }
    private static class Speaker implements Runnable
    {
        private int which;
        private Communicator com;

        public Speaker(int which, Communicator com) {
            this.which = which;
            this.com = com;
        }

        @Override
        public void run() {
            for (int i=0;i<2;i++)
            {
                Random random=new Random(System.currentTimeMillis());
                com.speak(random.nextInt(100));
                //System.out.println("Speraker done");
            }
        }
    }

}

class AlarmTest{
    private static class AlarmTestRunnable implements Runnable{
        private final long time;
        private final Alarm alarm;

        private AlarmTestRunnable(long time, Alarm alarm) {
            this.time = time;
            this.alarm = alarm;
        }

        @Override
        public void run() {
            System.out.println(KThread.currentThread().getName()+" rings at "+ Machine.timer().getTime());
            alarm.waitUntil(time);
            System.out.println(KThread.currentThread().getName()+" rings at "+ Machine.timer().getTime());
        }
    }
    public void performTest()
    {
        System.out.println("----------------------------");
        System.out.println("Testing for task 3 initiated");
        System.out.println("----------------------------");
        KThread t0=new KThread(new AlarmTestRunnable(1000,ThreadedKernel.alarm)).setName("Alarm Thread 0");
        KThread t1=new KThread(new AlarmTestRunnable(500,ThreadedKernel.alarm)).setName("Alarm Thread 1");
        KThread t2=new KThread(new AlarmTestRunnable(200,ThreadedKernel.alarm)).setName("Alarm Thread 2");
        t0.fork();
        t1.fork();
        t2.fork();
        t0.join();
        t1.join();
        t2.join();
        System.out.println("----------------------------");
        System.out.println("Testing for task 3 finished");
        System.out.println("----------------------------");

    }
}
