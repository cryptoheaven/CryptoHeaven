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

package com.CH_co.queue;

import java.util.Iterator;

import com.CH_co.trace.Trace;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class PriorityJobFifo extends PriorityFifo {

  public static final int MAIN_WORKER_HIGH_PRIORITY = 0;
  public static final int MAIN_WORKER_LOW_PRIORITY = 100;

  public static final int JOB_TYPE_HEAVY = 1;
  public static final int JOB_TYPE_LIGHT = 2;
  public static final int JOB_TYPE_SEAMLESS = 3;
  public static final int JOB_TYPE_ALL = 4;

  /** Creates new PriorityJobFifo */
  public PriorityJobFifo() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "PriorityJobFifo()");
    if (trace != null) trace.exit(PriorityJobFifo.class);
  }

  public static boolean isJobComputationallyIntensive(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "isJobComputationallyIntensive(int code)");
    if (trace != null) trace.args(code);
    boolean rc = code == CommandCodes.FILE_Q_REMOVE_FILES ||
                 code == CommandCodes.FLD_Q_REMOVE_FOLDER ||
                 code == CommandCodes.FLD_Q_REMOVE_FOLDER_SHARES ||
                 code == CommandCodes.MSG_Q_REMOVE ||
                 code == CommandCodes.MSG_Q_REMOVE_OLD;
    if (trace != null) trace.exit(PriorityJobFifo.class, rc);
    return rc;
  }

  public static boolean isJobLogin(int code) {
    return code == CommandCodes.USR_Q_LOGIN_SECURE_SESSION || 
           code == CommandCodes.SYSENG_Q_LOGIN;
  }
  public static boolean isJobForRetry(MessageAction msgAction) {
    return msgAction != null && !msgAction.areRetriesExceeded() && 
           !PriorityJobFifo.isJobLogin(msgAction.getActionCode()) &&
           msgAction.getActionCode() != CommandCodes.SYS_Q_PING &&
           getJobType(msgAction) != JOB_TYPE_HEAVY;
  }

  public long getJobPriority(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "getJobPriority(MessageAction msgAction)");
    if (trace != null) trace.args(msgAction);

    int code = msgAction.getActionCode();
    long priority = 0;
    switch (code) {
      case CommandCodes.SYS_Q_VERSION :
      case CommandCodes.USR_Q_LOGIN_SECURE_SESSION :
        priority = MAIN_WORKER_HIGH_PRIORITY + 10;
        break;
      case CommandCodes.SYS_Q_NOTIFY :
        priority = MAIN_WORKER_HIGH_PRIORITY + 20;
        break;
      case CommandCodes.FILE_Q_GET_FILES_DATA :
      case CommandCodes.FILE_Q_NEW_FILES :
        priority = PRIORITY_LOWEST;
        // try to establish if file is small to make the priority NORMAL
        try {
          if (code == CommandCodes.FILE_Q_GET_FILES_DATA) {
            Obj_IDs_Co request = (Obj_IDs_Co) msgAction.getMsgDataSet();
            Long[] fileLinkIDs = request.IDs[0];
            long sizeSum = getFileOrigSizeSum(fileLinkIDs);
            if (sizeSum < getMaxFileSizeForMainConnection()) {
              priority = MAIN_WORKER_LOW_PRIORITY;
            }
          } else if (code == CommandCodes.FILE_Q_NEW_FILES) {
            File_NewFiles_Rq request = (File_NewFiles_Rq) msgAction.getMsgDataSet();
            long sizeSum = FileDataRecord.getFileEncSizeSum(request.fileDataRecords);
            if (sizeSum < getMaxFileSizeForMainConnection()) {
              priority = MAIN_WORKER_LOW_PRIORITY;
            }
          }
        } catch (Throwable t) {
        }
        break;
      case CommandCodes.MSG_Q_NEW :
        int type = getJobType(msgAction);
        if (type == JOB_TYPE_HEAVY) {
          priority = PRIORITY_LOWEST;
          // try to establish if file is small to make the priority NORMAL
          try {
            Msg_New_Rq request = (Msg_New_Rq) msgAction.getMsgDataSet();
            long sizeSum = FileDataRecord.getFileEncSizeSum(request.localFiles.fileDataRecords);
            if (sizeSum < getMaxFileSizeForMainConnection()) {
              priority = MAIN_WORKER_LOW_PRIORITY;
            }
          } catch (Throwable t) {
          }
        } else {
          priority = MAIN_WORKER_LOW_PRIORITY;
        }
        break;
      case CommandCodes.FLD_A_GET_FOLDERS :
        priority = MAIN_WORKER_LOW_PRIORITY + 20;
        break;

      default :
        priority = MAIN_WORKER_LOW_PRIORITY;
    }

    if (trace != null) trace.exit(PriorityJobFifo.class, priority);
    return priority;
  }

  public static int getJobType(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "getJobType(MessageAction msgAction)");
    if (trace != null) trace.args(msgAction);

    int code = msgAction.getActionCode();
    int jobType = -1;
    if (code != CommandCodes.MSG_Q_NEW) {
      jobType = getJobType(msgAction.getActionCode());
    }
    // else check if a message has attachments..., if so, threat it as JOB_TYPE_HEAVY;
    else {
      Msg_New_Rq request = (Msg_New_Rq) msgAction.getMsgDataSet();
      if (request.localFiles == null)
        jobType = getJobType(code);
      else
        jobType = JOB_TYPE_HEAVY;
    }

    if (trace != null) trace.exit(PriorityJobFifo.class, jobType);
    return jobType;
  }

  /**
   * @return job type based on the code, new message request is treated as LIGHT (even with attachments)
   * For HEAVY treatment of new message request with uploadable file attachments, use getJobType(MessageAction) call.
   */
  public static int getJobType(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "getJobType(int code)");
    if (trace != null) trace.args(code);

    int jobType = 0;

    switch (code) {
      case CommandCodes.USR_Q_LOGIN_SECURE_SESSION :
      case CommandCodes.SYS_Q_NOTIFY :
      case CommandCodes.USR_Q_LOGOUT :
      case CommandCodes.SYS_Q_VERSION :
        jobType = JOB_TYPE_SEAMLESS; // seamless jobs don't spin the globe
        break;
      default:
        if (CommandCodes.isFileTransferCode(code))
          jobType = JOB_TYPE_HEAVY;
        else
          jobType = JOB_TYPE_LIGHT;
    }

    if (trace != null) trace.exit(PriorityJobFifo.class, jobType);
    return jobType;
  }

  /**
   * @return a count of jobs matching specified TYPE.
   */
  private synchronized int countJobs(int jobType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "countJobs(int jobType)");
    if (trace != null) trace.args(jobType);

    int count = 0;

    Iterator iter = iterator();
    while (iter.hasNext()) {
      Object obj = iter.next();

      if (obj instanceof MessageAction) {
        MessageAction msgAction = (MessageAction) obj;
        int code = msgAction.getActionCode();

        if (jobType == JOB_TYPE_ALL) {
          count ++;
        } else if (jobType == getJobType(msgAction)) {
          count ++;
        } else {
          if (trace != null) trace.data(10, "skipping code", code);
        }
      }

    }
    if (trace != null) trace.exit(PriorityJobFifo.class, count);
    return count;
  } // end countWorkerJobs();

  /**
   * @return a count of jobs matching specified PRIORITY.
   */
  public synchronized int countJobs(boolean priorityAll, boolean priorityLowest) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "countJobs(boolean priorityAll, boolean priorityLowest)");
    if (trace != null) trace.args(priorityAll);
    if (trace != null) trace.args(priorityLowest);

    int count = 0;

    PriorityFifoIterator iter = (PriorityFifoIterator) iterator();
    while (iter.hasNext()) {
      Object obj = iter.next();
      if (obj != null) {
        if (priorityAll)
          count ++;
        else if (priorityLowest)
          count += iter.priority() == PRIORITY_LOWEST ? 1 : 0;
      }
    }

    if (trace != null) trace.exit(PriorityJobFifo.class, count);
    return count;
  } // end countWorkerJobs();

  /**
   * Prioritazes and adds a jobs.
   */
  public synchronized void addJob(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityJobFifo.class, "addJob(MessageAction msgAction)");
    if (trace != null) trace.args(msgAction);

    add(msgAction, getJobPriority(msgAction));

    if (trace != null) trace.exit(PriorityJobFifo.class);
  }

  public long getFileOrigSizeSum(Long[] fileLinkIDs) {
    return -1;
  }

  public long getMaxFileSizeForMainConnection() {
    return Long.MAX_VALUE;
  }

}