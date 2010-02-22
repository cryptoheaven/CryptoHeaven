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

package com.CH_gui.table;

import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.addressBook.*;
import com.CH_gui.chatTable.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.groupTable.*;
import com.CH_gui.gui.*;
import com.CH_gui.folder.*;
import com.CH_gui.keyTable.*;
import com.CH_gui.localFileTable.*;
import com.CH_gui.msgs.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.postTable.PostTableComponent;
import com.CH_gui.recycleTable.*;
import com.CH_gui.tree.*;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.FolderRecUtil;

import com.CH_co.gui.MyInsets;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.tree.*;
import com.CH_co.util.*;

import java.awt.*;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;

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
 * <b>$Revision: 1.40 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class TableComponent extends JPanel implements TreeSelectionListener, VisualsSavable, DisposableObj {

  public static final boolean DEBUG__DISABLE_LOCAL_FILE_CHOOSER = false;

  private RecordTableComponent lastFilterableTableComponent;

  private AddressTableComponent addressTableComponent;
  private MsgPreviewPanel addressPreviewPanel;
  private JSplitPane addressSplitPane;

  private WhiteListTableComponent whiteListTableComponent;
  private MsgPreviewPanel whiteListPreviewPanel;
  private JSplitPane whiteListSplitPane;

  private FileTableComponent fileTableComponent;
  private KeyTableComponent keyTableComponent;
  private GroupTableComponent groupTableComponent;
  private RecycleTableComponent recycleTableComponent;

  private PostTableComponent postTableComponent;

  private ChatTableComponent chatTableComponent;
  private MsgComposePanel chatComposePanel;
  private JSplitPane chatSplitPane;

  private MsgTableComponent msgTableComponent;
  private MsgPreviewPanel msgPreviewPanel;
  private JSplitPane msgSplitPane;

  private MsgInboxTableComponent msgInboxTableComponent;
  private MsgPreviewPanel msgInboxPreviewPanel;
  private JSplitPane msgInboxSplitPane;

  private MsgSentTableComponent msgSentTableComponent;
  private MsgPreviewPanel msgSentPreviewPanel;
  private JSplitPane msgSentSplitPane;

  private MsgSpamTableComponent msgSpamTableComponent;
  private MsgPreviewPanel msgSpamPreviewPanel;
  private JSplitPane msgSpamSplitPane;

  private MsgDraftsTableComponent msgDraftsTableComponent;
  private MsgPreviewPanel msgDraftsPreviewPanel;
  private JSplitPane msgDraftsSplitPane;

  private short displayMode;
  private boolean displayMode_IsInboxFolder;
  private boolean displayMode_IsSentFolder;
  private boolean displayMode_IsSpamFolder;
  private boolean displayMode_IsDraftsFolder;

  // for local file browsing...
  private FileChooserComponent fileChooserComponent;

  private Long lastSelectedFolderID;

  // Handle marking chat/posting folder as open
  private Long openChatFolderID;

  private String propertyName;

  private Component displayed;
  private Component displayedTable;
  private Component welcomeScreen;

  private Component categoryMailFolder;
  private Component categoryFileFolder;
  private Component categoryChatFolder;
  private Component categoryGroupFolder;

  /** Creates new TableComponent */
  public TableComponent(String propertyName) {
    this(propertyName, new JPanel());
  }
  public TableComponent(String propertyName, Component welcomeScreen) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "TableComponent(String propertyName, Component welcomeScreen)");
    if (trace != null) trace.args(propertyName, welcomeScreen);

    this.propertyName = propertyName;

    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(0,0,0,0));
    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this), "Dimension width 450 height 350"));
    setWelcomeScreenComponent(welcomeScreen);

    if (trace != null) trace.exit(TableComponent.class);
  }

  private void removeRecordListeners() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "removeRecordListeners()");
    if (addressTableComponent != null)
      addressTableComponent.removeRecordListeners();
    if (whiteListTableComponent != null)
      whiteListTableComponent.removeRecordListeners();
    if (fileTableComponent != null)
      fileTableComponent.removeRecordListeners();
    if (postTableComponent != null)
      postTableComponent.removeRecordListeners();
    if (chatTableComponent != null)
      chatTableComponent.removeRecordListeners();
    if (msgTableComponent != null)
      msgTableComponent.removeRecordListeners();
    if (msgInboxTableComponent != null)
      msgInboxTableComponent.removeRecordListeners();
    if (msgSentTableComponent != null)
      msgSentTableComponent.removeRecordListeners();
    if (msgSpamTableComponent != null)
      msgSpamTableComponent.removeRecordListeners();
    if (msgDraftsTableComponent != null)
      msgDraftsTableComponent.removeRecordListeners();
    if (keyTableComponent != null)
      keyTableComponent.removeRecordListeners();
    if (groupTableComponent != null)
      groupTableComponent.removeRecordListeners();
    if (recycleTableComponent != null)
      recycleTableComponent.removeRecordListeners();
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void clearCachedFetchedFolderIDs() {
    try { getAddressTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getWhiteListTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getFileTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getPostTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getChatTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getMsgTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getMsgInboxTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getMsgSentTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getMsgSpamTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getMsgDraftsTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getKeyTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getGroupTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
    try { getRecycleTableComponent().getRecordTableScrollPane().getTableModel().getCachedFetchedFolderIDs().clear(); } catch (Throwable t) { }
  }

  public AddressTableComponent getAddressTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getAddressTableComponent()");
    AddressTableComponent rc = addressTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public WhiteListTableComponent getWhiteListTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getWhiteListTableComponent()");
    WhiteListTableComponent rc = whiteListTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public FileTableComponent getFileTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getFileTableComponent()");
    FileTableComponent rc = fileTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public PostTableComponent getPostTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getPostTableComponent()");
    PostTableComponent rc = postTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public ChatTableComponent getChatTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getChatTableComponent()");
    ChatTableComponent rc = chatTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public MsgTableComponent getMsgTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getMsgTableComponent()");
    MsgTableComponent rc = msgTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public MsgInboxTableComponent getMsgInboxTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getMsgInboxTableComponent()");
    MsgInboxTableComponent rc = msgInboxTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public MsgSentTableComponent getMsgSentTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getMsgSentTableComponent()");
    MsgSentTableComponent rc = msgSentTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public MsgSpamTableComponent getMsgSpamTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getMsgSpamTableComponent()");
    MsgSpamTableComponent rc = msgSpamTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public MsgDraftsTableComponent getMsgDraftsTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getMsgDraftsTableComponent()");
    MsgDraftsTableComponent rc = msgDraftsTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public KeyTableComponent getKeyTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getKeyTableComponent()");
    KeyTableComponent rc = keyTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public GroupTableComponent getGroupTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getGroupTableComponent()");
    GroupTableComponent rc = groupTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public RecycleTableComponent getRecycleTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getRecycleTableComponent()");
    RecycleTableComponent rc = recycleTableComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public FileChooserComponent getLocalFileTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getLocalFileTableComponent()");
    FileChooserComponent rc = fileChooserComponent;
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public void initAddressTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initAddressTableComponent()");
    if (addressTableComponent == null) {
      addressTableComponent = new AddressTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(addressTableComponent);
        ((JActionFrame)w).removeComponentActions(addressTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initWhiteListTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initWhiteListTableComponent()");
    if (whiteListTableComponent == null) {
      whiteListTableComponent = new WhiteListTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(whiteListTableComponent);
        ((JActionFrame)w).removeComponentActions(whiteListTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initFileTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initFileTableComponent()");
    if (fileTableComponent == null) {
      fileTableComponent = new FileTableComponent();
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(fileTableComponent);
        ((JActionFrame)w).removeComponentActions(fileTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initPostTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initPostTableComponent()");
    if (postTableComponent == null) {
      postTableComponent = new PostTableComponent();
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(postTableComponent);
        ((JActionFrame)w).removeComponentActions(postTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  } 

  public void initChatTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initChatTableComponent()");
    if (chatTableComponent == null) {
      chatTableComponent = new ChatTableComponent();
      chatComposePanel = new MsgComposePanel(null, MsgDataRecord.OBJ_TYPE_MSG, true);
      if (chatTableComponent.getToolBarModel() != null)
        chatTableComponent.getToolBarModel().addComponentActions(chatComposePanel);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(chatTableComponent);
        ((JActionFrame)w).addComponentActions(chatComposePanel);
        ((JActionFrame)w).removeComponentActions(chatTableComponent);
        ((JActionFrame)w).removeComponentActions(chatComposePanel);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  } 

  public void initMsgTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initMsgTableComponent()");
    if (msgTableComponent == null) {
      msgTableComponent = new MsgTableComponent();
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(msgTableComponent);
        ((JActionFrame)w).removeComponentActions(msgTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initMsgInboxTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initMsgInboxTableComponent()");
    if (msgInboxTableComponent == null) {
      msgInboxTableComponent = new MsgInboxTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(msgInboxTableComponent);
        ((JActionFrame)w).removeComponentActions(msgInboxTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initMsgSentTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initMsgSentTableComponent()");
    if (msgSentTableComponent == null) {
      msgSentTableComponent = new MsgSentTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(msgSentTableComponent);
        ((JActionFrame)w).removeComponentActions(msgSentTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initMsgSpamTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initMsgSpamTableComponent()");
    if (msgSpamTableComponent == null) {
      msgSpamTableComponent = new MsgSpamTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(msgSpamTableComponent);
        ((JActionFrame)w).removeComponentActions(msgSpamTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initMsgDraftsTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initMsgDraftsTableComponent()");
    if (msgDraftsTableComponent == null) {
      msgDraftsTableComponent = new MsgDraftsTableComponent(false);
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(msgDraftsTableComponent);
        ((JActionFrame)w).removeComponentActions(msgDraftsTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initKeyTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initKeyTableComponent()");
    if (keyTableComponent == null) {
      keyTableComponent = new KeyTableComponent(FetchedDataCache.getSingleInstance().getKeyRecords());
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(keyTableComponent);
        ((JActionFrame)w).removeComponentActions(keyTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initGroupTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initGroupTableComponent()");
    if (groupTableComponent == null) {
      groupTableComponent = new GroupTableComponent();
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(groupTableComponent);
        ((JActionFrame)w).removeComponentActions(groupTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initRecycleTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initRecycleTableComponent()");
    if (recycleTableComponent == null) {
      recycleTableComponent = new RecycleTableComponent();
      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(recycleTableComponent);
        ((JActionFrame)w).removeComponentActions(recycleTableComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public void initLocalFileTableComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "initLocalFileTableComponent()");
    if (DEBUG__DISABLE_LOCAL_FILE_CHOOSER) {
      System.out.println("Warning: Local file chooser is disabled.");
    } else if (fileChooserComponent == null) {
      if (propertyName == null)
        fileChooserComponent = new FileChooserComponent(UploadUtilities.getDefaultSourceDir());
      else
        fileChooserComponent = new FileChooserComponent(propertyName);

      Window w = SwingUtilities.windowForComponent(this);
      if (w instanceof JActionFrame) {
        ((JActionFrame)w).addComponentActions(fileChooserComponent);
        ((JActionFrame)w).removeComponentActions(fileChooserComponent);
      }
    }
    if (trace != null) trace.exit(TableComponent.class);
  }


  private void setDisplay(Short mode, boolean isInboxFolder, boolean isSentFolder, boolean isSpamFolder, boolean isDraftsFolder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "setDisplay(Short mode, boolean isInboxFolder, boolean isSentFolder, boolean isSpamFolder, boolean isDraftsFolder)");
    if (trace != null) trace.args(mode);
    if (trace != null) trace.args(isInboxFolder);
    if (trace != null) trace.args(isSentFolder);
    if (trace != null) trace.args(isSpamFolder);
    if (trace != null) trace.args(isDraftsFolder);
    // only setDisplay if the mode has changed (special handling for send msg folder as its different from regular)
    if (mode != null && 
        (displayMode != mode.shortValue() || 
          (mode.shortValue() == FolderRecord.MESSAGE_FOLDER && isInboxFolder != displayMode_IsInboxFolder) ||
          (mode.shortValue() == FolderRecord.MESSAGE_FOLDER && isSentFolder != displayMode_IsSentFolder) ||
          (mode.shortValue() == FolderRecord.MESSAGE_FOLDER && isSpamFolder != displayMode_IsSpamFolder) ||
          (mode.shortValue() == FolderRecord.MESSAGE_FOLDER && isDraftsFolder != displayMode_IsDraftsFolder)
        )
       )
    {
      // save the visuals from the last component to GlobalProperties
      VisualsSavable vs1 = null;
      VisualsSavable vs2 = null;
      switch (displayMode) {
        case FolderRecord.ADDRESS_FOLDER :
          vs1 = addressTableComponent;
          vs2 = addressTableComponent.getActionTable();
          break;
        case FolderRecord.WHITELIST_FOLDER :
          vs1 = whiteListTableComponent;
          vs2 = whiteListTableComponent.getActionTable();
          break;
        case FolderRecord.FILE_FOLDER :
          vs1 = fileTableComponent;
          vs2 = fileTableComponent.getActionTable();
          break;
        case FolderRecord.MESSAGE_FOLDER :
          if (!displayMode_IsInboxFolder && !displayMode_IsSentFolder && !displayMode_IsSpamFolder && !displayMode_IsDraftsFolder) {
            vs1 = msgTableComponent;
            vs2 = msgTableComponent.getActionTable();
          } else if (displayMode_IsInboxFolder) {
            vs1 = msgInboxTableComponent;
            vs2 = msgInboxTableComponent.getActionTable();
          } else if (displayMode_IsSentFolder) {
            vs1 = msgSentTableComponent;
            vs2 = msgSentTableComponent.getActionTable();
          } else if (displayMode_IsSpamFolder) {
            vs1 = msgSpamTableComponent;
            vs2 = msgSpamTableComponent.getActionTable();
          } else {
            vs1 = msgDraftsTableComponent;
            vs2 = msgDraftsTableComponent.getActionTable();
          }
          break;
        case FolderRecord.POSTING_FOLDER :
          vs1 = postTableComponent;
          vs2 = postTableComponent.getActionTable();
          break;
        case FolderRecord.CHATTING_FOLDER :
          vs1 = chatTableComponent;
          vs2 = chatTableComponent.getActionTable();
          break;
        case FolderRecord.KEY_FOLDER :
          vs1 = keyTableComponent;
          vs2 = keyTableComponent.getActionTable();
          break;
        case FolderRecord.GROUP_FOLDER :
          vs1 = groupTableComponent;
          vs2 = groupTableComponent.getActionTable();
          break;
        case FolderRecord.RECYCLE_FOLDER :
          vs1 = recycleTableComponent;
          vs2 = recycleTableComponent.getActionTable();
          break;
      }
      if (vs1 != null) {
        String key = MiscGui.getVisualsKeyName(vs1);
        String value = vs1.getVisuals();
        GlobalProperties.setProperty(key, value);
      }
      if (vs2 != null) {
        String key = MiscGui.getVisualsKeyName(vs2);
        String value = vs2.getVisuals();
        GlobalProperties.setProperty(key, value);
      }

      // remember the new mode
      displayMode = mode.shortValue();
      if (mode.shortValue() == FolderRecord.MESSAGE_FOLDER) {
        displayMode_IsInboxFolder = isInboxFolder;
        displayMode_IsSentFolder = isSentFolder;
        displayMode_IsSpamFolder = isSpamFolder;
        displayMode_IsDraftsFolder = isDraftsFolder;
      }

      // before removing all components in that table, lets disable their actions
      ActionUtils.disableAllActions(this);
      removeAll();


      Component c = null;
      switch (displayMode) {
        case FolderRecord.CATEGORY_MAIL_FOLDER:
          if (categoryMailFolder == null) {
            RecordTableComponent table = new MsgTableComponent();
            table.initData(new Long(FolderRecord.CATEGORY_MAIL_ID));
            categoryMailFolder = table;
          }
          c = categoryMailFolder;
          break;
        case FolderRecord.CATEGORY_FILE_FOLDER:
          if (categoryFileFolder == null) {
            RecordTableComponent table = new FileTableComponent();
            table.initData(new Long(FolderRecord.CATEGORY_FILE_ID));
            categoryFileFolder = table;
          }
          c = categoryFileFolder;
          break;
        case FolderRecord.CATEGORY_CHAT_FOLDER:
          if (categoryChatFolder == null) {
            RecordTableComponent table = new ChatTableComponent();
            table.initData(new Long(FolderRecord.CATEGORY_CHAT_ID));
            categoryChatFolder = table;
          }
          c = categoryChatFolder;
          break;
        case FolderRecord.CATEGORY_GROUP_FOLDER:
          if (categoryGroupFolder == null) {
            RecordTableComponent table = new GroupTableComponent();
            table.initData(new Long(FolderRecord.CATEGORY_GROUP_ID));
            categoryGroupFolder = table;
          }
          c = categoryGroupFolder;
          break;
        case FolderRecord.FILE_FOLDER:
          // make sure we have the file table component;
          initFileTableComponent();
          c = fileTableComponent;
          break;
        case FolderRecord.ADDRESS_FOLDER:
          if (addressSplitPane == null) {
            // make sure we have the table component;
            initAddressTableComponent();
            addressPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_ADDR);
            addressSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + addressTableComponent.getVisualsClassKeyName(), JSplitPane.HORIZONTAL_SPLIT, 0.5d);
            addressTableComponent.addPreviewComponent(addressSplitPane, addressPreviewPanel);
          }
          c = addressTableComponent;
          break;
        case FolderRecord.WHITELIST_FOLDER:
          if (whiteListSplitPane == null) {
            // make sure we have the table component;
            initWhiteListTableComponent();
            whiteListPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_ADDR);
            whiteListSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + whiteListTableComponent.getVisualsClassKeyName(), JSplitPane.HORIZONTAL_SPLIT, 0.5d);
            whiteListTableComponent.addPreviewComponent(whiteListSplitPane, whiteListPreviewPanel);
          }
          c = whiteListTableComponent;
          break;
        case FolderRecord.MESSAGE_FOLDER:
          if (!isInboxFolder && !isSentFolder && !isSpamFolder && !isDraftsFolder) {
            if (msgSplitPane == null) {
              // make sure we have the msg table component;
              initMsgTableComponent();
              msgPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_MSG);
              msgSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + msgTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              msgTableComponent.addPreviewComponent(msgSplitPane, msgPreviewPanel);
            }
            c = msgTableComponent;
          } else if (isInboxFolder) {
            if (msgInboxSplitPane == null) {
              // make sure we have the msg table component;
              initMsgInboxTableComponent();
              msgInboxPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_MSG);
              msgInboxSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + msgInboxTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              msgInboxTableComponent.addPreviewComponent(msgInboxSplitPane, msgInboxPreviewPanel);
            }
            c = msgInboxTableComponent;
          } else if (isSentFolder) {
            if (msgSentSplitPane == null) {
              // make sure we have the msg table component;
              initMsgSentTableComponent();
              msgSentPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_MSG);
              msgSentSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + msgSentTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              msgSentTableComponent.addPreviewComponent(msgSentSplitPane, msgSentPreviewPanel);
            }
            c = msgSentTableComponent;
          } else if (isSpamFolder) {
            if (msgSpamSplitPane == null) {
              // make sure we have the msg table component;
              initMsgSpamTableComponent();
              msgSpamPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_MSG);
              msgSpamSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + msgSpamTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              msgSpamTableComponent.addPreviewComponent(msgSpamSplitPane, msgSpamPreviewPanel);
            }
            c = msgSpamTableComponent;
          } else {
            if (msgDraftsSplitPane == null) {
              // make sure we have the msg table component;
              initMsgDraftsTableComponent();
              msgDraftsPreviewPanel = new MsgPreviewPanel(MsgDataRecord.OBJ_TYPE_MSG);
              msgDraftsSplitPane = createSplitPane(getVisualsClassKeyName() + "_" + msgDraftsTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              //msgDraftsSplitPane = createSplitPane(msgDraftsTableComponent, msgDraftsPreviewPanel, "_" + msgDraftsTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.40d);
              msgDraftsTableComponent.addPreviewComponent(msgDraftsSplitPane, msgDraftsPreviewPanel);
            }
            c = msgDraftsTableComponent;
          }
          break;
        case FolderRecord.POSTING_FOLDER:
          // make sure we have the post table component;
          initPostTableComponent();
          c = postTableComponent;
          break;
        case FolderRecord.CHATTING_FOLDER:
          // make sure we have the chat table component;
          initChatTableComponent();
          if (chatSplitPane == null) {
            chatSplitPane = createSplitPane(chatTableComponent, chatComposePanel, "_chatComp", JSplitPane.VERTICAL_SPLIT, 0.90d);
          }
          c = chatSplitPane;
          break;
        case FolderRecord.KEY_FOLDER:
          // make sure we have key table component;
          initKeyTableComponent();
          c = keyTableComponent;
          break;
        case FolderRecord.GROUP_FOLDER:
          // make sure we have group table component;
          initGroupTableComponent();
          c = groupTableComponent;
          break;
        case FolderRecord.RECYCLE_FOLDER:
          // make sure we have recycle table component;
          initRecycleTableComponent();
          c = recycleTableComponent;
          break;
        case FolderRecord.LOCAL_FILES_FOLDER:
          // make sure we have the file chooser component ready;
          initLocalFileTableComponent();
          c = fileChooserComponent;
          break;
      }
      // Transfer Filter properties to the new component
      Component comp = c;
      if (comp instanceof JSplitPane) {
        comp = ((JSplitPane) comp).getTopComponent();
      }
      if (comp instanceof RecordTableComponent) {
        RecordTableComponent tableComp = (RecordTableComponent) comp;
        if (tableComp.isFilterable()) {
          if (lastFilterableTableComponent != null) {
            tableComp.setFilterFromComponent(lastFilterableTableComponent);
            lastFilterableTableComponent.setFilterEnabled(Boolean.FALSE);
          }
          lastFilterableTableComponent = tableComp;
        }
      }
      setDisplayed(c);
    }
    if (trace != null) trace.exit(TableComponent.class);
  }

  public Component getWelcomeScreenComponent() {
    return welcomeScreen;
  }

  public void setWelcomeScreenComponent(Component c) {
    welcomeScreen = c;
    if (displayed == null) {
      setDisplayed(null);
    }
  }

  /**
   * Switch the displayed component.
   */
  private void setDisplayed(Component c) {
    boolean displayedChanged = false;
    if (c == null) {
      c = welcomeScreen;
      displayMode = 0;
      displayMode_IsInboxFolder = false;
      displayMode_IsSentFolder = false;
      displayMode_IsSpamFolder = false;
      displayMode_IsDraftsFolder = false;
    }

    Component previousDisplayed = displayed;

    if (c != null && c != displayed) {
      displayedChanged = true;
      if (displayed != null)
        remove(displayed);
      displayed = c;
      add(c, new GridBagConstraints(0, 0, 1, 1, 10, 10, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }

    if (displayedChanged) {
      Window w = SwingUtilities.windowForComponent(this);
      JActionFrame aFrame = null;
      if (w instanceof JActionFrame) {
        aFrame = (JActionFrame) w;
        aFrame.removeComponentActions(previousDisplayed);
        aFrame.rebuildAllActions(this);
      }

      // we changed our children, lets display/hide them
      validate();
      repaint();
    }
  }

  public void setSelectedId(Long objId) {
    Component comp = displayed;
    if (comp instanceof JSplitPaneVS) {
      JSplitPaneVS pane = (JSplitPaneVS) comp;
      Component[] comps = pane.getComponents();
      for (int i=0; i<comps.length; i++) {
        if (comps[i] instanceof RecordTableComponent) {
          comp = comps[i];
          break;
        }
      }
    }
    if (comp instanceof RecordTableComponent) {
      RecordTableComponent tableComp = (RecordTableComponent) comp;
      RecordTableScrollPane recordTable = tableComp.getRecordTableScrollPane();
      RecordTableSelection selection = new RecordTableSelection(objId);
      selection.restoreData(recordTable);
    }
  }

  /**
   * Creates a coupled vertically split pane view of a record table and component viewer.
   * The attached viewer must be a RecordSelectionListener to respond to record selection changes
   * in the table.  Property name is used to store visuals about the split bar.
   */
  public static JSplitPane createSplitPane(String propertyName, int orientation, double resizeWeight) {
    JSplitPane splitPane = new JSplitPaneVS(propertyName, orientation, resizeWeight);
    splitPane.setOneTouchExpandable(false);
    if (splitPane.getDividerSize() > 5) splitPane.setDividerSize(5);
    return splitPane;
  }
  private JSplitPane createSplitPane(RecordTableComponent recordTableComp, JComponent viewer, String propertyNamePrefix, int orientation, double resizeWeight) {
    return createSplitPane(getVisualsClassKeyName() + propertyNamePrefix, recordTableComp, viewer, orientation, resizeWeight);
  }
  public static JSplitPane createSplitPane(String propertyName, RecordTableComponent recordTableComp, JComponent viewer, int orientation, double resizeWeight) {
    viewer.setBorder(new EmptyBorder(0,0,0,0));

    // link the table with preview panel
    if (viewer instanceof RecordSelectionListener)
      recordTableComp.getActionTable().addRecordSelectionListener((RecordSelectionListener) viewer);
    recordTableComp.setBorder(new EmptyBorder(0,0,0,0));

    JSplitPane splitPane = new JSplitPaneVS(propertyName, orientation, recordTableComp, viewer, resizeWeight);
    splitPane.setOneTouchExpandable(false);
    if (splitPane.getDividerSize() > 5) splitPane.setDividerSize(5);

//    if (viewer instanceof RecordSelectionListener) {
//      recordTableComp.addPreviewComponent(splitPane, viewer);
//    }

    return splitPane;
  }

  /** Tree selection listener interface */
  public void valueChanged(TreeSelectionEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "valueChanged(final TreeSelectionEvent event)");
    if (trace != null) trace.args(event);

    Object source = event.getSource();
    Long selectedFolderID = null;
    boolean selectionAttempted = false;

    if (source instanceof FolderTree) {
      FolderTree tree = (FolderTree) source;
      if (!tree.isSelectionSuppressed()) {
        if (trace != null) trace.data(10, "selection not suppressed");
        TreeSelectionModel selectionModel = tree.getSelectionModel();
        selectionAttempted = true;
        TreePath path = selectionModel.getSelectionPath();

        if (path != null) {
          FolderTreeNode node = (FolderTreeNode) path.getLastPathComponent();
          FolderPair pair = node.getFolderObject();
          // if Root, skip
          if (pair == null) { 
            // skip ROOT
            if (trace != null) trace.data(15, "skip ROOT");
          }
          // else if all other regular folders
          else if (pair != null && pair.getFolderRecord() != null) {
            if (trace != null) trace.data(16, pair);
            Long folderId = pair.getId();
            if (trace != null) trace.data(19, "selectedPath, folder id", folderId);
            selectedFolderID = folderId;
            if (trace != null) trace.data(20, selectedFolderID);
          }
        }
      } // end selection !suppressed
    }


    if (selectedFolderID != null) {
      if (trace != null) trace.data(40, "selectedFolderID", selectedFolderID);

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord folderRecord = null;
      folderRecord = cache.getFolderRecord(selectedFolderID);

      if (selectedFolderID.equals(lastSelectedFolderID)) {
        // do nothing
      } else if (folderRecord != null) {
        Short folderType = folderRecord.folderType;
        boolean isSentFolder = false;
        // also check if folder is a message folder and directly below the Sent Folder, if yes, consider it as Sent type too
        if (selectedFolderID != null) {
          Long sentFolderId = cache.getUserRecord().sentFolderId;
          isSentFolder = selectedFolderID.equals(sentFolderId);
          if (!isSentFolder && folderRecord.folderType.shortValue() == FolderRecord.MESSAGE_FOLDER) {
            FolderPair fPair = FolderOps.getRootmostFolderInViewHierarchy(selectedFolderID);
            isSentFolder = fPair != null && fPair.getId().equals(sentFolderId);
          }
        }
        boolean isInboxFolder = selectedFolderID != null && selectedFolderID.equals(cache.getUserRecord().msgFolderId);
        boolean isSpamFolder = selectedFolderID != null && selectedFolderID.equals(cache.getUserRecord().junkFolderId);
        boolean isDraftsFolder = selectedFolderID != null && selectedFolderID.equals(cache.getUserRecord().draftFolderId);
        boolean isChattingFolder = folderRecord.isChatting();
        setDisplay(folderType, isInboxFolder, isSentFolder, isSpamFolder, isDraftsFolder);

        if (openChatFolderID != null) {
          FolderRecUtil.clearOpenChatFolder(openChatFolderID, this);
          openChatFolderID = null;
        }

        if (isChattingFolder) {
          FolderPair folderPair = new FolderPair(cache.getFolderShareRecordMy(selectedFolderID, true), folderRecord);
          chatComposePanel.setRecipients(new Record[] { folderPair });
        }

        switch (folderType.shortValue()) {
          case FolderRecord.LOCAL_FILES_FOLDER:
            // no init required
            break;
          case FolderRecord.ADDRESS_FOLDER:
            addressTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.WHITELIST_FOLDER:
            whiteListTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.FILE_FOLDER:
            fileTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.MESSAGE_FOLDER:
            if (!isInboxFolder && !isSentFolder && !isSpamFolder && !isDraftsFolder)
              msgTableComponent.initData(selectedFolderID);
            else if (isInboxFolder)
              msgInboxTableComponent.initData(selectedFolderID);
            else if (isSentFolder)
              msgSentTableComponent.initData(selectedFolderID);
            else if (isSpamFolder)
              msgSpamTableComponent.initData(selectedFolderID);
            else 
              msgDraftsTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.POSTING_FOLDER:
            postTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.CHATTING_FOLDER:
            openChatFolderID = selectedFolderID;
            FolderRecUtil.setOpenChatFolder(selectedFolderID, this);
            chatTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.KEY_FOLDER:
            keyTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.GROUP_FOLDER:
            groupTableComponent.initData(selectedFolderID);
            break;
          case FolderRecord.RECYCLE_FOLDER:
            recycleTableComponent.initData(selectedFolderID);
            break;
        }
      }
    } else if (selectionAttempted) {
      if (trace != null) trace.data(100, "selectionAttempted -- in");
      setDisplayed(null);
      if (openChatFolderID != null) {
        FolderRecUtil.clearOpenChatFolder(openChatFolderID, this);
        openChatFolderID = null;
      }
      if (trace != null) trace.data(110, "selectionAttempted -- out");
    }

    if (selectionAttempted)
      lastSelectedFolderID = selectedFolderID;

    if (trace != null) trace.exit(TableComponent.class);
  }

  public void addFolderSelectionListener(FolderSelectionListener l) {
    initFileTableComponent();
    ((FileActionTable) fileTableComponent.getRecordTableScrollPane()).addFolderSelectionListener(l);
  }

  public void removeTreeSelectionListener(FolderSelectionListener l) {
    if (fileTableComponent != null)
      ((FileActionTable) fileTableComponent.getRecordTableScrollPane()).removeFolderSelectionListener(l);
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append("Dimension width ");
    Dimension dim = getSize();
    visuals.append(dim.width);
    visuals.append(" height ");
    visuals.append(dim.height);

    String rc = visuals.toString();
    if (trace != null) trace.exit(TableComponent.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableComponent.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      StringTokenizer st = new StringTokenizer(visuals);  
      st.nextToken();
      st.nextToken();
      int width = Integer.parseInt(st.nextToken());
      st.nextToken();
      int height = Integer.parseInt(st.nextToken());
      setPreferredSize(new Dimension(width, height));
    } catch (Throwable t) {
      if (trace != null) trace.exception(TableComponent.class, 100, t);
    }

    if (trace != null) trace.exit(TableComponent.class);
  }
  public String getExtension() {
    return propertyName;
  }
  public static final String visualsClassKeyName = "TableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeRecordListeners();

    if (addressTableComponent != null) {
      addressTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(addressSplitPane);
    }
    if (whiteListTableComponent != null) {
      whiteListTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(whiteListSplitPane);
    }
    if (fileTableComponent != null)
      fileTableComponent.disposeObj();
    if (postTableComponent != null)
      postTableComponent.disposeObj();
    if (chatTableComponent != null) {
      chatTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(chatSplitPane);
    }
    if (msgTableComponent != null) {
      msgTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(msgSplitPane);
    }
    if (msgInboxTableComponent != null) {
      msgInboxTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(msgInboxSplitPane);
    }
    if (msgSentTableComponent != null) {
      msgSentTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(msgSentSplitPane);
    }
    if (msgSpamTableComponent != null) {
      msgSpamTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(msgSpamSplitPane);
    }
    if (msgDraftsTableComponent != null) {
      msgDraftsTableComponent.disposeObj();
      MiscGui.removeAllComponentsAndListeners(msgDraftsSplitPane);
    }
    if (keyTableComponent != null)
      keyTableComponent.disposeObj();
    if (groupTableComponent != null)
      groupTableComponent.disposeObj();
    if (recycleTableComponent != null)
      recycleTableComponent.disposeObj();
  }

}