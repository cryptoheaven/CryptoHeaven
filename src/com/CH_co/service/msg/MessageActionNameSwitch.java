/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.service.msg;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import java.util.Arrays;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
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

      { new Integer(CommandCodes.SYS_Q_CHECK), "System ASCII Check" },
      { new Integer(CommandCodes.SYS_A_CHECK), "System ASCII Check" },

      // =====================
      // *** User Commands ***
      // =====================

      // Login Secure Session
      { new Integer(CommandCodes.USR_Q_LOGIN_SECURE_SESSION), "Open Secure Channel" },
      // Get Login Info
      { new Integer(CommandCodes.USR_Q_GET_LOGIN_INFO), "Get Login Data" },
      // Get Initial Data -- for loading client application views
      { new Integer(CommandCodes.USR_Q_GET_INIT_DATA), "Get Initial Data" },
      // Get My Info
      { new Integer(CommandCodes.USR_Q_GET_INFO), "Retrieve Account" },
      // Get Sub-Account Info
      { new Integer(CommandCodes.USR_Q_GET_SUB_ACCOUNTS), "Get Account Information" },
      // Create New User
      { new Integer(CommandCodes.USR_Q_NEW_USER), "Create New User Account" },
      // Create New Sub-User Accounts
      { new Integer(CommandCodes.USR_Q_NEW_SUB), "Create New User Account" },
      // Alter User Data
      { new Integer(CommandCodes.USR_Q_ALTER_DATA), "Alter User Data" },
      // Alter User Password
      { new Integer(CommandCodes.USR_Q_ALTER_PASSWORD), "Change Credentials" },
      // Setup Password Recovery
      { new Integer(CommandCodes.USR_Q_PASS_RECOVERY_UPDATE), "Setup Password Recovery" },
      // Get Challenge Questions
      { new Integer(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE), "Get Challenge Questions" },
      // Get Password Recovery Data
      { new Integer(CommandCodes.USR_Q_PASS_RECOVERY_GET_COMPLETE), "Get Password Recovery Data" },
      // Change Online Status
      { new Integer(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS), "Change Online Status" },
      // Delete Account
      { new Integer(CommandCodes.USR_Q_DELETE), "Delete Account" },
      // Password Reset
      { new Integer(CommandCodes.USR_Q_PASSWORD_RESET), "Password Reset" },
      // Logout
      { new Integer(CommandCodes.USR_Q_LOGOUT), "Logout" },
      // Client session recycling
      { new Integer(CommandCodes.USR_Q_RECYCLE_SESSION_SEQUENCE), "Re-connect initiated" },
      // Delete an account
      { new Integer(CommandCodes.USR_Q_REMOVE), "Remove User Account" },
      // Change status of account (Active/Suspended)
      { new Integer(CommandCodes.USR_Q_CHANGE_STATUS), "Change Account Status" },
      // Get User Handles
      { new Integer(CommandCodes.USR_Q_GET_HANDLES), "Get User Handles" },
      // Search for Users
      { new Integer(CommandCodes.USR_Q_SEARCH), "Search For Users" },
      // Check for availability of User Name
      { new Integer(CommandCodes.USR_Q_CHECK_AVAIL), "Check availability of Username" },
      // Send Email Invitation
      { new Integer(CommandCodes.USR_Q_SEND_EMAIL_INVITATION), "Send Email Invitation" },
      // Get Accessors
      { new Integer(CommandCodes.USR_Q_GET_ACCESSORS), "Track Accessors" },
      // Calculate Cummulative Usage
      { new Integer(CommandCodes.USR_Q_CUMULATIVE_USAGE), "Calculate Usage" },
      // Apply Code
      { new Integer(CommandCodes.USR_Q_APPLY_CODE), "Apply Code" },
      // Recalculate Storage
      { new Integer(CommandCodes.USR_Q_RECALCULATE_STORAGE), "Recalculate Storage" },
      // Error: Handle already taken
      { new Integer(CommandCodes.USR_E_HANDLE_ALREADY_TAKEN), "Error: Username already taken" },


      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { new Integer(CommandCodes.EML_Q_GET), "Get Email Address" },
      // Get Available Domains
      { new Integer(CommandCodes.EML_Q_GET_DOMAINS), "Get Available Email Domains" },
      // Create Emails
      { new Integer(CommandCodes.EML_Q_CREATE), "Create Email Address" },
      // Alter Emails
      { new Integer(CommandCodes.EML_Q_ALTER), "Alter Email Address" },
      // Manage Emails
      { new Integer(CommandCodes.EML_Q_MANAGE), "Manage Email Addresses" },
      // Remove Emails
      { new Integer(CommandCodes.EML_Q_REMOVE), "Remove Email Address" },
      // Check for availability of Email Address
      { new Integer(CommandCodes.EML_Q_CHECK_AVAIL), "Check Availability" },
      // Lookup email addresses
      { new Integer(CommandCodes.EML_Q_LOOKUP_ADDR), "Lookup email address" },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { new Integer(CommandCodes.ADDR_Q_FIND_HASH), "Find Address in Book" },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get All My Folders
      { new Integer(CommandCodes.FLD_Q_GET_FOLDERS), "Get All Folders" },
      // Get My Child Folders
      { new Integer(CommandCodes.FLD_Q_GET_FOLDERS_CHILDREN), "Get My Child Folders" },
      // Get My Specified Folders
      { new Integer(CommandCodes.FLD_Q_GET_FOLDERS_SOME), "Get My Specified Folders" },
      // Get My Root Folders
      { new Integer(CommandCodes.FLD_Q_GET_FOLDERS_ROOTS), "Get My Root Folders" },
      // New Private Folder
      { new Integer(CommandCodes.FLD_Q_NEW_FOLDER), "Create Folder" },
      // New Private Folder Or Get Old
      { new Integer(CommandCodes.FLD_Q_NEW_OR_GET_OLD), "Prepare Chat Log" },
      // New Private Default Address Folder
      { new Integer(CommandCodes.FLD_Q_NEW_DFT_ADDRESS_OR_GET_OLD), "Prepare Address Book Folder" },
      // New Private Default Draft Folder
      { new Integer(CommandCodes.FLD_Q_NEW_DFT_DRAFT_OR_GET_OLD), "Prepare Draft Folder" },
      // New Private Default Junk Folder
      { new Integer(CommandCodes.FLD_Q_NEW_DFT_JUNK_OR_GET_OLD), "Prepare Spam Folder" }, // "Junk email"
      // New Private Default Recycle Folder
      { new Integer(CommandCodes.FLD_Q_NEW_DFT_RECYCLE_OR_GET_OLD), "Prepare Recycle Folder" },
      // Alter Folder Attributes
      { new Integer(CommandCodes.FLD_Q_ALTER_FLD_ATTR), "Alter Folder Attributes" },
      // Alter Share Strings
      { new Integer(CommandCodes.FLD_Q_ALTER_STRS), "Alter Folder Strings" },
      // To Symmetric Encryption
      { new Integer(CommandCodes.FLD_Q_TO_SYM_ENC), "Recrypt New Folder Shares" },
      // Alter Share Permissions
      { new Integer(CommandCodes.FLD_Q_ALTER_PERMISSIONS), "Alter Folder Permissions" },
      // Move Folder
      { new Integer(CommandCodes.FLD_Q_MOVE_FOLDER), "Move Folder" },
      // Transfer Ownership
      { new Integer(CommandCodes.FLD_Q_TRANSFER_OWNERSHIP), "Transfer Folder Ownership" },
      // Remove Folder
      { new Integer(CommandCodes.FLD_Q_REMOVE_FOLDER), "Remove Folder" },
      // Remove Folder Shares
      { new Integer(CommandCodes.FLD_Q_REMOVE_FOLDER_SHARES), "Remove Folder Shares" },
      // Get Folder Shares
      { new Integer(CommandCodes.FLD_Q_GET_FOLDER_SHARES), "Get Folder Shares" },
      // Add Folder Shares
      { new Integer(CommandCodes.FLD_Q_ADD_FOLDER_SHARES), "Add Folder Shares" },
      // Get Number of Files and Size of Files in the Folder
      { new Integer(CommandCodes.FLD_Q_GET_FOLDER_SIZE), "Get Folder Size" },
      // Get Number of Files/Messages and Folders, and number of bytes stored and used on disk
      { new Integer(CommandCodes.FLD_Q_GET_SIZE_SUMMARY), "Get Folder Size Summary" },
      // Get All Access Users for specified Folder IDs
      { new Integer(CommandCodes.FLD_Q_GET_ACCESS_USERS), "Fetch Group Members" },
      // Get Last Content Update (Msg or File)
      { new Integer(CommandCodes.FLD_Q_GET_LAST_UPDATE), "Fetch Last Update" },
      // Get Folder Red Flag Count
      { new Integer(CommandCodes.FLD_Q_RED_FLAG_COUNT), "Count New Items" },
      // Ring Ring
      { new Integer(CommandCodes.FLD_Q_RING_RING), "Ringing..." },
      // Synch File/Message Folders, Folder tree, Contacts
      { new Integer(CommandCodes.FLD_Q_SYNC), "Synch Folders" },
      { new Integer(CommandCodes.FLD_Q_SYNC_NEXT), "Synch Folders Next" },
      { new Integer(CommandCodes.FLD_Q_SYNC_CONTACTS), "Synch Contacts" },
      { new Integer(CommandCodes.FLD_Q_SYNC_FOLDER_TREE), "Synch Folder Shares" },


      // =====================
      // *** File Commands ***
      // =====================

      // New File
      { new Integer(CommandCodes.FILE_Q_NEW_FILES), "Create File" },
      // New File Intent
      { new Integer(CommandCodes.FILE_Q_NEW_FILE_STUDS), "New File Intent" },
      { new Integer(CommandCodes.FILE_Q_NEW_FILE_STUDS_BACKGROUND), "New Background File Intent" },
      // Query upload progress
      { new Integer(CommandCodes.FILE_Q_GET_PROGRESS), "File Progress" },
      // Upload file content bytes
      { new Integer(CommandCodes.FILE_Q_UPLOAD_CONTENT), "File Content" },
      // Update digests - typicaly after or during upload
      { new Integer(CommandCodes.FILE_Q_UPDATE_DIGESTS), "Update File Signatures" },
      // Upload Abort
      { new Integer(CommandCodes.FILE_Q_UPLOAD_ABORT), "Upload Aborted" },
      // Upload Reset
      { new Integer(CommandCodes.FILE_Q_UPLOAD_RESET), "Upload Reset" },
      // Get Files
      { new Integer(CommandCodes.FILE_Q_GET_FILES), "Get File Listing" },
      { new Integer(CommandCodes.FILE_Q_GET_FILES_STAGED), "Get File Listing" },
      // Get Msg File Attachments
      { new Integer(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS), "Get Message File Attachment(s)" },
      // Get File Data Attributes
      { new Integer(CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES), "Get File Attributes" },
      // Get File Data Attributes
      { new Integer(CommandCodes.FILE_Q_GET_FILE_DATA_ATTRIBUTES), "Get File Attributes" },
      // Get File Data
      { new Integer(CommandCodes.FILE_Q_GET_FILES_DATA), "Get File Data" },
      // Remove Files
      { new Integer(CommandCodes.FILE_Q_REMOVE_FILES), "Remove File(s)" },
      // Move Files
      { new Integer(CommandCodes.FILE_Q_MOVE_FILES), "Move File(s)" },
      // Copy Files
      { new Integer(CommandCodes.FILE_Q_COPY_FILES), "Copy File(s)" },
      // Rename File
      { new Integer(CommandCodes.FILE_Q_RENAME), "Rename File" },
      // Save Msg File Attachments
      { new Integer(CommandCodes.FILE_Q_SAVE_MSG_FILE_ATT), "Save Message File Attachment(s)" },
      // Update Status
      { new Integer(CommandCodes.FILE_Q_UPDATE_STATUS), "Update Status" },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { new Integer(CommandCodes.CNT_Q_GET_CONTACTS), "Get Contacts" },
      // Get My group account Contacts
      { new Integer(CommandCodes.CNT_Q_GET_GROUP_CONTACTS), "Get group account Contacts" },
      // Set My group Contacts, includes create/change/delete
      { new Integer(CommandCodes.CNT_Q_SET_GROUP_CONTACTS), "Set group account Contacts" },
      // New Contact
      { new Integer(CommandCodes.CNT_Q_NEW_CONTACT), "Create Contact" },
      // New Sub-user Contacts
      { new Integer(CommandCodes.CNT_Q_NEW_SUB_CONTACTS), "Create Contacts" },
      // Accept Contacts
      { new Integer(CommandCodes.CNT_Q_ACCEPT_CONTACTS), "Accept Contact(s)" },
      // Decline Contacts
      { new Integer(CommandCodes.CNT_Q_DECLINE_CONTACTS), "Decline Contact(s)" },
      // Acknowledge Contacts
      //Notes:  Once contact is acknowledged, the final encrypted name and description are given.
      { new Integer(CommandCodes.CNT_Q_ACKNOWLEDGE_CONTACTS), "Acknowledge Contact(s)" },
      // Move Contacts
      { new Integer(CommandCodes.CNT_Q_MOVE_CONTACTS), "Move Contact(s)" },
      // Remove Contacts
      { new Integer(CommandCodes.CNT_Q_REMOVE_CONTACTS), "Remove Contact(s)" },
      // Rename My Contact
      { new Integer(CommandCodes.CNT_Q_RENAME_MY_CONTACT), "Rename Contact" },
      // Rename Contacts With Me
      { new Integer(CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME), "Alter Contact(s)" },
      // Alter Permission
      { new Integer(CommandCodes.CNT_Q_ALTER_PERMITS), "Alter Contact Permissions" },
      // Alter Settings
      { new Integer(CommandCodes.CNT_Q_ALTER_SETTINGS), "Alter Contact Settings" },
      // Alter contact usage stamp
      { new Integer(CommandCodes.CNT_Q_UPDATE_USED), "Update Contact Used" },
      // Accept Contacts Failure
      { new Integer(CommandCodes.CNT_E_CONTACT_IS_NOT_ACCESSIBLE), "Contact is not accessible" },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full
      { new Integer(CommandCodes.MSG_Q_GET_FULL), "Fetch Message(s)" },
      // Get Messages Briefs
      { new Integer(CommandCodes.MSG_Q_GET_BRIEFS), "Fetch Message Briefs" },
      // Get Msg Attachments Briefs
      { new Integer(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS), "Fetch Message Attachment Briefs" },
      // Get Message Body
      { new Integer(CommandCodes.MSG_Q_GET_BODY), "Fetch Message Body" },
      // Get Single Full Message
      { new Integer(CommandCodes.MSG_Q_GET_MSG), "Fetch Message" },
      // New Message
      { new Integer(CommandCodes.MSG_Q_NEW), "Create New Message" },
      // Convert message link to symmetric encryption
      { new Integer(CommandCodes.MSG_Q_TO_SYM_ENC), "Message Encryption to Symmetric" },
      // Remove Messages
      { new Integer(CommandCodes.MSG_Q_REMOVE), "Remove Message(s)" },
      // Remove Old Messages
      { new Integer(CommandCodes.MSG_Q_REMOVE_OLD), "Remove Old Message(s)" },
      // Move Messages
      { new Integer(CommandCodes.MSG_Q_MOVE), "Move Message(s)" },
      // Copy Messages
      { new Integer(CommandCodes.MSG_Q_COPY), "Copy Message(s)" },
      // Save Message Attachments
      { new Integer(CommandCodes.MSG_Q_SAVE_MSG_ATT), "Save Message Attachment(s)" },
      // Notify participants about typing a message
      { new Integer(CommandCodes.MSG_Q_TYPING), "Typing message" },
      // Expiry Revocation
      { new Integer(CommandCodes.MSG_Q_EXPIRY), "Change Expiry or Revocation" },
      // Update Status
      { new Integer(CommandCodes.MSG_Q_UPDATE_STATUS), "Update Status" },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { new Integer(CommandCodes.KEY_Q_GET_KEY_PAIRS), "Load Key Pairs" },
      // Get Public Keys For Users
      { new Integer(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS), "Get Public Keys" },
      // Get Public Key By IDs
      { new Integer(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS), "Get Public Keys" },
      // New Key Pair
      { new Integer(CommandCodes.KEY_Q_NEW_KEY_PAIR), "New Key Pair" },
      // Remove Key Pairs
      { new Integer(CommandCodes.KEY_Q_REMOVE_KEY_PAIRS), "Remove Key Pair" },
      // Set Key Recovery / Enable Password Reset
      { new Integer(CommandCodes.KEY_Q_SET_KEY_RECOVERY), "Enable Password Reset" },
      // Get Key Recovery settings
      { new Integer(CommandCodes.KEY_Q_GET_KEY_RECOVERY), "Get Password Reset settings" },
      { new Integer(CommandCodes.KEY_A_GET_KEY_RECOVERY), "Get Password Reset settings" },


      // =======================
      // *** InvEml Commands ***
      // =======================

      // Remove My InvEmls
      { new Integer(CommandCodes.INV_Q_REMOVE), "Remove Invitation" },


      // =====================
      // *** Stat Commands ***
      // =====================
      { new Integer(CommandCodes.STAT_Q_GET), "Get Object Statistics" },
      { new Integer(CommandCodes.STAT_Q_UPDATE), "Update Object Statistics" },
      // Fetch All Object Stats
      { new Integer(CommandCodes.STAT_Q_FETCH_ALL_OBJ_STATS), "Fetch Access History and Privileges" },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      { new Integer(CommandCodes.ORG_Q_GET_ORG), "Get Organization Structure" },

      // =============================
      // *** System level messages ***
      // =============================

      // Ping-Pong
      { new Integer(CommandCodes.SYS_Q_PING), "Keep Alive" },
      // Client provides Version info and requests Server Version info reply
      { new Integer(CommandCodes.SYS_Q_VERSION), "Version Info" },

      { new Integer(CommandCodes.SYS_Q_NOTIFY), "Register Persistent Connection" },

      { new Integer(CommandCodes.SYS_Q_GET_AUTO_UPDATE), "Auto Update" },

      // Get Temporary Public Key
      { new Integer(CommandCodes.SYS_Q_GET_TEMP_PUB_KEY), "Get Public Key" },

      // System Login
      { new Integer(CommandCodes.SYS_Q_LOGIN), "Open Secure Channel" },

      // System Query
      { new Integer(CommandCodes.SYS_Q_QUERY), "Query" },

      // =====================
      // *** User Commands ***
      // =====================


      // Login Secure Session
      { new Integer(CommandCodes.USR_A_LOGIN_SECURE_SESSION), "Open Secure Channel" },

      // Login failed --
      { new Integer(CommandCodes.USR_E_HANDLE_PASSWORD_COMBO_DNE), "User Handle-Password pair does not exist" },
      { new Integer(CommandCodes.USR_E_USER_LOCKED_OUT), "User Locked Out" },
      { new Integer(CommandCodes.USR_E_LOGIN_FAILED), "Login Failed" },
      // Get My Info
      { new Integer(CommandCodes.USR_A_GET_INFO), "Retrieve Account" },
      // Get Sub-Account Info
      { new Integer(CommandCodes.USR_A_GET_SUB_ACCOUNTS), "Get Account Information" },
      // Create New User
      { new Integer(CommandCodes.USR_A_NEW_USER), "Create New User Account" },
      // Alter User Password
      { new Integer(CommandCodes.USR_A_ALTER_PASSWORD), "Change Credentials" },
      // Get Challenge Questions
      { new Integer(CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE), "Get Challenge Questions" },
      // Get Password Recovery Data
      { new Integer(CommandCodes.USR_A_PASS_RECOVERY_GET_COMPLETE), "Get Password Recovery Data" },
      // Delete Account
      { new Integer(CommandCodes.USR_A_DELETE), "Delete Account" },
      // Logout
      { new Integer(CommandCodes.USR_A_LOGOUT), "Logout" },
      // System asking for session recycle
      { new Integer(CommandCodes.USR_A_RECYCLE_SESSION_REQUEST), "Re-connect request" },
      // Client session recycling
      { new Integer(CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE), "Re-connect confirmed" },
      // Delete an account
      { new Integer(CommandCodes.USR_A_REMOVE), "Remove User Account" },
      // User suspended
      { new Integer(CommandCodes.USR_A_SUSPENDED), "Account Suspended" },
      // Get User Handles
      { new Integer(CommandCodes.USR_A_GET_HANDLES), "Get User Handles" },
      // Calculate Cummulative Usage
      { new Integer(CommandCodes.USR_A_CUMULATIVE_USAGE), "Calculate Usage" },

      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { new Integer(CommandCodes.EML_A_GET), "Get Email Address" },
      // Get Available Domains
      { new Integer(CommandCodes.EML_A_GET_DOMAINS), "Get Available Email Domains" },
      // Remove Emails
      { new Integer(CommandCodes.EML_A_REMOVE), "Remove Email Address" },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { new Integer(CommandCodes.ADDR_A_FOUND_HASH), "Find Address in Book" },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get My Folders
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS), "Get Folders" },
      // Get My Child Folders
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS_CHILDREN), "Get My Child Folders" },
      // Get My Root Folders
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS_ROOTS), "Get My Root Folders" },
      // Remove Folder
      { new Integer(CommandCodes.FLD_A_REMOVE_FOLDER), "Remove Folders" },
      // Get Folder Size
      { new Integer(CommandCodes.FLD_A_GET_FOLDER_SIZE), "Get Folder Size" },
      // Get Folder Red Flag Count
      { new Integer(CommandCodes.FLD_A_RED_FLAG_COUNT), "Count New Items" },
      // Ring Ring
      { new Integer(CommandCodes.FLD_A_RING_RING), "Ring, ring..." },
      // Synch File/Message Folders, Folder tree, Contacts
      { new Integer(CommandCodes.FLD_A_SYNC), "Synch Folders" },
      { new Integer(CommandCodes.FLD_A_SYNC_NEXT), "Synch Folders Next" },
      // Folder Errors
      { new Integer(CommandCodes.FLD_E_CANNOT_REMOVE_SUPER_ROOT_FOLDER), "Cannot remove super root folder" },
      { new Integer(CommandCodes.FLD_E_FOLDER_DNE_OR_NOT_YOURS), "Folder does not exist or is not yours" },
      { new Integer(CommandCodes.FLD_E_ILLEGAL_FOLDER_MOVE), "Illegal folder move" },
      { new Integer(CommandCodes.FLD_E_PARENT_FOLDER_DNE_OR_NOT_YOURS), "Parent folder does not exist or is not yours" },


      // =====================
      // *** File Commands ***
      // =====================

      // Upload file content bytes
      { new Integer(CommandCodes.FILE_A_UPLOAD_COMPLETED), "Content Completed" },
      // New File
      // Get Files
      // Move Files
      // Copy Files
      { new Integer(CommandCodes.FILE_A_GET_FILES), "Get File List" },
      // Get File Data Attributes
      { new Integer(CommandCodes.FILE_A_GET_FILES_DATA_ATTRIBUTES), "Get File Attributes" },
      // Get File Data
      { new Integer(CommandCodes.FILE_A_GET_FILES_DATA), "Get File Data" },
      // Remove Files
      { new Integer(CommandCodes.FILE_A_REMOVE_FILES), "Remove File(s)" },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { new Integer(CommandCodes.CNT_A_GET_CONTACTS), "Get Contact List" },
      // Remove Contacts
      { new Integer(CommandCodes.CNT_A_REMOVE_CONTACTS), "Remove Contact(s)" },
      // Alter contact usage stamp
      { new Integer(CommandCodes.CNT_A_UPDATE_USED), "Update Contact Used" },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full or Briefs
      { new Integer(CommandCodes.MSG_A_GET), "Fetch Message(s)" },
      // Get Message Body
      { new Integer(CommandCodes.MSG_A_GET_BODY), "Fetch Message Body" },
      // Remove Message(s)
      { new Integer(CommandCodes.MSG_A_REMOVE), "Remove Message(s)" },
      // Notify participants about typing a message
      { new Integer(CommandCodes.MSG_A_TYPING), "Typing message" },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { new Integer(CommandCodes.KEY_A_GET_KEY_PAIRS), "Load Key Pairs" },
      // Get Public Keys For Users
      { new Integer(CommandCodes.KEY_A_GET_PUBLIC_KEYS), "Get Public Key(s)" },
      // Remove Key Pairs
      { new Integer(CommandCodes.KEY_A_REMOVE_KEY_PAIRS), "Remove Key Pair(s)" },

      // =====================
      // *** Stat Commands ***
      // =====================
      { new Integer(CommandCodes.STAT_A_GET), "Get Object Statistics" },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      { new Integer(CommandCodes.ORG_A_GET_ORG), "Get Organization Structure" },


      // =============================
      // *** System level messages ***
      // =============================


      // System Commands
      { new Integer(CommandCodes.SYS_A_PONG), "Keep Alive" },
      // No-op, sometimes used to release the Writer-Reader worker pair
      // when no reply to a request is available.
      { new Integer(CommandCodes.SYS_A_NOOP), "Confirmation" },
      // Display a message to user
      { new Integer(CommandCodes.SYS_A_MSG), "System Message" },
      // Server replies with array of reply-sets
      { new Integer(CommandCodes.SYS_A_REPLY_DATA_SETS), "Reply Data Sets" },
      // Server replies with Version information
      { new Integer(CommandCodes.SYS_A_VERSION), "Version Info" },

      { new Integer(CommandCodes.SYS_A_NOTIFY), "Register Persistent Connection" },

      // Connection timeout
      { new Integer(CommandCodes.SYS_A_CONNECTION_TIMEOUT), "Connection Timeout" },
      { new Integer(CommandCodes.SYS_E_BANDWIDTH_EXCEEDED), "Error: Bandwidth Limit Exceeded" },
      { new Integer(CommandCodes.SYS_E_ACCOUNT_EXPIRED), "Error: Account Expired" },
      { new Integer(CommandCodes.SYS_E_STORAGE_EXCEEDED), "Error: Storage Limit Exceeded" },

      { new Integer(CommandCodes.SYS_A_GET_AUTO_UPDATE), "Auto Update" },

      // Get Temporary Public Key
      { new Integer(CommandCodes.SYS_A_GET_TEMP_PUB_KEY), "Get Public Key" },

      // System Login
      { new Integer(CommandCodes.SYS_A_LOGIN), "Open Secure Channel" },
      { new Integer(CommandCodes.SYS_E_LOGIN), "Open Secure Channel Failed" },

      // System Query
      { new Integer(CommandCodes.SYS_A_QUERY), "Query" },

      { new Integer(CommandCodes.SYSNET_Q_DISTRIBUTE), "SYSNET_Q_DISTRIBUTE" },
      { new Integer(CommandCodes.SYSENG_A_DISTRIBUTED), "SYSENG_A_DISTRIBUTED" },
      { new Integer(CommandCodes.SYSENG_Q_LOGIN), "SYSENG_Q_LOGIN" },
      { new Integer(CommandCodes.SYSNET_A_LOGIN), "SYSNET_A_LOGIN" },
      { new Integer(CommandCodes.SYSNET_A_LOGIN_FAILED), "SYSNET_A_LOGIN_FAILED" },
      { new Integer(CommandCodes.SYSENG_Q_SET_USERS), "SYSENG_Q_SET_USERS" },
      { new Integer(CommandCodes.SYSNET_A_SET_USERS), "SYSNET_A_SET_USERS" },
      { new Integer(CommandCodes.SYSENG_Q_ADD_USER), "SYSENG_Q_ADD_USER" },
      { new Integer(CommandCodes.SYSNET_A_ADD_USER), "SYSNET_A_ADD_USER" },
      { new Integer(CommandCodes.SYSENG_Q_REMOVE_USER), "SYSENG_Q_REMOVE_USER" },
      { new Integer(CommandCodes.SYSNET_A_REMOVE_USER), "SYSNET_A_REMOVE_USER" },
      { new Integer(CommandCodes.SYSENG_Q_SERVER_AVAILABLE), "SYSENG_Q_SERVER_AVAILABLE" },
      { new Integer(CommandCodes.SYSNET_A_SERVER_AVAILABLE), "SYSNET_A_SERVER_AVAILABLE" },
      { new Integer(CommandCodes.SYSENG_Q_CONSOLE_COMMAND), "SYSENG_Q_CONSOLE_COMMAND" },
      { new Integer(CommandCodes.SYSNET_A_CONSOLE_COMMAND), "SYSNET_A_CONSOLE_COMMAND" },
      { new Integer(CommandCodes.SYSENG_Q_TOKEN), "SYSENG_Q_TOKEN" },
      { new Integer(CommandCodes.SYSENG_Q_GET_TEMP_USER_PACKETS), "SYSENG_Q_GET_TEMP_USER_PACKETS" },
      { new Integer(CommandCodes.SYSNET_A_GET_TEMP_USER_PACKETS), "SYSNET_A_GET_TEMP_USER_PACKETS" },

      // =====================
      // *** System Errors ***
      // =====================
      { new Integer(CommandCodes.SYS_E_EXCEPTION), "System Exception" }
    };

    Arrays.sort(actionInfoNames, codeComparator);
  } // end static


  public static String getActionInfoName(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageActionNameSwitch.class, "getActionInfoName(int code)");
    if (trace != null) trace.args(code);

    String actionInfoName = null;

    int index = Arrays.binarySearch(actionInfoNames, new Integer(code), codeComparator);
    if (index >= 0)
      actionInfoName = (String) actionInfoNames[index][1];
    else
      actionInfoName = "code="+code;

    if (actionInfoName.startsWith("com.CH_")) {
      if (trace != null) trace.data(90, actionInfoName);
      actionInfoName = Misc.getClassNameWithoutPackage(actionInfoName);
      if (trace != null) trace.data(91, actionInfoName);
    }

    if (trace != null) trace.exit(MessageActionNameSwitch.class, actionInfoName);
    return actionInfoName;
  }

}