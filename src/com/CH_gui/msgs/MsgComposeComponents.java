/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.MsgDataOps;
import com.CH_cl.util.MsgUtils;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.usr.Usr_AltUsrData_Rq;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.JActionCheckBoxMenuItem;
import com.CH_gui.dialog.AccountOptionsDialog;
import com.CH_gui.dialog.UserSelectorDialog;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.Images;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.25 $</b>
*
* @author  Marcin Kurzawa
*/
public class MsgComposeComponents extends Object implements DisposableObj {

  private MsgComposeManagerI composeMngrI;
  private MsgTypeManagerI msgTypeManagerI;
  private UndoManagerI undoMngrI;

  private short objType;
  private boolean isChatComposePanel;
  private boolean isInitialRecipientPresent;

  private boolean isFromLineVisible;
  private boolean isExpiryLineVisible;
  private boolean isQuestionPasswordLinesVisible;
  private boolean isVoicemailLineVisible;

  private JMyListCombo jSendCombo; // in chat mode
  private JLabel jTyping; // typing notification in chat mode
  private JLabel jFromLabel;
  private JComponent jFromComponent;
  private JPanel jFromLine;
  private JMyListCombo jFromCombo; // private combo available only if multiple email addresses are present
  private JButton[] jSelectRecipients = new JButton[3];
  private JTextField[] jRecipientsInput = new JTextField[3];  // text box where to write recipients list
  private JPanel[] jRecipients = new JPanel[3];               // panel displaying all chosen recipients
  private JButton jSelectAttachments;
  private JPanel jAttachments;
  private JTextField jSubject;
  private JPanel jExpiryLine;
  private JLabel jExpiryLabel;
  private JMyCalendarDropdownField jExpiry;
  private JCheckBox jRequestRecipt;
  private JCheckBox jStagedSecureCheck;
  private JPanel jQuestionPasswordLine;
  private JLabel jQuestionLabel;
  private JTextField jQuestionField;
  private JLabel jPasswordLabel;
  private JTextField jPasswordField;
  private JLabel jVoicemailLabel;
  private AudioCapturePanel jVoicemailPanel;
  private JLabel jPriorityLabelStretch;
  private JLabel jPriorityLabel;
  private JMyListCombo jPriority;
  private JCheckBox jCopyToSent;
  private JLabel jCopyToLabel;
  private JButton jRing;
  private JButton jAttach;
  private JLabel jReplyToLabel;
  private JLabel jReplyTo;
//  private JMyLinkLikeLabel jHTML;
  private JButton jHTML;
//  private JMyLinkLikeLabel jShowBcc;
  private JButton jShowBcc;
  private boolean isShownBcc = false;
  private MsgTypeArea msgTypeArea;
  private JButton jSetupSignatures;

  private KeyListener enterKeyListener;
  private MsgTypeListener msgTypeListener;

  private FetchedDataCache cache;

  /** Creates new MsgComposeComponents */
  public MsgComposeComponents(short objType, boolean isChatComposePanel, boolean isInitialRecipientPresent, MsgComposeManagerI composeMngrI, MsgTypeManagerI msgTypeManagerI, UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "MsgComposeComponents()");
    this.objType = objType;
    this.isChatComposePanel = isChatComposePanel;
    this.isInitialRecipientPresent = isInitialRecipientPresent;
    this.composeMngrI = composeMngrI;
    this.msgTypeManagerI = msgTypeManagerI;
    this.undoMngrI = undoMngrI;
    cache = FetchedDataCache.getSingleInstance();
    initComponents();
    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  public void clearMessageArea() {
    msgTypeArea.clearMessageArea();
  }

  public void focusMessageArea() {
    msgTypeArea.focusMessageArea();
  }

  public JPanel getAttachmentsPanel() {
    return jAttachments;
  }

  public AudioCapturePanel getAudioCapturePanel() {
    return jVoicemailPanel;
  }

  public Record getFromAccount() {
    Record fromAccount = jFromCombo != null ? (Record) jFromCombo.getSelected() : null;
    return fromAccount;
  }

  public String[] getContent() {
    if (msgTypeArea.isSubjectGenerated()) {
      return msgTypeArea.getContent();
    } else {
      String[] content = msgTypeArea.getContent();
      content[0] = getMsgSubject();
      return content;
    }
  }

  public Short getContentType() {
    return msgTypeArea.getContentType();
  }

  public short getContentMode() {
    return msgTypeArea.getContentMode();
  }

  public Timestamp getExpiry() {
    Timestamp expiry = null;
    if (jExpiry != null) {
      Date d = jExpiry.getDate();
      expiry = d != null ? new Timestamp(d.getTime()) : null;
    }
    return expiry;
  }

  private String getMsgSubject() {
    String subject = "";
    if (jSubject != null) {
      subject = jSubject.getText().trim();
    }
    return subject;
  }

  protected JTextComponent getMsgTypeArea() {
    return msgTypeArea.getTextComponent();
  }

  public String getQuestion() {
    String question = isStagedSecure() ? jQuestionField.getText().trim() : null;
    return question != null && question.length() > 0 ? question : null;
  }

  public String getPassword() {
    String pass = isStagedSecure() ? MsgUtils.getTrimmedPassword(jPasswordField.getText()) : null;
    return pass != null && pass.length() > 0 ? pass : null;
  }

  public int getPriorityIndex() {
    return jPriority.getSelectedIndex();
  }

  public String getRecipientsInput(int recipientType) {
    return jRecipientsInput[recipientType].getText().trim();
  }

  public JTextField[] getRecipientsInputs() {
    return jRecipientsInput;
  }

  public boolean getRequestRecipt() {
    boolean rr = false;
    if (jRequestRecipt != null) {
      rr = jRequestRecipt.isSelected();
    }
    return rr;
  }

