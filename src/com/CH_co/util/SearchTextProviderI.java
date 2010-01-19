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

import java.util.Collection;

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