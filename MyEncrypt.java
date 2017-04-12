public class MyEncrypt extends Thread{
	
	// Key and message variables
	private int[] secret_key;
	
	// Constructor for MyEncrypt
	public MyEncrypt(int[] secret_key) {
		this.secret_key = secret_key;
	}

	public int[] encryption(int[] message) {
		try {
			
			// Load encryption library
			System.loadLibrary("encrypt");

			// Runs the C implementation of encryption
			encrypt(this.secret_key, message);
			
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return message;
	}
		
	// Native method for insertionsort
	public native void encrypt(int[] secret_key, int[] message);	
}
