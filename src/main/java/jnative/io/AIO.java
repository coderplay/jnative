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

package jnative.io;

import jnative.JNativeCodeLoader;
import jnative.utils.NativeObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.nio.ch.DirectBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is not thread safe.
 */
public class AIO {

  private static final Log LOG = LogFactory.getLog(AIO.class.getName());

  private static boolean nativeLzmaLoaded = false;

  static {
    if (JNativeCodeLoader.isNativeCodeLoaded()) {
      nativeLzmaLoaded = true;
      if (nativeLzmaLoaded) {
        LOG.info(
            "Successfully loaded & initialized native aio library");
      } else {
        LOG.error("Failed to load/initialize native aio library");
      }
    } else {
      LOG.error("Cannot load native aio library without jnative");
      nativeLzmaLoaded = false;
    }
  }

  private static final int SIZE_IOCB = sizeOfIocb();

  // Flags for open() call from bits/fcntl.h
  public static final int O_RDONLY   =    00;
  public static final int O_WRONLY   =    01;
  public static final int O_RDWR     =    02;
  public static final int O_CREAT    =  0100;
  public static final int O_EXCL     =  0200;
  public static final int O_NOCTTY   =  0400;
  public static final int O_TRUNC    = 01000;
  public static final int O_APPEND   = 02000;
  public static final int O_NONBLOCK = 04000;
  public static final int O_SYNC   =  010000;
  public static final int O_ASYNC  =  020000;
  public static final int O_DIRECT = 040000;	 /* Direct disk access.	*/
  public static final int O_FSYNC = O_SYNC;
  public static final int O_NDELAY = O_NONBLOCK;


  public static final int IO_CMD_PREAD   = 0;
  public static final int IO_CMD_PWRITE  = 1;
  public static final int IO_CMD_FSYNC   = 2;
  public static final int IO_CMD_FDSYNC  = 3;
  public static final int IO_CMD_POLL    = 5; /* Never implemented in mainline, see io_prep_poll */
  public static final int IO_CMD_NOOP    = 6;
  public static final int IO_CMD_PREADV  = 7;
  public static final int IO_CMD_PWRITEV = 8;

  static class IOCommand {
    int type;
    int fd;
    long offset;
    ByteBuffer data;

    IOCommand(int type, int fd, long offset, ByteBuffer data) {
      this.type = type;
      this.fd = fd;
      this.offset = offset;
      this.data = data;
    }

  }

  private final long context;
  private List<IOCommand> pendingOps;
  private int eventFd;
  private long[] events;

  public AIO(int maxEvents) {
    context = setup();
    events = new long[maxEvents];
    pendingOps = new ArrayList<IOCommand>();
  }

  public void register(int eventFd) {
    this.eventFd = eventFd;
  }

  public void prepareRead(int fd, long offset, ByteBuffer dst)  {
    pendingOps.add(new IOCommand(IO_CMD_PREAD, fd, offset, getDirect(dst)));
  }

  public void prepareWrite(int fd, long offset, ByteBuffer src) {
    pendingOps.add(new IOCommand(IO_CMD_PWRITE, fd, offset, getDirect(src)));
  }

  private ByteBuffer getDirect(ByteBuffer buf) {
    if (buf instanceof DirectBuffer)
      return buf;

    int pos = buf.position();
    int lim = buf.limit();
    assert (pos <= lim);
    int rem = (pos <= lim ? lim - pos : 0);
    ByteBuffer bb = ByteBuffer.allocate(rem);
    bb.put(buf);
    bb.flip();
    return bb;
  }

  public void submit() {
    NativeObject eventArray = new NativeObject(pendingOps.size() * SIZE_IOCB, true);
    long eventArrayAddress = eventArray.address();
    // TODO: more efficient way, pass the command list to native code through only one jni call
    for (int i = 0; i < pendingOps.size(); i++) {
      IOCommand command = pendingOps.get(i);

      final ByteBuffer bb = command.data;
      int pos = bb.position();
      int lim = bb.limit();
      assert (pos <= lim);
      int rem = (pos <= lim ? lim - pos : 0);

      prepare(eventArrayAddress + i * SIZE_IOCB, command.type, command.fd,
          command.offset,
          ((DirectBuffer) bb).address() + pos,
          rem, eventFd);
    }
    submit0(context, pendingOps.size(), eventArrayAddress);
    eventArray.free();
    pendingOps.clear();
  }

  public int poll(long timeout) {
    return getEvents(context, events, timeout);
  }


  public void close() {
    destroy(context);
  }

  public static int open(String fileName, int mode) throws FileNotFoundException {
    return open0(fileName, mode);
  }

  static native int open0(String name, int mode) throws FileNotFoundException;

  public static native void close(int fd) throws IOException;

  static native int sizeOfIocb();

  /**
   * Create an Asynchronous I/O context
   * <code>int io_setup(unsigned nr_events, aio_context_t *ctxp);</code>
   * @return
   * @throws IOException
   */
  static native long setup();

  static native void prepare(long iocb, int command, int fd, long offset, long buf, int len, int eventFd);

  static native void submit0(long context, long nr, long iocbs);

  static native int getEvents(long context, long[] events, long timeout);

  static native void destroy(long context);

}
