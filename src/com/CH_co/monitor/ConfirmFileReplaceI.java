/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.monitor;

import com.CH_co.service.records.FileDataRecord;
import java.io.File;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface ConfirmFileReplaceI {

  public File getRenamdFile();
  public void init(File originalFile, Long newSize, FileDataRecord newFile);
  public boolean isRename();
  public boolean isReplace();

}