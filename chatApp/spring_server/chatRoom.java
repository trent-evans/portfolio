package demo;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.w3c.dom.Text;

import java.awt.*;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class chatRoom {

    public static Map<WebSocketSession,chatRoom> socketToRoom = new HashMap<>(); // Has the socket and its associated room
    public static Map<String, chatRoom> roomnameAndChatRoom = new HashMap<>(); // Has the room name and its associated room

    public ArrayList<String> MessageHistory = new ArrayList<>();
    public ArrayList<WebSocketSession> userList = new ArrayList<>();

    public static chatRoom findUserRoom(WebSocketSession user){
        return socketToRoom.get(user);
    }

    public static chatRoom getRoomFromName(String room){
        return roomnameAndChatRoom.get(room);
    }

    public static boolean doesRoomExist(String room){
        return roomnameAndChatRoom.containsKey(room);
    }

    public static chatRoom createRoom(String room, WebSocketSession userSocket){
        chatRoom newRoom = new chatRoom();
        roomnameAndChatRoom.put(room,newRoom);
        socketToRoom.put(userSocket,newRoom);
        return newRoom;
    }

    public void joinRoom(WebSocketSession userSocket, chatRoom roomToJoin) throws IOException {
        socketToRoom.put(userSocket,roomToJoin);
        sendMessageHistory(userSocket);
    }

    public void addUserToRoom(WebSocketSession userSocket){
        userList.add(userSocket);
    }

    public void postMessage(String user, String message) throws IOException {
        String jsonString = createJsonString(user,message);
        MessageHistory.add(jsonString);
        TextMessage textMessage = new TextMessage(jsonString);
        for(int x = 0; x < userList.size(); x++){
            userList.get(x).sendMessage(textMessage);
        }
    }

    public String createJsonString(String username, String messageFromUser){
        String jsonString = "{\"user\": \"" + username +
                "\", \"message\": \"" + messageFromUser +
                "\"}";
        return jsonString;
    }

    public void sendMessageHistory(WebSocketSession socket) throws IOException {
        for(int x = 0; x < MessageHistory.size(); x++){
            TextMessage textMessage = new TextMessage(MessageHistory.get(x));
            socket.sendMessage(textMessage);
        }
    }


}
