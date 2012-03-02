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

package com.CH_gui.localFileTable;

import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_co.service.records.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_gui.action.*;
import com.CH_gui.dialog.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.menuing.*;
import com.CH_gui.msgTable.*;
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
 * <b>$Revision: 1.30 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class DNDActionFileChooser extends DNDFileChooser implements ActionProducerI, VisualsSavable {

  private Action[] actions;

  private static final int REFRESH_ACTION = 0;
  private static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 1;
  private static final int UPLOAD_ACTION = 2;
  private static final int WIPE_ACTION = 3;


  private int leadingActionId = Actions.LEADING_ACTION_ID_LOCALFILE_ACTION_TABLE;
  private int leadingActionIdFiles = Actions.LEADING_ACTION_ID_FILE_ACTION_TABLE;
  private int leadingActionIdMsgs = Actions.LEADING_ACTION_ID_MSG_ACTION_TABLE;

  private String propertyName;

  /** Creates new DNDActionFileChooser */
  public DNDActionFileChooser(String propertyName) {
    this(new File(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(visualsClassKeyName, null, propertyName), "")));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDActionFileChooser.class, "DNDActionFileChooser(String propertyName)");
    if (trace != null) trace.args(propertyName);
    this.propertyName = propertyName;
    if (trace != null) trace.exit(DNDActionFileChooser.class);
  }
  public DNDActionFileChooser(File currentDirectory) {
    super(currentDirectory != null && currentDirectory.exists() ? currentDirectory : null);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDActionFileChooser.class, "DNDActionFileChooser(File currentDirectory)");
    if (trace != null) trace.args(currentDirectory);

    assignMousePopupListeners(this);
    addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, new PropertyChangeListener() {
      // This method gets called when a bound property is changed.
      public void propertyChange(PropertyChangeEvent evt) {
        setEnabledActions();
      }
    });

    if (trace != null) trace.exit(DNDActionFileChooser.class);
  }
  private void assignMousePopupListeners(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDFileChooser.class, "assignDropAndDragComponents(Component c)");
    if (trace != null) trace.args(c);

    if (c != null &&
          (
            c instanceof JList ||
            c instanceof JViewport
          )
        )
    {
      if (trace != null) trace.data(10, "assign mouse popup listener to", c);
      c.addMouseListener(new PopupMouseAdapter(c, this));
    }
    if (c instanceof Container) {
      Container cont = (Container) c;
      Component[] cc = cont.getComponents();
      if (cc != null) {
        for (int i=0; i<cc.length; i++)
          assignMousePopupListeners(cc[i]);
      }
    }
    if (trace != null) trace.exit(DNDFileChooser.class);
  }


  private void initActions() {
    actions = new Action[4];
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionIdMsgs + MsgActionTable.REFRESH_ACTION);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionIdFiles + FileActionTable.OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[UPLOAD_ACTION] = new UploadAction(leadingActionId + UPLOAD_ACTION);
    actions[WIPE_ACTION] = new WipeAction(leadingActionIdMsgs + MsgActionTable.DELETE_ACTION);
    // do not 'disable' (not in Window yet) since in the record chooser dialog, the actions would never become enabled.
    //setEnabledActions();
  }
  public Action getRefreshAction() {
    if (actions == null) initActions();
    return actions[REFRESH_ACTION];
  }
  public Action getCloneAction() {
    if (actions == null) initActions();
    return actions[OPEN_IN_SEPERATE_WINDOW_ACTION];
  }

  /**
   * Refresh File List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Files"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_File_List_from_local_file_system."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      rescanCurrentDirectory();
    }
  }

  /**
   * Open in seperate window
   */
  private static class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_File_View"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_file_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new LocalFileTableFrame("Browse");
    }
  }

  /**
   * Securely wipe the contents of selected files and directories.
   */
  private class WipeAction extends AbstractActionTraced {
    public WipeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Wipe_File"), Images.get(ImageNums.FILE_REMOVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Securely_and_permanently_wipe_selected_file."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_REMOVE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Wipe_File"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      File[] files = getSelectedFiles();
      if (files != null && files.length > 0) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JCheckBox[] jFiles = new JCheckBox[files.length];
        JLabel q = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_securely_and_permanently_wipe_the_selected_files_from_your_local_file_system?"));
        panel.add(q, new GridBagConstraints(0, 0, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(new EmptyBorder(0,0,0,0));
        innerPanel.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(innerPanel);
        panel.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 10, 10,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
        for (int i=0; i<files.length; i++) {
          File f = files[i];
          Icon icon = ListRenderer.getRenderedIcon(f);
          String text = ListRenderer.getRenderedText(f);
          jFiles[i] = new JMyCheckBox();
          jFiles[i].setSelected(true);
          JLabel label = new JMyLabel(text);
          label.setHorizontalAlignment(SwingConstants.LEFT);
          label.setIcon(icon);
          innerPanel.add(jFiles[i], new GridBagConstraints(0, i, 1, 1, 0, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 2, 1), 0, 0));
          innerPanel.add(label, new GridBagConstraints(1, i, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 1, 2, 5), 0, 0));
        }

        // show question dialog
        String title = com.CH_gui.lang.Lang.rb.getString("title_File_Wipe");
        boolean option = MessageDialog.showDialogYesNo(DNDActionFileChooser.this, panel, title);

        if (option == true) {
          // gather all chosen files
          Vector filesToWipeV = new Vector();
          for (int i=0; i<jFiles.length; i++) {
            if (jFiles[i].isSelected()) {
              filesToWipeV.addElement(files[i]);
            }
          }
          if (filesToWipeV.size() > 0) {
            File[] filesToWipe = new File[filesToWipeV.size()];
            filesToWipeV.toArray(filesToWipe);
            setSelectedFiles(null);
            new WipingThread(filesToWipe, DNDActionFileChooser.this).start();
          }
        }

      } // end if any files selected
    } // end actionPerformed
    private void updateText(int countSelectedFiles) {
      if (countSelectedFiles > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Wipe_Files_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Securely_and_permanently_wipe_selected_files."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Wipe_File_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Securely_and_permanently_wipe_selected_file."));
      }
    }
  }


  private class UploadAction extends AbstractActionTraced {
    public UploadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Upload_File_..."), Images.get(ImageNums.EXPORT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Upload"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.EXPORT24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Upload"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      File[] files = getSelectedFiles();
      if (files != null && files.length > 0) {
        boolean anyDirs = false;
        boolean anyFiles = false;
        for (int i=0; i<files.length; i++) {
          if (files[i].isDirectory())
            anyDirs = true;
          if (files[i].isFile())
            anyFiles = true;
        }
        FolderPair chosenFolderPair = getUploadDestination(anyDirs, anyFiles);
        if (chosenFolderPair != null) {
          UploadUtilities.uploadFilesStartCoordinator(files, chosenFolderPair.getFolderShareRecord(), MainFrame.getServerInterfaceLayer());
        }
      }
    }
    private void updateText(int countSelectedFiles) {
      if (countSelectedFiles > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Upload_Files_..."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Upload_File_..."));
      }
    }
  }


  /**
   * Upload dialog and get the chosen destination FolderPair.
   */
  private FolderPair getUploadDestination(boolean forCreateFolder, boolean forFileUpload) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDActionFileChooser.class, "getUploadDestination(boolean forCreateFolder, boolean forFileUpload)");
    if (trace != null) trace.args(forCreateFolder);
    if (trace != null) trace.args(forFileUpload);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheUtilities.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) FolderFilter.MOVE_FOLDER.filterInclude(allFolderPairs);

    Window w = SwingUtilities.windowForComponent(this);

    String title = com.CH_gui.lang.Lang.rb.getString("title_Upload_to_Folder");
    FolderPair[] folderPairs = null;
    if (forFileUpload)
      folderPairs = (FolderPair[]) FolderFilter.NON_FILE_FOLDERS.filterInclude(allFolderPairs);
    // If we are not moving folders because there are no folder pairs selected, than desendants are OK.
    boolean isDescendantOk = true;

    Long myFilesFolderId = cache.getUserRecord().fileFolderId;
    FolderPair myFiles = new FolderPair(cache.getFolderShareRecordMy(myFilesFolderId, false), cache.getFolderRecord(myFilesFolderId));
    Move_NewFld_Dialog d = null;
    if (w instanceof Frame) d = new Move_NewFld_Dialog((Frame) w, allFolderPairs, folderPairs, myFiles, title, isDescendantOk, cache);
    else if (w instanceof Dialog) d = new Move_NewFld_Dialog((Dialog) w, allFolderPairs, folderPairs, myFiles, title, isDescendantOk, cache);

    FolderPair chosenPair = null;
    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    if (trace != null) trace.exit(DNDActionFileChooser.class, chosenPair);
    return chosenPair;
  }

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I
  /****************************************************************************/

  /**
   * @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    if (actions == null) initActions();
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
    if (actions == null) initActions();
    File[] selectedFiles = getSelectedFiles();
    int countDirs = 0;
    int countFiles = 0;
    boolean anyDirs = false;
    boolean anyFiles = false;

    for (int i=0; i<selectedFiles.length; i++) {
      File f = selectedFiles[i];
      if (f.isFile()) {
        anyFiles = true;
        countFiles ++;
      }
      if (f.isDirectory()) {
        anyDirs = true;
        countDirs ++;
      }
    }

    Window w = SwingUtilities.windowForComponent(this);
    actions[REFRESH_ACTION].setEnabled(w != null);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);
    actions[WIPE_ACTION].setEnabled(countFiles + countDirs > 0);
    actions[UPLOAD_ACTION].setEnabled(countFiles + countDirs > 0);

    WipeAction wipeAction = (WipeAction) actions[WIPE_ACTION];
    UploadAction uploadAction = (UploadAction) actions[UPLOAD_ACTION];

    wipeAction.updateText(countFiles + countDirs);
    uploadAction.updateText(countFiles + countDirs);
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDFileChooser.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append(getCurrentDirectory().getAbsolutePath());

    String rc = visuals.toString();
    if (trace != null) trace.exit(DNDFileChooser.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDFileChooser.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      File dir = new File(visuals);
      setCurrentDirectory(dir);
    } catch (Throwable t) {
      if (trace != null) trace.exception(DNDFileChooser.class, 100, t);
      // reset the properties since they are corrupted
      GlobalProperties.resetMyAndGlobalProperties();
    }

    if (trace != null) trace.exit(DNDFileChooser.class);
  }

  public String getExtension() {
    return propertyName;
  }
  public static final String visualsClassKeyName = "DNDActionFileChooser";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }

}