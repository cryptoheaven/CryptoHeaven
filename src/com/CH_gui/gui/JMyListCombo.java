/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.CH_co.gui.*;
import com.CH_co.util.*;

import com.CH_gui.list.*;

import com.CH_guiLib.gui.*;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyListCombo extends JPanel {

  private int selectedIndex;
  private Object selectedObj;
  private Object[] objects;
  private JButton jLabel;
  private JButton jDropDown;

  private EventListenerList myListenerList = new EventListenerList();
  private ObjectsProviderI provider;

  /** 
   * Creates new JMyListCombo 
   * @param index=0 is element in the list, index=1 is version of element displayed on top, index=2 custom action listener
   */
  public JMyListCombo(int initialSelection, ObjectsProviderI provider, ActionListener defaultActionListener) {
    this(initialSelection, provider.provide(null), defaultActionListener);
    this.provider = provider;
  }
  public JMyListCombo(int initialSelection, Object[] objs) {
    this(initialSelection, objs, null);
  }
  public JMyListCombo(int initialSelection, Object[] objs, ActionListener defaultActionListener) {
    this.selectedIndex = initialSelection;
    this.objects = objs;
    Object[] set = makeSet(objs[initialSelection]);
    Object topObj = getRenderableTopElement(set);

    String text = ListRenderer.getRenderedText(topObj);
    String tip = null;
    Icon icon = ListRenderer.getRenderedIcon(topObj);
    if (icon != null && icon.equals(Images.get(ImageNums.TRANSPARENT16)))
      icon = null;
    if (topObj instanceof JComponent)
      tip = ((JComponent) topObj).getToolTipText();
    jLabel = new JMyButtonNoFocus(text, icon);
    jLabel.setToolTipText(tip);
    //jLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
    jLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
    jLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    jDropDown = new JMyButtonNoFocus(new JMyDropdownIcon());
    //jDropDown.setBorder(new EmptyBorder(2, 0, 2, 0));
    jDropDown.setBorder(new EmptyBorder(0, 0, 0, 0));
    jDropDown.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    EtchedBorder border = new EtchedBorder();
    setBorder(border);
    setLayout(new GridBagLayout());
    add(jLabel, new GridBagConstraints(0, 0, 1, 1, 10, 10, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    add(jDropDown, new GridBagConstraints(1, 0, 1, 1, 0, 10, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    ActionListener popupAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (provider != null)
          objects = provider.provide(null);
        if (objects != null && objects.length > 0) {
          JPopupMenu popup = new JMyPopupMenu("Options");
          final JMenuItem[] menuItems = new JMenuItem[objects.length];
          for (int i=0; i<menuItems.length; i++) {
            Object[] set = makeSet(objects[i]);
            Object item = set[0];
            if (item instanceof JMenuItem)
              menuItems[i] = (JMenuItem) item;
            else {
              menuItems[i] = new JMyMenuItem();
              menuItems[i].setText(ListRenderer.getRenderedText(item));
              menuItems[i].setIcon(ListRenderer.getRenderedIcon(item));
              if (item instanceof JComponent)
                menuItems[i].setToolTipText(((JComponent) item).getToolTipText());
            }
            menuItems[i].addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                int index = 0;
                JMenuItem source = (JMenuItem) e.getSource();
                for (int k=0; k<menuItems.length; k++) {
                  if (source.equals(menuItems[k])) {
                    index = k;
                    Object[] set = makeSet(objects[k]);
                    if (set[2] == null) {
                      setSelectedIndex(index);
                      fireActionPerformed(new ActionEvent(JMyListCombo.this, 0, "selected index"));
                    } else if (set[2] instanceof ActionListener) {
                      ((ActionListener) set[2]).actionPerformed(new ActionEvent(JMyListCombo.this, 0, "selected index"));
                    }
                    break;
                  }
                }
                fireActionPerformed(e);
              }
            });
            popup.add(menuItems[i]);
          }
          popup.pack();
          // wrong-popup-location 
          // Point point = MiscGui.getSuggestedPopupLocation(JMyListCombo.this, popup);
          // popup.show(JMyListCombo.this, point.x, point.y);
          popup.show(JMyListCombo.this, 0, JMyListCombo.this.getSize().height);
        }
      }
    };
    jLabel.addActionListener(defaultActionListener != null ? defaultActionListener : popupAction);
    jDropDown.addActionListener(popupAction);
  }

  public Object getSelected() {
    return selectedObj;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public boolean isEnabledLabel() {
    return jLabel.isEnabled();
  }
  public boolean isEnabledDropdown() {
    return jDropDown.isEnabled();
  }
  public void setEnabled(boolean flag) {
    super.setEnabled(flag);
    jLabel.setEnabled(flag);
    jDropDown.setEnabled(flag);
  }
  public void setEnabledLabel(boolean flag) {
    jLabel.setEnabled(flag);
  }
  public void setEnabledDropdown(boolean flag) {
    jDropDown.setEnabled(flag);
  }

  public void setObjectsProvider(ObjectsProviderI provider) {
    this.provider = provider;
  }

  public void setSelectedObject(Object obj) {
    if (provider != null)
      objects = provider.provide(null);
    setSelectedObject(obj, objects);
  }

  public void setSelectedObject(Object obj, Object[] objects) {
    this.objects = objects;
    int index = 0;
    for (int i=0; i<objects.length; i++) {
      Object[] set = makeSet(objects[i]);
      if (set[0].equals(obj)) {
        index = i;
        break;
      }
    }
    setSelectedIndex(index);
  }

  public void setSelectedIndex(int index) {
    Object[] selectedSet = makeSet(objects[index]);
    Object topObj = getRenderableTopElement(selectedSet);
    String text = ListRenderer.getRenderedText(topObj);
    Icon icon = ListRenderer.getRenderedIcon(topObj);
    if (icon != null && icon.equals(Images.get(ImageNums.TRANSPARENT16)))
      icon = null;
    jLabel.setText(text);
    jLabel.setIcon(icon);
    if (topObj instanceof JComponent)
      jLabel.setToolTipText(((JComponent) topObj).getToolTipText());
    selectedIndex = index;
    selectedObj = selectedSet[0];
  }

  public void addActionListener(ActionListener l) {
    myListenerList.add(ActionListener.class, l);
  }

  public void removeDocumentListener(ActionListener l) {
    myListenerList.remove(ActionListener.class, l);
  }

  private void fireActionPerformed(ActionEvent event) {
    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == ActionListener.class) {
        ((ActionListener)listeners[i+1]).actionPerformed(event);
      }
    }
  }

  /**
   * @return set of "list object" / "selectable top object version" / "custom action listener"
   */
  private Object[] makeSet(Object obj) {
    if (obj instanceof Object[])
      return (Object[]) obj;
    else
      return new Object[] { obj, null, null };
  }

  private Object getRenderableTopElement(Object[] set) {
    Object o = set[1];
    if (o == null)
      o = set[0];
    return o;
  }
}