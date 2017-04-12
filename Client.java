// Name: Kevin Wong
// ID: 1402456

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.KeyAgreement;

/*
 * This is the Client that connects to a Server pair
 * Creates secure connection and receives username
 * and password as inputs for authorization of server.
 * Then receives files from server with secure transfer
 */
public class Client {

	// Conversion of Byte array into Int array
	public static int[] convertBytetoIntArr(byte[] array) {
		
		// Cycles through Byte array and converts into a int array
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Client connection...");
		try {
			// Connect to server
			Socket clientSocket = new Socket(InetAddress.getByName("localhost"), 16000);
			
			// Create input and output communication streams
			ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
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

			// Receives username from user
			System.out.println("Please enter a username: ");
			String username = br.readLine();
			
			// Receives password from user
			System.out.println("Please enter a password: ");
			String password = br.readLine();
			
			// Converts username and password into int[]
			byte[] username_byte_arr = username.getBytes();
			int[] username_int_arr = convertBytetoIntArr(username_byte_arr);
			
			byte[] password_byte_arr = password.getBytes();
			int[] password_int_arr = convertBytetoIntArr(password_byte_arr);

			// Converts secretkey into a int array
			int[] secret_key = convertBytetoIntArr(shared_secret);
			
			// Creates encryption class
			MyEncrypt encrypt = new MyEncrypt(secret_key);
			
			// Encrypts username and sends to server
			encrypt.encryption(username_int_arr);
			os.writeObject(username_int_arr);
			
			// Encrypts password and sends to server
			encrypt.encryption(password_int_arr);
			os.writeObject(password_int_arr);
			
			// Creates decryption class
			MyDecrypt decrypt = new MyDecrypt(secret_key);
			
			// Reads acknowledgement from server
			int[] acknowledgement = (int[]) is.readObject();

			// Decrypts and converts the acknowledgement
			decrypt.decryption(acknowledgement);
			byte[] converted_ack = convertInttoByteArr(acknowledgement);
			String string_ack = new String(converted_ack);
			
			// Checks if the acknowledgement is good
			if (string_ack.equals("ACK")) {
				System.out.println("Authorized");
				
				// Authorized. Take filename inputs from user to receive from server
				while (true) {
					
					// Obtain filename from user and converts into int[]
					System.out.println("Please enter a filename: (Type FIN if no more files to be requests)");
					String filename = br.readLine();
					byte[] filename_byte_arr = filename.getBytes();
					int[] filename_int_arr = convertBytetoIntArr(filename_byte_arr);
					
					// Encrypts and sends the encrypted filename
					encrypt.encryption(filename_int_arr);
					os.writeObject(filename_int_arr);
					
					// Receive acknowledgement from server
					acknowledgement = (int[]) is.readObject();
					
					// Decrypt and convert acknowledgement
					decrypt.decryption(acknowledgement);
					converted_ack = convertInttoByteArr(acknowledgement);
					string_ack = new String(converted_ack);
			
					System.out.println("Requesting... " + filename);
					
					// Checks if acknowledgement is received. If file is present
					if (string_ack.equals("ACK")) {
						System.out.println("File was found.. Receiving file.. ");
						
						// Read and decrypt file from the server
						int[] file_int = (int[]) is.readObject();
						decrypt.decryption(file_int);
						byte[] file_byte = convertInttoByteArr(file_int);

						// Outputs the file into directory of the client
						FileOutputStream stream = new FileOutputStream(filename);
						try {
							stream.write(file_byte);
						}
						catch (Exception e) {
							System.out.println(e);
						}
						finally {
							stream.close();
						}
					}
					else {
						System.out.println("File was not found");
					}
				}
				
			}
			else if (string_ack.equals("NOP")) {
				System.out.println("Not Authorized...");
				clientSocket.close();
			}
			else {
				System.out.println("Error receiving ack");
				clientSocket.close();
			}
		}
		catch (Exception e){
			System.out.println("Connection Closed.");
		}
	}
}
