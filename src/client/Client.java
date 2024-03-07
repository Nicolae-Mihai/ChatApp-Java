/*
 * This class is the constructor and the implements all the logic necessary 
 * for the client to work properly
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

// kind of ready, still need some tweaks tho

public class Client extends Connection{
	private boolean running=true;
	private String name;
	private PublicKey serverPublicKey;
	private KeyPair keyPair;
	
	public Client(String name) throws IOException, NoSuchAlgorithmException{
		super("client");
		this.name=name;
		KeyPairGenerator kpa= KeyPairGenerator.getInstance("RSA");
		this.keyPair = kpa.generateKeyPair();
	}
	public void startClient() {
		try {
			DataInputStream in= new DataInputStream(cs.getInputStream());
			DataOutputStream out=new DataOutputStream(cs.getOutputStream());
			ObjectOutputStream OOut= new ObjectOutputStream(cs.getOutputStream());
			ObjectInputStream OIn= new ObjectInputStream(cs.getInputStream());

			serverPublicKey=(PublicKey) OIn.readObject();
			OOut.writeObject(keyPair.getPublic());
			String message=in.readUTF();
			System.out.println(message);
			out.write(encrypt(name));
			
			try (Scanner entry = new Scanner(System.in)) {
				while(true) {
					System.out.println("\nChoose one option (write only the number)!\n 1. Join chat room!\n 2. Create chat room!\n 3. Show chat rooms!\n 4.Close the app!\n");
					optionResponse(entry.nextLine(),out);
//					String option=entry.nextLine();
//					out.writeUTF(option);
					if(!running) break;
					System.out.println(decrypt(in.readAllBytes()));
				}
			}
			
		} catch (Exception e) {
			System.out.println("Client disconnected!");
		}
		
	}
	
	private void optionResponse(String option,DataOutputStream out) throws Exception {

		try(Scanner info=new Scanner(System.in)){
			String chatRoomName;
			switch (option) {
			case "1": // join chat room
				System.out.println("\nYou chose to join a chat room! Please write the name of the chat room with it's identifier!\n(The room name and the identifier need to be separated by a '#')\n");
				chatRoomName=info.nextLine();
				out.write(encrypt(option+","+chatRoomName));
				break;
				
			case "2": // create chat room
				
				System.out.println("\nYou chose to create a chat room! To start please provide the name of the chat room!\n");
				chatRoomName= info.nextLine();
				System.out.println("Do you wish to assign a password to this chat room?(Write only the numbers)\n 1.Yes!\n 2.No!\n");
				String passwordProtected=info.nextLine();

				if(passwordProtected.equalsIgnoreCase("1")) {
					boolean passwordsMatch=false;
					while (!passwordsMatch) {
						
						System.out.println("Please write the password you wish to assing to this chat room!\n");
						String password=info.nextLine();
						System.out.println("Please Repeat the password!\n");
						String passwordConfirmation=info.nextLine();
						
						if(password.equals(passwordConfirmation)) {
							System.out.println("The passwords match!\n");
							passwordsMatch=true;
							out.write(encrypt(option+","+chatRoomName+","+password));

						}else {
							System.out.println("The passwords do not match so we will create the chat room without a password!");
						
						}
					}
				}
				out.write(encrypt(option+","+chatRoomName+","+null));
				break;
				
			case "3": // show chat rooms
				out.write(encrypt(option));
				break;
				
			case "4": // exit 
				this.running=false;
				break;
				
			default:
				System.out.println("That option is no available! Please write only the number.");
			}
		}
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
