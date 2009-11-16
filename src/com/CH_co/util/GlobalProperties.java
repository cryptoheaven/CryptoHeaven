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

import java.io.*;
import java.util.*;

import com.CH_co.trace.TraceProperties;

/**
 * This class acts as a central repository for an program specific
 * properties. It reads an (program).properties file containing program-
 * specific properties. <p>
 *
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.96 $</b>
 * @author  Marcin Kurzawa
 */
public class GlobalProperties extends Object {

// Constants and variables with relevant static code
//...........................................................................

  private static final String SOFTWARE_NAME = "CryptoHeaven";
  private static final String SOFTWARE_NAME_EXT = "Client";
  public static final String PROGRAM_NAME = "CryptoHeavenClient";

  public static final short PROGRAM_RELEASE_ALPHA = 1;
  public static final short PROGRAM_RELEASE_BETA = 2;
  public static final short PROGRAM_RELEASE_FINAL = 3;

  // These final values are used in other places during compilation... keep them final!
  public static final float PROGRAM_VERSION = 2.9f;
  public static final short PROGRAM_VERSION_MINOR = 7;
  public static final String PROGRAM_VERSION_STR = "v"+PROGRAM_VERSION+"."+PROGRAM_VERSION_MINOR;
  public static final short PROGRAM_RELEASE = PROGRAM_RELEASE_FINAL;
  public static final short PROGRAM_BUILD_NUMBER = 474;  // even

  public static String PROGRAM_BUILD_DATE; // read in from a file
  public static String PROGRAM_FULL_NAME = SOFTWARE_NAME + " " + SOFTWARE_NAME_EXT + " build " + PROGRAM_BUILD_NUMBER;

