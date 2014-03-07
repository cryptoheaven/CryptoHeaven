/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.gui;

import java.io.File;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface FileChooserI {

  public File getSelectedFile();
  public void setDialogTitle(String title);
  public void setSelectedFile(File file);
  public int showOpenDialog(Object parentComponent);
  public boolean isApproved(int retVal);

}