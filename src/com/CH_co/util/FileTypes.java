/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.util;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
*
* @author  Marcin Kurzawa
* @version
*/
public class FileTypes {

  private static FileTypeI implFileType;

  public static void setFileTypeImpl(Class fileTypeI) throws InstantiationException, IllegalAccessException {
    implFileType = (FileTypeI) fileTypeI.newInstance();
  }

  public static String getFileType(String fileName) {
    if (implFileType != null) {
      return implFileType.getFileType(fileName);
    } else {
      return getFileInternalType(fileName);
    }
  }

  public static String getFileInternalType(String fileName) {
    String fileType;
    int index = fileName.lastIndexOf('.');

    /* if no extension or extension is one letter only */
    if (index == -1 || index+1 == fileName.length()) {
      fileType = "File";
    } else {
      String ext = fileName.substring(index + 1).toUpperCase();
      fileType = ext + " File";
    }
    return fileType;
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

  public static String getFileSafeShortString(String str) {
    return getFileSafeShortString(str, false);
  }
  public static String getFileSafeShortString(String str, boolean useLegacyCompatibilityMode) {
    str = str.trim();
    String fileStr = getFileSafeString(str, useLegacyCompatibilityMode);
    int maxByteChars = 200;
    if (Misc.convStrToBytes(fileStr).length > maxByteChars) {
      // leave room for encryption headers..
      fileStr = makeShortFileName(fileStr, maxByteChars);
    }
    return fileStr;
  }

  /**
  * Used by server to create email file attachment filenames so don't change this...
  * @param str to normalize
  * @return normalized str
  */
  private static String getFileSafeString(String str, boolean useLegacyCompatibilityMode) {
    StringBuffer sb = new StringBuffer(str.length());
    int len = str.length();
    for (int i=0; i<len; i++) {
      char ch = str.charAt(i);
      char next = i+1 < len ? str.charAt(i+1) : '.';
      if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '(' || ch == ')' || ch == '[' || ch == ']') {
        // filenames should not have '..' patters
        if (useLegacyCompatibilityMode || sb.length() == 0)
          sb.append(ch);
        else {
          if (ch == '.' && sb.charAt(sb.length()-1) == '.') {
            // skip '..'
          } else if (ch == '.' && next == '.') {
            // avoid finishing with a '.'
          } else {
            sb.append(ch);
          }
        }
      } else {
        // lets not add more than one '-' in sequence
        if (useLegacyCompatibilityMode || sb.length() == 0 || sb.charAt(sb.length()-1) != '-')
          sb.append('-');
      }
    }
    return sb.toString();
  }

  private static String makeShortFileName(String fileName, int originalMaxByteChars) {
    int decrementMax = originalMaxByteChars / 3; // max decrement 33%
    int decrementMin = 1; // min decrement 1 character
    int maxByteChars = originalMaxByteChars;
    int maxExt = originalMaxByteChars;
    int round = 0;
    int currentByteLength;
    while ((currentByteLength = Misc.convStrToBytes(fileName).length) > originalMaxByteChars) {
      round ++;
      if (round > 1) {
        // Decrement depends on the amount over the limit, but don't decrement too sharply as it may not be a linear relationship...
        int decrement = (int) (((double) (currentByteLength - originalMaxByteChars)) / 1.2);
        decrement = Math.min(decrementMax, decrement);
        decrement = Math.max(decrementMin, decrement);
        maxByteChars = maxByteChars - decrement;
        maxExt = maxExt - decrement;
      }
      int iDot = fileName.lastIndexOf('.');
      String ext = "";
      if (iDot >= 0) {
        ext = fileName.substring(iDot);
        fileName = fileName.substring(0, iDot);
      }
      if (ext.length() >= maxExt) {
        // Allow single character from beginning of 'fileName' if extension would take the 
        // entire string making it start with a dot '.' and invisible in unix/MacOSX
        int effectiveMaxExt = fileName.length() > 0 && fileName.charAt(0) != '.' ? maxExt - 1 : maxExt;
        ext = ext.substring(0, effectiveMaxExt);
      }
      if (fileName.length()+ext.length() > maxByteChars)
        fileName = fileName.substring(0, maxByteChars-ext.length());
      fileName = fileName + ext;
    }
    return fileName;
  }

}