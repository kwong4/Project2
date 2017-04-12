import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.KeyAgreement;

public class Server implements Runnable {
	Socket csocket;
	Server(Socket csocket) {
		this.csocket = csocket;
	}
	
	public static int[] convertBytetoIntArr(byte[] array) {
		int[] converted = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			converted[i] = array[i];
		}
		return converted;
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
			// Create input and output communication streams
			PrintWriter out =  new PrintWriter(csocket.getOutputStream(), true);
			BufferedReader input = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			ObjectOutputStream os = new ObjectOutputStream(csocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(csocket.getInputStream());
			
			// Generate Private Key and Public Key Pair with Diffie Hellman
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
			keyGen.initialize(512);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey secretkey = pair.getPrivate();
			PublicKey publickey = pair.getPublic();
			
			//KeyAgreement Setup
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(secretkey);
			
			// Send Public Key
			os.writeObject(publickey);
			
			// Receive Client's Public Key
			PublicKey received_publickey = (PublicKey) is.readObject();
			
			// Generate Secret Key
			keyAgree.doPhase(received_publickey, true);
			byte[] shared_secret = keyAgree.generateSecret();

			// Message
			int[] received = (int[]) is.readObject();

			// Convert received message to int array
			int[] secret_key = convertBytetoIntArr(shared_secret);

			System.out.println("Here's the message I got ENCRYPTED: " + Arrays.toString(received));			

			// Create Decryption Class
			MyDecrypt decrypt = new MyDecrypt(secret_key);
			
			// Decrypt Message
			decrypt.decryption(received);
			System.out.print("Here's what I really got: " + Arrays.toString(received));
			
			// Close socket
			csocket.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
