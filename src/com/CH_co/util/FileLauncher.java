/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
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
import java.io.IOException;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class FileLauncher {

  private static FileOpener primaryFileOpener;

  public static boolean openFile(Object context, FileDataRecord fileRec) {
    boolean cachedFileOpened = false;
    if (fileRec != null &&
        fileRec.getPlainDataFile() != null &&
        fileRec.getPlainDataFile().exists())
    {
      if (primaryFileOpener != null) {
        cachedFileOpened = primaryFileOpener.open(context, fileRec.getPlainDataFile());
      } else {
        try {
          File fileToOpen = fileRec.getPlainDataFile();
  //        if (isAudioWaveFilename(fileToOpen.getName()))
  //          DecodingAudioPlayer.play(fileToOpen);
  //        else if (isImageFilename(fileToOpen.getName()))
  //          ImageViewer.showImage(fileToOpen);
  //        else
            BrowserLauncher.openFile(fileToOpen);
          cachedFileOpened = true;
        } catch (IOException x1) {
        }
      }
    }
    return cachedFileOpened;
  }

  public static void setPrimaryFileOpener(FileOpener fileOpener) {
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

  public interface FileOpener {
    public boolean open(Object context, File file);
  }
}