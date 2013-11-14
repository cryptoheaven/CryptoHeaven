/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.action;

import javax.swing.Action;
import com.CH_gui.util.ActionUtils;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.19 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Actions extends Object {

  // Properties for actions used in menus, toolbars, popups

  public static final String ACTION_ID = "actionId";                // Integer

  public static final String NAME = Action.NAME;                    // String
  public static final String TOOL_NAME = "toolName";                // String
  public static final String GENERATED_NAME = "generatedName";      // Boolean
  public static final String MENU_ICON = Action.SMALL_ICON;         // Icon
  public static final String TOOL_ICON = "icon24";                  // Icon
  public static final String TOOL_TIP = Action.SHORT_DESCRIPTION;   // String
  public static final String F1_HELP = Action.LONG_DESCRIPTION;     // String
  public static final String MNEMONIC = "mnemonic";                 // Integer
  public static final String ACCELERATOR = "accelerator";           // KeyStroke
  // for checkboxes
  public static final String STATE_CHECK = "state";                 // Boolean
  // for radio buttons
  public static final String SELECTED_RADIO = "selected";           // Boolean
  // Button group if changed is not propegated to the gui, once the gui creates a 
  // representation of a radio button, the group cannot be changed by changing the action property.
  public static final String BUTTON_GROUP = "group";                // ButtonGroup

  // Changes the parent menu name for whatever menu the action is situated
  public static final String PARENT_NAME = "parentName";            // String
  public static final String PARENT_MNEMONIC = "parentMnemonic";    // String

  public static final String ENABLED = "enabled";                   // Boolean

  // inclusion default is TRUE
  public static final String IN_MENU = "inMenu";                    // Boolean
  public static final String IN_POPUP = "inPopup";                  // Boolean // dynamic property, optionally disable certain actions in popup menus
  public static final String IN_POPUP_SHOW_DEACTIVATED = "inPopupD";// Boolean // dynamic property, optionally disable showing of deactivated actions
  public static final String IN_TOOLBAR = "inToolbar";              // Boolean

  // removable default is FALSE
  public static final String REMOVABLE_MENU = "removableMenu";      // Boolean
  public static final String REMOVABLE_TOOLBAR = "removableToolbar";// Boolean

  // for non-removable menus, see if we should disable action if component is removed, default is TRUE
  public static final String DISABABLE = ActionUtils.DISABABLE;     // Boolean

  // Reserve IDs in groups of 100
  public static final int LEADING_ACTION_ID_JACTION_FRAME = 100;
  public static final int LEADING_ACTION_ID_MAIN_FRAME = 200;
  public static final int LEADING_ACTION_ID_FOLDER_ACTION_TREE = 300;
  public static final int LEADING_ACTION_ID_FILE_ACTION_TABLE = 400;
  public static final int LEADING_ACTION_ID_CONTACT_ACTION_TABLE = 500;
  public static final int LEADING_ACTION_ID_USER_ACTION_TABLE = 600;
  public static final int LEADING_ACTION_ID_MSG_ACTION_TABLE = 700;
  public static final int LEADING_ACTION_ID_COMPOSE_MESSAGE_PANEL = 750;
  public static final int LEADING_ACTION_ID_ACTION_FRAME_CLOSABLE = 800;
  public static final int LEADING_ACTION_ID_LOCALFILE_ACTION_TABLE = 900;
  public static final int LEADING_ACTION_ID_EMAIL_INVITATION_PANEL = 1000;
  public static final int LEADING_ACTION_ID_STAT_ACTION_TABLE = 1100;
  public static final int LEADING_ACTION_ID_RECORD_ACTION_TABLE = 1200;
  public static final int LEADING_ACTION_ID_SUB_USER_ACTION_TABLE = 1300;
  public static final int LEADING_ACTION_ID_MSG_TABLE_FRAME = 1400;
  public static final int LEADING_ACTION_ID_GROUP_ACTION_TABLE = 1500;
  public static final int LEADING_ACTION_ID_RECYCLE_ACTION_TABLE = 1600;
  public static final int LEADING_ACTION_ID_PREVIEW_MESSAGE_PANEL = 1700;

}