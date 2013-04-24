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

package com.CH_gui.userTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_cl.service.cache.event.UserRecordEvent;
import com.CH_cl.service.cache.event.UserRecordListener;
import com.CH_cl.service.records.filters.SubUserFilter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;
import java.util.ArrayList;

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
* <b>$Revision: 1.29 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class UserTableModel extends RecordTableModel {

  private UserListener userListener;
  private Long parentUserId;

  /* parentIds for which the sub-account user records has been fetched already */
  private static final ArrayList fetchedIds = new ArrayList();

  private static String STR_USER_NAME = com.CH_cl.lang.Lang.rb.getString("column_Username");
  private static String STR_USER_ID = com.CH_cl.lang.Lang.rb.getString("column_User_ID");
  private static String STR_MESSAGING = com.CH_cl.lang.Lang.rb.getString("column_Messaging");

  private static String STR_EMAIL_ADDRESS = "Email Address";
  private static String STR_OTHER_CONTACT_ADDRESS = "Other Contact Address";
  private static String STR_STATUS = "Status";
  private static String STR_STORAGE_LIMIT = "Storage Limit";
  private static String STR_STORAGE_USED = "Storage Used";
  private static String STR_DATE_CREATED = "Date Created";
  private static String STR_LAST_LOGIN = "Last Login";

  static final ColumnHeaderData columnHeaderData = 
      new ColumnHeaderData(new Object[][]
        { { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS },
          { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS },
          { null, null, null },
          { null, null, null },
          { new Integer(135), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(135), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(135), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(  0), new Integer(100), new Integer(200), new Integer(  0) },
          { new Integer( 90), new Integer( 70), new Integer( 70), new Integer(150) },
          { new Integer(0), new Integer(3), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(3), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(3), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(1) }
        });
  static final ColumnHeaderData columnHeaderData_subAccounts = 
      new ColumnHeaderData(new Object[][]
        { { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS, STR_OTHER_CONTACT_ADDRESS, STR_STORAGE_LIMIT, STR_STORAGE_USED, STR_LAST_LOGIN, STR_DATE_CREATED, STR_STATUS },
          { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS, STR_OTHER_CONTACT_ADDRESS, STR_STORAGE_LIMIT, STR_STORAGE_USED, STR_LAST_LOGIN, STR_DATE_CREATED, STR_STATUS },
          { null, null, null },
          { null, null, null },
          { new Integer(130), new Integer( 70), new Integer( 90), new Integer(250), new Integer(250), new Integer( 90), new Integer( 90), new Integer(100), new Integer(100), new Integer(90) },
          { new Integer(130), new Integer( 70), new Integer( 90), new Integer(250), new Integer(250), new Integer( 90), new Integer( 90), new Integer(100), new Integer(100), new Integer(90) },
          { new Integer(130), new Integer( 70), new Integer( 90), new Integer(250), new Integer(250), new Integer( 90), new Integer( 90), new Integer(100), new Integer(100), new Integer(90) },
          { new Integer(  0), new Integer(100), new Integer(200), new Integer(  0), new Integer(  0), new Integer(100), new Integer(100), new Integer(110), new Integer(110), new Integer(90) },
          { new Integer( 90), new Integer( 70), new Integer( 70), new Integer(150), new Integer(150), new Integer( 90), new Integer( 90), new Integer( 90), new Integer( 90), new Integer(50) },
          { new Integer(0), new Integer(3), new Integer(9), new Integer(5), new Integer(6), new Integer(7), new Integer(8) },
          { new Integer(0), new Integer(3), new Integer(9), new Integer(5), new Integer(6), new Integer(7), new Integer(8) },
          { new Integer(0), new Integer(3), new Integer(9), new Integer(5), new Integer(6), new Integer(7), new Integer(8) },
          { new Integer(0), new Integer(3) }
        });
  static final ColumnHeaderData columnHeaderData_passRecovery = 
      new ColumnHeaderData(new Object[][]
        { { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS },
          { STR_USER_NAME, STR_USER_ID, STR_MESSAGING, STR_EMAIL_ADDRESS },
          { null, null, null },
          { null, null, null },
          { new Integer(100), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(100), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(100), new Integer( 70), new Integer( 90), new Integer(250) },
          { new Integer(  0), new Integer(100), new Integer(200), new Integer(  0) },
          { new Integer( 90), new Integer( 70), new Integer( 70), new Integer(150) },
          { new Integer(0), new Integer(3), new Integer(1) },
          { new Integer(0), new Integer(3), new Integer(1) },
          { new Integer(0), new Integer(3), new Integer(1) },
          { new Integer(0), new Integer(1) }
        });

  /** Creates new UserTableModel */
  public UserTableModel() {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserTableModel.class, "UserTableModel()");
    if (trace != null) trace.exit(UserTableModel.class);
  }

  /** Creates new UserTableModel */
  public UserTableModel(ColumnHeaderData columnHeaderData) {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserTableModel.class, "UserTableModel(ColumnHeaderData columnHeaderData)");
    if (trace != null) trace.exit(UserTableModel.class);
  }

  /** Creates new UserTableModel */
  public UserTableModel(ColumnHeaderData columnHeaderData, RecordFilter filter) {
    super(columnHeaderData, filter);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserTableModel.class, "UserTableModel(ColumnHeaderData columnHeaderData, RecordFilter filter)");
    setAutoUpdate(true);
    initData(FetchedDataCache.getSingleInstance().getMyUserId());
    if (trace != null) trace.exit(UserTableModel.class);
  }

  private Long getParentUserId() {
    return parentUserId;
  }

  /**
  * When folders are fetched, their IDs are cached so that we know if table fetch is required when
  * user switches focus to another folder...
  * This vector should also be cleared when users are switched...
  */
  public void clearCachedFetchedFolderIDs() {
  }

  /**
  * Sets auto update mode by listening on the cache user updates.
  */
  public synchronized void setAutoUpdate(boolean flag) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (userListener == null) {
        userListener = new UserListener();
        cache.addUserRecordListener(userListener);
      }
    } else {
      if (userListener != null) {
        cache.removeUserRecordListener(userListener);
        userListener = null;
      }
    }
  }

  /**
  * Initializes the model with specified user's sub-user accounts
  */
  public synchronized void initData(Long newParentId) {
    Long parentId = getParentUserId();
    if (parentId == null || !parentId.equals(newParentId)) {
      RecordFilter filter = new SubUserFilter(newParentId, false, true);
      setFilter(filter);
      switchData(newParentId);
      refreshData(newParentId, false);
    }
    setCollapseFileVersions(true);
  }

  /**
  * @param fetch true if data should be refetched from the database.
  */
  public synchronized void refreshData(boolean forceFetch) {
    Long parentId = getParentUserId();
    if (parentId != null) {
      refreshData(parentId, forceFetch);
    }
  }

  /**
  * Initializes the model setting the specified parent user id as its main variable
  */
  private synchronized void switchData(Long newParentId) {
    Long parentId = getParentUserId();
    if (parentId == null || !parentId.equals(newParentId)) {

      removeData();

      if (newParentId != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserRecord[] userRecords = cache.getUserRecords();
        // filter will filter out unwanted records
        updateData(userRecords);
      } 
      this.parentUserId = newParentId;
    }
  }


  /**
  * Forces a refresh of data displayed even if its already displaying the specified data.
  */
  private synchronized void refreshData(Long parentId, boolean forceFetch) {
    if (parentId != null) {
      UserRecord userRec = FetchedDataCache.getSingleInstance().getUserRecord(parentId);
      if (userRec != null) {
        // fetch the users for this parent
        fetchUsers(userRec.userId, forceFetch);
      }
    }
  }

  /** 
  * Send a request to fetch files for the <code> parentId </code> from the server
  * if users were not fetched for this parent, otherwise get them from cache
  * @param force true to force a fetch from the database
  */
  private void fetchUsers(final Long parentId, boolean force) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserTableModel.class, "fetchUsers(final Long parentId, boolean force)");
    if (trace != null) trace.args(parentId);
    if (trace != null) trace.args(force);

    synchronized (fetchedIds) {
      if (force || !fetchedIds.contains(parentId)) {

        // remove file links from the cache, leave the folders
        int rowCount = getRowCount();
        ArrayList recordsToRemove = new ArrayList();
        for (int row=0; row<rowCount; row++) {
          Record rec = getRowObjectNoTrace(row);
          if (rec instanceof UserRecord) {
            recordsToRemove.add(rec);
          }
        }
        if (recordsToRemove.size() > 0) {
          UserRecord[] recs = new UserRecord[recordsToRemove.size()];
          recordsToRemove.toArray(recs);
          fetchedIds.remove(parentId);
          FetchedDataCache.getSingleInstance().removeUserRecords(recs);
        }

        Obj_List_Co request = new Obj_List_Co();
        request.objs = new Object[] { Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, null, Boolean.FALSE };

        MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_SUB_ACCOUNTS, request);
        Runnable afterJob = new Runnable() {
          public void run() {
            if (!fetchedIds.contains(parentId)) {
              fetchedIds.add(parentId);
            }
          }
        };
        MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 5000, afterJob, afterJob);
      }
    } // end synchronized


    if (trace != null) trace.exit(UserTableModel.class);
  } 




  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof UserRecord) {
      UserRecord userRecord = (UserRecord) record;

      switch (column) {
        case 0: value = userRecord.handle;
          break;
        case 1: value = userRecord.userId;
          break;
        case 2: value = userRecord.acceptingSpam;
          break;
          /*
        case 3: // parent user
          if (userRecord.parentId == null)
            value = "";
          else {
            UserRecord parentUser = FetchedDataCache.getSingleInstance().getUserRecord(userRecord.parentId);
            if (parentUser == null)
              value = userRecord.parentId;
            else
              value = parentUser.shortInfo();
          }
          break;
          */
        case 3:
          String[] emailStrings = CacheUsrUtils.getCachedDefaultEmail(userRecord, false);
          String emailAddress = emailStrings != null ? emailStrings[2] : "N/A";
          value = emailAddress;
          break;
        case 4: value = userRecord.emailAddress;
          break;
        case 5: value = userRecord.storageLimit;
          break;
        case 6: value = userRecord.storageUsed;
          break;
        case 7: value = userRecord.dateLastLogin;
          break;
        case 8: value = userRecord.dateCreated;
          break;
        case 9: value = userRecord.isHeld() ? "Suspended" : "Active";
          break;
      }
    }

    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new UserTableCellRenderer();
  }


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /** 
  * Listen on updates to the ContactRecords in the cache.
  */
  private class UserListener implements UserRecordListener {
    public void userRecordUpdated(UserRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new UserGUIUpdater(event));
    }
  }

  private class UserGUIUpdater implements Runnable {
    private RecordEvent event;
    public UserGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserGUIUpdater.class, "UserGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(UserGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserGUIUpdater.class, "UserGUIUpdater.run()");

      Record[] records = event.getRecords();
      if (event.getEventType() == RecordEvent.SET) {
        updateData(records);
      } else if (event.getEventType() == RecordEvent.REMOVE) {
        removeData(records);
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(UserGUIUpdater.class);
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