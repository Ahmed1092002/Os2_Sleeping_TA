import javax.swing.*;
import java.util.concurrent.Semaphore;
class TeachingAssistant implements Runnable {
    private int id;
    private Semaphore taMutex;
    private Semaphore chairsMutex;
    private Semaphore studentSemaphore;
    private JTextArea logTextArea;
    private volatile boolean isRunning;

    public TeachingAssistant(int id, Semaphore taMutex, Semaphore chairsMutex, Semaphore studentSemaphore, JTextArea logTextArea, boolean isRunning) {
        this.id = id;
        this.taMutex = taMutex;
        this.chairsMutex = chairsMutex;
        this.studentSemaphore = studentSemaphore;
        this.logTextArea = logTextArea;
        this.isRunning = isRunning;
    }

    public void run() {
        while (isRunning) {
            try {
                if (studentSemaphore.tryAcquire()) { // If a student is available, the TA helps the student
                    taMutex.acquire(); // Acquire TA mutex lock
                    logTextArea.append("TA " + id + " is helping a student.\n");
                    System.out.println("TA " + id + " is helping a student.\n");
                    Thread.sleep(2000); // Simulate TA helping a student for 2 seconds
                    logTextArea.append("TA " + id + " finished helping the student.\n");
                    System.out.println("TA " + id + " finished helping the student.\n");
                    taMutex.release(); // Release TA mutex lock
                    chairsMutex.release(); // Release a chair after helping a student
                } else { // If there are no students who need help, the TA takes a nap
                    logTextArea.append("TA " + id + " is taking a nap.\n");
                    System.out.println("TA " + id + " is taking a nap.\n");
                    Thread.sleep(2000); // Simulate TA taking a nap for 2 seconds
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
