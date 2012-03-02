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

package com.CH_co.cryptx;

import com.CH_co.trace.Trace;
import com.CH_co.util.Hasher;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public final class BAEncodedPassword extends BASymmetricKey {

  /** Creates new EncodedPassword */
  public BAEncodedPassword(char[] password) {
    super(Hasher.getEncodedPassword(password));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAEncodedPassword.class, "EncodedPassword()");
    if (trace != null) trace.exit(BAEncodedPassword.class);
  }

  /**
   * @return cloned content of this EncodedPassword as a password hash.
   */
  public Long getHashValue() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAEncodedPassword.class, "getHash()");
    byte[] bytes = toByteArray();
    Long hashValue = Hasher.getPasswordHash(bytes);
    for (int i=0; i<bytes.length; i++)
      bytes[i] = 0;
    if (trace != null) trace.exit(BAEncodedPassword.class, hashValue);
    return hashValue;
  }

}