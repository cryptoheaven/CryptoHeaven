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

package com.CH_gui.contactTable;

import com.CH_cl.service.cache.CacheUtilities;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ImageNums;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;
import java.util.ArrayList;
import java.util.Vector;

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
 * <b>$Revision: 1.27 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ContactTableModel extends RecordTableModel {

  private ContactListener contactListener;
  private FolderShareListener shareListener;
  private FolderListener folderListener;
  private InvEmlListener invEmlListener;

  static final ColumnHeaderData columnHeaderData =
      new ColumnHeaderData(new Object[][]
        { { null, com.CH_gui.lang.Lang.rb.getString("column_Name"), null, com.CH_gui.lang.Lang.rb.getString("column_Contact_ID"), com.CH_gui.lang.Lang.rb.getString("column_User_ID"), com.CH_gui.lang.Lang.rb.getString("column_Encryption"), com.CH_gui.lang.Lang.rb.getString("column_Created"), com.CH_gui.lang.Lang.rb.getString("column_Updated"), null },
          { com.CH_gui.lang.Lang.rb.getString("column_Direction"), com.CH_gui.lang.Lang.rb.getString("column_Name"), com.CH_gui.lang.Lang.rb.getString("column_Status"), com.CH_gui.lang.Lang.rb.getString("column_Contact_ID"), com.CH_gui.lang.Lang.rb.getString("column_User_ID"), com.CH_gui.lang.Lang.rb.getString("column_Encryption"), com.CH_gui.lang.Lang.rb.getString("column_Created"), com.CH_gui.lang.Lang.rb.getString("column_Updated"), com.CH_gui.lang.Lang.rb.getString("column_Permissions") },
          { com.CH_gui.lang.Lang.rb.getString("columnTip_Direction_of_the_contact..."), null, com.CH_gui.lang.Lang.rb.getString("columnTip_Contact_Status"), com.CH_gui.lang.Lang.rb.getString("columnTip_Contact_Permissions") },
          { new Integer(ImageNums.ARROW_DOUBLE16), null, new Integer(ImageNums.HANDSHAKE16), null, null, null, null, null, new Integer(ImageNums.TOOLS16) },
          { new Integer(18), new Integer(128), new Integer(18), new Integer( 60), new Integer( 60), new Integer(120), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer(30) },
          { new Integer(18), new Integer(128), new Integer(18), new Integer( 60), new Integer( 60), new Integer(120), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer(30) },
          { new Integer(18), new Integer(128), new Integer(18), new Integer( 60), new Integer( 60), new Integer(120), TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer(30) },
          { new Integer(18), new Integer(  0), new Integer(18), new Integer(120), new Integer(120), new Integer(130), TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(30) },
          { new Integer(18), new Integer( 70), new Integer(18), new Integer( 50), new Integer( 50), new Integer( 70), TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer(30) },
          { new Integer(0), new Integer(1) },
          { new Integer(0), new Integer(1) },
          { new Integer(0), new Integer(1) },
          { new Integer(1), new Integer(0) }
        });

  /** Creates new ContactTableModel */
  public ContactTableModel() {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableModel.class, "ContactTableModel()");
    if (trace != null) trace.exit(ContactTableModel.class);
  }
  /** Creates new ContactTableModel and set the filter. */
  public ContactTableModel(RecordFilter recordFilter) {
    this();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableModel.class, "ContactTableModel(RecordFilter recordFilter)");
    if (trace != null) trace.args(recordFilter);
    setFilter(recordFilter);
    if (trace != null) trace.exit(ContactTableModel.class);
  }
  /** Creates new ContactTableModel and set the initial data. */
  public ContactTableModel(Record[] contacts, RecordFilter recordFilter) {
    this();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableModel.class, "ContactTableModel(ContactRecord[] contacts, RecordFilter recordFilter)");
    if (trace != null) trace.args(contacts);
    if (trace != null) trace.args(recordFilter);
    setFilter(recordFilter);
    setData(contacts);
    if (trace != null) trace.exit(ContactTableModel.class);
  }

  /**
   * When folders are fetched, their IDs are cached so that we know if table fetch is required when
   * user switches focus to another folder...
   * This vector should also be cleared when users are switched...
   */
  public ArrayList getCachedFetchedFolderIDs() {
    return null;
  }

  /**
   * Sets auto update mode by listening on the cache contact updates.
   */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (contactListener == null) {
        contactListener = new ContactListener();
        shareListener = new FolderShareListener();
        folderListener = new FolderListener();
        invEmlListener = new InvEmlListener();

        cache.addContactRecordListener(contactListener);
        cache.addFolderShareRecordListener(shareListener);
        cache.addFolderRecordListener(folderListener);
        cache.addInvEmlRecordListener(invEmlListener);
      }
    } else {
      if (contactListener != null) {
        cache.removeContactRecordListener(contactListener);
        cache.removeFolderShareRecordListener(shareListener);
        cache.removeFolderRecordListener(folderListener);
        cache.removeInvEmlRecordListener(invEmlListener);

        contactListener = null;
        shareListener = null;
        folderListener = null;
        invEmlListener = null;
      }
    }
    if (trace != null) trace.exit(ContactTableModel.class);
  }

  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableModel.class, "getValueAtColumn(Record record, int column, boolean forSortOnly)");
    Object value = null;

    if (record instanceof ContactRecord) {
      ContactRecord contactRecord = (ContactRecord) record;

      FetchedDataCache cache = null;
      Long myUserId = null;
      boolean contactWithMe = false;
      Long otherUID = null;

      if (column == 0 || column == 1 || column == 4 || column == 5) {
        cache = FetchedDataCache.getSingleInstance();
        myUserId = cache.getMyUserId();
        contactWithMe = contactRecord.contactWithId.equals(myUserId);
      }
      if (column == 4 || column == 5) {
        otherUID = contactWithMe ? contactRecord.ownerUserId : contactRecord.contactWithId;
      }
      switch (column) {
        case 0: value = Boolean.valueOf(contactWithMe);
          break;
        case 1:
          value = contactWithMe ? contactRecord.getOtherNote() : contactRecord.getOwnerNote();
          value = value != null ? value : "";
          short status = contactRecord.status.shortValue();
          // overload a few flags because they look the same in user's display
          if (status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED)
            status = ContactRecord.STATUS_ACCEPTED;
          else if (status == ContactRecord.STATUS_DECLINED_ACKNOWLEDGED)
            status = ContactRecord.STATUS_DECLINED;
          int alphaStatus = -status + ((int) 'x');
          value = "" + ((char) alphaStatus) + value;
          break;
        case 2: value = contactRecord.status;
          break;
        case 3: value = contactRecord.contactId;
          break;
        case 4: value = otherUID;
          break;
        case 5:
          KeyRecord kRec = cache.getKeyRecordForUser(otherUID);
          if (kRec != null)
            value = kRec.plainPublicKey.shortInfo();
          break;
        case 6: value = contactRecord.dateCreated;
          break;
        case 7: value = contactRecord.dateUpdated;
          break;
        case 8:
          boolean isAllowMessaging = (contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_MESSAGING) == 0;
          boolean isAllowFolderSharing = (contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS) == 0;
          boolean isAllowOnlineStatusNotify = (contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS) == 0;
          value = "" + (isAllowMessaging ? "M":"") + (isAllowFolderSharing ? "F":"") + (isAllowOnlineStatusNotify ? "O":"");
          break;
      }
    } else if (record instanceof FolderPair) {
      FolderPair groupRecord = (FolderPair) record;
      switch (column) {
        case 1:
          value = "y" + groupRecord.getMyName();
          break;
      }
    } else if (record instanceof InvEmlRecord) {
      InvEmlRecord invEmlRecord = (InvEmlRecord) record;
      switch (column) {
        case 1:
          value = "z" + invEmlRecord.emailAddr;
          break;
      }
    }

    if (trace != null) trace.exit(ContactTableModel.class, value);
    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new ContactTableCellRenderer();
  }



  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /**
   * Listen on updates to the ContactRecords in the cache.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderShareRecords in the cache.
    * If the event happens, add, move or remove shares
    */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderRecords in the cache.
    * If the event happens, set or remove records
    */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }

  /** Listen on updates to the InvEmlRecords in the cache.
    * If the event happens, set or remove records
    */
  private class InvEmlListener implements InvEmlRecordListener {
    public void invEmlRecordUpdated(InvEmlRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }

  private class ContactGUIUpdater implements Runnable {
    private RecordEvent event;
    public ContactGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater.run()");

      Record[] recs = event.getRecords();
      if (recs != null && recs.length > 0) {

        // filter out only interested records
        Vector halfPairPicksV = new Vector();
        Vector contactPicksV = new Vector();
        Record[] halfPairPicks = null;
        FolderPair[] pairPicks = null;
        Record[] contactPicks = null;

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long userId = cache.getMyUserId();

        for (int i=0; i<recs.length; i++) {
          Record rec = recs[i];
          if (rec instanceof FolderRecord) {
            halfPairPicksV.addElement(rec);
          } else if (rec instanceof FolderShareRecord) {
            FolderShareRecord sRec = (FolderShareRecord) rec;
            if (sRec.ownerUserId.equals(userId)) {
              halfPairPicksV.addElement(rec);
            }
          } else if (rec instanceof ContactRecord || rec instanceof InvEmlRecord) {
            contactPicksV.addElement(rec);
          }
        }

        if (halfPairPicksV.size() > 0) {
          halfPairPicks = new Record[halfPairPicksV.size()];
          halfPairPicksV.toArray(halfPairPicks);
          pairPicks = CacheUtilities.convertRecordsToPairs(halfPairPicks);
        }

        if (contactPicksV.size() > 0) {
          contactPicks = new Record[contactPicksV.size()];
          contactPicksV.toArray(contactPicks);
        }

        if (event.getEventType() == RecordEvent.SET) {
          if (pairPicks != null && pairPicks.length > 0) {
            updateData(pairPicks);
          }
          if (contactPicks != null && contactPicks.length > 0) {
            updateData(contactPicks);
          }
        } else if (event.getEventType() == RecordEvent.REMOVE) {
          if (pairPicks != null && pairPicks.length > 0) {
            removeData(pairPicks);
          }
          if (halfPairPicks != null && halfPairPicks.length > 0) {
            // If Share is already gone, use the Folder only to construct a Pair for removal
            for (int i=0; i<halfPairPicks.length; i++) {
              if (halfPairPicks[i] instanceof FolderRecord) {
                FolderRecord fRec = (FolderRecord) halfPairPicks[i];
                FolderPair halfPair = new FolderPair(null, fRec);
                removeData(new Record[] { halfPair });
              }
            }
          }
          if (contactPicks != null && contactPicks.length > 0) {
            removeData(contactPicks);
          }
        }
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
  }

  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

  /**
   * Checks if folder share's content of a given ID was already retrieved.
   */
  public boolean isContentFetched(Long shareId) {
    return false;
  }

}