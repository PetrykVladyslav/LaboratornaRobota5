package task2;

import java.net.*;

public class UDPClient {
    private static final int SERVER_PORT = 9876;
    private static final int MAX_ATTEMPTS = 5;
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(2000);
            InetAddress serverAddress = InetAddress.getByName("localhost");

            byte[] sendData = "Register me".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);

            int attempts = 0;
            boolean receivedResponse = false;
            while (!receivedResponse && attempts < MAX_ATTEMPTS) {
                clientSocket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    clientSocket.receive(receivePacket);
                    receivedResponse = true;

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Відповідь з сервера:\n" + response);

                } catch (SocketTimeoutException e) {
                    System.out.println("Час очікування відповіді вичерпано. Повторна спроба...");
                    attempts++;
                }
            }
            if (!receivedResponse) {
                System.out.println("Сервер не відповідає. Припинення спроб з'єднання.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}