/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.trace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.Hashtable;
import java.io.*;

import com.CH_co.io.*;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Thread safe Tracing Facility
 * @author  Marcin Kurzawa
 * @version
 */
public class Trace extends Object {

  // debug help to show threads that are non-deamon useful in debugin program non-exits after logout...
  private static boolean DEBUG__PRINT_OUT_NON_DEAMON_THREADS = true;

  // Global flag to indicate if DEBUG is enabled
  public static boolean DEBUG = false;

  // Debug.Levels
  // error and exception short description
  public static final int ERROR = 2;
  // always trace args -- just like entry and exit points
  public static final int ARGS = 0;
  // exception stack
  public static final int EXCEPTION_STACK_DUMP = 4;
  public static final int WARNING = 6;
  public static final int DATA = 8;
  public static final int INFO = 10;
  public static final int VERBOSE = 12;

  // Storage for Trace objects
  private static Hashtable hashTable;
  private static boolean initialized;
  private static Trace dumpingTrace = new Trace();
  private static Thread lastThread;
  private static final Object staticMonitor = new Object();

  // Storage for Class objects
  private Stack stack;
  // Storage for String objects
  private Stack stack2;

  // output of the trace is redirected to stdout, stderr, or file based on Output property in TraceProperties
  private static PrintWriter out;
  private static boolean flushEveryTime = true;
  private static CountingOutputStream cOut;
  private static long maxOutputFileSize;
  private static long maxBufferedSize;
  private static long bufferedSize;
  private static int outputFileSequenceNumber;

  private static String[] headings;

  private int pauseCount;
//  private static Hashtable tracePauseCount;

  /** Creates new Trace */
  private Trace() {
    stack = new Stack();
    stack2 = new Stack();
  }

  /** Initializes the Tracing Facility if Tracing is enabled in TraceProperties.
   *  To Enable Tracing set property Trace.* = true
   */
  static {
    // touch TraceProperties class as this will initialize Trace properly.
    TraceProperties.isTraceEnabled();
  }

  public static void initialLoad() {
    initialLoad(false, false);
  }

  public static void initialLoad(boolean withConsoleDebugInfo, boolean withTraceFileCleanup) {
    // initialize this static only once
    if (headings == null) {
      headings = new String[100];
      for (int i=0; i<headings.length; i++) {
        StringBuffer hBuf = new StringBuffer("");
        for (int k=0; k<i; k++) {
          hBuf.append("| ");
        }
        headings[i] = hBuf.toString();
      }
    }

    // close previously openned files
    if (out != null) {
      try { out.flush(); } catch (Throwable t) { }
      try { out.close(); } catch (Throwable t) { }
      out = null;
    }

    DEBUG = TraceProperties.isTraceEnabled();
    initialized = DEBUG;
    if (withConsoleDebugInfo) {
      if (initialized)
        System.out.println("Trace is Enabled");
      else
        System.out.println("Trace is Disabled");
    }
    if (initialized) {
      hashTable = new Hashtable();
      if (outputFileSequenceNumber == 0) {
        outputFileSequenceNumber = Integer.parseInt(TraceProperties.getProperty("OutputFileInitialSeqNum", "1"));
        if (withConsoleDebugInfo)
          System.out.println("Sequence number initialized to " + outputFileSequenceNumber);
      } else {
        if (withConsoleDebugInfo)
          System.out.println("Sequence number was already initialized to " + outputFileSequenceNumber);
      }
      out = getNewOutput(withConsoleDebugInfo, withTraceFileCleanup);
    }
  }

  private static PrintWriter getNewOutput() {
    return getNewOutput(false, false);
  }

