/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import java.sql.Timestamp;

/**
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.11 $</b>
*
* @author  Marcin Kurzawa
*/
public interface SendMessageInfoProviderI {

  public static final short TO = MsgLinkRecord.RECIPIENT_TYPE_TO;
  public static final short CC = MsgLinkRecord.RECIPIENT_TYPE_CC;
  public static final short BCC = MsgLinkRecord.RECIPIENT_TYPE_BCC;
  public static final short[] RECIPIENT_TYPES = new short[] { TO, CC, BCC };

  public static final short CONTENT_MODE_MAIL_PLAIN = 1;
  public static final short CONTENT_MODE_MAIL_HTML = 2;
  public static final short CONTENT_MODE_ADDRESS_BOOK_ENTRY = 3;

  // get the provider gui component
  public Object getContext();
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