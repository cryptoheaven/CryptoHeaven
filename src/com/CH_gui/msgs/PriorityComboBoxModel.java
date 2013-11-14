/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import java.util.*;
import javax.swing.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.util.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class PriorityComboBoxModel extends AbstractListModel implements ComboBoxModel {

  private Object currentValue;
  private ImageIcon images[];
  private Hashtable cache[];

  public PriorityComboBoxModel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityComboBoxModel.class, "PriorityComboBoxModel()");
    images = new ImageIcon[3]; 
    images[0] = Images.get(ImageNums.PRIORITY_LOW16);
    images[1] = Images.get(ImageNums.TRANSPARENT16);
    images[2] = Images.get(ImageNums.PRIORITY_HIGH16);
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
        result.put("title",com.CH_cl.lang.Lang.rb.getString("priority_FYI"));
      else if (index == 1)
        result.put("title",com.CH_cl.lang.Lang.rb.getString("priority_Normal"));
      else if (index == 2)
        result.put("title",com.CH_cl.lang.Lang.rb.getString("priority_High"));
      if (images[index] != null)
        result.put("image", images[index]);
      cache[index] = result;
      rc = result;
    }
    if (trace != null) trace.exit(PriorityComboBoxModel.class, rc);
    return rc;
  }

}