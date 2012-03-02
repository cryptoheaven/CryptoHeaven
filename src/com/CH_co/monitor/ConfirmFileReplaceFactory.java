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

package com.CH_co.monitor;

import com.CH_co.service.records.FileDataRecord;
import java.io.File;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class ConfirmFileReplaceFactory {

  public static Class confirmFileReplaceImpl;

  public static void setImpl(Class impl) {
    confirmFileReplaceImpl = impl;
  }
  public static Class getImplementation() {
    return confirmFileReplaceImpl;
  }

  public static ConfirmFileReplaceI newInstance(File originalFile, Long newSize, FileDataRecord newFile) {
    ConfirmFileReplaceI impl = null;
    try {
      impl = (ConfirmFileReplaceI) confirmFileReplaceImpl.newInstance();
      impl.init(originalFile, newSize, newFile);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return impl;
  }

}