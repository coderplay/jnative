/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "aio.h"

JNIEXPORT jlong JNICALL
Java_jnative_io_AIO_setup(JNIEnv *env, jclass clazz) {
  io_context_t ctx = 0;
  if (io_setup(8192, &ctx) < 0) {
    THROW(env, "java/lang/InternalError", "Error when setting up an AIO context");
    return (jlong) 0;
  }

  return (jlong) ctx;
}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_setEventFd(JNIEnv *env, jclass clazz, jint eventFd) {

}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_submitIO(JNIEnv *env, jclass clazz) {


}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_preparePRead(JNIEnv *env, jclass clazz, jlong ctx,
    jint eventfd, jobject buf, jlong pos, jint lim) {

}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_preparePWrite(JNIEnv *env, jclass clazz, jlong ctx,
    jint eventfd, jobject buf, jlong pos, jint lim) {

}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_getEvents(JNIEnv *env, jclass clazz) {

}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_destory(JNIEnv *env, jclass clazz, jlong ctx) {

  if (io_destroy((io_context_t) ctx) < 0) {
    THROW(env, "java/io/IOException", "Error when destorying an AIO context");
  }
}
