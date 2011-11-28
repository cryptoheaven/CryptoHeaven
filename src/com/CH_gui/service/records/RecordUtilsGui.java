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

package com.CH_gui.service.records;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.util.*;

import javax.swing.Icon;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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