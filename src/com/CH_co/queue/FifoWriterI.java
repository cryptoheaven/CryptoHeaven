/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.queue;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public interface FifoWriterI {

  /** Add object to the fifo */
  public void add(Object obj);

  /** Clears the contents of the FIFO.  All references in the FIFO are discarded. */
  public void clear();

  /** Get number of objects in the fifo */
  public int size();


}