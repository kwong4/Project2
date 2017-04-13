// Name: Kevin Wong
// ID: 1402456

/*
 * MyDecrypt class used to call decrpyt from c implementation
 * to decrypt data
 */

public class MyDecrypt{
	
	// Key variable
	private int[] secret_key;
	
	// Constructor for MyDecrypt
	public MyDecrypt(int[] secret_key) {
		this.secret_key = secret_key;
	}
	
	public int[] decryption(int[] message) {
		try {
			
			// Load decrypt library
			System.loadLibrary("decrypt");

			// Runs the C implementation of decrypt
			decrypt(this.secret_key, message);
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return message;
	}
	
	// Native method for decrypt
	public native void decrypt(int[] secret_key, int[] message);	
}
