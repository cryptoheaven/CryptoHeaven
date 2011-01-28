/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.util;

import com.CH_co.util.*;

import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FileTypesIcons implements FileTypeI {

  private static Hashtable cachedFileTypesHT = new Hashtable(); // keys are file extensions
  private static Hashtable cachedFileIconsHT = new Hashtable(); // keys are file extensions

  public String getFileType(String fileName) {
    return (String) getFileIconAndType(fileName)[1];
  }

  public static Icon getFileIcon(File file) {
    if (file.isDirectory()) {
      return Images.get(ImageNums.FLD_CLOSED16);
    } else {
      return (Icon) getFileIconAndType(file.getName())[0];
    }
  }

  public static Icon getFileIcon(String fileName) {
    return (Icon) getFileIconAndType(fileName)[0];
  }

  /**
   * @return array composed of file icon, file type
   */
  public static Object[] getFileIconAndType(String fileName) {
    Object[] info = new Object[2];
    String fileType = null;
    Icon icon = null;

    // get extension
    String ext = null;
    int index = fileName.lastIndexOf('.');
    if (index > 0) {
      ext = fileName.substring(index + 1).toUpperCase();
    }
    info[1] = ext;

    // get type and icon
    if (ext == null || ext.length() == 0) {
      fileType = FileTypes.getFileInternalType(fileName);
      icon = getFileInternalIconForType(fileType);
    } else {
      icon = (Icon) cachedFileIconsHT.get(ext);
      fileType = (String) cachedFileTypesHT.get(ext);
      if (icon == null || fileType == null) {
        Object[] iconAndType = getSystemFileIconAndType(ext);
        icon = (Icon) iconAndType[0];
        fileType = (String) iconAndType[1];
        // if file type not found on the system, use internal file type clasification
        if (fileType == null)
          fileType = FileTypes.getFileInternalType(fileName);
        if (fileType != null)
          cachedFileTypesHT.put(ext, fileType);
        // if icon not found on the system, use internal icons
        if (icon == null)
          icon = getFileInternalIconForType(FileTypes.getFileInternalType(fileName)); // make sure to use internal file type classification when getting internal icon
        if (icon != null)
          cachedFileIconsHT.put(ext, icon);
      }
    }

    info[0] = icon;
    info[1] = fileType;

    return info;
  }

  private static Icon getFileInternalIconForType(String fileType) {
    return Images.get(ImageNums.FILE_TYPE_OTHER);
  }

  private static Object[] getSystemFileIconAndType(String extension) {
    Icon icon = null;
    String type = null;
    File tmpFile = null;
    // add a '.' if extension does not start with one
    if (!extension.startsWith(".")) {
      extension = "." + extension;
    }
    // create tmp file
    try {
      //System.out.println("Fetching from system for extension " + extension);
      tmpFile = File.createTempFile("tempIconAndTypeExtract", extension);
      // create fileSystemView and get icon for .html files for current OS
      javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView.getFileSystemView();
      try {
        java.lang.reflect.Method m1 = fsv.getClass().getMethod("getSystemIcon", new Class[] { File.class });
        icon = (Icon) m1.invoke(fsv, new Object[] { tmpFile });
      } catch (Throwable t1) {
        //t1.printStackTrace();
      }
      try {
        java.lang.reflect.Method m2 = fsv.getClass().getMethod("getSystemTypeDescription", new Class[] { File.class });
        type = (String) m2.invoke(fsv, new Object[] { tmpFile });
        //System.out.println("system type description : " + tmpFile + " is " + type);
      } catch (Throwable t2) {
        //t2.printStackTrace();
      }
      if (type == null) {
        if (icon != null) {
          String iconName = icon.toString();
          //System.out.println("iconName : " + iconName);
          if (!iconName.startsWith("javax.swing") && !iconName.startsWith("sun.swing") && iconName.indexOf('@') < 0) {
            type = iconName;
          }
        }
      }
      //System.out.println("Fetched from system, icon="+icon+", type="+type);
    } catch (Throwable t) {
      //System.out.println(t.getMessage());
      //t.printStackTrace();
    }
    // delete temporary file
    if (tmpFile != null) tmpFile.delete();
    return new Object[] { icon, type };
  }

}