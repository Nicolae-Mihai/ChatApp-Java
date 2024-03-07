package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.PublicKey;

public class ClientStorage {
	
	private String clientName;
	private DataOutputStream out;
	private DataInputStream in;
	private PublicKey publicKey;
	
	public ClientStorage(String clientName,DataOutputStream out,DataInputStream in, PublicKey publicKey) {
		this.clientName=clientName;
		this.out=out;
		this.in=in;
		this.publicKey=publicKey;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public DataOutputStream getOut() {
		return out;
	}

	public void setOut(DataOutputStream out) {
		this.out = out;
	}

	public DataInputStream getIn() {
		return in;
	}

	public void setIn(DataInputStream in) {
		this.in = in;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
}
