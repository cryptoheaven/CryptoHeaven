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

package com.CH_co.service.msg;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class CommandCodes extends Object {

  // ===========================
  // *** System ASCII Checks ***
  // ===========================

  public static final int SYS_Q_CHECK = 'i' << 24 | 'n' << 16 | 'i' << 8 | 't';
  public static final int SYS_A_CHECK = 'r' << 24 | 'e' << 16 | 's' << 8 | 'p';

  // =====================
  // *** User Commands ***
  // =====================

  // Login Secure Session
  public static final int USR_Q_LOGIN_SECURE_SESSION = 1000;
  public static final int USR_A_LOGIN_SECURE_SESSION = 10000;
  //-10001 User handle and passwordHash combination does not exist.
  public static final int USR_E_HANDLE_PASSWORD_COMBO_DNE = -10001;
  //-10002 User is locked out.
  public static final int USR_E_USER_LOCKED_OUT = -10002;
  public static final int USR_E_LOGIN_FAILED = -10003;

  // Get Login Info
  public static final int USR_Q_GET_LOGIN_INFO = 1005;
  // Get Re-Connection Update
  public static final int USR_Q_GET_RECONNECT_UPDATE = 1006;

  // Get My Info
  public static final int USR_Q_GET_INFO = 1010;
  public static final int USR_A_GET_INFO = 10010;

  // Get Sub-Account Info
  public static final int USR_Q_GET_SUB_ACCOUNTS = 1011;
  public static final int USR_A_GET_SUB_ACCOUNTS = 10011;

  // Create New User
  public static final int USR_Q_NEW_USER = 1020;
  public static final int USR_A_NEW_USER = 10020;
  public static final int USR_E_HANDLE_ALREADY_TAKEN = -10021;

  // Create New Sub-User Accounts
  public static final int USR_Q_NEW_SUB = 1025;

  // Alter User Data
  public static final int USR_Q_ALTER_DATA = 1030;
  // answer with:
  // public static final int USR_A_GET_INFO = 10010;

  // Alter User Password
  public static final int USR_Q_ALTER_PASSWORD = 1040;
  public static final int USR_A_ALTER_PASSWORD = 10040;
  public static final int USR_E_ALL_KEYS_MUST_BE_SPECIFIED = -10041;
  //public static final int USR_E_LIST_WITH_DUPLICATE_ENTRIES = -10042;
  public static final int USR_E_PROOF_ORIGINALITY_CHECK_FAILED = -10043;

  // Password Recovery codes
  public static final int USR_Q_PASS_RECOVERY_UPDATE = 1041;
  public static final int USR_Q_PASS_RECOVERY_GET_CHALLENGE = 1042;
  public static final int USR_A_PASS_RECOVERY_GET_CHALLENGE = 10042;
  public static final int USR_Q_PASS_RECOVERY_GET_COMPLETE = 1043;
  public static final int USR_A_PASS_RECOVERY_GET_COMPLETE = 10043;

  // Change Online Status
  public static final int USR_Q_CHANGE_ONLINE_STATUS = 1044;

  // Delete Account
  public static final int USR_Q_DELETE = 1045;
  public static final int USR_A_DELETE = 10045;

  // Password Reset for sub-accounts
  public static final int USR_Q_PASSWORD_RESET = 1046;

  // Logout
  public static final int USR_Q_LOGOUT = 1050;
  public static final int USR_A_LOGOUT = 10050;

  // System asking for session recycle
  public static final int USR_A_RECYCLE_SESSION_REQUEST = 10051;
  public static final int USR_Q_RECYCLE_SESSION_SEQUENCE = 1052;
  public static final int USR_A_RECYCLE_SESSION_SEQUENCE = 10052;

  // Delete an account
  public static final int USR_Q_REMOVE = 1055;
  public static final int USR_A_REMOVE = 10055;

  // Change status of account (Active/Suspended)
  public static final int USR_Q_CHANGE_STATUS = 1056;
  public static final int USR_A_SUSPENDED = 10056;

  // Get User Handles
  public static final int USR_Q_GET_HANDLES = 1060;
  public static final int USR_A_GET_HANDLES = 10060;
  public static final int USR_E_UNKNOWN_ID = -10061;

  // Search for Users
  public static final int USR_Q_SEARCH = 1070;

  // Check for availability of User Name
  public static final int USR_Q_CHECK_AVAIL = 1075;

  // Send Email Invitation
  public static final int USR_Q_SEND_EMAIL_INVITATION = 1080;

  // Get Accessors
  public static final int USR_Q_GET_ACCESSORS = 1090;

  // Calculate Cummulative Usage
  public static final int USR_Q_CUMULATIVE_USAGE = 1095;
  public static final int USR_A_CUMULATIVE_USAGE = 10095;

  // Apply Code
  public static final int USR_Q_APPLY_CODE = 1096;


  // ======================
  // *** Email Commands ***
  // ======================

  // Get Emails
  public static final int EML_Q_GET = 1900;
  public static final int EML_A_GET = 10900;
  // Get Available Domains
  public static final int EML_Q_GET_DOMAINS = 1905;
  public static final int EML_A_GET_DOMAINS = 10905;
  // Create Emails
  public static final int EML_Q_CREATE = 1910;
  // Alter Emails
  public static final int EML_Q_ALTER = 1920;
  public static final int EML_Q_MANAGE = 1925;
  // Remove Emails
  public static final int EML_Q_REMOVE = 1930;
  public static final int EML_A_REMOVE = 10930;
  // Check for availability of Email Address
  public static final int EML_Q_CHECK_AVAIL = 1940;
  // Lookup email addresses
  public static final int EML_Q_LOOKUP_ADDR = 1950;


  // =============================
  // *** Address Book Commands ***
  // =============================

  // Find if email address exists in Address Book
  public static final int ADDR_Q_FIND_HASH = 2000;
  public static final int ADDR_A_FOUND_HASH = 12000;


  // =======================
  // *** Folder Commands ***
  // =======================

  // Get My Folders
  public static final int FLD_Q_GET_FOLDERS = 1100;
  public static final int FLD_Q_GET_FOLDERS_SOME = 1104;
  public static final int FLD_A_GET_FOLDERS = 10100;

  public static final int FLD_Q_GET_FOLDERS_CHILDREN = 1102;
  public static final int FLD_A_GET_FOLDERS_CHILDREN = 10102;

  public static final int FLD_Q_GET_FOLDERS_ROOTS = 1106;
  public static final int FLD_A_GET_FOLDERS_ROOTS = 10106;

  // New Private Folder
  public static final int FLD_Q_NEW_FOLDER = 1110;
  public static final int FLD_Q_NEW_OR_GET_OLD = 1111;
  public static final int FLD_Q_NEW_DFT_DRAFT_OR_GET_OLD = 1112;
  public static final int FLD_Q_NEW_DFT_ADDRESS_OR_GET_OLD = 1113;
  public static final int FLD_Q_NEW_DFT_JUNK_OR_GET_OLD = 1114;
  public static final int FLD_Q_NEW_DFT_WHITE_OR_GET_OLD = 1116;
  public static final int FLD_Q_NEW_DFT_RECYCLE_OR_GET_OLD = 1117;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;
  // public static final int FLD_E_CANNOT_BE_POSTING_FOLDER = - 10111;
  public static final int FLD_E_PARENT_FOLDER_DNE_OR_NOT_YOURS = -10112;
  // Alter Folder Attributes
  public static final int FLD_Q_ALTER_FLD_ATTR = 1115;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;

  // Alter Share Strings
  public static final int FLD_Q_ALTER_STRS = 1120;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;

  // To Symmetric Encryption
  public static final int FLD_Q_TO_SYM_ENC = 1125;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;

  // Alter Share Permissions
  public static final int FLD_Q_ALTER_PERMISSIONS = 1130;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;
  public static final int FLD_E_FOLDER_DNE_OR_NOT_YOURS = -10131;

  // Move Folder
  public static final int FLD_Q_MOVE_FOLDER = 1140;
  public static final int FLD_E_ILLEGAL_FOLDER_MOVE = -10141;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;

  // Transfer Ownership
  public static final int FLD_Q_TRANSFER_OWNERSHIP = 1145;
  // answer with:
  // public static final int FLD_A_GET_FOLDERS = 10100;

  // Remove Folder
  public static final int FLD_Q_REMOVE_FOLDER = 1150;
  public static final int FLD_A_REMOVE_FOLDER = 10150;
  public static final int FLD_E_CANNOT_REMOVE_SUPER_ROOT_FOLDER = -10151;

  // Remove Folder Shares
  public static final int FLD_Q_REMOVE_FOLDER_SHARES = 1160;

  // Get Folder Shares
  public static final int FLD_Q_GET_FOLDER_SHARES = 1170;
  // Add Folder Shares
  public static final int FLD_Q_ADD_FOLDER_SHARES = 1180;
  // Get Folder Size
  public static final int FLD_Q_GET_FOLDER_SIZE = 1190;
  public static final int FLD_A_GET_FOLDER_SIZE = 10190;
  // Get Folder Size Summary
  public static final int FLD_Q_GET_SIZE_SUMMARY = 1191;
  public static final int FLD_A_GET_SIZE_SUMMARY = 1191; // bad return code but keep it for compatibility
  // Get Access Users
  public static final int FLD_Q_GET_ACCESS_USERS = 1192;
  // Get Folder Red Flag Count
  public static final int FLD_Q_RED_FLAG_COUNT = 1195;
  public static final int FLD_A_RED_FLAG_COUNT = 10195;

  // Ring Ring
  public static final int FLD_Q_RING_RING = 1196;
  public static final int FLD_A_RING_RING = 10196;


  // =====================
  // *** Stat Commands ***
  // =====================

  // Get Stats
  public static final int STAT_Q_GET = 1200;
  public static final int STAT_A_GET = 10200;

  // Update Mark
  public static final int STAT_Q_UPDATE = 1210;

  // Fetch All Object Stats
  public static final int STAT_Q_FETCH_ALL_OBJ_STATS = 1220;


  // =====================
  // *** File Commands ***
  // =====================

  // New File
  public static final int FILE_Q_NEW_FILES = 1800;
  public static final int FILE_A_GET_FILES = 10800;

  // Get Files
  public static final int FILE_Q_GET_FILES = 1810;
  public static final int FILE_Q_GET_FILES_STAGED = 1811;
  // reply is the same as for FILE_A_GET_FILES

  // Get Msg File Attachments
  public static final int FILE_Q_GET_MSG_FILE_ATTACHMENTS = 1815;
  // reply is the same as for FILE_A_GET_FILES

  // Get File Data Attributes
  public static final int FILE_Q_GET_FILES_DATA_ATTRIBUTES = 1820;
  public static final int FILE_A_GET_FILES_DATA_ATTRIBUTES = 10820;

  // Get File Data
  public static final int FILE_Q_GET_FILES_DATA = 1830;
  public static final int FILE_A_GET_FILES_DATA = 10830;

  // Remove Files
  public static final int FILE_Q_REMOVE_FILES = 1840;
  public static final int FILE_A_REMOVE_FILES = 10840;

  // Move Files
  public static final int FILE_Q_MOVE_FILES = 1850;
  // reply is the same as for FILE_A_GET_FILES

  // Copy Files
  public static final int FILE_Q_COPY_FILES = 1860;
  // reply is the same as for FILE_A_GET_FILES

  // Rename File
  public static final int FILE_Q_RENAME = 1870;

  // Save Msg File Attachments
  public static final int FILE_Q_SAVE_MSG_FILE_ATT = 1880;

  // Update Status
  public static final int FILE_Q_UPDATE_STATUS = 1890;

  // ========================
  // *** Contact Commands ***
  // ========================

  // Get My Contacts
  public static final int CNT_Q_GET_CONTACTS = 1300;
  public static final int CNT_A_GET_CONTACTS = 10300;

  // Get My group account Contacts
  public static final int CNT_Q_GET_GROUP_CONTACTS = 1305;
  //public static final int CNT_A_GET_CONTACTS = 10300;

  // Set My group Contacts, includes create/change/delete
  public static final int CNT_Q_SET_GROUP_CONTACTS = 1306;

  // New Contact
  public static final int CNT_Q_NEW_CONTACT = 1310;
  // New Sub-user Contacts
  public static final int CNT_Q_NEW_SUB_CONTACTS = 1315;
  // reply is the same as for CNT_A_GET_CONTACTS
  //public static final int CNT_E_FOLDER_DNE_OR_NOT_YOURS = -10310;
  //public static final int CNT_E_EQUIVALENT_CONTACT_ALREADY_EXISTS = -10312;
  //public static final int CNT_E_USERID_DNE_OR_INVALID = -10313;

  // Accept Contacts
  public static final int CNT_Q_ACCEPT_CONTACTS = 1320;
  //public static final int CNT_A_ACCEPT_CONTACTS = 10320;
  public static final int CNT_E_CONTACT_IS_NOT_ACCESSIBLE = -10320;

  // Decline Contacts
  public static final int CNT_Q_DECLINE_CONTACTS = 1330;
  //public static final int CNT_A_DECLINE_CONTACTS = 10330;

  // Acknowledge Contacts
  //Notes:  Once contact is acknowledged, the final encrypted name and description are given.
  public static final int CNT_Q_ACKNOWLEDGE_CONTACTS = 1340;
  //public static final int CNT_A_ACKNOWLEDGE_CONTACTS = 10340;

  // Move Contacts
  public static final int CNT_Q_MOVE_CONTACTS = 1350;
  //public static final int CNT_A_MOVE_CONTACTS = 10350;

  // Remove Contacts
  public static final int CNT_Q_REMOVE_CONTACTS = 1360;
  public static final int CNT_A_REMOVE_CONTACTS = 10360;

  // Rename My Contact
  public static final int CNT_Q_RENAME_MY_CONTACT = 1370;

  // Rename Contacts With Me
  public static final int CNT_Q_RENAME_CONTACTS_WITH_ME = 1380;

  // Alter Permissions for the other party
  public static final int CNT_Q_ALTER_PERMITS = 1390;

  // Alter personal Settings
  public static final int CNT_Q_ALTER_SETTINGS = 1391;

  //====================================
  // *** Message / Posting  Commands ***
  //====================================

  // Get Messages Full
  public static final int MSG_Q_GET_FULL = 1400;
  public static final int MSG_A_GET = 10400;
  // Get Messages Briefs
  public static final int MSG_Q_GET_BRIEFS = 1410;
  // Get Msg Attachments
  public static final int MSG_Q_GET_MSG_ATTACHMENT_BRIEFS = 1415;
  // Get Message Body
  public static final int MSG_Q_GET_BODY = 1420;
  public static final int MSG_A_GET_BODY = 10420;
  // Get Single Full Message
  public static final int MSG_Q_GET_MSG = 1421;
  // New Message
  public static final int MSG_Q_NEW = 1430;
  // To Symmetric Encryption
  public static final int MSG_Q_TO_SYM_ENC = 1440;
  // Remove Messages
  public static final int MSG_Q_REMOVE = 1450;
  public static final int MSG_A_REMOVE = 10450;
  public static final int MSG_Q_REMOVE_OLD = 1460;
  // Move Messages
  public static final int MSG_Q_MOVE = 1470;
  // Copy Messages
  public static final int MSG_Q_COPY = 1480;
  //Save Messages Attachments
  public static final int MSG_Q_SAVE_MSG_ATT = 1490;

  // Typing message
  public static final int MSG_Q_TYPING = 1500;
  public static final int MSG_A_TYPING = 10510;

  // Expiry Revocation
  public static final int MSG_Q_EXPIRY = 1510;

  // Update Status
  public static final int MSG_Q_UPDATE_STATUS = 1520;


  // =====================
  // *** Keys Commands ***
  // =====================

  // Get My Key Pairs
  public static final int KEY_Q_GET_KEY_PAIRS = 1600;
  public static final int KEY_A_GET_KEY_PAIRS = 10600;

  // Get My Current Key Pair
  //public static final int KEY_Q_GET_CURRENT_KEY_PAIR = 1610;

  // Get Public Keys For Users
  public static final int KEY_Q_GET_PUBLIC_KEYS_FOR_USERS = 1620;
  public static final int KEY_A_GET_PUBLIC_KEYS = 10620;

  // Get Public Key By IDs
  public static final int KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS = 1630;

  // New Key Pair
  public static final int KEY_Q_NEW_KEY_PAIR = 1640;

  // Remove Key Pairs
  public static final int KEY_Q_REMOVE_KEY_PAIRS = 1650;
  public static final int KEY_A_REMOVE_KEY_PAIRS = 10650;

  // Set Key Recovery / Enable Password Reset
  public static final int KEY_Q_SET_KEY_RECOVERY = 1660;
  // Get Key Recovery records
  public static final int KEY_Q_GET_KEY_RECOVERY = 1661;
  public static final int KEY_A_GET_KEY_RECOVERY = 10661;

  // =============================
  // *** Organization Commands ***
  // =============================

  // Get My Organization
  public static final int ORG_Q_GET_ORG = 1700;
  public static final int ORG_A_GET_ORG = 10700;

  // =======================
  // *** InvEml Commands ***
  // =======================

  // Remove My InvEmls
  public static final int INV_Q_REMOVE = 2100;

  // =============================
  // *** System level messages ***
  // =============================

  // Ping-Pong
  public static final int SYS_Q_PING = 5000;
  public static final int SYS_A_PONG = 5001;

  // No-op, sometimes used to release the Writer-Reader worker pair
  // when no reply to a request is available.
  public static final int SYS_A_NOOP = 5002;

  // Display a message to user
  public static final int SYS_A_MSG = 5003;

  // Server replies with array of reply-sets
  public static final int SYS_A_REPLY_DATA_SETS = 5004;

  public static final int SYS_Q_VERSION = 5005;
  public static final int SYS_A_VERSION = 5006;

  // Registration for inter-user notifications
  public static final int SYS_Q_NOTIFY = 5010;
  public static final int SYS_A_NOTIFY = 5011;

  // Connection timeout
  public static final int SYS_A_CONNECTION_TIMEOUT = 5020;
  public static final int SYS_E_BANDWIDTH_EXCEEDED = 5030;
  public static final int SYS_E_ACCOUNT_EXPIRED = 5040;
  public static final int SYS_E_STORAGE_EXCEEDED = 5050;

  // Auto Update
  public static final int SYS_Q_GET_AUTO_UPDATE = 5060;
  public static final int SYS_A_GET_AUTO_UPDATE = 5061;

  // Get Temporary Public Key
  public static final int SYS_Q_GET_TEMP_PUB_KEY = 5070;
  public static final int SYS_A_GET_TEMP_PUB_KEY = 5071;

  // System Login used in AdminMenu
  public static final int SYS_Q_LOGIN = 5080;
  public static final int SYS_A_LOGIN = 5081;
  public static final int SYS_E_LOGIN = 5082;

  // System Query
  public static final int SYS_Q_QUERY = 5090;
  public static final int SYS_A_QUERY = 5091;

  // Message Distribution request
  public static final int SYSNET_Q_DISTRIBUTE = 6010;
  public static final int SYSENG_A_DISTRIBUTED = 6011;
  // Other distribution requests
  public static final int SYSENG_Q_LOGIN = 6020;
  public static final int SYSNET_A_LOGIN = 6021;
  public static final int SYSNET_A_LOGIN_FAILED = 6022;
  public static final int SYSENG_Q_SET_USERS = 6030;
  public static final int SYSNET_A_SET_USERS = 6031;
  public static final int SYSENG_Q_ADD_USER = 6040;
  public static final int SYSNET_A_ADD_USER = 6041;
  public static final int SYSENG_Q_REMOVE_USER = 6050;
  public static final int SYSNET_A_REMOVE_USER = 6051;
  public static final int SYSENG_Q_SERVER_AVAILABLE = 6060;
  public static final int SYSNET_A_SERVER_AVAILABLE = 6061;
  public static final int SYSENG_Q_CONSOLE_COMMAND = 6070;
  public static final int SYSNET_A_CONSOLE_COMMAND = 6071;
  public static final int SYSENG_Q_TOKEN = 6080;
  //public static final int SYSNET_A_TOKEN = 6081; // response to a token pass is not used
  public static final int SYSENG_Q_LICENSE_UPDATED = 6090;
  public static final int SYSNET_A_LICENSE_UPDATED = 6091;

  // =====================
  // *** System Errors ***
  // =====================

  public static final int SYS_E_EXCEPTION = -100;


  public static boolean isFileTransferCode(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommandCodes.class, "isFileTransferCode(int code)");
    if (trace != null) trace.args(code);
    boolean rc = (code == FILE_Q_NEW_FILES || code == FILE_A_GET_FILES_DATA || code == FILE_Q_GET_FILES_DATA);
    if (trace != null) trace.exit(CommandCodes.class, rc);
    return rc;
  }

  public static boolean isStreamingIncomingMessage(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommandCodes.class, "isStreamingIncomingMessage(int code)");
    if (trace != null) trace.args(code);
    boolean rc = (code == FILE_Q_NEW_FILES);
    if (trace != null) trace.exit(CommandCodes.class, rc);
    return rc;
  }
  public static boolean isStreamingOutgoingMessage(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommandCodes.class, "isStreamingOutgoingMessage(int code)");
    if (trace != null) trace.args(code);
    boolean rc = (code == FILE_A_GET_FILES_DATA);
    if (trace != null) trace.exit(CommandCodes.class, rc);
    return rc;
  }
  public static boolean isStreamingMessage(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommandCodes.class, "isStreamingMessage(int code)");
    if (trace != null) trace.args(code);
    boolean rc = (isStreamingIncomingMessage(code) || isStreamingOutgoingMessage(code));
    if (trace != null) trace.exit(CommandCodes.class, rc);
    return rc;
  }
  public static boolean isCodeForShortenStatusNotification(int code) {
    boolean shortenStatus = false;
    switch (code) {
      case SYS_Q_PING :
      case SYS_A_PONG :
      case SYS_A_NOOP :
      case SYS_A_MSG :
      case SYS_Q_NOTIFY :
      case SYS_A_NOTIFY :
      case SYS_A_CONNECTION_TIMEOUT :
      case SYS_E_BANDWIDTH_EXCEEDED :
      case SYS_E_ACCOUNT_EXPIRED :
      case SYS_E_STORAGE_EXCEEDED :
      case MSG_Q_TYPING :
      case MSG_A_TYPING :
      case STAT_Q_GET :
      case STAT_A_GET :
        shortenStatus = true;
        break;
    }
    return shortenStatus;
  }

}