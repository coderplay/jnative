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
 
package jnative;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class JNativeCodeLoader {
  public static final String LIBRARY_NAME = "jnative";
  /**
   * The system property that causes jnative to ignore the embedded native
   * library and load it from the normal library path instead. It is false by
   * default (i.e. it loads the embedded native library if found over the
   * library path).
   */
  public static final String USE_BINARIES_ON_LIB_PATH =
      "jnative.use.libpath";

  private static final Log LOG = LogFactory.getLog(JNativeCodeLoader.class);
  private static boolean nativeLibraryLoaded = false;

  static {
    try {
      //try to load the lib
      if (!useBinariesOnLibPath()) {
        File unpackedFile = unpackBinaries();
        if (unpackedFile != null) { // the file was successfully unpacked
          String path = unpackedFile.getAbsolutePath();
          System.load(path);
          LOG.info("Loaded jnative library from the embedded binaries");
        } else { // fall back
          System.loadLibrary(LIBRARY_NAME);
          LOG.info("Loaded jnative library from the library path");
        }
      } else {
        System.loadLibrary(LIBRARY_NAME);
        LOG.info("Loaded jnative library from the library path");
      }
      nativeLibraryLoaded = true;
    } catch (Throwable t) {
      LOG.error("Could not load native gpl library", t);
      nativeLibraryLoaded = false;
    }
  }

  /**
   * Are the jnative libraries loaded?
   * @return true if loaded, otherwise false
   */
  public static boolean isNativeCodeLoaded() {
    return nativeLibraryLoaded;
  }

  private static boolean useBinariesOnLibPath() {
    return Boolean.getBoolean(USE_BINARIES_ON_LIB_PATH);
  }

  /**
   * Locates the native library in the jar (loadble by the classloader really),
   * unpacks it in a temp location, and returns that file. If the native library
   * is not found by the classloader, returns null.
   */
  private static File unpackBinaries() {
    // locate the binaries inside the jar
    String fileName = System.mapLibraryName(LIBRARY_NAME);
    String directory = getDirectoryLocation();
    // use the current defining classloader to load the resource
    InputStream is =
        JNativeCodeLoader.class.getResourceAsStream(directory + "/" + fileName);
    if (is == null) {
      // specific to mac
      // on mac the filename can be either .dylib or .jnilib: try again with the
      // alternate name
      if (getOsName().contains("Mac")) {
        if (fileName.endsWith(".dylib")) {
          fileName = fileName.replace(".dylib", ".jnilib");
        } else if (fileName.endsWith(".jnilib")) {
          fileName = fileName.replace(".jnilib", ".dylib");
        }
        is = JNativeCodeLoader.class.getResourceAsStream(directory + "/" + fileName);
      }
      // the OS-specific library was not found: fall back on the library path
      if (is == null) {
        return null;
      }
    }

    // write the file
    byte[] buffer = new byte[8192];
    OutputStream os = null;
    try {
      // prepare the unpacked file location
      File unpackedFile = File.createTempFile("unpacked-", "-" + fileName);
      // ensure the file gets cleaned up
      unpackedFile.deleteOnExit();

      os = new FileOutputStream(unpackedFile);
      int read = 0;
      while ((read = is.read(buffer)) != -1) {
        os.write(buffer, 0, read);
      }

      // set the execution permission
      unpackedFile.setExecutable(true, false);
      LOG.debug("temporary unpacked path: " + unpackedFile);
      // return the file
      return unpackedFile;
    } catch (IOException e) {
      LOG.error("could not unpack the binaries", e);
      return null;
    } finally {
      try { is.close(); } catch (IOException ignore) {}
      if (os != null) {
        try { os.close(); } catch (IOException ignore) {}
      }
    }
  }

  private static String getDirectoryLocation() {
    String osName = getOsName().replace(' ', '_');
    boolean windows = osName.toLowerCase().contains("windows");
    if (!windows) {
      String location = "/native/" + osName + "-" + System.getProperty("os.arch") + "-" +
          System.getProperty("sun.arch.data.model") + "/lib";
      LOG.debug("location: " + location);
      return location;
    } else {
      String location = "/native/" + System.getenv("OS") + "-" + System.getenv("PLATFORM") + "/lib";
      LOG.debug("location: " + location);
      return location;
    }
  }

  private static String getOsName() {
    return System.getProperty("os.name");
  }
}