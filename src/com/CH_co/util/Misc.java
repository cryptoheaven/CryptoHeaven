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

package com.CH_co.util;

import com.CH_co.unicode.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class Misc extends Object {

  private static DisposableObj systemExitObj;

  public static void setSystemExitObj(DisposableObj exitObj) {
    systemExitObj = exitObj;
  }
  public static void systemExit(int code) {
    // cleanup files before exiting...
    GlobalProperties.cleanupTempFiles();
    GlobalProperties.cleanupTempFilesOnFinalize();
    // exit
    if (systemExitObj != null) {
      systemExitObj.disposeObj();
      // interrupt running deamon threads
      CleanupAgent.stopSingleInstance();
    } else {
      System.exit(code);
    }
  }
  public static boolean isRunningFromApplet() {
    return systemExitObj != null && systemExitObj instanceof javax.swing.JApplet;
  }


  /**
   * @return Class name from the full name, ex: 'com.CH_co.util.Misc' would be trimmed to 'Misc' only
   */
  public static String getClassNameWithoutPackage(String fullName) {
    int lastIndex = fullName.lastIndexOf('.');
    if (lastIndex > 0)
      return fullName.substring(lastIndex+1);
    return fullName;
  }

  /**
   * @return Class name from a class, ex: 'com.CH_co.util.Misc' would be trimmed to 'Misc' only
   */
  public static String getClassNameWithoutPackage(Class c) {
    String fullName = c.getName();
    return getClassNameWithoutPackage(fullName);
  }

  /**
   * @return Package name from a class, ex: 'com.CH_co.util.Misc' would be trimmed to 'com.CH_co.util' only
   */
  public static String getPackageName(String fullName) {
    int lastIndex = fullName.lastIndexOf('.');
    if (lastIndex > 0)
      return fullName.substring(0, lastIndex);
    return fullName;
  }
  /**
   * @return Package name from a class, ex: 'com.CH_co.util.Misc' would be trimmed to 'com.CH_co.util' only
   */
  public static String getPackageName(Class c) {
    String fullName = c.getName();
    return getPackageName(fullName);
  }

  /**
   * Converts a throwable into a String output
   */
  public static String getStack(Throwable t) {
    String stack = "Unknown";
    try {
      CharArrayWriter charsOut = new CharArrayWriter();
      PrintWriter pw = new PrintWriter(charsOut);
      t.printStackTrace(pw);
      pw.flush();
      stack = charsOut.toString();
      pw.close();
    } catch (Throwable x) {
    }
    return stack;
  }

  /**
   * Tries to digest array objects and convert them element-by-element.
   * @return a representation of specified object as a string.
   */

  public static String objToStr(Object obj) {
    if (obj == null) return "null";
    StringBuffer strB = new StringBuffer();
    objToStr(obj, strB);
    return strB.toString();
  }

  /**
   * Tries to digest array objects and convert them element-by-element.
   * @return a representation of specified object as a string, appended to the StringBuffer;
   */
  public static void objToStr(Object obj, StringBuffer strB) {
    int MAX_ITEMS_TO_LIST = 50;
    if (obj == null) {
      strB.append("null");
    } else {
      Class c = obj.getClass();
      if (c.isArray()) {
        int len = Array.getLength(obj);
        if (obj instanceof byte[]) {
          strB.append("BYTES[len="); strB.append(len); strB.append("](");
          if (len > MAX_ITEMS_TO_LIST) {
            strB.append("too many to list");
          } else {
            strB.append(ArrayUtils.toString((byte[]) obj));
          }
          strB.append(')');
        } else {
          strB.append("ARRAY[len="); strB.append(len); strB.append("](");
          if (len > MAX_ITEMS_TO_LIST) {
            strB.append("too many to list");
          } else {
            for (int i=0; i<len; i++) {
              objToStr(Array.get(obj, i), strB);
              if (i+1 < len)
                strB.append(", ");
            }
          }
          strB.append(')');
        }
      } else if (obj instanceof Vector) {
        Vector v = (Vector) obj;
        int len = v.size();
        strB.append("Vector[len="); strB.append(len); strB.append("](");
        if (len > MAX_ITEMS_TO_LIST) {
          strB.append("too many to list");
        } else {
          for (int i=0; i<len; i++) {
            objToStr(v.elementAt(i), strB);
            if (i+1 < len)
              strB.append(", ");
          }
        }
        strB.append(')');
      } else if (obj instanceof ArrayList) {
        ArrayList al = (ArrayList) obj;
        int len = al.size();
        strB.append("ArrayList[len="); strB.append(len); strB.append("](");
        if (len > MAX_ITEMS_TO_LIST) {
          strB.append("too many to list");
        } else {
          for (int i=0; i<len; i++) {
            objToStr(al.get(i), strB);
            if (i+1 < len)
              strB.append(", ");
          }
        }
        strB.append(')');
      } else if (obj instanceof Map) {
        Map map = (Map) obj;
        Set keys = map.keySet();
        strB.append("Map[len=");
        if (keys == null) {
          strB.append("null]");
        } else {
          strB.append(keys.size()); strB.append("](");
          int len = map.size();
          if (len > MAX_ITEMS_TO_LIST) {
            strB.append("too many to list");
          } else {
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
              Object key = iter.next();
              strB.append(objToStr(key));
              strB.append('=');
              strB.append(objToStr(map.get(key)));
              if (iter.hasNext())
                strB.append(", ");
            }
          }
          strB.append(')');
        }
      } else if (obj instanceof Collection) {
        Collection coll = (Collection) obj;
        Object[] objs = coll.toArray();
        strB.append("Collection");
        objToStr(objs, strB);
        strB.append(')');
      } else if (obj instanceof Enumeration) {
        Enumeration enm = (Enumeration) obj;
        ArrayList al = new ArrayList();
        while (enm.hasMoreElements()) {
          al.add(enm.nextElement());
        }
        strB.append("Enumeration");
        objToStr(al, strB);
        strB.append(')');
      } else if (obj instanceof Iterator) {
        Iterator iter = (Iterator) obj;
        ArrayList al = new ArrayList();
        while (iter.hasNext()) {
          al.add(iter.next());
        }
        strB.append("Iterator");
        objToStr(al, strB);
        strB.append(')');
//      } else if (obj instanceof Map) {
//        Map map = (Map) obj;
//        Set keys = map.keySet();
//        int len = map.size();
//        strB.append("Map[len="); strB.append(len); strB.append("](");
//        Iterator iter = keys.iterator();
//        int i = 0;
//        while (iter.hasNext()) {
//          Object key = iter.next();
//          Object value = map.get(key);
//          objToStr(key, strB);
//          strB.append('=');
//          objToStr(value, strB);
//          if (i+1 < len)
//            strB.append(", ");
//          i ++;
//        }
//        strB.append(')');
      } else if (obj instanceof String) {
        String str = (String) obj;
        // replace all \n with \n
        strB.append("\"");
        for (int i=0; i<str.length(); i++) {
          char ch = str.charAt(i);
          if (ch == '\n')
            strB.append("\\n");
          else if (ch == '\r')
            strB.append("\\r");
          strB.append(ch);
        }
        strB.append("\"");
        //strB.append("\"" + obj + "\"");
      } else if (obj instanceof Number) {
        strB.append("#" + obj);
      } else if (obj instanceof Boolean || obj instanceof Character) {
        strB.append("'" + obj + "'");
      } else {
        strB.append(obj);
        //toReadableOutput(obj.toString(), null, strB);
      }
    }
  }

