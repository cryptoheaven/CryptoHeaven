/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
// Source File Name:   WordComparator.java

package comx.Tiger.ssce;

import java.io.Serializable;

public interface WordComparator extends Serializable {

  public static final int MAX_SCORE = 100;

  public abstract int compare(String s, String s1);
}