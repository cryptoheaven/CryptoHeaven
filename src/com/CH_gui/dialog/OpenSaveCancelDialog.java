/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;
import com.CH_gui.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class OpenSaveCancelDialog extends GeneralDialog {

  private static final int DEFAULT_OPEN_INDEX = 0;
  private static final int DEFAULT_SAVE_INDEX = 1;
  private static final int DEFAULT_CANCEL_INDEX = 2;

  private FileLinkRecord fileLink;
  private MsgLinkRecord parentMsg;
  private Runnable openTask;
  private Runnable saveTask;

  private JLabel jFromLabel;
  private JLabel jFromText;
  private JLabel jOriginalSignerLabel;
  private JLabel jOriginalSignerText;

  /** Creates new OpenSaveCancelDialog */
  public OpenSaveCancelDialog(Dialog parent, FileLinkRecord fileLink, MsgLinkRecord parentMsg, Runnable openTask, Runnable saveTask) {
    super(parent, "File Download - Security Warning");
    initialize(parent, fileLink, parentMsg, openTask, saveTask);
  }
  public OpenSaveCancelDialog(Frame parent, FileLinkRecord fileLink, MsgLinkRecord parentMsg, Runnable openTask, Runnable saveTask) {
    super(parent, "File Download - Security Warning");
    initialize(parent, fileLink, parentMsg, openTask, saveTask);
  }

  private void initialize(Component parent, FileLinkRecord fileLink, MsgLinkRecord parentMsg, Runnable openTask, Runnable saveTask) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OpenSaveCancelDialog.class, "OpenSaveCancelDialog()");

    this.fileLink = fileLink;
    this.parentMsg = parentMsg;
    this.openTask = openTask;
    this.saveTask = saveTask;

    JButton[] buttons = createButtons();
    JComponent mainComponent = createMainComponent();

    setModal(false);
    //MessageDialog.playSound(MessageDialog.WARNING_MESSAGE);
    init(parent, buttons, mainComponent, DEFAULT_SAVE_INDEX, DEFAULT_CANCEL_INDEX);

    final FileLinkRecord _fileLink = fileLink;
    final MsgLinkRecord _parentMsgLink = parentMsg;

    Thread th = new ThreadTraced("File-From Updater") {
      public void runTraced() {
        boolean isFromSet = false;
        boolean isSignerSet = false;
        ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
        FetchedDataCache cache = SIL.getFetchedDataCache();
        MsgDataRecord parentMsgData = null;
        Record emailSender = null;
        Record msgOriginator = null;
        Record fileOriginator = null;
        if (_fileLink.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
          parentMsgData = cache.getMsgDataRecord(_fileLink.ownerObjId);
        }
        String fromEmailAddress = null;
        if (parentMsgData != null)
          fromEmailAddress = parentMsgData.getFromEmailAddress();
        if (parentMsgData != null && (parentMsgData.isEmail() || fromEmailAddress != null)) {
          // email delivered from outside
          emailSender = CacheUtilities.convertToFamiliarEmailRecord(fromEmailAddress);
          jFromText.setText(ListRenderer.getRenderedText(emailSender, false, false, true));
          jFromText.setIcon(ListRenderer.getRenderedIcon(emailSender));
          jOriginalSignerText.setText("Mail Server");
          isFromSet = true;
          isSignerSet = true;
        } else {
          // internal Mail message or File from folder
          if (parentMsgData != null) {
            Long senderUserId = parentMsgData.senderUserId;
            msgOriginator = MsgPanelUtils.convertUserIdToFamiliarUser(senderUserId, true, true, true);
            if (msgOriginator == null) {
              SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(senderUserId)), 30000);
              msgOriginator = MsgPanelUtils.convertUserIdToFamiliarUser(senderUserId, true, true, true);
            }
            jFromText.setText(ListRenderer.getRenderedText(msgOriginator));
            jFromText.setIcon(ListRenderer.getRenderedIcon(msgOriginator));
            isFromSet = true;
          }
          FileDataRecord fileData = cache.getFileDataRecord(_fileLink.fileId);
          if (fileData == null) {
            Obj_IDs_Co request = new Obj_IDs_Co();
            if (_parentMsgLink != null) {
              // request for attachment
              request.IDs = new Long[3][];
              if (_parentMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
                FolderShareRecord[] parentShares = cache.getFolderShareRecordsMyRootsForMsgs(new MsgLinkRecord[] { _parentMsgLink }, true);
                request.IDs[1] = new Long[] { parentShares[0].shareId };
              } else {
                // don't provide share id for nested attachments, server will check access using a longer way
                request.IDs[1] = new Long[0];
              }
              request.IDs[2] = new Long[] { _parentMsgLink.msgLinkId };
            } else {
              // request for file in folder
              request.IDs = new Long[2][];
              request.IDs[1] = new Long[] { cache.getFolderShareRecordMy(_fileLink.ownerObjId, true).shareId };
            }
            // always include file link Id
            request.IDs[0] = new Long[] { _fileLink.fileLinkId };

            MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES, request);
            ClientMessageAction replyMsg = SIL.submitAndFetchReply(msgAction, 30000);
            Misc.suppressMsgDialogsGUI(true);
            DefaultReplyRunner.nonThreadedRun(SIL, replyMsg);
            Misc.suppressMsgDialogsGUI(false);
            fileData = cache.getFileDataRecord(_fileLink.fileId);
          }
          if (fileData != null) {
            Long keyId = fileData.getSigningKeyId();
            KeyRecord keyRec = cache.getKeyRecord(keyId);
            if (keyRec == null) {
              SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyId)), 30000);
              keyRec = cache.getKeyRecord(keyId);
            }
            if (keyRec != null) {
              fileOriginator = MsgPanelUtils.convertUserIdToFamiliarUser(keyRec.ownerUserId, true, true, true);
              if (fileOriginator == null) {
                SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(keyRec.ownerUserId)), 30000);
                fileOriginator = MsgPanelUtils.convertUserIdToFamiliarUser(keyRec.ownerUserId, true, true, true);
              }
              jOriginalSignerText.setText(ListRenderer.getRenderedText(fileOriginator));
              jOriginalSignerText.setIcon(ListRenderer.getRenderedIcon(fileOriginator));
              isSignerSet = true;
            }
          }
          if (!isFromSet) {
            jFromText.setText("Unknown user!");
          }
          if (!isSignerSet) {
            jOriginalSignerText.setText("Unknown user!");
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
    if (trace != null) trace.exit(OpenSaveCancelDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Open"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOpen();
      }
    });

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Download"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedSave();
      }
    });

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }

  private JComponent createMainComponent() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    JMyLabel openOrSaveLabel = new JMyLabel("Do you want to open or save this file?");
    openOrSaveLabel.setFont(openOrSaveLabel.getFont().deriveFont(Font.BOLD));

    int posY = 0;
    panel.add(openOrSaveLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // Name line
    panel.add(new JMyLabel("Name:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 15, 2, 5), 0, 0));
    Object[] iconAndType = FileTypesIcons.getFileIconAndType(fileLink.getFileName());
    JMyLabel jFileName = new JMyLabel(fileLink.getFileName());
    jFileName.setIcon((Icon) iconAndType[0]);
    panel.add(jFileName, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;

    // Type line
    panel.add(new JMyLabel("Type:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 15, 2, 5), 0, 0));
    JMyLabel jFileType = new JMyLabel((String) iconAndType[1] + ", " + Misc.getFormattedSize(fileLink.origSize, 3, 2));
    panel.add(jFileType, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    posY ++;

    // From line
    jFromLabel = new JMyLabel("From:");
    jFromLabel.setVisible(parentMsg != null);
    panel.add(jFromLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 15, 2, 5), 0, 0));
    jFromText = new JMyLabel();
    jFromText.setVisible(parentMsg != null);
    panel.add(jFromText, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    posY ++;

    // From original signer
    jOriginalSignerLabel = new JMyLabel("File Signed By:");
    panel.add(jOriginalSignerLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 15, 5, 5), 0, 0));
    jOriginalSignerText = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("Fetching_Data..."));
    panel.add(jOriginalSignerText, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    posY ++;

    JPanel warnPanel = new JPanel();
    warnPanel.setBorder(new LineBorder(warnPanel.getBackground().darker(), 1, true));
    warnPanel.setLayout(new GridBagLayout());
    warnPanel.add(new JMyLabel(Images.get(ImageNums.SHIELD32)), new GridBagConstraints(0, 0, 1, 3, 0, 0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    warnPanel.add(new JMyLabel("While files from the Internet can be useful, this file can"), new GridBagConstraints(1, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    warnPanel.add(new JMyLabel("potentially harm your computer. If you do not trust the source,"), new GridBagConstraints(1, 1, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 1, 5), 0, 0));
    warnPanel.add(new JMyLabel("do not open or save this file."), new GridBagConstraints(1, 2, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));

    // warn panel
    panel.add(warnPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    return panel;
  }

  private void pressedOpen() {
    openTask.run();
    closeDialog();
  }

  private void pressedSave() {
    saveTask.run();
    closeDialog();
  }

  private void pressedCancel() {
    closeDialog();
  }

}