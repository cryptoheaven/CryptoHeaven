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

package com.CH_gui.msgTable;

import com.CH_cl.service.cache.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_gui.addressBook.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.tree.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgDND_DropTargetListener extends Object implements DropTargetListener {

  private MsgActionTable msgActionTable;
  private Point lastPt;

  /** Creates new MsgDND_DropTargetListener */
  public MsgDND_DropTargetListener(MsgActionTable msgActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDND_DropTargetListener.class, "MsgDND_DropTargetListener()");
    if (trace != null) trace.args(msgActionTable);
    this.msgActionTable = msgActionTable;
    if (trace != null) trace.exit(MsgDND_DropTargetListener.class);
  }

  public void dragEnter(DropTargetDragEvent event) {
    updateCursor(event);
  }
  public void dragOver(DropTargetDragEvent event) {
    Point pt = event.getLocation();
    if (lastPt == null || lastPt.x != pt.x || lastPt.y != pt.y) {
      lastPt = pt;
      updateCursor(event);
    }
  }
  private void updateCursor(DropTargetDragEvent event) {
    try {
      if (event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR)) {
        if (msgActionTable.getTableModel().getParentFolderPair().getFolderRecord().isAddressType())
          event.acceptDrag(DnDConstants.ACTION_COPY);
        else
          event.acceptDrag(DnDConstants.ACTION_MOVE);
      } else if (event.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR)) {
        if (msgActionTable.getTableModel().getParentFolderPair().getFolderRecord().isAddressType())
          event.acceptDrag(DnDConstants.ACTION_MOVE);
        else
          event.acceptDrag(DnDConstants.ACTION_COPY);
      } else if (event.isDataFlavorSupported(FolderDND_Transferable.FOLDER_RECORD_FLAVOR)) {
        event.rejectDrag();
      } else if ( event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR) ||
                event.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        event.acceptDrag(DnDConstants.ACTION_COPY);
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
  }
  public void dragExit(DropTargetEvent event) {
  }
  public void drop(DropTargetDropEvent event) {
    try {
      Transferable tr = event.getTransferable();

      // get this table context's parent folder
      MsgTableModel tableModel = (MsgTableModel) msgActionTable.getTableModel();
      FolderPair parentFolderPair = tableModel.getParentFolderPair();

      boolean typeFileRecs = false;
      boolean typeFileList = false;
      boolean typeAddrRecs = false;
      boolean typeMsgRecs = false;

      typeFileRecs = tr.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR);
      typeAddrRecs = tr.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
      typeMsgRecs = tr.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR);
      typeFileList = tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor);

      // New Message -- Upload attachment or Copy File Records
      if (typeFileRecs || typeFileList) {
        if (typeFileList) {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
          Iterator iterator = fileList.iterator();
          Vector filesV = new Vector();
          while (iterator.hasNext()) {
            File file = (File) iterator.next();
            if (file.isFile())
              filesV.addElement(file);
          }
          if (filesV.size() > 0) {
            File[] files = new File[filesV.size()];
            filesV.toArray(files);

            new MessageFrame(new Record[] { parentFolderPair }, files);
          }
        }
        else if (typeFileRecs) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          FileDND_TransferableData data = (FileDND_TransferableData) tr.getTransferData(FileDND_Transferable.FILE_RECORD_FLAVOR);
          if (data.fileRecordIDs[1] != null && data.fileRecordIDs[1].length > 0) {
            event.acceptDrop(DnDConstants.ACTION_COPY);
            FileLinkRecord[] fLinks = cache.getFileLinkRecords(data.fileRecordIDs[1]);
            new MessageFrame(new Record[] { parentFolderPair }, fLinks);
          }
          else
            event.rejectDrop();
        }
      }

      // Move or Copy Addresses
      else if (typeAddrRecs) {
        boolean isMove = false;
        if (msgActionTable.getTableModel().getParentFolderPair().getFolderRecord().isAddressType()) {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          isMove = true;
        } else {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          isMove = false;
        }
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        AddrDND_TransferableData data = (AddrDND_TransferableData) tr.getTransferData(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
        MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(data.msgLinkIDs);
        MsgLinkRecord[] mLinksFiltered = (MsgLinkRecord[]) new MsgFilter(Record.RECORD_TYPE_FOLDER, parentFolderPair.getId()).filterExclude(mLinks);
        MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(isMove, parentFolderPair, mLinksFiltered);
      }

      // Move or Copy Messages
      else if (typeMsgRecs) {
        boolean isMove = false;
        if (msgActionTable.getTableModel().getParentFolderPair().getFolderRecord().isAddressType()) {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          isMove = false;
        } else {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          isMove = true;
        }
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgDND_TransferableData data = (MsgDND_TransferableData) tr.getTransferData(MsgDND_Transferable.MSG_RECORD_FLAVOR);
        MsgLinkRecord[] mLinks = cache.getMsgLinkRecords(data.msgLinkIDs);
        MsgLinkRecord[] mLinksFiltered = (MsgLinkRecord[]) new MsgFilter(Record.RECORD_TYPE_FOLDER, parentFolderPair.getId()).filterExclude(mLinks);
        MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(isMove, parentFolderPair, mLinksFiltered);
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
  public void dropActionChanged(DropTargetDragEvent event) {
  }
} // end class MsgDND_DropTargetListener