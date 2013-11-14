/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.records;

import com.CH_co.util.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class NewsAddressRecord extends InternetAddressRecord {

  /** Creates new NewsAddressRecord */
  public NewsAddressRecord() {
    super();
  }

  /** Creates new NewsAddressRecord and auto-assign a unique id per unique address. */
  public NewsAddressRecord(String address) {
    super(address);
  }

  /**
   * @return the default icon to represent this Record type.
   */
  public int getIcon() {
    return ImageNums.POSTING16;
  }

}