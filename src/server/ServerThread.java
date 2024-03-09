/*
 * This class is the class where the logic for the server is implemented and 
 * the client is connected to a thread specific to itself
 */
package server;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

// TODO: Create something so once a client is in a room the main server actually sends messages to all the clients connected to the room.

public class ServerThread {
	private Random random= new Random();
	private String name;
	private Socket cs;
	private char[] leters= {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	private int[] numbers= {0,1,2,3,4,5,6,7,8,9,0};
	private List<Room>rooms;
	private Semaphore roomCreation;
	private ClientStorage client;
	private KeyPair keyPair;
	private PublicKey clientPublicKey;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	public ServerThread(String name,Socket cs,List<Room>rooms,Semaphore roomCreation) throws IOException{
		try {
			this.out= new ObjectOutputStream(cs.getOutputStream());
			this.name=name;
			this.rooms=rooms;
			this.roomCreation=roomCreation;
			this.cs=cs;
			this.in= new ObjectInputStream(cs.getInputStream());
			KeyPairGenerator kpa;
			kpa = KeyPairGenerator.getInstance("RSA");
			keyPair=kpa.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void startServer(){
	    try{
	
	    	String clientName =(String) in.readObject();
	        
			out.writeObject(keyPair.getPublic());
			clientPublicKey = (PublicKey) in.readObject();
	        //Sends a message to the client using it's out tunnel
	        System.out.println(clientName +" is online");
	        out.writeObject("Request recieved and accepted by "+ name);
//	        
	        this.client=new ClientStorage(clientName, out, in,clientPublicKey);
	        
	        while(true) {
	        		
	        		if(checkClient())
	        			System.out.println(clientName+" is connected a room!");
	        			broadcast();
	        	
		            String message =(String) in.readObject();
		            System.out.println(message);
		            String[] parts=message.split(",");
		            action(parts,out);
		        if(message.equalsIgnoreCase("/disconnect")) break;
	        }
	        cs.close();//Closes the connection to the client
	    } catch(Exception e){
	        System.out.println(client.getClientName()+" has disconnected!");
	    }
	}
	
	/*
	 * pre:
	 * post: Generates an random alphanumeric ID to be assigned to the rooms
	 */
	private String idGenerator() {
		String identifier="";
		for(int i=0;i<6;i++) {
			if(random.nextBoolean()) {
				int index=random.nextInt(0, numbers.length);
				identifier+=numbers[index];
			}else {
				int index=random.nextInt(0, leters.length);
				identifier+=leters[index];
			}
		}
		return identifier;
	}
	
	public void action(String[] parts,ObjectOutputStream out) throws IOException, InterruptedException{
		
		switch (parts[0]) {
			case "1": // join room
				String[] roomDetails = parts[1].split("#");
				try {
					if(!rooms.isEmpty()) {
						for(Room room:rooms)
							if(room.getName().equals(roomDetails[0]) || room.getId().equals(roomDetails[1])) {
								roomCreation.acquire();
								room.getConnectedClients().add(client);
								roomCreation.release();
//								out.writeUTF("Connected to room "+room.getName());
								out.writeObject("Connected to room!");
							}
					}
				} catch (IndexOutOfBoundsException ioobe) {
					if(roomDetails[1]==null)
						out.writeObject("No room ID provided please keep in mind to separate the room name and ID with '#'\n");
					else 
						out.writeObject("An error ocurred while processing your request\n");
				}
				break;
				
			case "2": // create room
				if(!parts[2].equalsIgnoreCase("null")) 
					rooms.add(new Room(parts[1], parts[2], checkIdValidity()));
				else 
					rooms.add(new Room(parts[1], checkIdValidity()));
				out.writeObject("Room created successfully!");
				break;
				
			
			case "3": // show rooms
				String roomList="This are all the rooms available:\n";
				for(Room room:rooms)
					roomList+="~ "+room.getName()+"#"+room.getId()+"\n";
					out.writeObject(roomList);
				break;
				
			case "4": //disconnect client
				
				break;

			default:
				System.out.println("Something unexpected happened while processing your request");
		}
	}
	
	/*
	 * pre:
	 * post: Checks if the Id generated by idGenerator() is valid if it's valid it returns it,
	 * if not it calls again the generator until it gets a valid ID.
	 */
	public String checkIdValidity() {
		boolean validId=false;
		String id="";
		
		while(!validId) {
			int count=0;
			id=idGenerator();
		
			if(!rooms.isEmpty())
				for(Room room:rooms)
					if(id.equals(room.getId()))
						count++;
			
			if(count==0)
				validId=true;
		}
		
		return id;
	}

	/*
	 * pre:
	 * post: This checks to see if the client is connected to any room created and returns a boolean accordingly
	 */
	private boolean checkClient() {
		for(Room room:rooms)
			if(room.getConnectedClients().contains(this.client))
				return true;
		return false;
	}
	
	/*
	 * pre:
	 * post:This method broadcasts to all the clients in the same room as the client what the client sends to this server thread
	 */
	private void broadcast() throws IOException, ClassNotFoundException {
		for(Room room:rooms)
			while(room.getConnectedClients().contains(this.client)) {
				String clientMessage=(String) this.client.getIn().readObject();
				if(clientMessage.equalsIgnoreCase("/disconnect")) {
					room.getConnectedClients().remove(this.client);
					client.getOut().writeObject("disconnected");
					break;
				}
				client.getOut().writeObject("recieved: "+clientMessage);
				for(ClientStorage client:room.getConnectedClients())
					if(!this.client.equals(client))
						client.getOut().writeObject(clientMessage);
			}
	}
	/*
	 * pre:
	 * post: method to encrypt a message
	 */
	private byte[] encrypt(String message) throws Exception{
		Cipher cipher=Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, clientPublicKey);
		return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
	}
	/*
	 * pre:
	 * post: method to decrypt a message
	 */
	private String decrypt(byte[] encryptedMessage) {
		byte[] decryptedMessage = {};
		try {
			Cipher cipher;
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			decryptedMessage=cipher.doFinal(encryptedMessage);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return new String(decryptedMessage);
	}
}