package server;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;

public class ClientStorage {
	
	private String clientName;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private PublicKey publicKey;
//	
	public ClientStorage(String clientName, ObjectOutputStream out, ObjectInputStream in, PublicKey publicKey) {
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

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
}
