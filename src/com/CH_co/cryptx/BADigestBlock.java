/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.11 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class BADigestBlock extends BA {

  /** Creates new BADigestBlock */
  public BADigestBlock(byte[] digestContent) {
    super(digestContent);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BADigestBlock.class, "BADigestBlock()");
    if (trace != null) trace.exit(BADigestBlock.class);
  }
  
  /** Creates new BADigestBlock */
  public BADigestBlock(BA digestContent) {
    super(digestContent);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BADigestBlock.class, "BADigestBlock()");
    if (trace != null) trace.exit(BADigestBlock.class);
  }
}