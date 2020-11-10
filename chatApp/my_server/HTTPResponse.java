import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class HTTPResponse {

    public static void Response(HTTPRequest newHTTPRequest, Socket clientSocket) throws IOException, InterruptedException {

        // Check if file exists
        File completeFilename = newHTTPRequest.getFilename();

        // Set up output stream and push it into the header
        PrintWriter outStream = new PrintWriter(clientSocket.getOutputStream());

        if(newHTTPRequest.isWebSocket()){
            outStream.println("HTTP/1.1 101 Switching Protocols\r");
            outStream.println("Upgrade: websocket\r");
            outStream.println("Connection: Upgrade\r");
            outStream.println("Sec-WebSocket-Accept: " + newHTTPRequest.getEncodedRequest() + "\r");
            outStream.println("\r");
//            System.out.println("Confirmed");
            outStream.flush();
        }else{
            if(!completeFilename.exists()) {
                completeFilename = new File("resources/error404.html");
            }
                outStream.println("HTTP/1.1 200 OK");
                long fileLength = completeFilename.length(); // Generate content length number
                outStream.println("Content-Length: " + fileLength);
                outStream.println("");
                outStream.flush();
                FileInputStream fileScan = new FileInputStream(completeFilename); // Read in HTML file
                fileScan.transferTo(clientSocket.getOutputStream());

        }
    }

}
