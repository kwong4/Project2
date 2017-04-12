import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Server implements Runnable {
	Socket csocket;
	Server(Socket csocket) {
		this.csocket = csocket;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Starting Server connection...");
		
		try {
			ServerSocket serverSocket = new ServerSocket(16000);
			while (true) {
				Socket sock = serverSocket.accept();
				System.out.println("New Client Accepted!!");
				new Thread(new Server(sock)).start();
			}
		} catch (Exception e) {
			System.out.println("Error!");
		}
	}

	public void run() {
		try {
			PrintWriter out =  new PrintWriter(csocket.getOutputStream(), true);
			BufferedReader input = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			ObjectOutputStream os = new ObjectOutputStream(csocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(csocket.getInputStream());
			
			// Generate Private Key
			//KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			//keyGen.init(256);
			//SecretKey secretkey = keyGen.generateKey();
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
			keyGen.initialize(512);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey secretkey = pair.getPrivate();
			PublicKey publickey = pair.getPublic();
			
			String encodedKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
			System.out.println("Here's my privatekey: " + encodedKey);
			
			//Setup KeyAgreement
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(secretkey);
			//keyAgree.doPhase(secretkey, false);
			
			// Encode key
			encodedKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
			
			System.out.println("Here's what I'm about to send" + encodedKey);
			// Send public key
			//out.println(encodedKey);
			os.writeObject(publickey);
			
			PublicKey received_publickey = (PublicKey) is.readObject();
			
			encodedKey = Base64.getEncoder().encodeToString(received_publickey.getEncoded());
			System.out.println("Here's what I got" + encodedKey);
			keyAgree.doPhase(received_publickey, true);
			
			// Generate Secret Key
			byte[] shared_secret = keyAgree.generateSecret();
			//encodedKey = Base64.getEncoder().encodeToString(shared_secret.getEncoded());
			//System.out.println("HERE IS MY SECRET KEY!!!" + encodedKey);
			
			byte[] decoded_key = Base64.getDecoder().decode(encodedKey);

			//System.out.println("Are they equal? : " + Arrays.equals(decoded_key, decoded_key2));
			//IntBuffer intBuf = ByteBuffer.wrap(decoded_key).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
			//int[] converted_key = new int[intBuf.remaining()];
			
			//String received = input.readLine();

			int[] received = (int[]) is.readObject();

			int[] ret2 = new int[shared_secret.length];
			for (int i = 0; i < shared_secret.length; i++) {
				ret2[i] = shared_secret[i];
			}

			System.out.println("Here's the message I got ENCRYPTED: " + Arrays.toString(received));			

			//System.out.println("Here's my converted key: " + Arrays.toString(decoded_key));

			MyDecrypt decrypt = new MyDecrypt(ret2, received, received.length);
			decrypt.start();
			try {
				decrypt.join();
				decrypt.getDecrpyted_message();
				//System.out.println("Here's the messsage I got DECRYPTED: " + Arrays.toString(message));
				//String str = new String(message, "UTF-8");
				System.out.print("Here's what I really got: " + Arrays.toString(received));
			} catch (Exception e) {
				System.out.println(e);
			}
			
			csocket.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		finally {
		}
	}
}
