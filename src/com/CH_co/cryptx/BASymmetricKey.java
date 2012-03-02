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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class BASymmetricKey extends BA {

  /**
   * Creates new SymmetricKey of specified length with randomly picked bytes.
   */
  public BASymmetricKey(int length) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(int length)");

    byte[] symmetricKeyMaterial = new byte[length];
    Rnd.getSecureRandom().nextBytes(symmetricKeyMaterial);
    setContent(symmetricKeyMaterial);

    if (trace != null) trace.exit(BASymmetricKey.class);
  }

  /**
   * Creates new SymmetricKey with specified raw key material.
   */
  public BASymmetricKey(byte[] keyMaterial) {
    super(keyMaterial);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(byte[])");
    if (trace != null) trace.exit(BASymmetricKey.class);
  }

  /**
   * Creates new SymmetricKey with specified raw key material.
   */
  public BASymmetricKey(byte[] keySource,int offset,int length) {
    super(keySource, offset, length);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(byte[], int offset, int length)");
    if (trace != null) trace.exit(BASymmetricKey.class);
  }


  /**
   * Creates new SymmetricKey with specified raw key material.
   */
  public BASymmetricKey(BA keyMaterial) {
    super(keyMaterial.toByteArray());
  }

}