  public JButton[] getSelectRecipientsButtons() {
    return jSelectRecipients;
  }

  public JComponent getSendCombo() {
    return jSendCombo;
  }

  public JLabel getTypingLabel() {
    return jTyping;
  }

  public boolean isAnyContent() {
    return getMsgSubject().length() > 0 || msgTypeArea.isAnyContent();
  }

  public boolean isSufficientContent() {
    boolean sufficient = false;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      sufficient = msgTypeArea.isSufficientContent();
    } else if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
      sufficient = isAnyContent();
    }
    return sufficient;
  }

  public boolean isHTML() {
    return msgTypeArea.isHTML();
  }

  public boolean isRecipientTypeSupported(int recipientType) {
    return jRecipientsInput.length > recipientType && jRecipientsInput[recipientType] != null;
  }

  public boolean isSelectedCopy() {
    return jCopyToSent.isSelected();
  }

  public boolean isStagedSecure() {
    return jStagedSecureCheck != null && jStagedSecureCheck.isSelected();
  }

  public void redrawRecipients(int recipientType, Record[] recipients) {
    // For the purpose of display, convert all user records to recipients;
    Record[] tempRecipients = null;
    if (recipients != null) {
      tempRecipients = new Record[recipients.length];
      for (int i=0; i<tempRecipients.length; i++) {
        tempRecipients[i] = recipients[i];
        if (tempRecipients[i] instanceof UserRecord) {
          Record converted = CacheUsrUtils.convertUserIdToFamiliarUser(((UserRecord)tempRecipients[i]).userId, true, false);
          tempRecipients[i] = converted != null ? converted : tempRecipients[i];
        }
      }
    }
    jRecipients[recipientType].removeAll();
    jRecipientsInput[recipientType].setText("");
    if (tempRecipients != null && tempRecipients.length > 0) {
      if (tempRecipients.length < 10) {
        jRecipients[recipientType].setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        jRecipients[recipientType].setPreferredSize(null);
        MsgPanelUtils.drawRecordFlowPanel(tempRecipients, jRecipients[recipientType]);
      } else {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        jRecipients[recipientType].setLayout(new BorderLayout());
        jRecipients[recipientType].add(scrollPane, BorderLayout.CENTER);
        jRecipients[recipientType].setPreferredSize(new Dimension(60, 60));
        jRecipients[recipientType].doLayout();
        MsgPanelUtils.drawRecordFlowPanel(tempRecipients, panel);
        jRecipients[recipientType].revalidate();
      }
    } else if (tempRecipients == null || tempRecipients.length == 0) {
      jRecipients[recipientType].setLayout(new BorderLayout(0, 0));
      jRecipients[recipientType].add(jRecipientsInput[recipientType], BorderLayout.CENTER);
    }
  }


  public void setEnabled(boolean enabled) {
    if (jFromCombo != null)
      jFromCombo.setEnabled(enabled);
    for (int i=0; i<jSelectRecipients.length; i++) {
      if (jSelectRecipients[i] != null)
        jSelectRecipients[i].setEnabled(enabled);
    }
    for (int i=0; i<jRecipientsInput.length; i++) {
      if (jRecipientsInput[i] != null)
        jRecipientsInput[i].setEnabled(enabled);
    }
    if (jPriority != null)
      jPriority.setEnabled(enabled);
    if (jSelectAttachments != null)
      jSelectAttachments.setEnabled(enabled);
    if (jCopyToSent != null)
      jCopyToSent.setEnabled(enabled);
    if (jSubject != null)
      jSubject.setEnabled(enabled);
    if (jExpiry != null)
      jExpiry.setEnabled(enabled);
    if (jRequestRecipt != null)
      jRequestRecipt.setEnabled(enabled);
    if (jStagedSecureCheck != null)
      jStagedSecureCheck.setEnabled(enabled);
    if (msgTypeArea != null) {
      msgTypeArea.setEnabled(enabled);
      msgTypeArea.setEditable(enabled);
    }
  }

  public void setEnabledSend(boolean enabled) {
    if (jSendCombo != null) {
      jSendCombo.setEnabledLabel(enabled);
    }
  }

  public void setMsgContent(String text, boolean isHtml) {
    msgTypeArea.setContentText(text, isHtml, false, true);
  }

  public void setQuestion(String question) {
    if (jQuestionField != null)
      jQuestionField.setText(question);
    else
      throw new IllegalStateException("Current mode does not support staged secure question setting.");
  }

  public void setPassword(String pass) {
    if (jPasswordField != null)
      jPasswordField.setText(pass);
    else
      throw new IllegalStateException("Current mode does not support staged secure password setting.");
  }

  public void setSelectedCopy(boolean select) {
    jCopyToSent.setSelected(select);
  }

  public void setSelectedPriorityIndex(int index) {
    jPriority.setSelectedIndex(index);
  }

  public void setStagedSecure(boolean isStagedSecure) {
    if (jStagedSecureCheck != null)
      jStagedSecureCheck.setSelected(isStagedSecure);
    else
      throw new IllegalStateException("Current mode does not support staged secure setting.");
  }

  public void setSubject(String subject) {
    if (jSubject != null)
      jSubject.setText(subject);
  }

  /** Show or hide advanced headers */
  public void setVisibleAllHeaders(boolean visible) {
    if (msgTypeArea.isSubjectGenerated()) { // xxx this should be more meaningful and correct
      //setVisibleRecipientHeaders(new int[] { MsgComposePanel.CC, MsgComposePanel.BCC }, visible);
      setVisibleRecipientHeaders(new int[] { MsgComposePanel.CC }, visible);
    } else {
      //setVisibleRecipientHeaders(new int[] { MsgComposePanel.BCC }, visible);
    }
    pressedShowBCC(visible);
    if (jCopyToSent != null) {
      jCopyToSent.setVisible(visible);
      jCopyToLabel.setVisible(visible);
    }
    if (jExpiry != null) {
      isExpiryLineVisible = visible;
      jExpiryLabel.setVisible(isExpiryLineVisible);
      jExpiryLine.setVisible(isExpiryLineVisible);
    }
    if (jStagedSecureCheck != null) {
      isQuestionPasswordLinesVisible = jStagedSecureCheck.isSelected();
      jQuestionLabel.setVisible(isQuestionPasswordLinesVisible);
      jQuestionPasswordLine.setVisible(isQuestionPasswordLinesVisible);
    }
    if (jVoicemailPanel != null) {
      isVoicemailLineVisible = visible;
      jVoicemailLabel.setVisible(isVoicemailLineVisible);
      jVoicemailPanel.setVisible(isVoicemailLineVisible);
    }
    if (msgTypeArea.isSubjectGenerated() && jFromLabel != null) {
      isFromLineVisible = visible;
      jFromLabel.setVisible(isFromLineVisible);
      jFromLine.setVisible(isFromLineVisible);
    }
  }
  private void setVisibleRecipientHeaders(int[] recipientTypes, boolean visible) {
    for (int i=0; i<recipientTypes.length; i++) {
      int type = recipientTypes[i];
      if (jSelectRecipients.length > type && jSelectRecipients[type] != null)
        jSelectRecipients[type].setVisible(visible);
      if (jRecipientsInput.length > type && jRecipientsInput[type] != null)
        jRecipientsInput[type].setVisible(visible);
      if (jRecipients.length > type && jRecipients[type] != null)
        jRecipients[type].setVisible(visible);
    }
  }

  private void pressedShowBCC() {
    pressedShowBCC(!isShownBcc);
  }
  private void pressedShowBCC(boolean toShow) {
    if (toShow) {
      jShowBcc.setText("Hide BCC");
      setVisibleRecipientHeaders(new int[] { MsgComposePanel.BCC }, true);
      isShownBcc = true;
    } else {
      jShowBcc.setText("Show BCC");
      setVisibleRecipientHeaders(new int[] { MsgComposePanel.BCC }, false);
      isShownBcc = false;
    }
  }

  public void setVisibleAttachments(boolean visible) {
    jSelectAttachments.setVisible(visible);
    jAttachments.setVisible(visible);
  }

  public void setVisiblePriority(boolean visible) {
    jPriorityLabelStretch.setVisible(!visible);
    jPriorityLabel.setVisible(visible);
    jPriority.setVisible(visible);
  }

  public void toggleVisibilityOfRecordingPanel() {
    boolean setVisible = !jVoicemailLabel.isVisible();
    jVoicemailLabel.setVisible(setVisible);
    jVoicemailPanel.setVisible(setVisible);
  }

  private void initComponents() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "initComponents()");
    String htmlPropertyPostfix = objType == MsgDataRecord.OBJ_TYPE_ADDR ? "_addr" : (isChatComposePanel ? "_chat" : "_mail");
    boolean defaultHTML = true;
    boolean suppressSpellChecker = false;
    msgTypeArea = new MsgTypeArea(htmlPropertyPostfix, objType, defaultHTML, undoMngrI, suppressSpellChecker, isChatComposePanel);
    jHTML = msgTypeArea.getHTMLSwitchButton();
