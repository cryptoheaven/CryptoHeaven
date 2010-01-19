/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.util;

import java.util.*;
import com.CH_co.trace.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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
public class SingleTokenArbiter extends Object {

  private Hashtable ht = new Hashtable();
  private Hashtable htTrace = new Hashtable();

  /**
   * Stores a token for a key, if another token is already stored for the same key, 
   * it quits without storing the new token.  Max one token per key.
   * @return true if the new token was stored, false when another token was already present.
   */
  public boolean putToken(Object key, Object token) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SingleTokenArbiter.class, "putToken(Object key, Object token)");
    if (trace != null) trace.args(key, token);
    boolean stored = false;
    synchronized (ht) {
      if (trace != null) trace.data(10, "synchronized block... entered", this);
      if (ht.get(key) == null) {
        ht.put(key, token);
        stored = true;
      }
      if (stored)
        htTrace.put(key, Thread.currentThread().getName());
      if (trace != null) trace.data(100, "synchronized block... done", this);
    }
    if (trace != null) trace.exit(SingleTokenArbiter.class, stored);
    return stored;
  }
  /**
   * Clears user token.
   */
  public void removeToken(Object key, Object token) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SingleTokenArbiter.class, "removeToken(Object key, Object token)");
    if (trace != null) trace.args(key, token);
    synchronized (ht) {
      if (trace != null) trace.data(10, "synchronized block... entered", this);
      Object obj = ht.get(key);
      if (obj == null) {
        throw new IllegalArgumentException("There is no token stored for a given key.");
      } else if (!token.equals(obj)) {
        throw new IllegalArgumentException("Specified token does not match the one stored.");
      } else {
        ht.remove(key);
        htTrace.remove(key);
      }
      if (trace != null) trace.data(100, "synchronized block... done", this);
    }
    if (trace != null) trace.exit(SingleTokenArbiter.class);
  }

  public String toString() {
    return "[SingleTokenArbiter"
      + ": ht=" + Misc.objToStr(ht)
      + ", htTrace=" + Misc.objToStr(htTrace)
      + "]";
  }
}