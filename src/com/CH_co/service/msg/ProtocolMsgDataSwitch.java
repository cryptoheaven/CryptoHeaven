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

import java.util.*;

import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.addr.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.key.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.service.msg.dataSets.sys.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.util.Misc;
import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Translates ProtocolMsgDataSet instances into code number and vice versa
 * For cross-version compatibility, do not change the number codes for already existing request types.
 * @author  Marcin Kurzawa
 * @version
 */
public class ProtocolMsgDataSwitch extends Object {

  private static final int NULL_CODE = -999;

  private static Switch_StrInt_Comparator nameComparator = new Switch_StrInt_Comparator(true, true);
  private static Switch_StrInt_Comparator codeComparator = new Switch_StrInt_Comparator(false, true);

  private static Object[][] classCodes;
  private static Object[][] classNames = new Object[][] {
    {Fld_Folders_Rp.class.getName(), new Integer(10)},

    {File_GetAttr_Rp.class.getName(), new Integer(20)},
    {File_GetData_Rp.class.getName(), new Integer(21)},
    {File_GetLinks_Rp.class.getName(), new Integer(22)},

    {Cnt_GetCnts_Rp.class.getName(), new Integer(30)},

    {Key_KeyPairs_Rp.class.getName(), new Integer(40)},
    {Key_PubKeys_Rp.class.getName(), new Integer(41)},

    {Msg_GetBody_Rp.class.getName(), new Integer(50)},
    {Msg_GetLinkAndData_Rp.class.getName(), new Integer(51)},

    {Org_GetOrg_Rp.class.getName(), new Integer(60)},

    {Str_Rp.class.getName(), new Integer(80)},

    {Usr_GetMyInfo_Rp.class.getName(), new Integer(90)},
    {Usr_LoginSecSess_Rp.class.getName(), new Integer(91)},
    {Usr_UsrHandles_Rp.class.getName(), new Integer(92)},



    {Obj_IDAndIDList_Rq.class.getName(), new Integer(100)},
    {Obj_IDList_Co.class.getName(), new Integer(101)},
    {Obj_IDPair_Co.class.getName(), new Integer(102)},
    {Obj_ID_Rq.class.getName(), new Integer(103)},
    {Obj_IDs_Co.class.getName(), new Integer(104)},
    {Obj_List_Co.class.getName(), new Integer(105)},
    {Obj_EncSet_Co.class.getName(), new Integer(106)},

    {Cnt_AcceptDecline_Rq.class.getName(), new Integer(110)},
    {Cnt_MovCnts_Rq.class.getName(), new Integer(111)},
    {Cnt_NewCnt_Rq.class.getName(), new Integer(112)},
    {Cnt_Remove_Rq.class.getName(), new Integer(113)},
    {Cnt_Rename_Rq.class.getName(), new Integer(114)},
    {Cnt_GroupCnt_Rq.class.getName(), new Integer(115)},

    {Fld_AddShares_Rq.class.getName(), new Integer(120)},
    {Fld_AltPerm_Rq.class.getName(), new Integer(121)},
    {Fld_AltStrs_Rq.class.getName(), new Integer(122)},
    {Fld_NewFld_Rq.class.getName(), new Integer(123)},
    {Fld_ToSymEnc_Rq.class.getName(), new Integer(124)},

    {File_MoveCopy_Rq.class.getName(), new Integer(130)},
    {File_NewFiles_Rq.class.getName(), new Integer(131)},
    {File_Rename_Rq.class.getName(), new Integer(132)},
    {File_GetFiles_Rq.class.getName(), new Integer(133)},
    {File_Transfer_Co.class.getName(), new Integer(134)},

    {Key_KeyIDs_Rq.class.getName(), new Integer(140)},
    {Key_NewPair_Rq.class.getName(), new Integer(141)},
    {Key_KeyRecov_Co.class.getName(), new Integer(142)},

    {Msg_GetMsgs_Rq.class.getName(), new Integer(150)},
    {Msg_New_Rq.class.getName(), new Integer(151)},
    {Msg_ToSymEnc_Rq.class.getName(), new Integer(152)},
    {Msg_MoveCopy_Rq.class.getName(), new Integer(153)},

    {Usr_AltUsrData_Rq.class.getName(), new Integer(170)},
    {Usr_AltUsrPass_Rq.class.getName(), new Integer(171)},
    {Usr_LoginSecSess_Rq.class.getName(), new Integer(172)},
    {Usr_NewUsr_Rq.class.getName(), new Integer(173)},
    {Usr_Search_Rq.class.getName(), new Integer(174)},
    {Usr_GetSubAcc_Rp.class.getName(), new Integer(175)},
    {Usr_PassRecovery_Co.class.getName(), new Integer(176)},
    {Usr_PassReset_Rq.class.getName(), new Integer(177)},

    {PingPong_Cm.class.getName(), new Integer(180)},

    {Stats_Get_Rq.class.getName(), new Integer(190)},
    {Stats_Get_Rp.class.getName(), new Integer(191)},
    {Stats_Update_Rq.class.getName(), new Integer(192)},

    {Eml_Get_Rp.class.getName(), new Integer(200)},

    {Addr_GetHash_Rp.class.getName(), new Integer(210)},

    {Sys_AutoUpdate_Co.class.getName(), new Integer(300)},

    {Sys_Check_Co.class.getName(), new Integer('d' << 24 | 'a' << 16 | 't' << 8 | 'a')}
  };

  static {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProtocolMsgDataSwitch.class, "static initializer");

    Arrays.sort(classNames, nameComparator);

    classCodes = (Object[][]) classNames.clone();
    Arrays.sort(classCodes, codeComparator);

    // check for duplicate codes
    try {
      for (int i=0; i<classCodes.length-1; i++) {
        if (((Integer) classCodes[i][1]).intValue() == ((Integer) classCodes[i+1][1]).intValue()) {
          String error = "Duplicate protocol data codes for " + Misc.objToStr(classCodes[i]) + " and " + Misc.objToStr(classCodes[i+1]);
          throw new IllegalStateException(error);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(-1);
    }

    if (trace != null) trace.exit(ProtocolMsgDataSwitch.class);
  }

  public static boolean isNull(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProtocolMsgDataSwitch.class, "isNull(int code)");
    if (trace != null) trace.args(code);
    boolean rc = code == NULL_CODE ? true : false;
    if (trace != null) trace.exit(ProtocolMsgDataSwitch.class, rc);
    return rc;
  }

  public static int getCode(ProtocolMsgDataSet protocolMsgDataSet) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProtocolMsgDataSwitch.class, "getCode(ProtocolMsgDataSet protocolMsgDataSet)");
    if (trace != null) trace.args(protocolMsgDataSet);

    int code = NULL_CODE;

    if (protocolMsgDataSet != null) {
      int index = Arrays.binarySearch(classNames, protocolMsgDataSet.getClass().getName(), nameComparator);
      if (index >= 0)
        code = ((Integer)classNames[index][1]).intValue();
    }

    if (trace != null) trace.exit(ProtocolMsgDataSwitch.class, code);
    return code;
  }

  public static String getClassName(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProtocolMsgDataSwitch.class, "getClassName(int code)");
    if (trace != null) trace.args(code);

    String rc = null;

    if (!isNull(code)) {
      int index = Arrays.binarySearch(classCodes, new Integer(code), codeComparator);
      if (index >= 0)
        rc = (String) classCodes[index][0];
    }

    if (trace != null) trace.exit(ProtocolMsgDataSwitch.class, rc);
    return rc;
  }
}