/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.service.msg.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public interface RequestSubmitterI {

  public void submitAndReturn(MessageAction msgAction);
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction);
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction, long timeout);
  public void submitAndWait(MessageAction msgAction);
  public boolean submitAndWait(MessageAction msgAction, long timeout);

}