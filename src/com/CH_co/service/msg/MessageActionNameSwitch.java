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

package com.CH_co.service.msg;

import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.44 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MessageActionNameSwitch extends Object {

  private static Switch_StrInt_Comparator codeComparator = new Switch_StrInt_Comparator(false, false);
  private static Object[][] actionInfoNames = null;

  static {
    actionInfoNames = new Object[][] {

      // ===========================
      // *** System ASCII Checks ***
      // ===========================

      { Integer.valueOf(CommandCodes.SYS_Q_CHECK), "System ASCII Check" },
      { Integer.valueOf(CommandCodes.SYS_A_CHECK), "System ASCII Check" },

      // =====================
      // *** User Commands ***
      // =====================

      // Login Secure Session
      { Integer.valueOf(CommandCodes.USR_Q_LOGIN_SECURE_SESSION), "Open a Secure Channel and Login" },
      // Get Login Info
      { Integer.valueOf(CommandCodes.USR_Q_GET_LOGIN_INFO), "Get Login Data" },
      // Get Re-Connection Update
      { Integer.valueOf(CommandCodes.USR_Q_GET_RECONNECT_UPDATE), "Get Re-Connection Update" },
      // Get My Info
      { Integer.valueOf(CommandCodes.USR_Q_GET_INFO), "Retrieve Account Information" },
      // Get Sub-Account Info
      { Integer.valueOf(CommandCodes.USR_Q_GET_SUB_ACCOUNTS), "Get Account Information" },
      // Create New User
      { Integer.valueOf(CommandCodes.USR_Q_NEW_USER), "Create New User Account" },
      // Create New Sub-User Accounts
      { Integer.valueOf(CommandCodes.USR_Q_NEW_SUB), "Create New User Account" },
      // Alter User Data
      { Integer.valueOf(CommandCodes.USR_Q_ALTER_DATA), "Alter User Data" },
      // Alter User Password
      { Integer.valueOf(CommandCodes.USR_Q_ALTER_PASSWORD), "Change Credentials" },
      // Setup Password Recovery
      { Integer.valueOf(CommandCodes.USR_Q_PASS_RECOVERY_UPDATE), "Setup Password Recovery" },
      // Get Challenge Questions
      { Integer.valueOf(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE), "Get Challenge Questions" },
      // Get Password Recovery Data
      { Integer.valueOf(CommandCodes.USR_Q_PASS_RECOVERY_GET_COMPLETE), "Get Password Recovery Data" },
      // Change Online Status
      { Integer.valueOf(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS), "Change Online Status" },
      // Delete Account
      { Integer.valueOf(CommandCodes.USR_Q_DELETE), "Delete Account" },
      // Password Reset
      { Integer.valueOf(CommandCodes.USR_Q_PASSWORD_RESET), "Password Reset" },
      // Logout
      { Integer.valueOf(CommandCodes.USR_Q_LOGOUT), "Logout" },
      // Client session recycling
      { Integer.valueOf(CommandCodes.USR_Q_RECYCLE_SESSION_SEQUENCE), "Re-connect initiated" },
      // Delete an account
      { Integer.valueOf(CommandCodes.USR_Q_REMOVE), "Remove User Account" },
      // Change status of account (Active/Suspended)
      { Integer.valueOf(CommandCodes.USR_Q_CHANGE_STATUS), "Change Account Status" },
      // Get User Handles
      { Integer.valueOf(CommandCodes.USR_Q_GET_HANDLES), "Get User Handles" },
      // Search for Users
      { Integer.valueOf(CommandCodes.USR_Q_SEARCH), "Search For Users" },
      // Check for availability of User Name
      { Integer.valueOf(CommandCodes.USR_Q_CHECK_AVAIL), "Check availability of Username" },
      // Send Email Invitation
      { Integer.valueOf(CommandCodes.USR_Q_SEND_EMAIL_INVITATION), "Send Email Invitation" },
      // Get Accessors
      { Integer.valueOf(CommandCodes.USR_Q_GET_ACCESSORS), "Track Accessors" },
      // Calculate Cummulative Usage
      { Integer.valueOf(CommandCodes.USR_Q_CUMULATIVE_USAGE), "Calculate Usage" },
      // Apply Code
      { Integer.valueOf(CommandCodes.USR_Q_APPLY_CODE), "Apply Code" },
      // Error: Handle already taken
      { Integer.valueOf(CommandCodes.USR_E_HANDLE_ALREADY_TAKEN), "Error: Username already taken" },


      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { Integer.valueOf(CommandCodes.EML_Q_GET), "Get Email Address" },
      // Get Available Domains
      { Integer.valueOf(CommandCodes.EML_Q_GET_DOMAINS), "Get Available Email Domains" },
      // Create Emails 
      { Integer.valueOf(CommandCodes.EML_Q_CREATE), "Create Email Address" },
      // Alter Emails 
      { Integer.valueOf(CommandCodes.EML_Q_ALTER), "Alter Email Address" },
      // Manage Emails 
      { Integer.valueOf(CommandCodes.EML_Q_MANAGE), "Manage Email Addresses" },
      // Remove Emails 
      { Integer.valueOf(CommandCodes.EML_Q_REMOVE), "Remove Email Address" },
      // Check for availability of Email Address
      { Integer.valueOf(CommandCodes.EML_Q_CHECK_AVAIL), "Check availability of Email Address" },
      // Lookup email addresses
      { Integer.valueOf(CommandCodes.EML_Q_LOOKUP_ADDR), "Lookup email address" },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { Integer.valueOf(CommandCodes.ADDR_Q_FIND_HASH), "Find Email Address in Address Book" },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get All My Folders
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDERS), "Get All Folders" },
      // Get My Child Folders
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDERS_CHILDREN), "Get My Child Folders" },
      // Get My Specified Folders
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDERS_SOME), "Get My Specified Folders" },
      // Get My Root Folders
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDERS_ROOTS), "Get My Root Folders" },
      // New Private Folder
      { Integer.valueOf(CommandCodes.FLD_Q_NEW_FOLDER), "Create Folder" },
      // New Private Folder Or Get Old
      { Integer.valueOf(CommandCodes.FLD_Q_NEW_OR_GET_OLD), "Prepare Chat Log" },
      // New Private Default Address Folder
      { Integer.valueOf(CommandCodes.FLD_Q_NEW_DFT_ADDRESS_OR_GET_OLD), "Prepare Address Book Folder" },
      // New Private Default Draft Folder
      { Integer.valueOf(CommandCodes.FLD_Q_NEW_DFT_DRAFT_OR_GET_OLD), "Prepare Draft Folder" },
      // New Private Default Junk Folder
      { Integer.valueOf(CommandCodes.FLD_Q_NEW_DFT_JUNK_OR_GET_OLD), "Prepare Spam Folder" }, // "Junk email"
      // Alter Folder Attributes
      { Integer.valueOf(CommandCodes.FLD_Q_ALTER_FLD_ATTR), "Alter Folder Attributes" },
      // Alter Share Strings
      { Integer.valueOf(CommandCodes.FLD_Q_ALTER_STRS), "Alter Folder Strings" },
      // To Symmetric Encryption
      { Integer.valueOf(CommandCodes.FLD_Q_TO_SYM_ENC), "Recrypt New Folder Shares" },
      // Alter Share Permissions
      { Integer.valueOf(CommandCodes.FLD_Q_ALTER_PERMISSIONS), "Alter Folder Permissions" },
      // Move Folder
      { Integer.valueOf(CommandCodes.FLD_Q_MOVE_FOLDER), "Move Folder" },
      // Transfer Ownership
      { Integer.valueOf(CommandCodes.FLD_Q_TRANSFER_OWNERSHIP), "Transfer Folder Ownership" },
      // Remove Folder
      { Integer.valueOf(CommandCodes.FLD_Q_REMOVE_FOLDER), "Remove Folder" },
      // Remove Folder Shares
      { Integer.valueOf(CommandCodes.FLD_Q_REMOVE_FOLDER_SHARES), "Remove Folder Shares" },
      // Get Folder Shares
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDER_SHARES), "Get Folder Shares" },
      // Add Folder Shares
      { Integer.valueOf(CommandCodes.FLD_Q_ADD_FOLDER_SHARES), "Add Folder Shares" },
      // Get Number of Files and Size of Files in the Folder
      { Integer.valueOf(CommandCodes.FLD_Q_GET_FOLDER_SIZE), "Get Folder Size" },
      // Get Number of Files/Messages and Folders, and number of bytes stored and used on disk
      { Integer.valueOf(CommandCodes.FLD_Q_GET_SIZE_SUMMARY), "Get Folder Size Summary" },
      // Get All Access Users for specified Folder IDs
      { Integer.valueOf(CommandCodes.FLD_Q_GET_ACCESS_USERS), "Fetch Group Members" },
      // Get Folder Red Flag Count
      { Integer.valueOf(CommandCodes.FLD_Q_RED_FLAG_COUNT), "Count New Objects for Folder(s)" },
      // Ring Ring
      { Integer.valueOf(CommandCodes.FLD_Q_RING_RING), "Ringing..." },


      // =====================
      // *** File Commands ***
      // =====================

      // New File
      { Integer.valueOf(CommandCodes.FILE_Q_NEW_FILES), "Create File" },
      // Get Files
      { Integer.valueOf(CommandCodes.FILE_Q_GET_FILES), "Get File Listing" },
      { Integer.valueOf(CommandCodes.FILE_Q_GET_FILES_STAGED), "Get File Listing" },
      // Get Msg File Attachments
      { Integer.valueOf(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS), "Get Message File Attachment(s)" },
      // Get File Data Attributes
      { Integer.valueOf(CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES), "Get File Attributes" },
      // Get File Data 
      { Integer.valueOf(CommandCodes.FILE_Q_GET_FILES_DATA), "Get File Data" },
      // Remove Files
      { Integer.valueOf(CommandCodes.FILE_Q_REMOVE_FILES), "Remove File(s)" },
      // Move Files
      { Integer.valueOf(CommandCodes.FILE_Q_MOVE_FILES), "Move File(s)" },
      // Copy Files
      { Integer.valueOf(CommandCodes.FILE_Q_COPY_FILES), "Copy File(s)" },
      // Rename File
      { Integer.valueOf(CommandCodes.FILE_Q_RENAME), "Rename File" },
      // Save Msg File Attachments
      { Integer.valueOf(CommandCodes.FILE_Q_SAVE_MSG_FILE_ATT), "Save Message File Attachment(s)" },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_GET_CONTACTS), "Get Contacts" },
      // Get My group account Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_GET_GROUP_CONTACTS), "Get group account Contacts" },
      // Set My group Contacts, includes create/change/delete
      { Integer.valueOf(CommandCodes.CNT_Q_SET_GROUP_CONTACTS), "Set group account Contacts" },
      // New Contact
      { Integer.valueOf(CommandCodes.CNT_Q_NEW_CONTACT), "Create Contact" },
      // New Sub-user Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_NEW_SUB_CONTACTS), "Create Contacts" },
      // Accept Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_ACCEPT_CONTACTS), "Accept Contact(s)" },
      // Decline Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_DECLINE_CONTACTS), "Decline Contact(s)" },
      // Acknowledge Contacts
      //Notes:  Once contact is acknowledged, the final encrypted name and description are given.
      { Integer.valueOf(CommandCodes.CNT_Q_ACKNOWLEDGE_CONTACTS), "Acknowledge Contact(s)" },
      // Move Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_MOVE_CONTACTS), "Move Contact(s)" },
      // Remove Contacts
      { Integer.valueOf(CommandCodes.CNT_Q_REMOVE_CONTACTS), "Remove Contact(s)" },
      // Rename My Contact
      { Integer.valueOf(CommandCodes.CNT_Q_RENAME_MY_CONTACT), "Rename Contact" },
      // Rename Contacts With Me
      { Integer.valueOf(CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME), "Alter Contact(s)" },
      // Alter Permission
      { Integer.valueOf(CommandCodes.CNT_Q_ALTER_PERMITS), "Alter Contact Permissions" },
      // Alter Settings
      { Integer.valueOf(CommandCodes.CNT_Q_ALTER_SETTINGS), "Alter Contact Settings" },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full
      { Integer.valueOf(CommandCodes.MSG_Q_GET_FULL), "Fetch Message(s)" },
      // Get Messages Briefs
      { Integer.valueOf(CommandCodes.MSG_Q_GET_BRIEFS), "Fetch Message Briefs" },
      // Get Msg Attachments Briefs
      { Integer.valueOf(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS), "Fetch Message Attachment Briefs" },
      // Get Message Body
      { Integer.valueOf(CommandCodes.MSG_Q_GET_BODY), "Fetch Message Body" },
      // Get Single Full Message
      { Integer.valueOf(CommandCodes.MSG_Q_GET_MSG), "Fetch Message" },
      // New Message
      { Integer.valueOf(CommandCodes.MSG_Q_NEW), "Create New Message" },
      // Convert message link to symmetric encryption
      { Integer.valueOf(CommandCodes.MSG_Q_TO_SYM_ENC), "Change Message Encryption to Symmetric" },
      // Remove Messages
      { Integer.valueOf(CommandCodes.MSG_Q_REMOVE), "Remove Message(s)" },
      // Remove Old Messages
      { Integer.valueOf(CommandCodes.MSG_Q_REMOVE_OLD), "Remove Old Message(s)" },
      // Move Messages
      { Integer.valueOf(CommandCodes.MSG_Q_MOVE), "Move Message(s)" },
      // Copy Messages
      { Integer.valueOf(CommandCodes.MSG_Q_COPY), "Copy Message(s)" },
      // Save Message Attachments
      { Integer.valueOf(CommandCodes.MSG_Q_SAVE_MSG_ATT), "Save Message Attachment(s)" },
      // Notify participants about typing a message
      { Integer.valueOf(CommandCodes.MSG_Q_TYPING), "Typing message" },
      // Expiry Revocation
      { Integer.valueOf(CommandCodes.MSG_Q_EXPIRY), "Change Expiry or Revocation" },
      // Update Status
      { Integer.valueOf(CommandCodes.MSG_Q_UPDATE_STATUS), "Update Status" },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { Integer.valueOf(CommandCodes.KEY_Q_GET_KEY_PAIRS), "Load Key Pairs" },
      // Get Public Keys For Users
      { Integer.valueOf(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS), "Get Public Keys" },
      // Get Public Key By IDs
      { Integer.valueOf(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS), "Get Public Keys" },
      // New Key Pair
      { Integer.valueOf(CommandCodes.KEY_Q_NEW_KEY_PAIR), "New Key Pair" },
      // Remove Key Pairs
      { Integer.valueOf(CommandCodes.KEY_Q_REMOVE_KEY_PAIRS), "Remove Key Pair" },
      // Set Key Recovery / Enable Password Reset
      { Integer.valueOf(CommandCodes.KEY_Q_SET_KEY_RECOVERY), "Enable Password Reset" },
      // Get Key Recovery settings
      { Integer.valueOf(CommandCodes.KEY_Q_GET_KEY_RECOVERY), "Get Password Reset settings" },
      { Integer.valueOf(CommandCodes.KEY_A_GET_KEY_RECOVERY), "Get Password Reset settings" },


      // =======================
      // *** InvEml Commands ***
      // =======================

      // Remove My InvEmls
      { Integer.valueOf(CommandCodes.INV_Q_REMOVE), "Remove Invitation" },


      // =====================
      // *** Stat Commands ***
      // =====================
      { Integer.valueOf(CommandCodes.STAT_Q_GET), "Get Object Statistics" },
      { Integer.valueOf(CommandCodes.STAT_Q_UPDATE), "Update Object Statistics" },
      // Fetch All Object Stats
      { Integer.valueOf(CommandCodes.STAT_Q_FETCH_ALL_OBJ_STATS), "Fetch Access History and Privileges" },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      { Integer.valueOf(CommandCodes.ORG_Q_GET_ORG), "Get Organization Structure" },

      // =============================
      // *** System level messages ***
      // =============================

      // Ping-Pong
      { Integer.valueOf(CommandCodes.SYS_Q_PING), "Keep Alive" },
      // Client provides Version info and requests Server Version info reply
      { Integer.valueOf(CommandCodes.SYS_Q_VERSION), "Version Info" },

      { Integer.valueOf(CommandCodes.SYS_Q_NOTIFY), "Register Persistant Connection" },

      { Integer.valueOf(CommandCodes.SYS_Q_GET_AUTO_UPDATE), "Auto Update" },

      // Get Temporary Public Key
      { Integer.valueOf(CommandCodes.SYS_Q_GET_TEMP_PUB_KEY), "Get Public Key" },

      // System Login
      { Integer.valueOf(CommandCodes.SYS_Q_LOGIN), "Open a Secure Channel" },

      // System Query
      { Integer.valueOf(CommandCodes.SYS_Q_QUERY), "Query" },

      // =====================
      // *** User Commands ***
      // =====================


      // Login Secure Session
      { Integer.valueOf(CommandCodes.USR_A_LOGIN_SECURE_SESSION), "Open a Secure Channel and Login" },

      // Login failed -- 
      { Integer.valueOf(CommandCodes.USR_E_HANDLE_PASSWORD_COMBO_DNE), "User Handle-Password pair does not exist" },
      { Integer.valueOf(CommandCodes.USR_E_USER_LOCKED_OUT), "User Locked Out" },
      { Integer.valueOf(CommandCodes.USR_E_LOGIN_FAILED), "Login Failed" },
      // Get My Info
      { Integer.valueOf(CommandCodes.USR_A_GET_INFO), "Retrieve Account Information" },
      // Get Sub-Account Info
      { Integer.valueOf(CommandCodes.USR_A_GET_SUB_ACCOUNTS), "Get Account Information" },
      // Create New User
      { Integer.valueOf(CommandCodes.USR_A_NEW_USER), "Create New User Account" },
      // Alter User Password
      { Integer.valueOf(CommandCodes.USR_A_ALTER_PASSWORD), "Change Credentials" },
      // Get Challenge Questions
      { Integer.valueOf(CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE), "Get Challenge Questions" },
      // Get Password Recovery Data
      { Integer.valueOf(CommandCodes.USR_A_PASS_RECOVERY_GET_COMPLETE), "Get Password Recovery Data" },
      // Delete Account
      { Integer.valueOf(CommandCodes.USR_A_DELETE), "Delete Account" },
      // Logout
      { Integer.valueOf(CommandCodes.USR_A_LOGOUT), "Logout" },
      // System asking for session recycle
      { Integer.valueOf(CommandCodes.USR_A_RECYCLE_SESSION_REQUEST), "Re-connect request" },
      // Client session recycling
      { Integer.valueOf(CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE), "Re-connect confirmed" },
      // Delete an account
      { Integer.valueOf(CommandCodes.USR_A_REMOVE), "Remove User Account" },
      // User suspended
      { Integer.valueOf(CommandCodes.USR_A_SUSPENDED), "Account Suspended" },
      // Get User Handles
      { Integer.valueOf(CommandCodes.USR_A_GET_HANDLES), "Get User Handles" },
      // Calculate Cummulative Usage
      { Integer.valueOf(CommandCodes.USR_A_CUMULATIVE_USAGE), "Calculate Usage" },

      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { Integer.valueOf(CommandCodes.EML_A_GET), "Get Email Address" },
      // Get Available Domains
      { Integer.valueOf(CommandCodes.EML_A_GET_DOMAINS), "Get Available Email Domains" },
      // Remove Emails
      { Integer.valueOf(CommandCodes.EML_A_REMOVE), "Remove Email Address" },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { Integer.valueOf(CommandCodes.ADDR_A_FOUND_HASH), "Find Email Address in Address Book" },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get My Folders
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS), "Get Folders" },
      // Get My Child Folders
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS_CHILDREN), "Get My Child Folders" },
      // Get My Root Folders
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS_ROOTS), "Get My Root Folders" },
      // Remove Folder
      { Integer.valueOf(CommandCodes.FLD_A_REMOVE_FOLDER), "Remove Folders" },
      // Get Folder Size
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDER_SIZE), "Get Folder Size" },
      // Get Folder Red Flag Count
      { Integer.valueOf(CommandCodes.FLD_A_RED_FLAG_COUNT), "Count New Objects for Folder(s)" },
      // Ring Ring
      { Integer.valueOf(CommandCodes.FLD_A_RING_RING), "Ring, ring..." },
      // Folder Errors
      { Integer.valueOf(CommandCodes.FLD_E_CANNOT_REMOVE_SUPER_ROOT_FOLDER), "Cannot remove super root folder" },
      { Integer.valueOf(CommandCodes.FLD_E_FOLDER_DNE_OR_NOT_YOURS), "Folder does not exist or is not yours" },
      { Integer.valueOf(CommandCodes.FLD_E_ILLEGAL_FOLDER_MOVE), "Illegal folder move" },
      { Integer.valueOf(CommandCodes.FLD_E_PARENT_FOLDER_DNE_OR_NOT_YOURS), "Parent folder does not exist or is not yours" },


      // =====================
      // *** File Commands ***
      // =====================

      // New File
      // Get Files
      // Move Files
      // Copy Files
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES), "Get File List" },
      // Get File Data Attributes
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES_DATA_ATTRIBUTES), "Get File Attributes" },
      // Get File Data 
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES_DATA), "Get File Data" },
      // Remove Files
      { Integer.valueOf(CommandCodes.FILE_A_REMOVE_FILES), "Remove File(s)" },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { Integer.valueOf(CommandCodes.CNT_A_GET_CONTACTS), "Get Contact List" },
      // Remove Contacts
      { Integer.valueOf(CommandCodes.CNT_A_REMOVE_CONTACTS), "Remove Contact(s)" },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full or Briefs
      { Integer.valueOf(CommandCodes.MSG_A_GET), "Fetch Message(s)" },
      // Get Message Body
      { Integer.valueOf(CommandCodes.MSG_A_GET_BODY), "Fetch Message Body" },
      // Remove Message(s)
      { Integer.valueOf(CommandCodes.MSG_A_REMOVE), "Remove Message(s)" },
      // Notify participants about typing a message
      { Integer.valueOf(CommandCodes.MSG_A_TYPING), "Typing message" },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { Integer.valueOf(CommandCodes.KEY_A_GET_KEY_PAIRS), "Load Key Pairs" },
      // Get Public Keys For Users
      { Integer.valueOf(CommandCodes.KEY_A_GET_PUBLIC_KEYS), "Get Public Key(s)" },
      // Remove Key Pairs
      { Integer.valueOf(CommandCodes.KEY_A_REMOVE_KEY_PAIRS), "Remove Key Pair(s)" },

      // =====================
      // *** Stat Commands ***
      // =====================
      { Integer.valueOf(CommandCodes.STAT_A_GET), "Get Object Statistics" },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      { Integer.valueOf(CommandCodes.ORG_A_GET_ORG), "Get Organization Structure" },


      // =============================
      // *** System level messages ***
      // =============================


      // System Commands
      { Integer.valueOf(CommandCodes.SYS_A_PONG), "Keep Alive" },
      // No-op, sometimes used to release the Writer-Reader worker pair 
      // when no reply to a request is available.
      { Integer.valueOf(CommandCodes.SYS_A_NOOP), "Confirmation" },
      // Display a message to user
      { Integer.valueOf(CommandCodes.SYS_A_MSG), "System Message" },
      // Server replies with array of reply-sets
      { Integer.valueOf(CommandCodes.SYS_A_REPLY_DATA_SETS), "Reply Data Sets" },
      // Server replies with Version information
      { Integer.valueOf(CommandCodes.SYS_A_VERSION), "Version Info" },

      { Integer.valueOf(CommandCodes.SYS_A_NOTIFY), "Register Persistant Connection Reply" },

      // Connection timeout
      { Integer.valueOf(CommandCodes.SYS_A_CONNECTION_TIMEOUT), "Connection Timeout" },
      { Integer.valueOf(CommandCodes.SYS_E_BANDWIDTH_EXCEEDED), "Error: Bandwidth Limit Exceeded" },
      { Integer.valueOf(CommandCodes.SYS_E_ACCOUNT_EXPIRED), "Error: Account Expired" },
      { Integer.valueOf(CommandCodes.SYS_E_STORAGE_EXCEEDED), "Error: Storage Limit Exceeded" },

      { Integer.valueOf(CommandCodes.SYS_A_GET_AUTO_UPDATE), "Auto Update" },

      // Get Temporary Public Key
      { Integer.valueOf(CommandCodes.SYS_A_GET_TEMP_PUB_KEY), "Get Public Key" },

      // System Login
      { Integer.valueOf(CommandCodes.SYS_A_LOGIN), "Open a Secure Channel" },
      { Integer.valueOf(CommandCodes.SYS_E_LOGIN), "Open a Secure Channel Failed" },

      // System Query
      { Integer.valueOf(CommandCodes.SYS_A_QUERY), "Query" },

      { Integer.valueOf(CommandCodes.SYSNET_Q_DISTRIBUTE), "SYSNET_Q_DISTRIBUTE" },
      { Integer.valueOf(CommandCodes.SYSENG_A_DISTRIBUTED), "SYSENG_A_DISTRIBUTED" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_LOGIN), "SYSENG_Q_LOGIN" },
      { Integer.valueOf(CommandCodes.SYSNET_A_LOGIN), "SYSNET_A_LOGIN" },
      { Integer.valueOf(CommandCodes.SYSNET_A_LOGIN_FAILED), "SYSNET_A_LOGIN_FAILED" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_SET_USERS), "SYSENG_Q_SET_USERS" },
      { Integer.valueOf(CommandCodes.SYSNET_A_SET_USERS), "SYSNET_A_SET_USERS" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_ADD_USER), "SYSENG_Q_ADD_USER" },
      { Integer.valueOf(CommandCodes.SYSNET_A_ADD_USER), "SYSNET_A_ADD_USER" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_REMOVE_USER), "SYSENG_Q_REMOVE_USER" },
      { Integer.valueOf(CommandCodes.SYSNET_A_REMOVE_USER), "SYSNET_A_REMOVE_USER" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_SERVER_AVAILABLE), "SYSENG_Q_SERVER_AVAILABLE" },
      { Integer.valueOf(CommandCodes.SYSNET_A_SERVER_AVAILABLE), "SYSNET_A_SERVER_AVAILABLE" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_CONSOLE_COMMAND), "SYSENG_Q_CONSOLE_COMMAND" },
      { Integer.valueOf(CommandCodes.SYSNET_A_CONSOLE_COMMAND), "SYSNET_A_CONSOLE_COMMAND" },
      { Integer.valueOf(CommandCodes.SYSENG_Q_TOKEN), "SYSENG_Q_TOKEN" },

      // =====================
      // *** System Errors ***
      // =====================
      { Integer.valueOf(CommandCodes.SYS_E_EXCEPTION), "System Exception" }
    };

    Arrays.sort(actionInfoNames, codeComparator);
  } // end static


  public static String getActionInfoName(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageActionNameSwitch.class, "getActionInfoName(int code)");
    if (trace != null) trace.args(code);

    String actionInfoName = null;

    int index = Arrays.binarySearch(actionInfoNames, Integer.valueOf(code), codeComparator);
    if (index >= 0)
      actionInfoName = (String) actionInfoNames[index][1];
    else 
      actionInfoName = "Internet connection quality is poor. Please retry in a little while, code="+code+".";

    if (actionInfoName.startsWith("com.CH_")) {
      if (trace != null) trace.data(90, actionInfoName);
      actionInfoName = Misc.getClassNameWithoutPackage(actionInfoName);
      if (trace != null) trace.data(91, actionInfoName);
    }

    if (trace != null) trace.exit(MessageActionNameSwitch.class, actionInfoName);
    return actionInfoName;
  }

}