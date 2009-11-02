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
import javax.swing.text.*;

import com.CH_co.cryptx.*;
import com.CH_cl.service.actions.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.key.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class InitiateContactDialog extends GeneralDialog {//implements VisualsSavable {

  JLabel jContactWith;
  JLabel jOwnerEncryption;
  JTextField jContactName;
  JLabel jOtherEncryption;
  JTextArea jContactReason;

  JButton jOk;

  static final int DEFAULT_OK_BUTTON = 0;
  static final int DEFAULT_CANCEL_BUTTON = 1;

  Long[] contactWithIds;
  Long shareId;

  ServerInterfaceLayer SIL;
  FetchedDataCache cache;

  private DocumentChangeListener documentChangeListener;

  /** Creates new InitiateContactDialog */
  public InitiateContactDialog(Frame owner, Long[] contactWithIds) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Add_New_Contact"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InitiateContactDialog.class, "InitiateContactDialog(Frame owner, Long[] contactWithIds)");
    if (trace != null) trace.args(contactWithIds);
    initialize(owner, contactWithIds);
    if (trace != null) trace.exit(InitiateContactDialog.class);
  }
  /** Creates new InitiateContactDialog */
  public InitiateContactDialog(Dialog owner, Long[] contactWithIds) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Add_New_Contact"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InitiateContactDialog.class, "InitiateContactDialog(Dialog owner, Long[] contactWithIds)");
    if (trace != null) trace.args(contactWithIds);
    initialize(owner, contactWithIds);
    if (trace != null) trace.exit(InitiateContactDialog.class);
  }
  private void initialize(Component owner, Long[] contactWithIds) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InitiateContactDialog.class, "initialize(Component owner, Long[] contactWithIds)");
    this.contactWithIds = contactWithIds;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    this.shareId = cache.getFolderShareRecordMy(cache.getUserRecord().contactFolderId, false).shareId;
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = SIL.getFetchedDataCache();FetchedDataCache.getSingleInstance();

    JButton[] jButtons = createButtons();
    JPanel jPanel = createPanel();
    setEnabledButtons();

    super.init(owner, jButtons, jPanel, DEFAULT_OK_BUTTON, DEFAULT_CANCEL_BUTTON);

    jContactReason.requestFocus();
    getPubKey();

    if (trace != null) trace.exit(InitiateContactDialog.class);
  }

  private void getPubKey() {
    Thread th = new Thread("Initiater Contact -- Public Key Fetcher") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        try {
          Obj_IDList_Co request = new Obj_IDList_Co();
          request.IDs = contactWithIds;
          MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
          ClientMessageAction replyMsg = SIL.submitAndFetchReply(msgAction, 60000);
          DefaultReplyRunner.nonThreadedRun(SIL, replyMsg);
          setEnabledButtons();

          if (contactWithIds.length == 1) {
            KeyRecord otherKeyRec = cache.getKeyRecordForUser(contactWithIds[0]);
            if (otherKeyRec != null) {
              jOtherEncryption.setText(otherKeyRec.plainPublicKey.shortInfo() + "/" + "AES(256)");
              jOtherEncryption.setIcon(otherKeyRec.getIcon());
            }
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
        }

        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private JButton[] createButtons() {
    JButton[] jButtons = new JButton[2];

    jButtons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    jButtons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });

    jButtons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    jButtons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    jOk = jButtons[0];

    return jButtons;
  }

  private JPanel createPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    panel.add(new JMyLabel(Images.get(ImageNums.CONTACT_NEW32)), new GridBagConstraints(0, 0, 1, 2, 0, 0, 
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    documentChangeListener = new DocumentChangeListener();
    jContactReason = new JMyTextArea(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {cache.getUserRecord().handle}), 3, 30);
    jContactReason.setWrapStyleWord(true);
    jContactReason.setLineWrap(true);
    jContactReason.getDocument().addDocumentListener(documentChangeListener);

    Keymap contactReasonMap = jContactReason.getKeymap();
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    contactReasonMap.removeKeyStrokeBinding(enter);

    if (contactWithIds.length == 1) {
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_To")), new GridBagConstraints(1, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Your_Encryption")), new GridBagConstraints(1, 1, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_Name")), new GridBagConstraints(1, 2, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact's_Encryption")), new GridBagConstraints(1, 3, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

      UserRecord uRec = cache.getUserRecord(contactWithIds[0]);
      jContactWith = new JMyLabel(uRec.shortInfo(), uRec.getIcon(), JLabel.LEFT);
      KeyRecord kRec = cache.getKeyRecord(cache.getUserRecord().currentKeyId);
      jOwnerEncryption = new JMyLabel(kRec.plainPublicKey.shortInfo() + "/" + "AES(256)", kRec.getIcon(), JLabel.LEFT);

      jContactName = new JMyTextField(uRec.handle, 15);
      jContactName.getDocument().addDocumentListener(documentChangeListener);

      jOtherEncryption = new JMyLabel(".../" + "AES(256)");

      Keymap contactNameMap = jContactName.getKeymap();
      contactNameMap.removeKeyStrokeBinding(enter);

      panel.add(jContactWith, new GridBagConstraints(2, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(jOwnerEncryption, new GridBagConstraints(2, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(jContactName, new GridBagConstraints(2, 2, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(jOtherEncryption, new GridBagConstraints(2, 3, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    }


    // Always include Reason field
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_Reason")), new GridBagConstraints(1, contactWithIds.length == 1 ? 4 : 0, 2, 1, 0, 0, 
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JScrollPane(jContactReason), new GridBagConstraints(1, contactWithIds.length == 1 ? 5 : 1, 2, 3, 10, 10, 
      GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }

  private void setEnabledButtons() {
    boolean hasAllKeys = true;
    for (int i=0; i<contactWithIds.length; i++) {
      KeyRecord otherKeyRec = cache.getKeyRecordForUser(contactWithIds[i]);
      if (otherKeyRec == null) {
        hasAllKeys = false;
        break;
      }
    }
    // see if Name or Reason has changed
    boolean enable = hasAllKeys && jContactReason.getText().trim().length() > 0;
    jOk.setEnabled(enable);
  }

  private void pressedOK() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InitiateContactDialog.class, "pressedOK()");
    closeDialog();

    Thread th = new Thread("Initiate Contact -- Pressed OK Submitter") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        // change the priority of this thread to minimum
        setPriority(MIN_PRIORITY);

        try {
          BASymmetricKey folderSymKey = cache.getFolderShareRecord(shareId).getSymmetricKey();

          for (int i=0; i<contactWithIds.length; i++) {
            //1310 <shareId>   <contactWithId> <encOwnerNote> <otherKeyId> <encOtherSymKey> <encOtherNote>

            // process each contact separately one at a time
            Long contactWithId = contactWithIds[i];

            Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
            request.shareId = shareId;
            request.contactRecord = new ContactRecord();
            request.contactRecord.contactWithId = contactWithId;
            request.contactRecord.setOwnerNote(jContactName != null && jContactName.getText().trim().length() > 0 ? jContactName.getText().trim() : cache.getUserRecord(contactWithId).handle);
            request.contactRecord.setOtherNote(jContactReason.getText().trim());
            request.contactRecord.setOtherSymKey(new BASymmetricKey(32));
            request.contactRecord.seal(folderSymKey, cache.getKeyRecordForUser(contactWithId));

            SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request));
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(InitiateContactDialog.class, 100, t);
        }

        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(InitiateContactDialog.class);
  }

  private void pressedCancel() {
    closeDialog();
  }


  public void closeDialog() {
    if (documentChangeListener != null) {
      if (jContactReason != null)
        jContactReason.getDocument().removeDocumentListener(documentChangeListener);
      if (jContactName != null)
        jContactName.getDocument().removeDocumentListener(documentChangeListener);
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
//
//  /*******************************************************
//  *** V i s u a l s S a v a b l e    interface methods ***
//  *******************************************************/
//  public static final String visualsClassKeyName = "InitiateContactDialog";
//  public String getVisualsClassKeyName() {
//    return visualsClassKeyName;
//  }
}