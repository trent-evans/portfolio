import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ChatRoom {

    private String roomName;
    private ArrayList<Socket> userSockets;
    private ArrayList<String> jsonMessages;

    public ChatRoom(String roomName){
        this.roomName = roomName;
        userSockets = new ArrayList<>(); // Initialize the user list
        jsonMessages = new ArrayList<>(); // Initialize the message list
    }

    public void addUserToRoom(Socket newUser, String username) throws IOException {
        userSockets.add(newUser);
        // Posts the message history to new entrants
        for(String message : jsonMessages){
            MessageHandling.sendMessage(message,newUser);
        }
        String jsonJoinMessage = "{\"user\": \"" + username +
                "\", \"message\": \"" + " has joined the chat" +
                "\"}";
        jsonMessages.add(jsonJoinMessage);
        // Send join out to all the people in the room
        for(int x = 0; x < userSockets.size(); x++) {
            MessageHandling.sendMessage(jsonJoinMessage, userSockets.get(x));
        }
    }

    public synchronized void roomGetMessage(String sentMessage) throws IOException {
        String[] splitSentMessage = sentMessage.split(" ",2);
        String username = splitSentMessage[0];
        String messageFromUser = splitSentMessage[1];
        String jsonFormatMessage = "{\"user\": \"" + username +
                "\", \"message\": \"" + messageFromUser +
                "\"}";
        jsonMessages.add(jsonFormatMessage);
    }

    public synchronized void roomPostMessage() throws IOException {
        for(int x = 0; x < userSockets.size(); x++){
            MessageHandling.sendMessage(jsonMessages.get(jsonMessages.size()-1),userSockets.get(x));
        }
    }



    public int getMessageArrayLength(){
        return jsonMessages.size();
    }

}
