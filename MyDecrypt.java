import java.util.Arrays;

public class MyDecrypt extends Thread{
	private int[] secret_key;
	private int size;
	private int[] message;
	private int[] decrpyted_message;

	public int[] getDecrpyted_message() {
		return this.message;
	}
	
	// Constructor for MyEncrypt
	public MyDecrypt(int[] secret_key, int[] message, int size) {
		this.secret_key = secret_key;
		this.size = size;
		this.message = message;
	}

	// Thread method required to overwrite to use run Thread
	public void run(){
			
		try{
			//System.out.println("Here's my converted key inside MyDecrypt: " + Arrays.toString(this.secret_key));
			// Loads the C library for insertionsort
			System.loadLibrary("decrypt");

			// Runs the C implementation of insertionsort
			decrypt(this.secret_key, this.message, this.size);
		}
		
		// Checks if we timed out
		catch (ThreadDeath td){
			System.out.println("Thread Timed Out");
			//decrpyted_message = "";
			throw new ThreadDeath();
		}
	}
		
	// Native method for insertionsort
	public native void decrypt(int[] secret_key, int[] message, int size);	
}
