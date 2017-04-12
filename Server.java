import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Random;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import java.lang.StringBuilder;

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

	public static byte[] convertInttoByteArr(int[] array) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(array);
        byte[] converted = byteBuffer.array();
		byte[] converted_nopad = new byte[array.length]; 
		for (int i = 0; i < array.length; i++) {
			converted_nopad[i] = converted[(i * 4) + 3];
		}
        return converted_nopad;
	}

	public static String ByteArrayToString(byte[] ba) {
		StringBuilder hex = new StringBuilder(ba.length * 2);
		for(int i = 0; i < ba.length; i++) {
			hex.append(String.format("%02X", ba[i]));
		}
		return hex.toString();
	}
	
	public static int shadow_lookup(int[] username, int[] password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			File file = new File("shadow_table.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			byte[] username_byte = convertInttoByteArr(username);
			byte[] password_byte = convertInttoByteArr(password);
			String str_username = new String(username_byte);
			String str_password = new String(password_byte);
			
			System.out.println("Password in str: " + str_password);
			System.out.println("Password in byte: " + Arrays.toString(password_byte));
			String line = null;
			while((line = br.readLine()) != null) {
				String[] shadow_table_entry = line.split("\\$");
				System.out.println("Comparing his: " + str_username);
				System.out.println("Comparing withours: " + shadow_table_entry[0]);
				if (str_username.equals(shadow_table_entry[0])) {
					System.out.println("Username passed");
					outputStream.reset();
					String combined = shadow_table_entry[1] + str_password;
					outputStream.write(combined.getBytes());
					byte[] salted_password = outputStream.toByteArray();
					
					messageDigest.update(salted_password);
					String encryptedpassword = ByteArrayToString(messageDigest.digest());
					System.out.println("Comparing his: " + encryptedpassword);
					System.out.println("Comparing withours: " + shadow_table_entry[2]);
					if (encryptedpassword.equals(shadow_table_entry[2])) {
						return 1;
					}
				}
			}
			return 0;
		}
		catch (Exception e) {
			System.out.println(e);
			return 0;
		}
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
			String ack;
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

			// Username
			int[] username = (int[]) is.readObject();
			
			// Password
			int[] password = (int[]) is.readObject();

			// Convert received message to int array
			int[] secret_key = convertBytetoIntArr(shared_secret);		

			// Create Decryption Class
			MyDecrypt decrypt = new MyDecrypt(secret_key);
			
			// Decrypt Message
			decrypt.decryption(username);
			System.out.println("Here's what I really got- username: " + Arrays.toString(username));
			
			Random rand = new Random();
			byte[] salt = new byte[16];
			rand.nextBytes(salt);
			
			decrypt.decryption(password);
			System.out.println("Here's what I really got- password: " + Arrays.toString(password));
			
			MyEncrypt encrypt = new MyEncrypt(secret_key);

			if (shadow_lookup(username, password) == 1) {
				ack = new String("ACK");
				byte[] ack_byte = ack.getBytes();
				int[] ack_int = convertBytetoIntArr(ack_byte);
				encrypt.encryption(ack_int);
				os.writeObject(ack_int);
				System.out.println("User is authorized!");
			}
			else {
				ack = new String("NOP");
				byte[] ack_byte = ack.getBytes();
				int[] ack_int = convertBytetoIntArr(ack_byte);
				encrypt.encryption(ack_int);
				os.writeObject(ack_int);
				System.out.println("User is not authorized! Exiting...");
				// Close socket
				csocket.close();
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
