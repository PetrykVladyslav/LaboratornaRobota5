package task2;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPServer {
    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;
    private static List<InetAddress> clientAddresses = new ArrayList<>();
    private static List<Integer> clientPorts = new ArrayList<>();
    private static DatagramSocket serverSocket;
    public static void main(String[] args) {
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            byte[] receiveData = new byte[BUFFER_SIZE];

            System.out.println("UDP Сервер запущено...");
            System.out.println("Для закриття сервера напишіть в консоль слово 'close'.");

            new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    try {
                        String command = reader.readLine();
                        if (command.equals("close")) {
                            System.out.println("Закриття сервера...");
                            serverSocket.close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (!serverSocket.isClosed()) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                if (!clientAddresses.contains(clientAddress) || !clientPorts.contains(clientPort)) {
                    clientAddresses.add(clientAddress);
                    clientPorts.add(clientPort);
                }

                System.out.println("Отримано запит від: " + clientAddress.getHostAddress() + ":" + clientPort);

                String response = getClientList();
                byte[] sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            if (!(e instanceof SocketException && e.getMessage().equals("socket closed"))) {
                e.printStackTrace();
            }
        }
    }
    private static String getClientList() {
        StringBuilder builder = new StringBuilder();
        builder.append("Зареєстрованих користувачів: ").append(clientAddresses.size()).append("\n");
        for (int i = 0; i < clientAddresses.size(); i++) {
            builder.append(clientAddresses.get(i).getHostAddress()).append(":").append(clientPorts.get(i)).append("\n");
        }
        return builder.toString();
    }
}