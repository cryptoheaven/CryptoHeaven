/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
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
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;
import java.lang.ref.WeakReference;

import com.CH_gui.action.Actions;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Toggle Button that contains icon only.  When Icon is
 *                    not available, text is displayed.
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class JActionToggleButton extends JToggleButton implements PropertyDrivenItem, DisposableObj {

  private class OurPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OurPropertyChangeListener.class, "propertyChange(PropertyChangeEvent propertyChangeEvent)");
      if (trace != null) trace.args(propertyChangeEvent);
      String propertyName = propertyChangeEvent.getPropertyName();
      if (propertyName.equals(Actions.NAME)) {
        String oldText = getText();
        if (!isToolButton && oldText != null && oldText.length() > 0) {
          String text = (String) propertyChangeEvent.getNewValue();
          setText(text);
        }
      } else if (propertyName.equals(Actions.MENU_ICON)) {  
        if (smallIcon) {
          Icon icon = (Icon) propertyChangeEvent.getNewValue();
          setIcon(icon);
        }
      } else if (propertyName.equals(Actions.TOOL_ICON)) {
        if (!smallIcon) {
          Icon icon = (Icon) propertyChangeEvent.getNewValue();
          setIcon(icon);
        }
      } else if (propertyName.equals(Actions.TOOL_NAME)) {
        if (isToolButton) {
          String text = (String) propertyChangeEvent.getNewValue();
          setText(text);
        }
      } else if (propertyName.equals(Actions.TOOL_TIP)) {
        String text = (String) propertyChangeEvent.getNewValue();
        setToolTipText(text);
      } else if (propertyName.equals(Actions.ENABLED)) {
        Boolean enabledState = (Boolean) propertyChangeEvent.getNewValue();
        setEnabled(enabledState.booleanValue());
      } else if (propertyName.equals(Actions.MNEMONIC)) {
        if (getText() != null && getText().length() > 0) {
          Integer mnemonic = (Integer) propertyChangeEvent.getNewValue();
          if (mnemonic != null)
            setMnemonic(mnemonic.intValue());
        }
      } else if (propertyName.equals(Actions.STATE_CHECK)) {
        Boolean checkState = (Boolean) propertyChangeEvent.getNewValue();
        if (checkState != null) {
          if (isSelected() != checkState.booleanValue())
            setSelected(checkState.booleanValue());
        }
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
      }
      // change of 'group' property is ignored.

      if (trace != null) trace.exit(OurPropertyChangeListener.class);
    }
  }

  private class OurItemListener implements ItemListener {
    public void itemStateChanged(ItemEvent event) {
      boolean selected = isSelected();
      if (action != null) {
        {
          Boolean oldValue = (Boolean) action.getValue(Actions.SELECTED_RADIO);
          if (oldValue != null && selected != oldValue.booleanValue())
            action.putValue(Actions.SELECTED_RADIO, Boolean.valueOf(selected));
        }
        {
          Boolean oldValue = (Boolean) action.getValue(Actions.STATE_CHECK);
          if (oldValue != null && selected != oldValue.booleanValue())
            action.putValue(Actions.STATE_CHECK, Boolean.valueOf(selected));
        }
      }
    }
  }

  private Action action;
  private ButtonGroup buttonGroup; // used only if button is created from a radio action
  PropertyChangeListener ourPropertyChangeListener;
  ItemListener ourItemListener;
  private boolean smallIcon;
  private boolean isToolButton;

  /** Creates new JActionToggleButton */
  public JActionToggleButton() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionToggleButton.class, "JActionToggleButton()");
    if (trace != null) trace.exit(JActionToggleButton.class);
  }
  public JActionToggleButton(Action action) {
    this(action, false, null, true);
  }
  public JActionToggleButton(Action action, boolean smallIcon) {
    this(action, smallIcon, null, true);
  }
  public JActionToggleButton(Action action, boolean smallIcon, ButtonGroup buttonGroup, boolean isToolButton) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionToggleButton.class, "JActionToggleButton(Action action, boolean smallIcon, ButtonGroup buttonGroup, boolean isToolButton)");
    if (trace != null) trace.args(action);
    if (trace != null) trace.args(smallIcon);
    if (trace != null) trace.args(buttonGroup);
    if (trace != null) trace.args(isToolButton);
    this.smallIcon = smallIcon;
    this.buttonGroup = buttonGroup;
    this.isToolButton = isToolButton;
    setAction(action);
    if (isToolButton) {
      AffineTransform newAT = new AffineTransform();
      newAT.setToScale(0.8, 0.8); // make Tool text under the icon 80% of default font size
      AffineTransform oldAT = getFont().getTransform();
      AffineTransform modifiedAT = new AffineTransform(oldAT);
      modifiedAT.preConcatenate(newAT); // first original transform will be applied, then the new one
      setFont(getFont().deriveFont(modifiedAT));
      setHorizontalTextPosition(JButton.CENTER);
      setVerticalTextPosition(JButton.BOTTOM);
      setBorder(new EmptyBorder(5,5,5,5));
      setOpaque(false);
    } else {
      setHorizontalTextPosition(JButton.RIGHT);
      setVerticalTextPosition(JButton.CENTER);
    }
    if (trace != null) trace.exit(JActionToggleButton.class);
  }

  public void setAction(Action newValue) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionToggleButton.class, "setAction(Action newValue)");
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
//      setToolTipText(null);
    } else {
      Icon icon = null;
      if (smallIcon)
        icon = (Icon) action.getValue(Actions.MENU_ICON);
      else
        icon = (Icon) action.getValue(Actions.TOOL_ICON);
      setIcon(icon);
      String text = isToolButton ? (String) action.getValue(Actions.TOOL_NAME) : (String) action.getValue(Actions.NAME);
      text = text != null ? text : " ";
      setText(text);
      setEnabled(action.isEnabled());

      if (buttonGroup == null)
        buttonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);

      Boolean state = (Boolean) action.getValue(Actions.SELECTED_RADIO);
      if (state == null)
        state = (Boolean) action.getValue(Actions.STATE_CHECK);
      if (buttonGroup != null) {
        buttonGroup.remove(this); // incase it was already there
        buttonGroup.add(this);
        if (state != null) {
          buttonGroup.setSelected(getModel(), state.booleanValue());
        }
      } else {
        if (state != null) {
          setSelected(state.booleanValue());
        }
      }

      String toolTipText = (String) action.getValue(Actions.TOOL_TIP);
      if (toolTipText != null)
        setToolTipText(toolTipText);

      if (icon == null) {
        Integer mnemonic = (Integer) action.getValue(Actions.MNEMONIC);
        if (mnemonic != null)
          setMnemonic(mnemonic.intValue());
      }

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
    if (trace != null) trace.exit(JActionToggleButton.class);
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