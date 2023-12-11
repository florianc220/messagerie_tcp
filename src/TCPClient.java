import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.SocketException;


public class TCPClient {
    private static final int SERVER_PORT = 2025;
    private static String SERVER_IP = "127.0.0.1"; // Adresse IP du serveur
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) {
        try {
            if(args.length != 1) {
                System.out.println("Usage: java UDPClient <server ip>");
                System.exit(0);
            }
            SERVER_IP = args[0];
            
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // Créez un thread pour recevoir les messages du serveur
            Thread receiveThread = new Thread(new TCPReceiver(socket));
            receiveThread.start();
            
            connect(socket);
            while (true) {
                String message = getUserInput();

                if (message.equalsIgnoreCase("!help")) {
                    sendMessage(socket, Code.HELP_CODE.getCode());
                } else if (message.equalsIgnoreCase("!list")) {
                    sendMessage(socket, Code.LIST_CODE.getCode());
                } else if (message.startsWith("!whisper")) {
                    sendMessage(socket, Code.WHISPER_CODE.getCode() + message.substring(8));
                } else if (message.equalsIgnoreCase("!quit")) {
                    disconnect(socket);
                    break;
                } else {
                    sendMessage(socket, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String getUserInput() throws IOException {
        return reader.readLine();
    }

    public static void connect(Socket socket){
        try{
            sendMessage(socket, Code.CONNECT_CODE.getCode());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void disconnect(Socket socket) throws IOException{
        sendMessage(socket, Code.DISCONNECT_CODE.getCode());
        socket.close();
        reader.close();
    }

    private static void sendMessage(Socket socket, String message) throws IOException {
        message = "\n"+message;
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println(message);
        writer.flush();
    }
}

class TCPReceiver implements Runnable {
    private Socket socket;

    public TCPReceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String receivedMessage;
            while (socket.isConnected() && (receivedMessage = reader.readLine()) != null) {
                System.out.println(receivedMessage);
            }
        } catch (SocketException e) {
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

