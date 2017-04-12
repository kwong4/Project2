#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include "MyEncrypt.h"

void encrypt (int *v, int *k);

// Defines the JNICALL of the MyInsertionSort for insertionsort
JNIEXPORT void JNICALL Java_MyEncrypt_encrypt
(JNIEnv *env, jobject object, jintArray secret_key, jintArray message, jint size_array){

  int len = (*env)->GetArrayLength(env, message);

  jint* converted_key = (*env)->GetIntArrayElements(env, secret_key, 0);
  jint* converted_message = (*env)->GetIntArrayElements(env, message, 0);

  encrypt(converted_message, converted_key);

  (*env)->SetIntArrayRegion(env, message, 0, len, converted_message);
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
