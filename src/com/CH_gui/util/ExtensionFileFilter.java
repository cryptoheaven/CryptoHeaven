/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ExtensionFileFilter extends FileFilter {

  String description;
  String[] extensions;

  public ExtensionFileFilter(String description, String extension) {
    this(description, new String[] { extension });
  }
  public ExtensionFileFilter(String description, String[] extensions) {
    this.description = description;
    this.extensions = extensions;
  }

  public boolean accept(File f) {
    String ext = null;
    if (f.isDirectory())
      return true;
    else {
      ext = getExtension(f);
      boolean isFound = false;
      if (ext != null) {
        for (int i=0; i<extensions.length; i++) {
          if (extensions[i].equals("*") || ext.equalsIgnoreCase(extensions[i])) {
            isFound = true;
            break;
          }
        }
      }
      return isFound;
    }
  }

  public String getDescription() {
    return description;
  }

  private String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 &&  i < s.length() - 1) {
      ext = s.substring(i+1).toLowerCase();
    }
    return ext;
  }
}