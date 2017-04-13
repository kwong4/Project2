clear
javac *.java
javah MyEncrypt
javah MyDecrypt
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libdecrypt.so lib_decrypt.c
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libencrypt.so lib_encrypt.c
