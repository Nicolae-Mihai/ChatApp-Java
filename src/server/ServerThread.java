/*
 * This class is the class where the logic for the server is implemented and 
 * the client is connected to a thread specific to itself
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerThread extends Server implements Runnable{
	String name;

	public ServerThread(String name) throws IOException {
		this.name=name;
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	public void run(){
	    try{
	        System.out.println("Client online");
	
	        DataInputStream in = new DataInputStream(cs.getInputStream());
	        DataOutputStream out = new DataOutputStream(cs.getOutputStream());
	        
	        //Sends a message to the client using it's out tunnel
	        out.writeUTF("Request recieved and accepted by "+ name);
	        while(true) {
	            String message = in.readUTF()+ ",";
	            if(message.equalsIgnoreCase("/disconnect")) break;
	//            System.out.println("Message recieved -> " + message);
	//            out.writeUTF("Vocals en: \"" + message + "\" : " + calculateVocals(message));
	        }
	        cs.close();//Se finaliza la conexión con el cliente
	    } catch(Exception e){
	        e.printStackTrace();
	    }
	}
}