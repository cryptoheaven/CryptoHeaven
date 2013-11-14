/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.trace;

import java.io.*;
import java.net.*;
import java.util.*;

import com.CH_co.io.*;
import com.CH_co.util.*;

/**
 * This class acts as a central repository for an trace specific
 * properties. It reads an 'TraceProperties.properties' file containing trace-
 * specific properties. <p>
 *
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.18 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class TraceProperties extends Object {


// Constants and variables with relevant static code
//...........................................................................
  static final String PROPERTIES_FILE_NAME = "TraceProperties";
  static final String SAVE_EXT = ".properties";

  static final Properties properties = new Properties();
  private static int fileUniqueID;

  /** Default properties in case .properties file was not found. */
  private static String[][] DEFAULT_PROPERTIES;

  private static String alternatePropertiesDir = null;

  // Last (most recent or current) trace file used
  private static File lastTraceFile = null;

  static {
    fileUniqueID = new Random().nextInt(999999);
    initialLoad();
  }

  public static void initialLoad() {
    initialLoad(false, false);
  }

  public static void initialLoad(boolean withConsoleDebugInfo, boolean withTraceFileCleanup) {

    if (DEFAULT_PROPERTIES == null) {
      DEFAULT_PROPERTIES = new String[][] {
        {"TraceEnabled",                  "false"},
        {"Trace.*",                       "true"},
        {"Debug.Level.*",                 "10"},
        {"TraceBufferKB",                 "16"},
        {"OutputType",                    "file"},
        {"OutputFilePrefix",              "Engine"},
        {"OutputFileUseUniqueID",         "true"},
        {"OutputFileExt",                 "out"},
        {"OutputFileSizeMB",              "20"},
        {"OutputFiles",                   "2"},
        {"OutputFileInitialSeqNum",       "0"}
      };
    }

    // always load default properties first
    int n = DEFAULT_PROPERTIES.length;
    for (int i = 0; i < n; i++) {
      if (properties.getProperty(DEFAULT_PROPERTIES[i][0]) == null) {
        properties.setProperty(DEFAULT_PROPERTIES[i][0], DEFAULT_PROPERTIES[i][1]);
      }
    }

    // now load the saved properties possibly overwriting the default ones.
    String it = getPropertiesFullFileName();
    FileInputStream is = null;
    boolean ok = true;
    try {
      is = new FileInputStream(it);
      properties.load(is);
    } catch (Exception e) {
      ok = false;
      if (e instanceof FileNotFoundException) {
        if (withConsoleDebugInfo) {
          System.out.println("Could not open " + it + " file for reading.");
          e.printStackTrace();
        }
      } else if (e instanceof IOException) {
        if (withConsoleDebugInfo) {
          System.out.println("Error loading properties from " + it);
          e.printStackTrace();
        }
      }
    } finally {
      try { if (is != null) is.close(); } catch (Exception e) { }
    }

    if (!ok) {
      if (withConsoleDebugInfo) {
        System.out.println("WARNING: Unable to load \"" + it + "\" .  Will use default values instead.");
      }
    } else {
      if (withConsoleDebugInfo) {
        System.out.println("Trace properties loaded from " + it);
      }
      Trace.initialLoad(withConsoleDebugInfo, withTraceFileCleanup);
    }
  }


// Properties methods (excluding load and save, which are deliberately not
// supported).
//...........................................................................

  /** Get the value of a property for this algorithm. */
  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Get the value of a property for this algorithm, or return
   * <i>value</i> if the property was not set.
   */
  public static String getProperty(String key, String value) {
    return properties.getProperty(key, value);
  }

  /**
   * Set a property value.  Calls the hashtable method put. 
   * Provided for parallelism with the getProperties method. 
   * Enforces use of strings for property keys and values.
   */
  public static Object setProperty(String key, String value) {
    return properties.setProperty(key, value);
  }

  /** List algorithm properties to the PrintStream <i>out</i>. */
  public static void list(PrintStream out) {
    properties.list(out);
  }

  /** List algorithm properties to the PrintWriter <i>out</i>. */
  public static void list(PrintWriter out) {
    properties.list(out);
  }

