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

package com.CH_gui.actionGui;

import com.CH_gui.util.PropertyDrivenItem;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.lang.ref.WeakReference;

import com.CH_gui.action.Actions;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class JActionCheckBoxMenuItem extends JCheckBoxMenuItem implements PropertyDrivenItem, DisposableObj {

  private class OurPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OurPropertyChangeListener.class, "propertyChange(PropertyChangeEvent propertyChangeEvent)");
      if (trace != null) trace.args(propertyChangeEvent);
      String propertyName = propertyChangeEvent.getPropertyName();
      if (propertyName.equals(Actions.NAME)) {
        String text = (String) propertyChangeEvent.getNewValue();
        setText(text);
      } else if (propertyName.equals(Actions.MENU_ICON)) {
        Icon icon = (Icon) propertyChangeEvent.getNewValue();
        setIcon(icon);
      } else if (propertyName.equals(Actions.TOOL_TIP)) {
        String text = (String) propertyChangeEvent.getNewValue();
        setToolTipText(text);
      } else if (propertyName.equals(Actions.ENABLED)) {
        Boolean enabledState = (Boolean) propertyChangeEvent.getNewValue();
        setEnabled(enabledState.booleanValue());
      } else if (propertyName.equals(Actions.STATE_CHECK)) {
        Boolean checkState = (Boolean) propertyChangeEvent.getNewValue();
        if (checkState != null) {
          if (isSelected() != checkState.booleanValue())
            setSelected(checkState.booleanValue());
        }
      } else if (propertyName.equals(Actions.MNEMONIC)) {
        Integer mnemonic = (Integer) propertyChangeEvent.getNewValue();
        if (mnemonic != null)
          setMnemonic(mnemonic.intValue());
      } else if (propertyName.equals(Actions.ACCELERATOR)) {
        KeyStroke keyStroke = (KeyStroke) propertyChangeEvent.getNewValue();
        setAccelerator(keyStroke);
      }
      if (trace != null) trace.exit(OurPropertyChangeListener.class);
    }
  }

  private class OurItemListener implements ItemListener {
    public void itemStateChanged(ItemEvent event) {
      if (action != null) {
        Boolean oldValue = (Boolean) action.getValue(Actions.STATE_CHECK);
        if (oldValue != null) {
          Boolean newValue = event.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
          if (!oldValue.equals(newValue)) {
            action.putValue(Actions.STATE_CHECK, newValue);
          }
        }
      }
    }
  }

  private Action action;
  PropertyChangeListener ourPropertyChangeListener;
  ItemListener ourItemListener;

  /** Creates new JActionCheckBoxMenuItem */
  public JActionCheckBoxMenuItem() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionCheckBoxMenuItem.class, "JActionCheckBoxMenuItem()");
    if (trace != null) trace.exit(JActionCheckBoxMenuItem.class);
  }
  public JActionCheckBoxMenuItem(Action action) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionCheckBoxMenuItem.class, "JActionCheckBoxMenuItem(Action action)");
    if (trace != null) trace.args(action);
    setAction(action);
    if (trace != null) trace.exit(JActionCheckBoxMenuItem.class);
  }

  public void setAction(Action newValue) {
    // Disconnect current action;
    if (action != null) {
      removeActionListener(action);
      removePropertyChangeListener();
    }
    if (ourItemListener != null) {
      removeItemListener(ourItemListener);
      //ourItemListener = null;
    }

    action = newValue;

    if (action == null) {
//      setText(null);
//      setIcon(null);
//      setState(false);
//      setToolTipText(null);
    } else {
      setText((String) action.getValue(Actions.NAME));
      setIcon((Icon) action.getValue(Actions.MENU_ICON));
      setEnabled(action.isEnabled());

      Boolean state = (Boolean) action.getValue(Actions.STATE_CHECK);
      setState(state.booleanValue());

      String toolTipText = (String) action.getValue(Actions.TOOL_TIP);
      if (toolTipText != null)
        setToolTipText(toolTipText);

      Integer mnemonic = (Integer) action.getValue(Actions.MNEMONIC);
      if (mnemonic != null)
        setMnemonic(mnemonic.intValue());

      KeyStroke keyStroke = (KeyStroke) action.getValue(Actions.ACCELERATOR);
      if (keyStroke != null)
        setAccelerator(keyStroke);

      addActionListener(action);

      // Connect SELECTION changes from the gui to the action property so that all
      // gui displaying this action changes SELECTION in synch.
      if (ourItemListener == null) {
        ourItemListener = new OurItemListener();
      }
      addItemListener(ourItemListener);

      if (ourPropertyChangeListener == null) {
        ourPropertyChangeListener = new OurPropertyChangeListener();
      }
      action.addPropertyChangeListener(ourPropertyChangeListener);
    }
  }

  public void removePropertyChangeListener() {
    if (ourPropertyChangeListener != null && action != null) {
      action.removePropertyChangeListener(ourPropertyChangeListener);
      //ourPropertyChangeListener = null;
    }
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

  /**  I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
   */
  public void disposeObj() {
    removePropertyChangeListener();
    setAction(null);
  }

}