/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.addressBook;

import java.io.Serializable;

import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.2 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class AddrDND_TransferableData extends Object implements Serializable {
  public Long[] msgLinkIDs;
  public AddrDND_TransferableData() {
  }
  public AddrDND_TransferableData(MsgLinkRecord[] msgLinks) {
    msgLinkIDs = RecordUtils.getIDs(msgLinks);
  }
}