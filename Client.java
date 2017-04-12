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
import java.util.Arrays;

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
			byte[] shared_secret = keyAgree.generateSecret();
			//encodedKey = Base64.getEncoder().encodeToString(shared_secret.getEncoded());
			System.out.println("HERE IS MY SECRET KEY!!!" + Arrays.toString(shared_secret));
			
			//byte[] decoded_key = Base64.getDecoder().decode(encodedKey);
			//byte[] decoded_secret_key = Base64.getDecoder().decode(shared_secret);
			//IntBuffer intBuf = ByteBuffer.wrap(decoded_key).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
			//int[] converted_key = new int[intBuf.remaining()];

			String test = "hello";
			byte[] tester = test.getBytes();

			System.out.println("Message to be sent" + Arrays.toString(tester));

			int[] ret = new int[tester.length];
			for (int i = 0; i < tester.length; i++) {
				ret[i] = tester[i];
			}

			int[] ret2 = new int[shared_secret.length];
			for (int i = 0; i < shared_secret.length; i++) {
				ret2[i] = shared_secret[i];
			}
			
			//System.out.println("Here's my converted key: " + Arrays.toString(converted_key));

			MyEncrypt encrypt = new MyEncrypt(ret2, ret, 6);
			encrypt.start();
			try {
				encrypt.join();
				encrypt.getEncrpyted_message();
				System.out.println("Here's the messsage I ENCRYPTED: " + Arrays.toString(ret));
				os.writeObject(ret);
				//out.println(message);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		catch (Exception e){
			System.out.println("Error");
		}
	}
}
