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
    // Decrypts the given message using RSA decryption.
    private String decrypt(byte[] message) throws Exception{
        // Create a Cipher instance for RSA decryption
        Cipher cipher = Cipher.getInstance("RSA");
        // Initialize the Cipher with the client's private key for decryption
        cipher.init(Cipher.DECRYPT_MODE, clientPrivateKey);
        // Decrypt the message and convert the resulting byte array to a string
        byte[] plainText = cipher.doFinal(message);
        return new String(plainText);
    }

}
