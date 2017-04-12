#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include "MyEncrypt.h"

void decrypt (int *v, int *k);
void encrypt (int *v, int *k);

// Defines the JNICALL of the MyInsertionSort for insertionsort
JNIEXPORT void JNICALL Java_MyEncrypt_encrypt
(JNIEnv *env, jobject object, jintArray secret_key, jintArray message, jint size_array){

  // Converts Java String into constant char * to be used in c
  //const char *input_str = (char *) (*env)->GetStringUTFChars(env, message, 0);
  //int len = sizeof(input_str)/(sizeof(const char *));

  int len = (*env)->GetArrayLength(env, message);

  jint* converted_key = (*env)->GetIntArrayElements(env, secret_key, 0);
  jint* converted_message = (*env)->GetIntArrayElements(env, message, 0);

  //const char *local_secret_key = (*env)->GetStringUTFChars(env, secret_key, 0);

  encrypt(converted_message, converted_key);

  //printf("Here's the encrypted: %s\n", (char*) input_str);

  //jbyteArray array;
  //array = (*env)->NewByteArray(env, strlen(input_str));
  //(*env)->SetByteArrayRegion(env, array, 0, len, input_str);

  //jstring jstrBuf = (*env)->NewStringUTF(env, input_str);

  (*env)->SetIntArrayRegion(env, message, 0, len, converted_message);

  //return array;
}

void encrypt (jint *v, jint *k){
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

void decrypt (int *v, int *k){
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

