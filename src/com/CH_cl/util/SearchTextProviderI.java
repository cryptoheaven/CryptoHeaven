/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.util;

import com.CH_cl.service.cache.FetchedDataCache;
import java.util.Collection;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public interface SearchTextProviderI {

  /**
   * Gathers a Collection of CharSequence objects
   * @param searchableObj
   * @return a Collection of CharSequence objects
   */
  public Collection getSearchableCharSequencesFor(FetchedDataCache cache, Object searchableObj);

  /**
   * Gathers a Collection of CharSequence objects
   * @param searchableObj
   * @param providerSetting
   * @return a Collection of CharSequence objects
   */
  public Collection getSearchableCharSequencesFor(FetchedDataCache cache, Object searchableObj, boolean providerSetting);

}