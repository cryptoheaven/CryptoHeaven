/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.keyTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.KeyRecordEvent;
import com.CH_cl.service.cache.event.KeyRecordListener;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class KeyTableModel extends RecordTableModel {
  
  private KeyListener keyListener;

  private static String STR_KEY = com.CH_cl.lang.Lang.rb.getString("column_Key");
  private static String STR_OWNER = com.CH_cl.lang.Lang.rb.getString("column_Owner");
  private static String STR_CREATED = com.CH_cl.lang.Lang.rb.getString("column_Created");
  private static String STR_UPDATED = com.CH_cl.lang.Lang.rb.getString("column_Updated");
  private static String STR_KEY_ID = com.CH_cl.lang.Lang.rb.getString("column_Key_ID");

  static final ColumnHeaderData columnHeaderData = 
      new ColumnHeaderData(new Object[][]
        { { STR_KEY, STR_OWNER, STR_CREATED, STR_UPDATED, STR_KEY_ID},
          { STR_KEY, STR_OWNER, STR_CREATED, STR_UPDATED, STR_KEY_ID},
          { null, null, null, null },
          { null, null, null, null },
          { new Integer(140), new Integer(122), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60) },
          { new Integer(140), new Integer(122), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60) },
          { new Integer(140), new Integer(122), TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 60) },
          { new Integer(  0), new Integer(  0), TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120) },
          { new Integer( 90), new Integer( 90), TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 50) },
          { new Integer(1), new Integer(0), new Integer(2) },
          { new Integer(1), new Integer(0), new Integer(2) },
          { new Integer(1), new Integer(0), new Integer(2) },
          { new Integer(1), new Integer(4) }
        });

  /** Creates new KeyTableModel */
  public KeyTableModel() {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableModel.class, "KeyTableModel()");
    if (trace != null) trace.exit(KeyTableModel.class);
  }
  /** Creates new KeyTableModel and set the initial data. */
  public KeyTableModel(KeyRecord[] keys) {
    this();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableModel.class, "KeyTableModel(KeyRecord[] keys)");
    if (trace != null) trace.args(keys);
    setData(keys);
    if (trace != null) trace.exit(KeyTableModel.class);
  }

  /**
   * When folders are fetched, their IDs are cached so that we know if table fetch is required when
   * user switches focus to another folder...
   * This vector should also be cleared when users are switched...
   */
  public void clearCachedFetchedFolderIDs() {
  }

  /**
   * Sets auto update mode by listening on the cache key updates.
   */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (keyListener == null) {
        keyListener = new KeyListener();
        cache.addKeyRecordListener(keyListener);
      }
    } else {
      if (keyListener != null) {
        cache.removeKeyRecordListener(keyListener);
        keyListener = null;
      }
    }
    if (trace != null) trace.exit(KeyTableModel.class);
  }

  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof KeyRecord) {
      KeyRecord keyRecord = (KeyRecord) record;

      switch (column) {
        case 0: 
          value = keyRecord.plainPublicKey.shortInfo();
          boolean hasPublic = true; // has Public is TRUE since it was dereferenced in "case 0"
          boolean hasPrivate = keyRecord.getPrivateKey() != null;
          String extras = null;
          if (hasPublic && !hasPrivate)
            extras = " " + com.CH_cl.lang.Lang.rb.getString("key_public_only");
          else if (hasPublic && hasPrivate)
            extras = " " + com.CH_cl.lang.Lang.rb.getString("key_private/public");
          else if (!hasPublic && hasPrivate)
            extras = " " + com.CH_cl.lang.Lang.rb.getString("key_private_only");
          else
            extras = " " + com.CH_cl.lang.Lang.rb.getString("key_no_key");
          value = value.toString() + extras;
          break;
        case 1: 
          value = keyRecord.ownerUserId;
          // use my contact list only, not the reciprocal contacts
          Record owner = CacheUsrUtils.convertUserIdToFamiliarUser(keyRecord.ownerUserId, true, false);
          if (owner == null) {
            UserRecord uRec = new UserRecord();
            uRec.userId = keyRecord.ownerUserId;
            uRec.handle = com.CH_cl.lang.Lang.rb.getString("User");
            owner = uRec;
          }
          value = ListRenderer.getRenderedText(owner);
          break;
        case 2: 
          value = keyRecord.dateCreated;
          break;
        case 3: 
          value = keyRecord.dateUpdated;
          break;
        case 4: 
          value = keyRecord.keyId;
          break;
      }
    }

    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new KeyTableCellRenderer();
  }



  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /** 
   * Listen on updates to the KeyRecords in the cache.
   */
  private class KeyListener implements KeyRecordListener {
    public void keyRecordUpdated(KeyRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new KeyGUIUpdater(event));
    }
  }

  private class KeyGUIUpdater implements Runnable {
    private RecordEvent event;
    public KeyGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyGUIUpdater.class, "KeyGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(KeyGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyGUIUpdater.class, "KeyGUIUpdater.run()");

      Record[] records = event.getRecords();
      if (event.getEventType() == RecordEvent.SET) {
        updateData(records);
      } else if (event.getEventType() == RecordEvent.REMOVE) {
        removeData(records);
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(KeyGUIUpdater.class);
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