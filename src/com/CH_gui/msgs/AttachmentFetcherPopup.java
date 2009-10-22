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

package com.CH_gui.msgs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_gui.list.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 *
 *************************************************************************************
 * Class to take care of fetching the attachments and showing the popup menu.
 ************************************************************************************/
public class AttachmentFetcherPopup extends Thread {

  private MsgLinkRecord[] paramParentMsgLinkRecords;

  private JPopupMenu popup = new JMyPopupMenu(com.CH_gui.lang.Lang.rb.getString("Attachment_Options"));
  private Component parent;


  public AttachmentFetcherPopup(Component parent, MsgLinkRecord parentLinkRecord) {
    this(parent, new MsgLinkRecord[] { parentLinkRecord });
  }

  public AttachmentFetcherPopup(Component parent, MsgLinkRecord[] parentLinkRecords) {
    super("AttachmentFetcherPopup");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AttachmentFetcherPopup.class, "AttachmentFetcherPopup(Component parent, MsgLinkRecord[] parentLinkRecords)");
    if (trace != null) trace.args(parent, parentLinkRecords);

    this.parent = parent;
    this.paramParentMsgLinkRecords = parentLinkRecords;

    popup.add(new JMyMenuItem(com.CH_gui.lang.Lang.rb.getString("Fetching_Attachment(s)_...")));
    if (parent != null) {
      popup.pack();
      // wrong-popup-location 
      // Point point = MiscGui.getSuggestedPopupLocation(parent, popup);
      // popup.show(parent, point.x, point.y);
      popup.show(parent, 0, parent.getSize().height);
    } else {
      throw new IllegalArgumentException("Parent component may not be null!");
    }

    // change the priority of this thread to minimum
    setPriority(MIN_PRIORITY);

