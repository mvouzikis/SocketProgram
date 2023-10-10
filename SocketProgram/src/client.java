import java.io.*;
import java.net.Socket;

public class client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Get host, port, operation, and username
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int operation = Integer.parseInt(args[2]);
        String username = args[3];

        try (Socket socket = new Socket(host, port)) {
            //Create in and out Object streams
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
            // Check the operation and send data accordingly
            if (operation == 1) {
                // Operation 1: Create user
                // Send operation and username to server
                outStream.writeObject(operation);
                outStream.writeObject(username);
                // Read authentication token from server
                int authToken = (int) inStream.readObject();
                // Print response from server
                System.out.println(authToken == 0 ? "Sorry, the user already exists." : "Auth token: " + authToken);
            }
            else if (operation == 2) {
                // Operation 2: Get user list
                int authToken = Integer.parseInt(args[3]);
                outStream.writeObject(operation);
                outStream.writeObject(authToken);
                // Read list of users from server
                String list = (String) inStream.readObject();
                // Print list of users
                System.out.println(list);
            }
            else if (operation == 3) {
                // Operation 3: Send message
                // Get authentication token, recipient, and message
                int authToken = Integer.parseInt(args[3]);
                String recipient = args[4];
                String message = args[5];
                // Send operation, authentication token, recipient, and message to server
                outStream.writeObject(operation);
                outStream.writeObject(authToken);
                outStream.writeObject(recipient);
                outStream.writeObject(message);
                String response = (String) inStream.readObject();
                System.out.println(response);
            }
            else if (operation == 4) {
                // Operation 4: Shows the messages of the messageBox of a user
                int authToken = Integer.parseInt(args[3]);
                outStream.writeObject(operation);
                outStream.writeObject(authToken);
                String message = (String) inStream.readObject();
                System.out.println(message);
            }
            else if (operation == 5) {
                // Operation 5: Marks a message with an ID equal to messageId that it has been read

                int authToken = Integer.parseInt(args[3]);
                int messageId = Integer.parseInt(args[4]);
                outStream.writeObject(operation);
                outStream.writeObject(authToken);
                outStream.writeObject(messageId);
                String response = (String) inStream.readObject();
                System.out.println(response);
            }
            else if (operation == 6) {
                // Operation 5: Deletes the message with an ID of messageId
                int authToken = Integer.parseInt(args[3]);
                int messageId = Integer.parseInt(args[4]);
                outStream.writeObject(operation);
                outStream.writeObject(authToken);
                outStream.writeObject(messageId);
                String response = (String) inStream.readObject();
                System.out.println(response);
            }
        }
    }
}