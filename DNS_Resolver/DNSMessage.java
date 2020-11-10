import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {

    DNSHeader dnsHeader;
    DNSQuestion[] questionArray;
    DNSRecord[] answerArray;
    DNSRecord[] authorityRecordArray;
    DNSRecord[] additionalRecordArray;
    byte[] completeMessage;

    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage decodedMessage = new DNSMessage();
        decodedMessage.completeMessage = bytes;
        ByteArrayInputStream newByteInStream = new ByteArrayInputStream(bytes);
        decodedMessage.dnsHeader = new DNSHeader().decodeHeader(newByteInStream);
//        System.out.println(decodedMessage.dnsHeader.toString());


        // Parse out the DNS Questions
        decodedMessage.questionArray = new DNSQuestion[decodedMessage.dnsHeader.getQuestionCount()];
        for(int x = 0; x < decodedMessage.dnsHeader.getQuestionCount(); x++){
//            if(x == 0){System.out.println("Question Array");}
            decodedMessage.questionArray[x] = new DNSQuestion().decodeQuestion(newByteInStream,decodedMessage);
//            System.out.println(decodedMessage.questionArray[x].toString());
        }

        // Parse out DNS Answers
        decodedMessage.answerArray = new DNSRecord[decodedMessage.dnsHeader.getAnswerCount()];
        for(int x = 0; x < decodedMessage.dnsHeader.getAnswerCount(); x++){
//            if(x == 0){ System.out.println("Answer Array");}
            decodedMessage.answerArray[x] = new DNSRecord().decodeRecord(newByteInStream,decodedMessage);
//            System.out.println(decodedMessage.answerArray[x].toString());
        }

        // Parse out Authority records
        decodedMessage.authorityRecordArray = new DNSRecord[decodedMessage.dnsHeader.getAuthorityCount()];
        for(int x = 0; x < decodedMessage.dnsHeader.getAuthorityCount(); x++){
//            if(x == 0){System.out.println("Authority Array");}
            decodedMessage.authorityRecordArray[x] = new DNSRecord().decodeRecord(newByteInStream,decodedMessage);
//            System.out.println(decodedMessage.authorityRecordArray[x].toString());
        }

        // Parse out Additional Records
        decodedMessage.additionalRecordArray = new DNSRecord[decodedMessage.dnsHeader.getAdditionalCount()];
        for(int x = 0; x < decodedMessage.dnsHeader.getAdditionalCount(); x++){
//            if(x == 0){System.out.println("Additional Records Array");}
            decodedMessage.additionalRecordArray[x] = new DNSRecord().decodeRecord(newByteInStream,decodedMessage);
//            System.out.println(decodedMessage.additionalRecordArray[x].toString());
        }

//        System.out.println(" ");

        return decodedMessage;
    }

    String[] readDomainName(InputStream inStream) throws IOException {
        inStream.mark(3); // Make to make sure you don't lose any bytes in case there's no offset
        short headerInfo = new BigInteger(inStream.readNBytes(2)).shortValue();

        if(headerInfo < 0){ // If there is compression (because there are no unsigned integers in Java)
            short maskedByteValue = (short) (headerInfo & 0x3fff); // Mask: 0011 1111 1111 1111 = 3fff
            return readDomainName(maskedByteValue);

        }else { // If there is no compression
            inStream.reset();
            ArrayList<String> domainNameArrayList = new ArrayList<>();
            byte domainSize = (byte) inStream.read();
            while (domainSize > 0) {
                String domainTemp = "";
                for (int x = 0; x < (int) domainSize; x++) {
                    domainTemp += (char) inStream.read();
                }
                domainNameArrayList.add(domainTemp);
                domainSize = (byte) inStream.read();
            }
            String[] domainNameArray = new String[domainNameArrayList.size()];
            for (int x = 0; x < domainNameArrayList.size(); x++) {
                domainNameArray[x] = domainNameArrayList.get(x);
            }
            return domainNameArray;
        }
    }


    String[] readDomainName(int firstByte) throws IOException {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(completeMessage);
        byte[] stripHeader = byteInStream.readNBytes(firstByte);
        return readDomainName(byteInStream);
    }

    static String octetsToString(String[] octets){
        String fullDomainString = "";
        for(int x = 0; x < octets.length; x++){
            fullDomainString += octets[x] + '.';
        }
        return fullDomainString;
    }

    static DNSMessage buildResponse(DNSMessage request, DNSRecord answer){
        DNSMessage responseMessage = new DNSMessage();
        responseMessage.dnsHeader = DNSHeader.buildResponseHeader(request,answer);
        responseMessage.questionArray = request.questionArray;
        responseMessage.answerArray = new DNSRecord[1];
        responseMessage.answerArray[0] = answer;
        responseMessage.authorityRecordArray = request.authorityRecordArray;
        responseMessage.additionalRecordArray = request.additionalRecordArray;
        return responseMessage;
    }

    static void writeDomainName(ByteArrayOutputStream byteOutStream, HashMap<String,Short> domainLocations, String[] domainPieces) throws IOException {
        String domainName = octetsToString(domainPieces);

        if(!domainLocations.containsKey(domainName)){ // If the domain name isn't in the map
            short pointerToDomainName = (short) byteOutStream.size();
            domainLocations.put(domainName,pointerToDomainName); // Put it in the map
            for(int x = 0; x < domainPieces.length; x++){
                byte[] domainByteName = domainPieces[x].getBytes(); // Then write out the bytes to the outStream
                byte domainLength = (byte) domainByteName.length;
                byteOutStream.write(domainLength);
                byteOutStream.write(domainByteName);
            }
            byte zeroByte = 0;
            byteOutStream.write(zeroByte);

        }else{ // If the domain name is in the map

            if(domainPieces.length != 0) { // If the domainName field isn't empty => Isn't an additional record
                String fullDomainString = "";
                for (int x = 0; x < domainPieces.length; x++) {
                    fullDomainString += domainPieces[x] + '.';
                }
                short pointerToDomain = domainLocations.get(fullDomainString);
                short indicatorOfPointer = (short) 0xc000; // 1100 0000 0000 0000 = 12 0 0 0 = c000
                short domainPointerAndSize = (short) (pointerToDomain | indicatorOfPointer);
                byteOutStream.write(ByteBuffer.allocate(2).putShort(domainPointerAndSize).array());

            }else{ // If it's an additional record, just pop a zero in there
                byte zeroByte = 0;
                byteOutStream.write(zeroByte);
            }
        }
    }

    byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        HashMap<String,Short> domainNameToPointer = new HashMap<>();
        dnsHeader.writeBytes(byteOutStream);

        for(DNSQuestion q: questionArray){
            writeDomainName(byteOutStream,domainNameToPointer,q.QName);
            q.writeBytes(byteOutStream,domainNameToPointer);
        }
        for(DNSRecord ans: answerArray){
            writeDomainName(byteOutStream,domainNameToPointer,ans.domainName);
            ans.writeBytes(byteOutStream,domainNameToPointer);
        }
        for(DNSRecord auth: authorityRecordArray){
            writeDomainName(byteOutStream,domainNameToPointer,auth.domainName);
            auth.writeBytes(byteOutStream,domainNameToPointer);
        }
        for(DNSRecord addR: additionalRecordArray){
            writeDomainName(byteOutStream,domainNameToPointer,addR.domainName);
            addR.writeBytes(byteOutStream,domainNameToPointer);
        }

        return byteOutStream.toByteArray();
    }

    @Override
    public String toString() {
        return "DNSMessage{" +
                "dnsHeader=" + dnsHeader +
                ", questionArray=" + Arrays.toString(questionArray) +
                ", answerArray=" + Arrays.toString(answerArray) +
                ", authorityRecordArray=" + Arrays.toString(authorityRecordArray) +
                ", additionalRecordArray=" + Arrays.toString(additionalRecordArray) +
                '}';
    }
}
