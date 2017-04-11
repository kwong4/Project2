#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include "MyEncrypt.h"

void encrypt (int *v, int *k);

// Defines the JNICALL of the MyInsertionSort for insertionsort
JNIEXPORT jstring JNICALL Java_MyEncrypt_encrypt
(JNIEnv *env, jobject object, jint *secret_key, jstring message, jint size_array){

  // Converts Java String into constant char * to be used in c
  const char *input_str = (*env)->GetStringUTFChars(env, message, 0);

  // Master call of insertionsort
  encrypt((int*) input_str, secret_key);

  // Releases the converted variables
  (*env)->ReleaseStringUTFChars(env, input, input_str);

  return input_str;
}

void encrypt (int *v, jint *k){
/* TEA encryption algorithm */
unsigned int y = v[0], z=v[1], sum = 0;
unsigned int delta = 0x9e3779b9, n=32;

	while (n-- > 0){
		sum += delta;
		y += (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		z += (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
	}

	v[0] = y;
	v[1] = z;
}

