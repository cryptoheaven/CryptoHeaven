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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SysAReplyDataSets extends ClientMessageAction {

  /** Creates new SysAReplyDataSets */
  public SysAReplyDataSets() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAReplyDataSets.class, "SysAReplyDataSets()");
    if (trace != null) trace.exit(SysAReplyDataSets.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAReplyDataSets.class, "runAction(Connection)");

    Obj_List_Co dataSet = (Obj_List_Co) getMsgDataSet();
    Object[] sets = dataSet.objs;

    if (sets != null) {
      for (int i=0; i<sets.length; i++) {
        Object[] set = (Object[]) sets[i];
        Integer actionCode = (Integer) set[0];
        Object objSet = set[1];
        Object[] objSets = null;
        if (objSet instanceof Object[]) {
          if (trace != null) trace.data(10, "array of ProtocolMsgDataSet objects");
          objSets = (Object[]) objSet;
        } else {
          objSets = new Object[] { objSet };
        }
        for (int j=0; j<objSets.length; j++) {
          ProtocolMsgDataSet data = (ProtocolMsgDataSet) objSets[j];
          String actionClassName = ClientActionSwitch.switchCodeToActionName(actionCode.intValue());
          ClientMessageAction action = null;
          try {
            action = (ClientMessageAction) Class.forName(actionClassName).newInstance();
          } catch (ClassNotFoundException e1) {
          } catch (InstantiationException e2) {
          } catch (IllegalAccessException e3) {
          }
          if (action != null) {
            // Do not set random stamp as it is not needed anymore (because the carrying action already returned) and it would mess up the progress monitors
            // Don't want ZERO stamp either as this interferes with LOGIN progress monitors
            action.copyStampFromAction(this);
            action.setMsgDataSet(data);
            action.setActionCode(actionCode.intValue());
            action.setServerInterfaceLayer(getServerInterfaceLayer());
            action.setClientContext(getClientContext());
            DefaultReplyRunner.nonThreadedRun(getServerInterfaceLayer(), action);
          }
        }
      }
    }

    if (trace != null) trace.exit(SysAReplyDataSets.class, null);
    return null;
  }

}