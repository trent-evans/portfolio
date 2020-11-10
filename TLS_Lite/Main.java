import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;

public class Main {

    // Server common name = Trent
    // Client common name = Leon

    public static void main(String[] args) throws Exception {

        // Client generation of the nonce
        byte[] clientNonce = new byte[32];
        SecureRandom secRan = new SecureRandom();
        secRan.nextBytes(clientNonce);

        /** Server gets ready to send information */

        // Certificate
        String serverCertificateFileName = "CASignedServerCertificate.pem";
        Certificate serverCertificate = CertificateReader.readInCertificate(serverCertificateFileName);

        // Public Key
        BigInteger serverSecret = SecretGenerator.generateNewSecret();
        BigInteger serverPublicKey = Keys.makePublicKey(serverSecret);

        // Signed public key
        String serverKeyPath = "serverPrivateKey.der";
        PrivateKey serverPrivateKey = Keys.readInPrivateKey(serverKeyPath);
        byte[] signedServerPublicKey = Keys.signPublicKey(serverPrivateKey,serverPublicKey);

        /** Client recives, verifies, and prepares to send its own information */

        // Verify the server's digital signature
        if(!SignatureVerify.verifySignature(serverCertificate,serverPublicKey,signedServerPublicKey)){
            System.err.println("Certificate not verified as being from server");
            return;
        }

        // Certificate
        String clientCertificateFileName = "CASignedClientCertificate.pem";
        Certificate clientCertificate = CertificateReader.readInCertificate(clientCertificateFileName);

        // Public Key
        BigInteger clientSecret = SecretGenerator.generateNewSecret();
        BigInteger clientPublicKey = Keys.makePublicKey(clientSecret);

        // Signed public key
        String clientKeyPath = "clientPrivateKey.der";
        PrivateKey clientPrivateKey = Keys.readInPrivateKey(clientKeyPath);
        byte[] signedClientPublicKey = Keys.signPublicKey(clientPrivateKey,clientPublicKey);

        /** Server verifies client certificate */
        if(!SignatureVerify.verifySignature(clientCertificate,clientPublicKey,signedClientPublicKey)){
            System.err.println("Certificate not verified as being from client");
            return;
        }

        /** Both sides calculate the shared DEHKey */

        BigInteger clientSharedDEHKey = Keys.getDiffeHellmanSecretKey(serverPublicKey,clientSecret);
        BigInteger serverSharedDEHKey = Keys.getDiffeHellmanSecretKey(clientPublicKey,serverSecret);

        // Verify that everything is correct so far
        if(!clientSharedDEHKey.equals(serverSharedDEHKey)){
            System.err.println("Diffe Hellman private keys are not the same");
            return;
        }

        /** Server calculation of the session key, MAC, and IV for CBC */

        SecretKeys serverSecretKeys = new SecretKeys();
        serverSecretKeys.makeSecretKeys(clientNonce,serverSharedDEHKey);

        /** Client calculation of the session key, MAC, and IV for CBC */
        SecretKeys clientSecretKeys = new SecretKeys();
        clientSecretKeys.makeSecretKeys(clientNonce,clientSharedDEHKey);

        // Verify that everything is correct so far
        if(!clientSecretKeys.equals(serverSecretKeys)){
            System.err.println("Secret key generation not equal");
            return;
        }

        /** Server prepares to send a MAC'd version of the message history */

        MessageHistory serverMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,null);

        byte[] messageHistoryPlusMAC = serverMessageHistory.createMAC(serverSecretKeys.serverMAC);

        /** Client checks the MAC'd version of the message history, then builds their complete message history, then MACs and sends */

        // Verify the Server MAC history
        MessageHistory fullServerMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,null);

        if(!fullServerMessageHistory.verifyMAC(clientSecretKeys.serverMAC,messageHistoryPlusMAC)){
            System.err.println("MAC Message from server not authenticated");
            return;
        }

        // Build, MAC, and send client complete message history
        MessageHistory clientMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,messageHistoryPlusMAC);

        byte[] clientMessageHistoryPlusMAC = clientMessageHistory.createMAC(clientSecretKeys.clientMAC);

        /** Server verifies the MAC'ed client message history */

        MessageHistory serverClientMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,messageHistoryPlusMAC);

        if(!serverClientMessageHistory.verifyMAC(serverSecretKeys.clientMAC,clientMessageHistoryPlusMAC)){
            System.err.println("MAC Message from client not authenticated");
            return;
        }

//        /** Begin encrypted messages */
//
//        String handshakeComplete = "Handshake complete!\n";
//
//        byte[] messageAndMac = EncryptAndDecrypt.macTheMessageAndEncrypt(handshakeComplete.getBytes(),
//                serverSecretKeys.serverMAC,);
//
//        /** Decrypt on the server side */
//
//        Cipher clientCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        SecretKeySpec clientEncryptKeySpec = new SecretKeySpec(clientSecretKeys.serverEncrypt,"AES");
//        IvParameterSpec clientIVParameterSpec = new IvParameterSpec(clientSecretKeys.serverIV);
//        clientCipher.init(Cipher.DECRYPT_MODE,clientEncryptKeySpec,clientIVParameterSpec);
//
//        byte[] decryptHandshakeComplete = clientCipher.doFinal(messageAndMac);
//
//        for(int x = 0; x < decryptHandshakeComplete.length; x++){
//            System.out.print((char)decryptHandshakeComplete[x]);
//            if(x == (decryptHandshakeComplete.length -1)){
//                System.out.println();
//            }
//        }


    }


}
