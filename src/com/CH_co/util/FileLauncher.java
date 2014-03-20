/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.util;

import com.CH_co.service.records.FileDataRecord;
import java.io.File;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class FileLauncher {

  private static FileOpenerI primaryFileOpener;

  public static boolean openFile(Object context, FileDataRecord fileRec) {
    boolean cachedFileOpened = false;
    if (primaryFileOpener != null
            && fileRec != null
            && fileRec.getPlainDataFile() != null
            && fileRec.getPlainDataFile().exists())
    {
      cachedFileOpened = primaryFileOpener.openFile(context, fileRec.getPlainDataFile());
    }
    return cachedFileOpened;
  }

  public static void setPrimaryFileOpener(FileOpenerI fileOpener) {
    primaryFileOpener = fileOpener;
  }

  public static String getVoicemailPrefix() {
    return "Voice-";
  }

  public static boolean isAudioWaveFilename(String filename) {
    filename = filename.toLowerCase();
    return filename.endsWith(".wav");
  }

  public static boolean isImageFilename(String filename) {
    filename = filename.toLowerCase();
    return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif") || filename.endsWith(".png");
  }

  public interface FileOpenerI {
    public boolean openFile(Object context, File file);
  }
}