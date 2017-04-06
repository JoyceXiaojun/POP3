
/**
 * @file: POP3Server.java
 *
 * @author: Shuqin Ye
 *
 * @date: April 29, 2016 1:13:37 AM EST
 *
 */

import java.io.IOException;
import java.net.ServerSocket;

public class POP3ConcurServer {
    private static ServerSocket srvSock;

    public static void main(String args[]) throws Exception {
        int port = 4567;
        try {
            /*
             * Create a socket to accept() client connections. This combines
             * socket(), bind() and listen() into one call. Any connection
             * attempts before this are terminated with RST.
             */
            // int host = 1;
            srvSock = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Unable to listen on port " + port);
            System.exit(1);
        }

        /* Keep the server listening to requests.*/
        while (true) {
            try {
                // Once a new request comes in, start a new thread to handle the request.
                new ConcurThread(srvSock.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}