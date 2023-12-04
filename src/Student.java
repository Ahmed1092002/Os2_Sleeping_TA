import javax.swing.*;
import java.util.concurrent.Semaphore;

class Student implements Runnable {
    private int id;
    private Semaphore taMutex;
    private Semaphore chairsMutex;
    private Semaphore studentSemaphore;
    private JTextArea logTextArea;
    private volatile boolean isRunning;

    public Student(int id, Semaphore taMutex, Semaphore chairsMutex, Semaphore studentSemaphore, JTextArea logTextArea, boolean isRunning) {
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
                if (taMutex.tryAcquire()) { // Student tries to enter TA's room
                    logTextArea.append("Student " + id + " is getting help from the TA.\n");
                    System.out.println("Student " + id + " is getting help from the TA.\n");
                    studentSemaphore.release(); // Signal the TA
                    taMutex.release(); // Release TA mutex lock
                    Thread.sleep(1000); // Simulate student getting help for 1 second
                } else if (chairsMutex.tryAcquire()) { // If no TA available, student tries to wait in one of the chairs
                    logTextArea.append("Student " + id + " is waiting on a chair.\n");
                    System.out.println("Student " + id + " is waiting on a chair.\n");
                    Thread.sleep(1000); // Simulate student waiting for 1 second
                } else { // If no chair is available, student will leave and come later
                    logTextArea.append("No chairs available. Student " + id + " will come later.\n");
                    System.out.println("No chairs available. Student " + id + " will come later.\n");
                    Thread.sleep(5000); // Simulate student coming back later
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}