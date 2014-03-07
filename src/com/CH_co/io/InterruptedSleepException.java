/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.io;

import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class InterruptedSleepException extends RuntimeException {

  /** Creates new InterruptedSleepException */
  public InterruptedSleepException(String message) {
    super(message);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InterruptedSleepException.class, "InterruptedSleepException()");
    if (trace != null) trace.exit(InterruptedSleepException.class);
  }

}