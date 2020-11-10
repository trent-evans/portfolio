import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class HTTPRequest {

    private String key;
    private String encodedRequest;
    private File completeFilename;
    private boolean webSocketStatus;

    public HTTPRequest(Socket clientSocket) throws IOException, NoSuchAlgorithmException {
        Scanner scan = new Scanner(clientSocket.getInputStream());

        // Initialize file path/name
        String filename = ""; // Initialize
        String line;
        webSocketStatus = false;
        while (true) { // Read in lines
            // Break on empty lines
            line = scan.nextLine();
            if (line.equals("")) {
                break;
            }
            // Search for GET to find filename
            String[] arrOfLine = line.split(" ", 3); // Size 3 so we have GET, filepath, HTTP version
            if (arrOfLine[0].equals("GET")) {
                filename = arrOfLine[1];
            }else if (arrOfLine[0].equals("Sec-WebSocket-Key:")){ // Parse out the key if necessary
                key = arrOfLine[1];
                encodedRequest = serverEncode(key);
                webSocketStatus = true;
            }
        }
        if (filename.equals("/")) { // Add index.html if no file name is present
//            filename = "/index.html";
            filename = "/clientFrontEnd.html";
        }
        completeFilename = new File("resources" + filename);
    }

    public static String serverEncode(String key) throws NoSuchAlgorithmException{

        final String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String sha1Request = key + magicString;

        MessageDigest encode = MessageDigest.getInstance("SHA-1");
        encode.update(sha1Request.getBytes());
        byte[] requestBytes = encode.digest();
        String encodedString = Base64.getEncoder().encodeToString(requestBytes);
        return encodedString;
    }

    public File getFilename(){
        return completeFilename;
    }
    public void setFilename(File input){
        this.completeFilename = input;
    }
    public boolean isWebSocket(){
        return webSocketStatus;
    }
    public String getKey(){
        return key;
    }
    public String getEncodedRequest(){
        return encodedRequest;
    }
}
