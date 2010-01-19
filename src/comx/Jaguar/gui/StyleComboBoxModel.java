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

package comx.Jaguar.gui;

import java.util.*;
import java.awt.*;
import javax.swing.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StyleComboBoxModel extends AbstractListModel implements ComboBoxModel {

  Object currentValue;
  Object[][] items;
  Hashtable[] cache;

  public StyleComboBoxModel(Object[][] listItems) {
    items = listItems;
    cache = new Hashtable[getSize()];
  }

  public void setSelectedItem(Object anObject) {
    currentValue = anObject;
    fireContentsChanged(this,-1,-1);
  }

  public Object getSelectedItem() {
    return currentValue;
  }

  public int getSize() {
    return items[0].length;
  }

  public Object getElementAt(int index) {
    if(cache[index] != null)
      return cache[index];
    else {
      Object obj = items[0][index];
      Object listLabel = items[1][index];
      Object topLabel = items[2][index];

      Hashtable result = new Hashtable();
      if (obj != null)
        result.put("obj", obj);
      if (listLabel != null)
        result.put("listLabel", listLabel);
      if (topLabel != null)
        result.put("topLabel", topLabel);

      cache[index] = result;
      return result;
    }
  }
}