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
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 *
 * @author  Marcin Kurzawa
 */
public interface FifoReaderI {
  /** @return the next object in the fifo. */
  public Object remove();
  /** peek the next object without removing it. */
  public Object peek();
  /** Get number of objects in the fifo */
  public int size();

}