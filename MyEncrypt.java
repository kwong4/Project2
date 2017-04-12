// Name: Kevin Wong
// ID: 1402456

/*
 * MyEncrypt class used to call encrpyt from c implementation
 * to encrypt data
 */

public class MyEncrypt{
	
	// Secret key variable
	private int[] secret_key;
	
	// Constructor for MyEncrypt
	public MyEncrypt(int[] secret_key) {
		this.secret_key = secret_key;
	}

	public int[] encryption(int[] message) {
		try {
			
			// Load encrypt library
			System.loadLibrary("encrypt");

			// Runs the C implementation of encrypt
			encrypt(this.secret_key, message);
			
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return message;
	}
		
	// Native method for encrypt
	public native void encrypt(int[] secret_key, int[] message);	
}
