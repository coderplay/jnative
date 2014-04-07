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

import java.io.IOException;
import java.nio.ByteBuffer;

public class AIO {

  /**
   * Create an Asynchronous I/O context
   * <code>int io_setup(unsigned nr_events, aio_context_t *ctxp);</code>
   * @return
   * @throws IOException
   */
  public static native long setup() throws IOException;

  /**
   * io_set_eventfd
   * @param eventFd
   * @return
   * @throws IOException
   */
  public static native int setEventFd(int eventFd) throws IOException;

  public static native void submitIO() throws IOException;

  public static native void preparePRead(long context, int fd, ByteBuffer byteBuffer, long position, int limit)
      throws IOException;

  public static native void preparePWrite(long context, int fd, ByteBuffer byteBuffer, long position, int limit)
      throws IOException;

  public static native void getEvents() throws IOException;

  public static native void destory(long context) throws IOException;

}