  private static PrintWriter getNewOutput(boolean withConsoleDebugInfo, boolean withTraceFileCleanup) {
    CountingOutputStream[] cOutReturn = new CountingOutputStream[1];
    out = TraceProperties.getOutput(cOutReturn, outputFileSequenceNumber, withConsoleDebugInfo, withTraceFileCleanup);
    if (cOutReturn[0] != null) {
      cOut = cOutReturn[0];
      maxOutputFileSize = Long.parseLong(TraceProperties.getProperty("OutputFileSizeMB", "0")) * 1024L * 1024L;
    }
    if (out != null) {
      outputFileSequenceNumber ++;
      if (withConsoleDebugInfo)
        System.out.println("Next sequence number incremented to " + outputFileSequenceNumber);
    }
    int sz = 0;
    try {
      sz = Integer.parseInt(TraceProperties.getProperty("TraceBufferKB", "0")) * 1024;
      maxBufferedSize = sz;
    } catch (NumberFormatException e) {
    }
    flushEveryTime = sz == 0;
    if (sz > 0) {
      out = new PrintWriter(new BufferedWriter(out, sz), false);
    }
    return out;
  }

  public static Trace entry(Class c, String methodPrototype) {
    String className = Misc.getClassNameWithoutPackage(c);
    if (!initialized || !TraceProperties.isTraceable(c)) {
      return dumpingTrace;
    }
    else {
      synchronized (staticMonitor) {
        Trace threadTrace = (Trace) hashTable.get( new Integer(Thread.currentThread().hashCode()) );
        if (threadTrace == null) {
          threadTrace = new Trace();
          hashTable.put( new Integer(Thread.currentThread().hashCode()), threadTrace );
        }
        String str = className + " " + methodPrototype;
        threadTrace.addLine("|>" + str);
        threadTrace.stack.push(c);
        threadTrace.stack2.push(methodPrototype);
        return threadTrace;
      }
    } // end else
  }

  /**
   * Exit from a pair entry
   */
  private void exit(Class c, Object obj, boolean isObj) {
    // this debug is done on "exit" because threads may set its Deamon status in the constructor
    if (DEBUG__PRINT_OUT_NON_DEAMON_THREADS) {
      printOutNonDeamonThreads();
    }
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      String className = Misc.getClassNameWithoutPackage( c );
      if (stack.size() > 0) {
        Class expectedClass = (Class) stack.pop();
        String expectedMethod = (String) stack2.pop();
        {
          // clean up the method arguments
          int braceIndex = expectedMethod.indexOf('(');
          if (braceIndex > 0)
            expectedMethod = expectedMethod.substring(0, braceIndex) + "()";
        }

        // try poping missed exit points due to flying exceptions
        while (!c.equals(expectedClass)) {
          addLine("EXIT Trace: poping unexpected class: call with (Class="+Misc.getClassNameWithoutPackage(c)+" but expected (Class=" + Misc.getClassNameWithoutPackage(expectedClass) + ", method name="+expectedMethod+") encountered by thread="+Thread.currentThread());
          addLine("This signifies an out of synch trace exit point.");
          if (stack.size() > 0) {
            expectedClass = (Class) stack.pop();
            expectedMethod = (String) stack2.pop();
          } else {
            break;
          }
        }
        addLine("|<" + className + " " + expectedMethod + (isObj ? " rc="+objToStr(obj) : " rc void"));
        if (!c.equals(expectedClass)) {
          addLine("Exit points out of synch.  Expected exit was from " + Misc.getClassNameWithoutPackage(expectedClass));
        }
      } else {
        addLine("|<" + className + " " + "entry-method-was-not-stored" + (isObj ? " rc="+objToStr(obj) : " rc void"));
        addLine("Entry point for this exit point was not stored.");
      }
    }
  }
  public void exit(Class c, String str) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, (Object) str, true);
    }
  }
  public void exit(Class c, int rc) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, new Integer(rc), true);
    }
  }
  public void exit(Class c, long rc) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, new Long(rc), true);
    }
  }
  public void exit(Class c, double rc) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, new Double(rc), true);
    }
  }
  public void exit(Class c, boolean rc) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, Boolean.valueOf(rc), true);
    }
  }
  public void exit(Class c, Object obj) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, obj, true);
    }
  }
  public void exit(Class c) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      exit(c, null, false);
    }
  }
  public boolean isPaused() {
    synchronized (staticMonitor) {
      return pauseCount > 0;
    }
  }
  public int pause() {
    int token = 0;
    synchronized (staticMonitor) {
      token = pauseCount;
      pauseCount ++;
    }
    return token;
  }
  public void resume(int token) {
    synchronized (staticMonitor) {
      pauseCount = token;
    }
  }
