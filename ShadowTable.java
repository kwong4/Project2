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

public class ShadowTable {

	private static final Random RANDOM = new SecureRandom();
	
	public static byte[] getNextSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
    	return salt;
	}

	public static String ByteArrayToString(byte[] ba) {
		StringBuilder hex = new StringBuilder(ba.length * 2);
		for(int i = 0; i < ba.length; i++) {
			hex.append(String.format("%02X", ba[i]));
		}
		return hex.toString();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			
			BufferedWriter bw = null;
			FileWriter fw = null;
			
			System.out.println("Welcome to master control of shadow table..");
			while (true) {			
				outputStream.reset();
				System.out.println("Please enter a username to add: ");
				String username = br.readLine();
				
				System.out.println("Please enter a password to add: ");
				String password = br.readLine();
				
				byte[] salt = getNextSalt();
				String str_salt = ByteArrayToString(salt);
				String combined = str_salt + password;

				outputStream.write(combined.getBytes());
				byte[] salted_password = outputStream.toByteArray();
				
				messageDigest.update(salted_password);
				String encryptedpassword = ByteArrayToString(messageDigest.digest());
				
				File file = new File("shadow_table.txt");
				if (!file.exists()) {
					file.createNewFile();
				}
				
				fw = new FileWriter(file.getAbsoluteFile() ,true);
				bw = new BufferedWriter(fw);
				
				String shadow_table_entry = username + "$" + str_salt + "$" + encryptedpassword;
				
				bw.close();
				fw.close();
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

}