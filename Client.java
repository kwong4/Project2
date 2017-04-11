import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
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

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Client connection...");
		try {
			Socket clientSocket = new Socket(InetAddress.getByName("localhost"), 16000);
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out =  new PrintWriter(clientSocket.getOutputStream(), true);
			ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
			// Generate Private Key
//			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//			keyGen.init(256);
//			SecretKey secretkey = keyGen.generateKey();
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
			os.writeObject(publickey);
			//out.println(encodedKey);

			PublicKey received_publickey = (PublicKey) is.readObject();
			
			encodedKey = Base64.getEncoder().encodeToString(received_publickey.getEncoded());
			System.out.println("Here's what I got" + encodedKey);
			
			keyAgree.doPhase(received_publickey, true);
			
			// Generate Secret Key
			SecretKey shared_secret = keyAgree.generateSecret("AES");
			encodedKey = Base64.getEncoder().encodeToString(shared_secret.getEncoded());
			System.out.println("HERE IS MY SECRET KEY!!!" + encodedKey);
			
			byte[] decoded_key = Base64.getDecoder().decode(encodedKey);
			IntBuffer intBuf = ByteBuffer.wrap(decoded_key).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
			int[] converted_key = new int[intBuf.remaining()];
			
			MyEncrypt encrypt = new MyEncrypt(converted_key, "Hello", 6);
			encrypt.start();
			try {
				encrypt.join();
				String message = encrypt.getEncrpyted_message();
				System.out.println("Here's the messsage I got ENCRYPTED: " + message);
				out.println(message);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		catch (Exception e){
			System.out.println("Error");
		}
	}
}
