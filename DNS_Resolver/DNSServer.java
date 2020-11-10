import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class DNSServer {

    public static void main(String[] args) throws IOException {
        DNSCache dnsCache = new DNSCache();
        DatagramSocket datagramSocket = new DatagramSocket(8053);
        while(true) {
            byte[] digRequestArray = new byte[512]; // Because that's the packet size
            DatagramPacket digPacket = new DatagramPacket(digRequestArray, digRequestArray.length);
            datagramSocket.receive(digPacket);
            ByteArrayInputStream byteInStream = new ByteArrayInputStream(digPacket.getData());
//            System.out.println("Dig Request");
            DNSMessage decodedDigRequest = new DNSMessage().decodeMessage(digRequestArray);

            if(!dnsCache.questionInCache(decodedDigRequest.questionArray[0])){ // If the valid answer is not in the cache - query to Google
                byte[] googleResponseArray = new byte[512];
                DatagramPacket googleRequestPacket = new DatagramPacket(digPacket.getData(), digPacket.getLength(), InetAddress.getByName("8.8.8.8"), 53);
                DatagramPacket googleResponsePacket = new DatagramPacket(googleResponseArray, googleResponseArray.length);
                datagramSocket.send(googleRequestPacket);
                datagramSocket.receive(googleResponsePacket);
//                System.out.println("Google Answer");
                DNSMessage decodedGoogleAnswer = new DNSMessage().decodeMessage(googleResponseArray);
//                System.out.println("****************");

                if(decodedGoogleAnswer.answerArray.length == 0 || decodedGoogleAnswer.dnsHeader.ResponseCode != 0){ // If there is no answer OR there is an error present
                    System.err.println("Query to nonexistant host - Please enter a valid url");
                    DatagramPacket errorResponsePacket = new DatagramPacket(googleResponsePacket.getData(),googleResponsePacket.getData().length,digPacket.getAddress(),digPacket.getPort());
                    datagramSocket.send(errorResponsePacket);
                    continue;
                }else {
                    dnsCache.addToCache(decodedGoogleAnswer.questionArray[0], decodedGoogleAnswer.answerArray[0]);
                }
            }
            DNSMessage responseMessage = DNSMessage.buildResponse(decodedDigRequest, dnsCache.fetchFromCache(decodedDigRequest.questionArray[0]));
            byte[] responseMessageByteArray = responseMessage.toBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseMessageByteArray,responseMessageByteArray.length,digPacket.getAddress(),digPacket.getPort());
            datagramSocket.send(responsePacket);

        }
    }





}
