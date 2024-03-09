/*
 * this class is creates and redirects the client to a server thread after 
 * creating it and passes onto the thread the relevant information of the 
 * client needed to work properly
 */
package server;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server extends Connection{

	private KeyPair keyPair;
	List<Room> rooms=new ArrayList<Room>();
	Semaphore roomCreation= new Semaphore(1);
	public Server() throws IOException, NoSuchAlgorithmException {
		super("server");
		KeyPairGenerator kpa;
		kpa = KeyPairGenerator.getInstance("RSA");
		keyPair=kpa.generateKeyPair();
	}
	
	public void startServer() {
	
		try {
			System.out.println("Waiting...");
			int servNum=1;
			while(true){
				cs=ss.accept();
				ServerThread serv= new ServerThread("CharServer"+servNum,this.cs,this.rooms,this.roomCreation,this.keyPair.getPublic(),this.keyPair.getPrivate());
				serv.run();
				servNum++;
			}
		} catch (Exception e) {
			System.out.println("Client disconnected!");
			e.printStackTrace();
		}
	}

}