    if (trace != null) trace.exit(AttachmentFetcherPopup.class);
  }


  public void run() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AttachmentFetcherPopup.class, "run()");

    Record[] attachments = fetchAttachments(paramParentMsgLinkRecords);
    updatePopup(attachments);

    // help cleanup
    popup = null;

    if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
    if (trace != null) trace.exit(AttachmentFetcherPopup.class);
    if (trace != null) trace.clear();
  } // end run()


  /**
   * Send a request to fetch msg link attachments and file link attachments - wait for reply.
   */
  public static Record[] fetchAttachments(MsgLinkRecord[] parentMsgLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AttachmentFetcherPopup.class, "fetchAttachments(MsgLinkRecord[] parentMsgLinks)");
    if (trace != null) trace.args(parentMsgLinks);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

    parentMsgLinks = (MsgLinkRecord[]) ArrayUtils.removeDuplicates(parentMsgLinks);
    MsgDataRecord[] parentDatas = (MsgDataRecord[]) ArrayUtils.removeDuplicates(cache.getMsgDataRecords(MsgLinkRecord.getMsgIDs(parentMsgLinks)));

    Long[] parentLinkIDs = RecordUtils.getIDs(parentMsgLinks);
    Long[] parentMsgIDs = (Long[]) ArrayUtils.removeDuplicates(RecordUtils.getIDs(parentDatas));

    // prepare requests
    Obj_IDs_Co request = null;
    int sumFiles = MsgDataRecord.sumAttachedFiles(parentDatas);
    int sumMsgs = MsgDataRecord.sumAttachedMsgs(parentDatas);
    if (sumFiles > 0 || sumMsgs > 0) {
      request = new Obj_IDs_Co();
      request.IDs = new Long[2][];
      request.IDs[0] = parentLinkIDs;
      if (trace != null) trace.data(10, "gather folder IDs for parent Msg Links");
      // if parent message links don't reside in any folders, don't specify shares as server will find access path from parentLinkIDs
      Long[] folderIDs = MsgLinkRecord.getOwnerObjIDs(parentMsgLinks, Record.RECORD_TYPE_FOLDER);
      if (trace != null) trace.data(20, "gathered folder IDs", folderIDs);
      FolderShareRecord[] shareRecords = cache.getFolderSharesMyForFolders(folderIDs, true);
      if (trace != null) trace.data(30, "gathered my share records", shareRecords);
      //FolderShareRecord[] shareRecords = cache.getFolderShareRecordsMyRootsForMsgs(parentMsgLinks);
      if (shareRecords != null && shareRecords.length > 0)
        request.IDs[1] = RecordUtils.getIDs(shareRecords);
      else 
        request.IDs[1] = new Long[0];
      if (trace != null) trace.data(40, "request IDs", request.IDs[1]);
    }

    // send requests -- suppress any potential error displays
    if (sumFiles > 0) {
      ClientMessageAction msgAction = serverInterfaceLayer.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS, request), 60000);
      if (msgAction != null) {
        MiscGui.suppressMsgDialogsGUI(true);
        DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, msgAction);
        MiscGui.suppressMsgDialogsGUI(false);
      }
    }
    if (sumMsgs > 0) {
      ClientMessageAction msgAction = serverInterfaceLayer.submitAndFetchReply(new MessageAction(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS, request), 60000);
      if (msgAction != null) {
        MiscGui.suppressMsgDialogsGUI(true);
        DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, msgAction);
        MiscGui.suppressMsgDialogsGUI(false);
      }
    }

    Record[] attachmentsMsgs = cache.getMsgLinkRecordsOwnersAndType(parentMsgIDs, new Short(Record.RECORD_TYPE_MESSAGE));
    Record[] attachmentsFiles = cache.getFileLinkRecordsOwnersAndType(parentMsgIDs, new Short(Record.RECORD_TYPE_MESSAGE));

    // sort the attachment records for display
    if (attachmentsMsgs != null && attachmentsMsgs.length > 1)
      Arrays.sort(attachmentsMsgs, new ListComparator());
    if (attachmentsFiles != null && attachmentsFiles.length > 1)
      Arrays.sort(attachmentsFiles, new ListComparator());

    Record[] attachments = (Record[]) RecordUtils.concatinate(attachmentsMsgs, attachmentsFiles);

    if (trace != null) trace.exit(AttachmentFetcherPopup.class, attachments);
    return attachments;
  }


  private void updatePopup(final Record[] attachments) {

    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(attachments, MsgLinkRecord.class);
    FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(attachments, FileLinkRecord.class);

    popup.removeAll();

    JMenuItem saveItem = new JMyMenuItem(com.CH_gui.lang.Lang.rb.getString("Save_Attachment(s)_..."));
    saveItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Window w = SwingUtilities.windowForComponent(parent);
        if (w instanceof Frame) new SaveAttachmentsDialog((Frame) w, attachments, paramParentMsgLinkRecords);
        else if (w instanceof Dialog) new SaveAttachmentsDialog((Dialog) w, attachments, paramParentMsgLinkRecords);
      }
    });

    popup.add(saveItem);

    popup.add(new JSeparator());

    for (int i=0; i<msgLinks.length; i++) {
      String subject = ListRenderer.getRenderedText(msgLinks[i], true, false, true);
      JMenuItem msgItem = new JMyMenuItem(subject, ListRenderer.getRenderedIcon(msgLinks[i]));
      msgItem.addActionListener(new MsgPreviewShower(msgLinks[i]));
      popup.add(msgItem);
    }
    for (int i=0; i<fileLinks.length; i++) {
      JMenuItem fileItem = new JMyMenuItem(ListRenderer.getRenderedText(fileLinks[i], true, false, true), ListRenderer.getRenderedIcon(fileLinks[i]));
      fileItem.addActionListener(new FileDownloader(parent, paramParentMsgLinkRecords, fileLinks[i]));
      popup.add(fileItem);
    }

    popup.pack();
    popup.revalidate();
    popup.repaint();
  }



  /*******************************************************
   * Private class to display a Message Attachment Preview
   *******************************************************/
  private static class MsgPreviewShower implements ActionListener {
    private MsgLinkRecord messageLink;
    private MsgPreviewShower(MsgLinkRecord msgLink) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MsgPreviewShower(MsgLinkRecord msgLink)");
      if (trace != null) trace.args(msgLink);
      messageLink = msgLink;
      if (trace != null) trace.exit(getClass());
    }
    public void actionPerformed(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
      if (trace != null) trace.args(event);

      MsgLinkRecord[] parents = FetchedDataCache.getSingleInstance().getMsgLinkRecordsForMsg(messageLink.ownerObjId);
      if (parents != null && parents.length > 0)
        new MsgPreviewFrame(parents[0], new MsgLinkRecord[] { messageLink });

      if (trace != null) trace.exit(getClass());
    }
  } // end private class MsgPreviewShower


  /*******************************************************
   * Private class to display a File Download Dialog
   *******************************************************/
  private static class FileDownloader implements ActionListener {
    private Component parent;
    private MsgLinkRecord[] paramParentMsgLinkRecords;
    private FileLinkRecord fLink;
    
    private FileDownloader(Component parent, MsgLinkRecord[] paramParentMsgLinkRecords, FileLinkRecord fileLink) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "FileDownloader(FileLinkRecord fileLink)");
      if (trace != null) trace.args(parent, paramParentMsgLinkRecords, fileLink);
      this.parent = parent;
      this.paramParentMsgLinkRecords = paramParentMsgLinkRecords;
      this.fLink = fileLink;
      if (trace != null) trace.exit(getClass());
    }
    public void actionPerformed(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
      if (trace != null) trace.args(event);

      if (false && FileLauncher.isAudioWaveFilename(fLink.getFileName())) { // skip play, default to save
        DownloadUtilities.downloadAndOpen(fLink, paramParentMsgLinkRecords, MainFrame.getServerInterfaceLayer(), true, true);
      } else {
        Runnable openTask = new Runnable() {
          public void run() {
            DownloadUtilities.downloadAndOpen(fLink, paramParentMsgLinkRecords, MainFrame.getServerInterfaceLayer(), true, false);
          }
        };
        Runnable saveTask = new Runnable() {
          public void run() {
            DownloadUtilities.downloadFilesChoice(new FileLinkRecord[] { fLink }, paramParentMsgLinkRecords, parent, MainFrame.getServerInterfaceLayer());
          }
        };
        MsgLinkRecord parentMsgLink = null;
        if (paramParentMsgLinkRecords.length == 1) {
          parentMsgLink = paramParentMsgLinkRecords[0];
        } else {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord parentMsgData = cache.getMsgDataRecord(fLink.ownerObjId);
          MsgLinkRecord[] possibleLinks = cache.getMsgLinkRecordsForMsg(parentMsgData.msgId);
          parentMsgLink = possibleLinks != null && possibleLinks.length > 0 ? possibleLinks[0] : null;
        }
        Window w = SwingUtilities.windowForComponent(parent);
        if (w instanceof Frame)
          new OpenSaveCancelDialog((Frame) w, fLink, parentMsgLink, openTask, saveTask);
        else if (w instanceof Dialog)
          new OpenSaveCancelDialog((Dialog) w, fLink, parentMsgLink, openTask, saveTask);
      }

      if (trace != null) trace.exit(getClass());
    }
  }

} // end class AttachmentFetcherPopup