/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.actionGui;

import com.CH_gui.util.PropertyDrivenItem;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

import com.CH_gui.action.Actions;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class JActionRadioButtonMenuItem extends JRadioButtonMenuItem implements PropertyDrivenItem, DisposableObj {

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
      } else if (propertyName.equals(Actions.SELECTED_RADIO)) {
        // Since the radio buttons are grouped in a ButtonGroup, we must change the selected
        // button model in the group, not just setSelected for the button which would have no effect!
        Boolean checkState = (Boolean) propertyChangeEvent.getNewValue();
        if (checkState != null) {
          ButtonModel selectedModel = buttonGroup.getSelection();
          if (checkState.booleanValue() == true && getModel().equals(selectedModel)) {
            buttonGroup.setSelected(getModel(), true);
          }
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
        Boolean oldValue = (Boolean) action.getValue(Actions.SELECTED_RADIO);
        if (oldValue != null) {
          Boolean newValue = event.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
          if (!oldValue.equals(newValue)) {
            action.putValue(Actions.SELECTED_RADIO, newValue);
          }
        }
      }
    }
  }

  private Action action;
  private ButtonGroup buttonGroup;
  PropertyChangeListener ourPropertyChangeListener;
  ItemListener ourItemListener;

  /** Creates new JActionRadioButtonMenuItem */
  public JActionRadioButtonMenuItem() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionRadioButtonMenuItem.class, "JActionRadioButtonMenuItem()");
    if (trace != null) trace.exit(JActionRadioButtonMenuItem.class);
  }
  public JActionRadioButtonMenuItem(Action action) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionRadioButtonMenuItem.class, "JActionRadioButtonMenuItem(Action action)");
    if (trace != null) trace.args(action);
    setAction(action);
    if (trace != null) trace.exit(JActionRadioButtonMenuItem.class);
  }

  public void setAction(Action newValue) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionRadioButtonMenuItem.class, "setAction(Action newValue)");
    if (trace != null) trace.args(newValue);
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
//      setSelected(false);
//      setToolTipText(null);
    } else {
      setText((String) action.getValue(Actions.NAME));
      setIcon((Icon) action.getValue(Actions.MENU_ICON));
      setEnabled(action.isEnabled());

      buttonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);
      buttonGroup.remove(this); // incase it was already there
      buttonGroup.add(this);
      Boolean state = (Boolean) action.getValue(Actions.SELECTED_RADIO);
      buttonGroup.setSelected(getModel(), state.booleanValue());

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
    if (trace != null) trace.exit(JActionRadioButtonMenuItem.class);
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
    if (buttonGroup != null) {
//      Vector buttonsV = new Vector();
//      Enumeration enm = buttonGroup.getElements();
//      while (enm.hasMoreElements()) {
//        buttonsV.addElement(enm.nextElement());
//      }
      buttonGroup.remove(this);
//      for (int i=0; i<buttonsV.size(); i++) {
//        buttonGroup.remove((AbstractButton) buttonsV.elementAt(i));
//      }
//      buttonsV.clear();
//      buttonGroup = null;
    }
  }

}