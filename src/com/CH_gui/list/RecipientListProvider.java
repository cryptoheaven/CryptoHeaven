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

package com.CH_gui.list;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.MsgDataRecordEvent;
import com.CH_cl.service.cache.event.MsgDataRecordListener;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.service.records.filters.RecordIdFilter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_gui.frame.MainFrame;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;


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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class RecipientListProvider extends Object implements ObjectsProviderUpdaterI {

  ListUpdatableI updatable;
  boolean includeDefaultAddressBook;
  boolean includeMsgBoards;
  boolean includeChatFolders;
  boolean includeMsgFolders;
  boolean includeGroupFolders;

  MsgDataListener msgDataListener;

  /** Creates new RecipientListProvider */
  public RecipientListProvider(boolean includeDefaultAddressBook, boolean includeMsgBoards, boolean includeChatFolders, boolean includeMsgFolders, boolean includeGroupFolders) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecipientListProvider.class, "RecipientListProvider()");
    this.includeDefaultAddressBook = includeDefaultAddressBook;
    this.includeMsgBoards = includeMsgBoards;
    this.includeChatFolders = includeChatFolders;
    this.includeMsgFolders = includeMsgFolders;
    this.includeGroupFolders = includeGroupFolders;
    if (trace != null) trace.exit(RecipientListProvider.class);
  }

  private Object[] getInitialObjects() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    if (updatable != null && msgDataListener == null) {
      msgDataListener = new MsgDataListener();
      cache.addMsgDataRecordListener(msgDataListener);
    }

    Record[] allRecords = null;
    Comparator comparator = new ListComparator();

    Record[] allContactChoices = cache.getContactRecordsMyActive(true);
    Arrays.sort(allContactChoices, comparator);
    allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allContactChoices, Record.class);

    // include group folders
    if (includeGroupFolders) {
      Record[] allGroupFolderChoices = cache.getFolderPairsMyOfType(FolderRecord.GROUP_FOLDER, true);
      Arrays.sort(allGroupFolderChoices, comparator);
      allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allGroupFolderChoices, Record.class);
    }

    FolderPair[] allAddressBookChoices = cache.getFolderPairsMyOfType(FolderRecord.ADDRESS_FOLDER, true);
    FolderPair[] includedAddressBookChoices = allAddressBookChoices;
    if (!includeDefaultAddressBook)
      includedAddressBookChoices = (FolderPair[]) new RecordIdFilter(cache.getUserRecord().addrFolderId).filterExclude(allAddressBookChoices);
    if (includedAddressBookChoices != null && includedAddressBookChoices.length > 0) {
      Arrays.sort(includedAddressBookChoices, comparator);
      allRecords = (Record[]) ArrayUtils.concatinate(allRecords, includedAddressBookChoices, Record.class);
    }

    // include address contacts
    // only those ones which have the links (prevent from displaying deleted in current session addresses)
    MsgDataRecord[] addresses = cache.getMsgDataRecords(new MsgFilter(true)); // Address Cards with email address only
    MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForMsgs(RecordUtils.getIDs(addresses));
    Record[] allInitialAddressContactChoices = cache.getMsgDataRecords(MsgLinkRecord.getMsgIDs(addrLinks));
    Arrays.sort(allInitialAddressContactChoices, comparator);
    allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allInitialAddressContactChoices, Record.class);

    // include posting boards
    if (includeMsgBoards) {
      Record[] allBoardsChoices = cache.getFolderPairs(new FolderFilter(true, false), true);
      Arrays.sort(allBoardsChoices, comparator);
      allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allBoardsChoices, Record.class);
    }
    // include chat folders
    if (includeChatFolders) {
      Record[] allChattingChoices = cache.getFolderPairs(new FolderFilter(false, true), true);
      Arrays.sort(allChattingChoices, comparator);
      allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allChattingChoices, Record.class);
    }
    // include user's message folders
    if (includeMsgFolders) {
      Record[] allMsgFolderChoices = cache.getFolderPairsMyOfType(FolderRecord.MESSAGE_FOLDER, true);
      Arrays.sort(allMsgFolderChoices, comparator);
      allRecords = (Record[]) ArrayUtils.concatinate(allRecords, allMsgFolderChoices, Record.class);
    }

    if (updatable != null && msgDataListener != null) {
      makeSureAddressBooksAreFetched(allAddressBookChoices);
    }

    return allRecords;
  }

  private void makeSureAddressBooksAreFetched() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderPair[] allAddressBookChoices = cache.getFolderPairsMyOfType(FolderRecord.ADDRESS_FOLDER, true);
    makeSureAddressBooksAreFetched(allAddressBookChoices);
  }

  private void makeSureAddressBooksAreFetched(FolderPair[] allAddressBookChoices) {
    // make sure address books are fetched
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    for (int i=0; allAddressBookChoices!=null && i<allAddressBookChoices.length; i++) {
      FolderRecord fRec = ((FolderPair) allAddressBookChoices[i]).getFolderRecord();
      FolderShareRecord sRec = ((FolderPair) allAddressBookChoices[i]).getFolderShareRecord();
      if (!cache.wasFolderFetchRequestIssued(fRec.folderId)) {
        // Mark the folder as "fetch issued"
        cache.markFolderFetchRequestIssued(fRec.folderId);
        // <shareId> <ownerObjType> <ownerObjId> <fetchNum> <timestamp>
        Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(sRec.shareId, Record.RECORD_TYPE_FOLDER, fRec.folderId, null, (short) Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (Timestamp) null);
        MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_GET_BRIEFS, request), 30000);
      }
    }
  }

  private class MsgDataListener implements MsgDataRecordListener {
    public void msgDataRecordUpdated(MsgDataRecordEvent event) {
      // Exec on event thread to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new GUIUpdater(event));
    }
  }

  private class GUIUpdater implements Runnable {
    private MsgDataRecordEvent event;
    public GUIUpdater(MsgDataRecordEvent e) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "GUIUpdater(MsgDataRecordEvent e)");
      event = e;
      if (trace != null) trace.exit(GUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "GUIUpdater.run()");
      if (updatable != null && event.getEventType() == RecordEvent.SET) {
        final MsgDataRecord[] addresses = (MsgDataRecord[]) RecordUtils.filter(event.getMsgDataRecords(), new MsgFilter(true)); // Address Cards with email address only
        if (addresses != null && addresses.length > 0) {
          Arrays.sort(addresses, new ListComparator());
          updatable.update(addresses);
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(GUIUpdater.class);
    }
  }


  /*********************************************************************
   * O b j e c t s P r o v i d e r U p d a t e r I   interface methods *
   *********************************************************************/

  public Object[] provide(Object args) {
    return getInitialObjects();
  }

  public Object[] provide(Object args, ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
      return getInitialObjects();
    } else {
      throw new IllegalStateException("Already registered updatable object.");
    }
  }

  public void registerForUpdates(ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
      msgDataListener = new MsgDataListener();
      FetchedDataCache.getSingleInstance().addMsgDataRecordListener(msgDataListener);
      makeSureAddressBooksAreFetched();
    } else {
      throw new IllegalStateException("Already registered for updates.");
    }
  }


  /*****************************************************
  *** D i s p o s a b l e O b j    interface methods ***
  *****************************************************/
  public void disposeObj() {
    if (msgDataListener != null) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      cache.removeMsgDataRecordListener(msgDataListener);
      msgDataListener = null;
    }
  }

}