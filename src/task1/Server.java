package task1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Server extends JFrame {
    private JTextArea logArea;
    private JButton startButton, stopButton, closeButton;
    private ServerSocket serverSocket;
    private boolean serverRunning;
    public Server() {
        setTitle("Сервер");
        setSize(510, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel controlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Запустити сервер");
        startButton.addActionListener(new StartButtonListener());
        controlPanel.add(startButton);
        stopButton = new JButton("Зупинити сервер");
        stopButton.addActionListener(new StopButtonListener());
        controlPanel.add(stopButton);
        closeButton = new JButton("Завершити");
        closeButton.addActionListener(new CloseButtonListener());
        controlPanel.add(closeButton);

        logArea = new JTextArea();
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logArea);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    private class StartButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (!serverRunning) {
                try {
                    String host = JOptionPane.showInputDialog("Введіть ім'я хоста (наприклад, localhost):");
                    int port = Integer.parseInt(JOptionPane.showInputDialog("Введіть робочий порт:"));
                    serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
                    logArea.append("Сервер запущено на " + host + " порті " + port + "\n");
                    serverRunning = true;
                    new Thread(new ServerThread()).start();
                } catch (IOException e) {
                    logArea.append("Помилка запуску сервера: " + e.getMessage() + "\n");
                }
            } else {
                logArea.append("Сервер вже запущено\n");
            }
        }
    }
    private class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (serverRunning && !serverSocket.isClosed()) {
                try {
                    serverRunning = false;
                    serverSocket.close();
                    logArea.append("Сервер зупинено.\n");
                } catch (IOException e) {
                    logArea.append("Помилка зупинки сервера: " + e.getMessage() + "\n");
                }
            } else {
                logArea.append("Сервер не був запущено або вже зупинено.\n");
            }
        }
    }
    private class CloseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logArea.append("Помилка закриття сервера: " + e.getMessage() + "\n");
                }
            }
            System.exit(0);
        }
    }
    private class ServerThread implements Runnable {
        public void run() {
            try {
                while (serverRunning) {
                    Socket clientSocket = serverSocket.accept();
                    logArea.append("Новий клієнт підключився: " + clientSocket + "\n");

                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                }
            } catch (IOException e) {
                if (serverRunning) {
                    logArea.append("Помилка сервера: " + e.getMessage() + "\n");
                } else {
                    logArea.append("Сервер зупинений, нові з'єднання не приймаються.\n");
                }
            }
        }
    }
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        public void run() {
            try {
                InputStream input = clientSocket.getInputStream();
                ObjectInputStream objectInput = new ObjectInputStream(input);
                Task task = (Task) objectInput.readObject();

                String taskType = task.getClass().getSimpleName();
                logArea.append("Виконано завдання: " + taskType + ". Результат та час обчислення відправлено клієнту.\n");

                long startTime = System.nanoTime();
                Result result = task.execute();
                long endTime = System.nanoTime();
                long executionTime = endTime - startTime;

                OutputStream output = clientSocket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);
                objectOutput.writeObject(result);
                objectOutput.writeLong(executionTime);
                objectOutput.flush();

                objectInput.close();
                objectOutput.close();
            } catch (IOException | ClassNotFoundException e) {
                logArea.append("Помилка обробки запиту клієнта: " + e.getMessage() + "\n");
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    logArea.append("Помилка закриття сокету клієнта: " + e.getMessage() + "\n");
                }
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Server server = new Server();
            server.setVisible(true);
        });
    }
}