package demo;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class webSocketHandler extends TextWebSocketHandler {


    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws IOException {
        String stringMessage = message.getPayload();
        chatRoom room = chatRoom.findUserRoom(socket);
        if(room == null){ // Must be a join message otherwise the room would exist
            String[] splitArray = stringMessage.split(" ",3); // Splits to [0] join and [1] roomname [2]user
            if(chatRoom.doesRoomExist(splitArray[1])){ // If the room does exist
                chatRoom roomToJoin = chatRoom.getRoomFromName(splitArray[1]); // Get the room to join
                roomToJoin.joinRoom(socket,roomToJoin); // Join the room
                roomToJoin.addUserToRoom(socket);
                System.out.println(splitArray[2] + " joined " + splitArray[1]);
                roomToJoin.postMessage(splitArray[2]," has joined the chat");
            }else{ // If it does not
                chatRoom newRoom = chatRoom.createRoom(splitArray[1],socket);
                System.out.println(splitArray[1] + " created");
                System.out.println(splitArray[2] + " joined " + splitArray[1]);
                newRoom.addUserToRoom(socket);
                newRoom.postMessage(splitArray[2], " has joined the chat");
            }
        }else{
            String [] splitArray = stringMessage.split(" ",2);
            room.postMessage(splitArray[0],splitArray[1]);
        }

    }


}
