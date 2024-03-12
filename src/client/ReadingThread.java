package client;

import java.io.ObjectInputStream;
import java.security.PrivateKey;

import javax.crypto.Cipher;

/*
 * The reading thread ,in my opinion, should be separated from the client 
 * class logic and do it's own thing.
 */
public class ReadingThread extends Thread{
	
	private ObjectInputStream in;
	private String message;
	private PrivateKey clientPrivateKey;
	private boolean isConnectedToRoom;
	public ReadingThread(ObjectInputStream in,PrivateKey clientPrivateKey, boolean isConnectedToRoom ) {
		
		this.in=in;
		this.clientPrivateKey=clientPrivateKey;
		this.isConnectedToRoom=isConnectedToRoom;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				message =  decrypt((byte [])in.readObject());
				System.out.println(message);
				if(message.equalsIgnoreCase("disconnected")){
					this.isConnectedToRoom=false;
					break;
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private String decrypt(byte[] message) throws Exception{
		Cipher cipher=Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, clientPrivateKey);
		byte[] plainText = cipher.doFinal(message);
		return new String(plainText);
	}
}
