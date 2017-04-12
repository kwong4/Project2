import java.util.Arrays;

public class MyEncrypt extends Thread{
	private int[] secret_key;
	private int size;
	private int[] message;
	private int[] encrpyted_message;

	public int[] getEncrpyted_message() {
		return encrpyted_message;
	}
	
	// Constructor for MyEncrypt
	public MyEncrypt(int[] secret_key, int[] message, int size) {
		this.secret_key = secret_key;
		this.size = size;
		this.message = message;
	}

	// Thread method required to overwrite to use run Thread
	public void run(){
			
		try{
			//System.out.println("Here's my converted key inside MyEncrypt: " + Arrays.toString(this.secret_key));
			// Loads the C library for insertionsort
			System.loadLibrary("encrypt");

			// Runs the C implementation of insertionsort
			encrypt(this.secret_key, this.message, this.size);
		}
		
		// Checks if we timed out
		catch (ThreadDeath td){
			System.out.println("Thread Timed Out");
			//encrpyted_message = "";
			throw new ThreadDeath();
		}
	}
		
	// Native method for insertionsort
	public native void encrypt(int[] secret_key, int[] message, int size);	
}
