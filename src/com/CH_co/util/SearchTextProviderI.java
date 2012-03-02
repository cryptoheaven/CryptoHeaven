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

package com.CH_co.util;

import java.util.Collection;

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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface SearchTextProviderI {

  /**
   * Gathers a Collection of CharSequence objects
   * @param searchableObj
   * @return a Collection of CharSequence objects
   */
  public Collection getSearchableCharSequencesFor(Object searchableObj);

  /**
   * Gathers a Collection of CharSequence objects
   * @param searchableObj
   * @param providerSetting
   * @return a Collection of CharSequence objects
   */
  public Collection getSearchableCharSequencesFor(Object searchableObj, boolean providerSetting);

}