  // build 86-124 are the Address Book editions
  // build 138 is stable Dec 2003 - Feb 2004
  // build 142 is Auto Responder edition
  // build 176 is prep for Contact Manager edition with minor fixes
  // build 180 is Auto-Update and Contact Manager edition
  // build 184 is Multi email-address edition
  // build 186 is Sending email from selected email-address-account
  // build 196 is the v2.4.1 release
  // build 198 is the fix to auto-responder to send messages from original receiving email addresses
  // build 202 includes aprava.com as another default domain
  // build 204 includes private label
  // build 206 includes msg xfer stats
  // build 210 public snapshot
  // build 212 addition of email address "Display As"
  // build 214 unicode email delivery bugs fixed
  // build 218 enable single connection sessions with synchronous file transfers
  // build 220 Message expiry and Revokation
  // build 222 Protocol change to support autonomus asymmetric encryption of selected data sets
  // build 224 Rename folder reflected in shares
  // build 236 Performance and Memory optimizations
  // build 238 Initial load time optimization through global change in VisualsSavable and providing key-class-name
  // build 240 Rijndael CBC mode performance optimizations and other
  // build 246 Default number of concurrent file transfers set at 2
  // build 248 Reduced number of concurrent db connections
  // build 250 Password protected email
  // build 252 Pickup link
  // build 254 Persistant preview mode
  // build 260 Online Status
  // build 262 Online Status - Away timeout setting
  // build 264 Recipient's Dialog list auto-completion
  // build 268 Auto-completion popup list
  // build 270 Private Label multiple email domains
  // build 274 Web Accounts
  // build 278 Protected Master Account shared folders
  // build 282 Access through HTTP Proxy
  // build 286 Spell Check
  // build 288 Proxy includes SOCKET connection if possible
  // build 290 Partial folder tree fetching for large trees
  // build 292 Prioritized reply data sets
  // build 294 Email delivery fixes
  // build 296 Groups
  // build 308 Cleanup changes
  // build 310 Message encryption with Question and Answer
  // build 314 Address Hash caching
  // build 316 Junk eMail Folder
  // build 318 WhiteList
  // build 320 Action HyperLinks
  // build 322 Fixed folder tree ordering
  // build 324 Added Folder Categories
  // build 330 Chat Log folder name changes
  // build 332 Extended Cleanup changed message fetch to increase in sizes
  // build 334 force reload of tree structure after change to workaround a GUI library bug
  // build 336 Chat folder name were clashing with contact names so added a "chat" postfix
  // build 338 Optional New Account button
  // build 340 Native JDBC Driver Type 4 support
  // build 342 Unblocks AWT thread when fetching data in Message preview, warns if AWT thread is blocked with network request
  // build 344 Faster server logins and request serving
  // build 346 BurstableMonitor
  // build 348 non-Materialized lobs
  // build 350 Open/Save/Cancel for attachments
  // build 352 Narrow-Wide Split Layouts
  // build 354 Send QA bug
  // build 356 Fetch bug fetch 0 msgs, fixed update fetching for longer disconnections
  // build 358 Recycle folder
  // build 360 Recycle folder one click clear
  // build 362 Find filter
  // build 364 Skip already fetched and cached Msg Links
  // build 366 Add default 'Find' button to toolbar
  // build 368 Add Password Recovery
  // build 370 Fix AutoUpdate on Vista
  // build 372 Toolbar with text, memory leaks fixed
  // build 374 Changed transfer animation
  // build 376 Support for 640x480 and no windows manager displays
  // build 378 Email invites turn into contacts
  // build 380 Trace record BCC filter
  // build 382 Auto create reciprocal contacts
  // build 384 Switch to HTTP-protocoled sockets if standard socket is failing.
  // build 386 Code polishing after static analysis for potential bugs.
  // build 388 Message folder fetch complete and interrupted events.
  // build 392 Connection number indicator sometimes shown incorrect number after "switch identity"
  // build 394 Adjusted AutoUpdater to support running from one-jar
  // build 396 private-label setting in lib\private-label.jar
  // build 398 Message preview popup menu, chat scroll to most recent msg, search blank GUI fix, JRE 1.4.1 tree GUI was screwy, wiping of temp files
  // build 400 Private Label customization changes
  // build 402 Download transfer progress monitor synch with number of transfer connections
  // build 404 Chat table reload scroll
  // build 406 Image resize
  // build 408 Problem with some HTML rendering locking up the GUI
  // build 410 URL reference removed
  // build 420 chat/posting clickable links
  // build 422 Command line params for testing batch connectivity
  // build 424 Voicemail
  // build 426 Message preview attachments panel grid-layout
  // build 428 Audio attachment discard warning
  // build 430 Audio playing clip control
  // build 432 Problem Reporting facility
  // build 434 Re-enabled emoticons
  // build 436 Queued up request to fetch new item counts
  // build 438 Fixed message briefs fetching from being stuck on low limits causing too many fast paced requests
  // build 440 Sub-account Activate/Suspend
  // build 442 BUZZer
  // build 444 Light GUI cleanup of borders
  // build 446 Icons redesigned
  // build 452 Password Reset / Key Recovery
  // build 456 Fetch sub-accounts to include suspension reason
  // build 458 Allow Master account to automatically create Password Reset records.
  // build 460 Fix engine-to-engine distribution of Message Body replies
  // build 462 Better handling of shared Inbox and live updates for participants
  // build 464 Clicking of mailto: links will compose email from address that recived the original message
  // build 466 Menu Editor
  // build 468 Re-connect now includes synching of empty tables, recycle bin, and removal of deleted messages
  // build 470 Decluttering of chat window, rearranged some menus and toolbars, deamonizing and priority changes to helper and background threads, other minor fixes
  // build 472 Performance optimization of DB connection get/return, faster server side chat entry through queued/handled-off post operations
  // build 474 Staged File List Fetching, plus temp file cleanup fixes

  public static final String SAVE_EXT = ".properties";
  static final String SAVE_FULL_NAME = PROGRAM_NAME + SAVE_EXT;

  static final Properties properties = new Properties();

  /**
   * Loads initial properties from file when class is loaded.
   */
  static {
    String date = GlobalProperties.class.getPackage().getImplementationVersion();
    if (date != null && date.indexOf('$') < 0 && date.indexOf('{') < 0 && date.indexOf('}') < 0) {
      PROGRAM_BUILD_DATE = date.trim();
      PROGRAM_FULL_NAME += " compiled on " + PROGRAM_BUILD_DATE;
    }
    PROGRAM_FULL_NAME += " " + PROGRAM_VERSION_STR + " rel. " + PROGRAM_RELEASE;
    initialLoad();
  }

  private static void initialLoad() {

    // now load the saved properties possibly overwriting the default ones.
    String it = getPropertiesFullFileName();
    InputStream is = null;
    try {
      is = new FileInputStream(it);
    } catch (Exception e) {
    }
    //InputStream is = GlobalProperties.class.getResourceAsStream(it);
    boolean ok = is != null;
    if (ok) {
      try {
        properties.load(is);
        is.close();
      } catch (Exception x) {
        ok = false;
      }
    }

//    // Remove all the MenuTreeModel.* from the properties and replace them by properties from this class.
//    Vector keysToRemoveV = new Vector();
//    Enumeration enm = properties.keys();
//    while (enm.hasMoreElements()) {
//      String key = (String) enm.nextElement();
//      if (key.startsWith("MenuTreeModel.")) {
//        keysToRemoveV.addElement(key);
//      }
//    }
//    for (int i=0; i<keysToRemoveV.size(); i++) {
//      properties.remove(keysToRemoveV.elementAt(i));
//    }

    if (!ok) {
      //System.err.println("WARNING: Unable to load \"" + it + "\" .  Will use default values instead.");
    } else {
      cleanupTempFiles();
      cleanupTempFilesOnFinalize();
    } // end if
  } // end initialLoad()

