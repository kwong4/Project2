public class MyDecrypt{
	
	// Key and message variables
	private int[] secret_key;
	
	// Constructor for MyEncrypt
	public MyDecrypt(int[] secret_key) {
		this.secret_key = secret_key;
	}
	
	public int[] decryption(int[] message) {
		try {
			
			// Load decryption library
			System.loadLibrary("decrypt");

			// Runs the C implementation of decryption
			decrypt(this.secret_key, message);
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return message;
	}
	
	// Native method for decryption
	public native void decrypt(int[] secret_key, int[] message);	
}
