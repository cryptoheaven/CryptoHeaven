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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2011
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

    Obj_List_Co dataSet = (Obj_List_Co) getMsgDataSet();
    if (dataSet != null && dataSet.objs != null && dataSet.objs.length > 0 && dataSet.objs[0] instanceof byte[]) {
      RSAPublicKey publicKey = RSAPublicKey.bytesToObject((byte[]) dataSet.objs[0]);
      getCommonContext().setPublicKeyToSendWith(publicKey);
    }
    getClientContext().releaseLoginStreamers();

    if (trace != null) trace.exit(SysAGetTempPubKey.class, null);
    return null;
  }

}