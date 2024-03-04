/*
 * This class is the constructor and the implements all the logic necessary 
 * for the client to work properly
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Client extends Connection{
	private boolean running=true;
	public Client() throws IOException{
		super("client");
	}
	public void startClient() {
		try {
			DataInputStream in= new DataInputStream(cs.getInputStream());
			DataOutputStream out=new DataOutputStream(cs.getOutputStream());
			String message=in.readUTF();
			System.out.println(message);
			
			try (Scanner entry = new Scanner(System.in)) {
				while(true) {
					System.out.println("\nChoose one option (write only the number)!\n 1. Join chat room!\n 2. Create chat room!\n 3. Show chat rooms!\n 4.Close the app!\n");
					optionResponse(entry.nextLine(),out);
//					String option=entry.nextLine();
//					out.writeUTF(option);
					if(!running) break;
					System.out.println(in.readUTF());
				}
			}
			
		} catch (Exception e) {
			System.out.println("Client disconnected!");
		}
		
	}
	
	private void optionResponse(String option,DataOutputStream out) throws IOException {

		try(Scanner info=new Scanner(System.in)){
			String chatRoomName;
			switch (option) {
			case "1": // join chat room
				System.out.println("\nYou chose to join a chat room! Please write the name of the chat room with it's identifier!\n");
				chatRoomName=info.nextLine();
				out.writeUTF(option+","+chatRoomName);
				break;
				
			case "2": // create chat room
				System.out.println("\nYou chose to create a chat room! To start please provide the name of the chat room!\n");
				chatRoomName= info.nextLine();
				System.out.println("Do you wish to assign a password to this chat room?(Write only the numbers)\n 1.Yes!\n 2.No!\n");
				String passwordProtected=info.nextLine();
				if(passwordProtected.equalsIgnoreCase("1")) {
//					this should be in a loop I still need to think about it
					System.out.println("Please write the password you wish to assing to this chat room!\n");
					String password=info.nextLine();
					System.out.println("Please Repeat the password!\n");
					String passwordConfirmation=info.nextLine();
					if(password.equals(passwordConfirmation)) {
//						TODO:check if the password needs to be hashed before it can be sent to the server 
//						or if the server receives the password in plain text and then the server hashes the password
						System.out.println("The passwords match!\n");
//						TODO:Create the method for the random alphanumeric generation
						out.writeUTF(option+","+chatRoomName+"random alphanumeric"+","+password);
					}else {
						System.out.println("The passwords do not match so we will create the chat room without a password!");
					}
				}
				out.writeUTF(option+","+null);
				break;
				
			case "3": // show chat rooms
				out.writeUTF(option+","+null);
				break;
				
			case "4": // exit 
				this.running=false;
				break;
				
			default:
				System.out.println("That option is no available! Please write only the number.");
			}
		}
	}
}
