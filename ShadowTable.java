// Name: Kevin Wong
// ID: 1402456

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.lang.StringBuilder;
import java.util.Random;

/*
 * ShadowTable class used to add authorized users to the shadow_table.txt file
 * The password is salted and hashed
 */

public class ShadowTable {

	// Create secure random variable
	private static final Random RANDOM = new SecureRandom();
	
	// Get next secure random salt
	public static byte[] getNextSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
    	return salt;
	}

	// Conversion of Byte Array to Strings
	public static String ByteArrayToString(byte[] ba) {
		StringBuilder hex = new StringBuilder(ba.length * 2);
		for(int i = 0; i < ba.length; i++) {
			hex.append(String.format("%02X", ba[i]));
		}
		return hex.toString();
	}
	
	// To add accounts to shadow table
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			// Create reader to accept inputs
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			// Create output stream to write bytes
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			// Secure hashing function
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			
			// Place holders for writers to be used
			BufferedWriter bw = null;
			FileWriter fw = null;
			
			// Welcomes the user
			System.out.println("Welcome to master control of shadow table..");
			System.out.println("Please press Ctrl + c to exit account creation");
			while (true) {			
				
				// Clears the output stream
				outputStream.reset();
				
				// Gets the username to add
				System.out.println("Please enter a username to add: ");
				String username = br.readLine();
				
				// Gets the password to add
				System.out.println("Please enter a password to add: ");
				String password = br.readLine();
				
				// Obtains next salt, convert and combine with password
				byte[] salt = getNextSalt();
				String str_salt = ByteArrayToString(salt);
				String combined = str_salt + password;

				// Converts string into bytes using outputStream
				outputStream.write(combined.getBytes());
				byte[] salted_password = outputStream.toByteArray();
				
				// Use the hashing function on salted_password and converts into a string
				messageDigest.update(salted_password);
				String encryptedpassword = ByteArrayToString(messageDigest.digest());
				
				// Creates access point for file
				File file = new File("shadow_table.txt");
				
				// Checks if file exits, if not create it
				if (!file.exists()) {
					file.createNewFile();
				}
				
				// Create the writer classes
				fw = new FileWriter(file.getAbsoluteFile() ,true);
				bw = new BufferedWriter(fw);
				
				// Combines the entry of shadow table, separated by $ for harder to read
				String shadow_table_entry = username + "$" + str_salt + "$" + encryptedpassword;
				
				// Writes the entry in and saves the file
				bw.write(shadow_table_entry);
				bw.newLine();
				bw.close();
				fw.close();
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

}
