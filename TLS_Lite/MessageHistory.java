import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

public class MessageHistory {

    protected byte[] clientNonce;
    protected byte[] serverCertificate;
    protected byte[] serverDHPublicKey;
    protected byte[] signedServerPublicKey;
    protected byte[] clientCertificate;
    protected byte[] clientDHPublicKey;
    protected byte[] signedClientPublicKey;
    protected byte[] serverMAC;

    public MessageHistory(byte[] clientNonce, Certificate serverCertificate, BigInteger serverDHPublicKey, byte[] signedServerPublicKey, Certificate clientCertificate, BigInteger clientDHPublicKey, byte[] signedClientPublicKey, byte[] serverMAC) throws CertificateEncodingException {
        this.clientNonce = clientNonce;
        this.serverCertificate = serverCertificate.getEncoded();
        this.serverDHPublicKey = serverDHPublicKey.toByteArray();
        this.signedServerPublicKey = signedServerPublicKey;
        this.clientCertificate = clientCertificate.getEncoded();
        this.clientDHPublicKey = clientDHPublicKey.toByteArray();
        this.signedClientPublicKey = signedClientPublicKey;
        this.serverMAC = serverMAC;
    }

    private byte[] messageHistoryToByteArray() throws CertificateEncodingException {
        byte[] ret;
        byte[][] allFields;
        int length = 0;
        if(serverMAC == null){
            allFields = new byte[][]{clientNonce,serverCertificate,serverDHPublicKey,signedServerPublicKey,clientCertificate,clientDHPublicKey,signedClientPublicKey};
            length = 7;
        }else{
            allFields = new byte[][]{clientNonce,serverCertificate,serverDHPublicKey,signedServerPublicKey,clientCertificate,clientDHPublicKey,signedClientPublicKey,serverMAC};
            length = 8;
        }
        int totalSize = 0;
        for(int x = 0; x < length; x++){
            totalSize += allFields[x].length;
        }
        ret = new byte[totalSize];
        int currentIdx = 0;
        for(int x = 0; x < length; x++){
            System.arraycopy(allFields[x],0,ret,currentIdx,allFields[x].length);
            currentIdx += allFields[x].length;
        }
        return ret;
    }

    private static byte[] MACTheMessageHistory(byte[] messageHistory, byte[] MACKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac macMessageHistory = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(MACKey,"RawBytes");
        macMessageHistory.init(secretKeySpec);
        return macMessageHistory.doFinal(messageHistory);
    }

    private static byte[] concatenateHistoryAndMAC(byte[] messageHistory, byte[] MACMessageHistory){
        byte[] ret = new byte[messageHistory.length + MACMessageHistory.length];
        System.arraycopy(messageHistory,0,ret,0,messageHistory.length);
        System.arraycopy(MACMessageHistory,0,ret,messageHistory.length,MACMessageHistory.length);
        return ret;
    }

    boolean verifyMAC(byte[] MACKey, byte[] recievedMACMessage) throws CertificateEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] messageHistory = messageHistoryToByteArray();
        byte[] MACedMessageHistory = MACTheMessageHistory(messageHistory,MACKey);
        byte[] messageHistoryPlusMAC = concatenateHistoryAndMAC(messageHistory,MACedMessageHistory);
        return Arrays.equals(recievedMACMessage,messageHistoryPlusMAC);
    }

    byte[] createMAC(byte[] MACKey) throws CertificateEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] messageHistory = messageHistoryToByteArray();
        byte[] MACedMessageHistory = MACTheMessageHistory(messageHistory,MACKey);
        return concatenateHistoryAndMAC(messageHistory,MACedMessageHistory);
    }

}
