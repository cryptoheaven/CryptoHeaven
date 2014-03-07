/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.list;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public interface StringHighlighterI {

  public Object[] getExcludedObjs();
  public String getHighlightStr();
  public String[] getHighlightStrs();
  public int getHighlightMatch();
  public boolean hasHighlightingStr();
  public boolean includePreTags();
  public boolean alwaysArmorInHTML();

}