/*
 * This class facilitates the connection between the client and the server
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection {
    private final int PORT = 1234; //The port used for the connexion
    private final String HOST = "localhost"; //Host used for the connexion
    protected ServerSocket ss; //Server socket
    protected Socket cs; //Client socket
    
    public Connection(String type) throws IOException {//Constructor
        if(type.equalsIgnoreCase("server")) {
            ss = new ServerSocket(PORT);//A socket is created on port 1234 for the server
            //cs = new Socket(); //Client socket
        } else {
            cs = new Socket(HOST, PORT); //Client socket in localhost in port 1234
        }
    }
}