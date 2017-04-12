#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include "MyDecrypt.h"

void decrypt (int *v, int *k);

// Defines the JNICALL of the MyDecrypt for decrypt
JNIEXPORT void JNICALL Java_MyDecrypt_decrypt
(JNIEnv *env, jobject object, jintArray secret_key, jintArray message){

  // Get Size of message
  int len = (*env)->GetArrayLength(env, message);

  // Convert jintArray to jints
  jint* converted_key = (*env)->GetIntArrayElements(env, secret_key, 0);
  jint* converted_message = (*env)->GetIntArrayElements(env, message, 0);
	
  // Decrypt Message
  decrypt(converted_message, converted_key);

  // Pass by Reference result back into message
  (*env)->SetIntArrayRegion(env, message, 0, len, converted_message);
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

