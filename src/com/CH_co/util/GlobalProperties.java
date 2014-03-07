/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.util;

import com.CH_co.trace.TraceProperties;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/**
* This class acts as a central repository for an program specific
* properties. It reads an (program).properties file containing program-
* specific properties. <p>
*
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.96 $</b>
*
* @author  Marcin Kurzawa
*/
public class GlobalProperties extends Object {

// Constants and variables with relevant static code
//...........................................................................

  private static final boolean DEBUG_INSERT_RANDOM_PROPERTY_ERRORS = false;
  private static final String PROPERTIES_RESET_AT_NEXT_LOAD = "properties-reset-at-next-load";

  private static final String SOFTWARE_NAME = "CryptoHeaven";
  private static final String SOFTWARE_NAME_EXT = "Client";
  public static final String PROGRAM_NAME = "CryptoHeavenClient";

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
  // build 254 Persistent preview mode
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
  // build 476 Save Attachments dialog includes Open button for messages and files, Download button additionally doubles as message export
  // build 478 Rendering of Address objects (priority/attachment icons) in chat/posting folders should comply with new compressed/cleaned-up view
  // build 480 Yellow flags should not appear in chat/posting folders
  // build 482 Addition of trace initial data when doing Diagnostics
  // build 484 Fix freezing due to reciving of fetch msg body replies when link is already removed from the cache
  // build 486 Addition of global try-catch-trace for new Threads and all Actions so we can trace any problems better in Diagnostics
  // build 488 Initial creation of accounts and Export of Private Key gives user option to choose key file storage.
  // build 490 Fix exception in launching message preview threat due to null data record, change "Invite by Email" icons and "Problem Reporting" large icon
  // build 492 Removing chat participant did not change table header description, refresh was required.
  // build 494 Change Delete Confirmation dialogs and include "Skip in the future" option.
  // build 496 User Options dialog changes
  // build 498 Sending Invites also adds those email addresses to the Address Book
  // build 500 Invited Email Addresses show in the contact list with function to re-send invite.
  // build 502 Content Toolbars
  // build 506 HTML cleanup to exclude <PRE></PRE> tags
  // build 508 Offline chat and participants list with status icon
  // build 510 Static code cleanups based on FindBugs reports, use of Number.valueOf() to conserve memory.
  // build 512 Loading speed optimizations
  // build 514 '.valueOf' replaced by constructor call to revert from Java 1.5 to 1.4
  // build 516 login server list inserts the private-label servers
  // build 518 Folder tree upload bug fixed
  // build 520 File table "Type" column sort bug fix
  // build 522 Add Frame Icon to private label customizations
  // build 524 Chat and Post table print view row fix, also include "From" value on each printed line
  // build 526 Private Label customizations for invites and notifications
  // build 528 Pulled out Icon references from Records
  // build 530 Fixed deadlock causing application to hang after GUI exit, and popup dialogs in applet after GUI exit.
  // build 532 Progress monitors changed to Factory design pattern and moved into the GUI library.
  // build 534 Stats data handler and StatsBar gui view with data listener
  // build 536 Upload/Download control-GUI separation
  // build 538 more control-GUI separation
  // build 540 Trace of messages includes trace for its attachments
  // build 542 Journal Prog Monitor factory/interface/implementation for message export.
  // build 544 File Replace Confirmation factory/interface/implementation.
  // build 546 Move nudge to GUI utils and remove all GUI from wiping
  // build 550 Properties reset improvements, and fixes against corrupted properties.
  // build 552 Paste popup menu action on password fields.
  // build 554 Custom tree structure to avoid using java.swing.tree.* in shared code with android
  // build 556 AutoUpdate timer reset when user manually switches his preference, spelling properties loading fixed.
  // build 558 New layout for JMyPasswordKeyboardField to extend from JPasswordField
  // build 560 Timeout-retry for server submittions
  // build 562 Message compose and preview fonts/sizes, and window title change
  // build 564 Rework of synchronization between SIL job submitter and responder.
  // build 566 Create TraceDiagnostics from common static code.
  // build 568 Fix broken auto-responder
  // build 570 SIL-servant additional tracing and IP flooding prevention
  // build 572 BurstableMonitor -> BurstableBucket, minimized SIL retries to login related actions only
  // build 574 Ping includes information regarding last reported network delay
  // build 576 Speed Limited streams can calculate rate without limiting throughput, fixes with restoreVisuals() exception handling, private label enhancements for replyURL and support email template
  // build 578 Changed chat message entry layout and added 'typing' notification into chat entry panel, removed toolbars from attachment chooser dialog
  // build 580 New actions: Add Star, Remove Star, removed coloured flags leaving only the red flags
  // build 582 Login failed message with guessed handle choices and direct email to handle retry
  // build 584 StatRecords get another flag FLAG_MARKED_NEW to distinguish genuinely new items and those marked as new, simplified folder category names and 1-click category expand
  // build 586 Autoscrolling by listening for the scroll bar adjustements.
  // build 588 Changed sound effects
  // build 590 Auto-scroll to bottom when already at bottom and view is resizing
  // build 592 Chat slide-up change
  // build 594 Window Popup sound change
  // build 596 Option to disable online status popup notifications
  // build 598 Different sound for new mail and new chat message, changes for user account limit quotas wording
  // build 600 Misc improvements geared towards usability for newly created accounts.
  // build 602 Server reports suggested number of child folders and adds seperate APIs for view based folder roots and child fetching
  // build 604 Optimized folder fetching sequence by reducing number of server requests
  // build 606 Empty templates and new user sharing enhancements
  // build 608 Implement properties consistency checking through hashing mechanism.
  // build 610 Minor initial properties adjustment to fix window sizes for email pickup, chat message pickup.
  // build 612 Integration with Sferyx HTML Editor
  // build 614 Single click contact adding form inline ContactBuildingPanel
  // build 616 HTML type preview without loading images
  // build 618 terminate pasted links in the HTML Editor
  // build 620 Login screen facelift, right-click speller menu extension
  // build 622 Addition of default servers for connectivity and removal of web only (data prohibited) servers
  // build 624 Quick fix to restore Java 1.4 compatibility
  // build 626 Updated dialog sounds
  // build 628 Connectivity fixes to disable sockets through HTTP tunnels because sockets are already used in direct engine connections
  // build 630 HTTP request size threshold for 'GET' style commands lowered as it had problems on 3G->router->bridge setups
  // build 632 Rich/Plain text switch for message compose
  // build 634 Default speller language choice enabled only when pc locale matches available dictionary, dictionary language setting restored from user settings
  // build 636 Email delivery fix for "Delivered-To:" envelope change, fix background checker sometimes messy zig-zag underlines
  // build 638 Message list and File list fetching returns "anySkippedOver" flag
  // build 640 FolderTreeModel(s) and FolderTreeNode(s) cleanup
  // build 640 HTTP Socket overhaul to try eliminating the memory leak
  // build 642 File upload/download transfer progress upgrades
  // build 644 File data transfer overhaul to allow interrupted uploads to resume.
  // build 646 Fixes occasional download enc-file-size-zero bug, msg preview removal of styles and background images, faster applet resource loading
  // build 648 Image pasted in to HTML editor gets uploaded as attachment
  // build 650 v3.5.1 updated installers
  // build 652 v3.5.1 updated installers with fix to file attachment upload with additional Q&A encryption
  // build 654 v3.5.1 updated installers with fix to voice file attachments static
  // build 656 v3.5.2 emergency fix to build 652 for multiple file attachments
  // build 658 Remove pasted image links and replace them by [filename], change warning when closing app with running uploads.
  // build 660 File uploads do all encryptions asap to free up local file dependency, status notifications changed, added upload aborted popup message.
  // build 662 Sferyx v11
  // build 664 Welcome email changed, welcome email notification removed and replaced by welcome email body, tell-a-friend enabled.
  // build 666 File Open tracking for edits
  // build 668 Exit wipes temp files open for edits
  // build 670 First public release of applets and installers with edit-synch
  // build 672 Sferxy editor 'insert symbol' icon resized, chat scroll to most recent when offline panel shows and reduces table view area.
  // build 674 Changes related to command line file uploads and msg attachment uploads, also stats and other server synchronizations.
  // build 676 Search in msg chat folder includes attachments dynamic rendering part, autoscroll to selected item after searching, save scroll pane size/orientation, logo 2012
  // build 678 Delayed GUI focusing adjustments
  // build 680 Elimination of .requestFocus() in favour to .requestFocusInWindow() and eliminate grabbing delayed focus to dialog's default button as it caused some random focus switching between windows.
  // build 682 Further trimming of styles in HTML rendering
  // build 684 Various performance tuning to make the GUI more snappy, increase minimum RSA key length to 2048.
  // build 686 Fix for reconnection updates and folder validation when computer is put to 'sleep'
  // build 688 Zoom-in and Zoom-out in message preview and email composer.
  // build 690 Language files moved to client side package, and extensive code remodeling for android. Added info/warning/error status bar on top of tables.
  // build 692 Changed the way file and message dates look in the tables.
  // build 694 Remove time from message/file table when record has last year's date or older, fix contact ordering for online contacts that never changed state to available/away/etc
  // build 696 Fix unwrapping of folder shares given through group memberships when they come in out of sequence with their parent dependencies
  // build 698 Simply status bar, update Sferyx v11 -> v12, fix interrupted errors when going from sleep to alive.
  // build 700 Icon changes
  // build 702 icon lock issues in From/To and recipient panels
  // build 704 Message print rendering should default to black, remove deadlock related to logging out workers
  // build 706 Limit to 1 connection during initial login stage
  // build 708 Added a few trace points to SIL
  // build 710 Fixed runtime compatibility with JRE 1.5 and JRE 1.4.2
  // build 712 Adjusting synchronization/concurrency
  // build 714 Fix message threading border indents in the msg/post tables
  // build 716 Fix conversions to familiar user to use our own cantact list and avoid reciprocal contacts, fix NullPointerException in creating chats when share is not cached.
  // build 718 Convert 'reciprocal' contact use as recipient for mail or shares, to our 'own' contact, smaller font for multiple chat attachments
  // build 720 Show calculated storage stamp for all accounts
  // build 722 Make the recycle table searchable
  // build 724 Fix message editor 'paste' ctrl-v for jre 1.4
  // build 726 Remove default 'Download' toolbar button from email tables, 'download'='email export' which is rare and confusable with 'save attachments'
  // build 728 Work in progress towards Folder Synch, also fix for legacy BrowserLauncher to use Java 1.6 Desktop class if possible
  // build 730 Folder Synch - working alpha
  // build 732 Folder Synch - working beta adds synch of Contacts and Folder-tree
  // build 734 Disconnected users get updates with transient-temporary packets upon their prompt reconnection.
  // build 736 Servers request temp user packets from relevant peer-servers only, and do not broadcast requests to all engines.
  // build 738 Adjustments for mobile to connection counts and ping-pong/timeouts.
  // build 740 Add manual storage recalculate
  // build 742 Folder synch should first remove then insert new items, important for synching folder trees.
  // build 744 Fix opening/downloading files with invalid filenames (possible when created on Mac OSX and downloaded to Windows)
  // build 746 Fix to skip sending too many redundant Online/Offline notifications.
  // build 748 Added 'dateUsed' to Contacts and FolderShares
  // build 750 Transfer 'dateUsed' for MsgLinkRecord
  // build 752 Consistent colors for chat user and background
  // build 754 Contact creation from 'email address' seperated as utility ops for Android
  // build 756 Default Uncaught Exception Handler
  // build 758 Case of 'ThreadDeath' in exception handler and minor fixes based on desktop crash reports.
  // build 760 Engine fixes for few minor reported exception
  // build 762 Engine restricting additional nicknames.
  // build 764 Message reply quoting parts moved to common client package.
  // build 766,768,770 Fixing more exceptions based on bug reports
  // build 772,774,776 Unsealing objects in the cache outside of synchronized
  // build 778 Removed GUI tree manipulations from non-gui threads
  // build 780 Filtering out 'no-subject' automatic .eml attachment, few reported exceptions fixed.
  // build 782 Upload/Download take context for attaching progress indicators.
  // build 784 Progress monitor changes to accommodate Android, several exception fixes based on crash reports
  // build 786 Fixes in background file uploads to prevent multiple workers competing for single file.
  // build 788 Minor fixes based on exception reports
  // build 790 Enable Socket-Timeout for sockets connecting to the engine, support I/O cancellation via ProgMonitor for moving data between streams.
  // build 792 Fixes for more detailed error message for creating folders that inherit from parent and creator has no access to all parent's share groups
  // build 794 Send clientOS with login info
  // build 796 Msg reply use 'div' instead of 'table', clear login info when server gives permanent error
  // build 798 SaluSafe v1.32
  // build 800 SecureRandom adjustment, and use more bits for randomizing symmetric keys, cleaner plain email warning, don't automatically convert not hosted email addresses to web accounts, contact dialog simpler status line
  // build 802 Fix file attachment chooser dialog for case of multiple file versions in folder. SaluSafe v1.33 and v1.34
  // build 804 Provide support Copy & Paste support for copying address book tables for pasting into external spreadsheets.
  // build 806 DND fix for flickering of Copy/Move cursors
  // build 808 Fix manifest permissions attribute
  // build 810 Clicking on URLs with encoding should decode url before passing it to the browser, plus Java 1.4 compatibility fix
  // build 812 Trace history reported incorrect stats in some cases involving privilege records.
  // build 814 Dragging msgs/files over folders in tree view was failing to update cursor.
  // build 816 Filtering out of external images should include removal of external base reference.
  // build 818 Addition of firstRead stamp to the access records

