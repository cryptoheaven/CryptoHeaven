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

package com.CH_gui.dialog;

import com.CH_gui.util.VisualsSavable;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.util.GeneralDialog;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import com.CH_gui.list.*;
import com.CH_gui.table.*;
import com.CH_gui.usrs.*;

import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class UserSelectorDialog extends GeneralDialog implements VisualsSavable, ObjectsProviderUpdaterI {

  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private UserSearchPanel userSearchPanel;
  private String selectButtonText;
  private UserRecord[] selectedUserRecords;

  private JButton jSelect;
  private RecordSelectionListener recordSelectionListener;
  private ListUpdatableI updatable;

  /** Creates new UserSelectorDialog */
  public UserSelectorDialog(Frame owner) {
    this(owner, com.CH_gui.lang.Lang.rb.getString("button_Close"), null);
  }
  /** Creates new UserSelectorDialog */
  public UserSelectorDialog(Dialog owner) {
    this(owner, com.CH_gui.lang.Lang.rb.getString("button_Close"), null);
  }

  /** Creates new UserSelectorDialog */
  public UserSelectorDialog(Frame owner, String selectButtonText, String searchString) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Select_Users"));
    constructDialog(owner, selectButtonText, searchString);
  }
  /** Creates new UserSelectorDialog */
  public UserSelectorDialog(Dialog owner, String selectButtonText, String searchString) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Select_Users"));
    constructDialog(owner, selectButtonText, searchString);
  }

  private void constructDialog(Component owner, String selectButtonText, String searchString) {
    this.selectButtonText = selectButtonText;
    this.userSearchPanel = new UserSearchPanel(false, true, null, searchString, false);
    this.recordSelectionListener = new RecordSelectionListener() {
      public void recordSelectionChanged(RecordSelectionEvent event) {
        setEnabledButtons();
      }
    };
    userSearchPanel.getUserActionTable().addRecordSelectionListener(recordSelectionListener);
    JButton[] jButtons = createButtons();

    this.getRootPane().setDefaultButton(userSearchPanel.getSearchButton());
    setEnabledButtons();
    super.init(owner, jButtons, userSearchPanel, -1, DEFAULT_CANCEL_BUTTON_INDEX);
  }


  /**
   * @return the dialog 'Search' and 'Cancel' buttons
   */
  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(selectButtonText);

    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // select action
        selectedUserRecords = (UserRecord[]) userSearchPanel.getUserActionTable().getSelectedRecords();
        if (updatable != null) updatable.update(selectedUserRecords);
        closeDialog();
      }
    });
    jSelect = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // cancel action
        selectedUserRecords = null;
        closeDialog();
      }
    });

    return buttons;
  }

  /**
   * @return User Records that were selected before the dialog was dismissed.
   * This is mostly useless, as frames cannot be modal!
   */
  public UserRecord[] getSelectedUserRecords() {
    return selectedUserRecords;
  }

  private void setEnabledButtons() {
    if (jSelect != null)
      jSelect.setEnabled(userSearchPanel.getUserActionTable().getSelectedRecords() != null);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "UserSelectorDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  } 

  /*************************************************************************
  *** O b j e c t s P r o v i d e r U p d a t e r I    interface methods ***
  *************************************************************************/

  public Object[] provide(Object args) {
    return selectedUserRecords;
  }

  public Object[] provide(Object args, ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
      return selectedUserRecords;
    } else {
      throw new IllegalStateException("Already registered updatable object.");
    }
  }

  public void registerForUpdates(ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
    } else {
      throw new IllegalStateException("Already registered for updates.");
    }
  }

  /*****************************************************
  *** D i s p o s a b l e O b j    interface methods ***
  *****************************************************/
  public void disposeObj() {
    updatable = null;
    if (recordSelectionListener != null) {
      userSearchPanel.getUserActionTable().removeRecordSelectionListener(recordSelectionListener);
      recordSelectionListener = null;
    }
    userSearchPanel = null;
    jSelect = null;
  }

}