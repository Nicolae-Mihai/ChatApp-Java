package client;

import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.Cipher;

public class WritingThread extends Thread{

	private PublicKey serverPublicKey;
	private ObjectOutputStream out;
	private Scanner message;
	
	public WritingThread(Scanner message , ObjectOutputStream out, PublicKey serverPublicKey ) {
		this.out=out;
		this.serverPublicKey=serverPublicKey;
		this.message=message;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				String message = this.message.nextLine();
				out.writeObject(encrypt(message));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
    // Encrypts the given message using RSA encryption.
    private byte[] encrypt(String message) throws Exception {
        // Create a Cipher instance for RSA encryption
        Cipher cipher = Cipher.getInstance("RSA");
        // Initialize the Cipher with the server's public key for encryption
        cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);
        // Encrypt the message using UTF-8 encoding and return the encrypted byte array
        return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

}