//    public synchronized void load(InputStream in) throws IOException {}
//    public void save (OutputStream os, String comment) {}

  public static Enumeration propertyNames() {
    return properties.propertyNames();
  }

/*
  public static void store (OutputStream out, String header) throws IOException {
    properties.store(out, header);
  }

  public static boolean store () {
    boolean success = true;
    try {
      String fileName = getPropertiesFullFileName();
      FileOutputStream out = new FileOutputStream(fileName);
      store(out, fileName);
    } catch (Exception e) {
      success = false;
    }
    return success;
  }
*/

  public static void setAlternatePropertiesDir(String dir) {
    alternatePropertiesDir = dir;
    int jarDirIndex = 0;
    // when we have a leading '!' ...
    if ((jarDirIndex = alternatePropertiesDir.indexOf('!')) == 0) {
      URL url = URLs.getResourceURL("ch/cl/License.txt");
      String path = URLDecoder.decode(url.getPath());
      path = path.substring(0, path.lastIndexOf("/ch/cl/")+1);	// will work from inside jar	
      if (url.getProtocol().equalsIgnoreCase("jar") && path.startsWith("file:")) {
        path = path.substring("file:".length());
      }
      // if inside a JAR, reduce the path to the JAR's directory
      if (path.length() >= 2 && path.charAt(path.length() - 2) == '!') {
        int jarIndex = path.lastIndexOf('/', path.length() - 2);
        if (jarIndex >= 0) {
          path = path.substring(0, jarIndex + 1);
        }
      }
      // replace the '!' by ClassLoader's base directory
      String altPropDir = alternatePropertiesDir.substring(0, jarDirIndex);
      altPropDir += path;
      altPropDir += alternatePropertiesDir.substring(jarDirIndex+1);
      alternatePropertiesDir = altPropDir;
    }
    initialLoad();
  }


  public static String getPropertiesFullPathName() {
    String dir = "";
    if (alternatePropertiesDir != null) {
      dir = alternatePropertiesDir;
    } else {
      String folder = System.getProperty("user.home");
      String filesep = System.getProperty("file.separator");
      dir = folder + filesep;
    }
    return dir;
  }


  private static String getPropertiesFullFileName() {
    return TraceProperties.getPropertiesFullPathName() + PROPERTIES_FILE_NAME + SAVE_EXT;
  }

  private static String getOutputFullFileName(String name) {
    return TraceProperties.getPropertiesFullPathName() + name;
  }

  public static File getLastTraceFile() {
    return lastTraceFile;
  }

