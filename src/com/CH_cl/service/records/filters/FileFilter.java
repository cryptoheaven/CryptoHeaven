/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.records.filters;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.13 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileFilter extends AbstractRecordFilter implements RecordFilter {

  // keep only Files/Folders with given viewParentId;
  private Long viewParentId;
  // keep root folders too?
  private boolean rootOk;

  /** Creates new FileFilter */
  public FileFilter(Long viewParentId, boolean rootOk) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileFilter.class, "FileFilter(Long viewParentId, boolean rootOk)");
    if (trace != null) trace.args(viewParentId);
    if (trace != null) trace.args(rootOk);

    this.viewParentId = viewParentId;
    this.rootOk = rootOk;

    if (trace != null) trace.exit(FileFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof FileRecord) {
      FileRecord fRec = (FileRecord) record;
      if (fRec.getFileViewParentId().equals(viewParentId)) { 
        if (rootOk || !fRec.getId().equals(viewParentId)) {
          keep = true;
        }
      }
    }

    return keep;
  }

}