  public static final short PROGRAM_BUILD_NUMBER = 818;  // even
  public static final boolean IS_BETA = false;

  // These final values are used in other places during compilation... keep them final!
  public static final float PROGRAM_VERSION = 3.8f;
  public static final short PROGRAM_VERSION_MINOR = 5;
  public static final String PROGRAM_VERSION_STR = "v"+PROGRAM_VERSION+(PROGRAM_VERSION_MINOR != 0 ? "."+PROGRAM_VERSION_MINOR : "");

  public static final short PROGRAM_RELEASE_ALPHA = 1;
  public static final short PROGRAM_RELEASE_BETA = 2;
  public static final short PROGRAM_RELEASE_FINAL = 3;

  public static final short PROGRAM_RELEASE = IS_BETA ? PROGRAM_RELEASE_BETA : PROGRAM_RELEASE_FINAL;

  public static String PROGRAM_FULL_NAME = SOFTWARE_NAME + " " + SOFTWARE_NAME_EXT + " build " + PROGRAM_BUILD_NUMBER;
  public static String PROGRAM_BUILD_DATE; // read in from a file

  public static final String SAVE_EXT = ".properties";
  static final String SAVE_FULL_NAME = PROGRAM_NAME + SAVE_EXT;

  static final Properties properties = new Properties();
  static String[][] defaultProperties = null;

