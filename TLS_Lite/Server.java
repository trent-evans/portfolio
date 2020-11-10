import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;

public class Server {

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing server\n");
        ServerSocket server = new ServerSocket(8123);
        System.out.println("Waiting for connections...\n");

        Socket serverSocket = server.accept();
        System.out.println("Connection to client established");

        ObjectOutputStream serverObjectOutStream = new ObjectOutputStream(serverSocket.getOutputStream());
        serverObjectOutStream.flush();
        ObjectInputStream serverObjectInStream = new ObjectInputStream(serverSocket.getInputStream());

        byte[] clientNonce = (byte[]) serverObjectInStream.readObject();
        System.out.println("Client nonce received\n");

        /** Server sends certificate, public key, and signed public key */

        // Certificate
        String serverCertificateFileName = "CASignedServerCertificate.pem";
        Certificate serverCertificate = CertificateReader.readInCertificate(serverCertificateFileName);
        serverObjectOutStream.writeObject(serverCertificate);

        // Public Key
        BigInteger serverSecret = SecretGenerator.generateNewSecret();
        BigInteger serverPublicKey = Keys.makePublicKey(serverSecret);
        serverObjectOutStream.writeObject(serverPublicKey);

        // Signed public key
        String serverKeyPath = "serverPrivateKey.der";
        PrivateKey serverPrivateKey = Keys.readInPrivateKey(serverKeyPath);
        byte[] signedServerPublicKey = Keys.signPublicKey(serverPrivateKey,serverPublicKey);
        serverObjectOutStream.writeObject(signedServerPublicKey);

        /** Server verifies client information */
        Certificate clientCertificate     = (Certificate) serverObjectInStream.readObject();
        BigInteger  clientPublicKey       = (BigInteger)  serverObjectInStream.readObject();
        byte[]      signedClientPublicKey = (byte[])      serverObjectInStream.readObject();

        if(!SignatureVerify.verifySignature(clientCertificate,clientPublicKey,signedClientPublicKey)){
            System.err.println("Certificate not verified as being from client - closing connection");
            serverSocket.close();
            return;
        }else{
            System.out.println("Client certificate and digital signature authenticated\n");
        }

        /** Calculate the Diffe Hellman private key and use that to generate the secret keys */

        BigInteger sharedDEHSecretKey = Keys.getDiffeHellmanSecretKey(clientPublicKey,serverSecret);

        SecretKeys serverSecretKeys = new SecretKeys();
        serverSecretKeys.makeSecretKeys(clientNonce,sharedDEHSecretKey);

        /** Server creates MAC'd version of the message history, sends to client */

        MessageHistory serverMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,null);

        byte[] serverMessageHistoryPlusMAC = serverMessageHistory.createMAC(serverSecretKeys.serverMAC);
        serverObjectOutStream.writeObject(serverMessageHistoryPlusMAC);

        /** Server receives MAC'd version of the message history from the client and verifies it */

        byte[] clientMessageHistoryPlusMAC = (byte[]) serverObjectInStream.readObject();

        MessageHistory serverClientMessageHistory = new MessageHistory(clientNonce,serverCertificate,serverPublicKey,signedServerPublicKey,
                clientCertificate,clientPublicKey,signedClientPublicKey,serverMessageHistoryPlusMAC);

        if(!serverClientMessageHistory.verifyMAC(serverSecretKeys.clientMAC,clientMessageHistoryPlusMAC)){
            System.err.println("MAC Message from client not authenticated - closing connection");
            serverSocket.close();
            return;
        }else{
            System.out.println("MAC'd Message History from client authenticated\n");
        }
        System.out.println("Handshake complete!\n");

        /** Prepare to send the file to the client */

        // Build out the individual cipher objects for the Server
        SecretKeySpec encryptKeySpec = new SecretKeySpec(serverSecretKeys.serverEncrypt,"AES");
        IvParameterSpec encryptIVSpec = new IvParameterSpec(serverSecretKeys.serverIV);

        Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE,encryptKeySpec,encryptIVSpec);

        SecretKeySpec decryptKeySpec = new SecretKeySpec(serverSecretKeys.clientEncrypt,"AES");
        IvParameterSpec decryptIVSpec = new IvParameterSpec(serverSecretKeys.clientIV);

        Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE,decryptKeySpec,decryptIVSpec);

        // Read in the file
        byte[] countOfMonteCristo = Files.readAllBytes(Paths.get("monte_cristo.txt"));

        // Set up the size of the new file
        int halfFileSize = countOfMonteCristo.length/2;

        // Prepare and send the first half of the file
        byte[] firstHalfCount = Arrays.copyOfRange(countOfMonteCristo,0,halfFileSize);
        byte[] encryptedFirstHalf = EncryptAndDecrypt.macTheMessageAndEncrypt(firstHalfCount,serverSecretKeys.serverMAC,encryptCipher);
        serverObjectOutStream.writeObject(encryptedFirstHalf);

        String expectedFirstACk = "ACk 1 - First part of the message received and written to file";
        // Receive client acceptance message and verify that it's what we expect
        byte[] clientAcceptMessage = (byte[]) serverObjectInStream.readObject();
        byte[] decretyptedClientFirstAccept = EncryptAndDecrypt.decryptAndVerifyMAC(clientAcceptMessage,serverSecretKeys.clientMAC,decryptCipher);
        if(!Arrays.equals(decretyptedClientFirstAccept,expectedFirstACk.getBytes())){
            System.err.println("First half of the message not received correctly - closing connection");
            serverSocket.close();
            return;
        }

        // Prepare and send the second half of the file
        byte[] secondHalfCount = Arrays.copyOfRange(countOfMonteCristo,halfFileSize,countOfMonteCristo.length);
        byte[] encryptedSecondHalf = EncryptAndDecrypt.macTheMessageAndEncrypt(secondHalfCount,serverSecretKeys.serverMAC,encryptCipher);
        serverObjectOutStream.writeObject(encryptedSecondHalf);

        // Receive client second acceptance message and verify that it's what we expect
        byte[] clientSecondAcceptMessage = (byte[]) serverObjectInStream.readObject();
        byte[] decryptedClientSecondAccept = EncryptAndDecrypt.decryptAndVerifyMAC(clientSecondAcceptMessage,serverSecretKeys.clientMAC,decryptCipher);

        if(!decryptedClientSecondAccept.equals(("Message Part 2 R").getBytes())){
            System.err.println("Second half of the message not received correctly - closing connection");
            serverSocket.close();
            return;
        }

        System.out.println("File successfully sent and received - terminating the connection with the client");
        serverSocket.close();
    }
}
