/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.tree;

import java.io.Serializable;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FolderDND_TransferableData extends Object implements Serializable {
  public Long[] folderIDs;
  public FolderDND_TransferableData() {
  }
  public FolderDND_TransferableData(Long[] folderIDs) {
    this.folderIDs = folderIDs;
  }
}