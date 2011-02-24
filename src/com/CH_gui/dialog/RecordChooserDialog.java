/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.fileTable.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;
import com.CH_gui.util.VisualsSavable;
import com.CH_gui.util.GeneralDialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.28 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecordChooserDialog extends GeneralDialog implements VisualsSavable, ObjectsProviderUpdaterI {

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 3;

  private JButton jOk;
  //private JButton jReset;
  private JButton jRemoveAll;
  private JButton jRemove;
  private JList jList;
  private JSplitPane hSplit;
  private JSplitPane hSplit2;

  private Object[] selectedObjects; // all selected Objects
  private ListUpdatableI updatable;

  private FolderTreeComponent folderTreeComponent;
  private TableComponent tableComponent;

  private Object[] initialSelectedObjects;

  private JFileChooser jFileChooser; // component to show local files from TableComponent
  private LocalFileSelectionListener localFileSelectionListener;

  private SelectedListSelectionListener selectedListSelectionListener;

  private boolean returnValuesSet = false;

  /** Creates new RecordChooserDialog */
  public RecordChooserDialog(Dialog owner, String title, String mainLabel, short[] folderTypes, Object[] initialSelectedObjects) {
    super(owner, title);
    constructDialog(owner, mainLabel, folderTypes, initialSelectedObjects);
  }

  /** Creates new RecordChooserDialog */
  public RecordChooserDialog(Frame owner, String title, String mainLabel, short[] folderTypes, Object[] initialSelectedObjects) {
    super(owner, title);
    constructDialog(owner, mainLabel, folderTypes, initialSelectedObjects);
  }

  private void constructDialog(Component owner, String mainLabel, short[] folderTypes, Object[] initialSelectedObjects) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "constructDialog(Component owner, String mainLabel, short[] folderTypes, Object[] initialSelectedObjects)");
    if (trace != null) trace.args(owner, mainLabel, folderTypes, initialSelectedObjects);
    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel(mainLabel, folderTypes);
    this.initialSelectedObjects = initialSelectedObjects;

    // Table Component will instantiate Msg, Post and File Table Components.
    // When the Msg, Post and File TableComponents are already created, we can attach a selection listeners.

    if (ArrayUtils.find(folderTypes, FolderRecord.FILE_FOLDER) >= 0) {
      tableComponent.initFileTableComponent();
      tableComponent.getFileTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new FileRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.MESSAGE_FOLDER) >= 0) {
      tableComponent.initMsgTableComponent();
      tableComponent.getMsgTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
      tableComponent.initMsgInboxTableComponent();
      tableComponent.getMsgInboxTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
      tableComponent.initMsgSentTableComponent();
      tableComponent.getMsgSentTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
      tableComponent.initMsgSpamTableComponent();
      tableComponent.getMsgSpamTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
      tableComponent.initMsgDraftsTableComponent();
      tableComponent.getMsgDraftsTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.POSTING_FOLDER) >= 0) {
      tableComponent.initPostTableComponent();
      tableComponent.getPostTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.CHATTING_FOLDER) >= 0) {
      tableComponent.initChatTableComponent();
      tableComponent.getChatTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.ADDRESS_FOLDER) >= 0) {
      tableComponent.initAddressTableComponent();
      tableComponent.getAddressTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.WHITELIST_FOLDER) >= 0) {
      tableComponent.initWhiteListTableComponent();
      tableComponent.getWhiteListTableComponent().getRecordTableScrollPane().addRecordSelectionListener(new MsgRecordSelectionListener());
    }

    if (ArrayUtils.find(folderTypes, FolderRecord.LOCAL_FILES_FOLDER) >= 0) {
      if (!TableComponent.DEBUG__DISABLE_LOCAL_FILE_CHOOSER) {
        tableComponent.initLocalFileTableComponent();
        jFileChooser = tableComponent.getLocalFileTableComponent().getJFileChooser();
        localFileSelectionListener = new LocalFileSelectionListener();
        jFileChooser.addPropertyChangeListener(localFileSelectionListener);
      }
    }


    pressedReset(); // also sets enabled buttons;
    init(owner, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
    if (trace != null) trace.exit(RecordChooserDialog.class);
  }

  private JButton[] createButtons() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "createButtons()");
    JButton[] buttons = new JButton[4];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Done"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
        closeDialog();
      }
    });
    jOk = buttons[0];

    /*
    buttons[1] = new JMyButton("Reset");
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedReset();
      }
    });
    jReset = buttons[1];
    */
    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Remove_All"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRemoveAll();
      }
    });
    jRemoveAll = buttons[1];

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Remove"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRemove();
      }
    });
    jRemove = buttons[2];

    buttons[3] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[3].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
        closeDialog();
      }
    });

    if (trace != null) trace.exit(RecordChooserDialog.class, buttons);
    return buttons;
  }

  private JPanel createMainPanel(String mainLabel, short[] folderTypes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "createMainPanel(String mainLabel, short[] folderTypes)");
    if (trace != null) trace.args(mainLabel, folderTypes);
    JPanel panel = new JPanel();

    // Initial tree is filled with file folders and file folder filter is set for the updates.
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderPair[] folderPairs = null;


    // add required types
    for (int i=0; i<folderTypes.length; i++) {
      FolderPair[] nextFolderPairs = cache.getFolderPairsMyOfType(folderTypes[i], true);
      folderPairs = (FolderPair[]) ArrayUtils.concatinate(folderPairs, nextFolderPairs);
    }


    // create the tree
    folderTreeComponent = new FolderTreeComponent(false, new FolderFilter(folderTypes), folderPairs, false);
    folderTreeComponent.setBorder(new EmptyBorder(0,0,0,0));
    folderTreeComponent.setPreferredSize(new Dimension(200, 300));
    // create the tables holder
    tableComponent = new TableComponent(getVisualsClassKeyName(), true, false, true);
    tableComponent.setBorder(new EmptyBorder(0,0,0,0));
    // connect the tree with tables
    folderTreeComponent.addTreeSelectionListener(tableComponent);
    tableComponent.addFolderSelectionListener(folderTreeComponent);

    jList = new JList(new DefaultListModel());
    jList.setBorder(new EmptyBorder(0,0,0,0));
    jList.setCellRenderer(new ListRenderer());
    selectedListSelectionListener = new SelectedListSelectionListener();
    jList.getSelectionModel().addListSelectionListener(selectedListSelectionListener);
    JScrollPane jListPane = new JScrollPane(jList);
    jListPane.setPreferredSize(new Dimension(150, 300));


    JPanel jTreePanel = new JPanel();
    jTreePanel.setBorder(new EmptyBorder(0,0,0,0));
    jTreePanel.setLayout(new GridBagLayout());
    jTreePanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_From_Folder")), new GridBagConstraints(0, 0, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jTreePanel.add(new JSeparator(), new GridBagConstraints(0, 1, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jTreePanel.add(folderTreeComponent, new GridBagConstraints(0, 2, 1, 1, 10, 10, 
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    JPanel jTablePanel = new JPanel();
    jTablePanel.setBorder(new EmptyBorder(0,0,0,0));
    jTablePanel.setLayout(new GridBagLayout());
    jTablePanel.add(new JMyLabel(mainLabel), new GridBagConstraints(0, 0, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jTablePanel.add(new JSeparator(), new GridBagConstraints(0, 1, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jTablePanel.add(tableComponent, new GridBagConstraints(0, 2, 1, 1, 10, 10, 
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));


    JPanel jListPanel = new JPanel();
    jListPanel.setBorder(new EmptyBorder(0,0,0,0));
    jListPanel.setLayout(new GridBagLayout());
    jListPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Current_choices")), new GridBagConstraints(0, 0, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jListPanel.add(new JSeparator(), new GridBagConstraints(0, 1, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jListPanel.add(jListPane, new GridBagConstraints(0, 2, 1, 1, 10, 10, 
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    hSplit = new JMySplitPane(getVisualsClassKeyName() + "_hSplit1", JSplitPane.HORIZONTAL_SPLIT, jTreePanel, jTablePanel, 0.0d);
    hSplit2 = new JMySplitPane(getVisualsClassKeyName() + "_hSplit2", JSplitPane.HORIZONTAL_SPLIT, hSplit, jListPanel, 1.0d);

    panel.setLayout(new GridBagLayout());
    panel.add(hSplit2, new GridBagConstraints(0, 0, 1, 1, 10, 10, 
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    if (trace != null) trace.exit(RecordChooserDialog.class, panel);
    return panel;
  }


  private void fileSelectionChanged() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "fileSelectionChanged()");
    RecordTableScrollPane recordTableScrollPane = tableComponent.getFileTableComponent().getRecordTableScrollPane();
    if (recordTableScrollPane instanceof FileActionTable) {
      FileActionTable fileActionTable = (FileActionTable) recordTableScrollPane;
      FileLinkRecord[] selectedLinks = (FileLinkRecord[]) fileActionTable.getSelectedInstancesOf(FileLinkRecord.class);

      if (selectedLinks != null) {
        DefaultListModel listModel = (DefaultListModel) jList.getModel();
        for (int i=0; i<selectedLinks.length; i++) {
          if (!listModel.contains(selectedLinks[i]))
            listModel.addElement(selectedLinks[i]);
        }
      }
    }
    else {
      throw new IllegalStateException("FileActionTable was expected, but found " + recordTableScrollPane.getClass());
    }
    setEnabledButtons();

    if (trace != null) trace.exit(RecordChooserDialog.class);
  }


  /**
   * Typically the only objects accepted are Records and Files
   */
  private void addObjectsToList(Object[] objs) {
    if (objs != null) {
      DefaultListModel listModel = (DefaultListModel) jList.getModel();
      for (int i=0; i<objs.length; i++)
        if (!listModel.contains(objs[i]))
          listModel.addElement(objs[i]);
    }
  }


  private void setEnabledButtons() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "setEnabledButtons()");
    jOk.setEnabled(jList.getModel().getSize() > 0 || 
                  (initialSelectedObjects != null && initialSelectedObjects.length > 0));
    int[] selected = jList.getSelectedIndices();
    jRemove.setEnabled(selected != null && selected.length > 0);
    jRemoveAll.setEnabled(jList.getModel().getSize() > 0);
    if (trace != null) trace.exit(RecordChooserDialog.class);
  }


  private void pressedOK() {
    // seperate all selected objects into distinct type lists
    DefaultListModel listModel = (DefaultListModel) jList.getModel();
    Object[] objs = listModel.toArray();
    if (objs != null && objs.length > 0) {
      selectedObjects = objs;
    }
    returnValuesSet = true;
  }


  private void pressedReset() {
    DefaultListModel listModel = (DefaultListModel) jList.getModel();
    listModel.removeAllElements();
    if (initialSelectedObjects != null) {
      for (int i=0; i<initialSelectedObjects.length; i++) {
        listModel.addElement(initialSelectedObjects[i]);
      }
    }
    setEnabledButtons();
  }


  private void pressedRemove() {
    DefaultListModel listModel = (DefaultListModel) jList.getModel();
    int size = listModel.getSize();
    for (int i=size-1; i>=0; i--) {
      if (jList.isSelectedIndex(i))
        listModel.removeElementAt(i);
    }
    setEnabledButtons();
  }

  private void pressedRemoveAll() {
    DefaultListModel listModel = (DefaultListModel) jList.getModel();
    int size = listModel.getSize();
    for (int i=size-1; i>=0; i--) {
      listModel.removeElementAt(i);
    }
    setEnabledButtons();
  }


  /** Pressing cancel is just like reseting the choices and pressing OK
   */
  private void pressedCancel() {
    pressedReset();
    pressedOK();
  }

  public Object[] getSelectedObjects() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordChooserDialog.class, "getSelectedObjects()");
    if (trace != null) trace.exit(RecordChooserDialog.class, selectedObjects);
    return selectedObjects;
  }



  public void closeDialog() {
    if (!returnValuesSet) {
      pressedReset();
      pressedOK();
    }
    // provide selection to the listener
    if (updatable != null) updatable.update(selectedObjects);
    // dissmount listeners and alike
    if (jFileChooser != null && localFileSelectionListener != null) {
      jFileChooser.removePropertyChangeListener(localFileSelectionListener);
      localFileSelectionListener = null;
    }
    if (tableComponent != null) {
      tableComponent.disposeObj();
    }
    if (folderTreeComponent != null) {
      folderTreeComponent.disposeObj();
    }
    if (selectedListSelectionListener != null) {
      jList.getSelectionModel().removeListSelectionListener(selectedListSelectionListener);
      selectedListSelectionListener = null;
    }
    if (tableComponent != null && folderTreeComponent != null) {
      folderTreeComponent.removeTreeSelectionListener(tableComponent);
    }
    if (updatable != null) {
      updatable = null;
    }
    super.closeDialog();
  }

  private class FileRecordSelectionListener implements RecordSelectionListener {
    public void recordSelectionChanged(RecordSelectionEvent recordSelectionEvent) {
      fileSelectionChanged();
    }
  }

  private class MsgRecordSelectionListener implements RecordSelectionListener {
    public void recordSelectionChanged(RecordSelectionEvent recordSelectionEvent) {
      addObjectsToList(recordSelectionEvent.getSelectedRecords());
      setEnabledButtons();
    }
  }

  private class LocalFileSelectionListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent event) {
      String changeName = event.getPropertyName();
      if (changeName.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
        File[] files = (File[]) jFileChooser.getSelectedFiles();
        ArrayList normalFilesL = new ArrayList();
        if (files != null) {
          for (int i=0; i<files.length; i++) {
            if (files[i].isFile()) {
              normalFilesL.add(new File(files[i].getAbsolutePath()));
            }
          }
          // set new Default Upload Directory to the one sourced last
          UploadUtilities.setDefaultSourceDir(jFileChooser.getCurrentDirectory());
        }
        if (normalFilesL.size() > 0) {
          File[] normalFiles = new File[normalFilesL.size()];
          normalFilesL.toArray(normalFiles);
          addObjectsToList(normalFiles);
          setEnabledButtons();
        }
      }
    }
  }

  private class SelectedListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      setEnabledButtons();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "RecordChooserDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

  /*************************************************************************
  *** O b j e c t s P r o v i d e r U p d a t e r I    interface methods ***
  *************************************************************************/

  public Object[] provide(Object args) {
    return selectedObjects;
  }

  public Object[] provide(Object args, ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
      return selectedObjects;
    } else {
      throw new IllegalStateException("Already registered updatable object.");
    }
  }

  public void registerForUpdates(ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
    } else {
      throw new IllegalStateException("Already registered for updates.");
    }
  }

  /*****************************************************
  *** D i s p o s a b l e O b j    interface methods ***
  *****************************************************/
  public void disposeObj() {
  }

}