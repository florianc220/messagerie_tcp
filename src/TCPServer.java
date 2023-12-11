import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.Date;
import java.util.TimeZone;

import java.text.SimpleDateFormat;

public class TCPServer {
    private static final Logger logger = Logger.getLogger(TCPServer.class.getName());
    private static final int SERVER_PORT = 2025;

    private static Handler fileHandler;
    private static ServerSocket serverSocket;
    private static int connectedClientsCount = 0;
    private static List<ClientInfo> clients = new ArrayList<>();

    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
    private static TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
    private static String time;
        
    public static void main(String[] args) {
        try {
            Date startdate = new Date();
            formatter.setTimeZone(tz);
            time = formatter.format(startdate);

            serverSocket = new ServerSocket(SERVER_PORT);
            fileHandler = new FileHandler("logs/server"+time+".log", true);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);

            String myip = InetAddress.getLocalHost().getHostAddress();
            logger.info("Server started on machine "+ myip +" and on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                //Ajoute les infos du clients en mémoire du serveur
                clients.add(new ClientInfo(clientSocket));
                connectedClientsCount++;
                logger.info("Users connected : " + connectedClientsCount);

                // Créez un thread pour gérer la connexion avec le client
                Thread clientThread = new Thread(new TCPClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (SocketException e) {
            // System.out.println("Socket closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessage(Socket senderSocket, String message) {
        for (ClientInfo client : clients) {
            if (client.getSocket() != senderSocket) {
                sendMessage(client.getSocket(), message);
            }
        }
        logger.info("Broadcasting message : " + message);
    }

    private static void sendMessage(Socket socket, String message) {
        try {
            logger.info("[" + socket.getInetAddress() + "]" + message);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println(message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String manual(){
        String manual = "!help : Display this menu\n" +
            "<message> : send message to server to broadcast to all users connected\n" +
            "!whisper <ip address> <message> : Send a private message to a user\n" +
            "!list : Display the list of connected users\n" +
            "!quit : Disconnect from the server\n";
        return manual;
    }
    
    public static class TCPClientHandler implements Runnable {
        private Socket socket;

        public TCPClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                if (socket != null) {
                    sendMessage(socket, "Welcome to the chat !\nType !help to see available commands");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        if(message.equals(Code.DISCONNECT_CODE.getCode())) {
                            System.out.println("Client disconnected");
                            logger.info("Client disconnected");
                            break;
                        } else if (message.equals(Code.CONNECT_CODE.getCode())) {
                            logger.info("Client "+socket.getInetAddress()+" connected");
                        } else if (message.equals(Code.HELP_CODE.getCode())) {
                            logger.info("Client "+socket.getInetAddress()+" asked for help");
                            sendMessage(socket, manual());
                        } else if (message.equals(Code.LIST_CODE.getCode())) {
                            logger.info("Client "+socket.getInetAddress()+" asked for list");
                            String list = "Total users connected : " + connectedClientsCount + "\n";
                            if(connectedClientsCount > 10) {
                                for (int i = 0; i < 10; i++) {
                                    list += "[user "+ i + "] " + clients.get(i).getSocket().getInetAddress().toString() + "\n";
                                    logger.info("[user "+ i + "] " + clients.get(i).getSocket().getInetAddress().toString());
                                }
                            } else {
                                for (int i = 0; i < clients.size(); i++) {
                                    list += "[user "+ i + "] " + clients.get(i).getSocket().getInetAddress().toString() + "\n";
                                    logger.info("[user "+ i + "] " + clients.get(i).getSocket().getInetAddress().toString());
                                }
                            }
                            sendMessage(socket, list);
                        } else if (message.startsWith(Code.WHISPER_CODE.getCode())) {
                            if (connectedClientsCount >= 2) {
                                String[] parts = message.split(" ", 3);
                                String destination = parts[1];
                                String whisper_message = parts[2];
                                logger.info("Client "+socket.getInetAddress()+" whispered to "+ destination);
                                for (ClientInfo client : clients) {
                                    if (client.getSocket().getInetAddress().toString().equals(destination)) {
                                        sendMessage(client.getSocket(), whisper_message);
                                    }
                                }
                            } else {
                                sendMessage(socket, "You are alone on the server");
                            }
                        } else if (message != null && !message.equals("")){
                            broadcastMessage(socket, message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connectedClientsCount--;
                if (connectedClientsCount == 0) {
                    try{
                        if (serverSocket != null && !serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Date endDate = new Date();
                    time = formatter.format(endDate);
                    logger.info("Server closed at "+ time);
                } else {
                    logger.info("Clients restants : " + connectedClientsCount);
                }
            }
        }
    }
}