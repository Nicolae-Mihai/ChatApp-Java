/*
 * This class is the class where the logic for the server is implemented and 
 * the client is connected to a thread specific to itself
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ServerThread extends Thread{
	private Random random= new Random();
	private String name;
	private Socket cs;
	private char[] leters= {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	private int[] numbers= {0,1,2,3,4,5,6,7,8,9,0};
	private List<Room>rooms;
	private Semaphore semRoomCreation;
	private ClientStorage client;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private PublicKey clientPublicKey;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public ServerThread(String name,Socket cs,List<Room>rooms,Semaphore roomCreation,PublicKey publicKey, PrivateKey privateKey ) throws IOException{
		try {
			this.name = name;
			this.rooms = rooms;
			this.semRoomCreation = roomCreation;
			this.cs = cs;
			this.publicKey = publicKey;
			this.privateKey = privateKey;
			this.out = new ObjectOutputStream(cs.getOutputStream());
			this.in = new ObjectInputStream(cs.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run(){
		try{

			clientPublicKey = (PublicKey) in.readObject();
			this.out.writeObject(publicKey);

			String clientName =decrypt((byte[])this.in.readObject());

			//Sends a message to the client using it's out tunnel
			System.out.println(clientName +" is online");
			this.client=new ClientStorage(clientName, out, in,clientPublicKey);
			out.writeObject(encrypt("Request recieved and accepted by "+ name, clientPublicKey));

			while(true) {
				if(checkClient()) 
					out.writeObject(encrypt(clientName+" is connected to a room!", clientPublicKey));
				
				broadcast();
				String message =(String) decrypt((byte[]) in.readObject());
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

	/*
	 * pre:
	 * post:This method acts in conformity with the clients's request and depending on the action
	 * the client decides to make acts on it.
	 */
	public void action(String[] parts,ObjectOutputStream out) throws Exception{
//	TODO: clean up the message parts so they are easier to understand i.e. assign them to variables
		switch (parts[0]) {

		case "1": // join room
			String[] roomDetails = parts[1].split("#");//Split the message sent by the client and read its instructions
			String serverMessage;
			try {
				if(!rooms.isEmpty()) {
					for(Room room:rooms)
						if(room.getName().equals(roomDetails[0]) && room.getId().equals(roomDetails[1])) {
							if(room.getIsProtected()) {
								out.writeObject(encrypt( "Please enter the password",clientPublicKey));
								for(int i = 0; i < 3; i ++) {
									String clientPassword = (String) decrypt((byte[]) in.readObject());  // Wait for password || it stops here
									if (room.getPassword().equals(clientPassword)) {  // Check password validity
										room.getConnectedClients().add(client); // Add the client to the list of clients of the room
										serverMessage = "Connected to room " + room.getName() + "#" + room.getId();
										out.writeObject(encrypt(serverMessage, clientPublicKey));
//										out.writeObject(encrypt("Please start writing your message!\n", clientPublicKey));
										
										break;
									} else {
										serverMessage = "Invalid password. You have " + (2 - i) + " tries left";
										if(i==2) {
											serverMessage = "Failed to connect. Incorrect password";
										}
										out.writeObject(encrypt(serverMessage, clientPublicKey));
									}
								}
							} else {
								serverMessage = "The room has no password";
								out.writeObject(encrypt(serverMessage, clientPublicKey));
								room.getConnectedClients().add(client); // Add the client to the list of clients of the room
//								serverMessage = "Connected to room " + room.getName() + "#" + room.getId();
//								out.writeObject(encrypt(serverMessage, clientPublicKey));
							}
						}else  	out.writeObject(encrypt("No room with that name exists!", clientPublicKey));
				}else 	out.writeObject(encrypt("There are no rooms created!", clientPublicKey));
			} catch (IndexOutOfBoundsException ioobe) {
				if(roomDetails[1]==null)
					out.writeObject(encrypt("No room ID provided please keep in mind to separate the room name and ID with '#'\n", clientPublicKey));
				else 
					out.writeObject(encrypt("An error ocurred while processing your request\n", clientPublicKey));
			}
			break;

		case "2": // create room
			semRoomCreation.acquire();
			if(!parts[2].equalsIgnoreCase("null")) 
				rooms.add(new Room(parts[1], parts[2], checkIdValidity()));
			else 
				rooms.add(new Room(parts[1], checkIdValidity()));
			out.writeObject(encrypt("Room created successfully!", clientPublicKey));
			semRoomCreation.release();
			break;


		case "3": // show rooms
			String roomList="This are all the rooms available:\n";
			for(Room room:rooms)
				roomList+="~ "+room.getName()+"#"+room.getId()+"\n";
			out.writeObject(encrypt(roomList,clientPublicKey));
			break;

		case "4": //disconnect client

			break;

		default:
			out.writeObject(encrypt("Something unexpected happened while processing your request", clientPublicKey));

			System.out.println();
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
	 * post:This method broadcasts to all the clients in the same room as the client what the client 
	 * sends to this server thread
	 */
	private void broadcast() throws Exception {
		for(Room room:rooms)
			while(room.getConnectedClients().contains(this.client)) {
				String clientMessage=decrypt((byte[]) this.client.getIn().readObject());

				if(clientMessage.equalsIgnoreCase("/disconnect")) {
					room.getConnectedClients().remove(this.client);
					client.getOut().writeObject(encrypt("disconnected", client.getPublicKey()));
					break;
				}

				//				this is here for testing purposes it echos bact to the client the messages it sends 
				//to test the encryption and connectivity
				//client.getOut().writeObject(encrypt("recieved: "+clientMessage,clientPublicKey));
				for(ClientStorage client:room.getConnectedClients())
					if(!this.client.equals(client))
						client.getOut().writeObject(encrypt(clientMessage, client.getPublicKey()));
			}
	}
	/*
	 * pre:
	 * post: method to encrypt a message. It asks for a public key so it can be used in broadcast too.
	 */
	private byte[] encrypt(String message, PublicKey clientKey) throws Exception{
		Cipher cipher=Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, clientKey);
		return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
	}
	/*
	 * pre:
	 * post: method to decrypt a message the same as encrypt but it decrypts.
	 */
	private String decrypt(byte[] encryptedMessage) {
		byte[] decryptedMessage = {};
		try {
			Cipher cipher;
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			decryptedMessage=cipher.doFinal(encryptedMessage);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		return new String(decryptedMessage);
	}
}