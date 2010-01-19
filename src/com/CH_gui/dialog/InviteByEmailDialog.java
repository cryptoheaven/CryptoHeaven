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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;

import com.CH_cl.service.ops.KeyOps;
import com.CH_cl.service.ops.UserOps;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.Cnt_NewCnt_Rq;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.msgs.MsgComposePanel;

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
 * <b>$Revision: 1.23 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class InviteByEmailDialog extends GeneralDialog {

  JLabel jInviteHeader = new JMyLabel("<html><body><font size='+1'>"+com.CH_gui.lang.Lang.rb.getString("label_Invite_Your_Friends_and_Associates.")+"</font></body></html>");
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
    doInvite(jToText.getText(), jBodyText.getText(), this);
  }

  public static void doInvite(String emlAddresses, String personalMessage, final InviteByEmailDialog dialog) {
    if (dialog != null)
      dialog.setEnabledButtons(false);

    final String emailAddresses = emlAddresses != null ? emlAddresses.trim() : null;
    final String personalMsg = personalMessage != null ? personalMessage.trim() : null;
    final String[] texts = Misc.getEmailInvitationText(personalMsg, null, FetchedDataCache.getSingleInstance().getUserRecord());

    // gather email addresses and nicks for adding to AddressBook
    final Vector emailAddressesV = new Vector();
    Vector emailNicksV = new Vector();
    if (emailAddresses != null) {
      StringTokenizer st = new StringTokenizer(emailAddresses, ",;:");
      while (st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        String[] emls = EmailRecord.gatherAddresses(token);
        if (emls != null && emls.length > 0) {
          for (int i=0; i<emls.length; i++) {
            String addrFull;
            // If emails were separeted by other than ,;: then we will have multiple addresses here.  Otherwise use the original token with original personal info.
            if (emls.length == 1)
              addrFull = token;
            else
              addrFull = emls[i];
            if (EmailRecord.findEmailAddress(emailAddressesV, addrFull) < 0) {
              emailAddressesV.addElement(addrFull);
              emailNicksV.addElement(EmailRecord.getPersonalOrNick(addrFull));
            }
          }
        }
      }
    }

    boolean shouldSend = emailAddressesV.size() > 0;
    if (shouldSend) {
      if (shouldSend == true) {
        Thread th = new ThreadTraced("Invitation Sender") {
          public void runTraced() {
            ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();

            // Check if that email address already belongs to an existing user account, create a contact too
            Object[] set = new Object[] { ArrayUtils.toArray(emailAddressesV, Object.class), Boolean.FALSE }; // do not auto-convert addresses to web-accounts
            SIL.submitAndWait(new MessageAction(CommandCodes.EML_Q_LOOKUP_ADDR, new Obj_List_Co(set)), 60000);
            FetchedDataCache cache = SIL.getFetchedDataCache();
            UserRecord myUser = cache.getUserRecord();
            Long myUserId = myUser.userId;
            Long shareId = cache.getFolderShareRecordMy(myUser.contactFolderId, false).shareId;
            BASymmetricKey folderSymKey = cache.getFolderShareRecord(shareId).getSymmetricKey();
            String contactReason = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {myUser.handle});

            for (int i=0; i<emailAddressesV.size(); i++) {
              String emlAddr = (String) emailAddressesV.elementAt(i);
              EmailRecord emlRec = cache.getEmailRecord(emlAddr);
              if (emlRec != null) {
                Long contactWithId = emlRec.userId;
                if (!myUserId.equals(contactWithId)) {
                  ContactRecord cRec = cache.getContactRecordOwnerWith(myUserId, contactWithId);
                  if (cRec == null) {
                    // Check if we have user's public key, if not fetch it
                    KeyRecord pubKey = cache.getKeyRecordForUser(contactWithId);
                    if (pubKey == null) {
                      Obj_IDList_Co request = new Obj_IDList_Co();
                      request.IDs = new Long[] { contactWithId };
                      MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
                      SIL.submitAndWait(msgAction, 60000);
                    }
                    Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
                    request.shareId = shareId;
                    request.contactRecord = new ContactRecord();
                    request.contactRecord.contactWithId = contactWithId;
                    request.contactRecord.setOwnerNote(emlAddr);
                    request.contactRecord.setOtherNote(contactReason);
                    request.contactRecord.setOtherSymKey(new BASymmetricKey(32));
                    request.contactRecord.seal(folderSymKey, cache.getKeyRecordForUser(contactWithId));
                    SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request));
                  }
                }
              }
            }

            // Send invites by Email
            // skip if email already belongs to my active or declined contact
            String filteredEmlAddresses = "";
            for (int i=0; i<emailAddressesV.size(); i++) {
              String emlAddr = (String) emailAddressesV.elementAt(i);
              EmailRecord emlRec = cache.getEmailRecord(emlAddr);
              if (emlRec != null) {
                Long contactWithId = emlRec.userId;
                if (!myUserId.equals(contactWithId)) {
                  ContactRecord cRec = cache.getContactRecordOwnerWith(myUserId, contactWithId);
                  if (cRec != null && (cRec.isOfActiveTypeAnyState() || cRec.isOfDeclinedTypeAnyState())) {
                    emlAddr = null;
                  }
                }
              }
              if (emlAddr != null) {
                if (filteredEmlAddresses.length() > 0)
                  filteredEmlAddresses += ",";
                filteredEmlAddresses += emlAddr;
              }
            }
            boolean inviteSuccess = true;
            if (filteredEmlAddresses.length() > 0) {
              Obj_List_Co request = new Obj_List_Co();
              request.objs = new String[] { filteredEmlAddresses, personalMsg };
              ClientMessageAction msgActionInv = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_SEND_EMAIL_INVITATION, request), 120000);
              DefaultReplyRunner.nonThreadedRun(SIL, msgActionInv);
              inviteSuccess = msgActionInv != null && msgActionInv.getActionCode() >= 0;
            }
            if (inviteSuccess && dialog != null)
              dialog.closeDialog();
            else if (!inviteSuccess && dialog != null)
              dialog.setEnabledButtons(true);
          }
        };
        th.setDaemon(true);
        th.start();
        // Add-at-once the email addresses that we sent invites to.
        MsgComposePanel.checkEmailAddressesForAddressBookAdition_Threaded(null, emailNicksV, emailAddressesV, false, new FolderFilter(FolderRecord.ADDRESS_FOLDER), true, null, true);
      }
    }
    if (!shouldSend) {
      if (dialog != null)
        dialog.setEnabledButtons(true);
    }
  }

  private void setEnabledButtons() {
    boolean formatOk = RecipientsDialog.isEmailLineValid(jToText.getText());
    jInvite.setEnabled(formatOk);
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