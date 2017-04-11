#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include "MyDecrypt.h"

void decrypt (int *v, jint *k)

// Defines the JNICALL of the MyInsertionSort for insertionsort
JNIEXPORT jstring JNICALL Java_MyDecrypt_decrypt
(JNIEnv *env, jobject object, jint *secret_key, jstring message, jint size_array){

  // Converts Java String into constant char * to be used in c
  const char *input_str = (*env)->GetStringUTFChars(env, message, 0);

  // Master call of insertionsort
  encrypt((int*) input_str, secret_key);

  // Releases the converted variables
  (*env)->ReleaseStringUTFChars(env, input, input_str);

  return input_str;
}

void decrypt (int *v, jint *k){
/* TEA decryption routine */
unsigned int n=32, sum, y=v[0], z=v[1];
unsigned int delta=0x9e3779b9l;

	sum = delta<<5;
	while (n-- > 0){
		z -= (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
		y -= (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		sum -= delta;
	}
	v[0] = y;
	v[1] = z;
}

