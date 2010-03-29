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

package com.CH_gui.menuing;

import javax.swing.*;

import com.CH_gui.action.Actions;
import com.CH_gui.list.List_Viewable;

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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MenuActionItem extends Object implements List_Viewable, Cloneable {

  public static String STR_SEPARATOR = com.CH_gui.lang.Lang.rb.getString("Separator");

  /** Name of the menu or menu item that is expected, or already showing. */
  private String menuName;
  /** Action Id, if action id is negative it is a JMenu, 0 for JSeparator, otherwise it is a JMenuItem */
  private Integer actionId;
  /** Customizable menu settings include name, mnemonic, keyCode, mask */
  private Integer mnemonic;
  private KeyStroke keyStroke;

  /** Action object for this menu item.  Objects of type JMenu do not have an associated Action object. */
  private Action action;
  /** If the action is in the Menu, it is showing.  If it is expected, it is not showing. */
  private boolean isShowing;
  /** Flags if the item is chosen in the properties */
  private boolean defaultProperty;

  private ButtonGroup buttonGroup;

  private AbstractButton GUIButton;

  public MenuActionItem(String menuName, int actionId, int mnemonic, int keyCode, int mask) {
    this.menuName = menuName;
    this.actionId = new Integer(actionId);
    if (mnemonic != -1)
      this.mnemonic = new Integer(mnemonic);
    if (keyCode != -1)
      this.keyStroke = KeyStroke.getKeyStroke(keyCode, mask);
    this.defaultProperty = true;
    if (!menuName.equals(MenuActionItem.STR_SEPARATOR) && actionId == 0)
      throw new IllegalArgumentException("Action item (non-separator) cannot have 0 actionId!");
  }
  public MenuActionItem(Action action) {
    this.menuName = (String) action.getValue(Actions.NAME);
    this.actionId = (Integer) action.getValue(Actions.ACTION_ID);
    this.action = action;
    if (!menuName.equals(MenuActionItem.STR_SEPARATOR) && actionId.intValue() == 0)
      throw new IllegalArgumentException("Action item (non-separator) cannot have 0 actionId!");
  }

  public Integer getActionId() {
    return actionId;
  }

  public KeyStroke getKeyStroke() {
    if (keyStroke != null)
      return keyStroke;
    else if (action != null)
      return (KeyStroke) action.getValue(Actions.ACCELERATOR);
    else
      return null;
  }

  public Action getAction() {
    return action;
  }

  public ButtonGroup getButtonGroup() {
    return buttonGroup;
  }

  public AbstractButton getGUIButton() {
    return GUIButton;
  }

  public Integer getMnemonic() {
    if (mnemonic != null)
      return mnemonic;
    else if (action != null)
      return (Integer) action.getValue(Actions.MNEMONIC);
    else
      return null;
  }

  public String getName() {
    return menuName;
  }

  public boolean isActionItem() {
    return actionId.intValue() != 0;
  }

  public boolean isDefaultProperty() {
    return defaultProperty;
  }

  public boolean isGUIButtonSet() {
    return GUIButton != null;
  }

  public boolean isMenu() {
    return actionId.intValue() < 0;
  }

  public boolean isMenuItem() {
    return actionId.intValue() > 0;
  }

  public boolean isSeparator() {
    return actionId.intValue() == 0;
  }

  public boolean isShowing() {
    return isShowing;
  }

  public void setAction(Action newAction) {
    //if (action != null)
      //throw new IllegalArgumentException("MenuActionItem already has an action, action cannot be set!");
    if (newAction != null) {
      if ( !actionId.equals((Integer) newAction.getValue(Actions.ACTION_ID)) )
        throw new IllegalArgumentException("This action does not belong to this MenuActionItem, expecting "+actionId+", received " +newAction.getValue(Actions.ACTION_ID));

      // All names are generated for translation reasons...
      menuName = (String) newAction.getValue(Actions.NAME);
      /*
      Boolean isNameGenerated = (Boolean) newAction.getValue(Actions.GENERATED_NAME);
      if (isNameGenerated != null && isNameGenerated.booleanValue() == true) {
        menuName = (String) newAction.getValue(Actions.NAME);
      } else {
        newAction.putValue(Actions.NAME, menuName);
      }
       */

      // Can't set mnemonic to null because the gui buttons do not support reseting of mnemonics
      if (mnemonic != null)
        newAction.putValue(Actions.MNEMONIC, mnemonic);

      // if setup does not specify shortcuts, leave the default from the code
      if (keyStroke != null)
        newAction.putValue(Actions.ACCELERATOR, keyStroke);
    }
    action = newAction;
    if (GUIButton != null) {
      GUIButton.setAction(action);
    }
  }

  public void setButtonGroup(ButtonGroup buttonGroup) {
    this.buttonGroup = buttonGroup;
  }

  public void setGUIButton(AbstractButton button) {
    // remove old item from button group
    if (GUIButton != null) {
      // remove it from menu group
      if (action != null) {
        ButtonGroup origButtonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);
        if (origButtonGroup != null)
          origButtonGroup.remove(GUIButton);
      }
      // remove it from other group (ie: toolbar group)
      if (buttonGroup != null)
        buttonGroup.remove(GUIButton);
    }
    GUIButton = button;
  }

  public void updateGUIButtonName(boolean isKeepSize) {
    if (GUIButton != null && menuName != null) {
      if (isKeepSize) {
        java.awt.Dimension d = GUIButton.getSize();
        if (d != null && d.width > 1 && d.height > 1)
          GUIButton.setPreferredSize(d);
      }
      GUIButton.setText(menuName);
      Integer newMnemonic = getMnemonic();
      if (newMnemonic != null)
        GUIButton.setMnemonic(newMnemonic.intValue());
      if (GUIButton instanceof JMenuItem && !(GUIButton instanceof JMenu)) {
        JMenuItem menuItem = (JMenuItem) GUIButton;
        menuItem.setAccelerator(getKeyStroke());
      }
    }
  }

  public void setName(String name) {
    this.menuName = name;
    if (action != null)
      action.putValue(Actions.NAME, name);
  }

  public void setKeyStroke(KeyStroke keyStroke) {
    this.keyStroke = keyStroke;
    if (action != null)
      action.putValue(Actions.ACCELERATOR, keyStroke);
  }

  public void setMnemonic(Integer mnemonic) {
    this.mnemonic = mnemonic;
    if (action != null)
      action.putValue(Actions.MNEMONIC, mnemonic);
  }

  public void setDefaultProperty(boolean newValue) {
    defaultProperty = newValue;
  }

  public void setShowing(boolean newValue) {
    isShowing = newValue;
  }

  public String toString() {
    return "MenuActionItem [menuName="+menuName+",actionId="+actionId+",mnemonic="+mnemonic+",keyStroke="+keyStroke+",action="+action+",isShowing="+isShowing+",defaultProperty="+defaultProperty+"]";
  }

  public boolean equals(Object o) {
    if (o instanceof MenuActionItem)
      return actionId.equals( ((MenuActionItem) o).actionId );
    else
      return super.equals(o);
  }
  public int hashCode() {
    if (actionId != null)
      return actionId.hashCode();
    else
      return super.hashCode();
  }

  /** List_Viewable interface method. */
  public String getLabel() {
    return menuName;
  }

  /** List_Viewable interface method. */
  public Icon getIcon() {
    return (Icon) ((action != null) ? action.getValue(Actions.MENU_ICON) : null);
  }

  public Object clone() {
    try {
      MenuActionItem clonedItem = (MenuActionItem) super.clone();
      clonedItem.buttonGroup = null;
      clonedItem.GUIButton = null;
      return clonedItem;
    } catch (CloneNotSupportedException e) {
    }
    return null;
  }
}