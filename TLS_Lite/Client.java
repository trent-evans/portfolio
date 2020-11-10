import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.print("Client attempting to connect to server...\n");
        Socket clientSocket = new Socket("localhost",8123);

        System.out.println("Connection successful!\n");

        ObjectOutputStream clientObjectOutStream = new ObjectOutputStream(clientSocket.getOutputStream());
        clientObjectOutStream.flush();
        ObjectInputStream clientObjectInStream = new ObjectInputStream(clientSocket.getInputStream());

        byte[] clientNonce = new byte[32];
        SecureRandom secRan = new SecureRandom();
        secRan.nextBytes(clientNonce);

        clientObjectOutStream.writeObject(clientNonce);
//        clientObjectOutStream.flush();
        System.out.println("Client nonce sent\n");

        /** Client receives and verifies the server information */

        Certificate serverCertificate     = (Certificate) clientObjectInStream.readObject();
        BigInteger  serverPublicKey       = (BigInteger)  clientObjectInStream.readObject();
        byte[]      signedServerPublicKey = (byte[])      clientObjectInStream.readObject();

        // Verify the server's digital signature
        if(!SignatureVerify.verifySignature(serverCertificate,serverPublicKey,signedServerPublicKey)){
            System.err.println("Certificate not verified as being from server - closing connection");
            clientSocket.close();
            return;
        }else{
            System.out.println("Server certificate and digital signature authenticated\n");
        }

        /** Client prepare their own certificate, public key, and signed public key, and sends them to the server */

        // Certificate
        String clientCertificateFileName = "CASignedClientCertificate.pem";
        Certificate clientCertificate = CertificateReader.readInCertificate(clientCertificateFileName);
        clientObjectOutStream.writeObject(clientCertificate);

        // Public Key
        BigInteger clientSecret = SecretGenerator.generateNewSecret();
        BigInteger clientPublicKey = Keys.makePublicKey(clientSecret);
        clientObjectOutStream.writeObject(clientPublicKey);

        // Signed public key
        String clientKeyPath = "clientPrivateKey.der";
        PrivateKey clientPrivateKey = Keys.readInPrivateKey(clientKeyPath);
        byte[] signedClientPublicKey = Keys.signPublicKey(clientPrivateKey,clientPublicKey);
        clientObjectOutStream.writeObject(signedClientPublicKey);

        /** Calculate the Diffe Hellman private key and use that to generate the secret keys */

        BigInteger sharedDEHSecretKey = Keys.getDiffeHellmanSecretKey(serverPublicKey,clientSecret);

        SecretKeys clientSecretKeys = new SecretKeys();
        clientSecretKeys.makeSecretKeys(clientNonce,sharedDEHSecretKey);

        /** Client receives MAC'd message history from server and verifies it */

        byte[] serverMessageHistoryPlusMAC = (byte[]) clientObjectInStream.readObject();

        MessageHistory clientMadeServerMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,null);

        if(!clientMadeServerMessageHistory.verifyMAC(clientSecretKeys.serverMAC,serverMessageHistoryPlusMAC)){
            System.err.println("MAC'd history from server not authenticated - closing connection");
            clientSocket.close();
            return;
        }else{
            System.out.println("MAC'd history from server authenticated\n");
        }

        /** Client generates its own message history, MACs it, and sends it to the server */
        MessageHistory clientMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,serverMessageHistoryPlusMAC);

        byte[] clientMessageHistoryPlusMAC = clientMessageHistory.createMAC(clientSecretKeys.clientMAC);
        clientObjectOutStream.writeObject(clientMessageHistoryPlusMAC);

        /** Prepare to receive the file from the server */

        // Create ciphers for encrypting and decrypting
        SecretKeySpec encryptKeySpec = new SecretKeySpec(clientSecretKeys.clientEncrypt,"AES");
        IvParameterSpec encryptIVSpec = new IvParameterSpec(clientSecretKeys.clientIV);

        Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE,encryptKeySpec,encryptIVSpec);

        SecretKeySpec decryptKeySpec = new SecretKeySpec(clientSecretKeys.serverEncrypt,"AES");
        IvParameterSpec decryptIVSpec = new IvParameterSpec(clientSecretKeys.serverIV);

        Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE,decryptKeySpec,decryptIVSpec);

        FileOutputStream fileOut = new FileOutputStream("messageOut.txt");

        byte[] encryptedFirstHalfOfMessage = (byte[]) clientObjectInStream.readObject();

        byte[] decryptedMessage = EncryptAndDecrypt.decryptAndVerifyMAC(encryptedFirstHalfOfMessage,clientSecretKeys.serverMAC,decryptCipher);

        if(decryptedMessage != null) {
            fileOut.write(decryptedMessage);
        }else{
            System.err.println("Bad message - closing connection");
            clientSocket.close();
            return;
        }

        byte[] firstHalfAccept = ("ACk 1 - First part of the message received and written to file").getBytes();

        clientObjectOutStream.writeObject(firstHalfAccept);

        byte[] encryptedSecondHalfOfMessage = (byte[]) clientObjectInStream.readObject();

        byte[] decryptedSecondMessage = EncryptAndDecrypt.decryptAndVerifyMAC(encryptedSecondHalfOfMessage,clientSecretKeys.serverMAC,decryptCipher);

        if(decryptedSecondMessage != null){
            fileOut.write(decryptedSecondMessage);
        }else{
            System.err.println("Bad message - closing connection");
            clientSocket.close();
            return;
        }

        byte[] secondHalfAccept = ("Message Part 2 R").getBytes();

        clientObjectOutStream.writeObject(secondHalfAccept);

        System.out.println("File received, terminating the connection");
        clientSocket.close();
        fileOut.close();
    }
}