//    jShowBcc = new JMyLinkLikeLabel("Show BCC", MsgPreviewPanel.LINK_RELATIVE_FONT_SIZE);
    jShowBcc = new JMyButtonNoFocus("Show BCC");
    jShowBcc.setBorder((new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2))));
    isShownBcc = false;
    jShowBcc.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        e.consume();
        pressedShowBCC();
      }
    });

    if (!isChatComposePanel) {
      jFromLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_From"));
      jFromLine = new JPanel();
      jFromLine.setLayout(new GridBagLayout());
      jSubject = new JUndoableTextField(undoMngrI);
      jExpiryLine = new JPanel();
      jExpiryLine.setLayout(new GridBagLayout());
      jExpiryLabel = new JMyLabel("Expiration:");
      jExpiry = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1, true, null,
          new String[] { "Never", "Tomorrow", "One Week", "Two Weeks", "One Month", "Custom..." },
          new int[][] { { 0, -1 },
                        { Calendar.DAY_OF_MONTH, 1 },
                        { Calendar.WEEK_OF_YEAR, 1 },
                        { Calendar.WEEK_OF_YEAR, 2 },
                        { Calendar.MONTH, 1 },
                        { 0, -2 } }, true);
      jRequestRecipt = new JMyCheckBox("Request Recipt");
      jRequestRecipt.setVisible(false); // for now this is NOT ACTIVE FEATURE
      jStagedSecureCheck = new JMyCheckBox("Encrypt with Question and Answer");
      jStagedSecureCheck.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          boolean selected = jStagedSecureCheck.isSelected();
          jQuestionLabel.setVisible(selected);
          jQuestionPasswordLine.setVisible(selected);
