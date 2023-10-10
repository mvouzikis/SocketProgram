import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class server {

    private static final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
    private static final Map<Integer, Message> messages = new ConcurrentHashMap<>();
    private static int authToken = 0;
    private static int messageId = 0;

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream inStream;
        private ObjectOutputStream outStream;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.inStream = new ObjectInputStream(socket.getInputStream());
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int operation = (int) inStream.readObject();
                        if(operation == 1) {
                            String username = (String) inStream.readObject();
                            for (Account account : accounts.values()) {
                                if (username.equals(account.getUsername())) {
                                    outStream.writeObject(0);
                                    return;
                                }
                            }
                            authToken = authToken + 1;
                            accounts.put(authToken, new Account(username, authToken));
                            outStream.writeObject(authToken);
                        }
                        if(operation == 2){
                            int authToken = (int) inStream.readObject();
                            if (!accounts.containsKey(authToken))
                                outStream.writeObject("Invalid auth token");

                            else {
                                StringBuilder sb = new StringBuilder();
                                int i = 1;
                                for (Account account : accounts.values())
                                {
                                    sb.append(i).append(". ").append(account.getUsername()).append("\n");
                                    i++;
                                }
                                outStream.writeObject(sb.toString());
                            }
                        }
                        if(operation == 3){
                            int authToken = (int) inStream.readObject();
                            String recipient = (String) inStream.readObject();
                            String message = (String) inStream.readObject();
                            if (!accounts.containsKey(authToken))
                                outStream.writeObject("Invalid auth token");
                            else {
                                Account recipientAccount = null;
                                for (Account account : accounts.values())
                                    if (account.getUsername().equals(recipient)) {
                                        recipientAccount = account;
                                        break;
                                    }

                                if (recipientAccount == null)
                                    outStream.writeObject("User does not exist");

                                else {
                                    messageId++;
                                    Account senderAccount = accounts.get(authToken);
                                    Message msg = new Message(senderAccount.getUsername(), message, false, messageId);
                                    recipientAccount.getMailbox().add(msg);
                                    messages.put(messageId, msg);
                                    outStream.writeObject("OK. Message Id = " + messageId);
                                }
                            }
                        }
                        if(operation == 4) {
                            int authToken = (int) inStream.readObject();
                            if (!accounts.containsKey(authToken))
                                outStream.writeObject("Invalid auth token");

                            else {
                                Account account = accounts.get(authToken);
                                List<Message> mailbox = account.getMailbox();
                                StringBuilder sb = new StringBuilder();

                                for (Message message : mailbox) {
                                    sb.append(message.getMessageId()).append(". from: ").append(message.getSender());
                                    if (!message.isRead())
                                        sb.append("*");

                                    sb.append("\n");
                                }

                                outStream.writeObject(sb.toString());
                            }
                        }
                        if(operation == 5){
                            int authToken = (int) inStream.readObject();
                            int messageId = (int) inStream.readObject();

                            if (!accounts.containsKey(authToken))
                                outStream.writeObject("Invalid auth token");
                            else if (!messages.containsKey(messageId))
                                outStream.writeObject("Invalid message id");

                            else {
                                Account account = accounts.get(authToken);
                                Message message = messages.get(messageId);
                                for (Message msg : account.getMailbox())
                                    if (msg.getMessageId() == messageId)
                                    {
                                        msg.setRead(true);
                                        outStream.writeObject("(" + message.getSender() + ")" + message.getMessage());
                                        break;
                                    }

                            }
                        }
                        if(operation == 6){
                            int authToken = (int) inStream.readObject();
                            int messageId = (int) inStream.readObject();

                            if (!accounts.containsKey(authToken))
                                outStream.writeObject("Invalid auth token");

                            else {
                                Account account = accounts.get(authToken);
                                Message messageToDelete = null;
                                for (Message message : account.getMailbox())
                                    if (message.getMessageId() == messageId) {
                                        messageToDelete = message;
                                        break;
                                    }

                                if (messageToDelete == null)
                                    outStream.writeObject("Message does not exist");
                                else {
                                    account.getMailbox().remove(messageToDelete);
                                    outStream.writeObject("OK");
                                }
                            }
                        }

                }
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    inStream.close();
                    outStream.close();
                    socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        System.out.println("Server is running on port " + port + "...");

        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    while (true) {
                        Socket socket = serverSocket.accept();
                        new Thread(new ClientHandler(socket)).start();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        inputThread.start();
    }
    private static class Message {
        private String sender;
        private String message;
        private boolean isRead;
        private int messageId;

        public Message(String sender, String message, boolean isRead, int messageId) {
            this.sender = sender;
            this.message = message;
            this.isRead = isRead;
            this.messageId = messageId;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }

        public int getMessageId() {
            return messageId;
        }
    }

    private static class Account {
        private String username;
        private int authToken;
        private List<server.Message> mailbox;

        public Account(String username, int authToken) {
            this.username = username;
            this.authToken = authToken;
            this.mailbox = new ArrayList<>();
        }

        public String getUsername() {
            return username;
        }

        public int getAuthToken() {
            return authToken;
        }

        public List<server.Message> getMailbox() {
            return mailbox;
        }
    }
}
