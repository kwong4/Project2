// Name: Kevin Wong
// ID: 1402456

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.KeyAgreement;
import java.lang.StringBuilder;

/*
 * This is the Server that is able to support multiple clients at once.
 * It is used to create an encrypted connection with a Client pair. 
 * This is done using Diffie Hellman algorithm, and once the Client
 * provides a valid Username and Password corresponding to ShadowTable,
 * will setup a secure file transfer
 */

public class Server implements Runnable {
	
	// Server Socket
	Socket csocket;
	
	// Constructor
	Server(Socket csocket) {
		this.csocket = csocket;
	}
	
	// Convert Byte Array into Int Array as return type
	public static int[] convertBytetoIntArr(byte[] array) {
		int[] converted = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			converted[i] = array[i];
		}
		return converted;
	}

	// Convert Int Array into Byte Array, eliminating the padding
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

	// Convert the ByteArray into a Hex String
	public static String ByteArrayToString(byte[] ba) {
		
		// Creates String Builder of double length size
		StringBuilder hex = new StringBuilder(ba.length * 2);
		
		// Cycle through and add hex format into a StringBuilder
		for(int i = 0; i < ba.length; i++) {
			hex.append(String.format("%02X", ba[i]));
		}
		
		// Return String converted Hex
		return hex.toString();
	}
	
	// Shadow table lookup to verify if correct credientials were given
	// If valid return 1, if not return 0
	public static int shadow_lookup(int[] username, int[] password) {
		try {
			
			// Creates a secure hashing function
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			
			// Creates access point to read from file
			File file = new File("shadow_table.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			// Converts inputted username and passwords to byte[] and strings
			byte[] username_byte = convertInttoByteArr(username);
			byte[] password_byte = convertInttoByteArr(password);
			String str_username = new String(username_byte);
			String str_password = new String(password_byte);
			
			// Initial point to read lines through
			String line = null;
			
			// Cycle through the shadow table and read each entry (this case a line)
			while((line = br.readLine()) != null) {
				
				// Split the shadow table by $ as the seperator
				String[] shadow_table_entry = line.split("\\$");
				if (str_username.equals(shadow_table_entry[0])) {
					
					// Reset Output Stream
					outputStream.reset();
					
					// Combines shadow table with salt and converts it
					String combined = shadow_table_entry[1] + str_password;
					outputStream.write(combined.getBytes());
					byte[] salted_password = outputStream.toByteArray();
					
					// Hash the combined entry and convert into String
					messageDigest.update(salted_password);
					String encryptedpassword = ByteArrayToString(messageDigest.digest());
					
					// Checks if the hashed password matches the entry
					if (encryptedpassword.equals(shadow_table_entry[2])) {
						return 1;
					}
				}
			}
			br.close();
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
		
		// Try creating first socket for server
		try {
			ServerSocket serverSocket = new ServerSocket(16000);
			
			// Keep accepting multiple connections and create threads for them
			while (true) {
				Socket sock = serverSocket.accept();
				System.out.println("New Client Accepted!!");
				new Thread(new Server(sock)).start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Server handling procedure for clients
	public void run() {
		try {
			System.out.println("Currently Connected to Client marked as #" + Thread.currentThread().getId());
			// Create input and output communication streams
			ObjectOutputStream os = new ObjectOutputStream(csocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(csocket.getInputStream());
			
			// Acknowledgement String
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
			os.flush();
			
			// Receive Client's Public Key
			PublicKey received_publickey = (PublicKey) is.readObject();
			
			// Generate Secret Key
			keyAgree.doPhase(received_publickey, true);
			byte[] shared_secret = keyAgree.generateSecret();

			System.out.println("Secure connection established");
			
			// Username
			int[] username = (int[]) is.readObject();
			
			// Password
			int[] password = (int[]) is.readObject();

			// Convert received message to int array
			int[] secret_key = convertBytetoIntArr(shared_secret);		

			// Create Decryption Class
			MyDecrypt decrypt = new MyDecrypt(secret_key);
			
			// Decrypt Username
			decrypt.decryption(username);
			
			// Decrypt Password
			decrypt.decryption(password);
			
			// Create Encryption Class
			MyEncrypt encrypt = new MyEncrypt(secret_key);
			
			System.out.println("Received Username and Passwords. Checking if valid.");

			// Check shadow table if valid entry
			if (shadow_lookup(username, password) == 1) {
				
				// Set the acknowledgement message
				ack = new String("ACK");
				
				// Convert acknowledgement
				byte[] ack_byte = ack.getBytes();
				int[] ack_int = convertBytetoIntArr(ack_byte);
				
				// Encrypt and send acknowledgement
				encrypt.encryption(ack_int);
				os.writeObject(ack_int);
				os.flush();
				
				System.out.println("User is authorized! Sent Acknowledgement");
				
				// Forever loop until terminating signal received from client
				while (true) {
					
					// Filename
					System.out.println("Waiting to get a filename...");
					
					// Read the filename, decrypt and convert the filename
					int[] filename = (int[]) is.readObject();
					decrypt.decryption(filename);
					byte[] filename_byte = convertInttoByteArr(filename);
					String filename_str = new String(filename_byte);
					
					// Checks if the filename is actually the terminating signal
					if (!filename_str.equals("FIN")) {
						
						// Creates access point of filename
						File file = new File(filename_str);

						// Checks if the file exists
						if (file.exists()) {
							
							// File is present. Setup acknowledgement, encrypt and send
							System.out.println("File is present. Sending..");
							ack = new String("ACK");
							ack_byte = ack.getBytes();
							ack_int = convertBytetoIntArr(ack_byte);
							encrypt.encryption(ack_int);
							os.writeObject(ack_int);

							// Holder for file of bytes
							byte[] file_byte = new byte[(int) file.length()];

							// Creates Input Stream to read file into byte format into holder
							FileInputStream fis = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(fis);
							bis.read(file_byte, 0, file_byte.length);							

							// Convert bytes into int[], encrypt, and send
							int[] file_int = convertBytetoIntArr(file_byte);
							encrypt.encryption(file_int);
							os.writeObject(file_int);
						}
						else {
							// File is not present. Send file not found signal
							System.out.println("File is not present.");
							ack = new String("FNF");
							ack_byte = ack.getBytes();
							ack_int = convertBytetoIntArr(ack_byte);
							encrypt.encryption(ack_int);
							os.writeObject(ack_int);
							os.flush();
						}
					}
					else {
						break;
					}
				}
				// Close if terminating signal is found
				System.out.println("Ending signal detected... Closing Connection for " + Thread.currentThread().getId());
				csocket.close();
			}
			else {
				// Sends encrypted not acknowledged signal to client if not authorized
				ack = new String("NOP");
				byte[] ack_byte = ack.getBytes();
				int[] ack_int = convertBytetoIntArr(ack_byte);
				encrypt.encryption(ack_int);
				os.writeObject(ack_int);
				os.flush();
				System.out.println("User is not authorized! Exiting...");
				
				// Close socket
				csocket.close();
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
