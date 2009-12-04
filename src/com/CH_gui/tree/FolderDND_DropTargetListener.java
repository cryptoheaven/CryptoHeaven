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

package com.CH_gui.tree;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.addressBook.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgTable.*;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.TreePath;

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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderDND_DropTargetListener extends Object implements DropTargetListener {

  private static int DROP_HARD_SELECT_DELAY = 1500;

  private FolderTree tree;
  private long startTimeOverPath;

  private boolean originalSelectionSaved;
  private TreePath[] originallySelectedPaths;
  private TreePath lastHighlightedPath;

  /** Creates new FolderDND_DropTargetListener */
  public FolderDND_DropTargetListener(FolderTree tree) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "FolderDND_DropTargetListener()");
    this.tree = tree;
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }


  /*************************************************************
   * D R O P   T A R G E T   L I S T E N E R   I n t e r f a c e
   *************************************************************/
  public void dragEnter(DropTargetDragEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "dragEnter(DropTargetDragEvent event)");
    if (trace != null) trace.args(event);
    updateCursor(event);
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  public void dragOver(DropTargetDragEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "dragOver(DropTargetDragEvent event)");
    updateCursor(event);
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  private void updateCursor(DropTargetDragEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "updateCursor(DropTargetDragEvent event)");
    if (trace != null) trace.args(event);
    try {
      boolean acceptCopy = false;
      boolean acceptMove = false;

      boolean toMoveAddr = false;
      boolean toMoveMsg = false;
      boolean toMoveFile = false;
      boolean toUpload = false;
      boolean toMoveFolder = false;

      toMoveAddr = event.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
      toMoveMsg = event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR);
      toMoveFile = event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR);
      toUpload = event.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
      toMoveFolder = event.isDataFlavorSupported(FolderDND_Transferable.FOLDER_RECORD_FLAVOR);

      /*
      if (toMoveMsg)
        System.out.println("toMoveMsg="+toMoveMsg);
      if (toMoveFile)
        System.out.println("toMoveFile="+toMoveFile);
      if (toUpload)
        System.out.println("toUpload="+toUpload);
      */
      Point location = event.getLocation();
      TreePath path = tree.getPathForLocation(location.x, location.y);

      if ((toMoveAddr || toMoveMsg || toMoveFile || toUpload || toMoveFolder) && path != null) {
        FolderPair[] pairs = tree.getLastPathComponentFolderPairs(new TreePath[] { path });
        if (pairs != null && pairs.length > 0 && pairs[0].getFolderRecord() != null) {
          FolderRecord fRec = pairs[0].getFolderRecord();
          short type = fRec.folderType.shortValue();
          int sourceActions = event.getSourceActions();

          if (toMoveAddr && fRec.isAddressType()) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
            else if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
              acceptCopy = true;
          }
          else if (toMoveAddr && fRec.isMailType()) {
            if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
              acceptCopy = true;
          }
          else if (toMoveAddr && fRec.isRecycleType()) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
          }
          else if (toMoveMsg && fRec.isMailType()) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
            else if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
              acceptCopy = true;
          }
          else if (toMoveMsg && fRec.isAddressType()) {
            if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
              acceptCopy = true;
          }
          else if (toMoveMsg && fRec.isRecycleType()) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
          }
          // Accept folder destination to be my AND other's people folders since we now manage a VIEW tree too.
          //else if (toMoveFolder && fRec.ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId()))
          else if (toMoveFolder)
            acceptMove = true;
          else if (toMoveFile && type == FolderRecord.FILE_FOLDER) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
            else if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
              acceptCopy = true;
          }
          else if (toMoveFile && fRec.isMsgType())
            acceptCopy = true;
          else if (toMoveFile && fRec.isRecycleType()) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
              acceptMove = true;
          }
          else if (toUpload && (type == FolderRecord.FILE_FOLDER || fRec.isMsgType()))
            acceptCopy = true;
          else if (toUpload && type == FolderRecord.CATEGORY_FILE_FOLDER) {
            acceptCopy = true;
//            // if Folders only then accept
//            List fileList = (List) event.get tr.getTransferData(DataFlavor.javaFileListFlavor);
//            Iterator iterator = fileList.iterator();
//            boolean anyNonDirectories = false;
//            while (iterator.hasNext()) {
//              File file = (File) iterator.next();
//              if (!anyNonDirectories && !file.isDirectory()) {
//                anyNonDirectories = true;
//                break;
//              }
//            }
//            acceptCopy = !anyNonDirectories;
          }
        }
        // allow moving folders to desktop (the tree root)
        else if (toMoveFolder)
          acceptMove = true;
      }
      if (path != null) {
        //tree.scrollPathToVisible(path);
        tree.scrollPathToVisible2(path);
        highlightMouseOverPath(path);
      }
      if (acceptCopy)
        event.acceptDrag(DnDConstants.ACTION_COPY);
      else if (acceptMove)
        event.acceptDrag(DnDConstants.ACTION_MOVE);
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  private void highlightMouseOverPath(TreePath path) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "highlightMouseOverPath(TreePath path)");
    if (trace != null) trace.args(path);
    if (!originalSelectionSaved) {
      lastHighlightedPath = null;
      originallySelectedPaths = tree.getSelectionPaths();
      originalSelectionSaved = true;
    }
    boolean shouldSelection = true;
    if (path != null) {
      if (!path.equals(lastHighlightedPath)) {
        // reset timer
        startTimeOverPath = System.currentTimeMillis();
        // remember the path
        lastHighlightedPath = path;
      }
      else if (System.currentTimeMillis() - startTimeOverPath > DROP_HARD_SELECT_DELAY) {
        shouldSelection = false;
        lastHighlightedPath = null;
      }
    }
    //tree.setSelectionPath(path);
    if (shouldSelection) {
      tree.suppressSelection(true);
      tree.setSelectionPath(path);
      tree.suppressSelection(false);
    }
    else {
      TreePath[] selectedPaths = tree.getSelectionPaths();
      tree.removeSelectionPaths(selectedPaths);
      tree.setSelectionPath(path);
      tree.expandPath(path);
      // Change the originalSelectedPaths to the newly hardly changed one so that we
      // can drop into the table, otherwise the table component would revert back.
      originallySelectedPaths = new TreePath[] { path };
    }
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  private void restoreOriginalSelection() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "restoreOriginalSelection()");
    if (originalSelectionSaved) {
      tree.setSelectionPaths(originallySelectedPaths);
      originalSelectionSaved = false;
      originallySelectedPaths = null;
    }
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  public void dragExit(DropTargetEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "dragExit(DropTargetEvent event)");
    if (trace != null) trace.args(event);
    restoreOriginalSelection();
    tree.suppressSelection(false);
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  public void drop(DropTargetDropEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "drop(DropTargetDropEvent event)");
    if (trace != null) trace.args(event);
    Point location = event.getLocation();
    try {
      Transferable tr = event.getTransferable();

      // get the selected drop folder
      //TreePath path = tree.getSelectionPath();
      //Point location = event.getLocation();
      TreePath path = tree.getPathForLocation(location.x, location.y);

      FolderPair[] pairs = null;
      if (path != null)
        pairs = tree.getLastPathComponentFolderPairs(new TreePath[] { path });

      boolean isFolderAccepted = false;
      boolean isFileAccepted = false;
      boolean isDropAccepted = false;

      // if valid destination for any non-folder drop operation
      if (pairs != null && pairs.length > 0 && pairs[0].getFolderRecord() != null && pairs[0].getFolderShareRecord() != null) {

        FolderRecord fRec = pairs[0].getFolderRecord();
        boolean isFileFolderType = fRec.isFileType();
        boolean isAddrFolderType = fRec.isAddressType();
        boolean isMsgFolderType = fRec.isMsgType(); // includes address folders too
        boolean isRecycleFolderType = fRec.isRecycleType();
        boolean isFileCategoryType = fRec.isCategoryType() && fRec.folderType.shortValue() == FolderRecord.CATEGORY_FILE_FOLDER;

        if (isFileFolderType || isMsgFolderType || isRecycleFolderType || isFileCategoryType) {
          FolderPair[] fPairs = new FolderPair[] { pairs[0] };
          boolean toMoveAddr = false;
          boolean toMoveMsg = false;
          boolean toMoveFile = false;
          boolean toMoveFolder = false;
          boolean toUploadFiles = false;

          toMoveAddr = event.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
          toMoveMsg = event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR);
          toMoveFile = event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR);
          toMoveFolder = event.isDataFlavorSupported(FolderDND_Transferable.FOLDER_RECORD_FLAVOR);
          toUploadFiles = event.isDataFlavorSupported(DataFlavor.javaFileListFlavor);


          // Moving files and Attaching files
          if (toMoveFolder) {
            // we will handle this later on regardless of destination folder type... look below
          }

          // Moving or Copying files
          else if (toMoveFile) {
            event.acceptDrop(DnDConstants.ACTION_MOVE);
            isDropAccepted = true;
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            FileDND_TransferableData data = (FileDND_TransferableData) tr.getTransferData(FileDND_Transferable.FILE_RECORD_FLAVOR);
            FileLinkRecord[] fLinks = cache.getFileLinkRecords(data.fileRecordIDs[1]);
            FolderPair[] fldPairs = CacheUtilities.convertRecordsToPairs(cache.getFolderRecords(data.fileRecordIDs[0]));
            if (isFileFolderType || isRecycleFolderType)
              FileActionTable.doMoveOrSaveAttachmentsAction(fPairs[0], fLinks, fldPairs);
            else if (isMsgFolderType)
              new MessageFrame(fPairs, fLinks);
          } // end Moving files and Attaching files

          // Moving or Copying addresses
          else if (toMoveAddr) {
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            AddrDND_TransferableData data = (AddrDND_TransferableData) tr.getTransferData(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
            MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(data.msgLinkIDs);
            if (isAddrFolderType || isMsgFolderType || isRecycleFolderType) {
              boolean isMove = false;
              if (isAddrFolderType || isRecycleFolderType) {
                event.acceptDrop(DnDConstants.ACTION_MOVE);
                isMove = true;
              } else if (isMsgFolderType) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                isMove = false;
              }
              isDropAccepted = true;
              MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(isMove, fPairs[0], mLinks);
            }
          }

          // Moving or Copying messages
          else if (toMoveMsg) {
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            MsgDND_TransferableData data = (MsgDND_TransferableData) tr.getTransferData(MsgDND_Transferable.MSG_RECORD_FLAVOR);
            MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(data.msgLinkIDs);
            if (isAddrFolderType || isMsgFolderType || isRecycleFolderType) {
              boolean isMove = false;
              if (isAddrFolderType) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                isMove = false;
              } else if (isMsgFolderType || isRecycleFolderType) {
                event.acceptDrop(DnDConstants.ACTION_MOVE);
                isMove = true;
              }
              isDropAccepted = true;
              MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(isMove, fPairs[0], mLinks);
            }
          }


          // uploading files or attaching files
          else if (toUploadFiles) {
            if (isFileFolderType || isFileCategoryType)
              isFolderAccepted = true;
            isFileAccepted = true;

            event.acceptDrop(DnDConstants.ACTION_COPY);
            isDropAccepted = true;
            List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
            Iterator iterator = fileList.iterator();
            Vector directoriesV = new Vector();
            Vector acceptedFilesV = new Vector();
            boolean anyFolders = false;
            boolean anyFiles = false;
            while (iterator.hasNext()) {
              File file = (File) iterator.next();
              if (!anyFolders)
                anyFolders = file.isDirectory();
              if (!anyFiles)
                anyFiles = file.isFile();
              if (file.isDirectory())
                directoriesV.addElement(file);
              if ((isFolderAccepted && file.isDirectory()) || (isFileAccepted && file.isFile()))
                acceptedFilesV.addElement(file);
            }

            if (!isMsgFolderType) { // isFileFolderType or any other folder other than message folder
              if (isFileCategoryType) {
                if (anyFiles) {
                  String title = "Invalid Selection";
                  String body = "The destination folder is capable of holding folders only.  To upload files please select a file folder destination.";
                  MessageDialog.showWarningDialog(null, body, title, true);
                } else if (anyFolders) {
                  File[] dirs = (File[]) ArrayUtils.toArray(directoriesV, File.class);
                  UploadUtilities.uploadFilesStartCoordinator(dirs, null, MainFrame.getServerInterfaceLayer());
                }
              } else if (acceptedFilesV.size() > 0) {
                File[] files = (File[]) ArrayUtils.toArray(acceptedFilesV, File.class);
                UploadUtilities.uploadFilesStartCoordinator(files, fPairs[0].getFolderShareRecord(), MainFrame.getServerInterfaceLayer());
              }
            } else {
              if (anyFiles && !anyFolders) {
                File[] files = (File[]) ArrayUtils.toArray(acceptedFilesV, File.class);
                new MessageFrame(fPairs, files); // don't use pairs array because it might contain folders for unacceptable type, so create a new one with well checked object
              } else if (!anyFiles && anyFolders) {
                File[] files = (File[]) ArrayUtils.toArray(directoriesV, File.class);
                UploadUtilities.uploadFilesStartCoordinator(files, fPairs[0].getFolderShareRecord(), MainFrame.getServerInterfaceLayer());
              } else {
                String title = "Invalid Selection";
                String body = "The destination folder is not compatible with your mixed selection of files and folders.  To attach files to a new message, please select files only.  To upload folders, please select folders only.  To upload files and folders please select a file folder destination.";
                MessageDialog.showWarningDialog(null, body, title, true);
              }
            }
          }
        } // end if fileType or msgType
      } // end valid destination for any non-folder drop operation

      boolean toMoveFolders = event.isDataFlavorSupported(FolderDND_Transferable.FOLDER_RECORD_FLAVOR);
      // Move Folders
      if (toMoveFolders) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderDND_TransferableData data = (FolderDND_TransferableData) tr.getTransferData(FolderDND_Transferable.FOLDER_RECORD_FLAVOR);
        FolderPair[] fldPairs = CacheUtilities.convertRecordsToPairs(cache.getFolderRecords(data.folderIDs));
        // Don't allow dropping onto itself
        boolean dropOntoItself = pairs != null && pairs.length == 1 && pairs[0].equals(fldPairs[0]);
        if (!dropOntoItself) {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          isDropAccepted = true;
          FolderPair toPair = pairs != null && pairs.length > 0 ? pairs[0] : fldPairs[0];
          if (toPair.getId().longValue() < 0)
            toPair = fldPairs[0];
          FileActionTable.doMoveOrSaveAttachmentsAction(toPair, null, fldPairs);
        }
      }

      if (!isDropAccepted) {
        event.rejectDrop();
      }
    } catch (IOException io) {
      event.rejectDrop();
    } catch (UnsupportedFlavorException ufe) {
      event.rejectDrop();
    }
    restoreOriginalSelection();
    event.getDropTargetContext().dropComplete(true);
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }
  public void dropActionChanged(DropTargetDragEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DropTargetListener.class, "dropActionChanged(DropTargetDragEvent event)");
    if (trace != null) trace.args(event);
    if (trace != null) trace.exit(FolderDND_DropTargetListener.class);
  }

}