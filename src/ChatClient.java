import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class ChatClient {
    private String name;
    private String serverIP;
    private Socket client;

    public ChatClient() {
        GUIClient guiClient = new GUIClient();
        guiClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiClient.setSize(515, 260);
        guiClient.setVisible(true);
        guiClient.setResizable(false);

        ServerReader sender = new ServerReader(guiClient);
        Thread readerThread = new Thread(sender);
        readerThread.start();
    }

    class GUIClient extends JFrame {
        private final JTextField chatField;
        private final JScrollPane chatAreaScroll;
        private final JTextArea chatArea;
        private final JButton chatSend;

        public GUIClient() {
            super("Chat Client");
            setLayout(new FlowLayout());
            setAlwaysOnTop(true);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            name = JOptionPane.showInputDialog("Username? (Anything You Want)").trim();

            if (name.trim().equals("")) {
                name = String.format("User#%d", (int) Math.floor(Math.random() * 1000 + 1));
            }

            serverIP = JOptionPane.showInputDialog("Server IP?");

            try {
                client = new Socket(serverIP, 5422);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Server Error", "Error Occurred", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(-1);
            }

            chatArea = new JTextArea(String.format("You are in %s", serverIP), 10, 60);
            chatArea.setEditable(false);
            chatArea.setLineWrap(true);
            // add(chatArea);

            chatAreaScroll = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(chatAreaScroll);

            chatField = new JTextField(null, 50);
            add(chatField);

            chatSend = new JButton("Send");
            chatSend.addActionListener(new ServerSender());
            add(chatSend);
        }

        class ServerSender implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == chatSend) {
                    try {
                        PrintWriter writer = new PrintWriter(client.getOutputStream());
                        writer.println(String.format("<%s>: %s", name, chatField.getText()));
                        writer.flush();
                        // System.out.println(String.format(" Sending to server: %s", chatField.getText()));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
            }
        }
    }

    class ServerReader implements Runnable {
        GUIClient gui;

        public ServerReader(GUIClient guiClient) {
            gui = guiClient;
        }

        @Override
        public void run() {
            try {
                InputStreamReader streamReader = new InputStreamReader(client.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                String ln = null;

                while ((ln = reader.readLine()) != null) {
                    gui.chatArea.append(String.format("%n%s", ln));
                    // System.out.println(String.format("Reading from server: %s", ln));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

