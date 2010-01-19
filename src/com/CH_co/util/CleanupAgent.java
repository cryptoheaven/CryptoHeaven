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

import com.CH_co.cryptx.Rnd;
import com.CH_co.io.FileUtils;
import com.CH_co.io.RandomInputStream;
import com.CH_co.monitor.ProgMonitor;
import java.awt.Component;
import java.io.*;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class CleanupAgent extends Thread {

  public static final int MODE_FINALIZATION = 1;
  public static final int MODE_GC = 2;
  public static final int MODE_TEMP_FILE_CLEANER = 4;

  private static CleanupAgent singleInstance;
  private static final Object singleInstanceMonitor = new Object();

  private long delayMillisHeartbeat;
  private long delayMillisFinalization, delayMillisFinalizationFirst, delayMillisFinalizationNext;
  private long delayMillisGC, delayMillisGCFirst, delayMillisGCNext;
  private long delayMillisFileCleaner, delayMillisFileCleanerFirst, delayMillisFileCleanerNext;
  private int mode;
  private String[][] fileSpecs;

  private long delayMillisFinalizationAccumulator;
  private long delayMillisGCAccumulator;
  private long delayMillisFileCleanerAccumulator;


  public static void startSingleInstance(int mode, String[][] fileSpecs, int delayMinutesHeartbeat, 
          int delayMinutesFinalizationFirst, int delayMinutesFinalizationNext,
          int delayMinutesGCFirst, int delayMinutesGCNext,
          int delayMinutesFileCleanerFirst, int delayMinutesFileCleanerNext) {
    synchronized (singleInstanceMonitor) {
      if (singleInstance == null) {
        singleInstance = new CleanupAgent(mode, fileSpecs, delayMinutesHeartbeat, 
                delayMinutesFinalizationFirst, delayMinutesFinalizationNext,
                delayMinutesGCFirst, delayMinutesGCNext,
                delayMinutesFileCleanerFirst, delayMinutesFileCleanerNext);
        singleInstance.start();
      }
    }
  }


  /** Creates new CleanupAgent */
  private CleanupAgent(int mode, String[][] fileSpecs, int delayMinutesHeartbeat, 
          int delayMinutesFinalizationFirst, int delayMinutesFinalizationNext,
          int delayMinutesGCFirst, int delayMinutesGCNext,
          int delayMinutesFileCleanerFirst, int delayMinutesFileCleanerNext) {
    super("CleanupAgent");
    this.mode = mode;
    this.fileSpecs = fileSpecs;
    this.delayMillisHeartbeat = delayMinutesHeartbeat * 60L * 1000L;
    this.delayMillisFinalizationFirst = delayMinutesFinalizationFirst * 60L * 1000L;
    this.delayMillisFinalizationNext = delayMinutesFinalizationNext * 60L * 1000L;
    this.delayMillisGCFirst = delayMinutesGCFirst * 60L * 1000L;
    this.delayMillisGCNext = delayMinutesGCNext * 60L * 1000L;
    this.delayMillisFileCleanerFirst = delayMinutesFileCleanerFirst * 60L * 1000L;
    this.delayMillisFileCleanerNext = delayMinutesFileCleanerNext * 60L * 1000L;

    delayMillisFinalization = delayMillisFinalizationFirst;
    delayMillisGC = delayMillisGCFirst;
    delayMillisFileCleaner = delayMillisFileCleanerFirst;

    setDaemon(true);
  }


  public void run() {
    while (true) {
      try {
        Thread.sleep(delayMillisHeartbeat);
        if ((mode & MODE_FINALIZATION) != 0)
          delayMillisFinalizationAccumulator += delayMillisHeartbeat;
        if ((mode & MODE_GC) != 0)
          delayMillisGCAccumulator += delayMillisHeartbeat;
        if ((mode & MODE_TEMP_FILE_CLEANER) != 0)
          delayMillisFileCleanerAccumulator += delayMillisHeartbeat;
      } catch (Throwable t) { }
      if (delayMillisFinalizationAccumulator >= delayMillisFinalization && (mode & MODE_FINALIZATION) != 0) {
        delayMillisFinalizationAccumulator -= delayMillisFinalization;
        try {
          System.runFinalization();
        } catch (Throwable t) { }
        // next cycle will use NEXT delay
        delayMillisFinalization = delayMillisFinalizationNext;
      }
      if (delayMillisGCAccumulator >= delayMillisGC && (mode & MODE_GC) != 0) {
        delayMillisGCAccumulator -= delayMillisGC;
        try {
          System.gc();
        } catch (Throwable t) { }
        // next cycle will use NEXT delay
        delayMillisGC = delayMillisGCNext;
      }
      if (delayMillisFileCleanerAccumulator >= delayMillisFileCleaner && (mode & MODE_TEMP_FILE_CLEANER) != 0) {
        delayMillisFileCleanerAccumulator -= delayMillisFileCleaner;
        try {
          String tempDirName = System.getProperty("java.io.tmpdir");
          if (tempDirName != null && tempDirName.length() > 0) {
            final File tempDir = new File(tempDirName);
            File[] filesToDelete = tempDir.listFiles(new FilenameFilter() {
              public boolean accept(File dir, String name) {
                boolean dirOk = tempDir.equals(dir);
                boolean nameOk = false;
                if (dirOk) {
                  for (int i=0; fileSpecs!=null && i<fileSpecs.length; i++) {
                    String prefix = fileSpecs[i][0];
                    String postfix = fileSpecs[i][1];
                    if ((prefix != null && prefix.length() > 0) || (postfix != null && postfix.length() > 0)) {
                      if ((prefix == null || name.startsWith(prefix)) && (postfix == null || name.endsWith(postfix))) {
                        nameOk = true;
                        break;
                      }
                    }
                  }
                }
                return dirOk && nameOk;
              }
            });
            long now = System.currentTimeMillis();
            long recentPast = now - delayMillisFileCleanerNext; // if the files are at least as the longer delay cycle
            for (int i=0; filesToDelete!=null && i<filesToDelete.length; i++) {
              File fileToDel = filesToDelete[i];
              long lastModified = fileToDel.lastModified();
              if (lastModified > 0 && lastModified < recentPast) {
                try { wipeOrDelete(fileToDel); } catch (Throwable t) { }
              }
            }
          }
        } catch (Throwable t) { }
        // next cycle will use NEXT delay
        delayMillisFileCleaner = delayMillisFileCleanerNext;
      }
    }
  } // end run()


  public static boolean wipeOrDelete(File file) {
    boolean rc = wipe(file);
    if (!rc) {
      rc = file.delete();
    }
    return rc;
  }

  private static boolean wipe(File file) {
    return wipe(file, new RandomInputStream(Rnd.getSecureRandom()), null, null);
  }

  public static boolean wipe(File file, InputStream randomIn, Component parent, ProgMonitor progMonitor) {
    boolean rc = false;
    try {
      if (file.isFile()) {
        // overwrite contents
        if (!file.canWrite()) {
          if (parent != null) {
            MessageDialog.showErrorDialog(parent, com.CH_gui.lang.Lang.rb.getString("msg_You_do_not_have_a_write_privilege_neccessary_to_wipe_the_contents_of_the_file_\n") + file.getAbsolutePath(), com.CH_gui.lang.Lang.rb.getString("msgTitle_Wipe_Error"));
          }
        }
        else {
          // when user requests wiping, wipe twice... otherwise once
          overwriteFile(randomIn, file, progMonitor, 1);
          if (progMonitor != null) {
            // overwrite the file twice to make it extra hard to forensic sciences to recover data.
            overwriteFile(randomIn, file, progMonitor, 2);
          }
        }
      }
      else if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (int i=0; i<files.length; i++) {
          if (!wipe(files[i], randomIn, parent, progMonitor))
            break;
        }
      }
      File tempFile = File.createTempFile("junk", "tmp", file.getParentFile());
      tempFile.delete();

      String task = null;
      if (file.isFile())
        task = com.CH_gui.lang.Lang.rb.getString("task_Deleting_File") + " ";
      else
        task = com.CH_gui.lang.Lang.rb.getString("task_Deleting_Directory") + " ";

      if (progMonitor != null) {
        progMonitor.setCurrentStatus(task + file.getName());
        progMonitor.appendLine(" " + task + file.getAbsolutePath());
      }

      boolean renamed = file.renameTo(tempFile);
      if (renamed)  {
        rc = tempFile.delete();
      } else {
        rc = file.delete();
      }

    } catch (Throwable t) {
      rc = false;
      if (parent != null) {
        MessageDialog.showErrorDialog(parent, com.CH_gui.lang.Lang.rb.getString("msg_Error_occurred_while_attempting_to_securely_wipe_the_contents_of_the_file_\n") + file.getAbsolutePath() + "\n" + t.getMessage(), com.CH_gui.lang.Lang.rb.getString("msgTitle_Wipe_Error"));
      }
    }
    return rc;
  }

  private static void overwriteFile(InputStream randomIn, File file, ProgMonitor progMonitor, int pass) throws FileNotFoundException, IOException {
    long len = file.length();
    if (progMonitor != null) {
      progMonitor.setTransferSize(len);
      String passS = " " + java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("[Pass_{0}/2]"), new Object[] {new Integer(pass)});
      String task = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("Wiping_{0}_{1}"), new Object[] {file.getName(), passS});
      progMonitor.setCurrentStatus(task);
      progMonitor.nextTask(task);
      progMonitor.appendLine(" " + java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("Wiping_{0}_{1}"), new Object[] {file.getAbsolutePath(), passS}));
      progMonitor.setFileNameSource(com.CH_gui.lang.Lang.rb.getString("label_Secure_Random_Stream"));
      progMonitor.setFileNameDestination(file.getAbsolutePath());
    }
    try {
      RandomAccessFile raf = new RandomAccessFile(file, "rw");
      FileUtils.moveData(new DataInputStream(randomIn), raf, len, progMonitor);
      raf.close();
    } catch (Throwable t) {
      FileOutputStream fos = new FileOutputStream(file);
      FileUtils.moveData(new DataInputStream(randomIn), fos, len, progMonitor);
      fos.flush();
      fos.close();
    }
  }

}