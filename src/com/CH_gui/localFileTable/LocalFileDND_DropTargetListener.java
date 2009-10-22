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

package com.CH_gui.localFileTable;

import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import com.CH_gui.fileTable.*;
import com.CH_gui.frame.MainFrame;

import com.CH_co.trace.Trace;
import com.CH_co.io.*;
import com.CH_co.monitor.*;
import com.CH_co.util.*;
import com.CH_co.service.records.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class LocalFileDND_DropTargetListener extends Object implements DropTargetListener {
  private JFileChooser jFileChooser;
  /** Creates new LocalFileDND_DropTargetListener */
  protected LocalFileDND_DropTargetListener(JFileChooser fileChooser) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LocalFileDND_DropTargetListener.class, "LocalFileDND_DropTargetListener()");
    if (trace != null) trace.args(fileChooser);
    jFileChooser = fileChooser;
    if (trace != null) trace.exit(LocalFileDND_DropTargetListener.class);
  }

  public void dragEnter(DropTargetDragEvent event) {
    //System.out.println("dragEnter");
    updateCursor(event);
  }
  public void dragOver(DropTargetDragEvent event) {
    //System.out.println("dragOver");
    updateCursor(event);
  }
  private void updateCursor(DropTargetDragEvent event) {
    //System.out.println("updateCursor");
    try {
      /*
      if (event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR)) {
        int sourceActions = event.getSourceActions();
        if ((sourceActions & DnDConstants.ACTION_MOVE) != 0)
          event.acceptDrag(DnDConstants.ACTION_MOVE);
        else if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
          event.acceptDrag(DnDConstants.ACTION_COPY);
        else
          event.rejectDrag();
      }
      */
      if (event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR))
        event.acceptDrag(DnDConstants.ACTION_COPY);
      //else if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        //event.acceptDrag(DnDConstants.ACTION_MOVE);
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
  }
  public void dragExit(DropTargetEvent event) {
    //System.out.println("dragExit");
  }
  public void drop(DropTargetDropEvent event) {
    //System.out.println("drop");

    try {
      Transferable tr = event.getTransferable();

      boolean typeFileRecs = false;
      //boolean typeFileList = false;

      typeFileRecs = tr.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR);
      //typeFileList = tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor);

      // Move files
      /*
      if (typeFileList) {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
          Iterator iterator = fileList.iterator();
          Vector filesV = new Vector();
          while (iterator.hasNext()) {
            File file = (File) iterator.next();
            if (file.isFile()) {
              File newFile = new File(jFileChooser.getCurrentDirectory(), file.getName());
              FileUtils.moveDataEOF(new FileInputStream(file), new FileOutputStream(newFile), new ProgMonitorDumping());
            }
          }
      }
       */

      // Download files or download attachments
      if (typeFileRecs) {
        event.acceptDrop(DnDConstants.ACTION_COPY);
        FileDND_TransferableData data = (FileDND_TransferableData) tr.getTransferData(FileDND_Transferable.FILE_RECORD_FLAVOR);
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Vector fileRecsV = new Vector();
        Vector fromMsgsV = new Vector();
        // if any file links
        if (data.fileRecordIDs[1] != null && data.fileRecordIDs[1].length > 0) {
          FileLinkRecord[] fLinks = cache.getFileLinkRecords(data.fileRecordIDs[1]);
          fileRecsV.addAll(Arrays.asList(fLinks));
          for (int i=0; i<fLinks.length; i++) {
            if (fLinks[i].ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
              MsgLinkRecord[] ownerMsgLinks = cache.getMsgLinkRecordsForMsg(fLinks[i].ownerObjId);
              if (ownerMsgLinks != null && ownerMsgLinks.length > 0 && !fromMsgsV.contains(ownerMsgLinks[0]))
                fromMsgsV.addElement(ownerMsgLinks[0]);
            }
          }
        }
        // if any folders
        if (data.fileRecordIDs[0] != null && data.fileRecordIDs[0].length > 0) {
          fileRecsV.addAll(Arrays.asList(CacheUtilities.convertRecordsToPairs(cache.getFolderRecords(data.fileRecordIDs[0]))));
        }
        FileRecord[] fileRecs = null;
        if (fileRecsV.size() > 0) {
          fileRecs = new FileRecord[fileRecsV.size()];
          fileRecsV.toArray(fileRecs);
        }
        MsgLinkRecord[] fromMsgs = null;
        if (fromMsgsV.size() > 0) {
          fromMsgs = new MsgLinkRecord[fromMsgsV.size()];
          fromMsgsV.toArray(fromMsgs);
          fromMsgs = (MsgLinkRecord[]) ArrayUtils.removeDuplicates(fromMsgs);
        }
        if (fileRecs != null && fileRecs.length > 0) {
          // now, download all selected files and directories...
          DownloadUtilities.downloadFilesStartCoordinator(fileRecs, fromMsgs, jFileChooser.getCurrentDirectory(), MainFrame.getServerInterfaceLayer());
        }
      }
      else {
        event.rejectDrop();
      }
    } catch (IOException io) {
      event.rejectDrop();
    } catch (UnsupportedFlavorException ufe) {
      event.rejectDrop();
    }
    event.getDropTargetContext().dropComplete(true);
  }
  public void dropActionChanged(DropTargetDragEvent event) {
    //System.out.println("dropActionChanged");
  }
}