package task1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextField hostField, portField, numberField;
    private JTextArea resultArea;
    private JButton calculateButton, clearButton, exitButton;
    private Socket socket;
    public Client() {
        setTitle("Клієнт");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Назва хосту:"));
        hostField = new JTextField();
        inputPanel.add(hostField);
        inputPanel.add(new JLabel("Номер порту:"));
        portField = new JTextField();
        inputPanel.add(portField);
        inputPanel.add(new JLabel("N (число для факторіалу):"));
        numberField = new JTextField();
        inputPanel.add(numberField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        calculateButton = new JButton("Порахувати");
        calculateButton.addActionListener(new CalculateButtonListener());
        buttonPanel.add(calculateButton);
        clearButton = new JButton("Очистити");
        clearButton.addActionListener(new ClearButtonListener());
        buttonPanel.add(clearButton);
        exitButton = new JButton("Завершити");
        exitButton.addActionListener(new ExitButtonListener());
        buttonPanel.add(exitButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    private class CalculateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String host = hostField.getText();
            try {
                int port = Integer.parseInt(portField.getText());
                int number = Integer.parseInt(numberField.getText());
                try {
                    socket = new Socket(host, port);
                    resultArea.append("Підключено до сервера на " + host + " порт " + port + "\n");

                    Task task = new FactorialTask(number);

                    OutputStream output = socket.getOutputStream();
                    ObjectOutputStream objectOutput = new ObjectOutputStream(output);
                    objectOutput.writeObject(task);
                    objectOutput.flush();

                    InputStream input = socket.getInputStream();
                    ObjectInputStream objectInput = new ObjectInputStream(input);
                    Result result = (Result) objectInput.readObject();
                    long executionTime = objectInput.readLong();

                    resultArea.append("Результат факторіалу " + number + ": " + result + ", виконано за " + executionTime + " нс\n");

                    objectOutput.close();
                    objectInput.close();
                } catch (IOException | ClassNotFoundException e) {
                    resultArea.append("Помилка: " + e.getMessage() + "\n");
                }
            } catch (NumberFormatException e) {
                resultArea.append("Помилка введення даних: перевірте правильність введення номера порту та числа\n");
            }
        }
    }
    private class ClearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            numberField.setText("");
            resultArea.setText("");
        }
    }
    private class ExitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}
class FactorialTask implements Task {
    private int number;
    public FactorialTask(int number) {
        this.number = number;
    }
    public Result execute() {
        long factorial = calculateFactorial(number);
        return new FactorialResult(factorial);
    }
    private long calculateFactorial(int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * calculateFactorial(n - 1);
        }
    }
}
class FactorialResult implements Result {
    private long factorial;
    public FactorialResult(long factorial) {
        this.factorial = factorial;
    }
    public String toString() {
        return String.valueOf(factorial);
    }
}