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

package com.CH_gui.msgTable;

import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.ops.FileLinkOps;
import com.CH_cl.service.ops.MsgLinkOps;
import com.CH_cl.service.records.FolderRecUtil;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_cl.service.records.filters.TextSearchFilter;
import com.CH_co.monitor.Interrupter;
import com.CH_co.monitor.Interruptible;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.HTML_Ops;
import com.CH_co.util.HTML_utils;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_gui.addressBook.AddressTableCellRenderer;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.postTable.PostTableCellRenderer;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

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
* <b>$Revision: 1.54 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class MsgTableModel extends RecordTableModel {

  // FolderShareIds for which records have been fetched already.
  private static final ArrayList fetchedIds = new ArrayList(); // default non-filter type (full or brief depending on folder type)
  private static final ArrayList fetchedIdsFull = new ArrayList();

  private MsgLinkListener linkListener;
  private MsgDataListener dataListener;

  // mode to use a message renderer, sent message renderer, or posting renderer for this model's data
  private int messageMode;
  public static final int MODE_MSG = 0;
  public static final int MODE_MSG_INBOX = 1;
  public static final int MODE_MSG_SENT = 2;
  public static final int MODE_MSG_SPAM = 3;
  public static final int MODE_DRAFTS = 4;
  public static final int MODE_POST = 5;
  public static final int MODE_CHAT = 6;
  public static final int MODE_ADDRESS = 7;
  public static final int MODE_WHITELIST = 8;

  public static final int COLUMN_INDEX__FROM = 3; // reflects the model index of "From" column
  public static final int COLUMN_INDEX__POSTING = 5; // reflects the model index of "Posting" column
  public static final int COLUMN_INDEX__SENT = 6; // reflects the model index of "Sent" column

  private static String STR_PRIORITY = com.CH_cl.lang.Lang.rb.getString("column_Priority");
  private static String STR_SECURE_LOCK = com.CH_cl.lang.Lang.rb.getString("column_Secure_Lock");
  private static String STR_ATTACHMENT = com.CH_cl.lang.Lang.rb.getString("column_Attachment");
  private static String STR_FLAG = com.CH_cl.lang.Lang.rb.getString("column_Flag");

  private static String STR_FROM = com.CH_cl.lang.Lang.rb.getString("column_From");

  private static String STR_TO = com.CH_cl.lang.Lang.rb.getString("column_To");
  private static String STR_E_MAIL_ADDRESS = com.CH_cl.lang.Lang.rb.getString("column_E_Mail_Address");

  private static String STR_SUBJECT = com.CH_cl.lang.Lang.rb.getString("column_Subject");
  private static String STR_POSTING = com.CH_cl.lang.Lang.rb.getString("column_Posting");
  private static String STR_MESSAGE = com.CH_cl.lang.Lang.rb.getString("column_Message");
  private static String STR_NAME = com.CH_cl.lang.Lang.rb.getString("column_Name");

  private static String STR_SENT = com.CH_cl.lang.Lang.rb.getString("column_Sent");
  private static String STR_LINK_ID = com.CH_cl.lang.Lang.rb.getString("column_Link_ID");
  private static String STR_DATA_ID = com.CH_cl.lang.Lang.rb.getString("column_Data_ID");
  private static String STR_FETCH_COUNT = com.CH_cl.lang.Lang.rb.getString("column_Fetch_count");
  private static String STR_CREATED = com.CH_cl.lang.Lang.rb.getString("column_Created");
  private static String STR_DELIVERED = com.CH_cl.lang.Lang.rb.getString("column_Delivered");
  private static String STR_EXPIRY = com.CH_cl.lang.Lang.rb.getString("column_Expiration");
  private static String STR_PASSWORD = com.CH_cl.lang.Lang.rb.getString("column_Password");
  private static String STR_UPDATED = com.CH_cl.lang.Lang.rb.getString("column_Updated");
  private static String STR_SIZE_ON_DISK = com.CH_cl.lang.Lang.rb.getString("column_Size_on_Disk");
  private static String STR_IN_REPLY_TO = com.CH_cl.lang.Lang.rb.getString("column_In_reply_to");

  private static String STR_BUSINESS_PHONE = com.CH_cl.lang.Lang.rb.getString("column_Business_Phone");
  private static String STR_HOME_PHONE = com.CH_cl.lang.Lang.rb.getString("column_Home_Phone");

  private static String STR_TIP_MESSAGE_PRIORITY = com.CH_cl.lang.Lang.rb.getString("columnTip_Message_Priority");
  private static String STR_TIP_MESSAGE_ATTACHMENTS = com.CH_cl.lang.Lang.rb.getString("columnTip_Message_Attachments");
  private static String STR_TIP_NEW_OLD_STATUS_FLAG = com.CH_cl.lang.Lang.rb.getString("columnTip_New/Old_Status_Flag");
  private static String STR_TIP_PASSWORD = com.CH_cl.lang.Lang.rb.getString("columnTip_Password");
  private static String STR_TIP_SECURE_LOCK = com.CH_cl.lang.Lang.rb.getString("columnTip_Secure_Lock");

  static final ColumnHeaderData[] columnHeaderDatas = new ColumnHeaderData[] {
      // mail
      new ColumnHeaderData(new Object[][]
      { { null, null, null, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null},
        { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
        { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // current list columns
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(3), new Integer(6) }, // narrow list columns
          { new Integer(-106) }
        }),
      // mail Inbox
      new ColumnHeaderData(new Object[][]
      { { null, null, null, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null},
        { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
        { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // current list columns
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(3), new Integer(6) }, // narrow list columns
          { new Integer(-106) }
        }),

      // mail sent
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(2), new Integer(0), new Integer(4), new Integer(1), new Integer(5), new Integer(6) }, // current list columns
          { new Integer(2), new Integer(0), new Integer(4), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(4), new Integer(6) }, // narrow list columns
          { new Integer(-106) }
        }),
      // mail spam
      new ColumnHeaderData(new Object[][]
      { { null, null, null, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null},
        { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
        { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // current list columns
          { new Integer(2), new Integer(0), new Integer(3), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(3), new Integer(6) }, // narrow list columns
          { new Integer(-106) }
        }),
      // Drafts
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_SUBJECT, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(2), new Integer(0), new Integer(4), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(2), new Integer(0), new Integer(4), new Integer(1), new Integer(5), new Integer(6) }, // wide list columns
          { new Integer(4), new Integer(6) }, // narrow list columns
          { new Integer(-106) }
        }),
      // postings
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_POSTING, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_POSTING, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(320), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(320), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(250), new Integer(400), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 60), new Integer( 70), new Integer( 90), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(3), new Integer(2), new Integer(0), new Integer(5), new Integer(6) }, // current list columns
          { new Integer(3), new Integer(2), new Integer(0), new Integer(5), new Integer(6) }, // wide list columns
          { }, // no short list
          { new Integer(-106) }
        }),
      // chat
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_MESSAGE, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, null, null, null, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_MESSAGE, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, null, null, null, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.PRIORITY_BLANK_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(520), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(520), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(520), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(200), new Integer(400), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), null, null, null, new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 60), new Integer( 70), new Integer( 90), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), null, null, null, new Integer(120), new Integer(16) },
          { new Integer(3), new Integer(2), new Integer(5) }, // current list columns
          { new Integer(3), new Integer(2), new Integer(5) }, // wide list columns
          { }, // no short list
          { new Integer(6) } // old on the top, new on the bottom
        }),
      // Addresses
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_NAME, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, STR_E_MAIL_ADDRESS, STR_BUSINESS_PHONE, STR_HOME_PHONE, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_NAME, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, STR_E_MAIL_ADDRESS, STR_BUSINESS_PHONE, STR_HOME_PHONE, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.EMAIL_SYMBOL_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), new Integer(  0), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 70), new Integer( 70), new Integer( 90), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), new Integer(120), new Integer(110), new Integer(110), new Integer(120), new Integer(16) },
          { new Integer(5), new Integer(1), new Integer(17) }, // current list columns
          { new Integer(2), new Integer(0), new Integer(5), new Integer(1), new Integer(16), new Integer(17), new Integer(18) }, // wide list columns
          { new Integer(2), new Integer(5), new Integer(1), new Integer(17) }, // narrow list columns
          { new Integer(5), new Integer(16), new Integer(6) }
        }),
      // WhiteList
      new ColumnHeaderData(new Object[][]
        { { null, null, null, STR_FROM, STR_TO, STR_NAME, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, null, STR_E_MAIL_ADDRESS, STR_BUSINESS_PHONE, STR_HOME_PHONE, STR_EXPIRY, null },
          { STR_PRIORITY, STR_ATTACHMENT, STR_FLAG, STR_FROM, STR_TO, STR_NAME, STR_SENT, STR_LINK_ID, STR_DATA_ID, STR_FETCH_COUNT, STR_CREATED, STR_UPDATED, STR_DELIVERED, STR_SIZE_ON_DISK, STR_IN_REPLY_TO, STR_SECURE_LOCK, STR_E_MAIL_ADDRESS, STR_BUSINESS_PHONE, STR_HOME_PHONE, STR_EXPIRY, STR_PASSWORD },
          { STR_TIP_MESSAGE_PRIORITY, STR_TIP_MESSAGE_ATTACHMENTS, STR_TIP_NEW_OLD_STATUS_FLAG, null, null, null, null, null, null, null, null, null, null, null, null, STR_TIP_SECURE_LOCK, STR_TIP_PASSWORD },
          { new Integer(ImageNums.PRIORITY_BLANK_SMALL), new Integer(ImageNums.ATTACH_SMALL_BLANK), new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null, null, null, null, null, null, null, new Integer(ImageNums.LOCK_CLOSED_BLACK_SMALL), new Integer(ImageNums.EMAIL_SYMBOL_SMALL), null, null, new Integer(ImageNums.STOPWATCH16), new Integer(ImageNums.KEY16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRL, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRL, TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 85), new Integer(120), new Integer(150), TIMESTAMP_PRS, new Integer( 60), new Integer( 60), new Integer( 60), TIMESTAMP_PRS, TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 95), new Integer( 60), new Integer(16), new Integer(150), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer(  0), new Integer(  0), new Integer(  0), TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(120), TIMESTAMP_MAX, TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120), new Integer(16), new Integer(  0), new Integer(150), new Integer(150), new Integer(130), new Integer(16) },
          { new Integer(16), new Integer(16), new Integer(16), new Integer( 70), new Integer( 70), new Integer( 90), TIMESTAMP_MIN, new Integer( 50), new Integer( 50), new Integer( 50), TIMESTAMP_MIN, TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 52), new Integer( 50), new Integer(16), new Integer(120), new Integer(120), new Integer(120), new Integer(120), new Integer(16) },
          { new Integer(5) }, // current list columns
          { new Integer(2), new Integer(5), new Integer(16) }, // wide list columns
          { new Integer(5) }, // narrow list columns
          { new Integer(5), new Integer(16) }
        }),
  };



  /**
  * Creates new MsgTableModel
  * @param folderId specifies the folder for which this table will display data.
  * @param mode can either be MODE_MSG, MODE_MSG_SENT, MODE_POST, MODE_CHAT
  */
  public MsgTableModel(Long folderId, int mode) {
    super(columnHeaderDatas[mode], new FixedFilter(false));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableModel.class, "MsgTableModel()");
    messageMode = mode;
    if (folderId != null)
      initData(folderId);
    if (trace != null) trace.exit(MsgTableModel.class);
  }

  public int getMode() {
    return messageMode;
  }
  public boolean isModeAddr() {
    return messageMode == MODE_ADDRESS || messageMode == MODE_WHITELIST;
  }
  public boolean isModeMsgBody() {
    return messageMode == MODE_CHAT || messageMode == MODE_POST;
  }

  /**
  * When folders are fetched, their IDs are cached so that we know if table fetch is required when
  * user switches focus to another folder...
  * This vector should also be cleared when users are switched...
  */
  public void clearCachedFetchedFolderIDs() {
    fetchedIds.clear();
    fetchedIdsFull.clear();
  }

  /**
  * Sets auto update mode by listening on the cache contact updates.
  */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (linkListener == null) {
        linkListener = new MsgLinkListener();
        cache.addMsgLinkRecordListener(linkListener);
        dataListener = new MsgDataListener();
        cache.addMsgDataRecordListener(dataListener);
      }
    } else {
      if (linkListener != null) {
        cache.removeMsgLinkRecordListener(linkListener);
        linkListener = null;
        cache.removeMsgDataRecordListener(dataListener);
        dataListener = null;
      }
    }
    if (trace != null) trace.exit(MsgTableModel.class);
  }

  /**
  * Initializes the model setting the specified folderId as its main variable
  */
  public synchronized void initData(Long folderId) {
    initData(folderId, false);
    setCollapseFileVersions(true);
  }
  public synchronized void initData(Long folderId, boolean forceSwitch) {
    FolderPair folderPair = getParentFolderPair();
    boolean isChatting = false;
    if (folderPair != null && folderPair.getFolderRecord() != null && folderPair.getFolderRecord().isChatting())
      isChatting = true;
    // chatting folders force reload so that it will scroll to the bottom...
    boolean effectiveForceSwitch = forceSwitch || (isChatting && !isAutoScrollSuppressed);
    if (forceSwitch || effectiveForceSwitch || folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {
      setFilter(folderId != null ? (RecordFilter) new MsgFilter(Record.RECORD_TYPE_FOLDER, folderId) : (RecordFilter) new FixedFilter(false));
      switchData(folderId, effectiveForceSwitch);
      refreshData(folderId, false);
    }
  }

  /**
  * Initializes the model setting the specified folderId as its main variable
  */
  private synchronized void switchData(Long folderId, boolean forceSwitch) {
    FolderPair folderPair = getParentFolderPair();
    if (forceSwitch || folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {

      removeData();

      if (folderId != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord shareRec = cache.getFolderShareRecordMy(folderId, true);
        FolderRecord folderRec = cache.getFolderRecord(folderId);

        if (shareRec != null && folderRec != null) {

          folderPair = new FolderPair(shareRec, folderRec);
          setParentFolderPair(folderPair);

          // add all messages for this folder
          MsgLinkRecord[] linkRecords = cache.getMsgLinkRecordsForFolder(folderId);
          if (linkRecords != null && linkRecords.length > 0) {
            updateData(linkRecords);
          }
        }
      } // end if folderId != null
      else
        setParentFolderPair(null);
    }
  }


  /**
  * @param fetch true if data should be re-fetched from the database.
  */
  public synchronized void refreshData(boolean forceFetch) {
    FolderPair folderPair = getParentFolderPair();
    if (folderPair != null) {
      refreshData(folderPair.getId(), forceFetch);
    }
  }

  /**
  * Forces a refresh of data displayed even if its already displaying the specified folder data.
  */
  private synchronized void refreshData(Long folderId, boolean forceFetch) {
    if (folderId != null) {
      FolderShareRecord shareRec = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(folderId, true);
      if (shareRec != null) {
        Long shareId = shareRec.shareId;
        // fetch the messages for this folder
        fetchMsgs(shareId, folderId, forceFetch);
      }
    }
  }

  public Collection getSearchableCharSequencesFor(Object searchableObj) {
    return getSearchableCharSequencesFor(searchableObj, true);
  }
  public Collection getSearchableCharSequencesFor(Object searchableObj, boolean includeMsgBody) {
    if (searchableObj instanceof Record) {
      // The searchable string should use the possibly dynamically prepared
      // version which may include attachments, etc.  So if we don't have the 
      // rendering cache ready, initiate its creation here for more complete
      // search results.
      if (searchableObj instanceof MsgLinkRecord) {
        MsgLinkRecord msgLink = (MsgLinkRecord) searchableObj;
        if (msgLink.getPostRenderingCache() == null) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
          getSubjectColumnValue(this, msgLink, msgData, null, cache);
        }
      }
      return TextRenderer.getSearchTextFor((Record) searchableObj, isModeAddr() || !isModeMsgBody() ? includeMsgBody : true); // chat and posting folder always includes bodies
    } else {
      return null;
    }
  }

  public synchronized Object getValueAtRawColumn(Record record, int rawColumn, boolean forSortOnly) {
    Object value = null;

    if (record instanceof MsgLinkRecord) {
      MsgLinkRecord msgLink = (MsgLinkRecord) record;

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord msgData = forSortOnly ? cache.getMsgDataRecordNoTrace(msgLink.msgId) : cache.getMsgDataRecord(msgLink.msgId);

      switch (rawColumn) {
        // importance
        case 0:
          if (msgData != null) {
            if (forSortOnly) {
              short imp = msgData.importance.shortValue();
              if (msgData.dateExpired != null) {
                value = new Integer(9);
              } else if (MsgDataRecord.isImpFYI(imp)) {
                value = new Integer(1);
              } else if (MsgDataRecord.isImpHigh(imp)) {
                value = new Integer(5);
              } else if (MsgDataRecord.isImpSystem(imp)) {
                value = new Integer(7);
              } else if (MsgDataRecord.isImpNormal(imp)) {
                value = new Integer(3);
              }
            } else {
              value = msgData.importance;
            }
          }
          break;
        // attachments
        case 1:
          if (msgData != null) {
            int numOfAttachments = 0;
            if (msgData.attachedFiles != null && msgData.attachedMsgs != null) {
              numOfAttachments = msgData.attachedFiles.shortValue() + msgData.attachedMsgs.shortValue();
              // if regular email, don't show serialized email as attachment in the table...
              if (msgData.isEmail()) {
                numOfAttachments --;
              }
            }
            value = new Short((short) numOfAttachments);
          }
          break;
        // Flag
        case 2:
          boolean isStarred = msgLink.isStarred();
          boolean isFlagged = false;
          StatRecord stat = cache.getStatRecord(msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
          if (stat != null)
            isFlagged = StatRecord.getIconForFlag(stat.getFlag(isModeMsgBody())) != ImageNums.IMAGE_NONE;
          if (isStarred && isFlagged)
            value = new Short((short) 1);
          else if (isStarred)
            value = new Short((short) 2);
          else if (isFlagged)
            value = new Short((short) 3);
          else
            value = new Short((short) 4);
          break;
        // From
        case 3:
          if (msgData != null) {
            String fromEmailAddress = msgData.getFromEmailAddress();
            if (msgData.isEmail() || fromEmailAddress != null) {
              value = fromEmailAddress;
            } else {
              if (forSortOnly) {
                value = ListRenderer.getRenderedText(CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, true, true));
              } else {
                value = msgData.senderUserId;
              }
            }
          }
          break;
        // To
        case 4:
          if (msgData != null) {
            if (forSortOnly) {
              String sortStr = null;
              // just get the second token because it is usually the first recipient
              Record[][] recipients = CacheMsgUtils.gatherAllMsgRecipients(msgData.getRecipients(), 1);
              for (int i=0; sortStr==null && i<recipients.length; i++)
                for (int k=0; sortStr==null && k<recipients[i].length; k++)
                  if (recipients[i][k] != null)
                    sortStr = ListRenderer.getRenderedText(recipients[i][k]);
              value = sortStr;
            } else {
              value = msgData.getRecipients();
            }
          }
          break;
        // Subject or Posting or Address Name
        case 5:
          if (forSortOnly) {
            String text = null;
            if (msgData != null) {
              if (msgData.isTypeAddress()) {
                // sorting by fileAs
                if (getColumnHeaderData().convertRawColumnToModel(16) == -1)
                  text = msgData.fileAs + msgData.email;
                else
                  text = msgData.fileAs;
              } else if (messageMode != MODE_POST && messageMode != MODE_CHAT) {
                text = msgData.getSubject();
              } else {
                text = msgData.getSubject() + msgData.getText();
              }
            }
            value = text;
          } else {
            value = null;
          }
          break;
        // Sent Date
        case 6: value = msgLink.dateCreated;
          break;
        // Link ID
        case 7: value = msgLink.msgLinkId;
          break;
        // Msg ID
        case 8: value = msgLink.msgId;
          break;
        // Fetch count
        case 9: value = msgLink.status;
          break;
        // Created
        case 10:
          if (msgData != null)
            value = msgData.dateCreated;
          break;
        // Updated
        case 11: value = msgLink.dateUpdated;
          break;
        // Delivered
        case 12: value = msgLink.dateDelivered;
          break;
        // Size on Disk
        case 13:
          if (msgData != null)
            value = msgData.recordSize;
          break;
        // In reply to
        case 14:
          if (msgData != null)
            value = msgData.replyToMsgId;
          break;
        // Secure Lock
        case 15:
          if (msgData != null)
            value = msgData.importance;
          break;
        // Email Address
        case 16:
          if (msgData != null)
            value = msgData.email;
          break;
        // Business Phone
        case 17:
          if (msgData != null) {
            String phone = "";
            //value = "";
            if (msgData.phoneB != null && msgData.phoneB.length() > 0)
              phone = msgData.phoneB;
            // alternatively if table has no Home Phone column, append it on new line
            if (getColumnHeaderData().convertRawColumnToModel(18) == -1 && msgData.phoneH != null && msgData.phoneH.length() > 0) {
              if (phone.length() > 0)
                phone = "<html>" + phone + "<br>h: " + msgData.phoneH + "</html>";
              else
                phone = "h: " + msgData.phoneH;
            }
            value = phone;
          }
          break;
        // Home Phone
        case 18:
          if (msgData != null) {
            String phone = "";
            if (msgData.phoneH != null && msgData.phoneH.length() > 0)
              phone = msgData.phoneH;
            // alternatively if table has no Business Phone column, append it on new line
            if (getColumnHeaderData().convertRawColumnToModel(17) == -1 && msgData.phoneB != null && msgData.phoneB.length() > 0) {
              if (phone.length() > 0)
                phone = "<html>" + phone + "<br>b: " + msgData.phoneB + "</html>";
              else
                phone = "b: " + msgData.phoneB;
            }
            value = phone;
          }
          break;
        // Expiration
        case 19:
          if (msgData != null)
            value = msgData.dateExpired;
          break;
        // Password
        case 20:
          if (msgData != null)
            value = Boolean.valueOf(msgData.bodyPassHash != null);
          break;
      }

    }

    return value;
  }

  public static Object getSubjectColumnValue(MsgTableModel model, MsgLinkRecord msgLink, MsgDataRecord msgData, MsgLinkRecord prevMsgLink, FetchedDataCache cache) {
    Object value = null;
    String subject = null;
    if (msgData == null) {
      subject = "";
    } else {
      if (msgData.isTypeAddress() && !model.isModeMsgBody()) {
        if (model.isModeAddr()) {
          subject = msgData.fileAs;
        } else {
          subject = msgData.fileAs;
          if (!msgData.fileAs.equals(msgData.name))
            subject = "" + subject + " ("+msgData.name+")";
          if (msgData.email != null)
            subject = "" + subject + " <"+msgData.email+">";
        }
      } else if (msgData.isTypeMessage() && !model.isModeMsgBody()) {
        // message table with subject line only
        subject = msgData.getSubject();
      } else {
        String postRenderingCache = msgLink.getPostRenderingCache();
        if (postRenderingCache != null) {
          value = new StringBuffer(postRenderingCache);
        } else {
          String messageText = "";
          if (msgData.isTypeAddress()) {
            subject = Misc.encodePlainIntoHtml(msgData.fileAs);
            messageText = msgData.parseAddressBody(true, true);
          } else if (msgData.isTypeMessage()) {
            subject = Misc.encodePlainIntoHtml(msgData.getSubject());
            messageText = msgData.getText();
          } else {
            subject = "error: unknown type";
          }

          boolean isHTML = msgData.isHtml();
          StringBuffer sb = new StringBuffer();

          sb.append(HTML_utils.HTML_START);
          sb.append(HTML_utils.HTML_BODY_START);

          boolean toAddFrom = false;
          boolean toAddSent = false;
          boolean toAddPriority = false;
          boolean toAddAttachment = false;
          boolean toAddFlag = false;
          boolean toAddStar = false;

          String fromName = null;
          String prevFromName = null;
          // if table has no 'From' column prepend it to the body
          if (model.getColumnHeaderData().convertRawColumnToModel(3) == -1 && msgData != null) {
            fromName = getFromName(msgData);
            prevFromName = getFromName(prevMsgLink, cache);
            if (!fromName.equals(prevFromName)) {
              toAddFrom = true;
            }
          }
          // if table has no 'Sent' column prepend it to the body
          if (model.getColumnHeaderData().convertRawColumnToModel(6) == -1) {
            toAddSent = true;
          }
          // if table has no 'Priority' column prepend 'Priority' to the body
          if (model.getColumnHeaderData().convertRawColumnToModel(0) == -1) {
            toAddPriority = true;
          }
          // if table has no 'Attachment' column prepend it to the body
          if (model.getColumnHeaderData().convertRawColumnToModel(1) == -1) {
            toAddAttachment = true;
          }
          // if table has no 'Flag' column prepend it to the body
          if (model.getColumnHeaderData().convertRawColumnToModel(2) == -1) {
            toAddFlag = true;
          }
          // if table has no 'Flag' column prepend 'Star' to the body only if it is present
          if (msgLink.isStarred() && model.getColumnHeaderData().convertRawColumnToModel(2) == -1) {
            toAddStar = true;
          }

          int flagIcon = ImageNums.IMAGE_NONE;
          if (toAddFlag) {
            StatRecord stat = cache.getStatRecord(msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
            if (stat != null) {
              Short flagS = stat.getFlag(model.isModeMsgBody());
              flagIcon = StatRecord.getIconForFlag(flagS);
            }
          }
          if (toAddStar && flagIcon != ImageNums.IMAGE_NONE) {
            sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.STAR_BRIGHTER) + "\" align=\"ABSBOTTOM\" width=\"14\" height=\"14\"/>");
          } else if (toAddStar) {
            sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.STAR_BRIGHT) + "\" align=\"ABSBOTTOM\" width=\"14\" height=\"14\"/>");
          } else if (flagIcon != ImageNums.IMAGE_NONE) {
            sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(flagIcon) + "\" align=\"ABSBOTTOM\" width=\"14\" height=\"14\"/>");
          }

          if (toAddFrom || toAddSent) {
            sb.append(HTML_utils.HTML_FONT_START);
          }

          if (toAddFrom) {
            sb.append("<font color=\"#9c2950\">");
            sb.append("<strong>");
            sb.append(fromName);
            sb.append("</strong>");
          }

          if (toAddPriority) {
            if (msgData.isImpHigh())
              sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.PRIORITY_HIGH12) + "\" align=\"ABSBOTTOM\" width=\"12\" height=\"12\"/>");
            else if (msgData.isImpFYI())
              sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.PRIORITY_LOW12) + "\" align=\"ABSBOTTOM\" width=\"12\" height=\"12\"/>");
            else if (msgData.isImpSystem())
              sb.append("<img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.PRIORITY_FIRE12) + "\" align=\"ABSBOTTOM\" width=\"12\" height=\"12\"/>");
          }

          if (toAddAttachment) {
            int numOfAttachments = 0;
            if (msgData.attachedFiles != null && msgData.attachedMsgs != null) {
              numOfAttachments = msgData.attachedFiles.shortValue() + msgData.attachedMsgs.shortValue();
              // if regular email, don't show serialized email as attachment in the table...
              if (msgData.isEmail()) {
                numOfAttachments --;
              }
            }
            if (numOfAttachments > 0) {
              String linkNames = "";
              FileLinkRecord[] fLinks = cache.getFileLinkRecordsOwnerAndType(msgData.msgId, new Short(Record.RECORD_TYPE_MESSAGE));
              if (fLinks != null && fLinks.length > 0) {
                for (int i=0; i<fLinks.length; i++) {
                  if (linkNames.length() > 0)
                    linkNames += "<br>";
                  linkNames += ListRenderer.getRenderedText(fLinks[i], true, false, false, true);
                }
              } else {
                FileLinkOps.addToLinkFetchQueue(MainFrame.getServerInterfaceLayer(), msgLink.msgLinkId, msgData.msgId, Record.RECORD_TYPE_MESSAGE);
              }
              MsgLinkRecord[] mLinks = cache.getMsgLinkRecordsOwnerAndType(msgData.msgId, new Short(Record.RECORD_TYPE_MESSAGE));
              if (mLinks != null && mLinks.length > 0) {
                for (int i=0; i<mLinks.length; i++) {
                  if (linkNames.length() > 0)
                    linkNames += "<br>";
                  linkNames += ListRenderer.getRenderedText(mLinks[i], true, false, false, true);
                }
              } else {
                MsgLinkOps.addToLinkFetchQueue(MainFrame.getServerInterfaceLayer(), msgLink.msgLinkId, msgData.msgId, Record.RECORD_TYPE_MESSAGE);
              }
              String strLinked = numOfAttachments == 1 ? linkNames : "("+numOfAttachments+" items)";
              String strNotLinked = numOfAttachments == 1 ? "" : " "+linkNames+"<br>";
              sb.append("<a href=\"http://localhost/actions/706\"><img src=\"images/" + com.CH_co.util.ImageNums.getImageName(ImageNums.ATTACH_SMALL) + "\" border=\"0\" align=\"ABSBOTTOM\" width=\"14\" height=\"16\"/>"+strLinked+"</a><br>"+strNotLinked);
              if (linkNames.length() > 0)
                sb.append(" ");
            }
          }

          if (toAddSent) {
            sb.append("<font size=\"-2\" color=\"#777777\">");
            if (toAddFrom)
              sb.append(' ');
            String prevDateStr = prevMsgLink != null ? Misc.getFormattedDate(prevMsgLink.dateCreated, true, true, false) : "";
            String dateStr = Misc.getFormattedDate(msgLink.dateCreated, true, true, false);
            if (!dateStr.equals(prevDateStr))
              sb.append(dateStr);
          }

          if (toAddFrom || toAddSent) {
            sb.append("</font> ");
            sb.append(HTML_utils.HTML_FONT_END);
          }

          sb.append(HTML_utils.HTML_FONT_START);

          boolean subjectAppended = false;
          // append subject
          if (subject != null && subject.length() > 0) {
            sb.append("<b>");
            sb.append(subject);
            sb.append("</b> ");
            subjectAppended = true;
          }
          // append body
          if (messageText != null && messageText.length() > 0) {
            // prepare message text
            if (!isHTML) {
              // If it is a PLAIN mail, then convert special characters <>& characters to entities.
              messageText = msgData.getEncodedHTMLData();
            } else {
              // simplify HTML message for chat display - also converting <p> to <br> to keep it simpler
              messageText = HTML_Ops.clearHTMLheaderAndConditionForDisplay(messageText, true, true, true, true, true, true, true);
              // move the BODY tag right after the HTML tag...
              int iBody1 = messageText.indexOf("<body");
              if (iBody1 < 0)
                iBody1 = messageText.indexOf("<BODY");
              int iBody2 = messageText.indexOf('>', iBody1);
              if (iBody1 >= 0 && iBody2 > iBody1) {
                sb.insert("<html>".length(), messageText.substring(iBody1, iBody2 + ">".length()));
                messageText = messageText.substring(iBody2 + ">".length());
              }
            }
            // skip the unnecessary line break if no subject was added
            if (subjectAppended) {
              sb.append("<br>");
            }
            sb.append(messageText);
          }
          sb.append(HTML_utils.HTML_FONT_END);
          sb.append(HTML_utils.HTML_BODY_END);
          sb.append(HTML_utils.HTML_END);
          value = sb;
          msgLink.setPostRenderingCache(sb.toString());
        }
      }
    }
    if (value == null)
      value = subject;
    return value;
  }

  private static String getFromName(MsgLinkRecord msgLink, FetchedDataCache cache) {
    return msgLink != null ? getFromName(cache.getMsgDataRecord(msgLink.msgId)) : null;
  }

  private static String getFromName(MsgDataRecord msgData) {
    String fromName = null;
    if (msgData != null) {
      String fromEmailAddress = msgData.getFromEmailAddress();
      if (msgData.isEmail() || fromEmailAddress != null) {
        fromName = fromEmailAddress;
      } else {
        fromName = ListRenderer.getRenderedText(CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, true, true));
      }
    }
    return fromName;
  }

  public void clearMsgPostRenderingCache() {
    FolderPair fp = getParentFolderPair();
    if (fp != null && fp.getId() != null) {
      MsgLinkRecord[] mLinks = FetchedDataCache.getSingleInstance().getMsgLinkRecordsForFolder(fp.getId());
      MsgLinkRecord.clearPostRenderingCache(mLinks);
    }
  }

  public RecordTableCellRenderer createRenderer() {
    if (messageMode == MODE_MSG || messageMode == MODE_MSG_INBOX || messageMode == MODE_MSG_SPAM)
      return new MsgTableCellRenderer();
    else if (messageMode == MODE_MSG_SENT)
      return new MsgSentTableCellRenderer();
    else if (messageMode == MODE_DRAFTS)
      return new MsgDraftsTableCellRenderer();
    else if (messageMode == MODE_POST || messageMode == MODE_CHAT)
      return new PostTableCellRenderer();
    else if (messageMode == MODE_ADDRESS || messageMode == MODE_WHITELIST)
      return new AddressTableCellRenderer();
    else if (true)
      throw new IllegalStateException("Don't know how to handle messageMode " + messageMode);
    return null;
  }

  private boolean isFilteringBodies() {
    RecordFilter filter = getFilterNarrowing();
    return filter instanceof TextSearchFilter && ((TextSearchFilter) filter).isIncludingMsgBodies();
  }

  /**
  * Send a request to fetch message briefs or full (depending on messages or postings)
  * for the <code> shareId </code> from the server
  * if messages were not fetched for this folder, otherwise get them from cache.
  * @param force true to force a fetch from the database
  */
  private void fetchMsgs(final Long shareId, final Long folderId, boolean force) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableModel.class, "fetchMsgs(Long shareId, Long folderId, boolean force)");
    if (trace != null) trace.args(shareId, folderId);
    if (trace != null) trace.args(force);

    synchronized (fetchedIds) {
      if (force || !fetchedIds.contains(shareId) || (isFilteringBodies() && !fetchedIdsFull.contains(shareId))) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();

        // if refreshing and folder previously fetched, remove message links from the cache, leave the message data records
        if (force && fetchedIds.contains(shareId)) {
          int rowCount = getRowCount();
          ArrayList linksToRemoveL = new ArrayList();
          for (int row=0; row<rowCount; row++) {
            Record rec = getRowObjectNoTrace(row);
            if (rec instanceof MsgLinkRecord) {
              linksToRemoveL.add(rec);
            }
          }
          if (linksToRemoveL.size() > 0) {
            MsgLinkRecord[] mRecs = new MsgLinkRecord[linksToRemoveL.size()];
            linksToRemoveL.toArray(mRecs);
            fetchedIds.remove(shareId);
            fetchedIdsFull.remove(shareId);
            Long[] ownerMsgIDs = MsgLinkRecord.getMsgIDs(mRecs);
            cache.removeMsgLinkRecords(mRecs);
            // also remove any attachment Msg and File links as they can be refetched and rendering refreshed.
            cache.removeFileLinkRecords(cache.getFileLinkRecordsOwnersAndType(ownerMsgIDs, new Short(Record.RECORD_TYPE_MESSAGE)));
            cache.removeMsgLinkRecords(cache.getMsgLinkRecordsOwnersAndType(ownerMsgIDs, new Short(Record.RECORD_TYPE_MESSAGE)));
          }
        }

        // if we should frech only when we already have the folder-share pair, or they weren't already deleted
        if (cache.getFolderShareRecord(shareId) != null &&
            cache.getFolderRecord(folderId) != null &&
            !cache.getFolderRecord(folderId).isCategoryType()) {

          FolderRecord folder = cache.getFolderRecord(folderId);
          if (folder != null) FolderRecUtil.markFolderViewInvalidated(folder.folderId, false);
          if (folder != null) FolderRecUtil.markFolderFetchRequestIssued(folder.folderId);

          final boolean _isFilteringBodies = isFilteringBodies();
          final int _action = (messageMode == MODE_POST || messageMode == MODE_CHAT || _isFilteringBodies) ? CommandCodes.MSG_Q_GET_FULL : CommandCodes.MSG_Q_GET_BRIEFS;

          // order of fetching is from newest to oldest
          short fetchNumMax = -Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE;

          // <shareId> <ownerObjType> <ownerObjId> <fetchNum> <timestamp>
          Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, fetchNumMax, (short) Msg_GetMsgs_Rq.FETCH_NUM_NEW__INITIAL_SIZE, (Timestamp) null);

          // Gather items already fetched so we don't re-fetch all items if not necessary.
          // Useful when performing fetch for content searches when user clicks body filtering on and off a few times -- it will skip previously fetched bodies and continue on...
          if (_action == CommandCodes.MSG_Q_GET_FULL) {
            MsgLinkRecord[] existingLinks = CacheMsgUtils.getMsgLinkRecordsWithFetchedDatas(folderId);
            request.exceptLinkIDs = RecordUtils.getIDs(existingLinks);
          } else {
            MsgLinkRecord[] existingLinks = FetchedDataCache.getSingleInstance().getMsgLinkRecordsForFolder(folderId);
            request.exceptLinkIDs = RecordUtils.getIDs(existingLinks);
          }

          Interrupter msgInterrupter = new Interrupter() {
            public boolean isInterrupted() {
              FolderPair fPair = getParentFolderPair();
              return fPair == null || !fPair.getId().equals(folderId) || (_isFilteringBodies && !isFilteringBodies());
            }
          };
          final boolean[] addedFetchIDs = new boolean[] { false, false };
          Interruptible msgInterruptible = new Interruptible() {
            public void interrupt() {
              if (addedFetchIDs[1]) {
                fetchedIdsFull.remove(shareId);
              }
              if (addedFetchIDs[0]) {
                fetchedIds.remove(shareId);
                // since we interrupted our basic data fetch, see if we should finish it... applies when canceling Msg BODY search without having basic one completed
                FolderPair fPair = getParentFolderPair();
                if (fPair != null) {
                  if (fPair.getFolderShareRecord() != null && fPair.getFolderRecord() != null && fPair.getId().equals(folderId))
                    fetchMsgs(shareId, folderId, false);
                }
              }
            }
          };
          MessageAction msgAction = new MessageAction(_action, request, msgInterrupter, msgInterruptible);
          Runnable replyReceivedJob = new Runnable() {
            public void run() {
              if (!fetchedIds.contains(shareId)) {
                fetchedIds.add(shareId);
                addedFetchIDs[0] = true;
              }
              if (_action == CommandCodes.MSG_Q_GET_FULL && _isFilteringBodies) {
                if (!fetchedIdsFull.contains(shareId)) {
                  fetchedIdsFull.add(shareId);
                  addedFetchIDs[1] = true;
                }
              }
            }
          };
          Runnable afterJob = new Runnable() {
            public void run() {
              FolderRecord folder = FetchedDataCache.getSingleInstance().getFolderRecord(folderId);
              if (folder != null) FolderRecUtil.markFolderViewInvalidated(folder.folderId, false);
            }
          };
          MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 30000, replyReceivedJob, afterJob, afterJob);
        }
      }
    } // end synchronized

    if (trace != null) trace.exit(MsgTableModel.class);
  }

  /**
  * Checks if folder share's content of a given ID was already retrieved.
  */
  public boolean isContentFetched(Long shareId) {
    synchronized (fetchedIds) {
      return fetchedIds.contains(shareId);
    }
  }


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE ****************************************/
  /****************************************************************************************/

  /** Listen on updates to the MsgLinkRecords in the cache.
    * if the event happens, add, move or remove message links.
    */
  private class MsgLinkListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new MsgGUIUpdater(event));
    }
  }
  private class MsgDataListener implements MsgDataRecordListener {
    public void msgDataRecordUpdated(MsgDataRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new MsgGUIUpdater(event));
    }
  }

  private class MsgGUIUpdater implements Runnable {
    private RecordEvent event;
    public MsgGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater.run()");

      if (event instanceof MsgLinkRecordEvent) {
        Record[] records = event.getRecords();
        if (event.getEventType() == RecordEvent.SET) {
          updateData(records);
        } else if (event.getEventType() == RecordEvent.REMOVE) {
          removeData(records);
        }
      } else if (event instanceof MsgDataRecordEvent) {
        // and changes to Address Records may affect the rendering of the table rows...
        FolderPair parentPair = MsgTableModel.this.getParentFolderPair();
        // only for message folders, exclude posting folders
        if (parentPair != null && parentPair.getFolderRecord().folderType.shortValue() == FolderRecord.MESSAGE_FOLDER) {
          if (event.getEventType() == RecordEvent.SET) {
            Record[] records = event.getRecords();
            for (int i=0; i<records.length; i++) {
              if (((MsgDataRecord) records[i]).isTypeAddress()) {
                fireTableRowsUpdated(0, MsgTableModel.this.getRowCount() -1);
                break;
              }
            }
          }
        }
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
  }

  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

}