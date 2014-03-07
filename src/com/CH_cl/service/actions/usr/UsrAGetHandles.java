/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.usr.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class UsrAGetHandles extends ClientMessageAction {

  /** Creates new UsrAGetHandles */
  public UsrAGetHandles() {
  }

  /** The action handler performs all actions related to the received message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {

    // get reply data
    Usr_UsrHandles_Rp set = (Usr_UsrHandles_Rp) getMsgDataSet();

    // add records to cache
    getFetchedDataCache().addUserRecords(set.userRecords);
    getFetchedDataCache().addEmailRecords(set.emailRecords);

    return null;
  }

}