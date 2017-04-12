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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Client connection...");
		try {
			// Connect to server
			Socket clientSocket = new Socket(InetAddress.getByName("localhost"), 16000);
			
			// Create input and output communication streams
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out =  new PrintWriter(clientSocket.getOutputStream(), true);
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
			//keyAgree.doPhase(secretkey, false);
			
			// Send Public Key
			os.writeObject(publickey);

			// Receive Client's Public Key
			PublicKey received_publickey = (PublicKey) is.readObject();
			
			// Generate Secret Key
			keyAgree.doPhase(received_publickey, true);
			byte[] shared_secret = keyAgree.generateSecret();

			System.out.println("Please enter a username: ");
			String username = br.readLine();
			
			System.out.println("Please enter a password: ");
			String password = br.readLine();
			
			byte[] username_byte_arr = username.getBytes();
			int[] username_int_arr = convertBytetoIntArr(username_byte_arr);
			
			byte[] password_byte_arr = password.getBytes();
			int[] password_int_arr = convertBytetoIntArr(password_byte_arr);

			// Convert received message to int array
			int[] secret_key = convertBytetoIntArr(shared_secret);

			System.out.println("Here's the messsage before ENCRYPTION: " + Arrays.toString(username_int_arr));
			
			MyEncrypt encrypt = new MyEncrypt(secret_key);
			encrypt.encryption(username_int_arr);
			os.writeObject(username_int_arr);
			System.out.println("Here's the messsage I ENCRYPTED: " + Arrays.toString(username_int_arr));
			
			System.out.println("Here's the messsage before ENCRYPTION: " + Arrays.toString(password_int_arr));
			
			encrypt.encryption(password_int_arr);
			os.writeObject(password_int_arr);
			System.out.println("Here's the messsage I ENCRYPTED: " + Arrays.toString(password_int_arr));
			
			MyDecrypt decrypt = new MyDecrypt(secret_key);
			int[] acknowledgement = (int[]) is.readObject();

			String acked = new String("NOP");
			byte[] comparison = acked.getBytes();

			decrypt.decryption(acknowledgement);
			//System.out.println("test");
			byte[] converted_ack = convertInttoByteArr(acknowledgement);

			System.out.println("Here's what we got : " + Arrays.toString(converted_ack));
			System.out.println("Here's what is it  : " + Arrays.toString(comparison));
			String string_ack = new String(converted_ack);
			System.out.println("What I received a" + string_ack + "a");
			if (string_ack.equals("ACK")) {
				System.out.println("Authorized");
			}
			else if (string_ack.equals("NOP")) {
				System.out.println("Not Authorized...");
			}
			else {
				System.out.println("Error receiving ack");
			}
		}
		catch (Exception e){
			System.out.println("Error");
		}
	}
}
