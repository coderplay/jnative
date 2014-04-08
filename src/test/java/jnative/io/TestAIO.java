/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnative.io;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

public class TestAIO {

  @Test
  public void testWrite() throws FileNotFoundException {
    int fd = AIO.open(".test", AIO.O_CREAT | AIO.O_RDWR | AIO.O_DIRECT);
    AIO aio = new AIO(64);
    byte[] data = "Hello AIO!\n".getBytes();
    ByteBuffer buf = ByteBuffer.allocateDirect(16);
    buf.put(data);
    for (int i = 0; i < 64; i++) {
      aio.prepareWrite(fd, data.length * i, buf);
    }

    aio.submit();

  }
}
