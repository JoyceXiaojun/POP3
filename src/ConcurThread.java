import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @file: ConcurThread.java
 *
 * @author: Xiaojun Li & Shunqin Ye
 *
 * @date: April 29, 2016 1:13:37 AM EST
 *
 */

public class ConcurThread extends Thread {

    /* The newly created socket by the server. */
    private Socket clientSock = null;
    static UserDatabase user = new UserDatabase();
    private static HashMap<String, String> map = user.getMap();
    private static File[] mails = null;
    private static ArrayList<File> mail = new ArrayList<>();
    private static HashSet<Integer> delete = new HashSet<Integer>();
    private static boolean isUser = false;
    private static boolean isConnected = false;
    private static boolean isLogin = false;
    private static String path;
    private static String username;

    BufferedReader in;
    DataOutputStream out;

    public ConcurThread(Socket clientSock) {
        this.clientSock = clientSock;

        // Set a timeout to the connection, if it remains inactive for 30 seconds, close the connection.
        try {
            clientSock.setSoTimeout(300000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start thread and handle request.
     */
    public void run() {
        if (openConnection()) {
            handleRequest();
            closeConnection();
        }
    }


    /**
     * Open connection with client.
     *
     * @return
     */
    private boolean openConnection() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            out = new DataOutputStream(clientSock.getOutputStream());
            if (clientSock != null && clientSock.isConnected()) {
                isConnected = true;

                System.out.println(
                        "Accpeted new connection from " + clientSock.getInetAddress() + ":" + clientSock.getPort());

            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to establish connection with the client");
            return false;
        }
    }


    /**
     * Handle client request.
     *
     */
    private void handleRequest() {

        String buffer = null;

        try {

            String response;
            response = "+OK xiaojun Mail Server POP3 ready\r\n";
            out.writeBytes(response);
            out.flush();
            while (isConnected && (buffer = in.readLine()) != null) {
                System.out.println(buffer);
                String request = null;
                try {
                    if (buffer.length() < 4 && !buffer.startsWith("TOP")) {
                        buffer = buffer +"    ";
                    }
                    request = buffer.substring(0, 4).trim();
                    if (request.startsWith("TOP")) {

                        request = request.substring(0, 3);
                    }
                } catch (Exception e) {
                    response = "Error, please re-send your request.\n";
                    out.writeBytes(response);
                    out.flush();
                    continue;
                }

                try {
                    switch (request) {
                        case "USER":
                            response = doUser(buffer);
                            break;
                        case "PASS":
                            response = doPass(buffer);
                            break;
                        case "STAT":
                            response = doStat();
                            break;
                        case "LIST":
                            response = doList();
                            break;
                        case "RETR":
                            response = doRetr(buffer);
                            break;
                        case "TOP":
                            response = doTop(buffer);
                            break;
                        case "DELE":
                            response = doDele(buffer);
                            break;
                        case "QUIT":
                            response = doQuit();
                            isConnected = false;
                            break;
                        default:
                            //isConnected = false;
                            response = "-Err: No such command! Please re-send your request.\r\n";
                    }
                } catch (Exception e) {
                    response = "Error, please re-send your request. \n";
                    out.writeBytes(response);
                    out.flush();
                    System.out.println(e);
                    continue;
                }

                out.writeBytes(response);
                out.flush();
            }

        } catch (IOException e) {
            closeConnection();
            System.err.println("Close connection run!!!!");
            e.printStackTrace();
        }

    }


    private String doStat() {
        // TODO Auto-generated method stub
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        int i;
        int oct = 0;
        //System.out.println(mails.length);
        for (i = 0; i < mail.size(); i++) {
            oct += mail.get(i).length();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("+OK " + mail.size() + " " + oct + "\r\n");
        return sb.toString();
    }

    /**
     * Close server-client connection.
     */
    private void closeConnection() {
        try {
            clientSock.close();
            in = null;
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to close connection.");
        }
    }



    private static String doQuit() {
        // TODO Auto-generated method stub
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        if (delete.size() > 0) {
            for (int i : delete) {
                mails[i].delete();
            }
        }
        path = null;
        username = null;
        mails = null;
        mail.clear();
        delete = new HashSet<Integer>();
        isUser = false;
        isConnected = false;
        isLogin = false;
        return "+OK xiaojun POP3 server signing off\r\n";
    }

    private static String doDele(String indexs) {
        // TODO Auto-generated method stub
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        String response;
        if (indexs.length() < 6) {
            return "-ERR: Please re-send your request.\r\n";
        }
        int index = Integer.parseInt(indexs.split(" ")[1]);
        if (indexs == null || index < 0 || index > mail.size()) {
            return "-ERR: no such message\r\n";
        }
        if (!delete.contains(index)) {
            delete.add(index);
        } else {
            return "-ERR: Message has deleted.\r\n";
        }
        response = "+OK message " + index + " deleted\r\n";
        return response;
    }

    private static String doTop(String indexs) throws FileNotFoundException {
        // TODO Auto-generated method stub
        //String response;
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        if (indexs.length() < 7) {
            return "-ERR: Please re-send your request.\r\n";
        }

        String args[] = indexs.split(" ");
        int index = Integer.parseInt(args[1]);
        int numOfLine = Integer.parseInt(args[2]);
        if (indexs == null || index < 0 || index > mail.size()) {
            return "-ERR: no such message\r\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("+OK " + mail.get(index-1).length() + " octets\r\n");
        BufferedReader bf = new BufferedReader(new FileReader(mail.get(index-1)));
        String line;
        try {
            while ((line = bf.readLine()).length() != 0) {
                sb.append(line+"\r\n");
            }
            if (numOfLine > 0) {
                sb.append("\r\n");
            } 
            while (numOfLine > 0 && (line = bf.readLine())!=null) {
                sb.append(line+"\r\n");
                numOfLine--;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                sb.append(".\r\n");
                bf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static String doRetr(String indexs) throws FileNotFoundException {
        // TODO Auto-generated method stub
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        if (indexs.length() < 6) {
            return "-ERR: Please re-send your request.\r\n";
        }
        int index = Integer.parseInt(indexs.split(" ")[1]);
        if (indexs == null || index < 0 || index > mail.size()) {
            return "-ERR: no such message\r\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("+OK " + mail.get(index-1).length() + " octets\r\n");
        BufferedReader bf = new BufferedReader(new FileReader(mail.get(index-1)));
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                sb.append(line + "\r\n");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                sb.append(".\r\n");
                bf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static String doList() {
        // TODO Auto-generated method stub
        if (!isLogin) {
            return "-ERR: Please Login.\r\n";
        }
        int i;
        int oct = 0;
        //System.out.println(mails.length);
        for (i = 0; i < mail.size(); i++) {
            oct += mail.get(i).length();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("+OK " + mail.size() + " messages (" + oct + " octets)\r\n");
        for (i = 0; i < mail.size(); i++) {
            sb.append(i+1 + " " + mail.get(i).length() + "\r\n");
        }
        sb.append(".\r\n");
        return sb.toString();
    }

    private static String doPass(String pass) {
        // TODO Auto-generated method stub
        if (isLogin) {
            return "-ERR: A user has logged in.\r\n";
        }
        if (pass.length() < 6) {
            return "-ERR: Please Enter your password.\r\n";
        }
        if (isUser && pass.substring(5).equals(map.get(username))) {
            
            isLogin = true;
            File file = new File(path);
            //System.out.println(file.exists());
            mails = file.listFiles();
            int i;
            String name;
            for (i = 0; i < mails.length;i++) {
                name = mails[i].getName();
                
                if (name.endsWith("txt")) {
                    
                    mail.add(mails[i]);
                }
            }
            //System.out.println(mails.length);
            return "+OK Password accepted\r\n";
        } else {
            isConnected = false;
            return "-ERR: Password Denied.\r\n";
        }
    }

    private static String doUser(String user) {
        // TODO Auto-generated method stub
        if (isLogin) {
            return "-ERR: A user has logged in.\r\n";
        }
        if (user.length() < 6) {
            return "-ERR: Please Enter your username.\r\n";
        }
        username = user.substring(5);

        if (map.containsKey(username)) {
            isUser = true;
            path = "MailBox/" + username + "/";
            //System.out.println(path);
            return "+OK User accepted\r\n";
        } else {
            isConnected = false;
            return "-ERR: Username Denied.\r\n";
        }
    }
    
}