import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageHandling {

    public static String getMessage(Socket clientSocket) throws IOException {
        DataInputStream messageByteStream = new DataInputStream(clientSocket.getInputStream());
        byte[] headerPiece = new byte[2]; // Index 0 = 1000 + ping, pong, data, close; Index 1 = mask + length indicator
        messageByteStream.readFully(headerPiece);

        int lengthIndicator = headerPiece[1] & 0b01111111; // Create unsigned integer of byte length indicator
        int numberOfBytesInLength = 1;

        if(lengthIndicator == 126){
            numberOfBytesInLength = 2;
        }else if(lengthIndicator == 127){
            numberOfBytesInLength = 8;
        }

        long messageLength;

        if(numberOfBytesInLength == 2){
            messageLength = messageByteStream.readShort();
        }else if(numberOfBytesInLength == 8){
            messageLength = messageByteStream.readLong();
        }else{
            messageLength = lengthIndicator;
        }

        //System.out.println(messageLength);

        byte[] maskingKey = new byte[4];
        messageByteStream.readFully(maskingKey);

        byte[] encoded = new byte[(int)messageLength];
        byte[] decoded = new byte[(int)messageLength];

        messageByteStream.readFully(encoded);

        for(int x = 0; x < encoded.length; x++){
            decoded[x] = (byte) (encoded[x]^maskingKey[x%4]);
        }

        // It prints the message! Blessed day!!
        String message = new String(decoded);
        return message;
    }

    public static void sendMessage(String message, Socket clientSocket) throws IOException {
        DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
        byte[] messageBytes = message.getBytes();
        int messageByteLength = messageBytes.length;

        byte opcode = (byte) 0x81; // Opcode for 10000001

        outStream.write(opcode);

        byte lengthIndicator;

        if (messageByteLength < 126) {
            lengthIndicator = (byte) messageByteLength;
            outStream.write(lengthIndicator);
        } else if (messageByteLength < 0x8000) { //32768
            lengthIndicator = (byte) -126;
            outStream.write(lengthIndicator);
            outStream.writeShort(messageByteLength);
        } else {
            lengthIndicator = (byte) -127;
            outStream.write(lengthIndicator);
            outStream.writeLong(messageByteLength);
        }
        outStream.write(messageBytes);
        outStream.flush();
    }


}
