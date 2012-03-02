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
    String fileType = null;
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
    str = str.trim();
    String fileStr = getFileSafeString(str);
    int maxCharacters = 200;
    // leave room for encryption headers..
    if (fileStr.length() > maxCharacters)
      fileStr = fileStr.substring(0, maxCharacters);
    return fileStr;
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

}