  public static final String PROPERTY_USER_NAME = "LastUserName";

  /**
  * Loads initial properties from file when class is loaded.
  */
  static {
    try {
      String date = GlobalProperties.class.getPackage().getImplementationVersion();
      if (date != null && date.indexOf('$') < 0 && date.indexOf('{') < 0 && date.indexOf('}') < 0 && !date.equals("0.0")) {
        PROGRAM_BUILD_DATE = date.trim();
        PROGRAM_FULL_NAME += " compiled on " + PROGRAM_BUILD_DATE;
      }
      PROGRAM_FULL_NAME += " " + PROGRAM_VERSION_STR + " rel. " + PROGRAM_RELEASE;
      initialLoad();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private static synchronized void initialLoad() {
    // now load the saved properties possibly overwriting the default ones.
    String it = getPropertiesFullFileName();
    InputStream is = null;
    boolean ok = false;
    try {
      is = new FileInputStream(it);
      ok = is != null;
      load(is);
    } catch (Exception x1) {
      ok = false;
    } finally {
      try {
        if (is != null) is.close();
      } catch (Exception x2) {
      }
    }
    if (DEBUG_INSERT_RANDOM_PROPERTY_ERRORS) {
      // insert random errors into every property
      Iterator iter = properties.keySet().iterator();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        String value = properties.getProperty(key);
        StringBuffer sb = new StringBuffer(value);
        int insertAt = sb.length() > 0 ? new Random().nextInt(sb.length()) : 0;
        sb.insert(insertAt, (char) new Random().nextInt(256));
        properties.setProperty(key, sb.toString());
      }
    }
    if (!ok) {
      // System.err.println("GlobalProperties: WARNING: Unable to load \"" + it + "\" .  Will use default values instead.");
    } else {
      // System.err.println("GlobalProperties: starting to cleanup temp files");
      cleanupTempFiles();
      cleanupTempFilesOnFinalize();
      // System.err.println("GlobalProperties: done cleanup of temp files");
    } // end if
    // if properties reset flag is set then reset properties
    String propResetFlag = properties.getProperty(PROPERTIES_RESET_AT_NEXT_LOAD);
    if (propResetFlag != null && propResetFlag.equalsIgnoreCase("true")) {
      resetMyAndGlobalProperties(false);
      // This is redundant since reset removes this property too,
      // but keep it just incase future changes keep the property.
      properties.remove(PROPERTIES_RESET_AT_NEXT_LOAD);
    }
  } // end initialLoad()

  public static void initDefaultProperties(String[][] initialDefaultProperties) {
    defaultProperties = initialDefaultProperties;
    // set initial default properties if they were not loaded from file
    if (defaultProperties != null) {
      for (int i=0; i<defaultProperties.length; i++) {
        try {
          if (getProperty(defaultProperties[i][0]) == null) {
            setProperty(defaultProperties[i][0], defaultProperties[i][1]);
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  /**
  * Removes all local properties that are global and for current user.
  */
  public static void resetMyAndGlobalProperties() {
    // reset all stored properties and mark for re-reset on next load to erase settings written at app closing
    resetMyAndGlobalProperties(true);
  }
  private static void resetMyAndGlobalProperties(boolean insertResetFlagForNextLoad) {
    Enumeration keys = properties.keys();
    //String myUserPropertyPrefix = getUserPropertyPrefix();
    String generalUserPropertyPrefix = getGeneralUserPropertyPrefix();
    ArrayList removeKeysL = new ArrayList();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (!key.startsWith(generalUserPropertyPrefix)) { // || key.startsWith(myUserPropertyPrefix)) {
        if (!key.startsWith(PROPERTY_USER_NAME) &&
                !key.startsWith("EncRSAPrivateKey") &&
                !key.startsWith("TempFiles") &&
                !key.startsWith("file-lob-up"))
          removeKeysL.add(key);
      }
    }
    for (int i=0; i<removeKeysL.size(); i++) {
      GlobalProperties.remove((String) removeKeysL.get(i));
    }
    if (insertResetFlagForNextLoad) {
      GlobalProperties.setProperty(PROPERTIES_RESET_AT_NEXT_LOAD, "true");
    }
    initDefaultProperties(defaultProperties);
  }

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
    String tempFiles = GlobalProperties.getProperty(propertyName, "");
    tempFiles += ";" + tempFile.getAbsolutePath();
    GlobalProperties.setProperty(propertyName, tempFiles);
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
    try {
      String tempFiles = getProperty(propertyName);
      if (tempFiles != null) {
        StringBuffer newTempFiles = new StringBuffer();
        StringTokenizer st = new StringTokenizer(tempFiles, ";");
        while (st.hasMoreTokens()) {
          try {
            String filePath = st.nextToken();
            if (filePath != null && filePath.length() > 0) {
              File file = new File(filePath);
              if (file.exists()) {
                try {
                  if (!CleanupAgent.wipeOrDelete(file)) {
                    newTempFiles.append(";");
                    newTempFiles.append(file.getAbsolutePath());
                  }
                } catch (Throwable t) {
                  newTempFiles.append(";");
                  newTempFiles.append(file.getAbsolutePath());
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } // end while
        setProperty(propertyName, newTempFiles.toString());
      }
    } catch (Throwable t) {
    }
  }

// Properties methods (excluding load and save, which are deliberately not
// supported). Store is supported.
//...........................................................................

  /** Get the value of a property for this key. */
  public static String getProperty(String key) {
    return getProperty(key, null, null);
  }

  /**
  * Get the value of a property for this key, or return
  * <i>value</i> if the property was not set.
  */
  public static String getProperty(String key, String value) {
    return getProperty(key, value, null);
  }
  public static String getProperty(String key, String value, Long userId) {
    String property = value;
    if (key != null) {
      if (userId != null) {
        property = properties.getProperty(getUserPropertyPrefix(userId) + key, value);
      } else {
        property = properties.getProperty(key, value);
      }
    }
    return property;
  }
  private static String getUserPropertyPrefix(Long userId) {
    return getGeneralUserPropertyPrefix() + userId + "-";
  }
  private static String getGeneralUserPropertyPrefix() {
    return "UID-";
  }

  /**
  * Set a property value.  Calls the hashtable method put.
  * Provided for parallelism with the getProperties method.
  * Enforces use of strings for property keys and values.
  * @return the previous value of the specified key, or null if it did not have one.
  */
  public static synchronized String setProperty(String key, String value) {
    return (String) properties.setProperty(key, value);
  }
  public static synchronized String setProperty(String key, String value, Long userId) {
    String property = null;
    if (userId != null) {
      property = (String) properties.setProperty(getUserPropertyPrefix(userId) + key, value);
    } else {
      property = (String) properties.setProperty(key, value);
    }
    return property;
  }

  /**
  * Remove a property value.
  */
  public static synchronized String remove(String key) {
    return (String) properties.remove(key);
  }

  /** List all properties to the PrintStream <i>out</i>. */
  public static synchronized void list(PrintStream out) {
    hashAdd();
    properties.list(out);
  }

  /** List all properties to the PrintWriter <i>out</i>. */
  public static synchronized void list(PrintWriter out) {
    hashAdd();
    properties.list(out);
  }

  /**
  * @return an enumeration of all the keys in this property list, including the keys in the default property list.
  */
  public static Enumeration propertyNames() {
    return properties.propertyNames();
  }

  private static synchronized String hashGet() {
    String hash = null;
    Enumeration enm = properties.keys();
    ArrayList keysL = new ArrayList();
    while (enm.hasMoreElements()) {
      String key = (String) enm.nextElement();
      if (!key.equalsIgnoreCase("md5")) {
        keysL.add(key);
      }
    }
    String[] keys = new String[keysL.size()];
    keysL.toArray(keys);
    Arrays.sort(keys);
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      for (int i=0; i<keys.length; i++) {
        String key = keys[i];
        String value = properties.getProperty(key);
        md5.update(key.getBytes());
        md5.update(value.getBytes());
      }
      hash = ArrayUtils.toString(md5.digest());
    } catch (Exception e) {
    }
    return hash;
  }

  private static synchronized void hashAdd() {
    String hash = hashGet();
    if (hash != null)
      properties.setProperty("md5", hash);
  }

  private static synchronized boolean hashVerify() {
    boolean hashOk = true;
    String hash = hashGet();
    if (hash != null) {
      String oldHash = properties.getProperty("md5");
      if (oldHash != null)
        hashOk = hash.equals(oldHash);
    }
    return hashOk;
  }

  /**
  * Loading is private because it needs to verify the current hash of all property values.
  */
  private static synchronized void load(InputStream is) throws IOException {
    properties.load(is);
    if (!hashVerify()) {
      resetMyAndGlobalProperties(false);
    }
  }

  /**
  * Storing is private because it needs to update the current hash of all property values.
  */
  private static synchronized void store(OutputStream out, String header) throws IOException {
    hashAdd();
    properties.store(out, header);
  }

  public static synchronized boolean store() {
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

  protected void finalize() throws Throwable {
    cleanupTempFilesOnFinalize();
    super.finalize();
  }
}