//  /**
//   * NEVER TRACE THIS METHOD AS IT IS USED IN TRACE CLASS AND IT WOULD CAUSE INFINITE LOOP!!!
//   * @param str String to output
//   * @param out Output channel
//   * @param sb Output channel
//   */
//  public static void toReadableOutput(String str, PrintWriter out, StringBuffer sb) {
//    for (int i=0; i<str.length(); i++) {
//      char ch = str.charAt(i);
//      if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
//              ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' ||
//              ch == '\'' || ch == '"' || ch == '|' || ch == '\\' || ch == '/' ||
//              ch == '?' || ch == '.' || ch == ',' || ch == ';' || ch == ':' ||
//              ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == ')' || ch == '(' ||
//              ch == '*' || ch == '&' || ch == '^' || ch == '#' || ch == '@' || ch == '!' ||
//              ch == '`' || ch == '~' || ch == '-' || ch == '_' || ch == '=' || ch == '+' ||
//              ch == '$' || ch == '%' || ch == '>' || ch == '<')
//      {
//        if (out != null) out.print(ch);
//        if (sb != null) sb.append(ch);
//      } else {
//        String hex = ""+ch;//ArrayUtils.toString(ch);
//        if (out != null) out.print(hex);
//        if (sb != null) sb.append(hex);
//      }
//    }
//  }



  private static final int KILO = 1024;
  private static String[] sizeUnits = new String[] { " bytes", " KB", " MB", " GB", " TB" };

  /** The number is less than 'maxNumOfDigits' long, otherwise
    * it is divided by 1000 and appropriate units added,
    * such as: bytes, KB, MB, GB
    */
  public static String getFormattedSize(Long byteSize, int maxNumOfDigits, int minNumOfDigits) {
    return getFormattedSize(byteSize.longValue(), maxNumOfDigits, minNumOfDigits);
  }
  public static String getFormattedSize(Integer byteSize, int maxNumOfDigits, int minNumOfDigits) {
    return getFormattedSize(byteSize.longValue(), maxNumOfDigits, minNumOfDigits);
  }
  public static String getFormattedSize(long byteSize, int maxNumOfDigits, int minNumOfDigits) {
    double longSize = byteSize;
    long divisor = (long) Math.pow(10, maxNumOfDigits);

    if (byteSize < 0)
      return "";

    String sizeString = "";

    for (int i=0; i<sizeUnits.length; i++) {

      if ( ((long) (longSize/divisor)) == 0) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        int numOfDigits = (int) ((Math.log(longSize) / Math.log(10)) + 1);
        int maxFracDigits = minNumOfDigits - numOfDigits;
        maxFracDigits = maxFracDigits >= 0 ? maxFracDigits : 0;
        nf.setMaximumFractionDigits(maxFracDigits);

        sizeString = nf.format(longSize) + sizeUnits[i];
        break;
      }
      else {
        longSize /= KILO;
      }
    }

    return sizeString;
  }


  /**
   * Format elapsed time.  Time must always be positive!
   * @param time is in seconds.
   */
  public static String getFormattedTime(long time) {
    // make sure 'time is positive or zero
    time = time > 0 ? time : 0;
    int seconds = (int) (time % 60);
    time /= 60;
    int minutes = (int) (time % 60);
    time /= 60;

    StringBuffer strBuffer = new StringBuffer(8);
    if (time > 0) {
      if (time < 10)
        strBuffer.append('0');
      strBuffer.append(time);
      strBuffer.append(":");
    }
    if (minutes < 10)
      strBuffer.append('0');
    strBuffer.append(minutes);
    strBuffer.append(":");
    if (seconds < 10)
      strBuffer.append('0');
    strBuffer.append(seconds);
    return strBuffer.toString();
  }

  /** if the date is less than a week, format is ex: Wed, 10:23:57 AM
    * if the date is more than a week but less than 90 days, format: Jan 22, 10:23 PM
    * if the date is more than 90 days, format is ex: Jan 22, 2000
    */
  public static String getFormattedDate(Date timestamp) {
    return getFormattedDate(timestamp, true);
  }
  public static String getFormattedDate(Date timestamp, boolean includeSeconds) {
    long givenTime = timestamp.getTime();
    Date currDate = new Date();
    long currTime = currDate.getTime();
    long diff = currTime - givenTime;
    diff = Math.abs(diff);
    DateFormat formatter;

    if (diff < hoursToMilliseconds(16)) {
      formatter = DateFormat.getTimeInstance(includeSeconds ? DateFormat.MEDIUM : DateFormat.SHORT);
    } else {
      formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    String dateString = formatter.format(timestamp);
    return dateString;
  }
  public static String getFormattedDateFileStr(Date timestamp) {
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    String s = formatter.format(timestamp);
    return s;
  }
  /**
   * Format a timestamp into more readable form, limit precision from nanoseconds to milliseconds.
   */
  public static String getFormattedTimestamp(Date timestamp) {
    String s = "";
    if (timestamp != null) {
      //DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
      DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
      s = formatter.format(timestamp);
      /* << skip the milliseconds
      int millis = timestamp.getNanos() / 1000000;
      if (millis >= 100) s += "." + millis;
      else if (millis >= 10) s += ".0" + millis;
      else s += ".00" + millis;
       */
    }
    return s;
  }
  /**
   * Format a timestamp into more readable form.
   */
  public static String getFormattedTimestamp(Date timestamp, int dateStyle, int timeStyle) {
    String s = "";
    if (timestamp != null) {
      DateFormat formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
      s = formatter.format(timestamp);
    }
    return s;
  }

  /** Convert days to milliseconds **/
  private static long daysToMilliseconds(int numDays) {
    return ((long) 1000) * 60 * 60 * 24 * numDays;
  }
  /** Convert days to milliseconds **/
  private static long hoursToMilliseconds(int numHours) {
    return ((long) 1000) * 60 * 60 * numHours;
  }


  public static byte[] compress(String text) throws IOException {
    byte[] bytes = Misc.convStrToBytes(text);
    return compress(bytes);
  }
  public static byte[] compress(byte[] bytes) throws IOException {
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    GZIPOutputStream gOut = new GZIPOutputStream(bOut);
    gOut.write(bytes);
    gOut.flush();
    gOut.finish();
    gOut.close();
    return bOut.toByteArray();
  }
  public static String decompressStr(byte[] compressedBytes) throws IOException {
    byte[] decompressedBytes = decompressBytes(compressedBytes);
    return Misc.convBytesToStr(decompressedBytes);
  }
  public static byte[] decompressBytes(byte[] compressedBytes) throws IOException {
    ByteArrayInputStream bIn = new ByteArrayInputStream(compressedBytes);
    GZIPInputStream gIn = new GZIPInputStream(bIn);
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    byte[] bb = new byte[1024];
    int read;
    while((read=gIn.read(bb, 0, bb.length)) >= 0) {
      if (read > 0)
        bOut.write(bb, 0, read);
    }
    bOut.flush();
    gIn.close();
    return bOut.toByteArray();
  }


  public static String encodePlainLineIntoHtmlLine(String plainSingleLine) {
    String encodedStr = null;
    String plain = plainSingleLine;
    if (plain != null) {
      StringBuffer sb = new StringBuffer();
      int len = plain.length();
      char prevCH = ' ';
      char currCH = ' ';
      for (int i=0; i<len; i++) {
        char ch = plain.charAt(i);
        currCH = ch;
        if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9'))
          sb.append(ch); // for performance, try most common case first
        else if (ch == '<')
          sb.append("&lt;");
        else if (ch == '>')
          sb.append("&gt;");
        else if (ch == '&')
          sb.append("&amp;");
        else if (ch == '\"')
          sb.append("&quot;");
        else if (ch == '\'')
          sb.append("&#39;");
//        else if (ch == '@')
//          sb.append("&#64;");
        else if (Character.isWhitespace(ch)) {
          if (ch == ' ') {
            if (prevCH == ' ') {
              sb.append("&nbsp;");
              currCH = '-'; // to make &nbsp alternate with spaces
            } else {
              sb.append(' ');
            }
          }
          else if (ch == '\t')
            sb.append("&nbsp; &nbsp; ");
        }
        else
          sb.append(ch);
        prevCH = currCH;
      }
      encodedStr = sb.toString();
    }
    return encodedStr;
  }

  public static String encodePlainIntoHtml(String plain) {
    String encodedStr = null;
    if (plain != null) {
      StringBuffer sb = new StringBuffer();
      // pre-process string to remove \n\r \r\n combos
      plain = ArrayUtils.replaceKeyWords(plain, new String[][] {
                  {"\n\r", "\n"},
                  {"\r\n", "\n"},
                  {"\r",   "\n"},
                });
      int len = plain.length();
      char prevCH = ' ';
      char currCH = ' ';
      for (int i=0; i<len; i++) {
        char ch = plain.charAt(i);
        currCH = ch;
        if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9'))
          sb.append(ch); // for performance, try most common case first
        else if (ch == '<')
          sb.append("&lt;");
        else if (ch == '>')
          sb.append("&gt;");
        else if (ch == '&')
          sb.append("&amp;");
        else if (ch == '\"')
          sb.append("&quot;");
        else if (ch == '\'')
          sb.append("&#39;");
//        else if (ch == '@')
//          sb.append("&#64;");
        else if (Character.isWhitespace(ch)) {
          if (ch == ' ') {
            if (prevCH == ' ') {
              sb.append("&nbsp;");
              currCH = '-'; // to make &nbsp alternate with spaces
            } else {
              sb.append(' ');
            }
          }
          else if (ch == '\t')
            sb.append("&nbsp; &nbsp; ");
          else if (ch == '\n')
            sb.append("<br>");
        }
        else
          sb.append(ch);
        prevCH = currCH;
      }
      encodedStr = sb.toString();
      // change all <br><br> into <p>
      do {
        int doubleBR = encodedStr.indexOf("<br><br>");
        if (doubleBR >= 0)
          encodedStr = encodedStr.substring(0, doubleBR) + "<p>" + encodedStr.substring(doubleBR + "<br><br>".length());
        else
          break;
      } while (true);
    }
    return encodedStr;
  }


  /**
   * Input string should be in format <hostname>[:<portNumber>] with port as an optional part.
   * If port is not present, it defaults to 80.
   * @return If format is ok, return Object[] with two elements, the server name and port number.
   * If not OK, return NULL
   */
  public static Object[] parseHostAndPort(String server) {
    boolean httpAdded = false;
    if (server.indexOf("://") < 0) {
      httpAdded = true;
      server = "http://" + server;
    }
    String host = null;
    Integer port = null;
    try {
      URL url = new URL(server);
      host = url.getHost();
      int p = url.getPort();
      if (p >= 0)
        port = new Integer(p);
      else
        port = new Integer(80);
    } catch (Throwable t) {
    }
    Object[] rc = null;
    if (host != null && port != null) {
      if (httpAdded) {
        rc = new Object[] { host, port };
      } else {
        rc = new Object[] { "http://"+host, port };
      }
    }
    return rc;
  }

  /**
   * Do cloning using serialization.
   * @return cloned instance of object
   */
  public static Object cloneSerializable(Object object) {
    Object rcObj = null;

    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
      objectOut.writeObject(object);
      objectOut.flush();

      byte[] bytes = byteOut.toByteArray();

      ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
      ObjectInputStream objectIn = new ObjectInputStream(byteIn);
      rcObj = objectIn.readObject();
    } catch (Throwable t) {
    }

    return rcObj;
  }

  public static String escapeWhiteEncode(String str) {
    String result = null;
    if (str != null) {
      StringBuffer sb = new StringBuffer();
      int len = str.length();
      for (int i=0; i<len; i++) {
        char ch = str.charAt(i);
        if (Character.isWhitespace(ch) || ch == '/') {
          sb.append('/');
          sb.append(ArrayUtils.byteToHexChars(ch));
        } else {
          sb.append(ch);
        }
      }
      result = sb.toString();
    }
    return result;
  }
  public static String escapeWhiteDecode(String str) {
    String result = null;
    if (str != null) {
      StringBuffer sb = new StringBuffer();
      int len = str.length();
      for (int i=0; i<len; i++) {
        char ch = str.charAt(i);
        if (ch == '/') {
          char ch1 = str.charAt(i+1);
          char ch2 = str.charAt(i+2);
          int b = ArrayUtils.hexCharsToByte(ch1, ch2);
          i += 2;
          sb.append((char) b);
        } else {
          sb.append(ch);
        }
      }
      result = sb.toString();
    }
    return result;
  }

  public  static String getDirSafeString(String str) {
    StringBuffer sb = new StringBuffer(str.length());
    int len = str.length();
    for (int i=0; i<len; i++) {
      char ch = str.charAt(i);
      if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == ',' || ch == '.' || ch == '(' || ch == ')' || ch == '[' || ch == ']')
        sb.append(ch);
      else
        sb.append('-');
    }
    String fileName = sb.toString();
    if (fileName.length() > 200) {
      int iDot = fileName.lastIndexOf('.');
      String ext = "";
      if (iDot >= 0) {
        ext = fileName.substring(iDot);
      }
      if (ext.length() > 190)
        ext = ext.substring(0, 190);
      fileName = fileName.substring(0, 195-ext.length());
      fileName += ext;
    }
    return fileName;
  }

  /**
   * Used by server to create email file attachment filenames so don't change this...
   * @param str to normalize
   * @return normalized str
   */
  private static String getFileSafeString(String str) {
    StringBuffer sb = new StringBuffer(str.length());
    int len = str.length();
    for (int i=0; i<len; i++) {
      char ch = str.charAt(i);
      if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '(' || ch == ')' || ch == '[' || ch == ']')
        sb.append(ch);
      else
        sb.append('-');
    }
    String fileName = sb.toString();
    if (fileName.length() > 200) {
      int iDot = fileName.lastIndexOf('.');
      String ext = "";
      if (iDot >= 0) {
        ext = fileName.substring(iDot);
      }
      if (ext.length() > 190)
        ext = ext.substring(0, 190);
      fileName = fileName.substring(0, 195-ext.length());
      fileName += ext;
    }
    return fileName;
  }

  public static String getFileSafeShortString(String str) {
    str = str.trim();
    String fileStr = getFileSafeString(str);
    int maxCharacters = 200;
    // leave room for encryption headers..
    if (fileStr.length() > maxCharacters)
      fileStr = fileStr.substring(0, maxCharacters);
    return fileStr;
  }

  private static final Compress compressorSCSU = new Compress();
  public static byte[] convStrToBytes(String doubleByteString) {
    return convCharsToBytes(doubleByteString != null ? doubleByteString.toCharArray() : null);
  }
  public static byte[] convCharsToBytes(char[] doubleByteChars) {
    byte[] bytes = null;
    if (doubleByteChars != null) {
      if (doubleByteChars.length == 0)
        bytes = new byte[0];
      else {
        synchronized (compressorSCSU) {
          try {
            compressorSCSU.resetAll();
            bytes = compressorSCSU.compress(doubleByteChars);
          } catch (IllegalInputException e1) {
          } catch (EndOfInputException e2) {
          }
        }
      }
    }
    return bytes;
  }
  public static String convStrToStrBytes(String doubleByteString) {
    byte[] bytes = convStrToBytes(doubleByteString);
    return bytes != null ? new String(bytes) : null;
  }

  private static final Expand expanderSCSU = new Expand();
  public static String convBytesToStr(byte[] bytes) {
    String doubleByteString = null;
    if (bytes != null) {
      if (bytes.length == 0)
        doubleByteString = "";
      else {
        synchronized (expanderSCSU) {
          try {
            expanderSCSU.resetAll();
            doubleByteString = expanderSCSU.expand(bytes);
          } catch (IllegalInputException e1) {
          } catch (EndOfInputException e2) {
          }
        }
      }
    }
    return doubleByteString;
  }
  public static char[] convBytesToChars(byte[] bytes) {
    String doubleByteStr = convBytesToStr(bytes);
    char[] doubleByteChars = null;
    if (doubleByteStr != null)
      doubleByteChars = doubleByteStr.toCharArray();
    return doubleByteChars;
  }
  public static String convStrBytesToStr(String singleByteString) {
    String doubleByteString = convBytesToStr(singleByteString != null ? singleByteString.getBytes() : null);
    return doubleByteString;
  }


  public static String[] parseDelimitedStr(String str, String delim) {
    String[] strings = null;
    if (str != null && str.length() > 0) {
      StringTokenizer st = new StringTokenizer(str, delim);
      ArrayList tokensL = new ArrayList();
      while (st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        if (token.length() > 0)
          tokensL.add(token);
      }
      strings = (String[]) ArrayUtils.toArray(tokensL, String.class);
    }
    return strings;
  }


  public static Date getNormalizedDay(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    return cal.getTime();
  }


  public static Number setBitObj(Number source, Number destination, long theBit) {
    return setBitObj(isBitSet(source, theBit), destination, theBit);
  }
  public static Number setBitObj(boolean turnOn, Number bits, long theBit) {
    long rc = setBit(turnOn, bits, theBit);
    if (bits instanceof Long)
      return new Long(rc);
    else if (bits instanceof Integer)
      return new Integer((int) rc);
    else if (bits instanceof Short)
      return new Short((short) rc);
    else
      throw new IllegalArgumentException("Argument 'bits' of type " + (bits == null ? "null" : bits.getClass().getName() ) + " is not supported.");
  }
  public static long setBit(boolean turnOn, Number bits, long theBit) {
    return setBit(turnOn, bits.longValue(), theBit);
  }
  public static long setBit(boolean turnOn, long bits, long theBit) {
    if (turnOn) {
      bits |= theBit;
    } else {
      bits &= ~theBit;
    }
    return bits;
  }
  public static boolean isBitSet(Number bits, long theBit) {
    return bits != null && (bits.longValue() & theBit) != 0;
  }
  public static String toBitStr(Number bits) {
    StringBuffer bitStr = new StringBuffer();
    int bitLen = 0;
    if (bits instanceof Long)
      bitLen = 64;
    else if (bits instanceof Integer)
      bitLen = 32;
    else if (bits instanceof Short)
      bitLen = 16;
    for (int i=bitLen; i>=0; i--) {
      bitStr.append(isBitSet(bits, 1L << i) ? "1":"0");
    }
    return bitStr.toString();
  }
}