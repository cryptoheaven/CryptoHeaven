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

package com.CH_co.service.records;

import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
abstract public class FileRecord extends Record {

  /** Creates new FileRecord */
  public FileRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileRecord.class, "FileRecord()");
    if (trace != null) trace.exit(FileRecord.class);
  }

  abstract public Long getParentId();
  abstract public Long getFileViewParentId();

  abstract public String getMyName();

  /**
   * Default comparison is made by object ID but this is rather user unfriendly comparison.
   * We overwrite with name comparison.
   */
  public int compareTo(Object record) {
    if (record instanceof FileRecord)
      return getMyName().compareToIgnoreCase( ((FileRecord) record).getMyName() );
    else
      return super.compareTo(record);
  }

  /**
   * @return only the children of the specified parent.  
   * FileLinkRecord(s) and FolderPair(s) are filtered.
   */
  public static FileRecord[] filterChildren(Record[] records, Long parentId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileRecord.class, "filterChildren(Record[] records, Long parentId)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(parentId);

    Vector recordsV = new Vector();
    for (int i=0; records != null && i<records.length; i++) {
      if (records[i] instanceof FileRecord) {
        FileRecord fRec = (FileRecord) records[i];
        if (fRec.getParentId().equals(parentId) && !fRec.getId().equals(fRec.getParentId())) {
          recordsV.addElement(fRec);
        }
      }
    }
    FileRecord[] fileRecords = (FileRecord[]) ArrayUtils.toArray(recordsV, FileRecord.class);

    if (trace != null) trace.exit(FileRecord.class, fileRecords);
    return fileRecords;
  }

}