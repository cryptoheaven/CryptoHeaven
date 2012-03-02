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

package com.CH_cl.service.actions;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class OKMessageAction extends ClientMessageAction {

  /** Creates new OKMessageAction */
  public OKMessageAction() {
  }

  /** The action handler performs all actions related to the received OK message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Str_Rp reply = (Str_Rp) getMsgDataSet();
    if (reply.message != null && reply.message.length() > 0) {
      String title = "OK Dialog";
      NotificationCenter.show(NotificationCenter.INFORMATION_MESSAGE, title, reply.message);
    }
    return null;
  }

}