/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;

import com.CH_gui.action.Actions;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class JActionButton extends JButton implements PropertyDrivenItem, DisposableObj {

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
      }
      if (trace != null) trace.exit(OurPropertyChangeListener.class);
    }
  }

  private Action action;
  PropertyChangeListener ourPropertyChangeListener;
  private boolean smallIcon;
  private boolean isToolButton;

  /** Creates new JActionButton */
  public JActionButton() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionButton.class, "JActionButton()");
    if (trace != null) trace.exit(JActionButton.class);
  }
  public JActionButton(Action action) {
    this(action, false, null, true);
  }
  public JActionButton(Action action, Dimension maxAndPrefSize) {
    this(action, false, maxAndPrefSize, true);
  }
  public JActionButton(Action action, boolean smallIcon) {
    this(action, smallIcon, null, true);
  }
  public JActionButton(Action action, boolean smallIcon, Dimension maxAndPrefSize, boolean isToolButton) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionButton.class, "JActionButton(Action action, boolean smallIcon, Dimension maxAndPrefSize, boolean isToolButton)");
    if (trace != null) trace.args(action);
    if (trace != null) trace.args(smallIcon);
    if (trace != null) trace.args(maxAndPrefSize);
    if (trace != null) trace.args(isToolButton);
    this.smallIcon = smallIcon;
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
    if (maxAndPrefSize != null) {
      setPreferredSize(maxAndPrefSize);
      setMaximumSize(maxAndPrefSize);
      setSize(maxAndPrefSize);
    }
    if (trace != null) trace.exit(JActionButton.class);
  }


  public void setAction(Action newValue) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionButton.class, "setAction(Action newValue)");
    if (trace != null) trace.args(newValue);

    // Disconnect current action;
    if (action != null) {
      removeActionListener(action);
      removePropertyChangeListener();
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

      String toolTipText = (String) action.getValue(Actions.TOOL_TIP);
      if (toolTipText != null)
        setToolTipText(toolTipText);

      if (icon == null) {
        Integer mnemonic = (Integer) action.getValue(Actions.MNEMONIC);
        if (mnemonic != null)
          setMnemonic(mnemonic.intValue());
      }

      addActionListener(action);
      if (ourPropertyChangeListener == null) {
        ourPropertyChangeListener = new OurPropertyChangeListener();
      }
      action.addPropertyChangeListener(ourPropertyChangeListener);
    }
    if (trace != null) trace.exit(JActionButton.class);
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