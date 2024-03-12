package client;

import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.Cipher;

/*
 * At the moment I am still doubting the approach of creating the writing 
 * thread like this but for the moment will do.
 */
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
	
	
	private  byte[] encrypt(String message) throws Exception {
		Cipher cipher= Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);
		
		return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
	}
}