  /**
   * Set a Temporary File to be cleaned up.
   * Cleanup on request through API call or, first try on JVM Exit, second try on program start.
   */
  public static void addTempFileToCleanup(File tempFile) {
    addTempFileToCleanup(tempFile, "TempFiles");
  }
  public static void addTempFileToCleanupOnFinalize(File tempFile) {
    addTempFileToCleanup(tempFile, "TempFilesFinalize");
  }
  private static void addTempFileToCleanup(File tempFile, String propertyName) {
    tempFile.deleteOnExit();
    String tempFiles = GlobalProperties.getProperty(propertyName, "");
    tempFiles += ";" + tempFile.getAbsolutePath();
    GlobalProperties.setProperty(propertyName, tempFiles);
    GlobalProperties.store();
  }

  /**
   * Cleanup all Temporary Files that were added for cleanup
   */
  public static void cleanupTempFiles() {
    cleanupTempFiles("TempFiles");
  }
  public static void cleanupTempFilesOnFinalize() {
    cleanupTempFiles("TempFilesFinalize");
  }
  private static void cleanupTempFiles(String propertyName) {
    String tempFiles = getProperty(propertyName);
    if (tempFiles != null) {
      String newTempFiles = "";
      StringTokenizer st = new StringTokenizer(tempFiles, ";");
      while (st.hasMoreTokens()) {
        String filePath = st.nextToken();
        if (filePath != null && filePath.length() > 0) {
          File file = new File(filePath);
          if (file.exists()) {
            try {
              if (!CleanupAgent.wipeOrDelete(file)) {
                newTempFiles += ";" + file.getAbsolutePath();
              }
            } catch (Throwable t) {
              newTempFiles += ";" + file.getAbsolutePath();
            }
          }
        }
      } // end while
      setProperty(propertyName, newTempFiles);
    }
  }

// Properties methods (excluding load and save, which are deliberately not
// supported). Store is supported.
//...........................................................................

  /** Get the value of a property for this key. */
  public static String getProperty (String key) {
    String property = key != null ? properties.getProperty(key) : null;
    return property;
  }

  /**
   * Get the value of a property for this key, or return
   * <i>value</i> if the property was not set.
   */
  public static String getProperty (String key, String value) {
    String property = key != null ? properties.getProperty(key, value) : value;
    return property;
  }

  /**
   * Set a property value.  Calls the hashtable method put. 
   * Provided for parallelism with the getProperties method. 
   * Enforces use of strings for property keys and values.
   * @return the previous value of the specified key, or null if it did not have one.
   */
  public static String setProperty(String key, String value) {
    return (String) properties.setProperty(key, value);
  }

  /**
   * Remove a property value.
   */
  public static String remove(String key) {
    return (String) properties.remove(key);
  }

  /** List all properties to the PrintStream <i>out</i>. */
  public static void list (PrintStream out) {
    properties.list(out);
  }

  /** List all properties to the PrintWriter <i>out</i>. */
  public static void list (PrintWriter out) {
    properties.list(out);
  }

  /**
   * @return an enumeration of all the keys in this property list, including the keys in the default property list.
   */
  public static Enumeration propertyNames() {
    return properties.propertyNames();
  }



  // ********** storing is private *********
  private static void store (OutputStream out, String header) throws IOException {
    properties.store(out, header);
  }

  public static boolean store () {
    boolean success = true;
    OutputStream out = null;
    try {
      out = new FileOutputStream(getPropertiesFullFileName());
      store(out, PROGRAM_FULL_NAME);
    } catch (Exception e) {
      success = false;
    }
    try { out.flush(); } catch (Exception e) { }
    try { out.close(); } catch (Exception e) { }
    return success;
  }



  public static void setAlternatePropertiesDir(String dir) {
    TraceProperties.setAlternatePropertiesDir(dir);
    initialLoad();
  }

  private static String getPropertiesFullFileName() {
    String dir = TraceProperties.getPropertiesFullPathName();
    String rc = dir + GlobalProperties.SAVE_FULL_NAME;
    return rc;
  }

  public void finalize() throws Throwable {
    cleanupTempFilesOnFinalize();
    super.finalize();
  }
}