import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SecretKeys {

    protected byte[] prk;
    protected byte[] serverEncrypt;
    protected byte[] clientEncrypt;
    protected byte[] serverMAC;
    protected byte[] clientMAC;
    protected byte[] serverIV;
    protected byte[] clientIV;

    // Becasue I replace everything later, I don't really have to initialize things now
    SecretKeys(){}

    private byte[] stringToBytesPlusOne(String tag){
        byte[] tagBytes = tag.getBytes();
        byte[] one = new byte[1];
        one[0] = 1;
        byte[] ret = new byte[tagBytes.length + 1];
        System.arraycopy(tagBytes,0,ret,0,tagBytes.length);
        System.arraycopy(one,0,ret,tagBytes.length,1);
        return ret;
    }

    private byte[] hkdfExpand(byte[] key, String tag) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,"RawBytes");
        byte[] tagConcatenate = stringToBytesPlusOne(tag);

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        byte[] macOut = mac.doFinal(tagConcatenate);

        return Arrays.copyOfRange(macOut,0,16);
    }

    public void makeSecretKeys(byte[] clientNonce, BigInteger DHSharedSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec serverSecretKeySpec = new SecretKeySpec(clientNonce,"RawBytes");

        Mac serverMac = Mac.getInstance("HmacSHA256");
        serverMac.init(serverSecretKeySpec); // Initialize MAC with the key

        prk = serverMac.doFinal(DHSharedSecret.toByteArray());
        serverEncrypt = hkdfExpand(prk,"server Encrypt");
        clientEncrypt = hkdfExpand(serverEncrypt, "client Encrypt");
        serverMAC = hkdfExpand(clientEncrypt,"server MAC");
        clientMAC = hkdfExpand(serverMAC,"client MAC");
        serverIV = hkdfExpand(clientMAC,"server IV");
        clientIV = hkdfExpand(serverIV,"client IV");
    }

    // This wasn't necessary, but I did use it to compare when everything was running in main()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecretKeys)) return false;
        SecretKeys that = (SecretKeys) o;
        return Arrays.equals(prk, that.prk) &&
                Arrays.equals(serverEncrypt, that.serverEncrypt) &&
                Arrays.equals(clientEncrypt, that.clientEncrypt) &&
                Arrays.equals(serverMAC, that.serverMAC) &&
                Arrays.equals(clientMAC, that.clientMAC) &&
                Arrays.equals(serverIV, that.serverIV) &&
                Arrays.equals(clientIV, that.clientIV);
    }

}
