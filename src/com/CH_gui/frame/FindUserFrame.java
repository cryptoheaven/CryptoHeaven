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

package com.CH_gui.frame;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.gui.*;
import com.CH_gui.usrs.*;
import com.CH_gui.userTable.*;
import com.CH_gui.util.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
public class FindUserFrame extends JActionFrameClosable {

  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 2;

  private UserSearchPanel userSearchPanel;
  private String closeButtonText;
  private UserRecord[] selectedUserRecords;

  private EscapeKeyListener escapeKeyListener;
  private JButton escapeButton;

  /** Creates new FindUserFrame */
  public FindUserFrame() {
    this(com.CH_gui.lang.Lang.rb.getString("button_Close"), null);
  }

  /** Creates new FindUserFrame */
  public FindUserFrame(String closeButtonText, String searchString) {
    super(com.CH_gui.lang.Lang.rb.getString("title_Find_Friends_and_Associates"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FindUserFrame.class, "FindUserFrame()");

    this.closeButtonText = closeButtonText;
    this.userSearchPanel = new UserSearchPanel(false, false, null, searchString, false);
    final JButton[] buttons = createButtons();

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(userSearchPanel, BorderLayout.CENTER);
    mainPanel.add(MiscGui.createButtonPanel(buttons), BorderLayout.SOUTH);

    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    this.getRootPane().setDefaultButton(userSearchPanel.getSearchButton());

    escapeButton = buttons[DEFAULT_CANCEL_BUTTON_INDEX];
    escapeKeyListener = new EscapeKeyListener();
    this.addKeyListener(escapeKeyListener);

    // this JActionFrames doesn't save their own size like others do
    setSize(600, 600);
    setLocationRelativeTo(MainFrame.getSingleInstance());

    setVisible(true);

    if (trace != null) trace.exit(FindUserFrame.class);
  }


  /**
   * @return the dialog 'Search' and 'Cancel' buttons
   */
  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(userSearchPanel.userActionTable.getActions()[UserActionTable.INITIATE_ACTION]);
    buttons[0].setText("Add to Contacts");
    buttons[1] = new JMyButton(userSearchPanel.emailInvitationPanel.getActions()[EmailInvitationPanel.SEND_EMAIL_INVITAION_ACTION]);
    buttons[1].setText("Invite by Email");
    buttons[2] = new JMyButton(closeButtonText != null ? closeButtonText : com.CH_gui.lang.Lang.rb.getString("button_Close"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // cancel action
        if (userSearchPanel != null)
          selectedUserRecords = (UserRecord[]) userSearchPanel.getUserActionTable().getSelectedRecords();
        closeFrame();
      }
    });

    // Mac OSX cleanup
    buttons[0].setIcon(null);
    buttons[1].setIcon(null);

    return buttons;
  }

  /**
   * @return User Records that were selected before the dialog was dismissed.
   * This is mostly useless, as frames cannot be modal!
   */
  public UserRecord[] getSelectedUserRecords() {
    return selectedUserRecords;
  }


  public void closeFrame() {
    if (escapeKeyListener != null) {
      this.removeKeyListener(escapeKeyListener);
      escapeKeyListener = null;
      escapeButton = null;
    }
    super.closeFrame();
  }


  /**
   * Clicks a specified button when ESCAPE key click is detected.
   */
  private class EscapeKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent event) {
      if (event.getModifiers() == 0) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
          escapeButton.doClick();
        }
      }
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FindUserFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public String getVisuals() {
    return null;
  }
  public void restoreVisuals(String visuals) {
  }
}