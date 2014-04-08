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

import jnative.utils.NativeObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is not thread safe.
 */
public class AIO {
  private static final int SIZE_IOCB = sizeOfIocb();

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
    FileDescriptor fd;
    ByteBuffer data;

    IOCommand(int type, FileDescriptor fd, ByteBuffer data) {
      this.type = type;
      this.fd = fd;
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

  public void prepareRead(FileDescriptor fd, ByteBuffer dst) throws IOException {
    pendingOps.add(new IOCommand(IO_CMD_PREAD, fd, dst));
  }

  public void prepareWrite(FileDescriptor fd, ByteBuffer src) throws IOException {
    pendingOps.add(new IOCommand(IO_CMD_PWRITE, fd, src));
  }

  public void submit() throws IOException {
    NativeObject eventArray = new NativeObject(pendingOps.size() * SIZE_IOCB, true);
    long eventArrayAddress  = eventArray.address();
    // TODO: more efficient way, pass the command list to native code through only one jni call
    for (int i = 0; i < pendingOps.size(); i++) {
      IOCommand command = pendingOps.get(i);
      prepare(eventArrayAddress + i * SIZE_IOCB, command.type, command.fd, command.data, eventFd);
    }
    submit0(context, pendingOps.size(), eventArrayAddress);
    eventArray.free();
    pendingOps.clear();
  }

  public int poll(long timeout) {
    return getEvents(context, events, timeout);
  }


  public void close() {
    destory(context);
  }

  static native int sizeOfIocb();

  /**
   * Create an Asynchronous I/O context
   * <code>int io_setup(unsigned nr_events, aio_context_t *ctxp);</code>
   * @return
   * @throws IOException
   */
  static native long setup();

  static native void prepare(long iocb, int command, FileDescriptor fd, ByteBuffer buf, int eventFd);

  static native void submit0(long context, long nr, long iocbs);

  static native int getEvents(long context, long[] events, long timeout);

  static native void destory(long context);

}
