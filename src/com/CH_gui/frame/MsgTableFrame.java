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

package com.CH_gui.frame;

import java.awt.*;
import javax.swing.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.addressBook.*;
import com.CH_gui.chatTable.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.postTable.*;
import com.CH_gui.table.*;

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
 * <b>$Revision: 1.29 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgTableFrame extends JActionFrameClosable implements DisposableObj {

  private RecordTableComponent mainTableComponent = null;

  /** Creates new MsgTableFrame */
  public MsgTableFrame(FolderPair folderPair) {
    this(folderPair, Frame.NORMAL);
  }
  public MsgTableFrame(FolderPair folderPair, int initialState) {
    this(folderPair, null, initialState, false, null);
  }
  protected MsgTableFrame(Record parent, MsgLinkRecord[] initialData) {
    this(parent, initialData, Frame.NORMAL, true, null);
  }
  protected MsgTableFrame(Record parent, MsgLinkRecord[] initialData, RecordTableScrollPane parentViewTable) {
    this(parent, initialData, Frame.NORMAL, true, parentViewTable);
  }
  /**
   * Constructor used for application startup with this frame as main application window.
   * Additional action is created to switch to full application.
   */
  protected MsgTableFrame(Record parent, MsgLinkRecord[] initialData, boolean isInitDataModel) {
    this(parent, initialData, isInitDataModel, Frame.NORMAL, !isInitDataModel, null);
  }
  /**
   * @args parent Either FolderPair for folder parent or MsgLinkRecord for message parent.
   */
  private MsgTableFrame(Record parent, MsgLinkRecord[] initialData, int initialState, boolean msgPreviewMode, RecordTableScrollPane parentViewTable) {
    this(parent, initialData, initialData == null, initialState, msgPreviewMode, parentViewTable);
  }
  private MsgTableFrame(Record parent, MsgLinkRecord[] initialData, boolean isInitDataModel, int initialState, boolean msgPreviewMode, RecordTableScrollPane parentViewTable) {
    super(ListRenderer.getRenderedText(parent, false, true, false), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableFrame.class, "MsgTableFrame(Record parent, MsgLinkRecord[] initialData, boolean isInitDataModel, int initialState, boolean msgPreviewMode, RecordTableScrollPane parentViewTable)");
    if (trace != null) trace.args(parent, initialData);
    if (trace != null) trace.args(isInitDataModel);
    if (trace != null) trace.args(initialState);
    if (trace != null) trace.args(msgPreviewMode);
    if (trace != null) trace.args(parentViewTable);

    // Set NORMAL or ICONIFIED state as specified.
    super.setState(initialState);

    FolderPair folderPair = null;
    if (parent instanceof FolderPair)
      folderPair = (FolderPair) parent;

    short folderType = FolderRecord.MESSAGE_FOLDER;
    if (folderPair != null)
      folderType = folderPair.getFolderRecord().folderType.shortValue();

    mainTableComponent = null;
    JComponent mainComponent = null;
    JSplitPane mainSplitPane = null;
    // message folder/address folder
    if (msgPreviewMode ||
        folderType == FolderRecord.MESSAGE_FOLDER ||
        folderType == FolderRecord.ADDRESS_FOLDER ||
        folderType == FolderRecord.WHITELIST_FOLDER ||
        folderType == FolderRecord.RECYCLE_FOLDER) {
      // check for message folder types or special folders
      if (folderType == FolderRecord.ADDRESS_FOLDER) {
        mainTableComponent = new AddressTableComponent(msgPreviewMode);
      } else if (folderType == FolderRecord.WHITELIST_FOLDER) {
        mainTableComponent = new WhiteListTableComponent(msgPreviewMode);
      } else if (folderPair != null && folderPair.getId().equals(FetchedDataCache.getSingleInstance().getUserRecord().msgFolderId)) {
        mainTableComponent = new MsgInboxTableComponent(msgPreviewMode);
      } else if (folderPair != null && folderPair.getId().equals(FetchedDataCache.getSingleInstance().getUserRecord().junkFolderId)) {
        mainTableComponent = new MsgSpamTableComponent(msgPreviewMode);
      } else if (folderPair != null && folderPair.getId().equals(FetchedDataCache.getSingleInstance().getUserRecord().sentFolderId)) {
        mainTableComponent = new MsgSentTableComponent(msgPreviewMode);
      } else if (folderPair != null && folderPair.getId().equals(FetchedDataCache.getSingleInstance().getUserRecord().draftFolderId)) {
        mainTableComponent = new MsgDraftsTableComponent(msgPreviewMode);
      } else { // recycle and all other mail will default here
        mainTableComponent = new MsgTableComponent(msgPreviewMode ? Template.get(Template.NONE) : Template.get(Template.EMPTY_MAIL), msgPreviewMode);
      }

      short objType = (folderType == FolderRecord.ADDRESS_FOLDER || folderType == FolderRecord.WHITELIST_FOLDER) ? MsgDataRecord.OBJ_TYPE_ADDR : MsgDataRecord.OBJ_TYPE_MSG;
      MsgPreviewPanel msgPreviewPanel = new MsgPreviewPanel(objType);

      if (msgPreviewMode) {
        if (initialData.length > 1) {
          if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
            //mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_msg", JSplitPane.VERTICAL_SPLIT, 0.33d);
            mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_" + mainTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.33d);
          } else {
            //mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_addr", JSplitPane.HORIZONTAL_SPLIT, 0.5d);
            mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_" + mainTableComponent.getVisualsClassKeyName(), JSplitPane.HORIZONTAL_SPLIT, 0.5d);
          }
          mainTableComponent.addPreviewComponent(mainSplitPane, msgPreviewPanel);
          mainComponent = mainTableComponent;
        } else {
          JPanel p = new JPanel();
          p.setLayout(new CardLayout());
          p.add(msgPreviewPanel, "top");
          p.add(mainTableComponent, "bottom"); // essentially hidden from view in the card layout
          mainTableComponent.getActionTable().addRecordSelectionListener(msgPreviewPanel);
          mainComponent = p;
          RecordTableScrollPane scrollPane = mainTableComponent.getRecordTableScrollPane();
          if (scrollPane instanceof MsgActionTable) {
            MsgActionTable msgActionTable = (MsgActionTable) scrollPane;
            msgActionTable.setParentViewTable(parentViewTable);
          }
        }
      } else {
        if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
          //mainSplitPane = createSplitPane(getVisualsClassKeyName() + "_msg", JSplitPane.VERTICAL_SPLIT, 0.33d);
          mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_" + mainTableComponent.getVisualsClassKeyName(), JSplitPane.VERTICAL_SPLIT, 0.33d);
        } else {
          //mainSplitPane = createSplitPane(getVisualsClassKeyName() + "_addr", JSplitPane.HORIZONTAL_SPLIT, 0.5d);
          mainSplitPane = createSplitPane(TableComponent.visualsClassKeyName + "_" + mainTableComponent.getVisualsClassKeyName(), JSplitPane.HORIZONTAL_SPLIT, 0.5d);
        }
        mainTableComponent.addPreviewComponent(mainSplitPane, msgPreviewPanel);
        mainComponent = mainTableComponent;
      }
    }
    // else posting/chatting folder
    else {
      boolean isChatting = folderPair.getFolderRecord().isChatting();
      // chatting folder
      if (isChatting) {
        mainTableComponent = new ChatTableComponent();
        MsgComposePanel msgComposePanel = new MsgComposePanel(folderPair, MsgDataRecord.OBJ_TYPE_MSG, true);
        mainSplitPane = createSplitPane(mainTableComponent, msgComposePanel, "_chat", JSplitPane.VERTICAL_SPLIT, 0.75d);
        mainComponent = mainSplitPane;
      }
      // posting folder
      else {
        mainTableComponent = new PostTableComponent();
        mainComponent = mainTableComponent;
      }
    }

    if (initialData != null) {
      // if this table is not to fetch messages then set filter to initial msgs only, otherwise filter to folder
      if (!isInitDataModel) {
        if (folderPair != null)
          mainTableComponent.getRecordTableScrollPane().getTableModel().setParentFolderPair(folderPair);
        mainTableComponent.getRecordTableScrollPane().getTableModel().setFilter(new MsgFilter(RecordUtils.getIDs(initialData), initialData[0].ownerObjType, MsgLinkRecord.getOwnerObjIDs(initialData, initialData[0].ownerObjType.shortValue())));
      } else if (folderPair != null) {
        mainTableComponent.getRecordTableScrollPane().getTableModel().setFilter(new MsgFilter(Record.RECORD_TYPE_FOLDER, folderPair.getId()));
      }
      mainTableComponent.getRecordTableScrollPane().getTableModel().setData(initialData);
      mainTableComponent.getRecordTableScrollPane().getJSortedTable().selectAll();
      mainTableComponent.setTitle(parent);
      if (initialData.length > 1 && mainSplitPane != null) {
        mainSplitPane.setDividerLocation(100);
      }
      if (msgPreviewMode) {
        RecordTableScrollPane pane = mainTableComponent.getRecordTableScrollPane();
        if (pane instanceof RecordActionTable) {
          ((RecordActionTable) pane).setEnabledFolderNewItemsNotify(false);
        }
      }
    }
    if (isInitDataModel && folderPair != null) {
      RecordTableModel model = null;
      if (initialData != null && (model = mainTableComponent.getRecordTableScrollPane().getTableModel()) instanceof MsgTableModel) {
        // add to the table only so we don't loose the selection...
        model.setParentFolderPair(folderPair);
        ((MsgTableModel) model).refreshData(true);
      } else {
        mainTableComponent.initData(folderPair.getId());
      }
    }

    this.getContentPane().add(mainComponent, BorderLayout.CENTER);
    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(MsgTableFrame.class);

  }

  private JSplitPane createSplitPane(String propertyName, int orientation, double resizeWeight) {
    return TableComponent.createSplitPane(propertyName, orientation, resizeWeight);
  }

  private JSplitPane createSplitPane(RecordTableComponent recordTableComp, JComponent viewer, String propertyNamePrefix, int orientation, double resizeWeight) {
    return TableComponent.createSplitPane(getVisualsClassKeyName() + propertyNamePrefix, recordTableComp, viewer, orientation, resizeWeight);
  }

  public RecordTableComponent getTableComponent() {
    return mainTableComponent;
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
   */
  public void disposeObj() {
    if (mainTableComponent != null) 
      mainTableComponent.removeRecordListeners();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}