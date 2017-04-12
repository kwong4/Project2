public class MyEncrypt extends Thread{
	
	// Key and message variables
	private int[] secret_key;
	private int[] message;

	// Get Encrypted Message
	public int[] getEncrpyted_message() {
		return message;
	}
	
	// Constructor for MyEncrypt
	public MyEncrypt(int[] secret_key, int[] message) {
		this.secret_key = secret_key;
		this.message = message;
	}

	public void encryption() {
		try {
			
			// Load encryption library
			System.loadLibrary("encrypt");

			// Runs the C implementation of encryption
			encrypt(this.secret_key, this.message);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
		
	// Native method for insertionsort
	public native void encrypt(int[] secret_key, int[] message, int size);	
}
