//import com.sun.tools.javac.util.Names;
import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Server {

    private static HashMap<String, ChatRoom> chatRoomMap;

    public static void main(String[] args) throws IOException {
        chatRoomMap = new HashMap<>();
        ServerSocket server = new ServerSocket(8080); // server socket
        System.out.println("Server started");
        System.out.println("Waiting for client");

        while (true) {
            Socket socket = server.accept(); // Open socket
            Thread newRequest = new Thread( () -> {
                try {
                    HTTPRequest newHTTPRequest = new HTTPRequest(socket);
                    HTTPResponse.Response(newHTTPRequest, socket);
                    if(newHTTPRequest.isWebSocket()){
                        String message = MessageHandling.getMessage(socket);
                        String[] parsedMessage = message.split(" ", 3);
                        ChatRoom threadRoom = roomAddOrJoin(parsedMessage[1], parsedMessage[2], socket);
                        while(true){
                            threadRoom.roomGetMessage(MessageHandling.getMessage(socket));
                            threadRoom.roomPostMessage();
                        }
                    }
                    socket.close();
                } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });
            newRequest.start();
        }
    }

    public static synchronized ChatRoom roomAddOrJoin(String roomName, String username, Socket newUser)throws IOException{
        if(!chatRoomMap.containsKey(roomName)){
            ChatRoom newRoom = new ChatRoom(roomName);
            System.out.println("Created chatroom: " + roomName);
            chatRoomMap.put(roomName,newRoom);
            newRoom.addUserToRoom(newUser, username);
            System.out.println(username + " joined " + roomName);
            return newRoom;
        }else{
            ChatRoom roomToAddClient = chatRoomMap.get(roomName);
            chatRoomMap.get(roomName).addUserToRoom(newUser, username);
            System.out.println(username + " joined " + roomName);
            return chatRoomMap.get(roomName);
        }
    }

    public static synchronized String createJsonString(String username, String messageFromUser){
        String jsonString = "{\"user\": \"" + username +
                "\", \"message\": \"" + messageFromUser +
                "\"}";
        return jsonString;
    }
}
