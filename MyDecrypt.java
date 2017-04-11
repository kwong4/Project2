
public class MyDecrypt extends Thread{
	private int[] secret_key;
	private int size;
	private String message;
	private String decrpyted_message;

	public String getDecrpyted_message() {
		return decrpyted_message;
	}
	
	// Constructor for MyEncrypt
	public MyDecrypt(int[] secret_key, String message, int size) {
		this.secret_key = secret_key;
		this.size = size;
		this.message = message;
		this.decrpyted_message = "";
	}

	// Thread method required to overwrite to use run Thread
	public void run(){
			
		try{
			// Loads the C library for insertionsort
			System.loadLibrary("decrypt");

			// Runs the C implementation of insertionsort
			decrpyted_message = decrypt(this.secret_key, this.message, this.size);
		}
		
		// Checks if we timed out
		catch (ThreadDeath td){
			System.out.println("Thread Timed Out");
			decrpyted_message = "";
			throw new ThreadDeath();
		}
	}
		
	// Native method for insertionsort
	public native String decrypt(int[] secret_key, String message, int size);	
}
