/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.cache;

import com.CH_co.service.records.Record;

/**
*
* @author Marcin
*/
public interface RecordIteratorI {

  public int getCount();
  public Record getItemNext(Record item, int direction);
  public Record getItem(int position);
  public int getPosition(Record item);

}