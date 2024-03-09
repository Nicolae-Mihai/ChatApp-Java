/*
 * This class is the constructor and the implements all the logic necessary 
 * for the client to work properly
 */
package client;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.Cipher;


public class Client extends Connection{
	private boolean running=true;
	private String name;
	private PublicKey serverPublicKey;
	private KeyPair keyPair;
	private boolean isConnectedToRoom;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public Client(String name) throws IOException, NoSuchAlgorithmException{
		super("client");
		this.in= new ObjectInputStream(cs.getInputStream());
		this.name=name;
		KeyPairGenerator kpa= KeyPairGenerator.getInstance("RSA");
		this.out= new ObjectOutputStream(cs.getOutputStream());
		this.keyPair = kpa.generateKeyPair();
		this.isConnectedToRoom=false;
	}
	
	public void startClient() {
		try {
			out.writeObject(name);
			
			serverPublicKey=(PublicKey) in.readObject();
			out.writeObject(keyPair.getPublic());
			String message= (String) in.readObject();
			System.out.println(message);
			String option;
			try (Scanner entry = new Scanner(System.in)) {
				while(true) {
					if(isConnectedToRoom) {
						roomMenu(in, out, entry);
					}else {
						System.out.println("\nChoose one option (write only the number)!\n 1. Join chat room!\n 2. Create chat room!\n 3. Show chat rooms!\n 4.Close the app!\n");
						option=entry.nextLine();
						optionResponse(option,out,entry);
	//					String option=entry.nextLine();
	//					out.writeUTF(option);
						if(!running) break;
						String response = (String) in.readObject();
						System.out.println(response);
						if(response.equalsIgnoreCase("Connected to room!"))
							isConnectedToRoom= true;
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Client disconnected!");
		}
		
	}
	
	private void optionResponse(String option,ObjectOutputStream out,Scanner info) throws Exception {

//		try(Scanner info=new Scanner(System.in)){
			String chatRoomName;
			switch (option) {
			case "1": // join chat room
				System.out.println("\nYou chose to join a chat room! Please write the name of the chat room with it's identifier!\n(The room name and the identifier need to be separated by a '#')\n");
				chatRoomName=info.nextLine();
				out.writeObject(option+","+chatRoomName);
				break;
				
			case "2": // create chat room
				
				System.out.println("\nYou chose to create a chat room! To start please provide the name of the chat room!\n");
				chatRoomName= info.nextLine();
				System.out.println("Do you wish to assign a password to this chat room?(Write only the numbers)\n 1.Yes!\n 2.No!\n");
				String passwordProtected=info.nextLine();
				String password="null";
				if(passwordProtected.equalsIgnoreCase("1")) {
					boolean passwordsMatch=false;
					while (!passwordsMatch) {
						
						System.out.println("Please write the password you wish to assing to this chat room!\n");
						password=info.nextLine();
						System.out.println("Please Repeat the password!\n");
						String passwordConfirmation=info.nextLine();
						
						if(password.equals(passwordConfirmation)) {
							System.out.println("The passwords match!\n");
							passwordsMatch=true;

						}else {
							System.out.println("The passwords do not match so we will create the chat room without a password!");
						
						}
					}
				}
				out.writeObject(option+","+chatRoomName+","+password);
				break;
				
			case "3": // show chat rooms
				out.writeObject(option);
				break;
				
			case "4": // exit 
				this.running=false;
				break;
				
			default:
				System.out.println("That option is no available! Please write only the number.");
			}
		}
//	}
	
	private void roomMenu(ObjectInputStream in ,ObjectOutputStream out, Scanner entry) throws IOException, ClassNotFoundException {
		out.writeObject(entry.nextLine());
		String response= (String) in.readObject();
		if(response.equalsIgnoreCase("disconnected"))
			this.isConnectedToRoom=false;
		System.out.println(response);
	}
	
	
	private  byte[] encrypt(String message) throws Exception {
		Cipher cipher= Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);
		
		return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
	}

	private String decrypt(byte[] message) throws Exception{
		Cipher cipher=Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte[] plainText = cipher.doFinal(message);
		return new String(plainText);
	}
}
