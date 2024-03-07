/*
 * this class is creates and redirects the client to a server thread after 
 * creating it and passes onto the thread the relevant information of the 
 * client needed to work properly
 */
package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server extends Connection{

	int servNum=1;
	List<Room> rooms=new ArrayList<Room>();
	Semaphore roomCreation= new Semaphore(1);
	public Server() throws IOException {
		super("server");
	}
	
	public void startServer() {
	
		try {
			System.out.println("Waiting...");
			while(true){
				cs=ss.accept();
				ServerThread serv= new ServerThread("CharServer"+servNum,cs,rooms,roomCreation);
				serv.startServer();
				
				servNum++;
			}
		} catch (Exception e) {
			System.out.println("Client disconnected!");
		}
	}

}
