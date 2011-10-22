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

package com.CH_gui.msgs;

import java.sql.Timestamp;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface MsgSendInfoProviderI {

  public String[] getContent();
  public Short getContentType();
  public short getContentMode();
  public Timestamp getExpiry();
  public Record getFromAccount();
  public String getQuestion();
  public String getPassword();
  public short getPriority();
  public MsgLinkRecord getReplyToMsgLink();
  public Object[] getSelectedAttachments();
  public Object[] getInlineAttachments();
  public Object[] getSelectedAndInlineAttachments();
  public Record[][] getSelectedRecipients();
  public Record[] getSelectedRecipients(short type);

  public boolean isCopyToOutgoing();
  public boolean isSavingAsDraft();
  public boolean isStagedSecure();

  public void messageSentNotify();
  public void setSendMessageInProgress(boolean inProgress);

}