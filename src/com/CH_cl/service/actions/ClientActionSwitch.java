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

      { Integer.valueOf(CommandCodes.SYS_A_CHECK), SysACheck.class.getName() },

      // =====================
      // *** User Commands ***
      // =====================

      // Login Secure Session
      { Integer.valueOf(CommandCodes.USR_A_LOGIN_SECURE_SESSION), UsrALoginSecureSession.class.getName() },

      // Login failed -- 
      { Integer.valueOf(CommandCodes.USR_E_HANDLE_PASSWORD_COMBO_DNE), UsrALoginFailed.class.getName() },
      { Integer.valueOf(CommandCodes.USR_E_USER_LOCKED_OUT), UsrALoginFailed.class.getName() },
      { Integer.valueOf(CommandCodes.USR_E_LOGIN_FAILED), UsrALoginFailed.class.getName() },
      // Get My Info
      { Integer.valueOf(CommandCodes.USR_A_GET_INFO), UsrAGetInfo.class.getName() },
      // Get Sub-Account Info
      { Integer.valueOf(CommandCodes.USR_A_GET_SUB_ACCOUNTS), UsrAGetSubAcc.class.getName() },
      // Create New User
      { Integer.valueOf(CommandCodes.USR_A_NEW_USER), UsrANewUser.class.getName() },
      // Alter User Password
      { Integer.valueOf(CommandCodes.USR_A_ALTER_PASSWORD), UsrAAlterPassword.class.getName() },
      // Find Password Recovery challenge questions
      { Integer.valueOf(CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE), UsrAPassRecoveryGetChallenge.class.getName() },
      // Find Password Recovery complete data
      { Integer.valueOf(CommandCodes.USR_A_PASS_RECOVERY_GET_COMPLETE), UsrAPassRecoveryGetComplete.class.getName() },
      // Delete Account
      { Integer.valueOf(CommandCodes.USR_A_DELETE), UsrADelete.class.getName() },
      // Logout
      { Integer.valueOf(CommandCodes.USR_A_LOGOUT), UsrALogout.class.getName() },
      // System asking for session recycle
      { Integer.valueOf(CommandCodes.USR_A_RECYCLE_SESSION_REQUEST), UsrARecycleSessionRequest.class.getName() },
      // Client session recycling
      { Integer.valueOf(CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE), UsrARecycleSessionSequence.class.getName() },
      // Delete an account
      { Integer.valueOf(CommandCodes.USR_A_REMOVE), UsrARemove.class.getName() },
      // Account Suspended
      { Integer.valueOf(CommandCodes.USR_A_SUSPENDED), UsrASuspended.class.getName() },
      // Get User Handles
      { Integer.valueOf(CommandCodes.USR_A_GET_HANDLES), UsrAGetHandles.class.getName() },
      // Calculate Cummulative Usage
      { Integer.valueOf(CommandCodes.USR_A_CUMULATIVE_USAGE), UsrAGetCumulativeUsage.class.getName() },


      // ======================
      // *** Email Commands ***
      // ======================

      // Get Emails
      { Integer.valueOf(CommandCodes.EML_A_GET), EmlAGet.class.getName() },
      // Get Available Domains
      { Integer.valueOf(CommandCodes.EML_A_GET_DOMAINS), EmlAGetDomains.class.getName() },
      // Remove Emails
      { Integer.valueOf(CommandCodes.EML_A_REMOVE), EmlARemove.class.getName() },


      // =============================
      // *** Address Book Commands ***
      // =============================

      // Find if email address exists in Address Book
      { Integer.valueOf(CommandCodes.ADDR_A_FOUND_HASH), AddrAFoundHash.class.getName() },


      // =======================
      // *** Folder Commands ***
      // =======================

      // Get My Folders
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS), FldAGetFolders.class.getName() },
      // Get My Folders Children
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS_CHILDREN), FldAGetChildren.class.getName() },
      // Get My Folders Roots
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDERS_ROOTS), FldAGetRoots.class.getName() },
      // Remove Folder
      { Integer.valueOf(CommandCodes.FLD_A_REMOVE_FOLDER), FldARemoveFolder.class.getName() },
        //actionClassName = OKMessageAction.class.getName() },
      { Integer.valueOf(CommandCodes.FLD_A_GET_FOLDER_SIZE), FldAGetSize.class.getName() },
      // Get Folder Red Flag Count
      { Integer.valueOf(CommandCodes.FLD_A_RED_FLAG_COUNT), FldARedFlagCount.class.getName() },
      // Ring Ring
      { Integer.valueOf(CommandCodes.FLD_A_RING_RING), FldARingRing.class.getName() },


      // =====================
      // *** File Commands ***
      // =====================

      // New File
      // Get Files
      // Move Files
      // Copy Files
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES), FileAGetFiles.class.getName() },
      // Get File Data Attributes
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES_DATA_ATTRIBUTES), FileAGetFilesDataAttr.class.getName() },
      // Get File Data 
      { Integer.valueOf(CommandCodes.FILE_A_GET_FILES_DATA), FileAGetFilesData.class.getName() },
      // Remove Files
      { Integer.valueOf(CommandCodes.FILE_A_REMOVE_FILES), FileARemoveFiles.class.getName() },


      // ========================
      // *** Contact Commands ***
      // ========================

      // Get My Contacts
      { Integer.valueOf(CommandCodes.CNT_A_GET_CONTACTS), CntAGetContacts.class.getName() },
      // Remove Contacts
      { Integer.valueOf(CommandCodes.CNT_A_REMOVE_CONTACTS), CntARemoveContacts.class.getName() },


      //====================================
      // *** Message / Posting  Commands ***
      //====================================

      // Get Messages Full or Briefs
      { Integer.valueOf(CommandCodes.MSG_A_GET), MsgAGet.class.getName() },
      // Get Message Body
      { Integer.valueOf(CommandCodes.MSG_A_GET_BODY), MsgAGetBody.class.getName() },
      // Remove Message(s)
      { Integer.valueOf(CommandCodes.MSG_A_REMOVE), MsgARemove.class.getName() },
      // Deliver 'typing' notification
      { Integer.valueOf(CommandCodes.MSG_A_TYPING), MsgATyping.class.getName() },


      // =====================
      // *** Keys Commands ***
      // =====================

      // Get My Key Pairs
      { Integer.valueOf(CommandCodes.KEY_A_GET_KEY_PAIRS), KeyAGetKeyPairs.class.getName() },
      // Get Public Keys For Users
      { Integer.valueOf(CommandCodes.KEY_A_GET_PUBLIC_KEYS), KeyAGetPublicKeys.class.getName() },
      // Remove Key Pairs
      { Integer.valueOf(CommandCodes.KEY_A_REMOVE_KEY_PAIRS), OKMessageAction.class.getName() },
      // Get Key Recovery records
      { Integer.valueOf(CommandCodes.KEY_A_GET_KEY_RECOVERY), KeyAGetRecovery.class.getName() },

      // =====================
      // *** Stat Commands ***
      // =====================

      // Get Stats
      { Integer.valueOf(CommandCodes.STAT_A_GET), StatAGet.class.getName() },


      // =============================
      // *** Organization Commands ***
      // =============================

      // Get My Organization
      //{ Integer.valueOf(CommandCodes.ORG_A_GET_ORG :
        //actionClassName = OrgAGetOrg.class.getName() },


      // =============================
      // *** System level messages ***
      // =============================

      // Ping-Pong
      { Integer.valueOf(CommandCodes.SYS_A_PONG), SysAPong.class.getName() },
      // No-op
      { Integer.valueOf(CommandCodes.SYS_A_NOOP), SysANoop.class.getName() },
      // Display a message to user
      { Integer.valueOf(CommandCodes.SYS_A_MSG), SysAMsg.class.getName() },
      // Server replies with array of reply-sets
      { Integer.valueOf(CommandCodes.SYS_A_REPLY_DATA_SETS), SysAReplyDataSets.class.getName() },
      // Server replies with Version information
      { Integer.valueOf(CommandCodes.SYS_A_VERSION), SysAVersion.class.getName() },
      // Registration for inter-user notifications
      { Integer.valueOf(CommandCodes.SYS_A_NOTIFY), SysANotify.class.getName() },

      { Integer.valueOf(CommandCodes.SYS_A_GET_AUTO_UPDATE), SysAGetAutoUpdate.class.getName() },

      // Get Temporary Public Key
      { Integer.valueOf(CommandCodes.SYS_A_GET_TEMP_PUB_KEY), SysAGetTempPubKey.class.getName() },
      
      // System Login
      { Integer.valueOf(CommandCodes.SYS_A_LOGIN), SysALogin.class.getName() },
      { Integer.valueOf(CommandCodes.SYS_E_LOGIN), SysELogin.class.getName() },

      // System Query
      { Integer.valueOf(CommandCodes.SYS_A_QUERY), SysAQuery.class.getName() },

      // Connection Timeout
      { Integer.valueOf(CommandCodes.SYS_A_CONNECTION_TIMEOUT), SysATimeout.class.getName() },
      { Integer.valueOf(CommandCodes.SYS_E_BANDWIDTH_EXCEEDED), ErrorBandwidthExceeded.class.getName() },
      { Integer.valueOf(CommandCodes.SYS_E_ACCOUNT_EXPIRED), ErrorAccountExpired.class.getName() },
      { Integer.valueOf(CommandCodes.SYS_E_STORAGE_EXCEEDED), ErrorStorageExceeded.class.getName() },

      // Engine-network Distribution Messages handled by the server.
      { Integer.valueOf(CommandCodes.SYSNET_Q_DISTRIBUTE), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_LOGIN), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_LOGIN_FAILED), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_SET_USERS), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_ADD_USER), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_REMOVE_USER), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_SERVER_AVAILABLE), SysQServerAction.class.getName() },
      { Integer.valueOf(CommandCodes.SYSNET_A_CONSOLE_COMMAND), SysQServerAction.class.getName() },
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

    int index = Arrays.binarySearch(actionClassNames, Integer.valueOf(msgCode), codeComparator);
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