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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.cryptx.RSAPublicKey;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.trace.Trace;

/**
* <b>Copyright</b> &copy; 2001-2012
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
* <b>$Revision: 1.3 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class SysAGetTempPubKey extends ClientMessageAction {

  /** Creates new SysAGetTempPubKey */
  public SysAGetTempPubKey() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAGetTempPubKey.class, "SysAGetTempPubKey()");
    if (trace != null) trace.exit(SysAGetTempPubKey.class);
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAGetTempPubKey.class, "runAction(Connection)");

    try {
      Obj_List_Co dataSet = (Obj_List_Co) getMsgDataSet();
      if (dataSet != null && dataSet.objs != null && dataSet.objs.length > 0 && dataSet.objs[0] instanceof byte[]) {
        RSAPublicKey publicKey = RSAPublicKey.bytesToObject((byte[]) dataSet.objs[0]);
        getCommonContext().setPublicKeyToSendWith(publicKey);
      }
    } catch (Throwable t) {
    } finally {
      // Unlock the Reader and Writer because synchronized sequence is done, wether successful or not.
      getClientContext().releaseLoginStreamers();
    }

    if (trace != null) trace.exit(SysAGetTempPubKey.class, null);
    return null;
  }

}