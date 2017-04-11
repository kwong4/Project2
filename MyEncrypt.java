
public class MyEncrypt extends Thread{
	private int[] secret_key;
	private int size;
	private String message;
	private String encrpyted_message;

	public String getEncrpyted_message() {
		return encrpyted_message;
	}
	
	// Constructor for MyEncrypt
	public MyEncrypt(int[] secret_key, String message, int size) {
		this.secret_key = secret_key;
		this.size = size;
		this.message = message;
		this.encrpyted_message = "";
	}

	// Thread method required to overwrite to use run Thread
	public void run(){
			
		try{
			// Loads the C library for insertionsort
			System.loadLibrary("encrypt");

			// Runs the C implementation of insertionsort
			encrpyted_message = encrypt(this.secret_key, this.message, this.size);
		}
		
		// Checks if we timed out
		catch (ThreadDeath td){
			System.out.println("Thread Timed Out");
			encrpyted_message = "";
			throw new ThreadDeath();
		}
	}
		
	// Native method for insertionsort
	public native String encrypt(int[] secret_key, String message, int size);	
}
