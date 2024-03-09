/*
 * The Room class can be private or public and depending on that it requires a
 *  password to enter or not
 *  It must have a List where it stores the client's in,out and public key
 */
package server;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private List<ClientStorage> connectedClients;
	private List<String> chat;
	private String password;
	private String id;
	private String name;
	private boolean isProtected;
	
	public Room (String name, String password, String id) {
		
		this.connectedClients=new ArrayList<ClientStorage>();
		this.name = name;
		this.password = password;
		this.id = id;
		this.isProtected = true;
	}
	
	public Room (String name, String id) {
		this.connectedClients=new ArrayList<ClientStorage>();
		this.name = name;
		this.id = id;
		this.isProtected = false;
	}

	public List<String> getChat() {
		return chat;
	}
	
	public void addMessage(String message) {//Method used to add a new message to the chat
		this.chat.add(message);
	}

	public String getPassword() {
		return password;
	}

	
	public String getId() {
		return id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getIsProtected() {
		return this.isProtected;
	}

	public List<ClientStorage> getConnectedClients() {
		return connectedClients;
	}

	public void setConnectedClients(List<ClientStorage> connectedClients) {
		this.connectedClients = connectedClients;
	}

	
}
