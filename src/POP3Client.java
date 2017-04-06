
/**
 * @file: POP3Client.java
 *
 * @author: Xiaojun Li
 *
 * @date: April 29, 2016 1:13:37 AM EST
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class POP3Client {

    private Socket socket;
    // private boolean debug = true;
    private BufferedReader inStream = null;
    private BufferedWriter outStream = null;
    private String response;
    private int numberOfMail;
    private String directory = "receiver";

    public static void main(String[] args) throws Exception {

        int port = 4567;
        String host = "localhost";
        String user = "user3";
        String password = "user3";
        POP3Client pop3Client = new POP3Client(host, port);

        pop3Client.login(user, password);
        pop3Client.retrieveMails();
        pop3Client.logout();

    }

    private void logout() throws IOException {
        // TODO Auto-generated method stub
        sendRequest("QUIT\r\n");
        response = inStream.readLine();
        System.out.println("S: " + response);
        System.out.println("pop3Client log out!");

        socket.close();
        System.out.println("Connection close with host");
        inStream.close();
        outStream.close();

    }

    private void retrieveMails() throws IOException {
        // TODO Auto-generated method stub
        Queue<String> queue = new LinkedList<String>();
        sendRequest("LIST\r\n");
        String firstLine;
        String line;
        try {
            // System.out.println("debug");

            while (!(line = inStream.readLine()).equals(".")) {
                queue.add(line);
            }
            // System.out.println(queue.size());
            firstLine = queue.poll();
            System.out.println("S: " + firstLine);
            numberOfMail = Integer.parseInt(firstLine.split(" ")[1]);
            // System.out.println(numberOfMail);
            queue.clear();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int i;
        for (i = 1; i <= numberOfMail; i++) {
            sendRequest("RETR " + i + "\r\n");
            String path = i + ".txt";

            File f = new File(directory, path);

            f.createNewFile();

            FileWriter fw = null;
            BufferedWriter writer = null;

            fw = new FileWriter(f);
            writer = new BufferedWriter(fw);

            while (!(line = inStream.readLine()).equals(".")) {

                writer.write(line);
                writer.newLine();

                writer.flush();

            }

            writer.close();
            fw.close();
        }

        for (i = 1; i <= numberOfMail; i++) {
            sendRequest("DELE " + i + "\r\n");
            try {
                System.out.println("S: " + inStream.readLine());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private void login(String user, String password) throws Exception {
        // TODO Auto-generated method stub
        sendRequest("USER " + user + "\r\n");
        try {
            response = inStream.readLine();
            if (!response.startsWith("+OK")) {
                throw new Exception("Error responsed fron server: Username Dennied!");
            } else {
                System.out.println("S: " + response);
            }
            sendRequest("PASS " + password + "\r\n");
            response = inStream.readLine();
            if (!response.startsWith("+OK")) {
                throw new Exception("Error responsed from server: Password Dennied!");
            } else {
                System.out.println("S: " + response);
                File dir = new File(directory);
                dir.mkdir();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendRequest(String string) {
        // TODO Auto-generated method stub
        try {
            outStream.write(string);
            outStream.flush();
            System.out.println("C: " + string.trim());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public POP3Client(String host, int port) throws UnknownHostException, IOException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            response = inStream.readLine();
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection setup...");
        }
    }

}