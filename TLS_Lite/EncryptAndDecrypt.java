import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class EncryptAndDecrypt {

    private static byte[] concatenateByteArrays(byte[] originalMessage, byte[] MACMessage){
        byte[] ret = new byte[originalMessage.length + MACMessage.length];
        System.arraycopy(originalMessage,0,ret,0,originalMessage.length);
        System.arraycopy(MACMessage,0,ret,originalMessage.length,MACMessage.length);
        return ret;
    }

    // Method for encrypting

    public static byte[] macTheMessageAndEncrypt(byte[] message, byte[] MACKey, Cipher encryptCipher) throws Exception{

        // Create the MAC and concatenate
        Mac messageMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec MACKeySpec = new SecretKeySpec(MACKey,"RawBytes");
        messageMAC.init(MACKeySpec);
        byte[] MACMessage = messageMAC.doFinal(message);
        byte[] concatenatedMessageAndMAC = concatenateByteArrays(message,MACMessage);

        return encryptCipher.doFinal(concatenatedMessageAndMAC);
    }

    // Method for decrypting - also verifies the MAC to make sure the information is good

    public static byte[] decryptAndVerifyMAC(byte[] encryptedMessage, byte[] MACKey, Cipher decryptCipher) throws Exception{
        // Decrypt
        byte[] decryptedMessagePlusMAC = decryptCipher.doFinal(encryptedMessage);

        int macStart = decryptedMessagePlusMAC.length - 32;

        // Separate the message and the MAC
        byte[] decryptedMessage = Arrays.copyOfRange(decryptedMessagePlusMAC,0,macStart);
        byte[] decryptedMAC     = Arrays.copyOfRange(decryptedMessagePlusMAC,macStart,decryptedMessagePlusMAC.length);

        // MAC the decrypted message to make sure its the right one
        SecretKeySpec MACSpec = new SecretKeySpec(MACKey,"RawBytes");
        Mac verifyMAC = Mac.getInstance("HmacSHA256");
        verifyMAC.init(MACSpec);

        byte[] messageMAC = verifyMAC.doFinal(decryptedMessage);

        // Verify that the MACs are correct
        if(Arrays.equals(decryptedMAC,messageMAC)){
            return decryptedMessage; // If they are, return the message
        }
        return null;
    }

}
