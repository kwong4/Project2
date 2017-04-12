public class MyDecrypt{
	
	// Key and message variables
	private int[] secret_key;
	private int[] message;
	
	// Get Decrypted Message
	public int[] getDecrpyted_message() {
		return this.message;
	}
	
	// Constructor for MyEncrypt
	public MyDecrypt(int[] secret_key, int[] message) {
		this.secret_key = secret_key;
		this.message = message;
	}
	
	public void decryption() {
		try {
			
			// Load decryption library
			System.loadLibrary("decrypt");

			// Runs the C implementation of decryption
			decrypt(this.secret_key, this.message);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

		
	// Native method for decryption
	public native void decrypt(int[] secret_key, int[] message, int size);	
}
