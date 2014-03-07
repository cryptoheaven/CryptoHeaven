/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records;

import java.sql.Timestamp;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.8 $</b>
*
* @author  Marcin Kurzawa
*/
public interface LinkRecordI {

  public int getCompatibleStatTypeIndex();
  public Timestamp getCreatedStamp();
  public Long getId();
  public Long getObjId();
  public Short getOwnerObjType();
  public Long getOwnerObjId();

}