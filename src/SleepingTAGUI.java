import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

public class SleepingTAGUI extends JFrame {
    private Semaphore taMutex;
    private Semaphore chairsMutex;
    private Semaphore studentSemaphore;
    private JTextArea logTextArea;
    private JLabel tasWorkingLabel;
    private JLabel tasSleepingLabel;
    private JLabel studentsWaitingLabel;
    private JLabel studentsToComeLaterLabel;
    private JTextField studentsInput;
    private JTextField chairsInput;
    private JTextField taInput;
    private volatile boolean isRunning = true;

    public SleepingTAGUI() {
        super("Sleeping Teaching Assistant");

        // Create semaphores.
        taMutex = new Semaphore(1); // Mutex lock for the TA
        chairsMutex = new Semaphore(0); // Mutex lock for chairs (initialized as 0 to make it a blocking semaphore)
        studentSemaphore = new Semaphore(0); // Semaphore for signaling students

        logTextArea = new JTextArea();

        // Set up the GUI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("#students: "));
        studentsInput = new JTextField();
        inputPanel.add(studentsInput);
        inputPanel.add(new JLabel("#chairs: "));
        chairsInput = new JTextField();
        inputPanel.add(chairsInput);
        inputPanel.add(new JLabel("#TA(s): "));
        taInput = new JTextField();
        inputPanel.add(taInput);
        inputPanel.add(new JLabel("")); // Empty space for alignment

        JPanel statusPanel = new JPanel(new GridLayout(4, 1));
        tasWorkingLabel = new JLabel("#TAs working: 0");
        tasSleepingLabel = new JLabel("#TAs Sleeping: 0");
        studentsWaitingLabel = new JLabel("#Students Waiting on chairs: 0");
        studentsToComeLaterLabel = new JLabel("#Students that will come later: 0");
        statusPanel.add(tasWorkingLabel);
        statusPanel.add(tasSleepingLabel);
        statusPanel.add(studentsWaitingLabel);
        statusPanel.add(studentsToComeLaterLabel);

        add(inputPanel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.CENTER);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startSimulation());
        add(startButton, BorderLayout.SOUTH);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopSimulation());
        add(stopButton, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void startSimulation() {
        isRunning = true;

        // Parse input values
        int numberOfStudents = Integer.parseInt(studentsInput.getText());
        int numberOfChairs = Integer.parseInt(chairsInput.getText());
        int numberOfTAs = Integer.parseInt(taInput.getText());
        chairsMutex = new Semaphore(numberOfChairs);

        // Create threads for each TA and student
        Thread[] tas = new Thread[numberOfTAs];
        Thread[] students = new Thread[numberOfStudents];

        for (int i = 0; i < numberOfTAs; i++) {
            tas[i] = new Thread(new TeachingAssistant(i+1, taMutex, chairsMutex, studentSemaphore, logTextArea, isRunning));
            tas[i].start();
        }

        for (int i = 0; i < numberOfStudents; i++) {
            students[i] = new Thread(new Student(i+1, taMutex, chairsMutex, studentSemaphore, logTextArea, isRunning));
            students[i].start();
        }
        new Thread(() -> {
            while (true) {
                SwingUtilities.invokeLater(() -> {
                    tasWorkingLabel.setText("#TAs working: " + taMutex.availablePermits());
                    tasSleepingLabel.setText("#TAs Sleeping: " + (numberOfTAs - taMutex.availablePermits()));
                    studentsWaitingLabel.setText("#Students Waiting on chairs: " + chairsMutex.availablePermits());
                    studentsToComeLaterLabel.setText("#Students that will come later: " + (numberOfStudents - chairsMutex.availablePermits()));                });
                try {
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Update GUI in real-time
//        new Thread(() -> {
//            while (isRunning) {
//                tasWorkingLabel.setText("#TAs working: " + taMutex.availablePermits());
//                tasSleepingLabel.setText("#TAs Sleeping: " + (numberOfTAs - taMutex.availablePermits()));
//                studentsWaitingLabel.setText("#Students Waiting on chairs: " + chairsMutex.availablePermits());
//                studentsToComeLaterLabel.setText("#Students that will come later: " + (numberOfStudents - chairsMutex.availablePermits()));
//                try {
//                    Thread.sleep(1000); // Update every second
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private void stopSimulation() {
        isRunning = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SleepingTAGUI gui = new SleepingTAGUI();
            gui.setVisible(true);
        });
    }
}