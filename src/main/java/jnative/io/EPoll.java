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

public class EPoll {
  private EPoll() {
  }

  private static final int SIZEOF_EPOLLEVENT = eventSize();
  private static final int OFFSETOF_EVENTS = eventsOffset();
  private static final int OFFSETOF_FD = dataOffset();

  // EventLoop operations and constants
  public static final int EPOLLIN = 0x01;
  public static final int EPOLLOUT = 0x02;
  public static final int EPOLLACCEPT = 0x04;
  public static final int EPOLLRDHUP = 0x08;


  // opcodes
  static final int EPOLL_CTL_ADD = 1;
  static final int EPOLL_CTL_DEL = 2;
  static final int EPOLL_CTL_MOD = 3;

  // flags
  static final int EPOLLONESHOT = (1 << 30);


  // -- Native methods --

  private static native int eventSize();

  private static native int eventsOffset();

  private static native int dataOffset();

  static native int eventFd();

  static native int epollCreate() throws IOException;

  static native int epollCtl(int epfd, int opcode, int fd, int events);

  static native int epollWait(int epfd, long pollAddress, int numfds)
      throws IOException;

//  static native int epollWait(int efd, long[] events, int timeout);
//  static native void epollCtlAdd(int epfd, final int fd, final int flags, final int id);
//  static native void epollCtlMod(int epfd, final int fd, final int flags, final int id);
//  static native void epollCtlDel(int epfd, final int fd);

}
