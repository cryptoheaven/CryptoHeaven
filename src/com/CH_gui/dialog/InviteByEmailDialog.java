/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.sys.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.23 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class InviteByEmailDialog extends GeneralDialog {

  private static final boolean SHOW_CONFIRMATION_DIALOG = false;

  JLabel jInviteHeader = new JMyLabel("<html><font size='+1'>"+com.CH_gui.lang.Lang.rb.getString("label_Invite_Your_Friends_and_Associates.")+"</font></html>");
  JLabel jFromLabel = new JMyLabel("<html>From:</html>");
  JLabel jToLabel = new JMyLabel("<html><p ALIGN='RIGHT'>To:<br><font size='-2'>(use&nbsp;commas&nbsp;to<br>separate&nbsp;emails)</font></html>");
  JLabel jBodyLabel = new JMyLabel("<html><p ALIGN='RIGHT'>Message:<br><font size='-2'>(optional)</font></html>");

  JLabel jFromText;
  JTextArea jToText;
  JTextArea jBodyText;

  JButton jInvite;
  JButton jCancel;

  static final int DEFAULT_OK_BUTTON = 0;
  static final int DEFAULT_CANCEL_BUTTON = 1;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  private DocumentChangeListener documentChangeListener;

  /** Creates new InviteByEmailDialog */
  public InviteByEmailDialog(Frame owner, String initialEmails) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Invite_Your_Friends_and_Associates"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InviteByEmailDialog.class, "InviteByEmailDialog(Frame owner, String initialEmails)");
    if (trace != null) trace.args(owner, initialEmails);
    initialize(owner, initialEmails);
    if (trace != null) trace.exit(InviteByEmailDialog.class);
  }
  /** Creates new InviteByEmailDialog */
  public InviteByEmailDialog(Dialog owner, String initialEmails) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Invite_Your_Friends_and_Associates"));
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
    setEnabledButtons();
    super.init(owner, jButtons, jPanel, DEFAULT_OK_BUTTON, DEFAULT_CANCEL_BUTTON);
    jToText.requestFocus();
  }

  private JButton[] createButtons() {
    JButton[] jButtons = new JButton[2];

    jButtons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Invite"));
    jButtons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedSend();
      }
    });

    jButtons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
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

    documentChangeListener = new DocumentChangeListener();
    jToText.getDocument().addDocumentListener(documentChangeListener);

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
        GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jFromText, new GridBagConstraints(1, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jToLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JScrollPane(jToText), new GridBagConstraints(1, posY, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jBodyLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JScrollPane(jBodyText), new GridBagConstraints(1, posY, 1, 1, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }

  private void setEnabledButtons() {
    boolean formatOk = RecipientsDialog.isEmailLineValid(jToText.getText());
    jInvite.setEnabled(formatOk);
  }

  private void pressedSend() {

    setEnabledButtons(false);

    final String personalMsg = jBodyText.getText().trim();
    final String[] texts = Misc.getEmailInvitationText(personalMsg, null, cache.getUserRecord());
    final String emailAddresses = jToText.getText().trim();

    JTextArea msg = new JMyTextArea(texts[0], 22, 50);
    msg.setWrapStyleWord(true);
    msg.setLineWrap(true);
    msg.setCaretPosition(0);
    msg.setEditable(false);

    boolean option = true;
    if (SHOW_CONFIRMATION_DIALOG) {
      JPanel msgPanel = new JPanel();
      msgPanel.setLayout(new GridBagLayout());
      msgPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Message_Preview")), new GridBagConstraints(0, 0, 1, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
      msgPanel.add(new JScrollPane(msg), new GridBagConstraints(0, 1, 1, 1, 10, 10,
          GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(10, 10, 10, 10), 0, 0));
      msgPanel.add(new JMyLabel(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("label_Send_the_above_e-mail_invitation_message_to_EMAIL_ADDRESSES_?"), new Object[] {emailAddresses})), new GridBagConstraints(0, 2, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
      String title = com.CH_gui.lang.Lang.rb.getString("title_Send_E-mail_Invitation?");
      option = MessageDialog.showDialogYesNo(this, msgPanel, title);
    }
    if (option == true) {
      Thread th = new ThreadTraced("Invitation Sender") {
        public void runTraced() {
          Obj_List_Co request = new Obj_List_Co();
          request.objs = new String[] { emailAddresses, personalMsg };
          ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_SEND_EMAIL_INVITATION, request), 60000);
          DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
          if (msgAction instanceof SysANoop)
            closeDialog();
          else {
            setEnabledButtons(true);
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
    else {
      setEnabledButtons(true);
    }
  }

  private void setEnabledButtons(boolean enable) {
    jInvite.setEnabled(enable);
    jCancel.setEnabled(enable);
  }

  private void pressedCancel() {
    closeDialog();
  }

  public void closeDialog() {
    if (documentChangeListener != null) {
      if (jToText != null)
        jToText.getDocument().removeDocumentListener(documentChangeListener);
      documentChangeListener = null;
    }
    super.closeDialog();
  }


  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void insertUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void removeUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
  }

}