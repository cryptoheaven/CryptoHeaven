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

import com.CH_co.util.GlobalProperties;
import com.CH_co.util.Misc;
import java.util.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class TraceDiagnostics {

  private static String[][] defaults = new String[][] {
      {"TraceEnabled",                  "true"},
      {"Trace.*",                       "true"},
      {"Debug.Level.*",                 "10"},
      {"TraceBufferKB",                 "1"},
      {"OutputType",                    "file"},
      {"OutputFilePrefix",              "BugReport"},
      {"OutputFileUseUniqueID",         "false"},
      {"OutputFileExt",                 "txt"},
      {"OutputFileSizeMB",              "0"},
  };

  public static void traceStart(Properties traceProperties) {
    // load defaults first
    for (int i=0; i<defaults.length; i++) {
      TraceProperties.setProperty(defaults[i][0], defaults[i][1]);
    }
    // load custom properties next
    if (traceProperties != null) {
      Enumeration keys = traceProperties.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = traceProperties.getProperty(key);
        TraceProperties.setProperty(key, value);
      }
    }
    // initialize trace after setting the properties
    Trace.initialLoad(false, true);
    initialDiagnosticsInfo(null);
  }

  /**
   * Default tracing with file output
   * @param userId Id used to customize output file name.
   */
  public static void traceStart(Long userId) {
    // load defaults first
    for (int i=0; i<defaults.length; i++) {
      TraceProperties.setProperty(defaults[i][0], defaults[i][1]);
    }
    // use supplied userID to customize the output file
    TraceProperties.setProperty("OutputFilePrefix", "BugReport-"+(userId != null ? userId+"-" : "")+Misc.getFormattedDateFileStr(new Date()));
    // initialize trace after setting the properties
    Trace.initialLoad(false, true);
    initialDiagnosticsInfo(null);
  }

  public static void initialDiagnosticsInfo(StringBuffer infoBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceDiagnostics.class, "initialDiagnosticsInfo(StringBuffer infoBuffer)");

    Runtime runtime = Runtime.getRuntime();

    if (trace != null) {
      trace.data(1, GlobalProperties.PROGRAM_FULL_NAME);
      trace.data(2, "max memory", Misc.getFormattedSize(runtime.maxMemory(), 4, 3));
      trace.data(3, "total memory", Misc.getFormattedSize(runtime.totalMemory(), 4, 3));
      trace.data(4, "free memory", Misc.getFormattedSize(runtime.freeMemory(), 4, 3));
      trace.data(100, "List of Environmet Variables");
    }

    if (infoBuffer != null) {
      infoBuffer.append(GlobalProperties.PROGRAM_FULL_NAME).append('\n');
      infoBuffer.append("max memory ").append(Misc.getFormattedSize(runtime.maxMemory(), 4, 3)).append('\n');
      infoBuffer.append("total memory ").append(Misc.getFormattedSize(runtime.totalMemory(), 4, 3)).append('\n');
      infoBuffer.append("free memory ").append(Misc.getFormattedSize(runtime.freeMemory(), 4, 3)).append('\n');
      infoBuffer.append("List of Environmet Variables").append('\n');
    }

    Properties props = System.getProperties();
    Enumeration keyEnum = props.keys();
    TreeMap propMap = new TreeMap();
    while (keyEnum.hasMoreElements()) {
      String key = (String) keyEnum.nextElement();
      String value = props.getProperty(key);
      propMap.put(key, value);
    }
    Set keySet = propMap.keySet();
    Iterator keyIter = keySet.iterator();
    int count = 0;
    while (keyIter.hasNext()) {
      String key = (String) keyIter.next();
      String value = (String) propMap.get(key);
      count ++;
      if (trace != null) trace.data(100+count, key, value);
      if (infoBuffer != null) infoBuffer.append(key).append(" : ").append(value).append('\n');
    }

    if (trace != null) trace.exit(TraceDiagnostics.class);
  }

  public static void traceStop() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceDiagnostics.class, "traceStop()");
    if (trace != null) {
      Runtime runtime = Runtime.getRuntime();
      trace.data(1, "total memory", Misc.getFormattedSize(runtime.totalMemory(), 4, 3));
      trace.data(2, "free memory", Misc.getFormattedSize(runtime.freeMemory(), 4, 3));
      trace.data(3, "TRACE STOP");
    }
    TraceProperties.setProperty("TraceEnabled", "false");
    Trace.initialLoad();
    if (trace != null) trace.exit(TraceDiagnostics.class);
  }

}