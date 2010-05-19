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

package com.CH_gui.frame;

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.userTable.*;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SubUserTableFrame extends JActionFrameClosable {

  /** Creates new SubUserTableFrame */
  public SubUserTableFrame() {
    super("Account Management", true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SubUserTableFrame.class, "SubUserTableFrame()");

    Component mainComponent = createMainPanel();
    this.getContentPane().add(mainComponent, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(SubUserTableFrame.class);
  }


  private Component createMainPanel() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    SubUserTableComponent mainTable = new SubUserTableComponent();

    {
      String hintLabelMsg = "<html>This is your user management console.  Create an account for each of your users, clients, and associates.  User accounts can be modified or deleted at any time.  From here you can modify their contact list, settings, options, and permissions.</html>";
      JLabel label = new JMyLabel(hintLabelMsg);
      label.setIcon(Images.get(ImageNums.PEOPLE32));
      label.setBorder(new EmptyBorder(5, 5, 5, 5));
      mainPanel.add(label, BorderLayout.NORTH);
    }

    mainPanel.add(mainTable, BorderLayout.CENTER);

    {
      Action[] actions = ((SubUserActionTable) mainTable.getActionTable()).getUserActions();
      if (actions != null && actions.length > 0) {
        actions = (Action[]) ArrayUtils.concatinate(actions, new Action[] { getCloseAction() });
        JButton[] actionButtons = new JButton[actions.length];
        for (int i=0; i<actions.length; i++) {
          actionButtons[i] = new JActionButton(actions[i], false, null, false);
//          actionButtons[i].setText((String) actions[i].getValue(Actions.NAME));
//          actionButtons[i].setHorizontalTextPosition(JButton.RIGHT);
//          actionButtons[i].setVerticalTextPosition(JButton.CENTER);
        }
        mainPanel.add(MiscGui.createButtonPanel(actionButtons), BorderLayout.SOUTH);
      }
    }
    return mainPanel;
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "SubUserTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}