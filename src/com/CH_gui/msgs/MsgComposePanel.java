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

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.MsgTypingListener;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.InternetAddressRecord;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.util.MsgUtils;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.cnt.Cnt_NewCnt_Rq;
import com.CH_co.service.msg.dataSets.key.Key_PubKeys_Rp;
import com.CH_co.service.msg.dataSets.msg.Msg_New_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_ID_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.Usr_AltUsrData_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.addressBook.*;
import com.CH_gui.chatTable.ChatActionTable;
import com.CH_gui.dialog.InitiateContactDialog;
import com.CH_gui.dialog.RecipientsDialog;
import com.CH_gui.dialog.RecordChooserDialog;
import com.CH_gui.fileTable.FileDND_Transferable;
import com.CH_gui.fileTable.FileDND_TransferableData;
import com.CH_gui.frame.AddressFrame;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.list.ListComparator;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.list.ListUpdatableI;
import com.CH_gui.menuing.PopupMouseAdapter;
import com.CH_gui.menuing.ToolBarModel;
import com.CH_gui.msgTable.MsgDND_Transferable;
import com.CH_gui.msgTable.MsgDND_TransferableData;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.JMyRadioButton;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.undo.UndoManager;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.73 $</b>
*
* @author  Marcin Kurzawa
*/
public class MsgComposePanel extends JPanel implements ActionProducerI, ToolBarProducerI, DropTargetListener, DisposableObj, MsgTypeManagerI, MsgComposeManagerI, SendMessageInfoProviderI, UndoManagerI, VetoRisibleI {

  public static final String PROPERTY_NAME__SHOW_ALL_HEADERS = "showAllHeaders";

  private Action[] actions;

  public static final int SEND_ACTION = 0;
  public static final int SELECT_RECIPIENTS_ACTION = 1;
  public static final int SELECT_ATTACHMENTS_ACTION = 2;
  public static final int CUT_ACTION = 3;
  public static final int COPY_ACTION = 4;
  public static final int PASTE_ACTION = 5;
  public static final int PRIORITY_ACTION = 6; // 7, 8 also
  public static final int UNDO_ACTION = 9;
  public static final int REDO_ACTION = 10;
  public static final int SHOW_ALL_HEADERS = 11;
  public static final int SELECT_RECIPIENTS_CC_ACTION = 12;
  public static final int SELECT_RECIPIENTS_BCC_ACTION = 13;
  public static final int SAVE_AS_DRAFT_ACTION = 14;
  public static final int SPELL_CHECK_ACTION = 15;
  public static final int SPELL_CHECK_EDIT_DICTIONARY_ACTION = 16;
  public static final int SPELL_CHECK_OPTIONS_ACTION = 17;
  public static final int RECORD_PANEL_ACTION = 18;
  public static final int RING_BELL_ACTION = 19;

  private int leadingActionId = Actions.LEADING_ACTION_ID_COMPOSE_MESSAGE_PANEL;

  public static final short PRIORITY_INDEX_FYI = 0;
  public static final short PRIORITY_INDEX_NORMAL = 1;
  public static final short PRIORITY_INDEX_HIGH = 2;

  private FetchedDataCache cache;

  private short objType;
  private boolean isChatComposePanel;
  private boolean isSuppressInitialSignatures;

  private MsgComposeComponents msgComponents;
  private TypingListener typingListener;

  // set when source of the message is a draft
  private MsgLinkRecord fromDraftMsgLink;
  private boolean isDeleteDraftAfterSave;
  // set when saving as draft, unset otherwise
  private boolean isSavingAsDraft;
  // hook up to the vetoable frame
  private boolean isVetoableHookAttempted;
  // for mail messages see if message was sent
  private boolean isMessageSent;

  private Record[][] selectedRecipients = new Record[RECIPIENT_TYPES.length][];
  private Record[][] preConversionSelectedRecipients = null;
  private Record[][] postConversionSelectedRecipients = null;

  private Object[] selectedAttachments;
  private MsgLinkRecord replyToMsgLink;

  private Object[] originalContent;
  private Object[] originalAttachments;

  private boolean isAttachmentsPanel = true;
  private boolean isPriorityPanel = true;

  private boolean sendMessageInProgress;  // while send message is in progress, disable input components

  private ArrayList dropTargetL = new ArrayList();
  private ArrayList componentsForDNDL = new ArrayList();
  private ArrayList componentsForPopupL = new ArrayList();

  private UndoManager undoMngr;

  private ToolBarModel toolBarModel;

