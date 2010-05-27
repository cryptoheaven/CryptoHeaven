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
import com.CH_gui.util.Images;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.*;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.*;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_gui.msgs.*;
import com.CH_gui.service.records.RecordUtilsGui;

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
 * <b>$Revision: 1.37 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgPropertiesDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_CANCEL_INDEX = 1;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;
  private MsgLinkRecord msgLink;
  private Record msgParent;

  private JLabel jSubject;
  private JLabel jSize;
  private JLabel jFromLabel;
  private JLabel jFrom;
  private JLabel jReplyToLabel;
  private JLabel jReplyTo;
  private JPanel jRecipientsTO;
  private JPanel jRecipientsCC;
  private JPanel jAttachments;
  private JLabel jPriority;
  private JLabel jSecureLock;
  private JPanel jPasswordPane;
  private JLabel jDelivered;
  private JLabel jExpiration;
  private JButton jExpirationChange;

  private JLabel jSizeOnDisk;
  private JLabel jSigningUser;
  private JLabel jKeyID;
  private JLabel jKeyInfo;
  private JTextField jDigest;
  private JTextField jEncDataDigest;
  private JLabel jVerifyOK;
  private JLabel jDataCreated;

  private JLabel jSubjectPlainLabel;
  private JTextField jSubjectPlain;
  private JLabel jBodyPlainLabel;
  private JTextArea jBodyPlain;

  // main panels in the notebook
  private JPanel jGeneralPanel;
  private JPanel jDataPanel;
  private JPanel jDetailsPanel;

  private JButton jTranscript;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  private MsgDataListener msgDataListener;

  /** Creates new MsgPropertiesDialog */
  public MsgPropertiesDialog(Frame owner, MsgLinkRecord msgLink) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Message_Properties"));
    constructDialog(owner, msgLink);
  }
  /** Creates new MsgPropertiesDialog */
  public MsgPropertiesDialog(Dialog owner, MsgLinkRecord msgLink) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Message_Properties"));
    constructDialog(owner, msgLink);
  }
  private void constructDialog(Component owner, MsgLinkRecord msgLink) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPropertiesDialog.class, "constructDialog(Component owner, MsgLinkRecord msgLink)");
    if (trace != null) trace.args(owner, msgLink);

    this.msgLink = msgLink;
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = SIL.getFetchedDataCache();

    if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
      Long folderId = msgLink.ownerObjId;
      msgParent = new FolderPair(cache.getFolderShareRecordMy(folderId, true), cache.getFolderRecord(folderId));
      if (msgParent == null) {
        throw new IllegalArgumentException("Don't know who the message's parent folder is!");
      }
    } else if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
      msgParent = cache.getMsgDataRecord(msgLink.ownerObjId);
    } else {
      throw new IllegalArgumentException("Don't know how to handle non-folder and non-attachment messages.");
    }

    JButton[] buttons = createButtons();
    JComponent mainComponent = createTabbedPane();

    init(owner, buttons, mainComponent, -1, DEFAULT_CANCEL_INDEX);

    cache.addMsgDataRecordListener(msgDataListener = new MsgDataListener());
    fetchData();

    if (trace != null) trace.exit(MsgPropertiesDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Transcript"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedTranscript();
      }
    });
    jTranscript = buttons[0];
    jTranscript.setEnabled(false);

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }

  private void pressedCancel() {
    closeDialog();
  }

  private void pressedTranscript() {
    MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
    KeyRecord kRec = cache.getKeyRecord(msgData.getSendPrivKeyId());
    String RSA = kRec.plainPublicKey.shortInfo().toUpperCase();
    FolderShareRecord shareRec = cache.getFolderShareRecordMy(msgLink.ownerObjId, true);
    UserRecord userRec = cache.getUserRecord();
    KeyRecord myKeyRec = cache.getKeyRecordMyCurrent();
    StringBuffer sb = new StringBuffer();
    sb.append("--- BEGIN RECEIVED MESSAGE");
    sb.append("\n--- BEGIN AES(256) ENCRYPTED MESSAGE SUBJECT\n\n");
    sb.append(ArrayUtils.breakLines(msgData.getEncSubject().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED MESSAGE SUBJECT");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED MESSAGE BODY\n\n");
    sb.append(ArrayUtils.breakLines(msgData.getEncText().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED MESSAGE BODY");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED SHA-256 DIGEST OF AES(256) ENCRYPTED MESSAGE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(msgData.getEncEncDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED SHA-256 DIGEST OF AES(256) ENCRYPTED MESSAGE");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN MESSAGE\n\n");
    sb.append(ArrayUtils.breakLines(msgData.getEncSignedDigest().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN MESSAGE");

    sb.append("\n\n--- BEGIN "+RSA+" PUBLIC PLAIN SIGNING KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.toString(kRec.plainPublicKey.objectToBytes()), 80));
    sb.append("\n\n--- END "+RSA+" PUBLIC PLAIN SIGNING KEY");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED MESSAGE AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(msgLink.getEncSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED MESSAGE AES(256) KEY");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FOLDER AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(shareRec.getEncSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER AES(256) KEY");

    sb.append("\n\n--- BEGIN "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS\n\n");
    sb.append(ArrayUtils.breakLines(userRec.getEncSymKeys().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS");

    sb.append("\n\n--- BEGIN AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY\n\n");
    sb.append(ArrayUtils.breakLines(myKeyRec.getEncPrivateKey().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY");

    sb.append("\n--- END RECEIVED MESSAGE");


    sb.append("\n\n--- BEGIN COMPUTED MESSAGE");
    sb.append("\n--- BEGIN MESSAGE AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(msgLink.getSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END MESSAGE AES(256) KEY");

    sb.append("\n\n--- BEGIN PLAIN MESSAGE SUBJECT\n\n");
    sb.append(msgData.getSubject());
    sb.append("\n\n--- END PLAIN MESSAGE SUBJECT");

    sb.append("\n\n--- BEGIN PLAIN MESSAGE BODY\n\n");
    sb.append(msgData.getTextBody() != null ? msgData.getTextBody() : "");
    sb.append("\n\n--- END PLAIN MESSAGE BODY");

    sb.append("\n\n--- BEGIN SHA-256 DIGEST OF AES(256) ENCRYPTED MESSAGE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(msgData.getEncDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END SHA-256 DIGEST OF AES(256) ENCRYPTED MESSAGE");

    sb.append("\n\n--- BEGIN "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN MESSAGE\n\n");
    sb.append(ArrayUtils.breakLines(msgData.getSignedDigest().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN MESSAGE");

    sb.append("\n\n--- BEGIN SHA-256 DIGEST OF PLAIN MESSAGE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(msgData.getDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END SHA-256 DIGEST OF PLAIN MESSAGE");
    sb.append("\n--- END COMPUTED MESSAGE");

    JTextArea textArea = new JMyTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setCaretPosition(0);
    textArea.setRows(35);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JButton jClose = new JMyButton("Close");
    jClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Dialog d = (Dialog) SwingUtilities.windowForComponent((Component) e.getSource());
        d.dispose();
      }
    });
    new GeneralDialog(this, com.CH_gui.lang.Lang.rb.getString("title_Message_Transcript"), new JButton[] { jClose }, -1, 0, new JScrollPane(textArea));
  }

  private JTabbedPane createTabbedPane() {
    JTabbedPane pane = new JMyTabbedPane();

    jGeneralPanel = createGeneralPanel();
    jDataPanel = createDataPanel();
    jDetailsPanel = createDetailsPanel();

    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_General"), jGeneralPanel);
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Data"), jDataPanel);
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Details"), jDetailsPanel);

    return pane;
  }

  private JPanel createGeneralPanel() {
    MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);

    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    jSubject = new JMyLabel(FETCHING_DATA);
    jFrom = new JMyLabel(FETCHING_DATA);
    jReplyTo = new JMyLabel(FETCHING_DATA);
    jSize = new JMyLabel(FETCHING_DATA);
    jRecipientsTO = new JPanel();
    jRecipientsTO.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    jRecipientsTO.setBorder(new EmptyBorder(0,0,0,0));
    jRecipientsCC = new JPanel();
    jRecipientsCC.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    jRecipientsCC.setBorder(new EmptyBorder(0,0,0,0));
    jAttachments = new JPanel();
    jAttachments.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    jAttachments.setBorder(new EmptyBorder(0,0,0,0));
    jPriority = new JMyLabel(FETCHING_DATA);
    jSecureLock = new JMyLabel(FETCHING_DATA);
    jPasswordPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    if (msgData.bodyPassHash != null && msgData.getTextBody() == null) {
      final JTextField jPasswordField = new JMyTextField("Enter message password here", 20);
      jPasswordField.selectAll();
      jPasswordPane.add(jPasswordField);
      jPasswordField.addKeyListener(new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
          MsgDataRecord msgDataRecord = cache.getMsgDataRecord(msgLink.msgId);
          Hasher.Set matchingSet = MsgPanelUtils.getMatchingPasswordHasher(msgDataRecord, jPasswordField.getText());
          if (matchingSet != null) {
            if (jPasswordPane.getComponentCount() == 1) {
              jPasswordPane.add(new JMyLabel(" match"));
              jPasswordPane.revalidate();
              jPasswordPane.repaint();
            }
            MsgPanelUtils.unlockPassProtectedMsg(msgDataRecord, matchingSet);
          } else if (jPasswordPane.getComponentCount() == 2) {
            jPasswordPane.remove(1);
            jPasswordPane.revalidate();
            jPasswordPane.repaint();
          }
        }
      });
    } else if (msgData.bodyPassHash != null && msgData.getTextBody() != null) {
      jPasswordPane.add(new JMyLabel("Password protected, unlocked"));
    } else if (msgData.bodyPassHash == null) {
      jPasswordPane.add(new JMyLabel("None"));
    }
    jDelivered = new JMyLabel(FETCHING_DATA);
    jExpiration = new JMyLabel(FETCHING_DATA);
    if (msgData.senderUserId.equals(cache.getMyUserId())) {
      jExpirationChange = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Change"));
      jExpirationChange.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));
      jExpirationChange.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          new ExpiryRevocationDialog(MsgPropertiesDialog.this, new MsgLinkRecord[] { msgLink });
        }
      });
    }

    int posY = 0;

    boolean isPosting = false;
    boolean isMail = false;
    boolean isAddress = false;

    if (msgData.isTypeAddress()) {
      isAddress = true;
    } else {
      if (msgParent instanceof FolderPair) {
        short folderType = ((FolderPair) msgParent).getFolderRecord().folderType.shortValue();
        isPosting = folderType == FolderRecord.POSTING_FOLDER || folderType == FolderRecord.CHATTING_FOLDER;
      }
      isMail = !isPosting;
    }
    Icon iconLarge = null;
    Icon iconSamll = null;
    String typeText = "";
    if (isMail) {
      iconLarge = Images.get(ImageNums.MAIL_CERT32);
      iconSamll = Images.get(ImageNums.MAIL_READ16);
      typeText = com.CH_gui.lang.Lang.rb.getString("msgType_Mail_Message");
    } else if (isPosting) {
      iconLarge = Images.get(ImageNums.POSTING_CERT32);
      iconSamll = Images.get(ImageNums.POSTING16);
      typeText = com.CH_gui.lang.Lang.rb.getString("msgType_Posting_Message");
    } else if (isAddress) {
      iconLarge = Images.get(ImageNums.ADDRESS32);
      iconSamll = Images.get(ImageNums.ADDRESS16);
      typeText = com.CH_gui.lang.Lang.rb.getString("msgType_Address_Contact");
    }
    panel.add(new JMyLabel(iconLarge), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jSubject, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Type")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    JLabel jType = new JMyLabel();
    jType.setText(typeText);
    jType.setIcon(iconSamll);
    panel.add(jType, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Location")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(new JMyLabel(ListRenderer.getRenderedText(msgParent), ListRenderer.getRenderedIcon(msgParent), JLabel.LEFT), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Size")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jSize, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    jFromLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_From"));
    panel.add(jFromLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jFrom, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    jReplyToLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Reply_To"));
    panel.add(jReplyToLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jReplyTo, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    JScrollPane sc = new JScrollPane(jRecipientsTO, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sc.getVerticalScrollBar().setUnitIncrement(5);
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_To")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(sc, new GridBagConstraints(1, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    sc = new JScrollPane(jRecipientsCC, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sc.getVerticalScrollBar().setUnitIncrement(5);
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Cc")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(sc, new GridBagConstraints(1, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    sc = new JScrollPane(jAttachments, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sc.getVerticalScrollBar().setUnitIncrement(5);
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Attachments")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(sc, new GridBagConstraints(1, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Priority")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jPriority, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Security")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jSecureLock, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jPasswordPane, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Status")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(new JMyLabel(msgLink.getStatusName()), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;

//
//    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Encryption")), new GridBagConstraints(0, posY, 1, 1, 0, 0, 
//          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
//    panel.add(new JMyLabel("AES(256)"), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
//          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
//    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Sent")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(msgLink.dateCreated)), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Received")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jDelivered, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Expiration")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    panel.add(jExpiration, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(4, 5, 4, 5), 0, 0));
    if (jExpirationChange != null) {
      panel.add(jExpirationChange, new GridBagConstraints(2, posY, 1, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(4, 5, 4, 5), 0, 0));
    }
    posY ++;


//    // Add a vertical filler.
//    panel.add(new JMyLabel(), new GridBagConstraints(1, posY, 2, 1, 10, 10, 
//          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(4, 5, 4, 5), 0, 0));

    return panel;
  }


  private JPanel createDataPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    jSizeOnDisk = new JMyLabel(FETCHING_DATA);
    jSigningUser = new JMyLabel(FETCHING_DATA);
    jKeyID = new JMyLabel(FETCHING_DATA);
    jKeyInfo = new JMyLabel(FETCHING_DATA);
    jDigest = new JMyTextField(FETCHING_DATA, 32);
    jDigest.setEditable(false);
    jEncDataDigest = new JMyTextField(FETCHING_DATA, 32);
    jEncDataDigest.setEditable(false);
    jVerifyOK = new JMyLabel(FETCHING_DATA);
    jDataCreated = new JMyLabel(FETCHING_DATA);

    int posY = 0;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Link_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(msgLink.msgLinkId.toString()), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Data_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(msgLink.msgId.toString()), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    Long replyToMsgId = cache.getMsgDataRecord(msgLink.msgId).replyToMsgId;
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_In_reply_to")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(replyToMsgId == null ? "" : replyToMsgId.toString()), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Size_on_Disk")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jSizeOnDisk, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_User")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jSigningUser, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_Key_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jKeyID, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_Key_Info")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jKeyInfo, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;




    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Verification")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jVerifyOK, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Digest_of_Plain_Message_(SHA-256)")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jDigest, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Digest_of_Encrypted_Message_(SHA-256)")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jEncDataDigest, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Data_Created")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jDataCreated, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Link_Updated")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(msgLink.dateUpdated)), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    /*
    panel.add(jTranscript, new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
     */

    // Add a vertical filler.
    panel.add(new JMyLabel(), new GridBagConstraints(1, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }


  private JPanel createDetailsPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    jSubjectPlain = new JMyTextField(FETCHING_DATA);
    jSubjectPlain.setEditable(false);

    jBodyPlain = new JMyTextArea(FETCHING_DATA);
    jBodyPlain.setWrapStyleWord(true);
    jBodyPlain.setLineWrap(true);
    jBodyPlain.setEditable(false);

    int posY = 0;

    jSubjectPlainLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Message_Subject_Plain_Text"));
    panel.add(jSubjectPlainLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jSubjectPlain, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    jBodyPlainLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Message_Body_Plain_Text"));
    panel.add(jBodyPlainLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JScrollPane(jBodyPlain), new GridBagConstraints(1, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    return panel;
  }



  private void fetchData() {
    Thread th = new ThreadTraced("Message Properties Data Fetcher") {
      public void runTraced() {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        // fetch Message Data
        MsgDataRecord dataRecord = cache.getMsgDataRecord(msgLink.msgId);

        // See if we have the senders public key to verify the body, if not, fetch it now.
        // If we only had a brief, we won't have it and we'll have to try again after BODY FETCH
        Long keyId = dataRecord.getSendPrivKeyId();
        if (keyId != null && cache.getKeyRecord(keyId) == null) {
          SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyId)), 60000);
        }

        // Data will never be null, its impossible to fetch links without some kind of data record.
        // However, if its a brief, then there is no Text and no SendPrivKeyId
        if (dataRecord.getEncText() == null) {
          // If no body, fetch the body.
          Obj_IDList_Co request = null;
          if (msgParent instanceof FolderPair) {
            request = new Obj_IDList_Co(new Long[] { ((FolderPair) msgParent).getFolderShareRecord().shareId, msgLink.msgLinkId });
          } else {
            request = new Obj_IDList_Co(new Long[] { null, msgLink.msgLinkId });
          }
          MessageAction msgAction = new MessageAction(CommandCodes.MSG_Q_GET_BODY, request);
          SIL.submitAndWait(msgAction, 60000);
          // When the reply is processed, the MsgData record hold by our reference should be updated,
          // also, the link will get updated with potentially new delivery timestamp, etc.

          // Check if we still don't have the key - this is possible if we skipped the first key fetch due to having only a MSG BRIEF and NO BODY
          keyId = dataRecord.getSendPrivKeyId();
          if (keyId != null && cache.getKeyRecord(keyId) == null) {
            SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyId)), 60000);
            updateData(dataRecord);
          }
        } else {
          updateData(dataRecord);
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private void updateData(final MsgDataRecord dataRecord) {
    // complete digest verification if not already done
    if (dataRecord.isDigestOk() == null || dataRecord.isEncDigestOk() == null) {
      MsgDataOps.tryToUnsealMsgDataWithVerification(dataRecord);
    }

    jTranscript.setEnabled(true);

    if (dataRecord.isTypeAddress()) {
      jSubject.setText(dataRecord.name);
    } else {
      jSubject.setText(dataRecord.getSubject());
    }

    long size = dataRecord.recordSize.longValue();
    String oSize = Misc.getFormattedSize(size, 3, 2);
    if (size >= 1000)
      oSize += " (" + Misc.getFormattedSize(size, 10, 10) + ")";
    jSizeOnDisk.setText(oSize);

    // jFrom email address, contact or user
    Record fromRec = null;
    Record signRec = MsgPanelUtils.convertUserIdToFamiliarUser(dataRecord.senderUserId, false, true);
    if (dataRecord.isEmail()) {
      fromRec = new EmailAddressRecord(dataRecord.getFromEmailAddress());
    } else {
      fromRec = signRec;
    }
    if (fromRec != null) {
      jFrom.setIcon(ListRenderer.getRenderedIcon(fromRec));
      String text = ListRenderer.getRenderedText(fromRec);
      if (fromRec instanceof ContactRecord)
        jFrom.setText(text + " (user ID: " + dataRecord.senderUserId + ")");
      else
        jFrom.setText(text);
    } else {
      jFrom.setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {dataRecord.senderUserId}));
      jFrom.setIcon(Images.get(ImageNums.PERSON_SMALL));
    }

    String[] replyTos = dataRecord.getReplyToAddresses();
    if (replyTos != null && (replyTos.length > 1 || (replyTos.length == 1 && !EmailRecord.isAddressEqual(replyTos[0], dataRecord.getFromEmailAddress())))) {
      jReplyTo.setIcon(Images.get(ImageNums.EMAIL_SYMBOL_SMALL));
      StringBuffer _replyTos = new StringBuffer();
      for (int i=0; i<replyTos.length; i++) {
        if (i > 0)
          _replyTos.append(", ");
        _replyTos.append(replyTos[i]);
      }
      jReplyTo.setText(_replyTos.toString());
    } else {
      jReplyToLabel.setVisible(false);
      jReplyTo.setVisible(false);
    }

    if (signRec != null) {
      jSigningUser.setIcon(ListRenderer.getRenderedIcon(signRec));
      String text = ListRenderer.getRenderedText(signRec);
      if (signRec instanceof ContactRecord)
        jSigningUser.setText(text + " (user ID: " + dataRecord.senderUserId + ")");
      else
        jSigningUser.setText(text);
    } else {
      jSigningUser.setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {dataRecord.senderUserId}));
      jSigningUser.setIcon(Images.get(ImageNums.PERSON_SMALL));
    }


    if (dataRecord.getTextBody() != null) {
      int len = dataRecord.getSubject().length() + dataRecord.getTextBody().length();
      jSize.setText(Misc.getFormattedSize(len, 4, 3));
    } else {
      jSize.setText(com.CH_gui.lang.Lang.rb.getString("unknown"));
    }

    ImageText pri = dataRecord.getPriorityTextAndIcon();
    jPriority.setText(pri.getText());
    jPriority.setIcon(Images.get(pri));

    ImageText sec = dataRecord.getSecurityTextAndIcon();
    jSecureLock.setText(sec.getText());
    jSecureLock.setIcon(Images.get(sec));

    if (dataRecord.bodyPassHash != null && dataRecord.getEncText() != null && dataRecord.getEncText().size() > 0 && dataRecord.getTextBody() != null) {
      jPasswordPane.removeAll();
      jPasswordPane.add(new JMyLabel("Password protected, unlocked"));
    }

    jDelivered.setText(Misc.getFormattedTimestamp(msgLink.dateDelivered));

    ImageText exp = dataRecord.getExpirationIconAndText(cache.getMyUserId());
    jExpiration.setIcon(Images.get(exp));
    jExpiration.setText(exp.getText());

    // list attachments
    // prepare requests
    Obj_IDs_Co request = null;
    int sumFiles = dataRecord.attachedFiles.shortValue();
    int sumMsgs = dataRecord.attachedMsgs.shortValue();
    if (sumFiles > 0 || sumMsgs > 0) {
      request = new Obj_IDs_Co();
      request.IDs = new Long[2][];
      request.IDs[0] = new Long[] { msgLink.msgLinkId };
      Long[] folderIDs = new Long[] { msgLink.ownerObjId };
      FolderShareRecord[] shareRecords = cache.getFolderSharesMyForFolders(folderIDs, true);
      request.IDs[1] = RecordUtils.getIDs(shareRecords);
    }

    if (sumFiles > 0 || sumMsgs > 0) {
      Runnable afterJob = new Runnable() {
        public void run() {
          MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsOwnerAndType(msgLink.msgId, new Short(Record.RECORD_TYPE_MESSAGE));
          FileLinkRecord[] fileLinks = cache.getFileLinkRecordsOwnerAndType(msgLink.msgId, new Short(Record.RECORD_TYPE_MESSAGE));
          final Record[] records = RecordUtils.concatinate(msgLinks, fileLinks);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              MsgPanelUtils.drawRecordFlowPanel(records, jAttachments);
            }
          });
        }
      };
      // send requests
      if (sumFiles > 0)
        SIL.submitAndReturn(new MessageAction(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS, request), 30000, afterJob, null);
      if (sumMsgs > 0)
        SIL.submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS, request), 30000, afterJob, null);
    }

    // jKeyID and jKeyInfo
    KeyRecord kRec = cache.getKeyRecord(dataRecord.getSendPrivKeyId());
    if (dataRecord.getSendPrivKeyId() != null)
      jKeyID.setText(dataRecord.getSendPrivKeyId().toString());
    if (kRec != null) {
      jKeyInfo.setText(kRec.plainPublicKey.shortInfo());
      jKeyInfo.setIcon(RecordUtilsGui.getIcon(kRec));
    } else {
      jKeyInfo.setText(com.CH_gui.lang.Lang.rb.getString("label_Key_is_not_available."));
      jKeyInfo.setIcon(Images.get(ImageNums.KEY16));
    }

    if (dataRecord.getDigest() != null) {
      jDigest.setText(dataRecord.getDigest().getHexContent());
    } else {
      jDigest.setText(com.CH_gui.lang.Lang.rb.getString("label_Digest_is_not_available."));
    }

    if (dataRecord.getEncDigest() != null)
      jEncDataDigest.setText(dataRecord.getEncDigest().getHexContent());

    // verification label
    if (dataRecord.isDigestOk() == null || dataRecord.isEncDigestOk() == null) {
      jVerifyOK.setText(com.CH_gui.lang.Lang.rb.getString("label_Insufficient_information_to_determine."));
    } else if (dataRecord.isDigestOk().booleanValue() && dataRecord.isEncDigestOk().booleanValue()) {
      jVerifyOK.setIcon(Images.get(ImageNums.SEAL8_15));
      jVerifyOK.setText(com.CH_gui.lang.Lang.rb.getString("label_Digest_signature_and_message_digests_verified."));
    } else {
      jVerifyOK.setText(com.CH_gui.lang.Lang.rb.getString("label_Signature_and_Digest_DO_NOT_verify."));
    }

    jDataCreated.setText(Misc.getFormattedTimestamp(dataRecord.dateCreated));

    if (dataRecord.isTypeAddress()) {
      jFromLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Creator"));
      jSubjectPlainLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Object_Preview_Plain_Text"));
      jBodyPlainLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Object_Body_Plain_Text"));
    }
    jSubjectPlain.setText(dataRecord.getSubject());
    jBodyPlain.setText(dataRecord.getTextBody());

    boolean includeLabels = false;
    // Create the recipients PANEL composed of Contacts, Users, Folders, unknown text labels.
    boolean includeTO = true;
    boolean includeCC = false;
    boolean includeBCC = false;
    MsgPanelUtils.drawMsgRecipientsPanel(dataRecord, jRecipientsTO, includeLabels, includeTO, includeCC, includeBCC);
    includeTO = false;
    includeCC = true;
    includeBCC = false;
    MsgPanelUtils.drawMsgRecipientsPanel(dataRecord, jRecipientsCC, includeLabels, includeTO, includeCC, includeBCC);
    jGeneralPanel.revalidate();
    jGeneralPanel.doLayout();
    jGeneralPanel.repaint();
  }

  public void closeDialog() {
    if (msgDataListener != null) {
      cache.removeMsgDataRecordListener(msgDataListener);
      msgDataListener = null;
    }
    super.closeDialog();
  }

  private class MsgDataListener implements MsgDataRecordListener {
    public void msgDataRecordUpdated(MsgDataRecordEvent event) {
      // Exec on event thread to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new MsgGUIUpdater(event));
    }
  }

  private class MsgGUIUpdater implements Runnable {
    private MsgDataRecordEvent event;
    public MsgGUIUpdater(MsgDataRecordEvent e) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater(MsgDataRecordEvent e)");
      event = e;
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater.run()");
      if (event.getEventType() == MsgDataRecordEvent.SET) {
        MsgDataRecord[] msgDatas = event.getMsgDataRecords();
        if (msgDatas != null) {
          for (int i=0; i<msgDatas.length; i++) {
            if (msgDatas[i].msgId.equals(msgLink.msgId)) {
              updateData(msgDatas[i]);
              break;
            }
          }
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgPropertiesDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}