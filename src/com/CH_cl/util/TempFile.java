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
package com.CH_cl.util;

import com.CH_co.util.*;

import java.io.File;

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