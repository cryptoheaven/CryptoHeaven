/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import java.io.File;
import javax.swing.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public interface MsgComposeManagerI {

  public void addAttachment(File fileAttachment);
  public Action[] getActions();
  public void priorityPressed();
  public void selectAttachmentsPressed();
  public void selectRecipientsPressed(int recipientType);
  public void ringPressed();

}