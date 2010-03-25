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

package com.CH_gui.msgs;

import java.util.*;

import javax.swing.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PriorityComboBoxModel extends AbstractListModel implements ComboBoxModel {

  private Object currentValue;
  private ImageIcon images[];
  private Hashtable cache[];

  public PriorityComboBoxModel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "PriorityComboBoxModel()");
    images = new ImageIcon[3]; 
    images[0] = Images.get(ImageNums.PRIORITY_LOW_SMALL);
    images[1] = Images.get(ImageNums.TRANSPARENT16);
    images[2] = Images.get(ImageNums.PRIORITY_HIGH_SMALL);
    cache = new Hashtable[getSize()];
    if (trace != null) trace.exit(PriorityComboBoxModel.class);
  }

  public void setSelectedItem(Object anObject) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "setSelectedItem(Object anObject)");
    if (trace != null) trace.args(anObject);
    currentValue = anObject;
    fireContentsChanged(this,-1,-1);
    if (trace != null) trace.exit(PriorityComboBoxModel.class);
  }

  public Object getSelectedItem() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "getSelectedItem()");
    if (trace != null) trace.exit(PriorityComboBoxModel.class, currentValue);
    return currentValue;
  }

  public int getSize() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "getSize()");
    if (trace != null) trace.exit(PriorityComboBoxModel.class, 3);
    return 3;
  }

  public Object getElementAt(int index) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "getElementAt(int index)");
    if (trace != null) trace.args(index);
    Object rc = null;
    if(cache[index] != null) {
      rc = cache[index];
    } else {
      Hashtable result = new Hashtable();
      if(index == 0)
        result.put("title",com.CH_gui.lang.Lang.rb.getString("priority_FYI"));
      else if (index == 1)
        result.put("title",com.CH_gui.lang.Lang.rb.getString("priority_Normal"));
      else if (index == 2)
        result.put("title",com.CH_gui.lang.Lang.rb.getString("priority_High"));
      if (images[index] != null)
        result.put("image", images[index]);
      cache[index] = result;
      rc = result;
    }
    if (trace != null) trace.exit(PriorityComboBoxModel.class, rc);
    return rc;
  }

}