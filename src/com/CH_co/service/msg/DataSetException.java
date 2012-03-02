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

package com.CH_co.service.msg;

import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class DataSetException extends Exception {

  /** Creates new DataSetException */
  public DataSetException(String str) {
    super(str);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataSetException.class, "DataSetException(String str)");
    if (trace != null) trace.args(str);
    if (trace != null) trace.exit(DataSetException.class);
  }

}