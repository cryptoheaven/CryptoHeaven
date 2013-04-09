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

package com.CH_gui.dialog;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.ContactOps;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.CallbackI;
import com.CH_co.util.ImageNums;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyTextArea;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.msgs.MsgComposePanel;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.Images;
import com.CH_gui.util.MessageDialog;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.23 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class InviteByEmailDialog extends GeneralDialog {

  JLabel jInviteHeader = new JMyLabel("<html><body><font size='+1'>"+com.CH_cl.lang.Lang.rb.getString("label_Invite_Your_Friends_and_Associates.")+"</font></body></html>");
  JLabel jFromLabel = new JMyLabel("<html><body>From:</body></html>");
  //JLabel jToLabel = new JMyLabel("<html><p ALIGN='RIGHT'>To:<br><font size='-2'>(use&nbsp;commas&nbsp;to<br>separate&nbsp;emails)</font></html>");
  JLabel jToLabel = new JMyLabel("<html><body><p ALIGN='RIGHT'>To:</p></body></html>");
  //JLabel jBodyLabel = new JMyLabel("<html><body><p ALIGN='RIGHT'>Message:<br><font size='-2'>(optional)</font></p></body></html>");
  JLabel jBodyLabel = new JMyLabel("<html><body><p ALIGN='RIGHT'>Message:</p></body></html>");

  JLabel jFromText;
  JTextArea jToText;
  JTextArea jBodyText;

  JButton jInvite;
  JButton jCancel;

  static final int DEFAULT_OK_BUTTON = 0;
  static final int DEFAULT_CANCEL_BUTTON = 1;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  /** Creates new InviteByEmailDialog */
  public InviteByEmailDialog(Frame owner, String initialEmails) {
    super(owner, com.CH_cl.lang.Lang.rb.getString("title_Invite_Your_Friends_and_Associates"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InviteByEmailDialog.class, "InviteByEmailDialog(Frame owner, String initialEmails)");
    if (trace != null) trace.args(owner, initialEmails);
    initialize(owner, initialEmails);
    if (trace != null) trace.exit(InviteByEmailDialog.class);
  }
  /** Creates new InviteByEmailDialog */
  public InviteByEmailDialog(Dialog owner, String initialEmails) {
    super(owner, com.CH_cl.lang.Lang.rb.getString("title_Invite_Your_Friends_and_Associates"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InviteByEmailDialog.class, "InviteByEmailDialog(Dialog owner, String initialEmails)");
    if (trace != null) trace.args(owner, initialEmails);
    initialize(owner, initialEmails);
    if (trace != null) trace.exit(InviteByEmailDialog.class);
  }

  private void initialize(Component owner, String initialEmails) {
    SIL = MainFrame.getServerInterfaceLayer();
    cache = SIL.getFetchedDataCache();
    JButton[] jButtons = createButtons();
    JPanel jPanel = createPanel(initialEmails);
    super.init(owner, jButtons, jPanel, DEFAULT_OK_BUTTON, DEFAULT_CANCEL_BUTTON);
    jToText.requestFocusInWindow();
  }

  private JButton[] createButtons() {
    JButton[] jButtons = new JButton[2];

    jButtons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Invite"));
    jButtons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedSend();
      }
    });

    jButtons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    jButtons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    jInvite = jButtons[0];
    jCancel = jButtons[1];

    return jButtons;
  }

  private JPanel createPanel(String initialEmails) {
    if (initialEmails == null)
      initialEmails = "";

    jFromText = new JMyLabel(cache.getEmailRecord(cache.getUserRecord().defaultEmlId).getEmailAddressFull());
    jToText = new JMyTextArea(initialEmails, 4, 30);
    jToText.setCaretPosition(jToText.getText().length());
    jBodyText = new JMyTextArea(4, 30);

    jToText.setWrapStyleWord(true);
    jToText.setLineWrap(true);
    jBodyText.setWrapStyleWord(true);
    jBodyText.setLineWrap(true);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    jInviteHeader.setHorizontalTextPosition(JButton.RIGHT);

    int posY = 0;
    jInviteHeader.setIcon(Images.get(ImageNums.MAIL_SEND_INVITE_32));
    panel.add(jInviteHeader, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
    posY ++;
    panel.add(jFromLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 10, 5, 5), 0, 0));
    panel.add(jFromText, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("<html><body><p><font size='-1'><i>Type in an email address of the person you would like to invite. <br>Seperate multiple entries with a comma.</i></font></p></body></html>"), new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 10), 0, 0));
    posY ++;
    panel.add(jToLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(5, 10, 5, 5), 0, 0));
    panel.add(new JScrollPane(jToText), new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("<html><body><p><font size='-1'><i>Personalize your invitation with a message:</i></font></p></body></html>"), new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 10), 0, 0));
    posY ++;
    panel.add(jBodyLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(5, 10, 5, 5), 0, 0));
    panel.add(new JScrollPane(jBodyText), new GridBagConstraints(1, posY, 1, 1, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }

  private void pressedSend() {
    boolean formatOk = RecipientsDialog.isEmailLineValid(jToText.getText());
    if (formatOk) {
      ArrayList emlAddrsL = new ArrayList();
      ArrayList emlNicksL = new ArrayList();
      CallbackI callback = new CallbackI() {
        public void callback(Object value) {
          if (value instanceof Boolean) {
            if (((Boolean) value).booleanValue()) {
              // success
              closeDialog();
            } else {
              // failed
              setEnabledButtons(true);
            }
          }
        }
      };
      setEnabledButtons(false);
      ContactOps.doInviteToContacts_Threaded(SIL, jToText.getText(), jBodyText.getText(), callback, false, emlAddrsL, emlNicksL);
      if (emlAddrsL.size() > 0) {
        // Add-at-once the email addresses that we sent invites to.
        MsgComposePanel.checkEmailAddressesForAddressBookAdition_Threaded(null, emlNicksL, emlAddrsL, false, new FolderFilter(FolderRecord.ADDRESS_FOLDER), true, null, true);
      }
    } else {
      String messageText = "Please enter a valid email address of the recipient(s).";
      String title = "Invalid email address";
      MessageDialog.showInfoDialog(InviteByEmailDialog.this, new JMyLabel(messageText), title, false);
    }
  }

  private void setEnabledButtons(boolean enable) {
    jInvite.setEnabled(enable);
    jCancel.setEnabled(enable);
  }

  private void pressedCancel() {
    closeDialog();
  }

}