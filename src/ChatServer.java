import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ChatServer {
    private ArrayList<Socket> clients;
    private ArrayList<Thread> handlers;
    private ServerSocket server;
    private JTextArea chatArea;
    private JScrollPane chatAreaScroll;

    public ChatServer() {
        try {
            server = new ServerSocket(5422);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clients = new ArrayList<Socket>();
        handlers = new ArrayList<Thread>();

        ClientHelper helper = new ClientHelper();
        Thread helperThread = new Thread(helper);
        helperThread.start();

        GUIClient guiClient = new GUIClient();
        guiClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiClient.setSize(515, 260);
        guiClient.setVisible(true);
        guiClient.setResizable(false);
    }

    class GUIClient extends JFrame {
        public GUIClient() {
            super("Chat Client");
            setLayout(new FlowLayout());
            setAlwaysOnTop(true);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            chatArea = new JTextArea(10, 60);

            try {
                chatArea.setText(String.format("You are in %s", InetAddress.getLocalHost().getHostAddress()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            chatArea.setEditable(false);
            chatArea.setLineWrap(true);
            // add(chatArea);

            chatAreaScroll = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(chatAreaScroll);

        }
    }

    class ClientHelper implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Socket client = server.accept();
                    clients.add(client);

                    Thread senderThread = new Thread(new ClientSender(client));
                    senderThread.start();
                    handlers.add(senderThread);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class ClientSender implements Runnable {
        Socket client;

        public ClientSender(Socket cli) {
            client = cli;
        }

        @Override
        public void run() {
            try {
                InputStreamReader streamReader = new InputStreamReader(client.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                String ln = null;

                while ((ln = reader.readLine()) != null) {
                    // System.out.println(String.format("Got from a client: %s", ln));
                    sendToClients(ln);
                    chatArea.append(String.format("%n%s", ln));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToClients(String message) {
        // System.out.println(String.format("Sending to clients: %s", message));
        for (Socket socket : clients) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(socket.getOutputStream());
                writer.println(message);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

