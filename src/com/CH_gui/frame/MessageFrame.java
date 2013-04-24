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

package com.CH_gui.frame;

import com.CH_cl.service.ops.MsgDataOps;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgComposePanel;
import com.CH_gui.util.MiscGui;
import java.awt.BorderLayout;
import java.io.File;

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
* <b>$Revision: 1.24 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class MessageFrame extends JActionFrameClosable {

  private MsgComposePanel composePanel;
  private static int MAX_TITLE_LENGTH = 30;

  /** Creates new MessageFrame */
  public MessageFrame() {
    this((Record[]) null, MsgDataRecord.OBJ_TYPE_MSG);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame()");
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave) {
    this(MsgDataRecord.OBJ_TYPE_MSG, draftMsgLink, isDeleteDraftAfterSave);
  }
  protected MessageFrame(short objType, MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave) {
    this(truncate(ListRenderer.getRenderedText(draftMsgLink)), (Record[][]) null, draftMsgLink, objType, false, isDeleteDraftAfterSave);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(short objType, MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave)");
    if (trace != null) trace.args(objType);
    if (trace != null) trace.args(draftMsgLink);
    if (trace != null) trace.args(isDeleteDraftAfterSave);
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record initialRecipient) {
    this(new Record[] { initialRecipient }, MsgDataRecord.OBJ_TYPE_MSG);
  }
  public MessageFrame(Record initialRecipient, EmailRecord sendFromEmailAccount) {
    this(new Record[] { initialRecipient }, MsgDataRecord.OBJ_TYPE_MSG);
    composePanel.setFromAccount(sendFromEmailAccount);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record initialRecipient, MsgLinkRecord replyToMsg) {
    this(new Record[][] {{ initialRecipient }}, replyToMsg);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record initialRecipient, MsgLinkRecord replyToMsg)");
    if (trace != null) trace.args(initialRecipient, replyToMsg);
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients) {
    this(initialRecipients, MsgDataRecord.OBJ_TYPE_MSG);
  }
  protected MessageFrame(Record[] initialRecipients, short objType) {
    this(new Record[][] { initialRecipients }, null, objType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[][] initialRecipients, short objType)");
    if (trace != null) trace.args(initialRecipients);
    if (trace != null) trace.args(objType);
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients, String subject) {
    super(com.CH_cl.lang.Lang.rb.getString("title_Compose_Message"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[] initialRecipients, String subject)");
    if (trace != null) trace.args(initialRecipients, subject);

    composePanel = new MsgComposePanel(initialRecipients);
    composePanel.setSubject(subject);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    composePanel.checkValidityOfRecipients();
    composePanel.markCurrentContentAndAttachmentsAsOriginal();
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients, String subject, String plainBody) {
    super(com.CH_cl.lang.Lang.rb.getString("title_Compose_Message"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[] initialRecipients, String subject, String plainBody)");
    if (trace != null) trace.args(initialRecipients, subject, plainBody);

    composePanel = new MsgComposePanel(initialRecipients);
    composePanel.setSubject(subject);
    composePanel.setBody(plainBody);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    composePanel.checkValidityOfRecipients();
    composePanel.markCurrentContentAndAttachmentsAsOriginal();
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[][] initialRecipients, MsgLinkRecord replyToMsg) {
    super(truncate(MsgDataOps.getSubjectReply(replyToMsg)), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[][] initialRecipients, MsgLinkRecord replyToMsg)");
    if (trace != null) trace.args(initialRecipients, replyToMsg);

    composePanel = new MsgComposePanel(initialRecipients, null, MsgDataRecord.OBJ_TYPE_MSG, false, true);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // Initializing the components involves focus request so do this after 
    // components is already added to the window.
    composePanel.setReplyTo_Threaded(replyToMsg, initialRecipients);

    // all JActionFrames already size themself
    setVisible(true);

    // setting a reply already checks validity of recipients
    //composePanel.checkValidityOfRecipients();
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients, LinkRecordI[] attachments) {
    super(truncate(MsgDataOps.getSubjectForward(attachments)), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[] initialRecipients, LinkRecordI[] attachments)");
    if (trace != null) trace.args(initialRecipients, attachments);

    composePanel = new MsgComposePanel(new Record[][] { initialRecipients }, attachments);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // Initializing the components involves focus request so do this after 
    // components is already added to the window.
    if (attachments != null && attachments.length == 1 && attachments[0] instanceof MsgLinkRecord) {
      composePanel.setForwardBody_Threaded((MsgLinkRecord) attachments[0]);
    }
    composePanel.setFocusToRecipient();

    // all JActionFrames already size themself
    setVisible(true);

    composePanel.checkValidityOfRecipients();
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients, File[] attachFiles) {
    this (initialRecipients, null, null, attachFiles);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[] initialRecipients, File[] attachFiles)");
    if (trace != null) trace.exit(MessageFrame.class);
  }


  /** Creates new MessageFrame */
  public MessageFrame(Record[] initialRecipients, String subject, String plainBody, File[] attachFiles) {
    super(truncate(MsgDataOps.getSubjectForward(attachFiles)), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(Record[] initialRecipients, String subject, String plainBody, File[] attachFiles)");
    if (trace != null) trace.args(initialRecipients, subject, plainBody, attachFiles);

    composePanel = new MsgComposePanel(new Record[][] { initialRecipients }, attachFiles);
    if (subject != null)
      composePanel.setSubject(subject);
    if (plainBody != null)
      composePanel.setBody(plainBody);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    composePanel.checkValidityOfRecipients();
    if (trace != null) trace.exit(MessageFrame.class);
  }

  /** Creates new MessageFrame */
  private MessageFrame(Record[][] initialRecipients, XMLElement draftData, short objType) {
    this(objType == MsgDataRecord.OBJ_TYPE_MSG ? com.CH_cl.lang.Lang.rb.getString("title_Compose_Message") : com.CH_cl.lang.Lang.rb.getString("title_Compose_Address"), 
          initialRecipients, draftData, objType, false, false);
  }
  public MessageFrame(String title, Record[] initialRecipients, XMLElement draftData) {
    this(title, new Record[][] { initialRecipients }, draftData, MsgDataRecord.OBJ_TYPE_MSG, true, false);
  }
  protected MessageFrame(String title, Record[] initialRecipients, XMLElement draftData, short objType) {
    this(title, new Record[][] { initialRecipients }, draftData, objType, true, false);
  }
  private MessageFrame(String title, Record[][] initialRecipients, Object draftData, short objType, boolean truncateTitle, boolean isDeleteDraftAfterSave) {
    super(truncateTitle ? truncate(title) : title, true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageFrame.class, "MessageFrame(String title, Record[][] initialRecipients, XMLElement draftData, short objType, boolean isDeleteDraftAfterSave)");
    if (trace != null) trace.args(title, initialRecipients, draftData);
    if (trace != null) trace.args(objType);
    if (trace != null) trace.args(isDeleteDraftAfterSave);

    boolean skipSignatures = draftData != null;
    composePanel = new MsgComposePanel(initialRecipients, null, objType, false, skipSignatures);
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      this.getContentPane().add(composePanel.initToolBarModel(MiscGui.getVisualsKeyName(this), null, composePanel).getToolBar(), BorderLayout.NORTH);
    this.getContentPane().add(composePanel, BorderLayout.CENTER);

    // Initializing the components involves focus request so do this after 
    // components is already added to the window.
    if (draftData != null) {
      if (draftData instanceof XMLElement)
        composePanel.setFromDraft((XMLElement) draftData);
      else if (draftData instanceof MsgLinkRecord)
        composePanel.setFromDraft_Threaded((MsgLinkRecord) draftData, isDeleteDraftAfterSave);
    } else {
      composePanel.setFocusToRecipient();
    }

    // all JActionFrames already size themself
    setVisible(true);

    composePanel.checkValidityOfRecipients();
    if (trace != null) trace.exit(MessageFrame.class);
  }


  private static String truncate(String str) {
    if (MAX_TITLE_LENGTH > 0 && str.length() > MAX_TITLE_LENGTH) {
      str = str.substring(0, MAX_TITLE_LENGTH) + "...";
    }
    return str;
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MessageFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}