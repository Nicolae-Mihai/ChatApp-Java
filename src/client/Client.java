/*
 * This class is the constructor and the implements all the logic necessary 
 * for the client to work properly
 */
package client;

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

public class Client extends Connection implements Runnable{
	private boolean running = true;
	private String name;
	private PublicKey serverPublicKey;
	private KeyPair keyPair;
	private boolean isConnectedToRoom;
	private ObjectInputStream in;
	private ObjectOutputStream out;
//	private String chatRoomName;

	public Client(String name) throws IOException, NoSuchAlgorithmException {
		super("client");
		this.in = new ObjectInputStream(cs.getInputStream());
		this.name = name;
		KeyPairGenerator kpa = KeyPairGenerator.getInstance("RSA");
		this.out = new ObjectOutputStream(cs.getOutputStream());
		this.keyPair = kpa.generateKeyPair();
		this.isConnectedToRoom = false;

	}
	@Override
	public void run() {
		try {
			out.writeObject(keyPair.getPublic());
			serverPublicKey = (PublicKey) in.readObject();
			out.writeObject(encrypt(name));

			String message = (String) decrypt((byte[]) in.readObject());
			System.out.println(message);
			String option;

			try (Scanner entry = new Scanner(System.in)) {
				while (true) {
					if (isConnectedToRoom) {
						roomMenu(in, out, entry);
					} else {
						System.out.println("\nChoose one option (write only the number)!\n 1. Join chat room!\n 2. Create chat room!\n 3. Show chat rooms!\n 4.Close the app!\n");
						option = entry.nextLine();
						optionResponse(option, out, entry);

						if (!running)
							break;

						String response = decrypt((byte[]) in.readObject());
						System.out.println(response);

						if(response.contains("connected to a room"))
							isConnectedToRoom= true;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Client disconnected!");
		}

	}

	/*
	 * pre: post:This method is the menu which is presented to the client when not
	 * connected to a room.
	 */
	private void optionResponse(String option, ObjectOutputStream out, Scanner info) throws Exception {
		String chatRoomName;
		switch (option) {

		case "1": // join chat room
			System.out.println("\nYou chose to join a chat room! Please write the name of the chat room with it's identifier!\n(The room name and the identifier need to be separated by a '#')\n");

			chatRoomName = info.nextLine();
//				this.chatRoomName=chatRoomName;
			out.writeObject(encrypt(option + "," + chatRoomName));
			String serverResponse = (String) decrypt((byte[]) in.readObject()); // TODO I want the client to wait here a
																				// response
			if (serverResponse.equals("Please enter the password")) {
				passwordHandler(out, in, info);
			}
			break;

		case "2": // create chat room

			System.out
					.println("\nYou chose to create a chat room! To start please provide the name of the chat room!\n");
			chatRoomName = info.nextLine();
			System.out.println(
					"Do you wish to assign a password to this chat room?(Write only the numbers)\n 1.Yes!\n 2.No!\n");
			String passwordProtected = info.nextLine();
			String password = "null";
			if (passwordProtected.equalsIgnoreCase("1")) {
				boolean passwordsMatch = false;
				while (!passwordsMatch) {

					System.out.println("Please write the password you wish to assing to this chat room!\n");
					password = info.nextLine();
					System.out.println("Please Repeat the password!\n");
					String passwordConfirmation = info.nextLine();

					if (password.equals(passwordConfirmation)) {
						System.out.println("The passwords match!\n");
						passwordsMatch = true;

					} else {
						System.out.println(
								"The passwords do not match so we will create the chat room without a password!");

					}
				}
			}
			out.writeObject(encrypt(option + "," + chatRoomName + "," + password));
			break;

		case "3": // show chat rooms
			out.writeObject(encrypt(option));
			break;

		case "4": // exit
			this.running = false;
			break;

		default:
			System.out.println("That option is no available! Please write only the number.");
			out.writeObject(encrypt("foo"));
		}
	}
	/*
	 * pre: post: This method "traps" the client while in a room so the other
	 * options don't appear once the client writes "/disconnect" the boolean
	 * "isConnectedToRoom" turns to false and disconnects the client.
	 */
	private void roomMenu(ObjectInputStream in ,ObjectOutputStream out, Scanner entry) throws Exception {
//		out.writeObject(encrypt(entry.nextLine()));
//		String response= (String) decrypt((byte[]) in.readObject());
//		
//		if(response.equalsIgnoreCase("disconnected")) {
//			this.isConnectedToRoom=false;
		ReadingThread rt = new ReadingThread(in, keyPair.getPrivate(), isConnectedToRoom);
		WritingThread wt = new WritingThread(entry, out, serverPublicKey);
		rt.run();
		wt.run();
//			
		}
//		
//		System.out.println(response);
//	}

	/*
	 * pre: post:The encryption method used to encrypt the messages.
	 * 
	 */
	private byte[] encrypt(String message) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);

		return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
	}

	/*
	 * pre post: The method used to decrypt the messages recieved from the server.
	 */
	private String decrypt(byte[] message) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte[] plainText = cipher.doFinal(message);
		return new String(plainText);
	}

	/**
	 * pre:-- post: Method that handles the first response from the server regarding
	 * the existence of a password in the room.
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void passwordHandler(ObjectOutputStream out, ObjectInputStream in, Scanner scan)
			throws ClassNotFoundException, IOException, Exception {
		String serverMessage;
//		serverMessage = decrypt((byte[]) in.readObject());//Decrypt the message from the server and print it || It stops here
//		System.out.println(serverMessage);

		System.out.println("Please enter a password"); // this could be better if it came from the server???IDK
		String messageClient = scan.nextLine();// Store and send pwd
		out.writeObject(encrypt(messageClient));

		serverMessage = decrypt((byte[]) in.readObject());
		/*
		 * If the server is responding any kind of message that indicates that our
		 * password is not correct we will be stuck in a loop until some condition is
		 * met
		 */
		if (!serverMessage.contains("Connected to room")) {
			System.out.println(serverMessage);
			boolean stuck = true;
//			print the server response
			while (stuck) {
				messageClient = scan.nextLine();// Retry sending pwd
				out.writeObject(encrypt(messageClient));
				serverMessage = decrypt((byte[]) in.readObject());// Decrypt the message from the server and print it
				System.out.println(serverMessage);

				if (serverMessage.equals("Failed to connect. Incorrect password"))
					break;
				if (serverMessage.contains("Connected to room")) {
					this.isConnectedToRoom = true;
					break;
				}
			}
		} else {
			this.isConnectedToRoom = true;
		}
	}
}