  /** Creates new MsgComposePanel */
  public MsgComposePanel(Record[] initialRecipients) {
    this(new Record[][] { initialRecipients });
  }
  public MsgComposePanel(Record[][] initialRecipients) {
    this(initialRecipients, (FileLinkRecord[]) null);
  }
  /** Creates new MsgComposePanel */
  public MsgComposePanel(Record initialRecipient, short objType, boolean isChatComposePanel) {
    this(new Record[][] {{ initialRecipient }}, (FileLinkRecord[]) null, objType, isChatComposePanel, false);
  }
  /** Creates new MsgComposePanel */
  public MsgComposePanel(Record[][] initialRecipients, Object[] initialAttachments) {
    this(initialRecipients, initialAttachments, MsgDataRecord.OBJ_TYPE_MSG, false, false);
  }
  /** Creates new MsgComposePanel */
  public MsgComposePanel(Record[][] initialRecipients, Object[] initialAttachments, short objType, boolean isChatComposePanel, boolean isSuppressInitialSignatures) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "MsgComposePanel(Record[][] initialRecipients, Object[] initialAttachments, short objType, boolean isChatComposePanel, boolean isSuppressInitialSignatures)");
    if (trace != null) trace.args(initialRecipients, initialAttachments);
    if (trace != null) trace.args(objType);
    if (trace != null) trace.args(isChatComposePanel);
    if (trace != null) trace.args(isSuppressInitialSignatures);

    this.objType = objType;
    this.isChatComposePanel = isChatComposePanel;
    this.isSuppressInitialSignatures = isSuppressInitialSignatures;
    for (int i=0; initialRecipients!=null && i<initialRecipients.length; i++) {
      if (i < this.selectedRecipients.length) {
        // make sure we have a Record[][] and not other arrays instances which extend from Record
        Record[] recipients = null;
        if (initialRecipients[i] != null) {
          recipients = new Record[initialRecipients[i].length];
          if (initialRecipients[i].length > 0)
            Arrays.asList(initialRecipients[i]).toArray(recipients);
        }
        this.selectedRecipients[i] = recipients;
      }
    }
    this.selectedAttachments = initialAttachments;

    // sort the initial recipients and attachments
    if (this.selectedRecipients != null)
      for (int i=0; i<this.selectedRecipients.length; i++)
        if (this.selectedRecipients[i] != null && this.selectedRecipients[i].length > 1)
          Arrays.sort(this.selectedRecipients[i], new ListComparator());
    if (this.selectedAttachments != null && this.selectedAttachments.length > 1)
      Arrays.sort(this.selectedAttachments, new ListComparator());

    // convert initial recipients to familiar contacts
    convertSelectedRecipientsToFamiliar_Threaded(true);

    init();

    if (selectedAttachments != null && selectedAttachments.length > 0) {
      msgComponents.getAttachmentsPanel().addHierarchyListener(new InitialRunner(new Runnable() {
        public void run() {
          // redraw the attachments panel because its parent container was initially sized.
          setAttachmentsPanel();
        }
      }));
    }

    Component[] components = MiscGui.getComponentsRecursively(this);
    addPopupsAndDND(components);

    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  private void convertSelectedRecipientsToFamiliar_Threaded(final boolean markAsOriginalContent) {
    Thread th = new ThreadTraced("MsgComposePanel Selected Recipient To Familiar Converter") {
      public void runTraced() {
        // convert all email addresses and address contacts to users or contacts if possible
        for (int i=TO; i<RECIPIENT_TYPES.length; i++) {
          try {
            if (selectedRecipients[i] != null && selectedRecipients[i].length > 0) {
              boolean anyConverted = false;
              // This will also trigger fetch of any EmailRecord that we might be sending to
              // and panel renderer will be able to substitute it to show that encryption is possible.
              anyConverted = UserOps.convertRecipientEmailAndUnknownUsersToFamiliars(MainFrame.getServerInterfaceLayer(), selectedRecipients[i], false, false);
              if (anyConverted)
                redrawRecipients(i);
              if (markAsOriginalContent)
                markCurrentContentAndAttachmentsAsOriginal();
            }
          } catch (Throwable t) {
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private void addPopupsAndDND(Component[] components) {
    if (components != null) {
      for (int i=0; i<components.length; i++) {
        Component c = components[i];
        //addPopup(c); -- it stopped working in newer Java
        addDND(c);
      }
    }
  }
  private void addPopup(Component c) {
    if (c != null && !componentsForPopupL.contains(c)) {
      c.addMouseListener(new PopupMouseAdapter(c, this));
      componentsForPopupL.add(c);
    }
  }
  private void addDND(Component c) {
    if (c != null && !componentsForDNDL.contains(c)) {
      dropTargetL.add(new DropTarget(c, this));
      componentsForDNDL.add(c);
    }
  }


  /** Initialize the class */
  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "init()");

    cache = FetchedDataCache.getSingleInstance();
    undoMngr = new UndoManager();

    initActions();
    initComponents();
    initCopyToSent();
    setBorder(new EmptyBorder(0,0,0,0));
    setAttachmentsPanel();
    setPriorityPanel();
    redrawRecipientsAll();
    //setEnabledActions(); -- already done when redrawing recipients panel

    markCurrentContentAndAttachmentsAsOriginal();

    addMouseListener(new PopupMouseAdapter(this, this));

    setMinimumSize(new Dimension(0, getMinimumSize().height));

    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        ancestorEvent(event);
      }
      public void ancestorMoved(AncestorEvent event) {
        ancestorEvent(event);
      }
      public void ancestorRemoved(AncestorEvent event) {
        ancestorEvent(event);
      }
      private void ancestorEvent(AncestorEvent event) {
        if (!isVetoableHookAttempted) {
          Container c = event.getAncestor();
          if (c.isVisible()) {
            isVetoableHookAttempted = true;
            Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
            if (w instanceof VetoableI) {
              ((VetoableI) w).addVetoRisibleI(MsgComposePanel.this);
            }
          }
        }
      }
    });

    typingListener = new TypingListener();
    cache.addMsgTypingListener(typingListener);

    // Clear all changes when creating components with initial text
    undoMngr.discardAllEdits();
    setEnabledActions();
    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  /**
  * Remembers all content as original for purposes of checking later on if any changes were made.
  */
  public void markCurrentContentAndAttachmentsAsOriginal() {
    markCurrentContentAsOriginal();
    markCurrentAttachmentsAsOriginal();
  }
  /**
  * Remembers the current content (subject, body) as original
  * for purpose of checking later on if any changes were made.
  */
  private void markCurrentContentAsOriginal() {
    originalContent = getContent();
  }
  /**
  * Remembers the current attachments as original
  * for purpose of checking later on if any changes were made.
  */
  private void markCurrentAttachmentsAsOriginal() {
    originalAttachments = getSelectedAttachments();
  }
  private boolean isOriginalContentOrAttachmentsChanged() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "isOriginalContentOrAttachmentsChanged()");
    String[] currentContent = getContent();
    Object[] currentAttachments = getSelectedAttachments();
    boolean anythingChanged = !originalContent[0].equals(currentContent[0]) || !originalContent[1].equals(currentContent[1]);
    // compare attachments
    if (!anythingChanged) {
      int len1 = currentAttachments != null ? currentAttachments.length : 0;
      int len2 = originalAttachments != null ? originalAttachments.length : 0;
      if (len1 != len2) {
        anythingChanged = true;
      } else if (currentAttachments != null && originalAttachments != null) {
        Object[] diff1 = ArrayUtils.getDifference2(currentAttachments, originalAttachments);
        Object[] diff2 = ArrayUtils.getDifference2(originalAttachments, currentAttachments);
        anythingChanged = (diff1 != null && diff1.length > 0) || (diff2 != null && diff2.length > 0);
      }
    }
    if (trace != null) trace.exit(MsgComposePanel.class, anythingChanged);
    return anythingChanged;
  }

  private boolean isAnyRecipients() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "isAnyRecipients()");
    boolean anyRecipients = false;
    if (selectedRecipients != null) {
      for (int k=0; k<selectedRecipients.length; k++) {
        if (!anyRecipients && selectedRecipients[k] != null) {
          for (int i=0; i<selectedRecipients[k].length; i++) {
            Record recipient = selectedRecipients[k][i];
            if (recipient != null) {
              anyRecipients = true;
              break;
            }
          }
        }
      }
    }
    if (trace != null) trace.exit(MsgComposePanel.class, anyRecipients);
    return anyRecipients;
  }

  private void initComponents() {
    msgComponents = new MsgComposeComponents(objType, isChatComposePanel, isAnyRecipients(), this, this, this);
    // mail mode
    if (!isChatComposePanel) {
      if (selectedAttachments != null && selectedAttachments.length > 0) {
        msgComponents.setForward(selectedAttachments);
        if (!isSuppressInitialSignatures)
          msgComponents.setSignatureIfRequired(true);
      } else {
        if (!isSuppressInitialSignatures)
          msgComponents.setSignatureIfRequired(replyToMsgLink != null);
      }
    // chat mode
    } else {
      // priority -- present by default in chat mode
      isPriorityPanel = true;
    }
    msgComponents.initMainPanel(this);
  }

  private void initCopyToSent() {
    // if initially any of the recipients is a ContactRecord or UserRecord -- enable copy to Outgoing
    int recipientCount = 0;
    boolean enableCopyToOutgoing = false;
    if (selectedRecipients != null) {
      for (int k=0; k<selectedRecipients.length; k++) {
        if (selectedRecipients[k] != null) {
          for (int i=0; i<selectedRecipients[k].length; i++) {
            Record recipient = selectedRecipients[k][i];
            if (recipient != null) {
              recipientCount ++;
              if (recipient instanceof ContactRecord ||
                  recipient instanceof UserRecord ||
                  recipient instanceof InternetAddressRecord ||
                  recipient instanceof InvEmlRecord
              ) {
                enableCopyToOutgoing = true;
              } else if (recipient instanceof FolderPair) {
                FolderPair fPair = (FolderPair) recipient;
                FolderRecord fRec = fPair.getFolderRecord();
                if (fRec.isAddressType() || fRec.isGroupType()) {
                  enableCopyToOutgoing = true;
                }
              } else if (recipient instanceof MsgDataRecord) {
                MsgDataRecord mData = (MsgDataRecord) recipient;
                if (mData.isTypeAddress()) {
                  enableCopyToOutgoing = true;
                }
              }
            } // end if recipient != null
          }
        }
      }
    }
    // if should enable or no recipients pre-selected -- enable copy to Outgoing
    msgComponents.setSelectedCopy(enableCopyToOutgoing || (recipientCount == 0 && !isChatComposePanel));
  }

  public void setFromAccount(EmailRecord sendFromEmailAccount) {
    msgComponents.setFromAccount(sendFromEmailAccount);
  }

  /**
  * Initialize the message composer from the selected draft message.
  */
  public void setFromDraft_Threaded(final MsgLinkRecord draftMsgLink, final boolean isDeleteDraftAfterSave) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setFromDraft_Threaded(MsgLinkRecord draftMsgLink)");
    if (trace != null) trace.args(draftMsgLink);
    if (trace != null) trace.args(isDeleteDraftAfterSave);
    final Runnable updateGUI = new Runnable() {
      public void run() {
        setFromDraft(draftMsgLink, isDeleteDraftAfterSave);
      }
    };
    Runnable updateGUIthreadSafe = new Runnable() {
      public void run() {
        SwingUtilities.invokeLater(updateGUI);
      }
    };
    MsgDataRecord dataRecord = cache.getMsgDataRecord(draftMsgLink.msgId);
    if (dataRecord.getText() != null) {
      updateGUIthreadSafe.run();
    } else {
      ProtocolMsgDataSet request = MsgDataOps.prepareRequestToFetchMsgBody(cache, draftMsgLink);
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 25000, null, updateGUIthreadSafe, updateGUIthreadSafe);
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  private void setFromDraft(final MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setFromDraft(MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave)");
    if (trace != null) trace.args(draftMsgLink);
    if (trace != null) trace.args(isDeleteDraftAfterSave);

    MsgDataRecord dataRecord = cache.getMsgDataRecord(draftMsgLink.msgId);
    UserRecord userRecord = cache.getUserRecord();

    // set the GUI
    msgComponents.setFromDraft(draftMsgLink, dataRecord);

    // set DraftSource so that it can be deleted when sent
    this.fromDraftMsgLink = draftMsgLink;
    this.isDeleteDraftAfterSave = isDeleteDraftAfterSave;
    this.replyToMsgLink = null;

    setPriorityPanel();

    // Initialize selected recipients for mail and anything in Drafts folder
    if (dataRecord.isTypeMessage() || (draftMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER && draftMsgLink.ownerObjId.equals(userRecord.draftFolderId))) {
      Record[][] recipients = CacheMsgUtils.gatherAllMsgRecipients(cache, dataRecord);
      for (int i=0; i<selectedRecipients.length && i<recipients.length; i++) {
        selectedRecipients[i] = recipients[i];
      }
    } else if (dataRecord.isTypeAddress()) {
      // For addresses, initialize recipient to be the folder of the link
      if (draftMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
        FolderRecord toFolder = cache.getFolderRecord(draftMsgLink.ownerObjId);
        selectedRecipients[TO] = CacheFldUtils.convertRecordToPairs(cache, toFolder);
      }
    }

    // do fetching of attachments in a seperate thread as this may take a while
    Thread th = new ThreadTraced("Setting From Draft Attachments Fetcher") {
      public void runTraced() {
        Record[] attachments = AttachmentFetcherPopup.fetchAttachments(new MsgLinkRecord[] { draftMsgLink });
        addAdditionalAttachments(attachments);
        markCurrentAttachmentsAsOriginal();
      }
    };
    th.setDaemon(true);
    th.start();

    // recipients are filled, so subject is first empty/editable field
    if (isAnyRecipients()) {
      msgComponents.setFocusToSubject();
    } else {
      msgComponents.setFocusToRecipient();
    }

    markCurrentContentAsOriginal();

    redrawRecipientsAll();
    initCopyToSent();
    revalidate();

    // previous email address maybe convertible to contacts ...
    convertSelectedRecipientsToFamiliar_Threaded(true);

    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  public void setFromDraft(XMLElement draftData) {
    msgComponents.setFromDraft(draftData);
    markCurrentContentAsOriginal();
  }

  public void setFocusToRecipient() {
    msgComponents.setFocusToRecipient();
  }

  /**
  * Initialize the message composer to form a forward of selected message.
  */
  public void setForwardBody_Threaded(final MsgLinkRecord forwardMsg) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setForwardBody_Threaded(MsgLinkRecord forwardMsg");
    if (trace != null) trace.args(forwardMsg);
    final Runnable updateGUI = new Runnable() {
      public void run() {
        setForwardBody(forwardMsg);
      }
    };
    Runnable updateGUIthreadSafe = new Runnable() {
      public void run() {
        SwingUtilities.invokeLater(updateGUI);
      }
    };
    MsgDataRecord dataRecord = cache.getMsgDataRecord(forwardMsg.msgId);
    if (dataRecord.getText() != null) {
      updateGUIthreadSafe.run();
    } else {
      ProtocolMsgDataSet request = MsgDataOps.prepareRequestToFetchMsgBody(cache, forwardMsg);
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 25000, null, updateGUIthreadSafe, updateGUIthreadSafe);
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  private void setForwardBody(MsgLinkRecord forwardMsg) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setforwardBody(MsgLinkRecord forwardMsg");
    if (trace != null) trace.args(forwardMsg);

    MsgDataRecord dataRecord = cache.getMsgDataRecord(forwardMsg.msgId);

    if (dataRecord != null) {
      // Forward Body
      msgComponents.setQuotedContent(forwardMsg, dataRecord, false, true);
    }

    msgComponents.setFocusToRecipient();
    markCurrentContentAndAttachmentsAsOriginal();

    initCopyToSent();
    revalidate();

    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  /**
  * Initialize the message composer to form a reply to selected message.
  */
  public void setReplyTo_Threaded(final MsgLinkRecord replyToMsg, final Record[][] overrideRecipientsList) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setReplyTo_Threaded(MsgLinkRecord replyToMsg, Record[][] overrideRecipientsList)");
    if (trace != null) trace.args(replyToMsg, overrideRecipientsList);
    final Runnable updateGUI = new Runnable() {
      public void run() {
        setReplyTo(replyToMsg, overrideRecipientsList);
      }
    };
    Runnable updateGUIthreadSafe = new Runnable() {
      public void run() {
        SwingUtilities.invokeLater(updateGUI);
      }
    };
    MsgDataRecord dataRecord = cache.getMsgDataRecord(replyToMsg.msgId);
    if (dataRecord.getText() != null) {
      if (trace != null) trace.data(10, "message text is available, will move to set reply content right away.");
      updateGUIthreadSafe.run();
    } else {
      if (trace != null) trace.data(20, "message text is NOT available, will request message body then set reply content.");
      ProtocolMsgDataSet request = MsgDataOps.prepareRequestToFetchMsgBody(cache, replyToMsg);
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 25000, null, updateGUIthreadSafe, updateGUIthreadSafe);
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  private void setReplyTo(MsgLinkRecord replyToMsg, Record[][] overrideRecipientsList) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setReplyTo(MsgLinkRecord replyToMsg, Record[][] overrideRecipientsList)");
    if (trace != null) trace.args(replyToMsg, overrideRecipientsList);

    // remember the replyToMsg so we can set the server request appropriately
    this.replyToMsgLink = replyToMsg;
    this.fromDraftMsgLink = null;

    MsgDataRecord dataRecord = cache.getMsgDataRecord(replyToMsg.msgId);

    if (dataRecord != null) {
      // Reply-To
      if (overrideRecipientsList != null && overrideRecipientsList.length > 0) {
        if (overrideRecipientsList.length > TO && overrideRecipientsList[TO] != null) {
          selectedRecipients[TO] = overrideRecipientsList[TO];
        } else {
          selectedRecipients[TO] = new Record[0];
        }
        if (overrideRecipientsList.length > CC && overrideRecipientsList[CC] != null) {
          selectedRecipients[CC] = overrideRecipientsList[CC];
        } else {
          selectedRecipients[CC] = new Record[0];
        }
        if (overrideRecipientsList.length > BCC && overrideRecipientsList[BCC] != null) {
          selectedRecipients[BCC] = overrideRecipientsList[BCC];
        } else {
          selectedRecipients[BCC] = new Record[0];
        }
        checkValidityOfRecipients();
      } else {
        Record[] replyToRecs = null;
        String[] replyToAddresses = dataRecord.getReplyToAddresses();
        if (replyToAddresses != null && replyToAddresses.length > 0) {
          replyToRecs = new Record[replyToAddresses.length];
          for (int i=0; i<replyToAddresses.length; i++) {
            EmailAddressRecord emlRec = new EmailAddressRecord(replyToAddresses[i]);
            replyToRecs[i] = emlRec;
          }
          // change the To: field
          if (replyToRecs != null) {
            selectedRecipients[TO] = replyToRecs;
            redrawRecipients(TO);
          }
        }
      }
      // if all recipients are folders.... then use short form -- 'no quotation'
      boolean allFolders = true;
      for (int i=0; selectedRecipients!=null && i<selectedRecipients.length; i++) {
        for (int k=0; selectedRecipients[i]!=null && k<selectedRecipients[i].length; k++) {
          if (selectedRecipients[i][k] != null && !(selectedRecipients[i][k] instanceof FolderPair)) {
            allFolders = false;
            break;
          }
        }
      }
      if (isAnyRecipients() && allFolders) {
        msgComponents.setPostReply(replyToMsg, dataRecord);
      } else {
        msgComponents.setQuotedContent(replyToMsg, dataRecord, true, false);
      }
    }

    // recipients are filled, so subject is first empty/editable field
    if (isAnyRecipients()) {
      msgComponents.setFocusToBody();
    } else {
      msgComponents.setFocusToRecipient();
    }

    markCurrentContentAndAttachmentsAsOriginal();

    initCopyToSent();
    revalidate();

    // previous email address maybe convertible to contacts...
    convertSelectedRecipientsToFamiliar_Threaded(true);

    if (trace != null) trace.exit(MsgComposePanel.class);
  }


  public void setSubject(String subject) {
    if (msgComponents != null)
      msgComponents.setSubject(subject);
  }

  public void setBody(String body) {
    if (msgComponents != null) {
      if (msgComponents.getContentMode() == CONTENT_MODE_MAIL_HTML) {
        if (body.startsWith("<HTML>") || body.startsWith("<html>"))
          msgComponents.setMsgContent(body, true);
        else
          msgComponents.setMsgContent("<html><body><p>"+Misc.encodePlainIntoHtml(body)+"</p></body></html>", true);
      } else if (msgComponents.getContentMode() == CONTENT_MODE_MAIL_PLAIN) {
        if (body.startsWith("<HTML>") || body.startsWith("<html>"))
          msgComponents.setMsgContent(MsgPanelUtils.extractPlainFromHtml(body), false);
        else
          msgComponents.setMsgContent(body, false);
      }
    }
  }

  private void setAttachmentsPanel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setAttachmentsPanel()");
    if (selectedAttachments != null && selectedAttachments.length > 0) {
      if (!isAttachmentsPanel) {
        msgComponents.setVisibleAttachments(true);
        isAttachmentsPanel = true;
      }
    } else {
      if (isAttachmentsPanel) {
        msgComponents.setVisibleAttachments(false);
        isAttachmentsPanel = false;
      }
    }
    if (isAttachmentsPanel) {
      MsgPanelUtils.drawRecordFlowPanel(selectedAttachments, msgComponents.getAttachmentsPanel());
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  private void setPriorityPanel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setPriorityPanel()");
    int priorityIndex = msgComponents.getPriorityIndex();
    if (priorityIndex != PRIORITY_INDEX_NORMAL) {
      if (!isPriorityPanel) {
        // do not add priority panel in chat mode -- it is already there
        if (!isChatComposePanel) {
          msgComponents.setVisiblePriority(true);
          isPriorityPanel = true;
        }
      }
    } else {
      if (isPriorityPanel) {
        // do not remove priority panel in chat mode
        if (!isChatComposePanel) {
          msgComponents.setVisiblePriority(false);
          isPriorityPanel = false;
        }
      }
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }


  private void initActions() {
    actions = new Action[20];
    actions[SEND_ACTION] = new SendAction(leadingActionId+SEND_ACTION);
    if (!isChatComposePanel)
      actions[SELECT_RECIPIENTS_ACTION] = new SelectRecipientsAction(leadingActionId+SELECT_RECIPIENTS_ACTION);
    actions[SELECT_ATTACHMENTS_ACTION] = new SelectAttachmentsAction(leadingActionId+SELECT_ATTACHMENTS_ACTION);
    actions[CUT_ACTION] = new CutAction(leadingActionId+CUT_ACTION);
    actions[COPY_ACTION] = new CopyAction(leadingActionId+COPY_ACTION);
    actions[PASTE_ACTION] = new PasteAction(leadingActionId+PASTE_ACTION);

    ButtonGroup priorityGroup = new ButtonGroup();
    actions[PRIORITY_ACTION] = new PriorityAction(MsgDataRecord.IMPORTANCE_FYI_HTML, leadingActionId+PRIORITY_ACTION, priorityGroup);
    actions[PRIORITY_ACTION+1] = new PriorityAction(MsgDataRecord.IMPORTANCE_NORMAL_HTML, leadingActionId+PRIORITY_ACTION+1, priorityGroup);
    actions[PRIORITY_ACTION+2] = new PriorityAction(MsgDataRecord.IMPORTANCE_HIGH_HTML, leadingActionId+PRIORITY_ACTION+2, priorityGroup);

    actions[UNDO_ACTION] = new UndoAction(leadingActionId+UNDO_ACTION);
    actions[REDO_ACTION] = new RedoAction(leadingActionId+REDO_ACTION);

    if (!isChatComposePanel)
      actions[SHOW_ALL_HEADERS] = new ShowAllHeaders(leadingActionId+SHOW_ALL_HEADERS);

    actions[SAVE_AS_DRAFT_ACTION] = new SaveAsDraftAction(leadingActionId+SAVE_AS_DRAFT_ACTION);
    actions[SPELL_CHECK_ACTION] = new SpellCheckAction(leadingActionId+SPELL_CHECK_ACTION);
    actions[SPELL_CHECK_EDIT_DICTIONARY_ACTION] = new SpellCheckEditDictionaryAction(leadingActionId+SPELL_CHECK_EDIT_DICTIONARY_ACTION);
    actions[SPELL_CHECK_OPTIONS_ACTION] = new SpellCheckOptionsAction(leadingActionId+SPELL_CHECK_OPTIONS_ACTION);

    if (!isChatComposePanel)
      actions[RECORD_PANEL_ACTION] = new RecordPanelAction(leadingActionId+RECORD_PANEL_ACTION);
    if (isChatComposePanel)
      actions[RING_BELL_ACTION] = new RingBellAction(leadingActionId+RING_BELL_ACTION);

    setEnabledActions();
  }

  public void ringPressed() {
    Record[] toRecipients = getSelectedRecipients(MsgLinkRecord.RECIPIENT_TYPE_TO);
    if (toRecipients != null && toRecipients.length == 1 && toRecipients[0] instanceof FolderPair) {
      final FolderPair _toFolderPair = (FolderPair) toRecipients[0];
      final Component _this = this;
      Thread th = new ThreadTraced("Ring Pressed") {
        public void runTraced() {
          ClientMessageAction msgAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_RING_RING, new Obj_ID_Rq(_toFolderPair.getId())), 15000, 3);
          DefaultReplyRunner.runAction(msgAction);
          if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_RING_RING) {
            Obj_List_Co reply = (Obj_List_Co) msgAction.getMsgDataSet();
            Object[] objs = reply.objs;
            Long[] distributionUserIDs = (Long[]) ArrayUtils.toArrayType((Object[]) objs[3], Long.class);
            UserRecord myUserRec = cache.getUserRecord();
            if (distributionUserIDs != null && distributionUserIDs.length == 1 && myUserRec != null && distributionUserIDs[0].equals(myUserRec.userId)) {
              MessageDialog.showInfoDialog(_this, "No one is available to hear your ring...", "Ring, Ring...");
            } else {
              ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
              new SendMessageRunner(SIL, new SendMessageInfoProviderI() {
                public Object getContext() {
                  return MsgComposePanel.this;
                }
                public String[] getContent() {
                  return new String[] { "ring...", "" };
                }
                public Short getContentType() {
                  return new Short(MsgDataRecord.OBJ_TYPE_MSG);
                }
                public short getContentMode() {
                  return MsgComposePanel.CONTENT_MODE_MAIL_PLAIN;
                }
                public Timestamp getExpiry() {
                  return null;
                }
                public Record getFromAccount() {
                  return MsgComposePanel.this.getFromAccount();
                }
                public String getQuestion() {
                  return null;
                }
                public String getPassword() {
                  return null;
                }
                public short getPriority() {
                  return MsgDataRecord.IMPORTANCE_NORMAL_PLAIN;
                }
                public MsgLinkRecord getReplyToMsgLink() {
                  return null;
                }
                public Object[] getSelectedAttachments() {
                  return null;
                }
                public Object[] getInlineAttachments() {
                  return null;
                }
                public Object[] getSelectedAndInlineAttachments() {
                  return null;
                }
                public Record[][] getSelectedRecipients() {
                  return MsgComposePanel.this.getSelectedRecipients();
                }
                public Record[] getSelectedRecipients(short type) {
                  return MsgComposePanel.this.getSelectedRecipients(type);
                }
                public boolean isCopyToOutgoing() {
                  return false;
                }
                public boolean isSavingAsDraft() {
                  return false;
                }
                public boolean isStagedSecure() {
                  return false;
                }
                public void messageSentNotify() {
                }
                public void setSendMessageInProgress(boolean inProgress) {
                }
              }).start();
              // person ringing must always hear the ring so that he refrains from abusing it and anoying other people...
              Nudge.nudge(_this, true);
            }
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================


  /**
  * Send composed message
  */
  private class SendAction extends AbstractActionTraced {
    public SendAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Send_message"), Images.get(ImageNums.MAIL_SEND16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Send_composed_message."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_SEND24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Send"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
        putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Save"));
        putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Save_composed_address."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.SAVE16));
        putValue(Actions.TOOL_ICON, Images.get(ImageNums.SAVE24));
        putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Save"));
      }
    }
    public void actionPerformedTraced(ActionEvent event) {
      isSavingAsDraft = false;
      actionPerformed();
    }
    public void actionPerformed() {
      // check if sufficient input is present
      if (!isVetoRaised(VetoRisibleI.TYPE_SAVE))
        actionPerformedNoVeto();
    }
    public void actionPerformedNoVeto() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformedNoVeto()");
      setSendMessageInProgress(true);
      Thread th = new ThreadTraced("Send Action Runner") {
        public void runTraced() {
          boolean isCanceled = false;
          // convert text input to recipient objects if needed
          for (int i=TO; i<RECIPIENT_TYPES.length; i++) {
            if (selectedRecipients[i] == null || selectedRecipients[i].length == 0) {
              boolean waitForResults = true;
              isCanceled = !selectRecipients(i, waitForResults);
              if (isCanceled)
                break;
            }
          }
          // Remember the original recipient list for purposes for Address Book insertion (before they get converted to familiar contacts, etc)
          // That insertion will take place if the send was successful..
          preConversionSelectedRecipients = (Record[][]) selectedRecipients.clone();
          // convert all email addresses and address contacts to users or contacts if possible
          if (!isSavingAsDraft) {
            ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
            for (int i=TO; i<RECIPIENT_TYPES.length; i++) {
              boolean expandAddressBooks = objType == MsgDataRecord.OBJ_TYPE_MSG;
              boolean expandGroups = true;
              int numRecipients = selectedRecipients[i].length;
              selectedRecipients[i] = UserOps.getExpandedListOfRecipients(SIL, selectedRecipients[i], expandAddressBooks, expandGroups);
              boolean anyExpanded = numRecipients != selectedRecipients[i].length;
              boolean anyConverted = false;
              if (selectedRecipients[i] != null && selectedRecipients[i].length > 0) {
                anyConverted = UserOps.convertRecipientEmailAndUnknownUsersToFamiliars(SIL, selectedRecipients[i], isStagedSecure(), isStagedSecure());
              }
              if (anyExpanded || anyConverted) {
                redrawRecipients(i);
                // Remember the converted recipient list for purpose of creating new contacts in care there were any additional new User web-accounts created...
                postConversionSelectedRecipients = (Record[][]) selectedRecipients.clone();
              }
            }
          }
          boolean anyRecipients = false;
          for (int i=0; i<selectedRecipients.length; i++) {
            anyRecipients |= selectedRecipients[i] != null && selectedRecipients[i].length > 0;
          }
          // if conversion to recipient objects was not canceled, then we should have at lest one recipient, else skip the rest
          if (!isCanceled && (anyRecipients || isSavingAsDraft)) {
            // Check if any attachments, if so then warn before skipping them for external recipients.
            // Also warn about non-encryption of external email.
            EmailAddressRecord[] emailRecs = null;
            InvEmlRecord[] invEmlRecs = null;
            int selectedRecipientsCount = 0;
            for (int i=0; i<selectedRecipients.length; i++) {
              emailRecs = (EmailAddressRecord[]) ArrayUtils.concatinate(emailRecs, ArrayUtils.gatherAllOfType(selectedRecipients[i], EmailAddressRecord.class));
              invEmlRecs = (InvEmlRecord[]) ArrayUtils.concatinate(invEmlRecs, ArrayUtils.gatherAllOfType(selectedRecipients[i], InvEmlRecord.class));
              selectedRecipientsCount += selectedRecipients[i].length;
            }
            Record[] emailAddresses = (Record[]) ArrayUtils.concatinate(emailRecs, invEmlRecs, Record.class);

            // Check if any attachments, to see if we should force the external-email-no-attachments warning
            boolean anyAttachments = false;
            Object[] allAttachments = getSelectedAndInlineAttachments();
            if (allAttachments != null && allAttachments.length > 0) {
              anyAttachments = true;
            }

            // Check if any external email addresses
            boolean anyEmailAddresses = false;
            if (emailAddresses != null && emailAddresses.length > 0) {
              anyEmailAddresses = true;
            }
            // Check if any internal recipients selected
            boolean anyInternalRecipients = true;
            if (emailAddresses != null && emailAddresses.length >= selectedRecipientsCount) {
              anyInternalRecipients = false;
            }

            // We must have at least one internal recipient if we have attachments and external email recipient(s)
            if (anyAttachments && anyEmailAddresses && !anyInternalRecipients) {
              msgComponents.setSelectedCopy(true);
            }

            final boolean _staged = isStagedSecure();
            final boolean _anyEmailAddresses = anyEmailAddresses;
            final Record[] _emailAddresses = (Record[]) ArrayUtils.removeDuplicates(emailAddresses);

            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                boolean canceled = false;
                if (!_staged && _anyEmailAddresses && !isSavingAsDraft)
                  canceled = !showStagedSecureChoiceDialog(_emailAddresses);
                if (!canceled) {
                  if (isStagedSecure() != _staged) {
                    actionPerformedNoVeto();
                  } else {
                    boolean showRegularEmailQuestion = _anyEmailAddresses && !isSavingAsDraft && !FetchedDataCache.getSingleInstance().getUserRecord().isSkipWarnExternal();
                    if (!showRegularEmailQuestion) {
                      ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
                      new SendMessageRunner(SIL, MsgComposePanel.this).start();
                    } else {
                      showRegularEmailWarningBeforeAndSend(_emailAddresses);
                    }
                  }
                } else {
                  setSendMessageInProgress(false);
                }
              }
            });
          } else {
            // If no recipient or user not found and user search window was cancelled, then resume message composer
            setSendMessageInProgress(false);
          }
        } // end run{}
      };
      th.setDaemon(true);
      th.start();
      if (trace != null) trace.exit(getClass());
    }
    private boolean showStagedSecureChoiceDialog(final Record[] emailAddresses) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "showStagedSecureChoiceDialog(final Record[] emailAddresses)");
      if (trace != null) trace.args(emailAddresses);
      final boolean[] okReturnBuffer = new boolean[] { false };

      JPanel panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      int posY = 0;
      String msgText1 = "The following email addresses support secure delivery via an encrypted";
      String msgText2 = "pick-up account, or directly in plain text: ";
      panel.add(new JMyLabel(msgText1), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 1, 10), 0, 0));
      posY ++;
      panel.add(new JMyLabel(msgText2), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 10, 10, 10), 0, 0));
      posY ++;
      JPanel emlListPanel = new JPanel();
      emlListPanel.setLayout(new GridBagLayout());
      for (int i=0; i<emailAddresses.length; i++) {
        Icon icon = ListRenderer.getRenderedIcon(emailAddresses[i]);
        String label = ListRenderer.getRenderedText(emailAddresses[i], false, false, false, false, false, true);
        emlListPanel.add(new JMyLabel(label, icon, JLabel.LEADING), new GridBagConstraints(0, i, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 2, 10), 0, 0));
      }
      emlListPanel.add(new JLabel(), new GridBagConstraints(0, emailAddresses.length, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
      JComponent listPane = emlListPanel;
      if (emailAddresses.length >= 3) {
        JScrollPane sc = new JScrollPane(emlListPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sc.getVerticalScrollBar().setUnitIncrement(5);
        listPane = sc;
      }
      panel.add(listPane, new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(10, 10, 10, 10), 0, 0));
      posY ++;
      final JRadioButton jSendPlain = new JMyRadioButton("Send in Plain Text", true);
      //final JRadioButton jSendEncrypted = new JMyRadioButton("Send Encrypted with Question and Answer", false);
      final JRadioButton jSendEncrypted = new JMyRadioButton("Send Encrypted", false);
      final JCheckBox jQuestionAndAnswer = new JMyCheckBox("Encrypt with Question and Answer", false);
      ButtonGroup group = new ButtonGroup();
      group.add(jSendPlain);
      group.add(jSendEncrypted);
      final JTextField jQuestion = new JMyTextField(10);
      final JTextField jPassword = new JMyTextField(10);
      jQuestionAndAnswer.setEnabled(false);
      jQuestion.setEnabled(false);
      jPassword.setEnabled(false);

      jSendEncrypted.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          jQuestionAndAnswer.setEnabled(jSendEncrypted.isSelected());
          jQuestion.setEnabled(jSendEncrypted.isSelected() && jQuestionAndAnswer.isSelected());
          jPassword.setEnabled(jSendEncrypted.isSelected() && jQuestionAndAnswer.isSelected());
        }
      });
      jQuestionAndAnswer.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          jQuestion.setEnabled(jSendEncrypted.isSelected() && jQuestionAndAnswer.isSelected());
          jPassword.setEnabled(jSendEncrypted.isSelected() && jQuestionAndAnswer.isSelected());
        }
      });

      panel.add(new JMyLabel("Please choose one of the following Send options:"), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 5, 10), 0, 0));
      posY ++;
      panel.add(jSendPlain, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 25, 2, 10), 0, 0));
      posY ++;
      if (msgComponents.getExpiry() != null) {
        panel.add(new JMyLabel("(Message expiry settings will not apply)"), new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 45, 2, 10), 0, 0));
        posY ++;
      }
      panel.add(jSendEncrypted, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 25, 2, 10), 0, 0));
      posY ++;
      panel.add(jQuestionAndAnswer, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 45, 2, 10), 0, 0));
      posY ++;
      panel.add(new JMyLabel("Question:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 65, 2, 5), 0, 0));
      panel.add(jQuestion, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 10), 0, 0));
      posY ++;
      panel.add(new JMyLabel("Answer:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 65, 10, 5), 0, 0));
      panel.add(jPassword, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 10, 10), 0, 0));
      posY ++;

      String title = "Additional Send Options...";

      JButton[] buttons = new JButton[2];
      buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Send"));
      buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
      buttons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (jSendPlain.isSelected() || jSendEncrypted.isSelected()) {
            msgComponents.setStagedSecure(jSendEncrypted.isSelected());
            String question = "";
            String passStripped = "";
            if (jSendEncrypted.isSelected() && jQuestionAndAnswer.isSelected()) {
              question = jQuestion.getText();
              // strip password leaving only letters and digits
              passStripped = MsgUtils.getTrimmedPassword(jPassword.getText());
            }
            msgComponents.setQuestion(question);
            msgComponents.setPassword(passStripped);
            okReturnBuffer[0] = true;
            SwingUtilities.windowForComponent((Component) e.getSource()).dispose();
          } else {
            MessageDialog.showDialog((Component) e.getSource(), "Please select one of the available options.", "Option...", NotificationCenter.ERROR_MESSAGE, true);
          }
        }
      });
      buttons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          okReturnBuffer[0] = false;
          SwingUtilities.windowForComponent((Component) e.getSource()).dispose();
        }
      });
      MessageDialog.showDialog(MsgComposePanel.this, panel, title, NotificationCenter.QUESTION_MESSAGE, buttons, 0, 0, null, true, true, true, true, null);
      if (trace != null) trace.exit(getClass(), okReturnBuffer[0]);
      return okReturnBuffer[0];
    }
    private void showRegularEmailWarningBeforeAndSend(final Record[] emlAddrs) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "showRegularEmailWarningBeforeAndSend(Record[] emlAddrs)");
      if (trace != null) trace.args(emlAddrs);
      String warnMsg = com.CH_cl.lang.Lang.rb.getString("msg_Delivery_to_regular_email_recipients_will_be_send_through_unencrypted_mail...");
      String title = com.CH_cl.lang.Lang.rb.getString("msgTitle_Regular_email_warning.");
      HTML_ClickablePane warnPane = new HTML_ClickablePane(warnMsg);
      JScrollPane warnScrollPane = new JScrollPane(warnPane);
      warnPane.setCaretPosition(0);
      JButton[] buttons = new JButton[3];
      buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Always_Proceed"));
      buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Proceed"));
      buttons[2] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
      final JDialog warningDialog = MessageDialog.showDialog(MsgComposePanel.this, warnScrollPane, title, NotificationCenter.WARNING_MESSAGE, buttons, 1, 2, null, false, true, true, true, null);
      warningDialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          setSendMessageInProgress(false);
        }
      });
      buttons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          warningDialog.dispose();
          // Change user preference about warning dialog
          Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
          request.userRecord = (UserRecord) cache.getUserRecord().clone();
          request.userRecord.notifyByEmail = new Short((short) Misc.setBit(false, request.userRecord.notifyByEmail, UserRecord.EMAIL_WARN_EXTERNAL));
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
          // send message triggered
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          new SendMessageRunner(SIL, MsgComposePanel.this).start();
        }
      });
      buttons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          warningDialog.dispose();
          // send message triggered
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          new SendMessageRunner(SIL, MsgComposePanel.this).start();
        }
      });
      buttons[2].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          warningDialog.dispose();
          setSendMessageInProgress(false);
        }
      });
      if (trace != null) trace.exit(getClass());
    }
  } // end class SendAction

  /**
  * Save composed message as Draft
  */
  private class SaveAsDraftAction extends AbstractActionTraced {
    public SaveAsDraftAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Save_as_Draft"), Images.get(ImageNums.SAVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Save_composition_in_the_Drafts_folder_for_future_editing."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.SAVE24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Save_Draft"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      actionPerformed();
    }
    public void actionPerformed() {
      isSavingAsDraft = true;
      ((SendAction) actions[SEND_ACTION]).actionPerformed();
    }
  }

  /**
  * Select recipients
  */
  private class SelectRecipientsAction extends AbstractActionTraced {
    public SelectRecipientsAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Select_Recipients"), Images.get(ImageNums.ADDRESS_BOOK16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Select_Recipients,_this_can_be_users,_folders_or_posting_boards."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_BOOK24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Address_Book"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      selectRecipientsPressed(TO);
    }
  }


  /**
  * Select attachments
  */
  private class SelectAttachmentsAction extends AbstractActionTraced {
    public SelectAttachmentsAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Select_Attachments"), Images.get(ImageNums.ATTACH_SMALL));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Select_Attachments,_this_could_be_a_file_or_a_message."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.ATTACH24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Attach"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      selectAttachmentsPressed();
    }
  }


  /**
  * Cut
  */
  private class CutAction extends DefaultEditorKit.CutAction {
    public CutAction(int actionId) {
      putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Cut"));
      putValue(Actions.MENU_ICON, Images.get(ImageNums.CUT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Cut_selected_text."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CUT24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Cut"));
    }
    public void actionPerformed(ActionEvent event) {
      try {
        super.actionPerformed(event);
      } catch (Throwable t) {
        // content issue - silent ignore
      }
      setEnabledActions();
    }
  }


  /**
  * Copy
  */
  private class CopyAction extends DefaultEditorKit.CopyAction {
    public CopyAction(int actionId) {
      putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Copy"));
      putValue(Actions.MENU_ICON, Images.get(ImageNums.COPY16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Copy_selected_text."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.COPY24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Copy"));
    }
    public void actionPerformed(ActionEvent event) {
      try {
        super.actionPerformed(event);
      } catch (Throwable t) {
        // content issue - silent ignore
      }
      setEnabledActions();
    }
  }


  /**
  * Paste
  */
  private class PasteAction extends DefaultEditorKit.PasteAction {
    public PasteAction(int actionId) {
      putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Paste"));
      putValue(Actions.MENU_ICON, Images.get(ImageNums.PASTE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("action_Paste"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.PASTE24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("action_Paste"));
    }
    public void actionPerformed(ActionEvent event) {
      try {
        super.actionPerformed(event);
      } catch (Throwable t) {
        // content issue - silent ignore
      }
      setEnabledActions();
    }
  }


  private class PriorityAction extends AbstractActionTraced {
    private short priorityIndex;
    public PriorityAction(short code, int actionId, ButtonGroup group) {
      super();
      switch (code) {
        case MsgDataRecord.IMPORTANCE_FYI_HTML:
          priorityIndex = PRIORITY_INDEX_FYI;
          putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("priority_FYI"));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.PRIORITY_LOW16));
          putValue(Actions.SELECTED_RADIO, Boolean.FALSE);
          break;
        case MsgDataRecord.IMPORTANCE_NORMAL_HTML:
          priorityIndex = PRIORITY_INDEX_NORMAL;
          putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("priority_Normal"));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.TRANSPARENT16));
          putValue(Actions.SELECTED_RADIO, Boolean.TRUE);
          break;
        case MsgDataRecord.IMPORTANCE_HIGH_HTML:
          priorityIndex = PRIORITY_INDEX_HIGH;
          putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("priority_High"));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.PRIORITY_HIGH16));
          putValue(Actions.SELECTED_RADIO, Boolean.FALSE);
          break;
      }
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Sets_the_priority."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.BUTTON_GROUP, group);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      msgComponents.setSelectedPriorityIndex(priorityIndex);
      setPriorityPanel();
    }
  }


  /**
  * Undo
  */
  private class UndoAction extends AbstractActionTraced {
    public UndoAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Undo"), Images.get(ImageNums.UNDO16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Undo_the_last_document_change."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.UNDO24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Undo"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      // We did experiance some NullPointerException being thrown here for no reason,
      // just catch it and ignore.
      try {
        if (undoMngr.canUndo()) {
          undoMngr.undo();
        }
      } catch (Throwable t) {
      }
      setEnabledSend();
      setEnabledUndoAndRedo();
    }
    protected void updateUndoState() {
      if (undoMngr.canUndo() && isInputActive()) {
        setEnabled(true);
        putValue(Actions.NAME, undoMngr.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Undo"));
      }
    }
  }

  /**
  * Redo
  */
  private class RedoAction extends AbstractActionTraced {
    public RedoAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Redo"), Images.get(ImageNums.REDO16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Redo_the_last_document_change."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REDO24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Redo"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      // We did experiance some NullPointerException being thrown here for no reason,
      // just catch it and ignore.
      try {
        if (undoMngr.canRedo()) {
          undoMngr.redo();
        }
      } catch (Throwable t) {
      }
      setEnabledSend();
      setEnabledUndoAndRedo();
    }
    protected void updateRedoState() {
      if (undoMngr.canRedo() && isInputActive()) {
        setEnabled(true);
        putValue(Actions.NAME, undoMngr.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Redo"));
      }
    }
  }

  /**
  * Show All Headers
  */
  private class ShowAllHeaders extends AbstractActionTraced {
    private String propertyName = null;
    public ShowAllHeaders(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Show_Advanced"));
//      super(com.CH_gui.lang.Lang.rb.getString("action_Show_BCC"));
//      if (objType == MsgDataRecord.OBJ_TYPE_ADDR)
//        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Show_CC_and_BCC"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Show_All_Headers"));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      // Initialize the state of showing headers
      propertyName = MiscGui.getVisualsKeyName("MsgComposePanel", null, PROPERTY_NAME__SHOW_ALL_HEADERS + "_" + objType);
      Boolean showAllHeaders = Boolean.valueOf(GlobalProperties.getProperty(propertyName, "false"));
      putValue(Actions.STATE_CHECK, showAllHeaders);
      setVisibleAllHeaders(showAllHeaders.booleanValue());
    }
    public void actionPerformedTraced(ActionEvent event) {
      boolean newValue = ((Boolean) getValue(Actions.STATE_CHECK)).booleanValue();
      setVisibleAllHeaders(newValue);
      GlobalProperties.setProperty(propertyName, "" + newValue);
    }
  }

  /**
  * Spell Check
  */
  private class SpellCheckAction extends AbstractActionTraced {
    public SpellCheckAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Spelling_..."), Images.get(ImageNums.SPELL16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      //putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Redo_the_last_document_change."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.SPELL24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Spelling"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
      if (w instanceof Frame) {
        Frame parent = (Frame) w;
        try {
          if (SpellCheckerWrapper.countLanguageLexicons() > 0) {
            JTextComponent textComp = msgComponents.getMsgTypeArea();
            JDialog dialog = SpellCheckerWrapper.buildCheckDialog(parent, textComp);
            MiscGui.setSuggestedWindowLocation(parent, dialog);
            dialog.setVisible(true);
            EventListener[] listeners = textComp.getListeners(CaretListener.class);
            for (int i=0; listeners!=null && i<listeners.length; i++)
              if (listeners[i] instanceof SpellCheckerI)
                ((SpellCheckerI) listeners[i]).recheckAll();
          } else {
            MessageDialog.showErrorDialog(parent, "No dictionary found!", "No dictionary", false);
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  /**
  * Spell Check Edit User Dictionary
  */
  private class SpellCheckEditDictionaryAction extends AbstractActionTraced {
    public SpellCheckEditDictionaryAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Edit_user_dictionary_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.REMOVABLE_MENU, Boolean.FALSE);
      putValue(Actions.DISABABLE, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
      if (w == null)
        w = GeneralDialog.getDefaultParent();
      if (w instanceof Frame) {
        Frame parent = (Frame) w;
        try {
          if (SpellCheckerWrapper.countUserLexicons() > 0) {
            JDialog dialog = SpellCheckerWrapper.buildUserDialog(parent); 
            MiscGui.setSuggestedWindowLocation(parent, dialog);
            dialog.setVisible(true);
          } else {
            MessageDialog.showErrorDialog(parent, "No user dictionary found!", "No dictionary", false);
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  /**
  * Spell Check Options
  */
  private class SpellCheckOptionsAction extends AbstractActionTraced {
    public SpellCheckOptionsAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Spelling_preferences_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.REMOVABLE_MENU, Boolean.FALSE);
      putValue(Actions.DISABABLE, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
      if (w == null)
        w = GeneralDialog.getDefaultParent();
      if (w instanceof Frame) {
        Frame parent = (Frame) w;
        try {
          SpellCheckerWrapper.buildOptionsDialog(parent);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  /**
  * Voice Recording panel
  */
  private class RecordPanelAction extends AbstractActionTraced {
    public RecordPanelAction(int actionId) {
      super("Voice Recording Panel", Images.get(ImageNums.SOUND_RECORD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Show Voice Recording Panel");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.SOUND_RECORD24));
      putValue(Actions.TOOL_NAME, "Record");
    }
    public void actionPerformedTraced(ActionEvent event) {
      msgComponents.toggleVisibilityOfRecordingPanel();
    }
  }

  /**
  * Ring panel
  */
  private class RingBellAction extends AbstractActionTraced {
    public RingBellAction(int actionId) {
      super("Ring the bell", Images.get(ImageNums.RING_BELL));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Ring_the_bell"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      ringPressed();
    }
  }

  /** Show or hide advanced headers */
  public void setVisibleAllHeaders(boolean visible) {
    if (msgComponents != null) {
      msgComponents.setVisibleAllHeaders(visible);
    }
  }

  /** Called when value is selected in the combo box. */
  public void priorityPressed() {
    int priorityIndex = msgComponents.getPriorityIndex();
    actions[PRIORITY_ACTION + priorityIndex].putValue(Actions.SELECTED_RADIO, Boolean.TRUE);
  }


  /**
  * Check if all recipients are valid and display a message for the ones not valid.
  * @return the valid recipients
  */
  public void checkValidityOfRecipients() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "checkValidityOfRecipients()");
    checkValidityOfRecipients(true);
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  private void checkValidityOfRecipients(boolean withRedraw) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "checkValidityOfRecipients(boolean withRedraw)");
    if (trace != null) trace.args(withRedraw);
    StringBuffer errorSB = new StringBuffer();
    selectedRecipients = CacheUsrUtils.checkValidityOfRecipients(cache, selectedRecipients, errorSB);
    if (errorSB.length() > 0) {
      String title = com.CH_cl.lang.Lang.rb.getString("msgTitle_Invalid_recipient");
      MessageDialog.showDialog(MsgComposePanel.this, errorSB.toString(), title, NotificationCenter.WARNING_MESSAGE, false);
    }
    if (withRedraw) {
      redrawRecipientsAll();
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  public void selectRecipientsPressed(int recipientType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "selectRecipientsPressed(int recipientType)");
    if (trace != null) trace.args(recipientType);
    selectRecipients(recipientType, false);
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  private boolean selectRecipients(final int recipientType, boolean waitForResults) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "selectRecipients(int recipientType, boolean waitForResults)");
    if (trace != null) trace.args(recipientType);
    if (trace != null) trace.args(waitForResults);
    String titlePostfix = com.CH_cl.lang.Lang.rb.getString(recipientType==TO ? "button_To" : ( recipientType==CC ? "button_Cc" : "button_Bcc"));
    RecipientsDialog d = null;
    boolean isOKed = false;
    boolean rc = false;
    if (selectedRecipients[recipientType] != null && selectedRecipients[recipientType].length > 0) {
      if (trace != null) trace.data(10, "initialize from array");
      d = new RecipientsDialog((JFrame) SwingUtilities.windowForComponent(this), titlePostfix, selectedRecipients[recipientType], null, null, waitForResults);
    } else {
      if (trace != null) trace.data(20, "initialize NOT from array");
      if (msgComponents.isRecipientTypeSupported(recipientType)) {
        if (trace != null) trace.data(30, "recipientType supported", recipientType);
        // if any emails or names written then tokenize them and use them as initial selections
        String recipientsInput = msgComponents.getRecipientsInput(recipientType);
        // if skipping GUI, only call for resolution using RecipientsDialog if there is any text input present
        if (!waitForResults || (recipientsInput != null && recipientsInput.length() > 0)) {
          if (trace != null) trace.data(40, "into RecipientsDialog");
          d = new RecipientsDialog((JFrame) SwingUtilities.windowForComponent(this), titlePostfix, selectedRecipients[recipientType], recipientsInput, null, waitForResults);
        }
      }
    }
    final Runnable resultConditioning = new Runnable() {
      public void run() {
        if (selectedRecipients[recipientType] == null) selectedRecipients[recipientType] = new Record[0];
        checkValidityOfRecipients(false);
        redrawRecipients(recipientType);
        // See if any entered email addresses could become Contact Records
        convertSelectedRecipientsToFamiliar_Threaded(false);
      }
    };
    if (waitForResults) {
      if (d != null) {
        if (trace != null) trace.data(50, "RecipientsDialog initialized");
        isOKed = d.isOKed();
        Record[] recipients = d.getRecipients();
        recipients = (Record[]) ArrayUtils.removeDuplicates(recipients);
        selectedRecipients[recipientType] = recipients;
      }
      resultConditioning.run();
      rc = isOKed || d == null;
    } else {
      d.registerForUpdates(new ListUpdatableI() {
        public void update(Object[] objects) {
          Record[] recipients = (Record[]) objects;
          recipients = (Record[]) ArrayUtils.removeDuplicates(recipients);
          selectedRecipients[recipientType] = recipients;
          resultConditioning.run();
        }
      });
    }
    if (trace != null) trace.exit(MsgComposePanel.class, rc);
    return rc;
  }

  public void setRecipients(Record[] newRecipients) {
    selectedRecipients[TO] = newRecipients;
    selectedRecipients[CC] = null;
    selectedRecipients[BCC] = null;
    redrawRecipientsAll();
  }

  private void redrawRecipientsAll() {
    redrawRecipients(TO);
    redrawRecipients(CC);
    redrawRecipients(BCC);
  }
  private void redrawRecipients(final int recipientType) {
    // Since there is a problem when a Message Compose frame shows up with this panel,
    // do this at the end of all AWT events after the frame is already shown.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "redrawRecipients.run()");
        if (msgComponents.isRecipientTypeSupported(recipientType)) {
          msgComponents.redrawRecipients(recipientType, selectedRecipients[recipientType]);
          setEnabledActions();
          revalidate();
        }
        // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
        if (trace != null) trace.exit(getClass());
      }
    });
  }

  public void addAttachment(File fileAttachment) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "addAttachment(File fileAttachment)");
    if (trace != null) trace.args(fileAttachment);

    ArrayList attachmentsL = new ArrayList();
    if (selectedAttachments != null) {
      for (int i=0; i<selectedAttachments.length; i++) {
        attachmentsL.add(selectedAttachments[i]);
      }
    }
    if (!attachmentsL.contains(fileAttachment))
      attachmentsL.add(fileAttachment);
    selectedAttachments = ArrayUtils.toArray(attachmentsL, Object.class);
    setEnabledActions();
    setAttachmentsPanel();

    if (trace != null) trace.exit(MsgComposePanel.class);
  }

  public void selectAttachmentsPressed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "selectAttachmentsPressed()");

    Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
    if (w instanceof Frame || w instanceof Dialog) {
      RecordChooserDialog d = null;
      short[] folderTypes = new short[] { FolderRecord.CATEGORY_MAIL_FOLDER, FolderRecord.CATEGORY_FILE_FOLDER, FolderRecord.CATEGORY_CHAT_FOLDER, FolderRecord.LOCAL_FILES_FOLDER, FolderRecord.FILE_FOLDER, FolderRecord.MESSAGE_FOLDER, FolderRecord.POSTING_FOLDER, FolderRecord.CHATTING_FOLDER, FolderRecord.ADDRESS_FOLDER };
      String title = com.CH_cl.lang.Lang.rb.getString("title_File_and_Message_Attachment_Chooser");
      String mainLabel = com.CH_cl.lang.Lang.rb.getString("label_Select_objects_from_table");
      if (w instanceof Frame)
        d = new RecordChooserDialog((Frame) w, title, mainLabel, folderTypes, selectedAttachments);
      else
        d = new RecordChooserDialog((Dialog) w, title, mainLabel, folderTypes, selectedAttachments);
      d.registerForUpdates(new ListUpdatableI() {
        public void update(Object[] objs) {
          selectedAttachments = objs;
          setEnabledActions();
          setAttachmentsPanel();
        }
      });
    }

    if (trace != null) trace.data(50, selectedAttachments);
    if (trace != null) trace.exit(MsgComposePanel.class);
  }


  private boolean isInputActive() {
    return !sendMessageInProgress && SwingUtilities.windowForComponent(this) != null;
  }


  private void checkForUnknownRecipientsForAddressBookAdition_Threaded(Record[][] preConversionSelectedRecipients, Record[][] postConversionSelectedRecipients) {
    ArrayList emailStringRecordsL = null;
    ArrayList userRecordsL = null;
    Record[][] recipients = null;
    for (int a=0; a<2; a++) {
      if (a == 0)
        recipients = preConversionSelectedRecipients;
      else
        recipients = postConversionSelectedRecipients;
      if (recipients != null) {
        for (int i=TO; i<RECIPIENT_TYPES.length; i++) {
          if (recipients[i] != null) {
            for (int k=0; k<recipients[i].length; k++) {
              Record rec = recipients[i][k];
              if (rec instanceof EmailAddressRecord || rec instanceof InvEmlRecord) {
                String addr = null;
                if (rec instanceof EmailAddressRecord)
                  addr = ((EmailAddressRecord) rec).address.trim();
                else if (rec instanceof InvEmlRecord)
                  addr = ((InvEmlRecord) rec).emailAddr.trim();
                if (emailStringRecordsL == null) emailStringRecordsL = new ArrayList();
                if (!emailStringRecordsL.contains(addr))
                  emailStringRecordsL.add(addr);
              } else if (rec instanceof UserRecord) {
                UserRecord uRec = (UserRecord) rec;
                if (userRecordsL == null) userRecordsL = new ArrayList();
                if (!userRecordsL.contains(uRec))
                  userRecordsL.add(uRec);
              }
            }
          }
        }
      }
    }
    if (emailStringRecordsL != null && emailStringRecordsL.size() > 0)
      checkEmailAddressesForAddressBookAdition_Threaded(null, null, emailStringRecordsL, false, new FolderFilter(FolderRecord.ADDRESS_FOLDER));
    if (userRecordsL != null && userRecordsL.size() > 0)
      checkUserRecordsForContactListAdition_Threaded(null, userRecordsL);
  }

  public static void checkEmailAddressesForAddressBookAdition_Threaded(final Component parent, final ArrayList emailNicksL, final ArrayList emailStringRecordsL, final boolean displayNoNewAddressesDialog, final RecordFilter folderFilter) {
    checkEmailAddressesForAddressBookAdition_Threaded(parent, emailNicksL, emailStringRecordsL, displayNoNewAddressesDialog, folderFilter, false, null, false);
  }
  public static void checkEmailAddressesForAddressBookAdition_Threaded(final Component parent, final ArrayList emailNicksL, final ArrayList emailStringRecordsL, final boolean displayNoNewAddressesDialog, final RecordFilter folderFilter, final boolean forceAddAtOnce, final FolderPair toAddressBook, final boolean skipProgressBar) {
    Thread th = new ThreadTraced("Address Book Email Checker") {
      public void runTraced() {
        checkEmailAddressesForAddressBookAdition(parent, emailNicksL, emailStringRecordsL, displayNoNewAddressesDialog, false, folderFilter, forceAddAtOnce, toAddressBook, skipProgressBar);
      }
    };
    th.setDaemon(true);
    th.start();
  }

  /**
  * @return Vector of emailAddresses Strings which do not exist in the Address Books
  * xxx To-Do: enforce check in specified folder filter type
  */
  public static ArrayList checkEmailAddressesForAddressBookAdition(Component parent, ArrayList emailNicksL, ArrayList emailStringRecordsL, boolean displayNoNewAddressesDialog, boolean performCheckOnly, RecordFilter folderFilter) {
    return checkEmailAddressesForAddressBookAdition(parent, emailNicksL, emailStringRecordsL, displayNoNewAddressesDialog, performCheckOnly, folderFilter, false, null, false);
  }
  public static ArrayList checkEmailAddressesForAddressBookAdition(final Component parent, ArrayList emailNicksL, ArrayList emailStringRecordsL, boolean displayNoNewAddressesDialog, boolean performCheckOnly, RecordFilter folderFilter, boolean forceAddAtOnce, final FolderPair toAddressBook, final boolean skipProgressBar) {

    final ArrayList emailRecordsShortL = new ArrayList();
    final ArrayList emailRecordsLowerL = new ArrayList();
    final ArrayList emailRecordsOrigL = new ArrayList();
    final ArrayList emailNicksOrigL = emailNicksL != null ? new ArrayList() : null;

    if (emailStringRecordsL.size() > 0) {
      for (int i=0; i<emailStringRecordsL.size(); i++) {
        String addrOrig = (String) emailStringRecordsL.get(i);
        String[] addrs = EmailRecord.gatherAddresses(addrOrig);
        if (addrs != null && addrs.length > 0) {
          String addrShort = addrs[addrs.length-1].trim();
          String addrLower = addrShort.toLowerCase(Locale.US);
          if (!emailRecordsLowerL.contains(addrLower)) {
            if (emailNicksL != null) emailNicksOrigL.add(emailNicksL.get(i));
            emailRecordsShortL.add(addrShort);
            emailRecordsLowerL.add(addrLower);
            emailRecordsOrigL.add(addrOrig);
          }
        }
      }
    }

    // check for hash of email address in cache between AddrHashRecords...
    if (emailRecordsLowerL.size() > 0) {
      eliminateCachedAddresses(folderFilter, emailNicksOrigL, emailRecordsShortL, emailRecordsLowerL, emailRecordsOrigL);
    }

    // check for emails address itself in cache between Address Records...
//    if (emailRecordsLowerV.size() > 0) {
//      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
//      FolderPair[] addrBooks = cache.getFolderPairsMyOfType(FolderRecord.ADDRESS_FOLDER, true);
//      if (addrBooks != null && addrBooks.length > 0) {
//        // look for addresses in those address books
//        Long[] folderIDs = RecordUtils.getIDs(addrBooks);
//        MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsOwnersAndType(folderIDs, new Short(Record.RECORD_TYPE_FOLDER));
//        if (msgLinks != null && msgLinks.length > 0) {
//          // gather Address Objects for the links
//          Long[] msgIDs = MsgLinkRecord.getMsgIDs(msgLinks);
//          MsgDataRecord[] msgDatas = cache.getMsgDataRecords(msgIDs);
//          // filter only Address Objects from those links
//          msgDatas = (MsgDataRecord[]) RecordUtils.filter(msgDatas, new MsgFilter(MsgDataRecord.OBJ_TYPE_ADDR));
//          if (msgDatas != null && msgDatas.length > 0) {
//            // for each Address in out Address Books, remove fould email addresses from 'unknown' emailRecords Vector
//            for (int x=0; x<msgDatas.length; x++) {
//              MsgDataRecord dataRecord = msgDatas[x];
//              String subject = dataRecord.getSubject();
//              int subjEmlStart = -1, subjEmlEnd = -1;
//              if (subject != null) {
//                subjEmlStart = subject.indexOf("<E-mail>");
//                subjEmlEnd = subject.lastIndexOf("</E-mail>");
//              }
//              String text = dataRecord.getText();
//              int emlStart = -1, emlEnd = -1;
//              if (text != null) {
//                emlStart = text.indexOf("<Emails"); // no closing bracket due to following attributes
//                emlEnd = text.lastIndexOf("</Emails>");
//              }
//              String emailsText = "";
//              if (subjEmlStart >= 0 && subjEmlEnd >= 0 && subjEmlEnd > subjEmlStart) {
//                emailsText += " " + subject.substring(subjEmlStart, subjEmlEnd);
//              }
//              if (emlStart >= 0 && emlEnd >= 0 && emlEnd > emlStart) {
//                emailsText += " " + text.substring(emlStart, emlEnd);
//              }
//              if (emailsText.length() > 0) {
//                String[] addresses = EmailRecord.gatherAddresses(emailsText);
//                if (addresses != null) {
//                  for (int i=0; i<addresses.length; i++) {
//                    String addr = addresses[i].trim().toLowerCase(Locale.US);
//                    for (int k=0; k<emailRecordsLowerV.size(); k++) {
//                      if (((String) emailRecordsLowerV.elementAt(k)).equals(addr)) {
//                        if (emailNicksOrigV != null) emailNicksOrigV.removeElementAt(k);
//                        emailRecordsLowerV.removeElementAt(k);
//                        emailRecordsOrigV.removeElementAt(k);
//                        break;
//                      }
//                    } // end for k
//                  } // end for i
//                }
//              }
//            } // end for x
//          }
//        }
//      }
//    } // end check for emails in cache

    // check for hashes on the server... only if it is not in the cache
    if (emailRecordsLowerL.size() > 0) {
      try {
        ArrayList hashesL = null;
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        for (int i=0; i<emailRecordsLowerL.size(); i++) {
          byte[] hash = FetchedDataCache.getAddrHashForEmail((String) emailRecordsLowerL.get(i));
          if (hash != null) {
            // If hash not already in the cache, add it to be fetched unless it was already requested.
            // Allways fetch if NOT CHECK ONLY, this will ensure folderFilter is effective.
            if (!performCheckOnly || (cache.getAddrHashRecords(hash) == null && !cache.wasRequestedAddrHash(hash))) {
              if (hashesL == null) hashesL = new ArrayList();
              hashesL.add(hash);
            }
          }
        }
        if (hashesL != null) {
          Obj_List_Co requestSet = new Obj_List_Co(hashesL);
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.ADDR_Q_FIND_HASH, requestSet), 30000, 3);
          cache.addRequestedAddrHashes(hashesL);
          DefaultReplyRunner.nonThreadedRun(SIL, reply);
          if (reply != null) {
            // look again in the cache for known hashes
            eliminateCachedAddresses(folderFilter, emailNicksOrigL, emailRecordsShortL, emailRecordsLowerL, emailRecordsOrigL);
          }
        }
      } catch (Throwable t) {
      }
    }

    if (!performCheckOnly) {

      final Component parentComp = parent != null ? parent : GeneralDialog.getDefaultParent();

      // ask in dialog if emails not found should be added to Address Book
      if (emailRecordsOrigL.isEmpty() && displayNoNewAddressesDialog) {
        String messageText = "The specified email address(es) already exist in your Address Book.";
        String title = "Address already in Address Book";
        MessageDialog.showInfoDialog(parentComp, messageText, title, false);
      } else if (emailRecordsOrigL.size() > 0) {
        StringBuffer sb = new StringBuffer();
        sb.append("The following email addresses are not listed in your Address Book.  Would you like to add them now?\n\n");
        for (int i=0; i<emailRecordsOrigL.size(); i++) {
          sb.append(emailRecordsOrigL.get(i));
          if (i<emailRecordsOrigL.size()-1)
            sb.append("\n");
        }

        if (forceAddAtOnce) {
          new AddAtOnceThread("Add Address Runner", parentComp, toAddressBook, emailRecordsLowerL, emailRecordsShortL, emailRecordsOrigL, emailNicksOrigL, skipProgressBar).start();
        } else {
          JButton[] buttons = new JButton[3];
          buttons[0] = new JButton("Add at Once");
          buttons[1] = new JButton("Add and Edit");
          buttons[2] = new JButton("No");
          final JDialog dialog = MessageDialog.showDialog(parentComp, sb.toString(), "New Addresses",  NotificationCenter.QUESTION_MESSAGE, buttons, false);
          buttons[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              dialog.dispose();
              new AddAtOnceThread("Add Address Runner", parentComp, toAddressBook, emailRecordsLowerL, emailRecordsShortL, emailRecordsOrigL, emailNicksOrigL, skipProgressBar).start();
            }
          });
          buttons[1].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              dialog.dispose();
              FolderPair addrBook = toAddressBook;
              if (addrBook == null) addrBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
              if (addrBook != null) {
                FolderPair[] fPairs = new FolderPair[] { addrBook };
                for (int i=0; i<emailRecordsLowerL.size(); i++) {
                  String emailShort = (String) emailRecordsShortL.get(i);
                  //String emailLower = (String) emailRecordsLowerV.elementAt(i);
                  String emailOrig = (String) emailRecordsOrigL.get(i);
                  String nick = (emailNicksOrigL != null) ? (String) emailNicksOrigL.get(i) : EmailRecord.getPersonalOrNick(emailOrig);
                  XMLElement draftData = ContactInfoPanel.getContent(new XMLElement[] {
                                              NamePanel.getContent(nick, null, null, null),
                                              EmailPanel.getContent(EmailPanel.getTypes(), new String[] { emailShort }, null, 0) });
                  new AddressFrame(emailShort, fPairs, draftData);
                }
              }
            }
          });
          buttons[2].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              dialog.dispose();
            }
          });
        }
      }
    }

    return emailRecordsOrigL;
  } // end checkForUnknownRecipientsForAddressBookAdition

  private static class AddAtOnceThread extends ThreadTraced {

    private Component parentComp;
    private FolderPair toAddressBook;
    private ArrayList emailRecordsLowerL, emailRecordsShortL, emailRecordsOrigL, emailNicksOrigL;
    private boolean skipProgressBar;

    public AddAtOnceThread(String name, Component parentComp, FolderPair toAddressBook, ArrayList emailRecordsLowerL, ArrayList emailRecordsShortL, ArrayList emailRecordsOrigL, ArrayList emailNicksOrigL, boolean skipProgressBar) {
      super(name);
      this.parentComp = parentComp;
      this.toAddressBook = toAddressBook;
      this.emailRecordsLowerL = emailRecordsLowerL;
      this.emailRecordsShortL = emailRecordsShortL;
      this.emailRecordsOrigL = emailRecordsOrigL;
      this.emailNicksOrigL = emailNicksOrigL;
      this.skipProgressBar = skipProgressBar;
    }
    public void runTraced() {
      FolderPair addrBook = toAddressBook;
      if (addrBook == null) addrBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
      Record[] recipients = new Record[] { addrBook };
      if (addrBook != null) {
        int countProcessed = 0;
        final boolean[] interrupted = new boolean[] { false };
        ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
        JProgressBar progressBar = null;
        GeneralDialog progressDialog = null;
        if (!skipProgressBar) {
          progressBar = new JProgressBar(0, emailRecordsLowerL.size());
          JPanel progressPanel = new JPanel();
          progressPanel.setLayout(new GridBagLayout());
          progressPanel.add(new JMyLabel("Addresses are being imported into your Address Book, please wait..."), new GridBagConstraints(0, 0, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
          progressPanel.add(progressBar, new GridBagConstraints(0, 1, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
          JButton[] buttons = new JButton[1];
          buttons[0] = new JButton("Cancel");
          buttons[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              interrupted[0] = true;
            }
          });
          //JDialog infoDialog = MessageDialog.showDialog(parentComp, progressPanel, "Import in Progress...", MessageDialog.INFORMATION_MESSAGE, buttons, null, false);
          Window w = SwingUtilities.windowForComponent(parentComp);
          if (parentComp instanceof Frame)
            progressDialog = new GeneralDialog((Frame) w, "Import in Progress...", buttons, -1, 0, progressPanel);
          else if (parentComp instanceof Dialog)
            progressDialog = new GeneralDialog((Dialog) w, "Import in Progress...", buttons, -1, 0, progressPanel);
          else
            progressDialog = new GeneralDialog("Import in Progress...", buttons, -1, 0, progressPanel);
        }

        for (int i=0; i<emailRecordsLowerL.size(); i++) {
          String emailShort = (String) emailRecordsShortL.get(i);
          //String emailLower = (String) emailRecordsLowerV.elementAt(i);
          String emailOrig = (String) emailRecordsOrigL.get(i);
          String nick = (emailNicksOrigL != null) ? (String) emailNicksOrigL.get(i) : EmailRecord.getPersonalOrNick(emailOrig);
          XMLElement addressFull = ContactInfoPanel.getContent(new XMLElement[] {
                                          NamePanel.getContent(nick, null, null, null),
                                          EmailPanel.getContent(EmailPanel.getTypes(), new String[] { emailShort }, null, 0) });
          XMLElement addressPreview = ContactInfoPanel.getContentPreview(nick, null, new String[] { emailShort }, null, 0, null, null);

          countProcessed ++;

          BASymmetricKey key = new BASymmetricKey(32);
          MsgLinkRecord[] links = SendMessageRunner.prepareMsgLinkRecords(SIL, recipients, key);
          MsgDataRecord data = SendMessageRunner.prepareMsgDataRecord(SIL.getFetchedDataCache(), key, new Short(MsgDataRecord.IMPORTANCE_NORMAL_PLAIN), new Short(MsgDataRecord.OBJ_TYPE_ADDR), addressPreview.toString(), addressFull.toString(), null);
          Msg_New_Rq request = new Msg_New_Rq(addrBook.getFolderShareRecord().shareId, null, links[0], data);
          request.hashes = SendMessageRunner.prepareAddrHashes(data);
          MessageAction action = new MessageAction(CommandCodes.MSG_Q_NEW, request);
          // escape loop if CANCEL pressed
          if (interrupted[0]) break;
          // synchronize every 5 addresses
          if (countProcessed % 5 == 0)
            SIL.submitAndWait(action, 60000, 3);
          else
            SIL.submitAndReturn(action);
          if (progressBar != null)
            progressBar.setValue(countProcessed);
        }
        if (progressDialog != null) {
          progressDialog.dispose();
        }
      }
    }
  }


  public static void eliminateCachedAddresses(RecordFilter folderFilter, ArrayList emailNicksOrigL, ArrayList emailRecordsShortL, ArrayList emailRecordsLowerL, ArrayList emailRecordsOrigL) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long[] filteredMsgIDs = null;
    { // setup filter folder filter
      if (folderFilter != null) {
        FolderRecord[] filteredFolders = cache.getFolderRecords(folderFilter);
        Long[] filteredFolderIDs = RecordUtils.getIDs(filteredFolders);
        MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsForFolders(filteredFolderIDs);
        filteredMsgIDs = MsgLinkRecord.getMsgIDs(msgLinks);
      }
    }
    for (int i=emailRecordsLowerL.size()-1; i>=0; i--) { // back to front to ommit problems with shrinking vectors
      boolean found = false;
      String eml = (String) emailRecordsLowerL.get(i);
      AddrHashRecord[] addrHashRecs = cache.getAddrHashRecords(eml);
      if (filteredMsgIDs == null) {
        found = addrHashRecs != null;
      }
      // else check if any of the found records are in the filtered set...
      else if (addrHashRecs != null) {
        Long[] msgIDs = AddrHashRecord.getMsgIDs(addrHashRecs);
        for (int k=0; k<msgIDs.length; k++) {
          if (ArrayUtils.find(filteredMsgIDs, msgIDs[k]) >= 0) {
            found = true;
            break;
          }
        }
      }
      if (found) {
        if (emailNicksOrigL != null) emailNicksOrigL.remove(i);
        emailRecordsShortL.remove(i);
        emailRecordsLowerL.remove(i);
        emailRecordsOrigL.remove(i);
      }
    }
  }

  public static void checkUserRecordsForContactListAdition_Threaded(final Component parent, final ArrayList userRecordsL) {
    Thread th = new ThreadTraced("Contact List User Checker") {
      public void runTraced() {
        checkUserRecordsForContactListAdition(parent, userRecordsL);
      }
    };
    th.setDaemon(true);
    th.start();
  }

  public static void checkUserRecordsForContactListAdition(Component parent, ArrayList userRecordsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "checkUserRecordsForContactListAdition(Component parent, ArrayList userRecordsL)");
    if (trace != null) trace.args(userRecordsL);

    final FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    final ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    Long[] userIDs = RecordUtils.getIDs(userRecordsL);
    ContactRecord[] cRecs = cache.getContactRecordsForUsers(userIDs);
    Long[] contactWithIDs = ContactRecord.getContactWithUserIDs(cRecs, true);
    final ArrayList usersToAddL = new ArrayList();
    for (int i=0; i<userRecordsL.size(); i++) {
      UserRecord uRec = (UserRecord) userRecordsL.get(i);
      if (ArrayUtils.find(contactWithIDs, uRec.userId) < 0) {
        usersToAddL.add(uRec);
      }
    }
    if (usersToAddL != null && usersToAddL.size() > 0) {
      StringBuffer sb = new StringBuffer();
      sb.append("The following users are not listed in your Contact List.  Would you like to add them now?\n\n");
      for (int i=0; i<usersToAddL.size(); i++) {
        UserRecord uRec = (UserRecord) usersToAddL.get(i);
        sb.append(uRec.handle);
        if (i<usersToAddL.size()-1)
          sb.append("\n");
      }

      JButton[] buttons = new JButton[3];
      buttons[0] = new JButton("Add at Once");
      buttons[1] = new JButton("Add and Edit");
      buttons[2] = new JButton("No");
      final Component parentComp = parent != null ? parent : GeneralDialog.getDefaultParent();
      final Window window = SwingUtilities.windowForComponent(parentComp);
      final JDialog dialog = MessageDialog.showDialog(parentComp, sb.toString(), "New Contacts",  NotificationCenter.QUESTION_MESSAGE, buttons, false);
      buttons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
          Thread th = new ThreadTraced("Contact Adding Thread") {
            public void runTraced() {
              for (int i=0; i<usersToAddL.size(); i++) {
                UserRecord uRec = (UserRecord) usersToAddL.get(i);
                Long contactWithId = uRec.userId;
                // get user's key
                KeyRecord otherKeyRec = cache.getKeyRecordForUser(contactWithId);
                if (otherKeyRec == null) {
                  Obj_IDList_Co request = new Obj_IDList_Co();
                  request.IDs = new Long[] { contactWithId };
                  MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
                  ClientMessageAction replyMsg = SIL.submitAndFetchReply(msgAction, 60000, 3);
                  if (replyMsg != null && replyMsg.getActionCode() == CommandCodes.KEY_A_GET_PUBLIC_KEYS) {
                    Key_PubKeys_Rp replyData = (Key_PubKeys_Rp) replyMsg.getMsgDataSet();
                    KeyRecord[] kRecs = replyData.keyRecords;
                    if (kRecs != null && kRecs.length == 1) {
                      otherKeyRec = kRecs[0];
                    }
                  }
                  DefaultReplyRunner.nonThreadedRun(SIL, replyMsg);
                }
                // if user's key is present, create the contact object
                if (otherKeyRec != null) {
                  Long shareId = cache.getFolderShareRecordMy(cache.getUserRecord().contactFolderId, false).shareId;
                  Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
                  request.shareId = shareId;
                  request.contactRecord = new ContactRecord();
                  request.contactRecord.contactWithId = contactWithId;
                  FetchedDataCache cache = FetchedDataCache.getSingleInstance();
                  BASymmetricKey folderSymKey = cache.getFolderShareRecord(shareId).getSymmetricKey();
                  request.contactRecord.setOwnerNote(uRec.handle.trim());
                  request.contactRecord.setOtherNote(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {cache.getUserRecord().handle}));
                  request.contactRecord.setOtherSymKey(new BASymmetricKey(32));
                  request.contactRecord.seal(folderSymKey, otherKeyRec);

                  SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request));
                }
              } // end for
            } // end run()
          };
          th.setDaemon(true);
          th.start();
        }
      });
      buttons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
          if (window instanceof Dialog)
            new InitiateContactDialog((Dialog) window, RecordUtils.getIDs(usersToAddL));
          else if (window instanceof Frame || window == null)
            new InitiateContactDialog((Frame) window, RecordUtils.getIDs(usersToAddL));
        }
      });
      buttons[2].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      });
    }

    if (trace != null) trace.exit(MsgComposePanel.class);
  } // end checkUserRecordsForContactListAdition

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I
  /****************************************************************************/

  /**
  * @return all the acitons that this objects produces.
  */
  public Action[] getActions() {
    return actions;
  }

  /**
  * Final Action Producers will not be traversed to collect its containing objects' actions.
  * @return true if this object will gather all actions from its childeren or hide them counciously.
  */
  public boolean isFinalActionProducer() {
    return true;
  }

  /**
  * Enables or Disables actions based on the current state of the Action Producing component.
  */
  public void setEnabledActions() {
    if (!isDisposed) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (!isDisposed) {
            boolean inputActive = isInputActive();

            setEnabledSend();

            if (!isChatComposePanel) {
              actions[SELECT_RECIPIENTS_ACTION].setEnabled(true && inputActive);
              actions[SHOW_ALL_HEADERS].setEnabled(true);
              actions[RECORD_PANEL_ACTION].setEnabled(true); // always active
            }
            actions[SELECT_ATTACHMENTS_ACTION].setEnabled(inputActive);
            actions[CUT_ACTION].setEnabled(inputActive);
            actions[COPY_ACTION].setEnabled(inputActive);
            actions[PASTE_ACTION].setEnabled(inputActive);
            actions[PRIORITY_ACTION].setEnabled(inputActive);
            actions[PRIORITY_ACTION+1].setEnabled(inputActive);
            actions[PRIORITY_ACTION+2].setEnabled(inputActive);
            actions[SPELL_CHECK_ACTION].setEnabled(inputActive);
            actions[SPELL_CHECK_EDIT_DICTIONARY_ACTION].setEnabled(true); // always active
            actions[SPELL_CHECK_OPTIONS_ACTION].setEnabled(true); // always active
            if (isChatComposePanel) {
              actions[RING_BELL_ACTION].setEnabled(true); // always active
            }

            setEnabledUndoAndRedo();

            if (msgComponents != null) {
              msgComponents.setEnabled(inputActive);
            }
          }
        }
      });
    }
  }


  private void setEnabledSend() {
    setEnabledSend(false);
  }
  private void setEnabledSend(boolean isContentInserted) {
    boolean inputActive = isInputActive();
    boolean enableSend = false;
    boolean enableDraft = false;

    // check if any recipients present
    boolean anyRecipients = false;
    for (int i=0; i<=BCC; i++) {
      if (selectedRecipients != null && selectedRecipients[i] != null && selectedRecipients[i].length > 0) {
        anyRecipients = true;
        break;
      }
    }
    if (!anyRecipients && msgComponents != null) {
      JTextField[] jRecipientsInputs = msgComponents.getRecipientsInputs();
      for (int i=0; i<jRecipientsInputs.length; i++) {
        if (jRecipientsInputs[i] != null && jRecipientsInputs[i].getText().trim().length() > 0) {
          anyRecipients = true;
          break;
        }
      }
    }

    if (anyRecipients) {
      enableSend = inputActive &&
        ( !isChatComposePanel ||
          (selectedAttachments != null && selectedAttachments.length > 0) ||
          (isContentInserted || msgComponents.isAnyContent())
        );
    }
    if (actions[SEND_ACTION].isEnabled() != enableSend) {
      actions[SEND_ACTION].setEnabled(enableSend);
    }
    if (msgComponents != null) {
      msgComponents.setEnabledSend(enableSend);
    }

    enableDraft = enableSend;
    if (!enableDraft && inputActive) {
      enableDraft = !isChatComposePanel && anyRecipients;
      if (!enableDraft) {
        enableDraft = isContentInserted || (selectedAttachments != null && selectedAttachments.length > 0);
        if (!enableDraft) {
          boolean anyContent = msgComponents != null ? msgComponents.isAnyContent() : false;
          enableDraft = anyContent;
        }
      }
    }
    if (actions[SAVE_AS_DRAFT_ACTION].isEnabled() != enableDraft) {
      actions[SAVE_AS_DRAFT_ACTION].setEnabled(enableDraft);
    }
  }


  private class TypingListener implements MsgTypingListener {
    public void msgTypingUpdate(EventObject event) {
      // Exec on event thread to avoid potential GUI deadlocks
      if (isChatComposePanel) {
        Object s = event.getSource();
        if (s instanceof Obj_List_Co) {
          Obj_List_Co o = (Obj_List_Co) s;
          Long userId = (Long) o.objs[0];
          Long folderId = (Long) o.objs[1];
          if (folderId != null) {
            javax.swing.SwingUtilities.invokeLater(new TypingGUIUpdater(userId, folderId));
          }
        }
      }
    }
  }


  private class TypingGUIUpdater implements Runnable {
    private long endTime;
    private Long userId;
    private Long folderId;
    public TypingGUIUpdater(Long userId, Long folderId) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TypingGUIUpdater.class, "TypingGUIUpdater(Long userId, Long folderId)");
      this.userId = userId;
      this.folderId = folderId;
      this.endTime = System.currentTimeMillis() + ChatActionTable.TYPING_NOTIFY_MILLIS;
      if (trace != null) trace.exit(TypingGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TypingGUIUpdater.class, "TypingGUIUpdater.run()");

      // use my contact list only, not the reciprocal contacts
      Record r = CacheUsrUtils.convertUserIdToFamiliarUser(cache, userId, true, false);
      final String name = ListRenderer.getRenderedText(r);

      Timer timer = new Timer(0, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JLabel jTyping = msgComponents.getTypingLabel();
          if (folderId != null && folderId.equals(getTypeNotifyFolderId()) && System.currentTimeMillis() < endTime) {
            jTyping.setText(name + " is typing a message...");
            if (!jTyping.isVisible())
              jTyping.setVisible(true);
          } else {
            ((Timer) e.getSource()).stop();
            jTyping.setVisible(false);
          }

        }
      });
      timer.start();
      timer.setDelay(1000);
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(TypingGUIUpdater.class);
    }
  }


  /***********************************************************
  *** T o o l B a r P r o d u c e r I    interface methods ***
  ***********************************************************/
  public ToolBarModel getToolBarModel() {
    return toolBarModel;
  }
  public String getToolBarTitle() {
    return "Message Compose Toolbar";
  }
  public ToolBarModel initToolBarModel(String propertyKeyName, String toolBarName, Component sourceComponent) {
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS && toolBarModel == null)
      toolBarModel = new ToolBarModel(propertyKeyName, toolBarName != null ? toolBarName : getToolBarTitle(), false);
    if (toolBarModel != null && sourceComponent != null)
      toolBarModel.addComponentActions(sourceComponent);
    return toolBarModel;
  }


  /*************************************************************
  * D R O P   T A R G E T   L I S T E N E R   I n t e r f a c e
  *************************************************************/

  public void dragEnter(DropTargetDragEvent event) {
    updateCursor(event);
  }
  public void dragOver(DropTargetDragEvent event) {
  }
  private void updateCursor(DropTargetDragEvent event) {
    try {
      if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
          event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR) ||
          event.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR) ||
          event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR))
        event.acceptDrag(DnDConstants.ACTION_COPY);
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
  }
  public void dragExit(DropTargetEvent p1) {
  }
  public void drop(DropTargetDropEvent event) {
    try {
      Transferable tr = event.getTransferable();
      if (tr.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR)) {
        FileDND_TransferableData data = (FileDND_TransferableData) tr.getTransferData(FileDND_Transferable.FILE_RECORD_FLAVOR);
        if (data.fileRecordIDs[1] != null && data.fileRecordIDs[1].length > 0) {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          FileLinkRecord[] fLinks = cache.getFileLinkRecords(data.fileRecordIDs[1]);
          addAdditionalAttachments(fLinks);
        }
        else
          event.rejectDrop();
      }
      else if (tr.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR) ||
              tr.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR)) {
        Long[] msgLinkIDs = null;
        if (tr.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR))
          msgLinkIDs = ((MsgDND_TransferableData) tr.getTransferData(MsgDND_Transferable.MSG_RECORD_FLAVOR)).msgLinkIDs;
        else
          msgLinkIDs = ((AddrDND_TransferableData) tr.getTransferData(AddrDND_Transferable.ADDR_RECORD_FLAVOR)).msgLinkIDs;
        if (msgLinkIDs != null && msgLinkIDs.length > 0) {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(msgLinkIDs);
          addAdditionalAttachments(mLinks);
        }
        else
          event.rejectDrop();
      }
      else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        event.acceptDrop(DnDConstants.ACTION_COPY);
        java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
        Iterator iterator = fileList.iterator();
        ArrayList filesL = new ArrayList();
        while (iterator.hasNext()) {
          File file = (File) iterator.next();
          if (file.isFile())
            filesL.add(file);
        }
        if (filesL.size() > 0) {
          File[] files = new File[filesL.size()];
          filesL.toArray(files);
          addAdditionalAttachments(files);
        }
      } else {
        event.rejectDrop();
      }
    } catch (IOException io) {
      event.rejectDrop();
    } catch (UnsupportedFlavorException ufe) {
      event.rejectDrop();
    }
    event.getDropTargetContext().dropComplete(true);
  }
  /**
  * Update the 'selectedAttachments' array to add the additionalAttachments.
  */
  private void addAdditionalAttachments(Object[] additionalAttachments) {
    if (additionalAttachments != null && additionalAttachments.length > 0) {
      selectedAttachments = ArrayUtils.concatinate(selectedAttachments, additionalAttachments, Object.class);
      selectedAttachments = ArrayUtils.removeDuplicates(selectedAttachments, Object.class);
      setEnabledActions();
      setAttachmentsPanel();
    }
  }
  public void dropActionChanged(DropTargetDragEvent event) {
    updateCursor(event);
  }


  /*************************************************************************
  * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
  * Dispose the object and release resources to help in garbage collection.
  ************************************************************************/
  private boolean isDisposed = false;
  public void disposeObj() {
    if (!isDisposed) {
      isDisposed = true;
      for (int i=0; i<dropTargetL.size(); i++) {
        try {
          DropTarget target = (DropTarget) dropTargetL.get(i);
          if (target != null) {
            Component c = target.getComponent();
            if (c != null)
              c.setDropTarget(null);
            target.setComponent(null);
          }
        } catch (Throwable t) {
        }
      }
      if (typingListener != null) {
        cache.removeMsgTypingListener(typingListener);
        typingListener = null;
      }
      dropTargetL.clear();
      componentsForDNDL.clear();
      componentsForPopupL.clear();
      msgComponents.disposeObj();
    }
  } // end disposeObj

  /*****************************************************************************
  * I N T E R F A C E   M E T H O D  ---   M s g T y p e M a n a g e r I  *****
  ****************************************************************************/
  public Long getTypeNotifyFolderId() {
    Long folderId = null;
    Record[] recipients = selectedRecipients[MsgLinkRecord.RECIPIENT_TYPE_TO];
    if (recipients != null && recipients.length == 1 && recipients[0] != null) {
      folderId = recipients[0].getId();
    }
    return folderId;
  }
  public boolean isTypeNotifyEnabled(boolean isInsert) {
    return isChatComposePanel && (isInsert || msgComponents.isAnyContent());
  }
  public void typeSourceUpdated(boolean isInsert) {
    setEnabledSend(isInsert);
    setEnabledUndoAndRedo();
  }

  /***************************************************************************************
  * I N T E R F A C E   M E T H O D  ---   M s g S e n d I n f o P r o v i d e r I  *****
  **************************************************************************************/
  public void messageSentNotify() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "messageSentNotify()");
    // do gui action in a gui thread...
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "messageSentNotify.run()");

        // Mail: close the compose message Frame
        if (!isChatComposePanel) {
          isMessageSent = true;
          // see if we should delete the source draft
          if (fromDraftMsgLink != null) {
            boolean isSave = !isSavingAsDraft;
            boolean isFromDraftFolderToDraftFolder =
              (isSavingAsDraft && // destination Draft folder
                fromDraftMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER &&
                fromDraftMsgLink.ownerObjId.equals(cache.getUserRecord().draftFolderId) // source Draft folder
                );
            if (isFromDraftFolderToDraftFolder || (isDeleteDraftAfterSave && isSave)) {
              Obj_IDs_Co request = new Obj_IDs_Co();
              request.IDs = new Long[2][];
              // Owner of a message link is a folder, but we need to specify the shares to aid in permission checking.
              Long shareId = cache.getFolderShareRecordMy(fromDraftMsgLink.ownerObjId, true).shareId;
              request.IDs[0] = new Long[] { fromDraftMsgLink.msgLinkId };
              request.IDs[1] = new Long[] { shareId };
              MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_REMOVE, request));
            }
          }
          Window w = SwingUtilities.windowForComponent(MsgComposePanel.this);
          if (w instanceof JActionFrameClosable) {
            JActionFrameClosable frame = (JActionFrameClosable) w;
            frame.closeFrame();
          }
          else if (w instanceof GeneralDialog) {
            GeneralDialog dialog = (GeneralDialog) w;
            dialog.closeDialog();
          }
        }
        // Chat: clear attachments panel, priority combo, message area, flip flags
        else {
          // clear all attachments
          selectedAttachments = new Object[0];
          setAttachmentsPanel();

          // clear priority
          if (msgComponents.getPriorityIndex() != 1) {
            msgComponents.setSelectedPriorityIndex(1);
            priorityPressed();
          }

          // clear message area
          msgComponents.clearMessageArea();
          msgComponents.focusMessageArea();

          undoMngr.discardAllEdits();

          // clear red flags
          Record[] toRecipients = getSelectedRecipients(MsgLinkRecord.RECIPIENT_TYPE_TO);
          if (toRecipients != null && toRecipients.length == 1 && toRecipients[0] instanceof FolderPair) {
            StatOps.markOldCachedMsgsInFolder(MainFrame.getServerInterfaceLayer(), toRecipients[0].getId());
          }
        }

        // check for unknown recipients for addition to Address Book
        checkForUnknownRecipientsForAddressBookAdition_Threaded(preConversionSelectedRecipients, postConversionSelectedRecipients);

        // update 'Used' stamps for the recipient contacts - pre-conversion recipients include un-expanded groups
        UserOps.updateUsedStamp(MainFrame.getServerInterfaceLayer(), preConversionSelectedRecipients, 10 * 60 * 1000);

        // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
        if (trace != null) trace.exit(getClass());
      }
    }); // end invoke later
    if (trace != null) trace.exit(MsgComposePanel.class);
  }
  public boolean isCopyToOutgoing() {
    return msgComponents.isSelectedCopy();
  }
  public boolean isSavingAsDraft() {
    return isSavingAsDraft;
  }
  public boolean isStagedSecure() {
    return msgComponents.isStagedSecure();
  }

  public boolean isMsgHTML() {
    return msgComponents.isHTML();
  }

  public Record getFromAccount() {
    return msgComponents.getFromAccount();
  }

  public Record[][] getSelectedRecipients() {
    return selectedRecipients;
  }
  public Record[] getSelectedRecipients(short type) {
    return selectedRecipients[type];
  }
  public Object[] getSelectedAttachments() {
    return selectedAttachments;
  }
  public Object[] getInlineAttachments() {
    File[] inlineAttachments = null;
    ArrayList inlineAttachmentsL = null;
    JTextComponent component = msgComponents.getMsgTypeArea();
    Document doc = component.getDocument();
    if (doc instanceof HTMLDocument) {
      HTMLDocument htmlDoc = (HTMLDocument) doc;
      for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.IMG); iterator.isValid(); iterator.next()) {
        AttributeSet attributes = iterator.getAttributes();
        if (attributes != null) {
          String srcString = (String) attributes.getAttribute(HTML.Attribute.SRC);
          if (srcString != null) {
            if (srcString.startsWith("file:/"))
              srcString = srcString.substring("file:/".length());
            File file = new File(srcString);
            if (file.exists()) {
              if (inlineAttachmentsL == null) inlineAttachmentsL = new ArrayList();
              inlineAttachmentsL.add(file);
            }
          }
        }
      }
      inlineAttachments = (File[]) ArrayUtils.toArray(inlineAttachmentsL, File.class);
    }
    return inlineAttachments;
  }
  public Object[] getSelectedAndInlineAttachments() {
    return ArrayUtils.concatinate(getSelectedAttachments(), getInlineAttachments(), Object.class);
  }

  public MsgLinkRecord getReplyToMsgLink() {
    return replyToMsgLink;
  }

  public Object getContext() {
    return this;
  }
  public String[] getContent() {
    return msgComponents.getContent();
  }
  public Short getContentType() {
    return msgComponents.getContentType();
  }
  public short getContentMode() {
    return msgComponents.getContentMode();
  }
  public Timestamp getExpiry() {
    return msgComponents.getExpiry();
  }
  public String getQuestion() {
    return msgComponents.getQuestion();
  }
  public String getPassword() {
    return msgComponents.getPassword();
  }
  public short getPriority() {
    int selectedIndex = msgComponents.getPriorityIndex();
    short imp = 0;
    if (selectedIndex == PRIORITY_INDEX_FYI)
      imp = MsgDataRecord.IMPORTANCE_FYI_PLAIN;
    else if (selectedIndex == PRIORITY_INDEX_NORMAL)
      imp = MsgDataRecord.IMPORTANCE_NORMAL_PLAIN;
    else if (selectedIndex == PRIORITY_INDEX_HIGH)
      imp = MsgDataRecord.IMPORTANCE_HIGH_PLAIN;
    if (getContentMode() == CONTENT_MODE_MAIL_HTML)
      imp *= 2;
    return imp;
  }

  public void setSendMessageInProgress(boolean inProgress) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "setSendMessageInProgress(boolean inProgress)");
    if (trace != null) trace.args(inProgress);
    if (sendMessageInProgress != inProgress) {
      sendMessageInProgress = inProgress;
      setEnabledActions();
    }
    if (trace != null) trace.exit(MsgComposePanel.class);
  }



  /***********************************************************************
  * I N T E R F A C E   M E T H O D  ---   U n d o M a n a g e r I  *****
  **********************************************************************/
  public UndoManager getUndoManager() {
    return undoMngr;
  }
  public void setEnabledUndoAndRedo() {
    ((UndoAction) actions[UNDO_ACTION]).updateUndoState();
    ((RedoAction) actions[REDO_ACTION]).updateRedoState();
  }


  /***********************************************************************
  * I N T E R F A C E   M E T H O D  ---   V e t o R i s i b l e I  *****
  **********************************************************************/
  public boolean isVetoRaised(int type) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgComposePanel.class, "isVetoRaised(int type)");
    if (trace != null) trace.args(type);
    boolean veto = false;
    if (type == VetoRisibleI.TYPE_WINDOW_CLOSE) {
      if (!isChatComposePanel) {
        if (!isMessageSent) {
          // don't save when SAVE button is enabled because that includes selected recipients... just check if there is any data!
          boolean anythingToSave = msgComponents.isAnyContent() || (getSelectedAttachments() != null && getSelectedAttachments().length > 0);
          boolean anythingChanged = isOriginalContentOrAttachmentsChanged();
          if (anythingToSave && anythingChanged) {
            String title = "Composition Not Saved";
//            Window w = SwingUtilities.windowForComponent(this);
//            if (w instanceof JFrame) 
//              ((JFrame) w).getTitle();
            String message = null;
            boolean isSaveAddressChanges = false; // special handling for editing address, as TO is changed to parent folder so SEND is ok
            if (objType == MsgDataRecord.OBJ_TYPE_ADDR && fromDraftMsgLink != null) {
              message = "Save changes?";
              isSaveAddressChanges = true;
            } else {
              message = objType == MsgDataRecord.OBJ_TYPE_ADDR ? "Save address to the Drafts folder for future editing?" : "Save message to the Drafts folder for future editing?";
            }
            int rc = JOptionPane.showConfirmDialog(MsgComposePanel.this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (rc == JOptionPane.YES_OPTION) {
              // save
              if (isSaveAddressChanges)
                ((SendAction) actions[SEND_ACTION]).actionPerformed((ActionEvent) null);
              else
                ((SaveAsDraftAction) actions[SAVE_AS_DRAFT_ACTION]).actionPerformed();
              veto = true;
            } else if (rc == JOptionPane.NO_OPTION) {
              // do not save
            } else if (rc == JOptionPane.CANCEL_OPTION) {
              // cancel
              veto = true;
            }
          }
        }
      }
    } else if (type == VetoRisibleI.TYPE_SAVE) {
      if (!isChatComposePanel) {
        if (!msgComponents.isSufficientContent()) {
          String title = "Composition Incomplete";
          Window w = SwingUtilities.windowForComponent(this);
          if (w instanceof JFrame)
            ((JFrame) w).getTitle();
          String message = objType == MsgDataRecord.OBJ_TYPE_ADDR ? "Please provide both name and email address." : "Please provide at least the subject or body for your message.";
          MessageDialog.showWarningDialog(w, new JMyLabel(message), title, false);
          veto = true;
        } else if (msgComponents.getAudioCapturePanel().anyCapturedAndNotAttached()) {
          String title = "Audio Recording Not Attached";
          String message = "Audio recording is not attached.  Attach it now?";
          int rc = JOptionPane.showConfirmDialog(MsgComposePanel.this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
          if (rc == JOptionPane.YES_OPTION)
            msgComponents.getAudioCapturePanel().attach();
          else if (rc == JOptionPane.CANCEL_OPTION)
            veto = true;
        }
      }
    }
    if (trace != null) trace.exit(MsgComposePanel.class, veto);
    return veto;
  }

}