/*
 * this class is creates and redirects the client to a server thread after 
 * creating it and passes onto the thread the relevant information of the 
 * client needed to work properly
 */
package server;

import java.io.IOException;

public class Server extends Connection{

	int servNum=1;

	public Server() throws IOException {
		super("server");
	}
	
	public void startServer() {
	
		try {
			System.out.println("Waiting...");
			while(true){
				cs=ss.accept();
				ServerThread serv= new ServerThread("CharServer"+servNum);
				serv.startServer();
				
				servNum++;
			}
		} catch (Exception e) {
			System.out.println("Client disconnected!");
		}
	}

}
