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

package com.CH_gui.contactTable;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.util.*;
import java.util.List;
import java.io.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_cl.service.cache.*;

import com.CH_gui.addressBook.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.msgs.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.*;
import com.CH_gui.userTable.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class PersonDND_DropTargetListener extends Object implements DropTargetListener {

  private RecordActionTable recordActionTable;
  private Point lastPt;

  /** Creates new PersonDND_DropTargetListener */
  public PersonDND_DropTargetListener(ContactActionTable contactActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PersonDND_DropTargetListener.class, "PersonDND_DropTargetListener(ContactActionTable contactActionTable)");
    if (trace != null) trace.args(contactActionTable);
    this.recordActionTable = contactActionTable;
    if (trace != null) trace.exit(PersonDND_DropTargetListener.class);
  }
  public PersonDND_DropTargetListener(UserActionTable userActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PersonDND_DropTargetListener.class, "PersonDND_DropTargetListener(UserActionTable userActionTable)");
    if (trace != null) trace.args(userActionTable);
    this.recordActionTable = userActionTable;
    if (trace != null) trace.exit(PersonDND_DropTargetListener.class);
  }
  public PersonDND_DropTargetListener(SubUserActionTable subUserActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PersonDND_DropTargetListener.class, "PersonDND_DropTargetListener(SubUserActionTable subUserActionTable)");
    if (trace != null) trace.args(subUserActionTable);
    this.recordActionTable = subUserActionTable;
    if (trace != null) trace.exit(PersonDND_DropTargetListener.class);
  }


  public void dragEnter(DropTargetDragEvent event) {
    updateCursor(event);
  }
  public void dragOver(DropTargetDragEvent event) {
    Point pt = event.getLocation();
    if (lastPt == null || lastPt.x != pt.x || lastPt.y != pt.y) {
      lastPt = pt;
      JSortedTable jTable = recordActionTable.getJSortedTable();
      int row = jTable.rowAtPoint(pt);

      boolean flavorSupported = isDropFlavourSupported(event);
      if (row >= 0 && flavorSupported) {
        Record[] selected = recordActionTable.getSelectedRecords();
        if (!jTable.getSelectionModel().isSelectedIndex(row) || selected == null || selected.length == 0)
          jTable.getSelectionModel().setSelectionInterval(row, row);
      }

      updateCursor(event);
    }
  }
  private void updateCursor(DropTargetDragEvent event) {
    try {
      boolean accept = false;
      if (isDropFlavourSupported(event)) {
        accept = isDropTargetOk();
      }
      if (accept)
        event.acceptDrag(DnDConstants.ACTION_COPY);
      else
        event.rejectDrag();
    } catch (Throwable t) {
    }
  }
  private boolean isDropTargetOk() {
    boolean accept = false;
    if (recordActionTable instanceof ContactActionTable) {
      if (((ContactActionTable) recordActionTable).getMsgAction().isEnabled())
        accept = true;
    } else if (recordActionTable instanceof UserActionTable) {
      if (((UserActionTable) recordActionTable).getMsgAction().isEnabled())
        accept = true;
    } else if (recordActionTable instanceof SubUserActionTable) {
      if (((SubUserActionTable) recordActionTable).getMsgAction().isEnabled())
        accept = true;
    }
    return accept;
  }
  private boolean isDropFlavourSupported(DropTargetDragEvent event) {
    return  event.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
            event.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR) ||
            event.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR) ||
            event.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR);
  }
  public void dragExit(DropTargetEvent event) {
    //System.out.println("PersonDND_DropTargetListener.dragExit");
  }
  public void drop(DropTargetDropEvent event) {
    //System.out.println("PersonDND_DropTargetListener.drop");
    Point location = event.getLocation();
    try {
      Transferable tr = event.getTransferable();

      // Only send message if selected recipients have valid send message action.
      // Get this table context's selected recipients.
      Record[] recipients = recordActionTable.getSelectedRecords();
      // change any Group Folders into familiar contacts or user records
      recipients = MsgPanelUtils.getOrFetchFamiliarUsers(recipients);

      //if (recipients != null && recipients.length > 0 && contactActionTable.getMsgAction().isEnabled()) {
      if (isDropTargetOk()) {

        // Forward FILES
        if (tr.isDataFlavorSupported(FileDND_Transferable.FILE_RECORD_FLAVOR)) {
          FileDND_TransferableData data = (FileDND_TransferableData) tr.getTransferData(FileDND_Transferable.FILE_RECORD_FLAVOR);
          if (data.fileRecordIDs[1] != null && data.fileRecordIDs[1].length > 0) {
            event.acceptDrop(DnDConstants.ACTION_COPY);
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            new MessageFrame(recipients, cache.getFileLinkRecords(data.fileRecordIDs[1]));
          }
          else
            event.rejectDrop();
        }
        // Forward ADDRESS
        else if (tr.isDataFlavorSupported(AddrDND_Transferable.ADDR_RECORD_FLAVOR)) {
          AddrDND_TransferableData data = (AddrDND_TransferableData) tr.getTransferData(AddrDND_Transferable.ADDR_RECORD_FLAVOR);
          event.acceptDrop(DnDConstants.ACTION_COPY);
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          new MessageFrame(recipients, cache.getMsgLinkRecords(data.msgLinkIDs));
        }
        // Forward MESSAGES
        else if (tr.isDataFlavorSupported(MsgDND_Transferable.MSG_RECORD_FLAVOR)) {
          MsgDND_TransferableData data = (MsgDND_TransferableData) tr.getTransferData(MsgDND_Transferable.MSG_RECORD_FLAVOR);
          event.acceptDrop(DnDConstants.ACTION_COPY);
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          new MessageFrame(recipients, cache.getMsgLinkRecords(data.msgLinkIDs));
        }
        // Upload FILE ATTACHMENTS
        else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
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
            new MessageFrame(recipients, files);
          }
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
    //System.out.println("PersonDND_DropTargetListener.dropActionChanged");
  }
} // end class PersonDND_DropTargetListener