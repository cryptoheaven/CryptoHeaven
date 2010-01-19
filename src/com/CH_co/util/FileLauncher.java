/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.util;

import java.io.IOException;
import com.CH_co.service.records.FileDataRecord;
import java.io.File;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileLauncher {

  public static boolean openFile(FileDataRecord fileRec) {
    boolean cachedFileOpened = false;
    if (fileRec != null &&
        fileRec.getPlainDataFile() != null &&
        fileRec.getPlainDataFile().exists())
    {
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
    return cachedFileOpened;
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
}