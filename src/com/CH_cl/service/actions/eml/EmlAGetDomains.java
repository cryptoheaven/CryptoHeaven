/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.actions.eml;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class EmlAGetDomains extends ClientMessageAction {

  /** Creates new EmlAGetDomains */
  public EmlAGetDomains() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlAGetDomains.class, "EmlAGetDomains()");
    if (trace != null) trace.exit(EmlAGetDomains.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlAGetDomains.class, "runAction(Connection)");

    Obj_List_Co reply = (Obj_List_Co) getMsgDataSet();

    EmailRecord[] emailRecords = reply.objs.length > 1 ? ((Eml_Get_Rp) reply.objs[1]).emailRecords : null;

    getFetchedDataCache().addEmailRecords(emailRecords);

    if (trace != null) trace.exit(EmlAGetDomains.class, null);
    return null;
  }

}