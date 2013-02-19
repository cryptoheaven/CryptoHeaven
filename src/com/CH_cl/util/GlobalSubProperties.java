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

package com.CH_cl.util;

import java.io.*;
import java.util.*;

import com.CH_co.trace.TraceProperties;
import com.CH_co.util.GlobalProperties;

/**
* This class acts as a central repository for an program specific
* properties. It reads an (program).properties file containing program-
* specific properties. <p>
*
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* <b>$Revision: 1.13 $</b>
* @author  Marcin Kurzawa
*/
public class GlobalSubProperties extends Properties {

  public static final String PROPERTY_EXTENSION_KEYS = "keys";

  private String alternateFileNamePart;
  private static Hashtable fileHT;

  public GlobalSubProperties(String subFileNameKey) {
    this(null, subFileNameKey);
  }

  public GlobalSubProperties(File subFile, String subFileNameKey) {
    this.alternateFileNamePart = subFileNameKey;
    if (subFile != null) {
      if (fileHT == null)
        fileHT = new Hashtable();
      fileHT.put(subFileNameKey, subFile);
    }
    load();
  }

  private void load() {
    //String error = null;
    // now load the saved properties possibly overwriting the default ones.
    FileInputStream in = null;
    try {
      in = new FileInputStream(getPropertiesFullFileName());
      load(in);
    } catch (Exception e) {
//      if (fileHT != null)
//        fileHT.remove(alternateFileNamePart);
      //error = "Loading of persistent data ("+alternateFileNamePart+") failed with error: " + e.getMessage();
    }
    try { in.close(); } catch (Exception e) { }
    //if (error != null)
      //throw new IllegalStateException(error);
  }

  public void store () {
    String error = null;
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(getPropertiesFullFileName());
      store(out, GlobalProperties.PROGRAM_FULL_NAME);
    } catch (Exception e) {
      error = "Storing of persistent data ("+getPropertiesFullFileName()+") failed with error: " + e.getMessage();
    }
    try { out.flush(); } catch (Exception e) { }
    try { out.close(); } catch (Exception e) { }
    if (error != null)
      throw new IllegalStateException(error);
  }


  public String getPropertiesFullFileName() {
    String rc = null;
    File file = null;
    if (fileHT != null && (file = (File) fileHT.get(alternateFileNamePart)) != null) {
      rc = file.getAbsolutePath();
    } else {
      rc = getPropertiesDefaultFullFileName(alternateFileNamePart);
    }
    return rc;
  }

  public static String getPropertiesDefaultFullFileName(String alternateFileNamePart) {
    String dir = TraceProperties.getPropertiesFullPathName();
    return getPropertiesDefaultFullFileName(dir, alternateFileNamePart);
  }

  public static String getPropertiesDefaultFullFileName(String dir, String alternateFileNamePart) {
    return dir + GlobalProperties.PROGRAM_NAME + "_" + alternateFileNamePart + GlobalProperties.SAVE_EXT;
  }

}