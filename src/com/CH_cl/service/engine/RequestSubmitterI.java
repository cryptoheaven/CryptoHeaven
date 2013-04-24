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

package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.service.msg.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public interface RequestSubmitterI {

  public void submitAndReturn(MessageAction msgAction);
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction);
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction, long timeout);
  public void submitAndWait(MessageAction msgAction);
  public boolean submitAndWait(MessageAction msgAction, long timeout);

}