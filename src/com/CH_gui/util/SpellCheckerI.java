/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import java.awt.Point;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* @author  Marcin Kurzawa
*/
public interface SpellCheckerI {

  public JPopupMenu createPopupMenu(int x, int y, int maxSuggestions, String ignoreAllLabel, String addLabel, String noSuggestionsLabel);
  public boolean isInMisspelledWord(Point pt);
  public void pause();
  public void recheckAll();
  public void restart(JTextComponent textComp);
  public void resume();
  public void stop();
  
}