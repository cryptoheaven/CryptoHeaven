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

package com.CH_gui.service.records;

import com.CH_co.service.records.FileLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_co.util.ImageNums;
import com.CH_gui.util.FileTypesIcons;
import com.CH_gui.util.Images;
import javax.swing.Icon;

/**
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
*
* @author  Marcin Kurzawa
* @version
*/
public class RecordUtilsGui {

  public static Icon getIcon(Record rec) {
    int iconIndex = rec.getIcon();
    Icon icon = null;
    try {
      if (iconIndex == ImageNums.IMAGE_SPECIAL_HANDLING) {
        if (rec instanceof FileLinkRecord) {
          String fileName = ((FileLinkRecord) rec).getFileName();
          icon = fileName != null ? FileTypesIcons.getFileIcon(fileName) : null;
        }
      } else {
        icon = Images.get(iconIndex);
      }
    } catch (OutOfMemoryError err) {
    }
    return icon;
  }

}