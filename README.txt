// Name: Kevin Wong
// ID: 1402456

README:

To Compile program:

javac *.java
javah MyEncrypt
javah MyDecrypt
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libdecrypt.so lib_decrypt.c
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libencrypt.so lib_encrypt.c
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.

// or use export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PWD if the first export doesn't work.
// Or run the provided make.sh file

Pre-req:
-Note to have the program running correctly. There must be be shadow_table.txt in the Server directory encrypted with the correct entries and corresponding salt and hash program that the server runs with. If there is not a shadow_table.txt available, you must create one with the provided ShadowTable.java file. This will be a stand-alone program used to create valid entries into the shadow_table.txt. This function is compile with javac ShadowTable.java. To run it you:

java ShadowTable

This will run the program and with a limit of at least 2 characters for the username and password field, will add onto any existing shadow_table.txt that is present in the directory. If there is not any, it will create one and add that entry. To exit the program, you stop it with Ctrl + c. This shadow_table.txt is produced with a different algorithm than TEA. It uses a SecureRandom to produce secure and random numbers (like the name implies) for salt and uses MessageDigest with an encryption of SHA-256 to hash. I used this as I believe it would be more secure than just using TEA, especially the communication line is using TEA. In addition, the username, salt and hashed fields were seperated by $ to make the message more encrypted (harder to tell which parts were seperated) than using a space. The salt and final values were changed to hex to make sure that they could be placed into the Shadow table without fear of formatting issues causing errors in the values. In addition, adding hex may make it more secure if people are not fimilar with it.

After the shadow_table.txt is created, you are able to run the Server and Client Pair.

To Run program:
After compilation, run the Server program first:

java Server

Afterwards run the Client program afterwards to connect:

java Client

Background:

In this program, we have a ShadowTable that generates a secure hashed and salted password shadow table. As mentioned above, which is used in the Server as explained below.

In the Server program, we have a multi-threaded program that will continously create threads to handle any new Clients that connect. This way it can handle many Clients at a single time. After the Server program connects with a Client pair, the pair will communicate with the given connection and exchange public keys produced a the KeyPairGenerator class. The algorithm used is the Diffie Hellman, in which the public keys are exchanged and combined with a private key to create a secret key. This secret key was used to encrypt with the TEA algorithm used for communication.

After the secret keys were exchanged, the client accepted a username and password from the user (limited to at least 2 characters) in which it would encrypt in TEA and send to the server. Note: The TEA algorithm given is used multiple times on the same message to ensure it is secure. It encrypts two int's (8 bytes at a time) so to get the message fully encrypted, I traversed throughout the message two at a time, overlapping only one at most for each element (except for the first and last) which resulted in chaining and a more encrypted message. After the encrypted message was sent to the server, the server would decrypt it with TEA, and then using the username, would look up in the Shadow table if there was a matching username. Once there was a matching username (if there wasn't authentication fails), the salt is taken from the Shadow Table, then added to the password sent, and hashed with the same hashing function to create the table. This was compared with the hashed field in the Shadow Table. If it matched, the Client was authenticated and sent an acknowledge signal ("ACK"). If it didn't the Client was sent an not acknowledged signal ("NOP"). In either case the signal was encrypted, as well as any communication.

After the Client is authenticated, the Client is able to sent filenames to request to download from the Server's directory. This is also encrypted, and if the file is not found, the Server sents a File Not Found signal ("FNF"), but if the file is found the Server sends a File Found signal ("ACK"). Then the Server will send the file over (after encryption) in the form of int[] to the user. Then the user will receive the file, decrypt the message and save it to their directory. They will overwrite any file that has the same name as the file they receive, so becareful you don't pick files you don't have copies of!

This will keep looping (Asking the Client for filenames and the Server will keep giving the corresponding reply and send the files if available), until the Client sends a encrypted finished signal ("FIN"). Once the Server obtains this signal, they will disconnect the thread, and both programs will end. However, the main Server program will still run and in addition will keep accepting multiple Clients at the same time. This can all happen concurrently.

Some Notes:
- Please keep the Client and Server in different directories to verify correct operation of the file transfer. As transfering the same file to the same directory may cause issues if both are using it at the same time. (Reading from the Server and Writing from the Client).
- In the Sequencing Diagram, not all of the conversions were noted as they were repetitive
- The Client, and ShadowTable only accept inputs that are >= 2
- The comparsions of the messages were done in the lowest level (String conversions) to ensure that any padding or additional affects of the conversions were verified to be eliminated. In addition the program works on any file.
- The TEA encrpytion was done in native C code and is done with a pass-by-reference which is one of the perks of using C. The TEA algorithm was run multiple times in this C code (for encrypt and decrypt) but the decrypt was ran backwards from the ending index that was last used by the encrypt
- General case exceptions were handled with Exception call to overlay all of the exceptions thrown through the code.
