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

package com.CH_cl.service.actions;

import java.util.*;

import com.CH_cl.service.actions.addr.*;
import com.CH_cl.service.actions.cnt.*;
import com.CH_cl.service.actions.eml.*;
import com.CH_cl.service.actions.error.*;
import com.CH_cl.service.actions.file.*;
import com.CH_cl.service.actions.fld.*;
import com.CH_cl.service.actions.key.*;
import com.CH_cl.service.actions.msg.*;
import com.CH_cl.service.actions.stat.*;
import com.CH_cl.service.actions.sys.*;
import com.CH_cl.service.actions.usr.*;

import com.CH_co.service.msg.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class ClientActionSwitch extends CommonActionSwitch {

  private static Switch_StrInt_Comparator codeComparator = new Switch_StrInt_Comparator(false, false);
  private static Object[][] actionClassNames = null;
  private static String nullActionClassName = SysANullAction.class.getName();

  static {
    actionClassNames = new Object[][] {

      // ===========================
      // *** System ASCII Checks ***
      // ===========================

      { new Integer(CommandCodes.SYS_A_CHECK), SysACheck.class.getName() },

      // =====================
      // *** User Commands ***
      // =====================

      // Login Secure Session
      { new Integer(CommandCodes.USR_A_LOGIN_SECURE_SESSION), UsrALoginSecureSession.class.getName() },

      // Login failed -- 
      { new Integer(CommandCodes.USR_E_HANDLE_PASSWORD_COMBO_DNE), UsrALoginFailed.class.getName() },
      { new Integer(CommandCodes.USR_E_USER_LOCKED_OUT), UsrALoginFailed.class.getName() },
      { new Integer(CommandCodes.USR_E_LOGIN_FAILED), UsrALoginFailed.class.getName() },
      // Get My Info
      { new Integer(CommandCodes.USR_A_GET_INFO), UsrAGetInfo.class.getName() },
      // Get Sub-Account Info
      { new Integer(CommandCodes.USR_A_GET_SUB_ACCOUNTS), UsrAGetSubAcc.class.getName() },
      // Create New User
      { new Integer(CommandCodes.USR_A_NEW_USER), UsrANewUser.class.getName() },
      // Alter User Password
      { new Integer(CommandCodes.USR_A_ALTER_PASSWORD), UsrAAlterPassword.class.getName() },
      // Find Password Recovery challenge questions
      { new Integer(CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE), UsrAPassRecoveryGetChallenge.class.getName() },
      // Find Password Recovery complete data
      { new Integer(CommandCodes.USR_A_PASS_RECOVERY_GET_COMPLETE), UsrAPassRecoveryGetComplete.class.getName() },
      // Delete Account
      { new Integer(CommandCodes.USR_A_DELETE), UsrADelete.class.getName() },
      // Logout
      { new Integer(CommandCodes.USR_A_LOGOUT), UsrALogout.class.getName() },
      // System asking for session recycle
      { new Integer(CommandCodes.USR_A_RECYCLE_SESSION_REQUEST), UsrARecycleSessionRequest.class.getName() },
      // Client session recycling
      { new Integer(CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE), UsrARecycleSessionSequence.class.getName() },
      // Delete an account
      { new Integer(CommandCodes.USR_A_REMOVE), UsrARemove.class.getName() },
      // Account Suspended
      { new Integer(CommandCodes.USR_A_SUSPENDED), UsrASuspended.class.getName() },
      // Get User Handles
      { new Integer(CommandCodes.USR_A_GET_HANDLES), UsrAGetHandles.class.getName() },
      // Calculate Cummulative Usage
      { new Integer(CommandCodes.USR_A_CUMULATIVE_USAGE), UsrAGetCumulativeUsage.class.getName() },


      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { new Integer(CommandCodes.EML_A_GET), EmlAGet.class.getName() },
      // Get Available Domains
      { new Integer(CommandCodes.EML_A_GET_DOMAINS), EmlAGetDomains.class.getName() },
      // Remove Emails
      { new Integer(CommandCodes.EML_A_REMOVE), EmlARemove.class.getName() },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { new Integer(CommandCodes.ADDR_A_FOUND_HASH), AddrAFoundHash.class.getName() },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get My Folders
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS), FldAGetFolders.class.getName() },
      // Get My Folders Children
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS_CHILDREN), FldAGetChildren.class.getName() },
      // Get My Folders Roots
      { new Integer(CommandCodes.FLD_A_GET_FOLDERS_ROOTS), FldAGetRoots.class.getName() },
      // Remove Folder
      { new Integer(CommandCodes.FLD_A_REMOVE_FOLDER), FldARemoveFolder.class.getName() },
        //actionClassName = OKMessageAction.class.getName() },
      { new Integer(CommandCodes.FLD_A_GET_FOLDER_SIZE), FldAGetSize.class.getName() },
      // Get Folder Red Flag Count
      { new Integer(CommandCodes.FLD_A_RED_FLAG_COUNT), FldARedFlagCount.class.getName() },
      // Ring Ring
      { new Integer(CommandCodes.FLD_A_RING_RING), FldARingRing.class.getName() },


      // =====================
      // *** File Commands ***
      // =====================

      // New File
      // Get Files
      // Move Files
      // Copy Files
      { new Integer(CommandCodes.FILE_A_GET_FILES), FileAGetFiles.class.getName() },
      // Get File Data Attributes
      { new Integer(CommandCodes.FILE_A_GET_FILES_DATA_ATTRIBUTES), FileAGetFilesDataAttr.class.getName() },
      // Get File Data 
      { new Integer(CommandCodes.FILE_A_GET_FILES_DATA), FileAGetFilesData.class.getName() },
      // Remove Files
      { new Integer(CommandCodes.FILE_A_REMOVE_FILES), FileARemoveFiles.class.getName() },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { new Integer(CommandCodes.CNT_A_GET_CONTACTS), CntAGetContacts.class.getName() },
      // Remove Contacts
      { new Integer(CommandCodes.CNT_A_REMOVE_CONTACTS), CntARemoveContacts.class.getName() },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full or Briefs
      { new Integer(CommandCodes.MSG_A_GET), MsgAGet.class.getName() },
      // Get Message Body
      { new Integer(CommandCodes.MSG_A_GET_BODY), MsgAGetBody.class.getName() },
      // Remove Message(s)
      { new Integer(CommandCodes.MSG_A_REMOVE), MsgARemove.class.getName() },
      // Deliver 'typing' notification
      { new Integer(CommandCodes.MSG_A_TYPING), MsgATyping.class.getName() },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { new Integer(CommandCodes.KEY_A_GET_KEY_PAIRS), KeyAGetKeyPairs.class.getName() },
      // Get Public Keys For Users
      { new Integer(CommandCodes.KEY_A_GET_PUBLIC_KEYS), KeyAGetPublicKeys.class.getName() },
      // Remove Key Pairs
      { new Integer(CommandCodes.KEY_A_REMOVE_KEY_PAIRS), OKMessageAction.class.getName() },
      // Get Key Recovery records
      { new Integer(CommandCodes.KEY_A_GET_KEY_RECOVERY), KeyAGetRecovery.class.getName() },

      // =====================
      // *** Stat Commands ***
      // =====================

      // Get Stats
      { new Integer(CommandCodes.STAT_A_GET), StatAGet.class.getName() },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      //{ new Integer(CommandCodes.ORG_A_GET_ORG :
        //actionClassName = OrgAGetOrg.class.getName() },


      // =============================
      // *** System level messages ***
      // =============================

      // Ping-Pong
      { new Integer(CommandCodes.SYS_A_PONG), SysAPong.class.getName() },
      // No-op
      { new Integer(CommandCodes.SYS_A_NOOP), SysANoop.class.getName() },
      // Display a message to user
      { new Integer(CommandCodes.SYS_A_MSG), SysAMsg.class.getName() },
      // Server replies with array of reply-sets
      { new Integer(CommandCodes.SYS_A_REPLY_DATA_SETS), SysAReplyDataSets.class.getName() },
      // Server replies with Version information
      { new Integer(CommandCodes.SYS_A_VERSION), SysAVersion.class.getName() },
      // Registration for inter-user notifications
      { new Integer(CommandCodes.SYS_A_NOTIFY), SysANotify.class.getName() },

      { new Integer(CommandCodes.SYS_A_GET_AUTO_UPDATE), SysAGetAutoUpdate.class.getName() },

      // Get Temporary Public Key
      { new Integer(CommandCodes.SYS_A_GET_TEMP_PUB_KEY), SysAGetTempPubKey.class.getName() },
      
      // System Login
      { new Integer(CommandCodes.SYS_A_LOGIN), SysALogin.class.getName() },
      { new Integer(CommandCodes.SYS_E_LOGIN), SysELogin.class.getName() },

      // System Query
      { new Integer(CommandCodes.SYS_A_QUERY), SysAQuery.class.getName() },

      // Connection Timeout
      { new Integer(CommandCodes.SYS_A_CONNECTION_TIMEOUT), SysATimeout.class.getName() },
      { new Integer(CommandCodes.SYS_E_BANDWIDTH_EXCEEDED), ErrorBandwidthExceeded.class.getName() },
      { new Integer(CommandCodes.SYS_E_ACCOUNT_EXPIRED), ErrorAccountExpired.class.getName() },
      { new Integer(CommandCodes.SYS_E_STORAGE_EXCEEDED), ErrorStorageExceeded.class.getName() },

      // Engine-network Distribution Messages handled by the server.
      { new Integer(CommandCodes.SYSNET_Q_DISTRIBUTE), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_LOGIN), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_LOGIN_FAILED), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_SET_USERS), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_ADD_USER), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_REMOVE_USER), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_SERVER_AVAILABLE), SysQServerAction.class.getName() },
      { new Integer(CommandCodes.SYSNET_A_CONSOLE_COMMAND), SysQServerAction.class.getName() },
    };

    Arrays.sort(actionClassNames, codeComparator);

    // check for duplicate codes
    try {
      for (int i=0; i<actionClassNames.length-1; i++) {
        if (((Integer) actionClassNames[i][0]).intValue() == ((Integer) actionClassNames[i+1][0]).intValue()) {
          String error = "Duplicate action codes for " + Misc.objToStr(actionClassNames[i]) + " and " + Misc.objToStr(actionClassNames[i+1]);
          throw new IllegalStateException(error);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(-1);
    }
  }


  /** 
   * Translates an Command Code into corresponding Client Action Class Name (classes located in this package only)
   * @return name of the action class, (not fully qualified) String
   */
  public static String switchCodeToActionName(int msgCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ClientActionSwitch.class, "switchCodeToActionName(int msgCode)");
    if (trace != null) trace.args(msgCode);

    String actionClassName = null;

    int index = Arrays.binarySearch(actionClassNames, new Integer(msgCode), codeComparator);
    if (index >= 0)
      actionClassName = (String) actionClassNames[index][1];
    else if (msgCode >= 0) 
      actionClassName = nullActionClassName;
    // errors should be negative
    else {
      actionClassName = ErrorMessageAction.class.getName();
    }

    if (trace != null) trace.exit(ClientActionSwitch.class, actionClassName);
    return actionClassName;
  } // end switch request

}