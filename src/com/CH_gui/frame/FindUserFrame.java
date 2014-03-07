/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.frame;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.ContactOps;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.trace.Trace;
import com.CH_co.util.CallbackI;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.userTable.UserActionTable;
import com.CH_gui.usrs.EmailInvitationPanel;
import com.CH_gui.usrs.UserSearchPanel;
import com.CH_gui.util.MiscGui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.16 $</b>
*
* @author  Marcin Kurzawa
*/
public class FindUserFrame extends JActionFrameClosable {

  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 2;

  private UserSearchPanel userSearchPanel;
  private String selectButtonCustomText;
  private String closeButtonCustomText;
  private boolean isSelectButtonHot;

  private CallbackI contactCreateHotButtonCallback;

  /** Creates new FindUserFrame */
  public FindUserFrame() {
    this(null, null, null, false);
  }

  /** Creates new FindUserFrame */
  public FindUserFrame(String selectButtonCustomText, String closeButtonCustomText, String searchString, boolean isSelectButtonHot) {
    super(com.CH_cl.lang.Lang.rb.getString("title_Find_Friends_and_Associates"), false, false);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FindUserFrame.class, "FindUserFrame()");

    this.selectButtonCustomText = selectButtonCustomText;
    this.closeButtonCustomText = closeButtonCustomText;
    this.isSelectButtonHot = isSelectButtonHot;

    this.userSearchPanel = new UserSearchPanel(true, false, false, null, searchString, false);
    final JButton[] buttons = createButtons();

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(userSearchPanel, BorderLayout.CENTER);
    mainPanel.add(MiscGui.createButtonPanel(buttons), BorderLayout.SOUTH);

    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    this.getRootPane().setDefaultButton(userSearchPanel.getSearchButton());

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

    buttons[0] = new JMyButton(isSelectButtonHot ? new ContactCreateAction() : userSearchPanel.userActionTable.getActions()[UserActionTable.INITIATE_ACTION]);
    buttons[0].setText(selectButtonCustomText != null ? selectButtonCustomText : "Add to Contacts");
    buttons[1] = new JMyButton(userSearchPanel.emailInvitationPanel.getActions()[EmailInvitationPanel.SEND_EMAIL_INVITAION_ACTION]);
    buttons[1].setText("Invite by Email");
    buttons[2] = new JMyButton(closeButtonCustomText != null ? closeButtonCustomText : com.CH_cl.lang.Lang.rb.getString("button_Close"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        closeFrame();
      }
    });

    // Mac OSX cleanup
    buttons[0].setIcon(null);
    buttons[1].setIcon(null);

    return buttons;
  }

  public void setContactCreateHotButtonCallback(CallbackI callback) {
    this.contactCreateHotButtonCallback = callback;
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

  /**
  * Silent contact creation action.
  */
  private class ContactCreateAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      Long[] contactWithIds = RecordUtils.getIDs(userSearchPanel.userActionTable.getSelectedRecords());
      if (contactWithIds != null && contactWithIds.length > 0) {
        ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
        FetchedDataCache cache = SIL.getFetchedDataCache();
        String reason = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {cache.getUserRecord().handle});
        ContactOps.doCreateContacts_Threaded(SIL, null, reason, contactWithIds, contactCreateHotButtonCallback);
        closeFrame();
      }
    }
  }
}