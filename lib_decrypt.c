#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include "MyDecrypt.h"

void decrypt (int *v, int *k);
void encrypt (int *v, int *k);

// Defines the JNICALL of the MyInsertionSort for insertionsort
JNIEXPORT void JNICALL Java_MyDecrypt_decrypt
(JNIEnv *env, jobject object, jintArray secret_key, jintArray message, jint size_array){

  // Converts Java String into constant char * to be used in c
  //const char *input_str = (*env)->GetStringUTFChars(env, message, 0);

  //int len = sizeof(input_str)/(sizeof(const char *));
  //char input_copy[len];
  //input_copy[len-1] = '\0';

  int len = (*env)->GetArrayLength(env, message);

  printf("Here's the array size: %i", len);
  //char buf[len];
  //GetByteArrayRegion(env, message, len, buf);
  //char* buf = Get

  //strncpy(input_copy, input_str, len);

  //printf("Here's what i'm going to decrypt: %s\n", message);
  //int len = GetArrayLength(env, secret_key);
  //int buf[len];
  //(*env)->GetByteArrayRegion(env, message, len, buf);
  //char *buf = (char *)(*env)->GetByteArrayElements(env, secret_key, 0);
  //char* buf = (char*) (*env)->GetByteArrayElements(env, secret_key, 0);

  //const char *local_secret_key = (*env)->GetStringUTFChars(env, secret_key, 0);

  //printf("Here's what I'm dealing with %s\n", local_secret_key);

  //printf("Here's my secretkey: %s\n", secret_key);
  //printf("Here's my secretkey converted: %i\n", buf);

  // Master call of insertionsort

  jint* converted_key = (*env)->GetIntArrayElements(env, secret_key, 0);
  jint* converted_message = (*env)->GetIntArrayElements(env, message, 0);
	
  decrypt(converted_message, converted_key);

  (*env)->SetIntArrayRegion(env, message, 0, len, converted_message);

  //printf("Here's what i'm dealing with afterwards %s\n", input_copy);

  //jstring jstrBuf = (*env)->NewStringUTF(env, input_copy);

  // Releases the converted variables
  //(*env)->ReleaseStringUTFChars(env, message, input_str);

  //jbyteArray array;
  //array = (*env)->NewByteArray(env, strlen(input_str));
  //(*env)->SetByteArrayRegion(env, array, 0, len, input_str);

  //jbyteArray array;
  //array = (*env)->NewByteArray(env, strlen(buf));
  //(*env)->SetByteArrayRegion(env, array, 0, len, (const char *) buf);

  //return array;
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

void encrypt (int *v, int *k){
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