// Developer support: Tracing and debugging enquiry methods (package-private)
//...........................................................................

  /**
   * Return true if tracing is requested for a given class.<p>
   *
   * User indicates this by setting the tracing <code>boolean</code>
   * property for <i>label</i> in the <code>(algorithm).properties</code>
   * file. The property's key is "<code>Trace.<i>label</i></code>".<p>
   *
   * @param label  The name of a class.
   * @return True iff a boolean true value is set for a property with
   *      the key <code>Trace.<i>label</i></code>.
   */
  public static boolean isTraceable(Class c) {
    return isTraceable(Misc.getClassNameWithoutPackage(c));
  }
  private static boolean isTraceable(String label) {
    String s = getProperty("Trace." + label);
    if (s == null) {
      s = getProperty("Trace.*");
      if (s == null)
        return false;
    }
    return Boolean.valueOf(s.trim()).booleanValue();
  }

  /** Global Trace Enablement */
  public static boolean isTraceEnabled () {
    String s = getProperty("TraceEnabled");
    if (s == null)
      return false;
    return Boolean.valueOf(s.trim()).booleanValue();
  }

  /**
   * Return the debug level for a given class.<p>
   *
   * User indicates this by setting the numeric property with key
   * "<code>Debug.Level.<i>label</i></code>".<p>
   *
   * If this property is not set, "<code>Debug.Level.*</code>" is looked up
   * next. If neither property is set, or if the first property found is
   * not a valid decimal integer, then this method returns 0.
   *
   * @param label  The name of a class.
   * @return  The required debugging level for the designated class.
   */
  public static int getLevel(Class c) {
    return getLevel(Misc.getClassNameWithoutPackage(c));
  }
  private static int getLevel(String label) {
    String s = getProperty("Debug.Level." + label);
    if (s == null) {
      s = getProperty("Debug.Level.*");
      if (s == null)
        return 0;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Return the PrintWriter to which tracing and debugging output is to
   * be sent.<p>
   *
   * User indicates this by setting the property with key <code>Output</code>
   * to the literal <code>out</code> or <code>err</code>.<p>
   *
   * By default or if the set value is not allowed, <code>System.err</code>
   * will be used.
   */
  public static PrintWriter getOutput(CountingOutputStream[] returnCountingOutput, int fileSequenceNumber, boolean withConsoleDebugInfo, boolean withTraceFileCleanup) {
    PrintWriter pw = null;
    String type = getProperty("OutputType");
    if (withConsoleDebugInfo) System.out.println("OutputType is " + type);

    if (type != null && type.equals("out"))
      pw = new PrintWriter(System.out, true);

    else if (type != null && !type.equals("err")) {
      try {
        String namePrefix = getProperty("OutputFilePrefix", "Trace");
        String nameExt = getProperty("OutputFileExt", "out");
        int numFiles = Integer.parseInt(TraceProperties.getProperty("OutputFiles", "1"));
        String uniqueID = "";
        if (Boolean.valueOf(TraceProperties.getProperty("OutputFileUseUniqueID", "false").trim()).booleanValue())
          uniqueID = ".traceID-" + fileUniqueID;
        String fileName = namePrefix + uniqueID + "-" + fileSequenceNumber + "." + nameExt;
        int fileSize = Integer.parseInt(TraceProperties.getProperty("OutputFileSizeMB", "0")) * 1024 * 1024;
        if (fileSize > 0 && returnCountingOutput != null && returnCountingOutput.length > 0) {
          if (withConsoleDebugInfo) System.out.println("Creating trace file " + fileName);
          String traceFileName = getOutputFullFileName(fileName);
          File traceFile = new File(traceFileName);
          returnCountingOutput[0] = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(traceFile), 32*1024));
          if (traceFile.exists()) {
            lastTraceFile = traceFile;
            if (withTraceFileCleanup) {
              // wipe the trace file on finalize only, we don't want it to be wiped while we are gathering data...
              GlobalProperties.addTempFileToCleanupOnFinalize(traceFile);
            }
          }
          if (withConsoleDebugInfo) System.out.println("created.");
          pw = new PrintWriter(returnCountingOutput[0], true);
          // see if we need to erase any old traces
          if (fileSequenceNumber+1 > numFiles) {
            String fileNameToDel = namePrefix + uniqueID + "-" + (fileSequenceNumber-numFiles) + "." + nameExt;
            File fileToDel = new File(getOutputFullFileName(fileNameToDel));
            try {
              fileToDel.delete();
            } catch (Throwable t) {
              if (withConsoleDebugInfo) t.printStackTrace();
            }
          }
        } else {
          if (withConsoleDebugInfo) System.out.println("Creating trace output file " + fileName);
          String traceFileName = getOutputFullFileName(fileName);
          File traceFile = new File(traceFileName);
          pw = new PrintWriter(new FileOutputStream(traceFile), true);
          if (traceFile.exists()) {
            lastTraceFile = traceFile;
            if (withTraceFileCleanup) {
              // wipe the trace file on finalize only, we don't want it to be wiped while we are gathering data...
              GlobalProperties.addTempFileToCleanupOnFinalize(traceFile);
            }
          }
          if (withConsoleDebugInfo) System.out.println("created.");
        }
      } catch (Throwable t) {
        pw = null;
        if (withConsoleDebugInfo) t.printStackTrace();
      }
    }

    if (pw == null)
      pw = new PrintWriter(System.err, true);
    return pw;
  }

}