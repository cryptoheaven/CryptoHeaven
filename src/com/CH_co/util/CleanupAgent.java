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

import com.CH_co.cryptx.Rnd;
import com.CH_co.io.FileUtils;
import com.CH_co.io.RandomInputStream;
import com.CH_co.lang.LangCo;
import com.CH_co.monitor.ProgMonitorI;

import java.io.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
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


  /**
   * Start without file cleaner
   */
  public static void startSingleInstance(int mode, String[][] fileSpecs,
          int delayMinutesFinalizationFirst, int delayMinutesFinalizationNext,
          int delayMinutesGCFirst, int delayMinutesGCNext) {
    synchronized (singleInstanceMonitor) {
      if (singleInstance == null) {
        singleInstance = new CleanupAgent(mode, fileSpecs,
                delayMinutesFinalizationFirst, delayMinutesFinalizationNext,
                delayMinutesGCFirst, delayMinutesGCNext,
                0, 0);
        singleInstance.start();
      }
    }
  }
  /**
   * Start single instance with all functions
   */
  public static void startSingleInstance(int mode, String[][] fileSpecs,
          int delayMinutesFinalizationFirst, int delayMinutesFinalizationNext,
          int delayMinutesGCFirst, int delayMinutesGCNext,
          int delayMinutesFileCleanerFirst, int delayMinutesFileCleanerNext) {
    synchronized (singleInstanceMonitor) {
      if (singleInstance == null) {
        singleInstance = new CleanupAgent(mode, fileSpecs,
                delayMinutesFinalizationFirst, delayMinutesFinalizationNext,
                delayMinutesGCFirst, delayMinutesGCNext,
                delayMinutesFileCleanerFirst, delayMinutesFileCleanerNext);
        singleInstance.start();
      }
    }
  }

  public static void stopSingleInstance() {
    synchronized (singleInstanceMonitor) {
      if (singleInstance != null) {
        singleInstance.interrupt();
      }
    }
  }


  /** Creates new CleanupAgent */
  private CleanupAgent(int mode, String[][] fileSpecs,
          int delayMinutesFinalizationFirst, int delayMinutesFinalizationNext,
          int delayMinutesGCFirst, int delayMinutesGCNext,
          int delayMinutesFileCleanerFirst, int delayMinutesFileCleanerNext) {
    super("CleanupAgent");
    this.mode = mode;
    this.fileSpecs = fileSpecs;
    this.delayMillisHeartbeat = 3L * 1000L; // heartbeat every 3 seconds and check timers/conditions
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
    while (!isInterrupted()) {
      try {
        Thread.sleep(delayMillisHeartbeat);
        if ((mode & MODE_FINALIZATION) != 0)
          delayMillisFinalizationAccumulator += delayMillisHeartbeat;
        if ((mode & MODE_GC) != 0)
          delayMillisGCAccumulator += delayMillisHeartbeat;
        if ((mode & MODE_TEMP_FILE_CLEANER) != 0)
          delayMillisFileCleanerAccumulator += delayMillisHeartbeat;
      } catch (InterruptedException e) {
        break;
      } catch (Throwable t) {
      }
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
      // if file cleaning enabled
      if (delayMillisFileCleanerFirst > 0) {
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
      } // end if enabled
    }
    // when thread exits, clean up the single instance so next one can be restarted
    synchronized (singleInstanceMonitor) {
      singleInstance = null;
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
    return wipe(file, new RandomInputStream(Rnd.getSecureRandom()), null, false, null);
  }

  public static boolean wipe(File file, InputStream randomIn, ProgMonitorI progMonitor, boolean wipeTwice, StringBuffer errBuffer) {
    boolean rc = false;
    
    String path = "";
    try {
      File parent = file.getParentFile();
      String name = file.getName();
      path = file.getAbsolutePath();
      
      if (file.isFile()) {
        // overwrite contents
        if (!file.canWrite()) {
          if (errBuffer != null) {
            errBuffer.append(LangCo.rb.getString("msg_You_do_not_have_a_write_privilege_neccessary_to_wipe_the_contents_of_the_file_\n")).append(file.getAbsolutePath());
            errBuffer.append("\n\n");
          }
        }
        else {
          // get available temp name for renaming
          File tempName = File.createTempFile(name, "tmp", parent);
          tempName.delete();
          // Create our own File instance so we don't change the callers reference.
          file = new File(parent, name);
          // Rename 1st to decouple from any file monitors that track file content changes
          // so this new garbage content is not treated as user's file edit save.
          boolean renamed = file.renameTo(tempName);
          if (renamed)
            file = tempName;
          else
            throw new IllegalStateException("File is locked by another process.");

          // when user requests wiping, wipe twice... otherwise once
          overwriteFile(randomIn, file, name, path, progMonitor, 1, wipeTwice ? 2 : 1);
          if (wipeTwice) {
            // overwrite the file twice to make it extra hard to forensic sciences to recover data.
            overwriteFile(randomIn, file, name, path, progMonitor, 2, 2);
          }
        }
      }
      else if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (int i=0; i<files.length; i++) {
          if (!wipe(files[i], randomIn, progMonitor, wipeTwice, errBuffer))
            break;
        }
      }
      File tempFile = File.createTempFile("junk", "tmp", parent);
      tempFile.delete();

      String task = null;
      if (file.isFile())
        task = LangCo.rb.getString("task_Deleting_File") + " ";
      else
        task = LangCo.rb.getString("task_Deleting_Directory") + " ";

      if (progMonitor != null) {
        progMonitor.setCurrentStatus(task + name);
        progMonitor.appendLine(" " + task + path);
      }

      boolean renamed = file.renameTo(tempFile);
      if (renamed)  {
        rc = tempFile.delete();
      } else {
        rc = file.delete();
      }

    } catch (Throwable t) {
      rc = false;
      if (errBuffer != null) {
        errBuffer.append(LangCo.rb.getString("msg_Error_occurred_while_attempting_to_securely_wipe_the_contents_of_the_file_\n")).append(path).append("\n").append(t.getMessage());
        errBuffer.append("\n\n");
      }
    }
    return rc;
  }

  /**
   * Overwrites contents of the file with randomness.
   * @param randomIn Source of randomness.
   * @param file File which to overwrite (usually already renamed from original source file)
   * @param name Name of the original file before rename to be used for user's progress reporting.
   * @param path Abstract path name of the original file before rename to be used for user's progress reporting.
   * @param progMonitor
   * @param pass 1st or 2nd
   * @throws FileNotFoundException
   * @throws IOException 
   */
  private static void overwriteFile(InputStream randomIn, File file, String name, String path, ProgMonitorI progMonitor, int pass, int maxPasses) throws FileNotFoundException, IOException {
    long len = file.length();
    if (progMonitor != null) {
      progMonitor.setTransferSize(len);
      String passS = " " + java.text.MessageFormat.format(LangCo.rb.getString("[Pass_{0}/{1}]"), new Object[] {new Integer(pass), new Integer(maxPasses)});
      String task = java.text.MessageFormat.format(LangCo.rb.getString("Wiping_{0}_{1}"), new Object[] {name, passS});
      progMonitor.setCurrentStatus(task);
      progMonitor.nextTask(task);
      progMonitor.appendLine(" " + java.text.MessageFormat.format(LangCo.rb.getString("Wiping_{0}_{1}"), new Object[] {path, passS}));
      progMonitor.setFileNameSource(LangCo.rb.getString("label_Secure_Random_Stream"));
      progMonitor.setFileNameDestination(name);
      progMonitor.setFilePathDestination(path);
    }
    try {
      RandomAccessFile raf = new RandomAccessFile(file, "rw");
      FileUtils.moveData(new DataInputStream(randomIn), raf, len, progMonitor);
      raf.close();
    } catch (Throwable t) {
      OutputStream fos = new BufferedOutputStream(new FileOutputStream(file), 32*1024);
      FileUtils.moveData(new DataInputStream(randomIn), fos, len, progMonitor);
      fos.flush();
      fos.close();
    }
  }

}