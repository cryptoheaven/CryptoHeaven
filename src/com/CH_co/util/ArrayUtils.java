/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import java.util.*; 
import java.lang.reflect.Array;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class ArrayUtils extends Object {

  /** @return true if an Array contain duplicate entries based of their equals comparison. */
  public static boolean anyDuplicates(Object[] elements) {
    HashSet hs = new HashSet();
    boolean anyDups = false;
    for (int i=0; i<elements.length; i++) {
      Object o = elements[i];
      if (!hs.contains(o))
        hs.add(o);
      else {
        anyDups = true;
        break;
      }
    }
    hs.clear();
    return anyDups;
  } // end anyDuplicates


  /**
   * Break off the string into multiple chunks inserting 'insertion' in between break points.
   */
  public static String spreadString(String str, int charsPerInsertion, char insertion) {
    if (str.length() > charsPerInsertion) {
      int index = 0;
      int len = str.length();
      StringBuffer sb = new StringBuffer();
      while (len-index > charsPerInsertion) {
        sb.append(str.substring(index, index+charsPerInsertion)).append(insertion);
        index += charsPerInsertion;
      }
      sb.append(str.substring(index));
      str = sb.toString();
    }
    return str;
  }
  public static String breakLines(String str, int charsPerLine) {
    return spreadString(str, charsPerLine, '\n');
  }


  /** data for hexadecimal visualisation. */
  private static final char[] HEX_DIGITS = {
    '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
  };
  /**
  * Returns a string of hexadecimal digits from a byte array. Each
  * byte is converted to 2 hex symbols.
  */
  public static String toString(byte[] ba) {
    return toString(ba, 0, ba.length);
  }
  public static String toString (byte[] ba, int offset, int length) {
    char[] buf = new char[length * 2];
    for (int i = offset, j = 0, k; i < offset+length; ) {
      k = ba[i++];
      buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
      buf[j++] = HEX_DIGITS[ k        & 0x0F];
    }
    return new String(buf);
  }

  /**
   * Returns a string of hexadecimal digits from an integer array. Each
   * int is converted to 8 hex symbols.
   */
  public static String toString(int[] ia) {
    int length = ia.length;
    char[] buf = new char[length * 8];
    for (int i = 0, j = 0, k; i < length; i++) {
      k = ia[i];
      buf[j++] = HEX_DIGITS[(k >>> 28) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>> 24) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>> 20) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>> 16) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>> 12) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>>  8) & 0x0F];
      buf[j++] = HEX_DIGITS[(k >>>  4) & 0x0F];
      buf[j++] = HEX_DIGITS[ k         & 0x0F];
    }
    return new String(buf);
  }

  /**
   * Returns a string of hexadecimal digits from a character. Each
   * char is converted to 4 hex symbols.
   */
  public static String toString(char c) {
    char[] buf = new char[4];
    buf[0] = HEX_DIGITS[(c >>> 12) & 0x0F];
    buf[1] = HEX_DIGITS[(c >>>  8) & 0x0F];
    buf[2] = HEX_DIGITS[(c >>>  4) & 0x0F];
    buf[3] = HEX_DIGITS[ c         & 0x0F];
    return new String(buf);
  }

  /**
   * Returns a byte array from a String containing hex encoding bytes.
   */
  public static byte[] toByteArray(String hex) {
    byte[] bytes = null;
    if (hex != null) {
      bytes = new byte[hex.length()/2];
      for (int i=0; i<hex.length(); i+=2) {
        char ch1 = hex.charAt(i);
        char ch2 = hex.charAt(i+1);
        bytes[i/2] = (byte) hexCharsToByte(ch1, ch2);
      }
    }
    return bytes;
  }
  public static int hexCharsToByte(char hexDigit1, char hexDigit2) {
    int b1 = 0; 
    int b2 = 0;
    char ch1 = hexDigit1;
    char ch2 = hexDigit2;
    if (ch1 >= HEX_DIGITS[0] && ch1 <= HEX_DIGITS[9]) {
      b1 = (int) (ch1-HEX_DIGITS[0]);
    }
    else {
      b1 = (int) (ch1-HEX_DIGITS[10] + 10);
    }
    if (ch2 >= HEX_DIGITS[0] && ch2 <= HEX_DIGITS[9]) {
      b2 = (int) (ch2-HEX_DIGITS[0]);
    }
    else {
      b2 = (int) (ch2-HEX_DIGITS[10] + 10);
    }
    return (int) ((b1 << 4) | b2);
  }

  /**
   * Compares two byte arrays for equality.
   *
   * @return true if the arrays have identical contents
   */
  private static boolean areEqual(byte[] a, byte[] b) {
    int aLength = a.length;
    if (aLength != b.length)
      return false;
    for (int i = 0; i < aLength; i++)
      if (a[i] != b[i])
        return false;
    return true;
  }

  /**
   * Returns an array of 2 hexadecimal digits (most significant
   * digit first) corresponding to the lowest 8 bits of <i>n</i>.
   */
  public static char[] byteToHexChars(int n) {
    char[] buf = {
      HEX_DIGITS[(n >>> 4) & 0x0F],
      HEX_DIGITS[ n        & 0x0F]
    };
    return buf;
  }

  /**
   * Returns a string of 8 hexadecimal digits (most significant
   * digit first) corresponding to the integer <i>n</i>, which is
   * treated as unsigned.
   */
  private static char[] intToHexChars(int n) {
    char[] buf = new char[8];
    for (int i = 7; i >= 0; i--) {
      buf[i] = HEX_DIGITS[n & 0x0F];
      n >>>= 4;
    }
    return buf;
  }




  /** @return basic information about an array of bytes */
  public static String info(byte[] ba) {
    return "bin(" + (ba==null ? "null" : String.valueOf(ba.length))  + ")";
  }


  /** @return a new array of bytes of length=wantedLength or source if the 
   *  wantedLength is the length of the source array. 
   */
  public static byte[] fixLength(byte[] source, int wantedLength) {
    if (source.length == wantedLength) 
      return source;

    int availDataLen = source.length < wantedLength ? source.length : wantedLength;

    byte[] result = new byte[wantedLength];
    System.arraycopy(source, 0, result, 0, availDataLen);

    // fill with 0's
    for (int i=availDataLen; i<wantedLength; i++) {
      result[i] = 0;
    }
    return result;
  }

  /**
   * @return a new array of Objects with two merged arrays and duplicates eliminated.
   * The resulting array is of the same runtime instance as elements in the source arrays.
   * The resulting array does not contain any duplicates when compared using the <code> equals </code> method.
   * Order is always preserved.
   */
  public static Object[] mergeWithoutDuplicates (Object[] a1, Object[] a2) {
    if (a1 != null && a2 != null && !a1.getClass().equals(a2.getClass())) 
      throw new IllegalArgumentException("Runtime instances of arrays do not match!");

    HashSet hs = new HashSet();
    Vector objsV = new Vector();

    if (a1 != null) {
      for (int i=0; i < a1.length; i++) {
        Object o = a1[i];
        if (!hs.contains(o)) {
          hs.add(o);
          objsV.addElement(o);
        }
      }
    }
    if (a2 != null) {
      for (int i=0; i < a2.length; i++) {
        Object o = a2[i];
        if (!hs.contains(o)) {
          hs.add(o);
          objsV.addElement(o);
        }
      }
    }

    if (objsV.size() == 0) {
      return null;
    }

    Object[] array = toArray(objsV, hs.iterator().next().getClass());

    return array;
  }


  /**
   * Concatinates arrays and returns an Object[] of the same runtime instance
   * as the source objects.
   */
  public static Object[] concatinate(Object[] a1, Object[] a2) {
    if (a1 == null)
      return a2;
    if (a2 == null)
      return a1;
    return concatinate(a1, a2, a1.getClass().getComponentType());
  }
  /**
   * Concatinates arrays and returns an Object[] and returns a new array for component type specified.
   */
  public static Object[] concatinate(Object[] a1, Object[] a2, Class type) {
    LinkedList lList = new LinkedList();
    if (a1 != null && a1.length > 0) {
      List list1 = Arrays.asList(a1);
      if (list1.size() > 0)
        lList.addAll(list1);
    }
    if (a2 != null && a2.length > 0) {
      List list2 = Arrays.asList(a2);
      if (list2.size() > 0)
        lList.addAll(list2);
    }
    Object[] array = toArray(lList, type);
    return array;
  }


  /**
   * Divides an array of Objects into series of smaller arrays.
   */
  public static Object[][] divideIntoChunks(Object[] objs, int chunkSize) {
    Object[][] chunkObjs = null;
    if (objs == null || objs.length == 0) {
      chunkObjs = new Object[0][];
    } else {
      int chunks = ((objs.length-1) / chunkSize) + 1;
      chunkObjs = new Object[chunks][];
      int count = 0;
      int batchCount = 0;
      while (count < objs.length) {
        int bunch = Math.min(chunkSize, objs.length-count);
        Object[] bunchObjs = new Object[bunch];
        for (int i=0; i<bunch; i++) {
          bunchObjs[i] = objs[count+i];
        }
        chunkObjs[batchCount] = bunchObjs;
        batchCount ++;
        count += bunch;
      }
    }
    return chunkObjs;
  }


  /**
   * Finds an object 'obj' in an array of objects.  n^2 performance, but quick for small arrays.
   * Compares objects using the equals() method.
   * @return an index to the found object or -1 if object is not found.
   */
  public static int find(Object[] array, Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ArrayUtils.class, "find(Object[], Object)");
    if (trace != null) trace.args(array);
    if (trace != null) trace.args(obj);
    int index = -1;
    for (int i=0; array!= null && i<array.length; i++) {
      if (obj.equals(array[i])) {
        index = i;
        break;
      }
    }
    if (trace != null) trace.exit(ArrayUtils.class, index);
    return index;
  }


  /* @return index of a found element if <code> array </code> contains <code> element </code> */
  public static int find(int[] array, int element) {
    int index = -1;
    for (int i=0; array!=null && i<array.length; i++) {
      if (element == array[i]) {
        index = i;
        break;
      }
    }
    return index;
  }

  /* @return index of a found element if <code> array </code> contains <code> element </code> */
  public static int find(short[] array, short element) {
    int index = -1;
    for (int i=0; array!=null && i<array.length; i++) {
      if (element == array[i]) {
        index = i;
        break;
      }
    }
    return index;
  }


  /**
   * @return the difference between specified arrays.
   * All runtime instances of the source and subtract arrays MUST be the same,
   * or the returned array may have a wrong instance type.
   * The runtime instance of the array is to hold object types specified by the
   * source array type.
   */
  public static Object[] getDifference(Object[] source, Object[] subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ArrayUtils.class, "getDifference(Object[] source, Object[] subtract)");
    if (trace != null) trace.args(source, subtract);

    Object[] objs = null;
    if (source != null) {
      if (source.length == 0) {
        objs = source;
      } else {
        // if there is a record that changed parents we should remove it
        List original = Arrays.asList(source);
        ArrayList aOriginal = new ArrayList(original);
        if (subtract != null && subtract.length > 0) {
          List subtractL = Arrays.asList(subtract);
          aOriginal.removeAll(subtractL);
        }
        objs = toArray(aOriginal, source.getClass().getComponentType());
      }
    }

    if (trace != null) trace.exit(ArrayUtils.class, objs);
    return objs;
  }

  /**
   * @return the difference between specified arrays.
   * The return instance is of runtime type Object[]
   */
  public static Object[] getDifference2(Object[] source, Object[] subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ArrayUtils.class, "getDifference2(Object[] source, Object[] subtract)");
    if (trace != null) trace.args(source, subtract);

    Object[] objs = null;
    if (source != null) {
      if (source.length == 0) {
        objs = source;
      } else {
        // if there is a record that changed parents we should remove it
        List original = Arrays.asList(source);
        ArrayList aOriginal = new ArrayList(original);
        if (subtract != null && subtract.length > 0) {
          List subtractL = Arrays.asList(subtract);
          aOriginal.removeAll(subtractL);
        }
        objs = toArray(aOriginal, Object.class);
      }
    }

    if (trace != null) trace.exit(ArrayUtils.class, objs);
    return objs;
  }

  /**
   * @return a new array of the same runtime instance without duplicate objects
   * or the same array if no duplicates can be found.
   */
  public static Object[] removeDuplicates(Object[] source) {
    if (source == null)
      return source;
    return removeDuplicates(source, source.getClass().getComponentType());
  }
  /**
   * @return a new array of the specified runtime instance without duplicate objects
   * or the same array if no duplicates can be found.  Order is always preserved.
   */
  public static Object[] removeDuplicates(Object[] source, Class type) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ArrayUtils.class, "removeDuplicates(Object[] source, Class type)");
    if (trace != null) trace.args(source, type);

    Object[] objs = source;
    if (source != null && source.length > 0 && anyDuplicates(source)) {
      // remove duplicates from source[]
      HashSet hs = new HashSet();
      // vector used to preserve the order 
      Vector objsV = new Vector();
      for (int i=0; i<source.length; i++) {
        Object o = source[i];
        if (!hs.contains(o)) {
          hs.add(source[i]);
          objsV.addElement(o);
        }
      }

      objs = toArray(objsV, type);
    }

    if (trace != null) trace.exit(ArrayUtils.class, objs);
    return objs;
  }

  /**
   * Removes all instances that 'equals()' the 'toRemove' object.
   * @param source
   * @param toRemove
   * @return New instance of source array if anything was removed, the same instance otherwise.
   */
  public static Object[] removeElements(Object[] source, Object toRemove) {
    Object[] result = null;
    boolean anyEquals = false;
    for (int i=0; i<source.length; i++) {
      if (source[i].equals(toRemove)) {
        anyEquals = true;
        break;
      }
    }
    if (!anyEquals) {
      result = source;
    } else {
      Vector resultV = new Vector();
      for (int i=0; i<source.length; i++)
        if (!source[i].equals(toRemove))
          resultV.addElement(source[i]);
      result = toArray(resultV, source.getClass().getComponentType());
    }
    return result;
  }
  
  /**
   * Removes only the leading elements of the source array that 'equals()' the 'toRemove' object.
   * @param source
   * @param toRemove
   * @return New instance of source array if anything was removed, the same instance otherwise.
   */
  public static Object[] removeLeadingElements(Object[] source, Object toRemove) {
    Object[] result = null;
    if (source.length == 0 || !source[0].equals(toRemove)) {
      result = source;
    } else {
      Vector resultV = new Vector();
      boolean processingLeading = true;
      for (int i=0; i<source.length; i++) {
        Object o = source[i];
        if (processingLeading && o.equals(toRemove)) {
          // skip
        } else {
          // copy element
          processingLeading = false;
          resultV.addElement(o);
        }
      }
      result = toArray(resultV, source.getClass().getComponentType());
    }
    return result;
  }
  
  /**
   * @return an array of objects from the source array that match the type specified.
   * Object classes are compared, instances subclassing specified type are considered different.
   */
  public static Object[] gatherAllOfType(Object[] source, Class type) {
    Vector selectedV = new Vector();
    if (source != null) {
      for (int i=0; i<source.length; i++) {
        if (source[i].getClass().equals(type))
          selectedV.addElement(source[i]);
      }
    }
    Object[] objs = toArray(selectedV, type);
    return objs;
  }

  /**
   * @return an array of objects from the source array that match the runtime instance specified.
   * Object class trees are compared, instances subclassing specified type are considered equal.
   */
  public static Object[] gatherAllOfInstance(Object[] source, Class type) {
    Vector selectedV = new Vector();
    if (source != null) {
      for (int i=0; i<source.length; i++) {
        if (source[i].getClass().equals(type))
          selectedV.addElement(source[i]);
        else {
          // check the inheritance tree
          Class sourceClass = source[i].getClass();
          while (true) {
            // quit at root OBJECT -- it has no superclass
            if (sourceClass.equals(Object.class))
              break;
            Class superClass = sourceClass.getSuperclass();
            if (superClass.equals(type)) {
              selectedV.addElement(source[i]);
              break;
            }
            sourceClass = superClass;
          } // end while (true)
        } // end else check tree
      } // end for
    }
    Object[] objs = toArray(selectedV, type);
    return objs;
  }

  /**
   * Replaces strings with substitute strings. The set is processed in sequence for result of set[0] may affect set[1].
   * This method is performance optimized.
   */
  public static String replaceKeyWords(String str, String[][] sets) {
    int numOfSets = sets.length;
    char[] chars = null;
    for (int i=0; str!=null && i<numOfSets; i++) {
      String[] set = sets[i];
      int start = 0;
      int oldStart = 0;
      StringBuffer resultB = new StringBuffer(str.length());
      boolean anyFound = false;
      while ((start = str.indexOf(set[0], oldStart)) >= 0) {
        anyFound = true;
        if (start > oldStart) {
          int len = start-oldStart;
          if (chars == null || chars.length < len) chars = new char[len];
          str.getChars(oldStart, start, chars, 0);
          resultB.append(chars, 0, len);
        }
        resultB.append(set[1]);
        start += set[0].length();
        oldStart = start;
      }
      if (anyFound) {
        if (oldStart < str.length()) {
          int end = str.length();
          int len = end-oldStart;
          if (chars == null || chars.length < len) chars = new char[len];
          str.getChars(oldStart, end, chars, 0);
          resultB.append(chars, 0, len);
        }
        str = resultB.toString();
      }
    }
    return str;
  }

  /**
   * Replaces tags, using startTags and endTags specified, replace them with replacementTags.
   * To match startTags only, leave endTag "" empty string
   * startTags and endTags allow for multiple sets to accomodate upper and lower case variations
   * For example: the following arguments will remove <HEAD> html tag and change "Hello" to "Hi"
    String str = "<html><head>Header</HEAD><body>Hello there</body></html>";
    String[][] startTags = new String[][] {
        { "<head", "<HEAD" },
        { "Hello" },
      };
      String[][] endTags = new String[][] {
        { "</head>", "</HEAD>" },
        { "" },
      };
      String[] replacementTags = new String[] {
        "",
        "Hi",
      };
   */
  public static String removeTags(String str, String[][] startTags, String[][] endTags, String[] replacementTags) {
    char[] chars = null;
    for (int x=0; x<startTags.length; x++) {
      String[] startTag = startTags[x];
      String[] endTag = endTags[x];
      String replacementTag = replacementTags != null ? replacementTags[x] : null;
      int iStart = 0;
      int iEnd = 0;
      int oldStart = 0;
      StringBuffer resultB = new StringBuffer(str.length());
      boolean anyFound = false;
      while (true) {
        boolean anyFoundStart = false;
        boolean anyFoundEnd = false;
        int startTagIndex = 0;
        int endTagIndex = 0;
        for (int i=0; i<startTag.length; i++) {
          int start = str.indexOf(startTag[i], oldStart);
          if (start > -1 && (!anyFoundStart || start < iStart)) {
            anyFoundStart = true;
            startTagIndex = i;
            iStart = start;
          }
        }
        if (anyFoundStart) {
          for (int i=0; i<endTag.length; i++) {
            String tag = endTag[i];
            int fromIndex = iStart+startTag[startTagIndex].length();
            int end = -1;
            if (tag == null || tag.length() == 0)
              end = fromIndex;
            else
              end = str.indexOf(tag, fromIndex);
            if (end > -1 && (!anyFoundEnd || end < iEnd)) {
              anyFoundEnd = true;
              endTagIndex = i;
              iEnd = end;
            }
          }
        }
        if (anyFoundStart && anyFoundEnd) {
          anyFound = true;
          if (iStart > oldStart) {
            int len = iStart-oldStart;
            if (chars == null || chars.length < len) chars = new char[len];
            str.getChars(oldStart, iStart, chars, 0);
            resultB.append(chars, 0, len);
          }
          if (replacementTag != null)
            resultB.append(replacementTag);
          oldStart = iEnd + endTag[endTagIndex].length();
        } else {
          break;
        }
      }
      if (anyFound) {
        if (oldStart < str.length()) {
          int end = str.length();
          int len = end-oldStart;
          if (chars == null || chars.length < len) chars = new char[len];
          str.getChars(oldStart, end, chars, 0);
          resultB.append(chars, 0, len);
        }
        str = resultB.toString();
      }
    }
    return str;
  }

  /**
   * @return new instance of array of objects with runtime instance type of 'componentType'
   */
  public static Object[] toArray(Collection objsCollection, Class componentType) {
    Object[] objs = null;
    if (objsCollection != null) {
      int size = objsCollection.size();
      objs = (Object[]) Array.newInstance(componentType, size);
      if (size > 0)
        objsCollection.toArray(objs);
    }
    return objs;
  }

  /**
   * @return new instance of array of objects with runtime instance type of 'componentType'
   */
  public static Object[] toArrayType(Object[] objsArrayToConvert, Class componentType) {
    Object[] objs = null;
    if (objsArrayToConvert != null) {
      int length = objsArrayToConvert.length;
      objs = (Object[]) Array.newInstance(componentType, length);
      if (length > 0)
        System.arraycopy(objsArrayToConvert, 0, objs, 0, length);
    }
    return objs;
  }

}