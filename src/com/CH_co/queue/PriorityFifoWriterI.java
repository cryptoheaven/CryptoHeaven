/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.queue;

/** 
 *
 * @author  Marcin Kurzawa
 */
public interface PriorityFifoWriterI extends FifoWriterI {


/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
   * Lower priority number, closer in the queue.
   */
  public void add(Object obj, long priority);

}