javac *.java
javah MyEncrypt
javah MyDecrypt
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libencrypt.so lib_encrypt.c
gcc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared -fpic -o libencrypt.so lib_encrypt.c