//          jQuestionLabel.setVisible(selected);
//          jQuestionField.setVisible(selected);
//          jPasswordLabel.setVisible(selected);
//          jPasswordField.setVisible(selected);
          jQuestionField.setEnabled(selected);
          jPasswordField.setEnabled(selected);
        }
      });
      jQuestionPasswordLine = new JPanel();
      jQuestionPasswordLine.setLayout(new GridBagLayout());
      jQuestionLabel = new JMyLabel("Question:");
      jQuestionField = new JMyTextField(20);
      jQuestionField.setEnabled(false);
      jPasswordLabel = new JMyLabel("Answer:");
      jPasswordField = new JMyTextField(10);
      jPasswordField.setEnabled(false);

      jVoicemailLabel = new JMyLabel("Voicemail:");
      jVoicemailPanel = new AudioCapturePanel(composeMngrI);

      // do for : TO, CC, BCC
      for (int recipientType=MsgComposePanel.TO; recipientType<MsgComposePanel.RECIPIENT_TYPES.length; recipientType++) {
        String label = recipientType==MsgComposePanel.TO ? "button_To" : ( recipientType==MsgComposePanel.CC ? "button_Cc" : "button_Bcc");
        jSelectRecipients[recipientType] = new JMyButtonNoFocus(com.CH_cl.lang.Lang.rb.getString(label));
        jSelectRecipients[recipientType].setAlignmentX(JButton.LEFT_ALIGNMENT);
        jSelectRecipients[recipientType].setBorder((new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2))));
        jSelectRecipients[recipientType].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final int recType = recipientType;
        jSelectRecipients[recipientType].addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
            composeMngrI.selectRecipientsPressed(recType);
            if (trace != null) trace.exit(getClass());
          }
        });
        jRecipientsInput[recipientType] = new JUndoableTextField(undoMngrI);
        ObjectsProviderUpdaterI userSearchProvider = new ObjectsProviderUpdaterI() {
          private ListUpdatableI updatable;
          public Object[] provide(Object args) {
            Window w = SwingUtilities.windowForComponent(jRecipientsInput[recType]);
            if (w instanceof Dialog || w instanceof Frame) {
              UserSelectorDialog d = null;
              String searchString = (String) args;
              String selectButtonText = com.CH_cl.lang.Lang.rb.getString("button_Select");
              if (w instanceof Dialog)
                d = new UserSelectorDialog((Dialog) w, selectButtonText, searchString);
              else
                d = new UserSelectorDialog((Frame) w, selectButtonText, searchString);
              d.registerForUpdates(updatable);
            }
            return null;
          }
          public Object[] provide(Object args, ListUpdatableI updatable) {
            registerForUpdates(updatable);
            return provide(args);
          }
          public void registerForUpdates(ListUpdatableI updatable) {
            this.updatable = updatable;
          }
          public void disposeObj() {
            updatable = null;
          }
          public String toString() {
            return "<html><i>Search for people outside of your Contact List</i></html>";
          }
        };
        jRecipientsInput[recipientType].addKeyListener(new TypeAheadPopupList(new RecipientListProvider(true, false, false, false, true), userSearchProvider, false));
        jRecipients[recipientType] = new JPanel();
        jRecipients[recipientType].setLayout(new BorderLayout(0, 0));
        jRecipients[recipientType].setBorder(new EmptyBorder(0,0,0,0));
        // initially add an input field to the recipients list
        jRecipients[recipientType].add(jRecipientsInput[recipientType], BorderLayout.CENTER);
      }

      // setup Reply To label
      jReplyToLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_In_reply_to"));
      jReplyToLabel.setVisible(false);
      jReplyTo = new JMyLabel();
      jReplyTo.setVisible(false);

      // setup FROM field
      UserRecord myUserRec = cache.getUserRecord();
      EmailRecord[] emlRecs = null;
      if (myUserRec != null)
        emlRecs = cache.getEmailRecords(myUserRec.userId);
      if (emlRecs != null && emlRecs.length > 1)
        Arrays.sort(emlRecs, new ListComparator());
      if (isChatComposePanel || emlRecs == null || emlRecs.length <= 1) {
        JLabel jFromStaticLabel = null;
        if (myUserRec != null)
          jFromStaticLabel = new JMyLabel(myUserRec.shortInfo(), RecordUtilsGui.getIcon(myUserRec), JLabel.LEFT);
        else
          jFromStaticLabel = new JMyLabel("Not logged In!");
        jFromComponent = jFromStaticLabel;
      } else {
        Object[] objs = new Object[1 + emlRecs.length];
        objs = ArrayUtils.concatinate(new Object[] { myUserRec }, emlRecs);
        jFromCombo = new JMyListCombo(0, objs);
        jFromComponent = jFromCombo;
        jFromCombo.setObjectsProvider(new ObjectsProviderI() {
          public Object[] provide(Object args) {
            UserRecord uRec = cache.getUserRecord();
            EmailRecord[] emlRecs = cache.getEmailRecords(uRec.userId);
            if (emlRecs != null && emlRecs.length > 1)
              Arrays.sort(emlRecs, new ListComparator());
            return ArrayUtils.concatinate(new Object[] { uRec }, emlRecs);
          }
        });
      }

      jSetupSignatures = new JMyButtonNoFocus("Setup Email Signatures...");
      jSetupSignatures.setVisible(false);
      jSetupSignatures.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          Window w = SwingUtilities.windowForComponent(jSetupSignatures);
          if (w instanceof Frame) {
            AccountOptionsDialog optionsDialog = new AccountOptionsDialog((Frame) w, cache.getUserRecord());
            optionsDialog.navigateToSignaturesTab();
          }
        }
      });

    } else {

      ObjectsProviderI sendObjectsProvider = new ObjectsProviderI() {
        public Object[] provide(Object args) {
          Icon icon = (Icon) composeMngrI.getActions()[MsgComposePanel.SEND_ACTION].getValue(Actions.MENU_ICON);
          String text = (String) composeMngrI.getActions()[MsgComposePanel.SEND_ACTION].getValue(Actions.NAME);
          String tip = "<html>" + composeMngrI.getActions()[MsgComposePanel.SEND_ACTION].getValue(Actions.TOOL_TIP) + "<br>" + com.CH_cl.lang.Lang.rb.getString("actionTip_Send_mail_button_tip");
          JLabel sendLabel = new JMyLabel(text, icon, JLabel.LEFT);
          sendLabel.setToolTipText(tip);

          final JActionCheckBoxMenuItem settingsItem = new JActionCheckBoxMenuItem();
          settingsItem.setText("Use ENTER key to send messages.");
          settingsItem.setToolTipText("Changes user setting.");
          final UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
          boolean selected = uRec != null && (uRec.flags.longValue() & UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES) != 0;
          settingsItem.setSelected(selected);
          ActionListener settingsListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
              request.userRecord = (UserRecord) uRec.clone();
              request.userRecord.flags = new Long(Misc.setBit(settingsItem.isSelected(), request.userRecord.flags, UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES));
              MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
            }
          };

          final JActionCheckBoxMenuItem copyItem = new JActionCheckBoxMenuItem();
          String sentFolderName = getSentFolderName();
          copyItem.setText("Copy To " + sentFolderName);
          copyItem.setToolTipText("Save a copy of composed messages in the " + sentFolderName + " folder.");
          copyItem.setSelected(jCopyToSent != null && jCopyToSent.isSelected());

          ActionListener copyListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              if (jCopyToSent != null)
                jCopyToSent.setSelected(copyItem.isSelected());
            }
          };

          Object[][] objects = new Object[][] { { settingsItem, sendLabel, settingsListener }, { copyItem, sendLabel, copyListener }};
          return objects;
        }
      };

      ActionListener sendActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          pressedSendButton(event);
        }
      };

      jSendCombo = new JMyListCombo(0, sendObjectsProvider, sendActionListener);
      jTyping = new JMyLabel("typing...");
      jTyping.setBorder(new EmptyBorder(2,2,2,2));
      jTyping.setIcon(Images.get(ImageNums.PENCIL16));
      jTyping.setVisible(false);
      jTyping.setOpaque(true);
      jTyping.setBackground(Color.white);
      jTyping.setFocusable(false);

      jRing = new JMyButtonNoFocus(Images.get(ImageNums.EDITOR_BELL));
      jRing.setToolTipText(com.CH_cl.lang.Lang.rb.getString("actionTip_Ring_the_bell"));
      //jRing.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      jRing.setBorder(new EmptyBorder(2, 2, 2, 2));
      jRing.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          composeMngrI.ringPressed();
        }
      });
    }

    jSelectAttachments = new JMyButtonNoFocus(com.CH_cl.lang.Lang.rb.getString("button_Attach"), Images.get(ImageNums.ATTACH_SMALL));
    jSelectAttachments.setAlignmentX(JButton.LEFT_ALIGNMENT);
    jSelectAttachments.setBorder(new EtchedBorder());
    jSelectAttachments.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jSelectAttachments.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        composeMngrI.selectAttachmentsPressed();
      }
    });


    if (!msgTypeArea.isAttachmentPanelEmbeded()) {
      jAttachments = new JPanel();
      jAttachments.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      jAttachments.setBorder(new EmptyBorder(0,0,0,0));

      jAttach = new JMyButtonNoFocus(Images.get(ImageNums.EDITOR_ATTACH));
      jAttach.setToolTipText(com.CH_cl.lang.Lang.rb.getString("actionTip_Attachments"));
      //jAttach.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      jAttach.setBorder(new EmptyBorder(2, 2, 2, 2));
      jAttach.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          composeMngrI.selectAttachmentsPressed();
        }
      });
    } else {
      jAttachments = msgTypeArea.getAttachmentsPanel();
    }

    jPriorityLabelStretch = new JLabel();
    jPriorityLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Priority"));
    jPriority = new JMyListCombo(0, new JLabel[] {
      new JMyLabel("FYI", Images.get(ImageNums.PRIORITY_LOW16), JLabel.LEFT),
      new JMyLabel("Normal", Images.get(ImageNums.TRANSPARENT16), JLabel.LEFT),
      new JMyLabel("High", Images.get(ImageNums.PRIORITY_HIGH16), JLabel.LEFT)
    });
    jPriority.setSelectedIndex(MsgComposePanel.PRIORITY_INDEX_NORMAL);
    jPriority.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        composeMngrI.priorityPressed();
      }
    });

    // "Copy to Sent"
    Icon sentFolderIcon = getSentFolderIcon();
    String sentShareName = getSentFolderName();
    jCopyToSent = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Copy_To"), false);

    jCopyToLabel = new JMyLabel(sentShareName, sentFolderIcon, JLabel.LEFT);
    jCopyToLabel.setBackground(jCopyToSent.getBackground());
    jCopyToLabel.setForeground(jCopyToSent.getForeground());
    jCopyToLabel.setFont(jCopyToSent.getFont());

    if (!isChatComposePanel) {
      // Initialize the state of showing headers
      setVisibleAllHeaders(((Boolean) composeMngrI.getActions()[MsgComposePanel.SHOW_ALL_HEADERS].getValue(Actions.STATE_CHECK)).booleanValue());
    } else {
      setVisibleAllHeaders(false);
    }

    // resize the send button to hight of priority combo button
    if (jSendCombo != null)
      jSendCombo.setPreferredSize(new Dimension(140, jPriority.getPreferredSize().height));

    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  private Icon getSentFolderIcon() {
    UserRecord myUser = cache.getUserRecord();
    FolderRecord sentFolder = null;
    if (myUser != null) {
      sentFolder = cache.getFolderRecord(cache.getUserRecord().sentFolderId);
    }
    Icon sentFolderIcon = sentFolder != null ? Images.get(sentFolder.getIcon(false, myUser, false)) : Images.get(ImageNums.FLD_MAIL_SENT16);
    return sentFolderIcon;
  }

  private String getSentFolderName() {
    UserRecord myUser = cache.getUserRecord();
    FolderRecord sentFolder = null;
    FolderShareRecord sentShare = null;
    if (myUser != null) {
      sentFolder = cache.getFolderRecord(cache.getUserRecord().sentFolderId);
    }
    if (sentFolder != null)
      sentShare = cache.getFolderShareRecordMy(sentFolder.folderId, false);
    String sentShareName = sentShare != null ? sentShare.getFolderName() : com.CH_cl.lang.Lang.rb.getString("folder_Sent");
    return sentShareName;
  }

  /**
  * Set message content with the signature is available.
  */
  public void setSignatureIfRequired(boolean isReplyOrForwardMsgType) {
    if (!isChatComposePanel && objType == MsgDataRecord.OBJ_TYPE_MSG) {
      UserSettingsRecord userSettingsRecord = cache.getMyUserSettingsRecord();
      if (userSettingsRecord != null) {
        String sig = userSettingsRecord.getDefaultSig();
        jSetupSignatures.setVisible(sig == null || sig.trim().length() == 0);
        if ((!isReplyOrForwardMsgType && Boolean.TRUE.equals(userSettingsRecord.sigAddToNew)) ||
            (isReplyOrForwardMsgType && Boolean.TRUE.equals(userSettingsRecord.sigAddToReFwd))
            ) {
          String[] sigText = CacheMsgUtils.getSigText(userSettingsRecord);
          if (sigText[1].trim().length() > 0) {
            msgTypeArea.setContentText(sigText[1], sigText[0].equals("text/html"), false, true);
          }
        }
      }
    }
  }

  public void setFocusToBody() {
    if (msgTypeArea != null) {
      JTextComponent textComp = msgTypeArea.getTextComponent();
      textComp.requestFocusInWindow();
      textComp.addHierarchyListener(new InitialFocusRequestor());
    }
  }

  public void setFocusToRecipient() {
    if (jRecipientsInput[MsgComposePanel.TO] != null) {
      jRecipientsInput[MsgComposePanel.TO].requestFocusInWindow();
      jRecipientsInput[MsgComposePanel.TO].addHierarchyListener(new InitialFocusRequestor());
    }
  }

  public void setFocusToSubject() {
    if (jSubject != null) {
      jSubject.requestFocusInWindow();
      jSubject.addHierarchyListener(new InitialFocusRequestor());
    }
  }


  public void initMainPanel(JPanel panel) {
    panel.setLayout(new GridBagLayout());
    if (isChatComposePanel) {
      initMainPanelForChat(panel);
    } else {
      initMainPanelForMail(panel);
    }

    enterKeyListener = new EnterKeyListener();
    msgTypeListener = new MsgTypeListener(msgTypeManagerI, isChatComposePanel);
    msgTypeArea.setEnterKeyListener(enterKeyListener);
    msgTypeArea.setDocumentListener(msgTypeListener);
    if (jSubject != null) {
      jSubject.getDocument().addDocumentListener(msgTypeListener);
    }
    if (jRecipientsInput != null) {
      for (int i=0; i<jRecipientsInput.length; i++) {
        if (jRecipientsInput[i] != null)
          jRecipientsInput[i].getDocument().addDocumentListener(msgTypeListener);
      }
    }
  }

  private void initMainPanelForMail(JPanel panel) {
    int posY = 0;

    panel.add(jReplyToLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 5), 0, 0));
    panel.add(jReplyTo, new GridBagConstraints(1, posY, 1, 1, 10, 0, // for super long reply subjects, keep the layout aligned to west
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;

    isFromLineVisible = true;

    panel.add(jFromLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 5), 0, 0));

    panel.add(jFromLine, new GridBagConstraints(1, posY, 6, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));


    jFromLine.add(jFromComponent, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 2, 5), 0, 0));

    // here posY is 0
    jFromLine.add(jPriorityLabelStretch, new GridBagConstraints(2, posY, 1, 1, 10, 0,
        GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 3), 0, 0));
    jFromLine.add(jPriorityLabel, new GridBagConstraints(3, posY, 1, 1, 10, 0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 3), 0, 0));
    jFromLine.add(jPriority, new GridBagConstraints(4, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(5, 3, 2, 15), 0, 0));

    // "Copy to Sent"
    jFromLine.add(jCopyToSent, new GridBagConstraints(5, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 15, 2, 0), 0, 0));
    jFromLine.add(jCopyToLabel, new GridBagConstraints(6, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 0, 2, 5), 0, 0));
    posY ++;

    for (int i=0; i<jSelectRecipients.length; i++) {
      panel.add(jSelectRecipients[i], new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 5, 0));
      boolean addShowBccLink = i == 0 && !msgTypeArea.isSubjectGenerated();
      int gridWidth = 6;
      if (addShowBccLink)
        gridWidth = 5;
      panel.add(jRecipients[i], new GridBagConstraints(1, posY, gridWidth, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(2, 5, 2, 5), 0, 0));
      if (addShowBccLink)
        panel.add(jShowBcc, new GridBagConstraints(6, posY, 1, 1, 0, 0,
              GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 0, 2, 5), 0, 0));
      posY ++;
    }

    // subject label, space and button
    if (!msgTypeArea.isSubjectGenerated()) {
      panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Subject")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));
      panel.add(jSubject, new GridBagConstraints(1, posY, 5, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
      //add(jAttach, new GridBagConstraints(4, posY, 1, 1, 0, 0,
            //GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 2, 0, 2), 0, 0));
      panel.add(jHTML, new GridBagConstraints(6, posY, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 0, 2, 5), 0, 0));
      posY ++;
    }

    isExpiryLineVisible = true;
    panel.add(jExpiryLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));

    panel.add(jExpiryLine, new GridBagConstraints(1, posY, 6, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    jExpiryLine.add(jExpiry, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));

    jExpiryLine.add(jRequestRecipt, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 15, 2, 5), 0, 0));

    jExpiryLine.add(jStagedSecureCheck, new GridBagConstraints(2, posY, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 15, 2, 5), 0, 0));

    jExpiryLine.add(new JLabel(), new GridBagConstraints(4, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    isQuestionPasswordLinesVisible = true;
    panel.add(jQuestionLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));
    panel.add(jQuestionPasswordLine, new GridBagConstraints(1, posY, 6, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    jQuestionPasswordLine.add(jQuestionField, new GridBagConstraints(0, posY, 1, 1, 20, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 15), 0, 0));
    jQuestionPasswordLine.add(jPasswordLabel, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));
    jQuestionPasswordLine.add(jPasswordField, new GridBagConstraints(2, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    jQuestionPasswordLine.add(new JLabel(), new GridBagConstraints(3, posY, 1, 1, 5, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    panel.add(jVoicemailLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));
    panel.add(jVoicemailPanel, new GridBagConstraints(1, posY, 6, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 5), 0, 0));
    posY ++;

    // space for attachments panel;
    if (!msgTypeArea.isAttachmentPanelEmbeded()) {
      panel.add(jSelectAttachments, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 5, 0));
      panel.add(jAttachments, new GridBagConstraints(1, posY, 6, 1, 100, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(2, 5, 2, 5), 0, 0));
      posY ++;
    }

    panel.add(msgTypeArea, new GridBagConstraints(0, posY, 7, 1, 10, 10,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(3, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jSetupSignatures, new GridBagConstraints(0, posY, 7, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 5, 5), 0, 0));

  }


  private void initMainPanelForChat(JPanel panel) {
    jPriority.setSelectedIndex(MsgComposePanel.PRIORITY_INDEX_NORMAL);
    composeMngrI.priorityPressed();

    int posY = 3;
    // most important components go first to be first in focus order
    panel.add(msgTypeArea, new GridBagConstraints(0, posY, 9, 1, 50, 50,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;
    // this row is for notifications
    panel.add(jTyping, new GridBagConstraints(0, posY, 9, 1, 10, 0,
          GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    // put other components next in the focus order
    posY = 0;
    panel.add(jPriorityLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));
    panel.add(jPriority, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

//    // attach
//    panel.add(jAttach, new GridBagConstraints(2, posY, 1, 1, 0, 0,
//          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 20, 0, 0), 0, 0));

    JComponent msgTypeToolBar = msgTypeArea.getToolBar();
    msgTypeToolBar.add(jAttach);
    // Separator before Ring
    JPanel sep = new JPanel();
    sep.setLayout(null);
    sep.setBackground(sep.getBackground().darker());
    sep.setMaximumSize(new Dimension(1, 50));
    msgTypeToolBar.add(sep);
    msgTypeToolBar.add(jRing);
    if (msgTypeToolBar != null) {
      panel.add(msgTypeToolBar, new GridBagConstraints(4, posY, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
    }

//    panel.add(jTyping, new GridBagConstraints(5, posY, 1, 1, 10, 0,
//          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 2), 0, 0));

    // stretcher
    panel.add(new JLabel(), new GridBagConstraints(6, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    panel.add(jSendCombo, new GridBagConstraints(8, posY, 1, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new MyInsets(2, 1, 2, 1), 0, 0));

    posY ++;

    // this row was for TO: in mail mode
    posY ++;

//    panel.add(jVoicemailLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
//          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 5), 0, 0));
//    panel.add(jVoicemailPanel, new GridBagConstraints(1, posY, 6, 1, 10, 0,
//          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 5), 0, 0));
//    posY ++;

    if (!msgTypeArea.isAttachmentPanelEmbeded()) {
      // this row was for Attachments in mail mode
      panel.add(jSelectAttachments, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 5, 0));
      panel.add(jAttachments, new GridBagConstraints(1, posY, 8, 1, 100, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(2, 2, 2, 2), 0, 0));
    }
  }


  public void setFromDraft(final MsgLinkRecord draftMsgLink, final MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "setFromDraft(MsgLinkRecord draftMsgLink, final MsgDataRecord)");
    if (trace != null) trace.args(draftMsgLink, dataRecord);

    // set priority
    short importance = dataRecord.importance.shortValue();
    int priorityIndex = MsgComposePanel.PRIORITY_INDEX_NORMAL; // default for regular email and normal priority cryptograms
    if (MsgDataRecord.isImpFYI(importance))
      priorityIndex = MsgComposePanel.PRIORITY_INDEX_FYI;
    else if (MsgDataRecord.isImpHigh(importance))
      priorityIndex = MsgComposePanel.PRIORITY_INDEX_HIGH;
    setSelectedPriorityIndex(priorityIndex);

    // set expiry
    jExpiry.setDate(dataRecord.dateExpired);

    // set From
    if (jFromCombo != null) {
      String fromEmailAddress = dataRecord.getFromEmailAddress();
      if (fromEmailAddress != null) {
        EmailRecord[] emlRecs = cache.getEmailRecords(cache.getMyUserId());
        for (int i=0; i<emlRecs.length; i++) {
          if (EmailRecord.isAddressEqual(emlRecs[i].emailAddr, fromEmailAddress)) {
            jFromCombo.setSelectedObject(emlRecs[i], emlRecs);
            break;
          }
        }
      }
    }

    // set subject
    setSubject(dataRecord.getSubject());

    // set content
    msgTypeArea.setContent(dataRecord);

    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  public void setFromDraft(XMLElement draftData) {
    msgTypeArea.setContent(draftData);
  }

  private void setReplyLabel(MsgDataRecord dataRecord) {
    if (dataRecord != null) {
      jReplyTo.setIcon(ListRenderer.getRenderedIcon(dataRecord, true));
      jReplyTo.setText(ListRenderer.getRenderedText(dataRecord));
      jReplyToLabel.setVisible(true);
      jReplyTo.setVisible(true);
    } else {
      jReplyToLabel.setVisible(false);
      jReplyTo.setVisible(false);
    }
  }

  public void setPostReply(MsgLinkRecord replyToMsg, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "setReplyTo(MsgLinkRecord replyToMsg, MsgDataRecord dataRecord)");
    setReplyLabel(dataRecord);
    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  /**
  * Sets the From field by searching the recipients lists for a potential match to one of users own accounts...
  * @param pickFromRecipients
  */
  public void setFromAccounts(MsgDataRecord originalMsg) {
    if (jFromCombo != null && originalMsg != null) {
      EmailRecord emlRecFound = MsgPanelUtils.getOurMatchingFromEmlRec(originalMsg);
      if (emlRecFound != null) {
        jFromCombo.setSelectedObject(emlRecFound);
      }
    }
  }
  public void setFromAccount(EmailRecord sendFromEmailAccount) {
    if (jFromCombo != null && sendFromEmailAccount != null) {
      jFromCombo.setSelectedObject(sendFromEmailAccount);
    }
  }

  public void setQuotedContent(MsgLinkRecord quotedMsg, MsgDataRecord dataRecord, boolean isReply, boolean isForward) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "setQuotedContent(MsgLinkRecord quotedMsg, MsgDataRecord dataRecord, boolean isReply, boolean isForward)");

    if (isReply)
      setReplyLabel(dataRecord);

    // get signature settings
    UserSettingsRecord userSettingsRecord = cache.getMyUserSettingsRecord();
    boolean addSignature = false;
    if (userSettingsRecord != null && Boolean.TRUE.equals(userSettingsRecord.sigAddToReFwd)) {
      addSignature = true;
    }

    // set From -- try sending from the same account which received the message
    setFromAccounts(dataRecord);

    if (isReply)
      setSubject(MsgDataOps.getSubjectReply(dataRecord, 250));
    else if (isForward)
      setSubject(MsgDataOps.getSubjectForward(new Object[] { dataRecord }, 250));

    String[] content = CacheMsgUtils.makeReplyToContent(quotedMsg, dataRecord, false, false, false);

    String text = null;
    String[] sigText = addSignature ? CacheMsgUtils.getSigText(userSettingsRecord) : null;
    String signatureText = sigText != null ? sigText[1] : "";
    if (content[0].equalsIgnoreCase("text/html")) {
      if (sigText != null && !sigText[0].equals("text/html")) {
        signatureText = Misc.encodePlainIntoHtml(signatureText);
      }
      if (signatureText.trim().length() > 0) {
        signatureText = ArrayUtils.replaceKeyWords(new StringBuffer(signatureText),
            new String[][] {
              {"<BODY>", " "},
              {"<body>", " "},
              {"</BODY>", " "},
              {"</body>", " "},
              {"<HTML>", " "},
              {"<html>", " "},
              {"</HTML>", " "},
              {"</html>", " "},
          }).toString();
      }
      text = "<html><body> " +
              (addSignature ? signatureText : "") +
              "<br>" +
              "<div style=\"border:1px solid gray;padding: 10px\">" +
                com.CH_cl.lang.Lang.rb.getString("-----_Original_Message_-----") +
                "<br>" +
                content[1] + content[3] +
              "</div>" +
              "<br>" +
              "</body></html>";
      msgTypeArea.setHTML(true);
      msgTypeArea.setContentText(text, true, false, true);
    } else {
      if (sigText != null && !sigText[0].equals("text/plain")) {
        signatureText = MsgPanelUtils.extractPlainFromHtml(signatureText);
      }
      if (signatureText.trim().length() > 0) {
        signatureText += "\n\n";
      }
      text =
          "\n\n" +
          (addSignature ? signatureText : "") +
          com.CH_cl.lang.Lang.rb.getString("-----_Original_Message_-----") +
          " \n" +
          content[1] + content[3] +
          "\n\n";
      msgTypeArea.setHTML(false);
      msgTypeArea.setContentText(text, false, false, true);
    }

    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  public void setForward(Object[] selectedAttachments) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposeComponents.class, "setForward(Record[] selectedAttachments)");
    if (trace != null) trace.args(selectedAttachments);
    setSubject(MsgDataOps.getSubjectForward(selectedAttachments, 250));
    if (trace != null) trace.exit(MsgComposeComponents.class);
  }

  private void pressedSendButton(ActionEvent event) {
    composeMngrI.getActions()[MsgComposePanel.SEND_ACTION].actionPerformed(event);
  }

  private class EnterKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent event) {
      if (isChatComposePanel && jSendCombo != null) {
        if ((FetchedDataCache.getSingleInstance().getUserRecord().flags.longValue() & UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES) != 0) {
          event.consume();
          // only react to ENTER if there is any content of message
          if (jSendCombo.isEnabledLabel())
            pressedSendButton(new ActionEvent(event.getSource(), event.getID(), "ENTER"));
        }
      }
    }
  }


  /*************************************************************************
  * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
  * Dispose the object and release resources to help in garbage collection.
  ************************************************************************/
  public void disposeObj() {
    // remove DocumentChangeListener
    if (msgTypeListener != null) {
      msgTypeArea.setDocumentListener(null);
      if (jSubject != null) {
        jSubject.getDocument().removeDocumentListener(msgTypeListener);
      }
      if (jRecipientsInput != null) {
        for (int i=0; i<jRecipientsInput.length; i++) {
          if (jRecipientsInput[i] != null) {
            jRecipientsInput[i].getDocument().removeDocumentListener(msgTypeListener);
            KeyListener[] keyListeners = jRecipientsInput[i].getKeyListeners();
            for (int k=0; keyListeners!=null && k<keyListeners.length; k++) {
              jRecipientsInput[i].removeKeyListener(keyListeners[k]);
              if (keyListeners[k] instanceof TypeAheadPopupList)
                ((TypeAheadPopupList) keyListeners[k]).disposeObj();
            }
          }
        }
      }
      msgTypeListener = null;
    }
    // remove enter key listener
    if (enterKeyListener != null) {
      msgTypeArea.setEnterKeyListener(null);
      enterKeyListener = null;
    }
  }

}