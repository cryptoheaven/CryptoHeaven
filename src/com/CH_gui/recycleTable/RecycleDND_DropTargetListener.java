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

package com.CH_gui.recycleTable;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.FileFilter;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;

import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.ListSelectionModel;

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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class RecycleDND_DropTargetListener extends Object implements DropTargetListener {

  private RecycleActionTable recycleActionTable;

  private boolean originalSelectionSaved;
  private Record[] originallySelectedRecords;
  private Point lastPt;

  /** Creates new RecycleDND_DropTargetListener */
  protected RecycleDND_DropTargetListener(RecycleActionTable recycleActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleDND_DropTargetListener.class, "RecycleDND_DropTargetListener(RecycleActionTable recycleActionTable)");
    if (trace != null) trace.args(recycleActionTable);
    this.recycleActionTable = recycleActionTable;
    if (trace != null) trace.exit(RecycleDND_DropTargetListener.class);
  }

  public void dragEnter(DropTargetDragEvent event) {
    updateCursor(event);
  }
  public void dragOver(DropTargetDragEvent event) {
    Point pt = event.getLocation();
    if (lastPt == null || lastPt.x != pt.x || lastPt.y != pt.y) {
      lastPt = pt;
      updateCursor(event);
      highlightMouseOverFolder(event);
    }
  }
  private void updateCursor(DropTargetDragEvent event) {
    try {
      if (event.isDataFlavorSupported(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR) ||
          event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR) ||
          event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR)) {
        int sourceActions = event.getSourceActions();
        if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
          event.acceptDrag(DnDConstants.ACTION_MOVE);
        else if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
          event.acceptDrag(DnDConstants.ACTION_COPY);
      }
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
  }
  private void highlightMouseOverFolder(DropTargetDragEvent event) {
    // If dragging over a folder record, select the folder
    Point location = event.getLocation();
    JSortedTable jTable = recycleActionTable.getJSortedTable();
    int row = jTable.rowAtPoint(location);
    if (row >= 0) {
      int modelRow = jTable.convertMyRowIndexToModel(row);
      RecycleTableModel model = (RecycleTableModel) jTable.getRawModel();
      Record rowRec = model.getRowObject(modelRow);
      if (rowRec instanceof FolderPair) {
        if (!originalSelectionSaved) {
          originallySelectedRecords = recycleActionTable.getSelectedRecords();
          originalSelectionSaved = true;
        }
        jTable.getSelectionModel().setSelectionInterval(row, row);
      }
    }
  }
  private void restoreOriginalSelection() {
    if (originalSelectionSaved) {
      originalSelectionSaved = false;
      JSortedTable jTable = recycleActionTable.getJSortedTable();
      ListSelectionModel selectionModel = jTable.getSelectionModel();
      selectionModel.clearSelection();

      if (originallySelectedRecords != null) {
        RecordTableModel tableModel = recycleActionTable.getTableModel();
        for (int i=0; i<originallySelectedRecords.length; i++) {
          int row = tableModel.getRowForObject(originallySelectedRecords[i].getId()); // xxx can't use IDs because they may not be unique
          row = jTable.convertMyRowIndexToView(row);
          selectionModel.addSelectionInterval(row, row);
        }
      }
      originallySelectedRecords = null;
    }
  }
  public void dragExit(DropTargetEvent event) {
    restoreOriginalSelection();
  }
  public void drop(DropTargetDropEvent event) {
    try {
      Transferable tr = event.getTransferable();

      // get this table context's parent folder
      RecycleTableModel tableModel = (RecycleTableModel) recycleActionTable.getTableModel();
      FolderPair parentFolderPair = tableModel.getParentFolderPair();
      FolderRecord parentFolder = null;
      FolderShareRecord parentShare = null;
      if (parentFolderPair != null) {
        parentFolder = parentFolderPair.getFolderRecord();
        parentShare = parentFolderPair.getFolderShareRecord();
      }


      // get drop location, either parent or Folder under the pointer
      FolderRecord uploadFolderRec = parentFolder;
      FolderShareRecord uploadShareRec = parentShare;
      Point location = event.getLocation();
      JSortedTable jTable = recycleActionTable.getJSortedTable();
      int row = jTable.rowAtPoint(location);
      if (row >= 0) {
        int modelRow = jTable.convertMyRowIndexToModel(row);
        RecycleTableModel model = (RecycleTableModel) jTable.getRawModel();
        Record rowRec = model.getRowObject(modelRow);
        if (rowRec instanceof FolderPair) {
          FolderPair selectedPair = (FolderPair) rowRec;
          uploadFolderRec = selectedPair.getFolderRecord();
          uploadShareRec = selectedPair.getFolderShareRecord();
        }
      }


      // MOVEs from within the program
      if (tr.isDataFlavorSupported(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR)) {
        RecycleDND_TransferableData transferRecs = (RecycleDND_TransferableData) tr.getTransferData(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR);
        event.acceptDrop(DnDConstants.ACTION_MOVE);
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderPair moveToPair = CacheUtilities.convertRecordToPair(uploadShareRec);
        // move Files and Folders
        FileLinkRecord[] fLinks = null;
        if (transferRecs.recycleRecordIDs[2] != null)
          fLinks = cache.getFileLinkRecords(transferRecs.recycleRecordIDs[2]);
        else 
          fLinks = cache.getFileLinkRecords(transferRecs.recycleRecordIDs[1]);
        FileLinkRecord[] fLinksFiltered = (FileLinkRecord[]) new FileFilter(moveToPair.getId(), true).filterExclude(fLinks);
        FileActionTable.doMoveOrSaveAttachmentsAction(moveToPair, fLinksFiltered, CacheUtilities.convertRecordsToPairs(cache.getFolderRecords(transferRecs.recycleRecordIDs[0])));
        // move Msgs
        MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(transferRecs.recycleRecordIDs[3]);
        MsgLinkRecord[] mLinksFiltered = (MsgLinkRecord[]) new MsgFilter(Record.RECORD_TYPE_FOLDER, moveToPair.getId()).filterExclude(mLinks);
        MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(true, moveToPair, mLinksFiltered);
        event.getDropTargetContext().dropComplete(true);
      }

      // UPLOADS from local system
      else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && parentShare != null) {
        event.acceptDrop(DnDConstants.ACTION_COPY);

        boolean isFileFolderType = uploadFolderRec.isFileType();
        boolean isMsgFolderType = uploadFolderRec.isMsgType();

        boolean isFolderAccepted = isFileFolderType;
        boolean isFileAccepted = isFileFolderType || isMsgFolderType;

        List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
        Iterator iterator = fileList.iterator();
        Vector filesV = new Vector();
        while (iterator.hasNext()) {
          File file = (File) iterator.next();
          if ((isFolderAccepted && file.isDirectory()) || (isFileAccepted && file.isFile()))
            filesV.addElement(file);
        }
        if (filesV.size() > 0) {
          File[] files = new File[filesV.size()];
          filesV.toArray(files);

          if (isFileFolderType) {
            new UploadUtilities.UploadCoordinator(files, uploadShareRec, MainFrame.getServerInterfaceLayer(), true).start();
          } else if (isMsgFolderType) {
            new MessageFrame(new FolderPair[] { new FolderPair(uploadShareRec, uploadFolderRec) }, files);
          }
        }
      }

      // data flavor not supported
      else {
        event.rejectDrop();
      }
    } catch (IOException io) {
      event.rejectDrop();
    } catch (UnsupportedFlavorException ufe) {
      event.rejectDrop();
    }
    restoreOriginalSelection();
    event.getDropTargetContext().dropComplete(true);
  }
  public void dropActionChanged(DropTargetDragEvent p1) {
    //System.out.println("FileDND_DropTargetListener.dropActionChanged");
  }
} // end class FileDND_DropTargetListener