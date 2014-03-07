/*
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.util;

import com.CH_co.util.*;

import java.io.File;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class TempFile extends File {

  public TempFile(File parentDir, String childName) {
    super(parentDir, childName);
    deleteOnExit();
    GlobalProperties.addTempFileToCleanup(this);
  }

  public void cleanup() {
    try { CleanupAgent.wipeOrDelete(this); } catch (Throwable t) { }
  }

  protected void finalize() throws Throwable {
    cleanup();
    super.finalize();
  }
}