//  public static boolean isPaused() {
//    synchronized (staticMonitor) {
//      Integer hashCode = new Integer(Thread.currentThread().hashCode());
//      if (tracePauseCount == null) return false;
//      Integer pauseCount = (Integer) tracePauseCount.get(hashCode);
//      return pauseCount != null;
//    }
//  }
//  public static Integer pause() {
//    Integer token = null;
//    synchronized (staticMonitor) {
//      Integer hashCode = new Integer(Thread.currentThread().hashCode());
//      if (tracePauseCount == null) tracePauseCount = new Hashtable();
//      Integer pauseCount = (Integer) tracePauseCount.get(hashCode);
//      token = pauseCount;
//      if (pauseCount == null)
//        tracePauseCount.put(hashCode, new Integer(1));
//      else
//        tracePauseCount.put(hashCode, new Integer(pauseCount.intValue()+1));
//    }
//    return token;
//  }
//  public static void resume(Integer token) {
//    synchronized (staticMonitor) {
//      if (tracePauseCount != null) {
//        Integer hashCode = new Integer(Thread.currentThread().hashCode());
//        if (token == null)
//          tracePauseCount.remove(hashCode);
//        else
//          tracePauseCount.put(hashCode, token);
//      }
//    }
//  }



  private String getHeading(int level) {
    switch(level) {
      case ARGS:
        return "args";
      case VERBOSE:
        return "verbose";
      case INFO:
        return "info";
      case DATA:
        return "data";
      case WARNING:
        return "warning";
      case ERROR:
        return "error";
      default:
        return "unsupported message type!";
    }
  }
  private void tracePoint(int level, int tracePoint, Object obj) {
    tracePoint(level, tracePoint, obj, null, null, 1);
  }
  private void tracePoint(int level, int tracePoint, Object obj1, Object obj2) {
    tracePoint(level, tracePoint, obj1, obj2, null, 2);
  }
  private void tracePoint(int level, int tracePoint, Object obj1, Object obj2, Object obj3) {
    tracePoint(level, tracePoint, obj1, obj2, obj3, 3);
  }
  private void tracePoint(int level, int tracePoint, Object obj1, Object obj2, Object obj3, int numObjs) {
    synchronized (staticMonitor) {
      if (debugLevelBelow(level)) return;
      switch (numObjs) {
        case 0:
          break;
        case 1:
          addLine(getHeading(level) + " " + tracePoint + ": " + objToStr(obj1));
          break;
        case 2:
          addLine(getHeading(level) + " " + tracePoint + ": " + objToStr(obj1) + ", " + objToStr(obj2));
          break;
        case 3:
          addLine(getHeading(level) + " " + tracePoint + ": " + objToStr(obj1) + ", " + objToStr(obj2) + ", " + objToStr(obj3));
          break;
      }
    }
  }

  public void verbose(int tracePoint, Object data) {
    if (this == dumpingTrace) return;
    tracePoint(VERBOSE, tracePoint, data);
  }


  public void info(int tracePoint, Object obj, boolean data) {
    if (this == dumpingTrace) return;
    tracePoint(INFO, tracePoint, obj, Boolean.valueOf(data));
  }
  public void info(int tracePoint, Object obj, long data) {
    if (this == dumpingTrace) return;
    tracePoint(INFO, tracePoint, obj, new Long(data));
  }
  public void info(int tracePoint, Object data) {
    if (this == dumpingTrace) return;
    tracePoint(INFO, tracePoint, data);
  }
  public void info(int tracePoint, Object obj1, Object obj2) {
    if (this == dumpingTrace) return;
    tracePoint(INFO, tracePoint, obj1, obj2);
  }
  public void info(int tracePoint, Object obj1, Object obj2, Object obj3) {
    if (this == dumpingTrace) return;
    tracePoint(INFO, tracePoint, obj1, obj2, obj3);
  }


  public void data(int tracePoint, Object data) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, data);
  }
  public void data(int tracePoint, long data) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, new Long(data));
  }
  public void data(int tracePoint, boolean state) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, Boolean.valueOf(state));
  }
  public void data(int tracePoint, String str, long data) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, str, new Long(data));
  }
  public void data(int tracePoint, String str, boolean data) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, str, Boolean.valueOf(data));
  }
  public void data(int tracePoint, String str, Object obj) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, str, obj);
  }
  public void data(int tracePoint, Object obj1, Object obj2) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, obj1, obj2);
  }
  public void data(int tracePoint, Object obj1, Object obj2, Object obj3) {
    if (this == dumpingTrace) return;
    tracePoint(DATA, tracePoint, obj1, obj2, obj3);
  }


  public void warning(int tracePoint, Object data) {
    if (this == dumpingTrace) return;
    tracePoint(WARNING, tracePoint, data);
  }


  public void exception(Class c, int tracePoint, Throwable t) {
    if (this == dumpingTrace) return;

    // attempt to pop skipped exit points due to thrown exception
    Class currentClass = (Class) stack.peek();
    String methodName = (String) stack2.peek();
    while (!c.equals(currentClass)) {
      addLine("trace: poping unexpected class: call with (Class="+Misc.getClassNameWithoutPackage(c)+", tracepoint="+tracePoint+") but expected (Class=" + Misc.getClassNameWithoutPackage(currentClass) + ", method name="+methodName+") encountered by thread="+Thread.currentThread());
      if (stack.size()<=1)
        break;
      stack.pop();
      stack2.pop();
      currentClass = (Class) stack.peek();
      methodName = (String) stack2.peek();
    }
    synchronized (staticMonitor) {
      error(c, tracePoint, t);
      if (debugLevelBelow(EXCEPTION_STACK_DUMP)) return;
      addLine(t);
    }
  }


  public void error(Class c, int tracePoint, Object obj) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      if (debugLevelBelow(ERROR)) return;
      addLine("ERROR in " + Misc.getClassNameWithoutPackage(c) + " method " + stack2.peek() + " " + tracePoint + ": " + objToStr(obj));
    }
  }
  public void error(int tracePoint, Object obj) {
    if (this == dumpingTrace) return;
    synchronized (staticMonitor) {
      if (debugLevelBelow(ERROR)) return;
      addLine("ERROR in method " + stack2.peek() + " " + tracePoint + ": " + objToStr(obj));
    }
  }

  private static String objToStr(Object obj) {
    String s = "Unknown";
    try {
      s = Misc.objToStr(obj);
    } catch (Throwable t) {
    }
    return s;
  }

  public void args(Object obj) {
    if (this == dumpingTrace) return;
    addLine(getHeading(ARGS) + " " + objToStr(obj));
  }
  public void args(long longArg) {
    if (this == dumpingTrace) return;
    addLine(getHeading(ARGS) + " " + longArg);
  }
  public void args(float floatArg) {
    if (this == dumpingTrace) return;
    addLine(getHeading(ARGS) + " " + floatArg);
  }
  public void args(boolean state) {
    if (this == dumpingTrace) return;
    addLine(getHeading(ARGS) + " " + state);
  }
  public void args(Object obj1, Object obj2) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2);
  }
  public void args(Object obj1, Object obj2, Object obj3) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2, obj3);
  }
  public void args(Object obj1, Object obj2, Object obj3, Object obj4) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2, obj3, obj4);
  }
  public void args(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2, obj3, obj4, obj5);
  }
  public void args(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2, obj3, obj4, obj5, obj6);
  }
  public void args(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7) {
    if (this == dumpingTrace) return;
    args(obj1);
    args(obj2, obj3, obj4, obj5, obj6, obj7);
  }

  /** Clears all resources associated with the calling Thread's trace object
   *  Every thread that used tracing facility should call this before dying.
   */
  public void clear() {
    if (this == dumpingTrace) return;
    try {
      synchronized (staticMonitor) {
        Thread thisThread = Thread.currentThread();
        String threadName = thisThread.getName();
        /*
        if (threadName.startsWith("AWT-EventQueue")) {
          try {
            System.out.println("A bad example of clearing the trace stack... here is where its at: ");
            throw new IllegalArgumentException(threadName + " Thread is clearing the trace stack, print stack:");
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
         */
        addLine("<-- Trace Cleared for thread " + threadName);
        if (lastThread == thisThread)
          lastThread = null;
        Trace trace = (Trace) hashTable.remove( new Integer(thisThread.hashCode()) );
        trace.stack.clear(); trace.stack = null;
        trace.stack2.clear(); trace.stack2 = null;
      }
    } catch (Throwable t) {
    }
  }

  private PrintWriter getExceptionStackOutput() {
    return out;
  }

  /** Adds a line of text to output at current stack level.
   * Prints String or Throwable.
   */
  private void addLine(Object obj) {
    if (!isPaused()) {
      String str = null;
      Throwable t = null;
      if (obj instanceof String)
        str = (String) obj;
      else if (obj instanceof Throwable)
        t = (Throwable) obj;

      synchronized (staticMonitor) {
        StringBuffer strBuffer = new StringBuffer();
        String strDate = new SimpleDateFormat("hh:mm:ss.SSS ").format(new Date());
        strBuffer.append(strDate);

        if (stack.size() < headings.length)
          strBuffer.append(headings[stack.size()]);
        else {
          for (int i=0; i<stack.size(); i++) {
            strBuffer.append("| ");
          }
        }

        Thread thisThread = Thread.currentThread();
        if (thisThread != lastThread) {
          output("");
          output("Thread -- " + thisThread.getName() + " ID=" + thisThread.hashCode() + " obj=" + thisThread);
          output("");
        }
        if (str != null) {
          strBuffer.append(str);
          output(strBuffer.toString());
        }
        else if (t != null) {
          output(Misc.getStack(t));
        }

        lastThread = thisThread;
      } // end synchronized
    }
  }

  private void output(String str) {
    try {
      synchronized (staticMonitor) {
        if (maxBufferedSize > 0) {
          bufferedSize += str.length()*2+2; // characters are double byte plus new-line-character
          if (bufferedSize > maxBufferedSize) {
            out.flush();
            bufferedSize = str.length()*2+2;
          }
        }
        //Misc.toReadableOutput(str, out, null);
        out.println(str);
        if (flushEveryTime)
          out.flush();
        // check if we need to get new output file
        if (maxOutputFileSize > 0 && cOut != null && cOut.getByteCount() > maxOutputFileSize) {
          out.flush();
          out.close();
          out = getNewOutput();
        }
      }
    } catch (Throwable t) {
    }
  }

  private boolean debugLevelBelow(int debugLevel) {
    if (TraceProperties.getLevel( (Class) stack.peek() ) < debugLevel)
      return true;
    return false;
  }

  private static Hashtable nonDeamonThreadsHT = new Hashtable();
  private static void printOutNonDeamonThreads() {
    try {
      String name = Thread.currentThread().getName();
      if (!name.startsWith("main") && !name.startsWith("AWT")) {
        if (!Thread.currentThread().isDaemon() && !nonDeamonThreadsHT.contains(name)) {
          System.out.println(name);
          nonDeamonThreadsHT.put(name, name);
        }
      }
    } catch (Throwable t) {
    }
  }

} // end class