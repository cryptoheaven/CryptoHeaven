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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.CH_gui.fileTable.*;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.list.*;
import com.CH_gui.msgTable.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.cryptx.*;
import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.frame.MsgPreviewFrame;

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
 * <b>$Revision: 1.31 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SaveAttachmentsDialog extends GeneralDialog implements DragGestureListener {


  private static final int DEFAULT_DOWNLOAD_BUTTON_INDEX = 2;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 3;

  private Record[] paramAttachments;
  private MsgLinkRecord[] paramParentMsgLinkRecords;
  private Long[] parentMsgIDs; // set by the attachment fetcher

  private boolean constructMsgDest;
  private boolean constructFileDest;

  private FolderPair msgDestination;
  private FolderPair fileDestination;
  private File localFileDestination;

  private JList jList;
  private JScrollPane jListPane;

  private JLabel jMsgLabel;
  private JLabel jFileLabel;
  private JLabel jLocalFileLabel;

  private JLabel jMsgDestination;
  private JLabel jFileDestination;
  private JLabel jLocalFileDestination;
  private JButton jMsgBrowse;
  private JButton jFileBrowse;
  private JButton jLocalFileBorwse;
  private JButton jCopy;
  private JButton jOpen;
  private JButton jDownload;

  private static final String PROPERTY_NAME__MSG_DEST_FOLDER = SaveAttachmentsDialog.class.getName() + "_msgDestinationFolder";
  private static final String PROPERTY_NAME__FILE_DEST_FOLDER = SaveAttachmentsDialog.class.getName() + "_fileDestinationFolder";

  private static final String FETCHING_ATTACHMENTS = com.CH_gui.lang.Lang.rb.getString("Fetching_Attachments...");

  private MsgLinkListener msgLinkListener;
  private FileLinkListener fileLinkListener;



  /**
   * Creates new SaveAttachmentsDialog.
   * Displays the specified attachments.
   * If attachments array is null or empty, the attachments are fetched from
   * specified message parents.
   */
  public SaveAttachmentsDialog(Frame owner, Record[] attachments, MsgLinkRecord[] fromMsgLinkRecords) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Attachments"));
    constructDialog(owner, attachments, fromMsgLinkRecords);
  }
  /** Creates new SaveAttachmentsDialog */
  public SaveAttachmentsDialog(Dialog owner, Record[] attachments, MsgLinkRecord[] fromMsgLinkRecords) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Attachments"));
    constructDialog(owner, attachments, fromMsgLinkRecords);
  }

  /**
   * Creates new SaveAttachmentsDialog.
   * The attachments array is null and the attachments are fetched from
   * specified message parents.
   */
  public SaveAttachmentsDialog(Frame owner, MsgLinkRecord[] msgLinksToFetchAttachmentsFrom) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Attachments"));
    constructDialog(owner, null, msgLinksToFetchAttachmentsFrom);
  }
  /** Creates new SaveAttachmentsDialog */
  public SaveAttachmentsDialog(Dialog owner, MsgLinkRecord[] msgLinksToFetchAttachmentsFrom) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Attachments"));
    constructDialog(owner, null, msgLinksToFetchAttachmentsFrom);
  }


  private void constructDialog(Component owner, Record[] attachments, MsgLinkRecord[] paramParentMsgLinkRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SaveAttachmentsDialog.class, "constructDialog(Component owner, Record[] attachments, MsgLinkRecord[] paramParentMsgLinkRecords)");
    if (trace != null) trace.args(owner, attachments, paramParentMsgLinkRecords);

    this.paramAttachments = attachments;
    this.paramParentMsgLinkRecords = paramParentMsgLinkRecords;

    this.constructMsgDest = true;
    this.constructFileDest = true;

    initializeComponents();
    JPanel panel = createMainPanel();
    JButton[] jButtons = createButtons();
    setEnabledButtons();

    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(jList, DnDConstants.ACTION_COPY, this);
    dragSource.createDefaultDragGestureRecognizer(jListPane.getViewport(), DnDConstants.ACTION_COPY, this);

    super.init(owner, jButtons, panel, DEFAULT_DOWNLOAD_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(SaveAttachmentsDialog.class);
  }


  private void initializeComponents() {

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long defaultMsgFolderId = cache.getUserRecord().msgFolderId;
    Long defaultFileFolderId = cache.getUserRecord().fileFolderId;

    Long tryMsgFolderId = Long.valueOf(GlobalProperties.getProperty(PROPERTY_NAME__MSG_DEST_FOLDER, defaultMsgFolderId.toString()));
    Long tryFileFolderId = Long.valueOf(GlobalProperties.getProperty(PROPERTY_NAME__FILE_DEST_FOLDER, defaultFileFolderId.toString()));

    msgDestination = getFolderPair(tryMsgFolderId);
    if (msgDestination == null)
      msgDestination = getFolderPair(defaultMsgFolderId);

    fileDestination = getFolderPair(tryFileFolderId);
    if (fileDestination == null)
      fileDestination = getFolderPair(defaultFileFolderId);

    localFileDestination = DownloadUtilities.getDefaultDestDir();

    jMsgDestination = new JMyLabel(msgDestination.getMyName());
    jMsgDestination.setIcon(msgDestination.getIcon());

    jFileDestination = new JMyLabel(fileDestination.getMyName());
    jFileDestination.setIcon(fileDestination.getIcon());

    jLocalFileDestination = new JMyLabel(localFileDestination.getAbsolutePath());
    jLocalFileDestination.setIcon(Images.get(ImageNums.FLD_CLOSED16));

    jMsgLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Copy_Messages_to"), Images.get(ImageNums.COPY16), JLabel.LEADING);
    jFileLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Copy_Files_to"), Images.get(ImageNums.COPY16), JLabel.LEADING);
    jLocalFileLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Download_to"), Images.get(ImageNums.IMPORT_FILE16), JLabel.LEADING);

    jMsgBrowse = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Browse_..."));
    jMsgBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedMsgBrowse();
      }
    });

    jFileBrowse = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Browse_..."));
    jFileBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedFileBrowse();
      }
    });

    jLocalFileBorwse = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Browse_..."));
    jLocalFileBorwse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedLocalFileBrowse();
      }
    });

    jCopy = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Copy"));
    jCopy.setIcon(Images.get(ImageNums.COPY16));
    jCopy.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCopy();
      }
    });

    jOpen = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Open"));
    jOpen.setIcon(Images.get(ImageNums.CLONE16));
    jOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOpen();
      }
    });

    jDownload = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Download"));
    jDownload.setIcon(Images.get(ImageNums.IMPORT_FILE16));
    jDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedDownload();
      }
    });

    DefaultListModel listModel = new DefaultListModel();
    jList = new JList(listModel);
    jListPane = new JScrollPane(jList);
    if (paramAttachments != null && paramAttachments.length > 0) {
      for (int i=0; i<paramAttachments.length; i++)
        listModel.addElement(paramAttachments[i]);
      jList.setSelectionInterval(0, paramAttachments.length-1);
    }
    else {
      listModel.addElement(FETCHING_ATTACHMENTS);
      sendFetchAttachmentsRequest(paramParentMsgLinkRecords);
    }
    jList.setCellRenderer(new ListRenderer(true, false, false));
    jList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        setEnabledButtons();
      }
    });

  }


  private FolderPair getFolderPair(Long folderId) {
    FolderPair pair = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord fRec = cache.getFolderRecord(folderId);
    if (fRec != null) {
      FolderShareRecord sRec = cache.getFolderShareRecordMy(folderId, true);
      if (sRec != null)
        pair = new FolderPair(sRec, fRec);
    }
    return pair;
  }

  /**
   * @return the dialog 'Search' and 'Cancel' buttons
   */
  private JButton[] createButtons() {
    JButton[] buttons = new JButton[4];

    buttons[0] = jCopy;
    buttons[1] = jOpen;
    buttons[2] = jDownload;

    buttons[3] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[3].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }



  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());
    int posY = 0;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Select_Attachments")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jListPane, new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (constructMsgDest) {
      panel.add(jMsgLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      posY ++;

      panel.add(jMsgDestination, new GridBagConstraints(0, posY, 1, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      panel.add(jMsgBrowse, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (constructFileDest) {
      panel.add(jFileLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      posY ++;

      panel.add(jFileDestination, new GridBagConstraints(0, posY, 1, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      panel.add(jFileBrowse, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      posY ++;

      panel.add(jLocalFileLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      posY ++;

      panel.add(jLocalFileDestination, new GridBagConstraints(0, posY, 1, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      panel.add(jLocalFileBorwse, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      posY ++;
    }

    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }


  /**
   * Set button enablement as appropriate.
   */
  private void setEnabledButtons() {

    Object[] selections = jList.getSelectedValues();
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selections, MsgLinkRecord.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selections, FileLinkRecord.class);

    boolean selectedMsgs = mLinks != null && mLinks.length > 0;
    boolean selectedFiles = fLinks != null && fLinks.length > 0;

    jMsgLabel.setEnabled(selectedMsgs);
    jMsgDestination.setEnabled(selectedMsgs);
    jMsgBrowse.setEnabled(selectedMsgs);

    jFileLabel.setEnabled(selectedFiles);
    jFileDestination.setEnabled(selectedFiles);
    jFileBrowse.setEnabled(selectedFiles);

    jLocalFileLabel.setEnabled(selectedFiles || selectedMsgs);
    jLocalFileDestination.setEnabled(selectedFiles || selectedMsgs);
    jLocalFileBorwse.setEnabled(selectedFiles || selectedMsgs);

    jCopy.setEnabled(selectedFiles || selectedMsgs);
    jOpen.setEnabled(selectedFiles || selectedMsgs);
    jDownload.setEnabled(selectedFiles || selectedMsgs);
  }


  private void pressedMsgBrowse() {
    FolderPair fPair = getCopyDestination(FolderFilter.NON_MSG_FOLDERS, msgDestination);
    if (fPair != null) {
      msgDestination = fPair;
      GlobalProperties.setProperty(PROPERTY_NAME__MSG_DEST_FOLDER, msgDestination.getId().toString());
      jMsgDestination.setText(msgDestination.getMyName());
      jMsgDestination.setIcon(msgDestination.getIcon());
    }
  }

  private void pressedFileBrowse() {
    FolderPair fPair = getCopyDestination(FolderFilter.NON_FILE_FOLDERS, fileDestination);
    if (fPair != null) {
      fileDestination = fPair;
      GlobalProperties.setProperty(PROPERTY_NAME__FILE_DEST_FOLDER, fileDestination.getId().toString());
      jFileDestination.setText(fileDestination.getMyName());
      jFileDestination.setIcon(fileDestination.getIcon());
    }
  }

  private void pressedLocalFileBrowse() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "pressedLocalFileBrowse()");

    // Native FileDialog is not customizable enough for this purpose...
//    FileDialog fd = new FileDialog(this, "Select Download Destination", FileDialog.SAVE);
//    fd.setVisible(true);
//    File destDir = new File(fd.getDirectory());

    FileChooser fileChooser = FileChooser.makeNew(this, true, localFileDestination,
      com.CH_gui.lang.Lang.rb.getString("title_Select_Download_Destination"),
      com.CH_gui.lang.Lang.rb.getString("button_Select"), new Character('S'),
      com.CH_gui.lang.Lang.rb.getString("actionTip_Approve_the_current_directory_selection."));
    File destDir = fileChooser.getSelectedDir();

    if (destDir != null && destDir.isDirectory()) {
      localFileDestination = destDir;
      DownloadUtilities.setDefaultDestDir(destDir);
      jLocalFileDestination.setText(localFileDestination.getAbsolutePath());
    }

    if (trace != null) trace.exit(getClass());
  }

  private void pressedOpen() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "pressedOpen()");

    Object[] selections = jList.getSelectedValues();
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selections, MsgLinkRecord.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selections, FileLinkRecord.class);

    if (mLinks.length > 0) {
      new MsgPreviewFrame(paramParentMsgLinkRecords[0], mLinks);
    }
    for (int i=0; i<fLinks.length; i++) {
      DownloadUtilities.downloadAndOpen(fLinks[i], paramParentMsgLinkRecords, MainFrame.getServerInterfaceLayer(), true, false);
    }
    closeDialog();

    if (trace != null) trace.exit(getClass());
  }

  private void pressedDownload() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "pressedDownload()");

    Object[] selections = jList.getSelectedValues();
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selections, MsgLinkRecord.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selections, FileLinkRecord.class);
    Record[] recs = (Record[]) ArrayUtils.concatinate(mLinks, fLinks, Record.class);

    new DownloadUtilities.DownloadCoordinator(recs, paramParentMsgLinkRecords, localFileDestination, MainFrame.getServerInterfaceLayer()).start();
    closeDialog();

    if (trace != null) trace.exit(getClass());
  }

  private void pressedCopy() {
    Thread th = new Thread("Attachment Copier") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        // change the priority of this thread to minimum
        setPriority(MIN_PRIORITY);

        Object[] selections = jList.getSelectedValues();
        MsgLinkRecord[] mLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selections, MsgLinkRecord.class);
        FileLinkRecord[] fLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selections, FileLinkRecord.class);

        boolean selectedMsgs = mLinks != null && mLinks.length > 0;
        boolean selectedFiles = fLinks != null && fLinks.length > 0;

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

        // if parent messages don't reside in folders, don't specify any shares, server will find access path from parentLinkIDs
        FolderShareRecord[] fromShares = cache.getFolderSharesMyForFolders(MsgLinkRecord.getOwnerObjIDs(paramParentMsgLinkRecords, Record.RECORD_TYPE_FOLDER), true);
        //FolderShareRecord[] fromShares = cache.getFolderShareRecordsMyRootsForMsgs(paramParentMsgLinkRecords);

        if (selectedMsgs) {
          Msg_MoveCopy_Rq request = new Msg_MoveCopy_Rq();

          request.toShareId = msgDestination.getFolderShareRecord().shareId;
          request.fromMsgLinkIDs = RecordUtils.getIDs(paramParentMsgLinkRecords);

          if (fromShares != null && fromShares.length > 0)
            request.fromShareIDs = RecordUtils.getIDs(fromShares);
          else
            request.fromShareIDs = new Long[0];

          MsgLinkRecord[] clonedLinks = (MsgLinkRecord[]) RecordUtils.cloneRecords(mLinks);
          // give the new encrypted symmetric keys for the new destination folder
          BASymmetricKey destinationSymKey = msgDestination.getFolderShareRecord().getSymmetricKey();
          for (int i=0; i<clonedLinks.length; i++) {
            // check if we have access to the message's content, if not, leave it in recryption pending state for the original user recipient to change from asymetric to symetric
            if (clonedLinks[i].getSymmetricKey() != null)
              clonedLinks[i].seal(destinationSymKey);
          }
          request.msgLinkRecords = clonedLinks;

          serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.MSG_Q_SAVE_MSG_ATT, request));
        } // if selectedMsgs

        if (selectedFiles) {
          File_MoveCopy_Rq request = new File_MoveCopy_Rq();

          request.toShareId = fileDestination.getFolderShareRecord().shareId;
          request.fromMsgLinkIDs = RecordUtils.getIDs(paramParentMsgLinkRecords);

          if (fromShares != null && fromShares.length > 0)
            request.fromShareIDs = RecordUtils.getIDs(fromShares);
          else
            request.fromShareIDs = new Long[0];

          FileLinkRecord[] clonedLinks = (FileLinkRecord[]) RecordUtils.cloneRecords(fLinks);
          // give the new encrypted symmetric keys for the new destination folder
          BASymmetricKey destinationSymKey = fileDestination.getFolderShareRecord().getSymmetricKey();
          for (int i=0; i<clonedLinks.length; i++) {
            clonedLinks[i].seal(destinationSymKey);
          }
          request.fileLinkRecords = clonedLinks;

          serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.FILE_Q_SAVE_MSG_FILE_ATT, request));
        }


        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    th.setDaemon(true);
    th.start();

    closeDialog();
  }

  private void pressedCancel() {
    closeDialog();
  }

  /**
   * Overwrite closeDialog to remove listeners on the cache.
   */
  public void closeDialog() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (msgLinkListener != null)
      cache.removeMsgLinkRecordListener(msgLinkListener);
    if (fileLinkListener != null)
      cache.removeFileLinkRecordListener(fileLinkListener);
    msgLinkListener = null;
    fileLinkListener = null;
    super.closeDialog();
  }


  /**
   * Show a Copy dialog and get the chosen destination FolderPair.
   */
  private FolderPair getCopyDestination(FolderFilter filter, FolderPair selectedFolder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SaveAttachmentsDialog.class, "getCopyDestination(filter, selectedFolder)");
    if (trace != null) trace.args(filter, selectedFolder);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheUtilities.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) FolderFilter.MOVE_FOLDER.filterInclude(allFolderPairs);

    // An invalid destinations are filtered out
    FolderPair[] forbidenPairs = (FolderPair[]) filter.filterInclude(allFolderPairs);

    // Since we are moving messages/postings only, not folders, descendant destination folders are always ok.
    boolean isDescendantOk = true;

    String title = com.CH_gui.lang.Lang.rb.getString("title_Copy_to_Folder");

    Move_NewFld_Dialog d = new Move_NewFld_Dialog(this, allFolderPairs, forbidenPairs, selectedFolder, title, isDescendantOk, cache, filter);

    FolderPair chosenPair = null;
    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    if (trace != null) trace.exit(SaveAttachmentsDialog.class, chosenPair);
    return chosenPair;
  }


  /**
   * Send a request to fetch msg link attachments and file link attachments.
   */
  private void sendFetchAttachmentsRequest(MsgLinkRecord[] parentMsgLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SaveAttachmentsDialog.class, "sendFetchAttachmentsRequest(MsgLinkRecord[] parentMsgLinks)");
    if (trace != null) trace.args(parentMsgLinks);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

    parentMsgLinks = (MsgLinkRecord[]) ArrayUtils.removeDuplicates(parentMsgLinks);
    MsgDataRecord[] parentDatas = (MsgDataRecord[]) ArrayUtils.removeDuplicates(cache.getMsgDataRecords(MsgLinkRecord.getMsgIDs(parentMsgLinks)));

    Long[] parentLinkIDs = RecordUtils.getIDs(parentMsgLinks);
    parentMsgIDs = (Long[]) ArrayUtils.removeDuplicates(RecordUtils.getIDs(parentDatas));


    // attach listeners if required
    if (msgLinkListener == null) {
      msgLinkListener = new MsgLinkListener();
      cache.addMsgLinkRecordListener(msgLinkListener);
    }
    if (fileLinkListener == null) {
      fileLinkListener = new FileLinkListener();
      cache.addFileLinkRecordListener(fileLinkListener);
    }


    // prepare requests
    Obj_IDs_Co request = null;
    int sumFiles = MsgDataRecord.sumAttachedFiles(parentDatas);
    int sumMsgs = MsgDataRecord.sumAttachedMsgs(parentDatas);
    if (sumFiles > 0 || sumMsgs > 0) {
      request = new Obj_IDs_Co();
      request.IDs = new Long[2][];
      request.IDs[0] = parentLinkIDs;
      // if parent messages don't reside in folders, don't specify any shares, server will find access path from parentLinkIDs
      Long[] folderIDs = MsgLinkRecord.getOwnerObjIDs(parentMsgLinks, Record.RECORD_TYPE_FOLDER);
      FolderShareRecord[] shareRecords = cache.getFolderSharesMyForFolders(folderIDs, true);
      //FolderShareRecord[] shareRecords = cache.getFolderShareRecordsMyRootsForMsgs(parentMsgLinks);
      request.IDs[1] = RecordUtils.getIDs(shareRecords);
    }

    // send requests
    if (sumFiles > 0)
      serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS, request));
    if (sumMsgs > 0)
      serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS, request));

    if (trace != null) trace.exit(SaveAttachmentsDialog.class);
  }


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE ****************************************/
  /****************************************************************************************/

  /** Listen on updates to the FileLinkRecords in the cache.
    * if the event happens, add file links.
    */
  private class FileLinkListener implements FileLinkRecordListener {
    public void fileLinkRecordUpdated(FileLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ListGUIUpdater(event));
    }
  }

  /** Listen on updates to the MsgLinkRecords in the cache.
    * if the event happens, add message links.
    */
  private class MsgLinkListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ListGUIUpdater(event));
    }
  }

  private class ListGUIUpdater implements Runnable {
    private RecordEvent event;
    public ListGUIUpdater(RecordEvent event) {
      this.event = event;
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ListGUIUpdater.class, "run()");

      listUpdate(event);

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ListGUIUpdater.class);
    }
  }

  /** Get the records and event type from the event and switch to appropriate methods
    * to set these records.  Removal is not supported here.
    */
  private void listUpdate(RecordEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SaveAttachmentsDialog.class, "listUpdate(RecordEvent event)");
    if (trace != null) trace.args(event);

    if (event.getEventType() == RecordEvent.SET) {
      Record[] records = event.getRecords();
      if (records != null) {

        // sort the attachment records for display
        if (records.length > 1)
          Arrays.sort(records, new ListComparator());

        DefaultListModel listModel = (DefaultListModel) jList.getModel();

        // remove the info message
        listModel.removeElement(FETCHING_ATTACHMENTS);

        for (int i=0; i<records.length; i++) {
          Record rec = records[i];
          boolean wanted = false;
          if (rec instanceof FileLinkRecord) {
            FileLinkRecord fRec = (FileLinkRecord) rec;
            if (fRec.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE && ArrayUtils.find(parentMsgIDs, fRec.ownerObjId) >= 0)
              wanted = true;
          }
          else if (rec instanceof MsgLinkRecord) {
            MsgLinkRecord mRec = (MsgLinkRecord) rec;
            if (mRec.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE && ArrayUtils.find(parentMsgIDs, mRec.ownerObjId) >= 0)
              wanted = true;
          }
          if (wanted && !listModel.contains(rec)) {
            int addedIndex = listModel.size();
            listModel.addElement(rec);
            jList.getSelectionModel().addSelectionInterval(addedIndex, addedIndex);
          }
        }
      }
    }

    if (trace != null) trace.exit(SaveAttachmentsDialog.class);
  }


  /****************************************************************
   * D R A G   G E S T U R E   L I S T E N E R   i n t e r f a c e
   ***************************************************************/
  public void dragGestureRecognized(DragGestureEvent event) {
    Object[] selections = jList.getSelectedValues();
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selections, MsgLinkRecord.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selections, FileLinkRecord.class);

    boolean selectedMsgs = mLinks != null && mLinks.length > 0;
    boolean selectedFiles = fLinks != null && fLinks.length > 0;

    if (selectedMsgs && !selectedFiles) {
      MsgDND_Transferable transferable = new MsgDND_Transferable(mLinks);
      event.getDragSource().startDrag(event, null, transferable, new MsgDND_DragSourceListener());
    }
    else if (!selectedMsgs && selectedFiles) {
      FileDND_Transferable transferable = new FileDND_Transferable(null, fLinks);
      event.getDragSource().startDrag(event, null, transferable, new FileDND_DragSourceListener());
    }
  }

}