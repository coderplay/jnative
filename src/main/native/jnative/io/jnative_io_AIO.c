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


JNIEXPORT jint JNICALL
Java_jnative_io_AIO_open0(JNIEnv *env, jclass clazz, jstring name, jint mode) {
  const char *fname = (*env)->GetStringUTFChars(env, name, 0);
  int fd = open(fname, mode);
  return (jint) fd;
}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_close(JNIEnv *env, jclass clazz, jint fd) {
  close(fd);
}


JNIEXPORT jint JNICALL
Java_jnative_io_AIO_sizeOfIocb(JNIEnv *env, jclass clazz) {
  return (jint) sizeof(struct iocb);
}

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
Java_jnative_io_AIO_prepare(JNIEnv *env, jclass clzz, jlong iocb_adr,
    jint command, jint fd, jlong offset, jlong buf_adr, jint len, jint eventfd) {
  struct iocb *iocb = (void *)iocb_adr;
  void* buffer = (void *) buf_adr;

  iocb->aio_fildes = fd;
  iocb->aio_lio_opcode = command;
  iocb->aio_reqprio = 0;
  iocb->u.c.buf = buffer;
  iocb->u.c.nbytes = len;
  iocb->u.c.offset = offset;

  if (eventfd > 0) {
    iocb->u.c.flags |= (1 << 0) /* IOCB_FLAG_RESFD */;
    iocb->u.c.resfd = (unsigned) eventfd;
  }
}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_submit0(JNIEnv *env, jclass clazz, jlong context, jlong nr, jlong iocbs_adr) {
  io_context_t ctx = (void *) context;
  struct iocb **iocbs = (void *)iocbs_adr;
  if(io_submit(ctx, (long)nr, iocbs) < 0) {
    THROW(env, "java/lang/InternalError", "Error when submitting IO");
  }
}


JNIEXPORT jint JNICALL Java_jnative_io_AIO_getEvents
  (JNIEnv *env, jclass clazz, jlong ctx, jlongArray eventsAddress, jlong timeout) {
  return (jint) 0;
}

JNIEXPORT void JNICALL
Java_jnative_io_AIO_destroy(JNIEnv *env, jclass clazz, jlong ctx) {
  if (io_destroy((io_context_t) ctx) < 0) {
    THROW(env, "java/lang/InternalError", "Error when destroying an AIO context");
  }
}
