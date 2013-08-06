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

package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.util.ThreadCheck;
import com.CH_co.monitor.DefaultProgMonitor;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.monitor.ProgMonitorPool;
import com.CH_co.monitor.Stats;
import com.CH_co.queue.Fifo;
import com.CH_co.queue.FifoWriterI;
import com.CH_co.queue.PriorityFifoReaderI;
import com.CH_co.queue.ProcessingFunctionI;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.Obj_EncSet_Co;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import comx.HTTP_Socket.HTTP_Socket;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
* <pre>
*                                                                          __ Independent Running Queue
*                                                                    ===|/   \
*                                                                +--->  |  X  |
*                                        _________________       |   ===|\___/
*  ||    ____________         ========|/                  \      |
*  ||-->|            |-------->   | | |   Execution Queue  |-->--+-----+
*  ||   |   Workers  |        ========|\__________________/            |
*  ||<--|____________|-<>-+                                            |
*  ||                     |                                            |
*  ||                     |                                            |
*  ||                     |                                            |
*  ||                     |   Job Queue  |------------------------|    |
*  ||                     |     |=====   | submitAndReturn        |    |
*  Wire                   +-<>--|||| <---| submitAndWait          |<---+
*                               |=====   | submitAndFetchReply    |
*               WaitingJobScanner        |                        |       Requests
*                                        | getFetchedDataCache    |<----------------
*                                        |------------------------|
*
*
* </pre>
* <b>$Revision: 1.49 $</b>
* @author  Marcin Kurzawa
* @version
*/
public final class ServerInterfaceLayer extends Object implements WorkerManagerI, RequestSubmitterI {

  private static final boolean DEBUG_ON__SUPPRESS_RETRIES = false;

  private static final String PROPERTY_LAST_ENGINE_HOST = "lastEngineHost";
  private static final String PROPERTY_LAST_ENGINE_PORT = "lastEnginePort";

  // Global flag to tweak connectivity and use staged fetching for mobile devices.
  public static boolean IS_MOBILE_MODE = false;

  // For every 3 additional non-heavy jobs waiting, create additional connection.
  private static final int FOR_EVERY_N_NON_HEAVY_JOBS_CREATE_CONNECTION = 2; // used to have 5 here

  // Delay between retrying establishing a new connection after connectivity broke.
  private static final int DELAY_NEW_CONNECTION_AFTER_NET_ERROR = 15 * 1000; // 15 sec

  // By default lets retry connections periodically indefinitly;
  private int MAX_CONNECTION_RETRY_COUNT = -1; // -1 for unlimited
  private int connectionRetryCount = 0;

  // main connection never transfer heavy jobs (files) unless they are not so big
  public static final long DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION = 10 * 1024;

  // The maximum number of connections to the server that we may establish.
  public static final int DEFAULT_MAX_CONNECTION_COUNT = 4;
  public static final int DEFAULT_MIN_CONNECTION_COUNT = 1;

  private int maxConnectionCount = DEFAULT_MAX_CONNECTION_COUNT;
  private static final int MAX_CONNECTION_COUNT_MOBILE = 3;

  public static final String PROPERTY_NAME_MAX_CONNECTION_COUNT = "ServerInterfaceLayer" + "_maxConnCount";

  /** ArrayList of connection workers. */
  private final ArrayList workers = new ArrayList();
  /** All the ready messages go through this queue.
      No jobs are being run by that queue, they are handled to the submitting threads to be run. */
  private Fifo executionQueue;
  /** Job Queue */
  private JobFifo jobFifo;
  /** Waiting Jobs Scanner to relieve the waiting jobs for extensive periods of time,
      and to pickup brand new jobs that came to the queue. */
  private WaitingJobsScanner jobScanner;
  /** The jobs that no one is waiting for are executed by the Independent queue.
      This queue ensures that jobs are executed in order that they arrive, and frees up the
      ExecutionQueue so that its not blocked while independent jobs are being run. */
  private Fifo independentExecutionQueue;
  private ProcessingFunctionI independentExecutor;

  /** List to put the waiting stamps. */
  private final ArrayList stampList = new ArrayList();
  /** Secondary list to put the waiting stamps as a signal for execution thread to start its work. */
  private final ArrayList stampList2 = new ArrayList();
  /** List to put the fetched Client Message Actions for the waiting stamps. */
  private final ArrayList doneList = new ArrayList();

  /** Last successful login message. */
  private MessageAction lastLoginMessageAction;
  private Long remoteSessionID;
  private boolean hasEverLoggedInSuccessfully;

  /** Server host address and port number */
  private Object[][] hostsAndPorts;
  private int currentHostIndex;
  private boolean currentHostShouldIncrement = false;

  /** When a worker fails, remember its type to try to delay next one of the same type being created.
  * Re-connection mechanism spawns multiple threads trying to connect to different hosts/ports with
  * possibly different connection protocols. We will delay the type that failed last to give better chance
  * for other to succeed first before this one connects again.  This should help to fight providers
  * deteriorating and breaking certain connection types.
  * Static variable because it is a property of our Internet provider connectivity, so must be global for all SILs.
  */
  private static Class penalizedSocketType;
  private static final int DELAY_PENALIZED_CONNECTION_TYPE = 2000;
  // always delay protocoled sockets just a tiny bit to allow plain Socket some advantage of being first to connect...
  private static final int DELAY_PROTOCOLED_CONNECTION  = 1500;
  private static final int CONNECTION_TIMEOUT = 7000;
  private static final int MAX_CONNECTION_DELAY = Math.max(DELAY_PENALIZED_CONNECTION_TYPE, DELAY_PROTOCOLED_CONNECTION);

  /**
  * Main Worker should send Ping-Pong to retain a persistent connection.
  * Also, this worker will handle the small item's queue.
  */
  private ServerInterfaceWorker mainWorker;
  private boolean mainWorkerSubmition;
  private final Object mainWorkerMonitor = new Object();
  private LoginCompletionNotifierI loginCompletionNotifier;

  private boolean isClient;
  public Date lastForcedWorkerStamp;
  public Date lastWorkerActivityStamp;
  public boolean lastWorkerActivityResyncPending;

  private BurstableBucket burstableMonitorWorkerCreatings;
  private BurstableBucket burstableMonitorWorkerCreationTrials;
  private BurstableBucket burstableMonitorWorkersExceptions;

  /** Last created SIL, maybe invalid */
  public static ServerInterfaceLayer lastSIL;

  private HashSet statusListenersL;

  /**
  * Creates new ServerInterfaceLayer
  * @param connectedSocket through which communication will take place
  */
  public ServerInterfaceLayer(Object[][] hostsAndPorts, boolean isClient) {
    this(hostsAndPorts, null, null, isClient);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ServerInterfaceLayer(Object[][] hostsAndPorts, boolean isClient)");
    if (trace != null) trace.args(hostsAndPorts);
    if (trace != null) trace.args(isClient);
    this.lastSIL = this;
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  /**
  * Creates new ServerInterfaceLayer
  * @param independentExecutor if null, IndependentClientQueueExecutionFunction will be used.
  * @param connectedSocket through which communication will take place
  */
  public ServerInterfaceLayer(Object[][] hostsAndPorts, ProcessingFunctionI independentExecutor, Integer fixedMaxConnectionCount, boolean isClient) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ServerInterfaceLayer(Object[][] hostsAndPorts, ProcessingFunctionI independentExecutor, Integer fixedMaxConnectionCount, boolean isClient)");
    if (trace != null) trace.args(hostsAndPorts, independentExecutor, fixedMaxConnectionCount);
    if (trace != null) trace.args(isClient);

    this.isClient = isClient;

    this.hostsAndPorts = hostsAndPorts;
    this.currentHostIndex = 0;
    {
      // Put the last successfully connected to engine as the first choice, a reserved spots at beginning of the list.
      // Shaffle the order of the other hostsAndPorts so that when a particular host goes down,
      // all of its connected clients spread on the other engines, and not all of them jump on
      // the next one in sequence.
      String lastEngineHost = GlobalProperties.getProperty(PROPERTY_LAST_ENGINE_HOST, "");
      String lastEnginePort = GlobalProperties.getProperty(PROPERTY_LAST_ENGINE_PORT, "");

      // Divide list into 3 parts, last-host-port, socket-hosts, protocoled-hosts
      Object[] lastHostAndPort = null;
      ArrayList hostsAndPortsSocketL = new ArrayList();
      ArrayList hostsAndPortsProtocolL = new ArrayList();
      for (int i=0; i<hostsAndPorts.length; i++) {
        Object[] hostAndPort = (Object[]) hostsAndPorts[i];
        if (hostAndPort[0].toString().equalsIgnoreCase(lastEngineHost) && hostAndPort[1].toString().equalsIgnoreCase(lastEnginePort))
          lastHostAndPort = hostAndPort;
        else if (hostAndPort[0].toString().indexOf("://") >= 0)
          hostsAndPortsProtocolL.add(hostAndPort);
        else
          hostsAndPortsSocketL.add(hostAndPort);
      }

      // randomize lists

      Random rnd = new Random();
      randomizeList(hostsAndPortsSocketL, rnd);
      randomizeList(hostsAndPortsProtocolL, rnd);

      // put lists back together with leading last-host-port
      int index = 0;
      if (lastHostAndPort != null) {
        hostsAndPorts[index] = lastHostAndPort;
        index ++;
      }
      for (int i=0; i<hostsAndPortsSocketL.size(); i++) {
        hostsAndPorts[index] = (Object[]) hostsAndPortsSocketL.get(i);
        index ++;
      }
      for (int i=0; i<hostsAndPortsProtocolL.size(); i++) {
        hostsAndPorts[index] = (Object[]) hostsAndPortsProtocolL.get(i);
        index ++;
      }
    }

    // Create the execution queue
    this.executionQueue = new Fifo();
    this.executionQueue.installSink("Execution Queue", new QueueExecutionFunction());

    // Create the job queue
    this.jobFifo = new JobFifo();

    // Create the independent execution queue
    this.independentExecutor = independentExecutor != null ? independentExecutor : new IndependentClientQueueExecutionFunction(this);
    this.independentExecutionQueue = new Fifo();
    this.independentExecutionQueue.installSink("Independent Exec Queue", this.independentExecutor);

    // set MAX connection count
    if (fixedMaxConnectionCount == null) {
      String sMaxConnCount = GlobalProperties.getProperty(PROPERTY_NAME_MAX_CONNECTION_COUNT);
      if (sMaxConnCount != null) {
        try {
          int maxConnCount = Integer.parseInt(sMaxConnCount);
          // Do not allow more than 3 so not to overwhelm the servers
          if (maxConnCount > 3)
            maxConnCount = 3;
          setMaxConnectionCount(maxConnCount);
        } catch (Throwable t) {
        }
      }
    }
    else {
      setMaxConnectionCount(fixedMaxConnectionCount.intValue());
    }

    // Start Waiting Jobs Scanner to relieve the waiting jobs for extensive periods of time,
    // and to pickup brand new jobs that came to the queue.
    jobScanner = new WaitingJobsScanner();
    jobScanner.setDaemon(true);
    jobScanner.start();

    if (isClient) {
      // Start monitoring for CPU suspension so we can requests updates when we wake up.
      new SleepMonitor().start();
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  public long calculateRate() {
    long rate = 0;
    synchronized (workers) {
      for (int i=0; i<workers.size(); i++) {
        ServerInterfaceWorker worker = (ServerInterfaceWorker) workers.get(i);
        rate += worker.calculateRate();
      }
    }
    return rate;
  }

  private void randomizeList(List list, Random rnd) {
    int length = list.size();
    for (int i=0; i<length-1; i++) {
      int swap1 = i;
      int swap2 = rnd.nextInt(length-i)+i;
      if (swap1 != swap2) {
        Object o1 = list.get(swap1);
        Object o2 = list.get(swap2);
        list.set(swap2, o1);
        list.set(swap1, o2);
      }
    }
  }

  public void setMaxConnectionRetryCount(int maxRetryCount) {
    this.MAX_CONNECTION_RETRY_COUNT = maxRetryCount;
  }
  public void setLoginCompletionNotifierI(LoginCompletionNotifierI loginCompletionNotifier) {
    this.loginCompletionNotifier = loginCompletionNotifier;
  }

  public Object[][] getHostsAndPorts() {
    return hostsAndPorts;
  }
  public void setHostsAndPorts(Object[][] newHostsAndPorts) {
    hostsAndPorts = newHostsAndPorts;
  }

  /**
  * @return Socket type worker count and HTTP type worker count
  */
  private int[] getWorkerCounts() {
    int[] counts = new int[2];
    synchronized (workers) {
      for (int i=0; i<workers.size(); i++) {
        ServerInterfaceWorker worker = (ServerInterfaceWorker) workers.get(i);
        Class socketType = worker.getSocketType();
        if (Socket.class.equals(socketType))
          counts[0] ++;
        else if (HTTP_Socket.class.equals(socketType))
          counts[1] ++;
      }
    }
    return counts;
  }

  /**
  * @return the Fetched Data Cache storage where all data is to be cached.
  * There should be only one instance of this cache in the program runtime.
  */
  public static FetchedDataCache getFetchedDataCache() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "getFetchedDataCache()");
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
    return cache;
  }

  /**
  * Assigns progress monitor if not already registered.
  */
  protected ProgMonitorI assignProgMonitor(MessageAction msgAction, Boolean withProgressDialog) {
    long msgActionStamp = msgAction.getStamp();
    ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(msgActionStamp);
    if (!destroyed) {
      if (ProgMonitorPool.isDummy(progressMonitor) && !Misc.isAllGUIsuppressed()) {
        boolean withDialog = withProgressDialog != null ? withProgressDialog.booleanValue() : !DefaultProgMonitor.isSuppressProgressDialog(msgAction.getActionCode());
        progressMonitor = new DefaultProgMonitor(withDialog);
        ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionStamp);
      }
    }
    return progressMonitor;
  }

  /**
  * Assigns and enqueues progress monitor if not already registered.
  */
  private void enqueueProgMonitor(MessageAction msgAction) {
    ProgMonitorI progressMonitor = assignProgMonitor(msgAction, null);
    if (progressMonitor != null)
      progressMonitor.enqueue(msgAction.getActionCode(), msgAction.getStamp());
  }

  /**
  * Submit and do not wait for the reply.  This method returns immediately.
  * When the reply is ready, it will be run by the executionQueue serving process.
  */
  private void submitAndReturnNow(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturnNow(MessageAction)");
    if (trace != null) trace.args(msgAction);

    if (!destroyed) {
      if (msgAction == null)
        throw new IllegalArgumentException("MessageAction cannot be null.");

      if (trace != null) trace.data(10, "submitting... (1/3) : job code ", msgAction.getActionCode());

      enqueueProgMonitor(msgAction);
      jobFifo.addJob(msgAction);

      if (trace != null) trace.data(11, "submitting... (2/3) : triggering");
      jobScanner.triggerCheckToServeNow();
      if (trace != null) trace.data(12, "submitting... (3/3) : done");
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Submit and do not wait for the reply.  This method returns immediately.
  * When the reply is ready, it will be run by the executionQueue serving process.
  */
  public void submitAndReturn(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturn(MessageAction)");
    if (trace != null) trace.args(msgAction);
    if (msgAction.isInterruptible()) // interruptable actions should pass interraptable objects down the reply chain, so use a thread that will wait for reply
      submitAndReturn(msgAction, 0, null, null, null);
    else
      submitAndReturnNow(msgAction);
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  public void submitAndReturn(MessageAction msgAction, long timeout) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturn(MessageAction msgAction, long timeout)");
    submitAndReturn(msgAction, timeout, null, null, null);
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  /**
  * Similar to submitAndReturn but runs an action after the job is done.
  * @param timeout max time to wait (millis) for the job to finish so we can start executing one of the after jobs, 0 for unlimited.
  * @param replyReceivedJob a runnable element to be done when job finishes within time allowed but prior of its execution in the client
  * @param afterJob a runnable element to be done after the job is done within time allowed and already executed
  * @param timeoutJob a runnable element when timeout is reached (afterJob and timeoutJob are exclusive, either one or the other)
  */
  public void submitAndReturn(final MessageAction msgAction, final long timeout, final Runnable afterJob, final Runnable timeoutJob) {
    submitAndReturn(msgAction, timeout, null, afterJob, timeoutJob);
  }
  public void submitAndReturn(final MessageAction msgAction, final long timeout, final Runnable replyReceivedJob, final Runnable afterJob, final Runnable timeoutJob) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturn(MessageAction msgAction, long timeout, final Runnable replyReceivedJob, Runnable afterJob, Runnable timeoutJob)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);
    if (trace != null) trace.args(afterJob, timeoutJob);

    Thread th = new ThreadTraced("Job-Submitter-and-After-Job-Runner") {
      public void runTraced() {
        Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(getClass(), "submitAndReturn.runTraced()");
        try {
          boolean noTimeout = submitAndWait(msgAction, timeout, replyReceivedJob);
          if (noTimeout) {
            if (afterJob != null)
              afterJob.run();
          } else {
            if (timeoutJob != null)
              timeoutJob.run();
          }
        } catch (Throwable t) {
          if (trace != null) trace.data(100, "Exception while running action", msgAction);
          if (trace != null) trace.exception(getClass(), 101, t);
          MyUncaughtExceptionHandlerOps.unhandledException(t);
        }
        if (trace != null) trace.exit(getClass());
      } // end run()
    }; // end Thread
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  } // end submitAndReturn()


  private void ensureEnoughAllWorkersExist() {
    ensureEnoughAllWorkersExist(false);
  }
  private void ensureEnoughAllWorkersExist(boolean forceAdditionalConnection) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ensureEnoughAllWorkersExist(boolean forceAdditionalConnection)");
    if (trace != null) trace.args(forceAdditionalConnection);

    try {
      ensureEnoughFreeWorkers(forceAdditionalConnection);
      triggerCheckForMainWorker();
      connectionRetryCount = 0;
    } catch (RuntimeException e) {
      // If we don't destroy server or its not already destroyed, the exception will be re-thrown...
      if (!destroyed && !destroying) {
        connectionRetryCount ++;
        if (MAX_CONNECTION_RETRY_COUNT > -1 && connectionRetryCount > MAX_CONNECTION_RETRY_COUNT) {
          if (trace != null) trace.data(10, "SIL: DESTROY, connectionRetryCount", connectionRetryCount);
          destroyServer();
        } else { // do some stuff, then re-throw...
          if (trace != null) trace.data(20, "SIL: RETRY, connectionRetryCount", connectionRetryCount);
          // Rest after worker creating failure.
          // Stall the current thread for some 3 seconds on repetitive exception
          if (burstableMonitorWorkersExceptions == null)
            burstableMonitorWorkersExceptions = new BurstableBucket(2, 0.333, true, 0);
          burstableMonitorWorkersExceptions.passThrough();

          // Schedule a retry in 30 seconds.
          // If new requests come, they will cause an immediate retry through
          // submit job mechanism, so no worry that we are waiting too much time.
          Timer timer = new Timer(true);
          timer.schedule(new TimerTask() {
            public void run() {
              Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "ensureEnoughAllWorkersExist.TimerTask.run()");
              if (trace != null) trace.data(10, "SIL: RETRY WOKE UP");
              try {
                if (destroyed || destroying) {
                  if (trace != null) trace.data(11, "While sleeping, we got destroyed, so we will not try to create a worker!");
                }
                else {
                  if (trace != null) trace.data(12, "... we will now retry establishing workers.");
                  try {
                    if (trace != null) trace.data(13, "SIL: RETRYING...");
                    ensureEnoughAllWorkersExist();
                  } catch (Throwable t) {
                    if (trace != null) trace.exception(getClass(), 100, t);
                  }
                }
              } catch (Throwable t) {
                if (trace != null) trace.exception(getClass(), 200, t);
              }
              if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
              if (trace != null) trace.exit(getClass());
              if (trace != null) trace.clear();
            }
          }, 30*1000);

          throw e;
        } // end else
      } // if !destroyed
    } // end catch

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Submit and wait for the reply.  This method stalls the Thread until reply becomes available.
  * @param timeout in milliseconds, 0=infinite
  */
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction) { return submitAndFetchReply(msgAction, 0); }
  public ClientMessageAction submitAndFetchReply(MessageAction msgAction, long timeout) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndFetchReply(MessageAction, long timeout)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);

    ClientMessageAction replyMsg = null;

    if (!destroyed) {
      if (!IS_MOBILE_MODE) {
        ThreadCheck.warnIfOnAWTthread();
      }

      Stamp lStamp = new Stamp(msgAction.getStamp());

      synchronized (stampList) {
        // register a stamp so that executor returns the reply message to the done list
        if (stampList.contains(lStamp)) {
          if (trace != null) trace.data(5, "stampList already contains " + lStamp);
          //System.out.println("stampList already contains " + lStamp);
        }
        stampList.add(lStamp);
        stampList.notifyAll();
      }

      // once stamp is registered, submit the action
      submitAndReturnNow(msgAction);

      // We cannot synchronize on the Stamp before submitAndReturn because other independent exec threads
      // might need to submitAndFetchReply and we will have a deadlock in the Independent exec queue.

      if (trace != null) trace.data(10, "synchronizing on submited stamp", lStamp);
      synchronized (lStamp) {
        if (trace != null) trace.data(11, "synchronizing on submited stamp done", lStamp);

        // Another stamp list used to synch up the execution queue, due to recursive submitAndReturn
        // call which cannot be synchronized on anything due to a potential deadlock problem.
        // The execution may only begin when stampList has the stamp and stampList2 has it too!
        // When stamp is put on the stampList2, the submitter is ready to hear about request completion.
        synchronized (stampList) {
          synchronized (stampList2) {
            if (stampList2.contains(lStamp)) {
              if (trace != null) trace.data(15, "stampList2 already contains " + lStamp);
              //System.out.println("stampList2 already contains " + lStamp);
            }
            stampList2.add(lStamp);
          } // end synchronized (stampList2)
          // This notify can unlock the execution thread waiting for the submitter to get ready...
          stampList.notifyAll();
        } // end synchronized (stampList)
        try {
          if (trace != null) trace.data(21, "wait on lStamp .. start", lStamp);
          // wait until this specific reply actions arrives
          lStamp.wait(timeout);
        } catch (InterruptedException e) {
        }
        if (trace != null) trace.data(22, "wait on lStamp .. done", lStamp);

        if (trace != null) trace.data(40, "woken and synch on stampList");
        // if we woke up, the reply must be ready,
        // or timeout reached,
        // or SIL killed and waiting threads released
        synchronized (stampList) {
          if (trace != null) trace.data(41, "woken and synch on stampList done");
          if (trace != null) trace.data(42, "woke up and ready to check doneList for reply");
          // remove the stamp and the reply message from the lists
          stampList.remove(lStamp);
          synchronized (stampList2) {
            stampList2.remove(lStamp);
          }
          // Pickup reply from done list (synchs on doneList)
          replyMsg = findAndRemoveMsgForStamp(lStamp);

          if (replyMsg == null) {
            if (trace != null) trace.data(70, "TIMEOUT");
            if (trace != null) trace.data(71, "specified timeout was", timeout);
          }
        } // end synchronized (stampList)
      } // end synchronized

      // copy original Interrupts to the reply
      if (replyMsg != null) {
        replyMsg.setInterruptsFrom(msgAction);
      }
    }

    if (replyMsg == null) {
      if (trace != null) trace.data(72, "clear any open progress dialogs");
      ProgMonitorPool.getProgMonitor(msgAction.getStamp()).jobKilled();
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class, replyMsg);
    return replyMsg;
  } // end submitAndFetchReply()

  public ClientMessageAction submitAndFetchReply(MessageAction msgAction, long timeout, int maxRetries) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndFetchReply(MessageAction, long timeout, int maxRetries)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);
    if (trace != null) trace.args(maxRetries);
    if (DEBUG_ON__SUPPRESS_RETRIES)
      maxRetries = 0;
    if (timeout == 0 && maxRetries > 0)
      throw new IllegalArgumentException("Timeout must be > 0 if maxRetries > 0");
    ClientMessageAction replyMsg = null;
    int tryCount = 0;
    while (true) { // individual action retry in case of timeout
      if (trace != null) trace.data(10, "submitting job, tryCount=", tryCount);
      tryCount ++;
      replyMsg = submitAndFetchReply(msgAction, timeout);
      if (replyMsg != null || tryCount > maxRetries)
        break;
      if (trace != null) trace.data(30, "loop again - timeout reached");
    }
    if (replyMsg == null) {
      if (trace != null) trace.data(40, "TIMEOUT reached, replyMsg is NULL");
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class, replyMsg);
    return replyMsg;
  }

  /**
  * Removes and returns the reply message from the done list.
  */
  private ClientMessageAction findAndRemoveMsgForStamp(Stamp stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "findAndRemoveMsgForStamp(Stamp)");
    if (trace != null) trace.args(stamp);

    ClientMessageAction replyMsg = null;

    synchronized (doneList) {
      for (int i=0; i<doneList.size(); i++) {
        ClientMessageAction reply = (ClientMessageAction) doneList.get(i);
        long stampValue = stamp.longValue();
        if (stampValue == reply.getStamp()) {
          replyMsg = reply;
          if (trace != null) trace.data(10, "reply found");
          doneList.remove(reply);
          break;
        }
      } // end loop
    } // end synchronized

    if (replyMsg != null) {
      if (trace != null) trace.data(20, "reply IS found");
    } else {
      if (trace != null) trace.data(20, "reply NOT found");
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class, replyMsg);
    return replyMsg;
  } // end findAndRemoveMsgForStamp()

  /**
  * Submit and wait for the reply.  This method stalls the Thread until reply becomes available.
  * @param timeout in milliseconds for each transaction, 0=infinite
  * @param msgAction the message action to be submitted.
  */
  public void submitAndWait(MessageAction msgAction) { submitAndWait(msgAction, 0, 0, null); }
  /**
  * Submit and wait for the reply.  This method stalls the Thread until reply becomes available.
  * @param timeout in milliseconds for each transaction, 0=infinite
  * @param msgAction the message action to be submitted.
  * @return true if reply was received in time, false when timeout reached.
  */
  public boolean submitAndWait(MessageAction msgAction, long timeout) {
    return submitAndWait(msgAction, timeout, 0, null);
  }
  public boolean submitAndWait(MessageAction msgAction, long timeout, int maxRetries) {
    return submitAndWait(msgAction, timeout, maxRetries, null);
  }
  public boolean submitAndWait(MessageAction msgAction, long timeout, final Runnable replyReceivedJob) {
    return submitAndWait(msgAction, timeout, 0, replyReceivedJob);
  }
  public boolean submitAndWait(MessageAction msgAction, long timeout, int maxRetries, final Runnable replyReceivedJob) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndWait(MessageAction msgAction, long timeout, int maxRetries, final Runnable replyReceivedJob)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);
    if (trace != null) trace.args(maxRetries);
    if (trace != null) trace.args(replyReceivedJob);

    boolean noTimeout = true;
    boolean replyReceivedJobExecuted = false;

    do {
      final ClientMessageAction replyMsg = submitAndFetchReply(msgAction, timeout, maxRetries);

      if (trace != null) trace.data(20, replyMsg);
      if (replyMsg != null) {
        // If a job was assigned after a reply is received and before executed, run it now.
        // Prevent from running many times if action generates some other request.
        if (replyReceivedJob != null && !replyReceivedJobExecuted) {
          replyReceivedJob.run();
          replyReceivedJobExecuted = true;
        }
        // Don't start a new DefaultReplyRunner thread here (because it recursively used ServerInterfaceLayer)
        // Just execute the action synchronously (in a MIN PRIORIY THREAD) while the user waits.
        final MessageAction[] returnBufferMsgAction = new MessageAction[1];
        Thread th = new ThreadTraced("Reply Runner") {
          public void runTraced() {
            Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "runTraced()");
            try {
              returnBufferMsgAction[0] = (MessageAction) independentExecutor.processQueuedObject(replyMsg);
            } catch (Throwable t) {
              if (trace != null) trace.exception(getClass(), 100, t);
              MyUncaughtExceptionHandlerOps.unhandledException(t);
            }
            if (trace != null) trace.exit(getClass());
          }
        };
        th.setDaemon(true);
        th.start();
        try {
          th.join();
          break;
        } catch (InterruptedException e) {
        }
        msgAction = returnBufferMsgAction[0];

        if (trace != null) trace.data(30, "Request for the reply is=", msgAction);
      } else {
        msgAction = null;
        noTimeout = false;
      }

      // if msgAction != null then we will submit the new request and wait till its done.
    } while (msgAction != null);

    if (trace != null) trace.exit(ServerInterfaceLayer.class, noTimeout);
    return noTimeout;
  }

  public int getMaxHeavyWorkerCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "getMaxHeavyWorkerCount()");
    int maxCount = getMaxAdjustedConnectionCount() - 1;
    if (maxCount < 1 && maxConnectionCount > 0)
      maxCount = 1;
    if (trace != null) trace.exit(ServerInterfaceLayer.class, maxCount);
    return maxCount;
  }
  private int getMaxAdjustedConnectionCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "getMaxAdjustedConnectionCount()");
    int maxCount = maxConnectionCount;
    if (IS_MOBILE_MODE && maxCount > MAX_CONNECTION_COUNT_MOBILE)
      maxCount = MAX_CONNECTION_COUNT_MOBILE;
    if (!hasEverLoggedInSuccessfully || !hasMainWorker()) {
      if (trace != null) trace.data(10, "temporary ceiling of 1 until logged in with main worker");
      maxCount = Math.min(1, maxConnectionCount);
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class, maxCount);
    return maxCount;
  }
  public void setMaxConnectionCount(int maxConnections) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "setMaxConnectionCount(int maxConnections)");
    if (trace != null) trace.args(maxConnections);
    maxConnectionCount = maxConnections;
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  private void ensureEnoughFreeWorkers(boolean forceAdditionalConnection) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ensureEnoughFreeWorkers(boolean forceAdditionalConnection)");
    if (trace != null) trace.args(forceAdditionalConnection);

    if (workers.size() < getMaxAdjustedConnectionCount() && !destroyed && !destroying) {
      // count free workers
      int countFreeAllWorkers = 0;
      int countFreeHeavyWorkers = 0;

      int countAllWorkers;

      synchronized (workers) {
        countAllWorkers = workers.size();
        if (trace != null) trace.data(10, "for all workers, size=", countAllWorkers);
        for (int i=0; i<countAllWorkers; i++) {
          ServerInterfaceWorker worker = (ServerInterfaceWorker) workers.get(i);
          if (!worker.isBusy())
            countFreeAllWorkers ++;
          if (!isMainWorker(worker)) {
            countFreeHeavyWorkers += worker.isBusy() ? 0 : 1;
          }
        }
      }

      if (trace != null) trace.data(11, "countFreeAllWorkers", countFreeAllWorkers);
      if (trace != null) trace.data(12, "countFreeHeavyWorkers", countFreeHeavyWorkers);

      int countLargeFileJobs = jobFifo.countJobs(false, true);
      if (trace != null) trace.data(13, "countLargeFileJobs", countLargeFileJobs);

      int countAllJobs = jobFifo.countJobs(true, true);
      if (trace != null) trace.data(14, "countAllJobs", countAllJobs);

      int countWorkersToCreate = 0;

      // if total number of workers is ZERO than create at least 1 worker to start with
      if (countAllWorkers == 0 && countAllJobs > 0) {
        if (trace != null) trace.data(20, "at least one worker would be nice");
        countWorkersToCreate ++;
      }

      // create one additional worker if there are additional heavy jobs waiting
      if (countLargeFileJobs - countFreeHeavyWorkers > 0) {
        if (trace != null) trace.data(21, "additional worker due to heavy job(s) waiting");
        countWorkersToCreate ++;
      }

      // when main worker exists (the login message is available for new connections)
      if (hasMainWorker()) {
        // for every some non-heavy jobs create additional worker
        if (countAllWorkers > 0) { // Avoid divizion-by-zero.
          // For non-mobile: Jobs waiting | Workers
          // 0|1, 1-2|2, 3-4|3, 5-10|4
          // For mobileFactor = 1 : 0-1|1, 2-4|2, 5-7|3, 8|4
          // For mobileFactor = 2 : 0-2|1, 3-6|2, 7-10|3
          // For mobileFactor = 3 : 0-3|1, 4-8|2, 9-13|3
          // For mobileFactor = 4 : 0-4|1, 5-10|2, 11-16|3
          // For mobileFactor = 5 : 0-5|1, 6-12|2, 13-19|3
          int adjustmentFactor = 0;
          if (IS_MOBILE_MODE) {
            // Used to have adjustmentFactor 5 for mobile, but it creates some connectivity problems
            // as bad connections take a LONG time to recycle... so do not adjust mobiles!
            //adjustmentFactor = 5;
          }
          countWorkersToCreate += (1 + countAllJobs - countLargeFileJobs) / ((FOR_EVERY_N_NON_HEAVY_JOBS_CREATE_CONNECTION+adjustmentFactor) * countAllWorkers);
        }
        // If we should create at least 1 connection because there are jobs long awaiting to be sent...
        if (forceAdditionalConnection && countWorkersToCreate < 1) {
          countWorkersToCreate = 1;
          if (trace != null) trace.data(24, "1 additional worker, total of", countWorkersToCreate);
        }
        // Never create more than 1 (used to have '2' here) additional workers at a time when main worker is already present
        if (countWorkersToCreate > 1) {
          if (trace != null) trace.data(25, "don't create more than 1 additional workers at a time when main worker is already present");
          countWorkersToCreate = 1;
        }
        // Maintain at least the minimum number of workers
        if (countWorkersToCreate == 0 && countAllWorkers < DEFAULT_MIN_CONNECTION_COUNT) {
          countWorkersToCreate = 1;
        }
      }

      // limit number of all workers to the preset maximum
      if (countWorkersToCreate + countAllWorkers > getMaxAdjustedConnectionCount()) {
        countWorkersToCreate = getMaxAdjustedConnectionCount() - countAllWorkers;
        if (trace != null) trace.data(28, "upper limit of workers reached, set back workers to create to", countWorkersToCreate);
      }

      if (trace != null) trace.data(30, countFreeAllWorkers);
      if (trace != null) trace.data(32, countFreeHeavyWorkers);
      if (trace != null) trace.data(34, countAllWorkers);
      if (trace != null) trace.data(36, countLargeFileJobs);
      if (trace != null) trace.data(38, countAllJobs);
      if (trace != null) trace.data(40, countWorkersToCreate);

      // If this is the first login connection, just 1 is enough
      if (lastLoginMessageAction == null) {
        if (trace != null) trace.data(50, "limitting countWorkersToCreate to 1 since no previous login was done");
        countWorkersToCreate = Math.min(1, countWorkersToCreate);
        if (trace != null) trace.data(51, countWorkersToCreate);
      }

      // If previously logged in, but no workers exist and no workers to create (due to no new requests),
      // just create the main one so he can register for notify again.
      if (countWorkersToCreate == 0 && countAllWorkers == 0 && lastLoginMessageAction != null) {
        if (trace != null) trace.data(60, "increasing countWorkersToCreate from 0 to 1 so that persistent connection can be established");
        countWorkersToCreate = 1;
      }

      createWorkers(countWorkersToCreate);
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  } // end ensureEnoughFreeWorkers


  /**
  * Make sure that at least one additional worker aside from the persistent connection
  * worker exists.  Useful as a preparation for file upload to establish a connection
  * prior to having the transaction ready to minimize the delay.
  */
  public void ensureAtLeastOneAdditionalWorker_SpawnThread() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ensureAtLeastOneAdditionalWorker()");
    Thread th = new ThreadTraced("Additional Worker Ensurer") {
      public void runTraced() {
        Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(getClass(), "ServerInterfaceLayer.ensureAtLeastOneAdditionalWorker_SpawnThread.runTraced()");

        int countAllWorkers = workers.size();
        if (trace != null) trace.data(10, "countAllWorkers", countAllWorkers);
        if (countAllWorkers < 2 && countAllWorkers < getMaxAdjustedConnectionCount())
          createWorkers(1);

        if (trace != null) trace.exit(getClass());
      }
    };
    th.setDaemon(true);
    th.start();
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }


  /**
  * Create additional workers.
  */
  private void createWorkers(int numberOfWorkersToCreate) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "createWorkers(int numberOfWorkersToCreate)");
    if (!destroyed && !destroying) {
      if (trace != null) trace.data(10, "numberOfWorkersToCreate", numberOfWorkersToCreate);
      // Create the calculated number of additional/new workers to
      // ensure that enough workers are free to handle the job queue.
      for (int i=0; i<numberOfWorkersToCreate; i++) {
        // Control throughput of worker creation as a safety mechanism for server connectivity
        if (burstableMonitorWorkerCreatings == null)
          burstableMonitorWorkerCreatings = new BurstableBucket(2, 1.0/5.0, true, 100); // max 2 workers in 5 seconds each at least 100ms apart
        burstableMonitorWorkerCreatings.passThrough();

        ServerInterfaceWorker worker = null;
        // roll through the valid hosts to attempt to create a connection
        // each unique server will get 2 connection trials before we bail out
        int maxWorkerTrials = hostsAndPorts.length*2;
        for (int workerTrial=0; workerTrial<maxWorkerTrials; workerTrial++) {
          // Control throughput of trials as a safety mechanism for server connectivity
          if (burstableMonitorWorkerCreationTrials == null)
            burstableMonitorWorkerCreationTrials = new BurstableBucket(10, 1.0/5.0, true, 100); // max 10 trials in 5 seconds each at least 100ms apart
          burstableMonitorWorkerCreationTrials.passThrough();

          if (trace != null) trace.data(11, "createWorker workerTrial="+workerTrial);

          int[] hostIndexesToTry;
          int hostIndexCompletedFirst = -1;
          try {
            // First trial gives perference to previously working host,
            // subsequest trials we'll look for a better and different working host.
            // Incase that last tried host was destroyed due to communication error,
            // then increment to next regardless if it is the first trial.
            if (workerTrial > 0 || currentHostShouldIncrement) {
              currentHostIndex = (currentHostIndex + 1) % hostsAndPorts.length;
              currentHostShouldIncrement = false;
            }
            String hostToTry = (String) hostsAndPorts[currentHostIndex][0];
            if (trace != null) trace.data(15, "hostToTry", hostToTry);

            // If encountered 'protocoled' host, simultaneously try other non-protocoled host too.
            int alternateHostIndex = -1;
            if (hostToTry.indexOf("://") >= 0) {
              // find next non-protocoled host/port
              if (trace != null) trace.data(20, "find next non-protocoled host/port");
              for (int k=0; k<hostsAndPorts.length; k++) {
                int indexCandidate = (currentHostIndex+1+k) % hostsAndPorts.length;
                String hostCandidate = (String) hostsAndPorts[indexCandidate][0];
                if (hostCandidate.indexOf("://") < 0) {
                  alternateHostIndex = indexCandidate;
                  break;
                }
              }
              if (trace != null) trace.data(21, "alternateHostIndex", alternateHostIndex);
            }

            // If encountered Socket error before, try other non-socket host too.
            if (Socket.class.equals(penalizedSocketType) && hostToTry.indexOf("://") < 0) {
              // find next protocoled host/port
              if (trace != null) trace.data(30, "find next protocoled host/port");
              for (int k=0; k<hostsAndPorts.length; k++) {
                int indexCandidate = (currentHostIndex+1+k) % hostsAndPorts.length;
                String hostCandidate = (String) hostsAndPorts[indexCandidate][0];
                if (hostCandidate.indexOf("://") >= 0) {
                  alternateHostIndex = indexCandidate;
                  break;
                }
              }
              if (trace != null) trace.data(31, "alternateHostIndex", alternateHostIndex);
            }

            if (alternateHostIndex >= 0) {
              hostIndexesToTry = new int[] { currentHostIndex, alternateHostIndex };
            } else {
              hostIndexesToTry = new int[] { currentHostIndex };
            }

            if (trace != null) trace.data(40, "hostIndexesToTry", hostIndexesToTry);
            if (trace != null) trace.data(41, "hostsAndPorts", hostsAndPorts);

            final Thread[] ths = new Thread[hostIndexesToTry.length];
            final Socket[][] socketBuffers = new Socket[hostIndexesToTry.length][1];
            Throwable[][] errBuffers = new Throwable[hostIndexesToTry.length][1];

            Stats.setStatusAll("Attempting connection.");

            StringBuffer sbSocketInfo = new StringBuffer();
            for (int k=0; k<hostIndexesToTry.length; k++) {
              sbSocketInfo.append("Socket host ").append(hostsAndPorts[hostIndexesToTry[k]][0]);
              sbSocketInfo.append(" port ").append(hostsAndPorts[hostIndexesToTry[k]][1]);
              if (k<hostIndexesToTry.length-1)
                sbSocketInfo.append(", ");
              ths[k] = createSocket_Threaded((String) hostsAndPorts[hostIndexesToTry[k]][0],
                                              ((Integer) hostsAndPorts[hostIndexesToTry[k]][1]).intValue(),
                                              socketBuffers[k], errBuffers[k]);
            }
            // Find the first one joined
            // We are interested in first successful or first failure iff all failed.
            int joinedIndexFirst = -1;
            while (isAnyNonNULL(ths)) {
              int joinedIndex = joinAny(ths); // join the first available thread that has completed
              if (joinedIndexFirst == -1)
                joinedIndexFirst = joinedIndex;
              if (errBuffers[joinedIndex][0] != null) {
                ths[joinedIndex] = null;
              } else {
                joinedIndexFirst = joinedIndex;
                break;
              }
            }

            hostIndexCompletedFirst = hostIndexesToTry[joinedIndexFirst];
            if (trace != null) trace.data(50, "hostIndexCompletedFirst", hostIndexCompletedFirst);

            if (errBuffers[joinedIndexFirst][0] != null)
              throw errBuffers[joinedIndexFirst][0];

            Socket socket = socketBuffers[joinedIndexFirst][0];
            if (trace != null) trace.data(60, "createWorker() attempted hosts and ports are", sbSocketInfo.toString());
            Stats.setStatusAll("Attempted "+sbSocketInfo.toString());
            if (trace != null) trace.data(61, "createWorker() created socket is", socket);
            String type = socket != null ? socket.getClass().toString() : null;
            type = type != null ? type.substring(type.lastIndexOf(".")+1) : null;
            if (trace != null) trace.data(62, "socketType", type);
            Stats.setStatusAll("Using "+socket+" type "+type);

            // Clear other sockets that might have been created too
            // remove socket that we are using so it doesn't get cleaned up here
            socketBuffers[joinedIndexFirst][0] = null;
            Thread cleanupOtherSockets = new ThreadTraced("Cleanup Other Sockets") {
              public void runTraced() {
                // join with all socket creating threads...
                for (int i=0; i<ths.length; i++) {
                  if (ths[i] != null) {
                    try { ths[i].join(60000); } catch (Throwable t) { }
                    try { socketBuffers[i][0].close(); } catch (Throwable t) { }
                  }
                }
              }
            };
            cleanupOtherSockets.setDaemon(true);
            cleanupOtherSockets.start();

            if (socket != null) {
              MessageAction loginAction = lastLoginMessageAction;
              if (loginAction != null) {
                if (trace != null) trace.data(70, "restamp the auto-login message");
                loginAction = new MessageAction(loginAction.getActionCode(), loginAction.getMsgDataSet());
                setLoginMsgAction(loginAction);
              } else {
                if (trace != null) trace.data(71, "no auto-login message to restamp");
              }
              worker = new ServerInterfaceWorker(socket, this, this,
                                                getReplyFifoWriterI(),
                                                getRequestPriorityFifoReaderI(),
                                                loginAction);
              if (trace != null) trace.data(80, "ServerInterfaceWorker instantiated");
            }

            if (worker != null) {
              // Remember this host and port for next session as the first to try.
              // If it is protocoled connection then only remember it if 24h passed from last one.
              String host = ""+hostsAndPorts[hostIndexCompletedFirst][0];
              String port = ""+hostsAndPorts[hostIndexCompletedFirst][1];
              GlobalProperties.setProperty(PROPERTY_LAST_ENGINE_HOST, host);
              GlobalProperties.setProperty(PROPERTY_LAST_ENGINE_PORT, port);
              break;
            }
          } catch (Throwable t) {
            if (trace != null) trace.exception(ServerInterfaceLayer.class, 70, t);
            String errMsg = "<html>Network error. "
                    + " Could not connect to the "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" Data Server at " + hostsAndPorts[hostIndexCompletedFirst][0] + " on port " + hostsAndPorts[hostIndexCompletedFirst][1] + ".  "
                    + "Please verify your internet connectivity.  "
                    + "If the problem persists please visit <a href=\""+URLs.get(URLs.CONNECTIVITY_PAGE)+"\">"+URLs.get(URLs.CONNECTIVITY_PAGE)+"</a> for help. <p>";
            // Only at the last round process the exception, else ignore and try another host.
            if (workerTrial+1 == maxWorkerTrials) {

              if (t instanceof UnknownHostException) {
                throw new SILConnectionException(errMsg + "Specified host is unknown or cannot be resolved.  The error message is: <br>" + t.getMessage());
              }
              else if (t instanceof IOException) {
                if (workers.size() == 0) {
                  if (trace != null) trace.data(90, "Not enough workers to ignore connection problem!");
                  throw new SILConnectionException(errMsg + "Input/Output error occurred.  The error message is: <br>" + t.getMessage());
                }
              }
              else {
                String msg = errMsg + "The error message is: \n\n" + t.getMessage();
                if (trace != null) trace.data(90, "Unsupported exception", t.getMessage());
                throw new SILConnectionException(msg);
              }

            } // end process last round exception
          } // end catch Throwable t
        } // end for workerTrial

        // store the worker in a collection
        if (worker != null) {// null when createWorker() failed!
          synchronized (workers) {
            workers.add(worker);
            // destroyed worker could be replaced by new one which should now be updating connection counts
            if (!destroyed && !destroying) Stats.setConnections(workers.size(), getWorkerCounts());
          }
        }
      } // end for
    } // end if !destroyed
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Joins first thread that dies.
  * @returns index of thread that it joined, (first dead/completed thread)
  */
  private int joinAny(Thread[] threads) {
    int rc = -1;
    while (true) {
      for (int i=0; i<threads.length; i++) {
        if (threads[i] != null && !threads[i].isAlive()) {
          rc = i;
          break;
        }
      }
      if (rc >= 0) {
        break;
      } else {
        try {
          Thread.sleep(10);
        } catch (Throwable t) {
        }
      }
    }
    return rc;
  }

  /**
  * @return true if there still are any non-null elements
  */
  private boolean isAnyNonNULL(Object[] objs) {
    boolean rc = false;
    for (int i=0; i<objs.length; i++) {
      if (objs[i] != null) {
        rc = true;
        break;
      }
    }
    return rc;
  }

  /**
  * Spawn a thread that will create a new worker.
  * @return the creating thread and worker is returned in the provided buffer.
  */
  private Thread createSocket_Threaded(final String hostName, final int portNumber, final Socket[] socketBuffer, final Throwable[] errBuffer) {
    Thread th = new ThreadTraced("Socket Creator") {
      public void runTraced() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "runTraced()");
        if (trace != null) trace.args(hostName);
        if (trace != null) trace.args(portNumber);
        try {
          Socket socket = createSocket(hostName, portNumber);
          socketBuffer[0] = socket;
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
          errBuffer[0] = t;
        }
        if (trace != null) trace.exit(getClass(), socketBuffer[0]);
      }
    };
    th.setDaemon(true);
    th.start();
    return th;
  }

  /**
  * @return newly created worker.
  */
  private Socket createSocket(final String hostName, final int portNumber) throws UnknownHostException, IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "createSocket(String hostName, int portNumber");
    if (trace != null) trace.args(hostName);
    if (trace != null) trace.args(portNumber);

    Socket socket = null;

    if (!destroyed && !destroying) {
      // Create the workers for serving connecitons
      // This could throw exceptions if connection to server is not established!
      final Socket[] socketBuffer = new Socket[1];
      final Exception[] exceptionBuffer = new Exception[1];

      Stats.setStatusAll("createSocket() > "+hostName+":"+portNumber);

      // try establishing a new connection in a seperate thread... so it doesn't block too long...
      Thread socketConnector = new ThreadTraced("Socket Connector") {
        public void runTraced() {
          Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(getClass(), "createSocket.socketConnector.runTraced()");
          if (trace != null) trace.data(10, "hostName", hostName);
          if (trace != null) trace.data(11, "portNumber", portNumber);
          if (trace != null) trace.data(12, "penalizedSocketType", penalizedSocketType);

          try {
            if (hostName.toLowerCase().startsWith("http://")) {
              // Handle special http sockets to enable communication through otherwise firewalled environments.
              // If HTTP_Socket class is not available, comment out this block and skip to 'else'.... which will throw
              // an exception and hopefully another server is available to make a socket connection.
              String host = hostName.substring("http://".length());
              try {
                if (HTTP_Socket.class.equals(penalizedSocketType)) {
                  if (trace != null) trace.data(20, "sleep DELAY_PENALIZED_CONNECTION_TYPE", DELAY_PENALIZED_CONNECTION_TYPE);
                  Thread.sleep(DELAY_PENALIZED_CONNECTION_TYPE);
                } else if (penalizedSocketType != null) {
                  long partialDelay = DELAY_PROTOCOLED_CONNECTION / 3;
                  if (trace != null) trace.data(21, "protocoled socket short sleep due to penalization of other type", partialDelay);
                  Thread.sleep(partialDelay);
                } else {
                  if (trace != null) trace.data(22, "sleep DELAY_PROTOCOLED_CONNECTION", DELAY_PROTOCOLED_CONNECTION);
                  Thread.sleep(DELAY_PROTOCOLED_CONNECTION);
                }
              } catch (InterruptedException x) {
              }
              socketBuffer[0] = new HTTP_Socket(host, portNumber, null, 0);
            } else {
              try {
                if (Socket.class.equals(penalizedSocketType)) {
                  if (trace != null) trace.data(30, "sleep DELAY_PENALIZED_CONNECTION_TYPE", DELAY_PENALIZED_CONNECTION_TYPE);
                  Thread.sleep(DELAY_PENALIZED_CONNECTION_TYPE);
                } else {
                  if (trace != null) trace.data(31, "sleep 0");
                }
              } catch (InterruptedException x) {
              }
              socketBuffer[0] = new Socket(hostName, portNumber);
              socketBuffer[0].setSoTimeout(ServerInterfaceWorker.PING_PONG_INTERVAL + (1000 * 10)); // a little more than ping-pong interval
            }
          } catch (UnknownHostException e1) {
            exceptionBuffer[0] = e1;
          } catch (IOException e2) {
            exceptionBuffer[0] = e2;
          } catch (RuntimeException e3) {
            exceptionBuffer[0] = e3;
          } catch (Throwable t) {
          }
          if (trace != null) trace.exit(getClass());
        }
      };
      socketConnector.setDaemon(true);
      socketConnector.start();

      // Wait for established connection reasonable amount of time.
      try {
        socketConnector.join(CONNECTION_TIMEOUT+MAX_CONNECTION_DELAY);
      } catch (InterruptedException e) {
      }

      if (exceptionBuffer[0] != null) {
        if (exceptionBuffer[0] instanceof UnknownHostException)
          throw new UnknownHostException(exceptionBuffer[0].getMessage());
        else if (exceptionBuffer[0] instanceof IOException)
          throw new IOException(exceptionBuffer[0].getMessage());
        else if (exceptionBuffer[0] instanceof RuntimeException)
          throw new RuntimeException(exceptionBuffer[0].getMessage());
      }

      if (socketConnector.isAlive())
        socketConnector.interrupt();

      socket = socketBuffer[0];

      if (socket == null)
        throw new IOException("Timeout reached while connecting to " + hostName + " at port " + portNumber);

      if (trace != null) trace.data(10, "new connection openned on socket", socket);
      if (trace != null) trace.data(11, "socket.getInetAddress()", socket.getInetAddress());
      if (trace != null) trace.data(12, "socket.getLocalAddress()", socket.getLocalAddress());
      if (trace != null) trace.data(13, "socket.getLocalPort()", socket.getLocalPort());
      if (trace != null) trace.data(14, "socket.getPort()", socket.getPort());
      if (trace != null) trace.data(15, "socket.getClass()", socket.getClass());
      if (trace != null) trace.data(16, "socket.getKeepAlive()", socket.getKeepAlive());
      if (trace != null) trace.data(17, "socket.getReceiveBufferSize()", socket.getReceiveBufferSize());
      if (trace != null) trace.data(18, "socket.getSendBufferSize()", socket.getSendBufferSize());
      if (trace != null) trace.data(19, "socket.getSoLinger()", socket.getSoLinger());
      if (trace != null) trace.data(20, "socket.getSoTimeout()", socket.getSoTimeout());
      if (trace != null) trace.data(21, "socket.getTcpNoDelay()", socket.getTcpNoDelay());

      StringBuffer sb = new StringBuffer();
      sb.append("createSocket() < "+ socket+"\n");
      sb.append("getInetAddress()="+ socket.getInetAddress()+"\n");
      sb.append("getLocalAddress()="+ socket.getLocalAddress()+"\n");
      sb.append("getLocalPort()="+ socket.getLocalPort()+"\n");
      sb.append("getPort()="+ socket.getPort()+"\n");
      sb.append("getClass()="+ socket.getClass()+"\n");
      sb.append("getKeepAlive()="+ socket.getKeepAlive()+"\n");
      sb.append("getReceiveBufferSize()="+ socket.getReceiveBufferSize()+"\n");
      sb.append("getSendBufferSize()="+ socket.getSendBufferSize()+"\n");
      sb.append("getSoLinger()="+ socket.getSoLinger()+"\n");
      sb.append("getSoTimeout()="+ socket.getSoTimeout()+"\n");
      sb.append("getTcpNoDelay()="+ socket.getTcpNoDelay());
      Stats.setStatusAll(sb.toString());
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
    return socket;
  }

  private PriorityFifoReaderI getRequestPriorityFifoReaderI() {
    return jobFifo;
  }

  private FifoWriterI getReplyFifoWriterI() {
    return executionQueue;
  }


  private boolean destroying = false;
  private boolean destroyed = false;
  public boolean isDestroyed() {
    return destroyed;
  }
  public boolean isDestroying() {
    return destroying;
  }

  /** Destroys this object and invalidates its state releasing all resources. */
  public void destroyServer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "destroy()");

    destroying = true;

    try {
      disconnectAndClear();
    } catch (Throwable t) {
    }

    synchronized (this) {
      if (!destroyed) {
        destroyed = true;

        try {
          if (loginCompletionNotifier != null)
            loginCompletionNotifier.serverDestroyed(this);
        } catch (Throwable t) {
        }

        // clear the job queue, killing and removing all jobs
        try {
          if (jobFifo != null) {
            while (true) {
              Object o = jobFifo.remove();
              if (o instanceof MessageAction) {
                MessageAction msgAction = (MessageAction) o;
                ProgMonitorI pm = ProgMonitorPool.getProgMonitor(msgAction.getStamp());
                if (pm != null)
                  pm.jobKilled();
              }
              if (jobFifo.size() == 0)
                break;
            }
          }
        } catch (Throwable t) {
        }

        // in case there are any progress monitors left over somewhere, kill them
        try {
          ProgMonitorPool.killAll();
        } catch (Throwable t) {
        }

        // kill the reply queue (execution queue)
        if (trace != null) trace.data(31, "killing execution queue");
        try {
          executionQueue.clear();
          executionQueue.close();
          executionQueue = null;
        } catch (Throwable t) {
        }
        if (trace != null) trace.data(32, "execution queue killed");

        // kill the independentExecutionQueue
        if (trace != null) trace.data(33, "killing independent execution queue");
        try {
          independentExecutionQueue.clear();
          independentExecutionQueue.close();
          independentExecutionQueue = null;
        } catch (Throwable t) {
        }
        if (trace != null) trace.data(34, "execution queue killed");

        // release all threads waiting on the stamps
        if (trace != null) trace.data(51, "releasing all threads waiting on stamps");
        ArrayList tempStampList = new ArrayList();
        try {
          synchronized (stampList) {
            // avoid deadlock by copying stamps and notifying threads outside of "stampList" synchronized block
            tempStampList.addAll(stampList);
            stampList.clear();
            synchronized (stampList2) {
              stampList2.clear();
            }
            synchronized (doneList) {
              doneList.clear();
            }
            stampList.notifyAll();
          } // end synchronized (stampList)
          for (int i=0; i<tempStampList.size(); i++) {
            Stamp stamp = (Stamp) tempStampList.get(i);
            synchronized (stamp) {
              stamp.notifyAll();
              // In case outstanding jobs were not in the queue, but already sent to server,
              // kill corresponding progress monitors so they don't popup in a few seconds.
              ProgMonitorI pm = ProgMonitorPool.getProgMonitor(stamp.longValue());
              if (pm != null)
                pm.jobKilled();
            }
          }
        } catch (Throwable t) {
        }
        if (trace != null) trace.data(70, "all threads released and stamps cleared.");

        if (trace != null) trace.data(71, "clearing data cach");
        FetchedDataCache.getSingleInstance().clear();
        if (trace != null) trace.data(80, "data cach cleared");
      }
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Disconnects the application and prevents workers from automatically
  * establishing any new connections.
  * Clear all account data and the cache -- removes all user objects.
  */
  public void disconnectAndClear() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "disconnectAndClear()");

    disconnect();
    // clear the cache
    FetchedDataCache.getSingleInstance().clear();

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }


  /**
  * Disconnects the application and prevents workers from automatically
  * establishing any new connections.
  */
  private void disconnect() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "disconnect()");

    // clear last login action so no worker can automatically login
    if (lastLoginMessageAction != null) {
      setLoginMsgAction(null);
      try {
        if (workers.size() > 0) {
          int countWorkers = workers.size();
          for (int i=0; i<countWorkers; i++) {
            submitAndReturn(new MessageAction(CommandCodes.USR_Q_LOGOUT));
          }
          if (countWorkers > 0) {
            // wait max 3 seconds for LOGOUT request to have a chance to be sent
            for (int i=0; i<300; i++) {
              if (workers.size() > 0)
                Thread.sleep(10);
            }
          }
        }
      } catch (Throwable t) {
      }
    }
    hostsAndPorts = null;
    try {
      destroyWorkers();
    } catch (Throwable t) {
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }


  //===========================================================================
  // Worker Manager interface methods
  //===========================================================================
  /**
  * Logs out all workers and disconnects them.
  * This call causes loss of all knowledge about connections for workers and data cache
  */
  public void destroyWorkers() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "logoutWorkers()");
    // kill all workers
    if (trace != null) trace.data(10, "killing all workers");

    // avoid blocking the thread on the 'workers' list
    ArrayList workersToDestroyL = new ArrayList();
    synchronized (workers) {
      workersToDestroyL.addAll(workers);
    }

    // do the destroying without-blocking
    int size = workersToDestroyL.size();
    for (int i=0; i<size; i++) {
      ServerInterfaceWorker worker = (ServerInterfaceWorker) workersToDestroyL.get(i);
      try {
        worker.destroyWorker();
      } catch (Throwable t) {
      }
    }

    if (trace != null) trace.data(20, "workers killed");
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Sets the last successful login action, called by workers after successful login.
  */
  public void setLoginMsgAction(MessageAction loginMsgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "setLoginMsgAction(MessageAction loginMsgAction)");
    if (trace != null) trace.args(loginMsgAction);
    lastLoginMessageAction = loginMsgAction;
    notifyStatusChanged();
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  public boolean isLastLoginMsgActionSet() {
    return lastLoginMessageAction != null;
  }
  public boolean isLoggedIn() {
    boolean rc = false;
    if (isLastLoginMsgActionSet())
      if (getFetchedDataCache().getMyUserId() != null)
        rc = true;
    return rc;
  }
  public ProtocolMsgDataSet getLoginMsgDataSet() {
    MessageAction lastLogin = lastLoginMessageAction;
    ProtocolMsgDataSet loginDataSet = null;
    if (lastLogin != null) {
      ProtocolMsgDataSet tempDataSet = lastLogin.getMsgDataSet();
      if (tempDataSet instanceof Obj_EncSet_Co) {
        loginDataSet = ((Obj_EncSet_Co) tempDataSet).dataSet;
      } else {
        loginDataSet = tempDataSet;
      }
    }
    return loginDataSet;
  }

  public synchronized void setRemoteSessionID(Long remoteSessionID) {
    this.remoteSessionID = remoteSessionID;
  }
  public synchronized Long getRemoteSessionID() {
    return remoteSessionID;
  }

  public void markLastWorkerActivityStamp() {
    if (!lastWorkerActivityResyncPending && lastWorkerActivityStamp != null) {
      if (System.currentTimeMillis() - lastWorkerActivityStamp.getTime() >= 2*ServerInterfaceWorker.PING_PONG_INTERVAL) {
        lastWorkerActivityResyncPending = true;
      }
    }
    lastWorkerActivityStamp = new Date();
  }

  /**
  * Worker notifies the manager that it quit processing and it will no longer be active.
  */
  public void workerDone(ServerInterfaceWorker worker, boolean cleanLogout, boolean suppressConnectionTypePenalization) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "workerDone(ServerInterfaceWorker worker, boolean cleanLogout, boolean suppressConnectionTypePenalization)");
    if (trace != null) trace.args(worker);
    if (trace != null) trace.args(cleanLogout);
    if (trace != null) trace.args(suppressConnectionTypePenalization);

    boolean workerRemoved;
    // get consistent state using synchronized block
    synchronized (workers) {
      workerRemoved = workers.remove(worker);
      // destroyed worker could be replaced by new one which should now be updating connection counts
      if (!destroyed && !destroying) Stats.setConnections(workers.size(), getWorkerCounts());
    }

    if (workerRemoved) {
      // penalize connection type if it broke and lasted for short time
      if (!suppressConnectionTypePenalization) {
        Class workerSocketType = worker.getSocketType();
        if (!cleanLogout && (System.currentTimeMillis()-worker.getSocketCreationStamp()) < 1000L * 60L * 3L) { // 3 minutes
          if (trace != null) trace.data(10, "assigning penalizedSocketType", workerSocketType);
          penalizedSocketType = workerSocketType;
          currentHostShouldIncrement = true;
        } else if (workerSocketType.equals(penalizedSocketType)) {
          // if clean logout or connection failure after long time of being alive, clear out penalized Socket type
          if (trace != null) trace.data(11, "clearing penalizedSocketType");
          penalizedSocketType = null;
          currentHostShouldIncrement = false;
        } else {
          if (trace != null) trace.data(12, "penalizedSocketType unchanged and equals", penalizedSocketType);
        }
      }

      // if not loging out, then maybe we need to create more workers if one died.
      if (!cleanLogout) {
        if (workers.size() == 0) {
          if (lastForcedWorkerStamp == null) {
            // Only stamp when last worker exits and do not overwrite stamps to newer
            // as we must ignore failed connection attempts and only mark last active
            // connection brakages.
            lastForcedWorkerStamp = new Date();
          }
        }
      }
      boolean isMainQuit = false;
      // If a MAIN WORKER quits, we need to designate another main worker.
      synchronized (mainWorkerMonitor) {
        if (worker == mainWorker) {
          mainWorker = null;
          isMainQuit = true;
        }
      }
      if (isMainQuit) {
        notifyStatusChanged();
        triggerCheckForMainWorker();
      }
      // Since a woker died, trigger the check for waiting jobs.
      jobScanner.triggerCheckToServeNow();
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * The worker that picks up the SYS_Q_NOTIFY message action should claim Main Worker status.
  */
  public void claimMainWorker(ServerInterfaceWorker worker) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "claimMainWorker(ServerInterfaceWorker worker)");
    if (trace != null) trace.args(worker);

    boolean isChanged = false;
    synchronized (mainWorkerMonitor) {
      if (trace != null) trace.data(10, worker != null ? "Claiming new main worker." : "Main worker won't be claimed.");
      if (mainWorker == null) {
        if (trace != null) trace.data(20, "Main worker submission cycle done.");
        mainWorkerSubmition = false;
        // since workers submitting NOTIFY requests claim Main Persistent status, this cycle cannot break when there is a comm failure
        mainWorker = worker;
        isChanged = true;
      }
      // Allow multiple main workers because this can happen briefly when main worker connection is being recycled
//      else
//        throw new IllegalStateException("Main Worker already claimed!");
    }
    if (isChanged)
      notifyStatusChanged();

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * The worker that picks up reply SYS_A_NOTIFY to the SYS_Q_NOTIFY request should claim persistent status.
  */
  public void claimPersistent(ServerInterfaceWorker worker) {
    // Let them claim persistency, we won't deny any.
    notifyStatusChanged();
  }

  /**
  * Causes that a Main Worker is designated or a submission is in progress.
  * If necessary, submits a Message Action to start Main Worker designation.
  * That message upon being picked up by some worker will grant a Main Worker
  * status when claimMainWorker() is called by that worker.
  */
  private void triggerCheckForMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "triggerCheckForMainWorker()");

    if (!destroyed && !destroying) {
      boolean submitNow = false;
      synchronized (mainWorkerMonitor) {
        if (mainWorker == null && mainWorkerSubmition == false) {
          mainWorkerSubmition = true;
          submitNow = true;
        }
      } // end synchronized

      if (submitNow) {
        if (trace != null) trace.data(10, "main worker needed - submitting request");
        // Whichever worker picks it up, it should claim Main Worker and send it to the server to register for notifications.
        // Quietly add a NOTIFY message without having aditional workers created to serve it -- do not use submitAndReturn.
        // This message is only meant for already existing workers, otherwise we would have an infinite loop.
        MessageAction msgActionNotify = new MessageAction(CommandCodes.SYS_Q_NOTIFY);
        enqueueProgMonitor(msgActionNotify);
        jobFifo.addJob(msgActionNotify);
        jobScanner.triggerCheckToServeNow();
      } else {
        if (trace != null) trace.data(20, "main worker exists");
      }
    } else {
      if (trace != null) trace.data(30, "SIL destroyed - check skipped");
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Client mode or Server mode
  * @return
  */
  public boolean isClientMode() {
    return isClient;
  }

  /**
  * @return true if the specified worker is registered as the Main Worker.
  */
  public boolean isMainWorker(ServerInterfaceWorker worker) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "isMainWorker(ServerInterfaceWorker worker)");
    if (trace != null) trace.args(worker);

    boolean rc = false;
    if (worker != null) {
      if (worker == mainWorker) {
        rc = true;
      }
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  /**
  * @return true if the specified worker is registered as the Main Worker.
  */
  public boolean isPersistentMainWorker(ServerInterfaceWorker worker) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "isPersistentMainWorker(ServerInterfaceWorker worker)");
    if (trace != null) trace.args(worker);

    boolean rc = false;
    if (worker != null) {
      if (worker.isPersistent()) {
        if (worker == mainWorker) {
          rc = true;
        }
      }
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  /**
  * @return true if there is a designated Main Worker.
  * Main Worker is registered when any Worker grabs a NOTIFY request and is ready to write it to server.
  */
  public boolean hasMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "hasMainWorker()");
    boolean rc = false;
    if (mainWorker != null) {
      rc = true;
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  /**
  * @return true if there is a main worker which is persistent;
  * Main Worker becomes persistent when it retrieves a NOTIFY reply from the server allowing it to become persistent.
  */
  public boolean hasPersistentMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "hasPersistentMainWorker()");
    ServerInterfaceWorker mWorker = mainWorker;
    boolean rc = mWorker != null ? mWorker.isPersistent() : false;
    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  public short getPersistentMainWorkerServerBuild() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "getPersistentMainWorkerServerBuild()");
    ServerInterfaceWorker mWorker = mainWorker;
    boolean isPersistent = mWorker != null ? mWorker.isPersistent() : false;
    short serverBuild = isPersistent ? mWorker.getSessionContextServerBuild() : 0;
    if (trace != null) trace.exit(ServerInterfaceLayer.class, serverBuild);
    return serverBuild;
  }

  /**
  * @return maximum number of workers this manager can have
  */
  public int getMaxWorkerCount() {
    return getMaxAdjustedConnectionCount();
  }

  /**
  * Push-back a request to be placed on the job queue again.
  */
  public void pushbackRequest(MessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "pushbackRequest(MessageAction msgAction)");
    if (trace != null) trace.args(msgAction);
    submitAndReturnNow(msgAction);
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
  * Called to inform that some worker has just logged in.
  */
  public void workerLoginComplete(ServerInterfaceWorker worker, boolean loginSuccessful) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "workerLoginComplete(ServerInterfaceWorker worker, boolean loginSuccessful)");
    if (trace != null) trace.args(worker);
    if (trace != null) trace.args(loginSuccessful);
    synchronized (workers) {
      // destroyed worker could be replaced by new one which should now be updating connection counts
      if (!destroyed && !destroying) Stats.setConnections(workers.size(), getWorkerCounts());
    }
    // Notify of successful login.
    if (loginSuccessful && loginCompletionNotifier != null)
      loginCompletionNotifier.loginComplete(this);
    // Each successful login warants check for Main Worker... incase we are doing some kind of auto-re-login due to connection drop etc.
    if (loginSuccessful) {
      triggerCheckForMainWorker();
      hasEverLoggedInSuccessfully = true;
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }


  //===========================================================================
  //===========================================================================

  /**
  * Private inner class.
  * Executes messages from the Execution Queue by handing them over to the waiting
  * processes, or by handing them over to the independent execution queue if no
  * process is waiting for it.  This queue does not actually run the replies, just manages them.
  */
  private class QueueExecutionFunction extends Object implements ProcessingFunctionI {
    /* =======================================================
    Methods from ProcessingFunctionI for the 'executionQueue'
    ========================================================= */

    /** start processing cached objects */
    public Object processQueuedObject(Object obj) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueExecutionFunction.class, "processQueuedObject(Object)");
      if (trace != null) trace.args(obj);

      // execution queue in global try
      try {

        ClientMessageAction nextMsgAction = (ClientMessageAction) obj;
        // set the ServerInterfaceLayer for every message reply that is about to be processed.
        nextMsgAction.setServerInterfaceLayer(ServerInterfaceLayer.this);

        if (trace != null) trace.data(10, "wait until submitter is prepared and ready, if not prepared escape without waiting");
        Stamp stamp = new Stamp(nextMsgAction.getStamp());
        Stamp exactStamp = null;
        int index = -1;

        // wait until submitter is prepared and ready, if not prepared escape without waiting
        boolean submitterPrepared = true;
        boolean submitterReady = false;
        long waitStart = System.currentTimeMillis();
        while (submitterPrepared && !submitterReady) {
          if (System.currentTimeMillis() - waitStart > 10000) {
            //System.out.println("Waited for submitter prepared-ready state over 10s, escaping!");
            if (trace != null) trace.data(15, "Waited for submitter prepared-ready state over 10s, escaping!");
            break;
          }
          if (trace != null) trace.data(20, "synchronizing on stampList to check if prepared");
          synchronized (stampList) {
            index = stampList.indexOf(stamp);
            submitterPrepared = index >= 0;
            if (trace != null) trace.data(21, "prepared=", submitterPrepared);
            if (submitterPrepared) {
              exactStamp = (Stamp) stampList.get(index);
              if (trace != null) trace.data(22, "synchronizing on stampList2 to check if ready");
              synchronized (stampList2) {
                submitterReady = stampList2.contains(stamp);
                if (trace != null) trace.data(23, "ready=", submitterReady);
              }
              if (!submitterReady) {
                try {
                  // wait for 1s or until submitter reaches ready state
                  if (trace != null) trace.data(24, "wait for 1s or until submitter reaches ready state");
                  stampList.wait(1000);
                } catch (InterruptedException e) {
                }
              }
            }
          }
        } // end waiting for prepared-ready state

        // use exact stamp if possible to match synchronizing block of the receiving thread
        Object lStamp = exactStamp != null ? exactStamp : stamp;
        synchronized (lStamp) {
          if (submitterPrepared && submitterReady) {
            synchronized (stampList) {
              // Recheck if waiting thread didn't escape due to timeout
              // while we were not synchronized on the original exactStamp.
              index = stampList.indexOf(stamp);
            }
          }
          // If someone is waiting for it for sure
          if (submitterPrepared && submitterReady && index >= 0) {
            // Submitter is ready for sure, so put the reply on the done list then
            // notify on monitor stamp to realease the submitter!
            if (trace != null) trace.data(40, "adding reply to done list");
            synchronized (doneList) {
              doneList.add(nextMsgAction);
              if (trace != null) trace.data(41, "adding done");
            }
            // wake up sibmitter to pickup the reply from done list
            if (trace != null) trace.data(51, "synchron done on exactStamp=", exactStamp);
            if (trace != null) trace.data(52, "notify/wake-up the Thread waiting on stamp=", lStamp);
            lStamp.notifyAll();
            // cleanup stampLists in case submitter doesn't do it in time for next queued reply
            if (trace != null) trace.data(55, "about to perform stamp cleanup");
            synchronized (stampList) {
              if (trace != null) trace.data(56, "performing stamp cleanup");
              // remove the stamp and the reply message from the lists
              stampList.remove(lStamp);
              synchronized (stampList2) {
                stampList2.remove(lStamp);
              }
            }
            if (trace != null) trace.data(57, "stamp cleanup done");
          } else {
            if (trace != null) trace.data(60, "no one waiting for this reply, submit it to independent execution queue");
            // no one waiting for this reply, run it.
            // Running independent jobs by independent queue frees up the execution queue while
            // those jobs are being run, plus it ensures the FCFS order of independent jobs.
            if (!destroyed) {
              synchronized (ServerInterfaceLayer.this) {
                try {
                  independentExecutionQueue.add(nextMsgAction);
                } catch (Throwable t) {
                  // maybe the queue got closed... ignore it then
                }
              }
            }
          }
        }
      } catch (Throwable t) {
        // execution of server reply went wrong -- critical error.
        if (trace != null) trace.exception(QueueExecutionFunction.class, 100, t);
        MyUncaughtExceptionHandlerOps.unhandledException(t);
      }

      if (trace != null) trace.exit(QueueExecutionFunction.class, null);
      return null;
    } // end processQueuedObject
  } // end inner class QueueExecutionFunction



  private class WaitingJobsScanner extends ThreadTraced {
    private Object lastScanHeadJob = null;
    private boolean triggeredMonitor = false;
    private final Object triggerMonitor = new Object();
    private WaitingJobsScanner() {
      super("Waiting Jobs Scanner");
      setDaemon(true);
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "WaitingJobsScanner.runTraced()");

      int DELAY_NORMAL = 1000;
      int DELAY_AFTER_CHECK = 10000;
      int delay = DELAY_NORMAL;
      while (!destroyed) {
        try {
          boolean checkForImmediateJobsNow = false;
          boolean triggeredButAlreadyConsumed = false;
          synchronized (triggerMonitor) {
            if (!triggeredMonitor) {
              try {
                if (trace != null) trace.data(10, "waiting in jobs scanner for (ms)", delay);
                triggerMonitor.wait(delay);
                if (trace != null) trace.data(11, "waiting in jobs scanner done");
              } catch (InterruptedException e) {
                if (trace != null) trace.data(12, "waiting in jobs scanner interrupted");
                triggeredMonitor = true;
              }
              delay = DELAY_NORMAL;
            }
            // If scanner sleep was interrupted, then check immediately in job queue
            // for waiting jobs and try to create workers to serve them.
            if (triggeredMonitor && jobFifo.size() > 0) {
              checkForImmediateJobsNow = true;
            } else if (triggeredMonitor) {
              triggeredButAlreadyConsumed = true;
            }
            triggeredMonitor = false;
          } // end synchronized
          if (trace != null) trace.data(20, "checkForImmediateJobsNow", checkForImmediateJobsNow);
          if (trace != null) trace.data(21, "triggeredButAlreadyConsumed", triggeredButAlreadyConsumed);
          // if we were interrupted because of Destroy, then exit now.
          if (destroyed) break;
          // If immediate jobs
          if (checkForImmediateJobsNow) {
            ensureEnoughAllWorkersExist();
          } else if (triggeredButAlreadyConsumed) {
            triggerCheckForMainWorker();
          } else {
            Object headJob = null;
            try {
              if (jobFifo.size() > 0)
                headJob = jobFifo.peek();
//              // Ignore file transfers as they will create their own heavy connection anyway.
//              if (headJob != null) {
//                if (headJob instanceof MessageAction &&
//                    jobFifo.getJobType((MessageAction) headJob) == JobFifo.JOB_TYPE_HEAVY)
//                {
//                  headJob = null;
//                }
//              }
            } catch (NoSuchElementException nsee) {
            }
            // If we are checking for the second time and the same job is still at the head
            // of the queue, then it looks like its stuck and we should help it
            // creating additional (at least one) worker to handle it.
            if (lastScanHeadJob != null && headJob != null && lastScanHeadJob == headJob) {
              boolean forceAdditionalConnection = true;
              // Don't want this thread to retry connection if the general connectivity is totally broken.
              // Another mechanizm is meant for that.
              if (hasPersistentMainWorker()) {
                ensureEnoughAllWorkersExist(forceAdditionalConnection);
                delay = DELAY_AFTER_CHECK;
              }
            }
            lastScanHeadJob = headJob;
          } // end no immediate jobs
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
          // If client with no prior login encounters a connection exception, it has to exit.
          if (isClient && lastLoginMessageAction == null && t instanceof SILConnectionException) {
            destroyServer();
            int msgType = NotificationCenter.ERROR_MESSAGE;
            String title = "No Connection";
            String key = msgType+title;
            NotificationCenter.show(key, msgType, title, t.getMessage());
          }
          // Delay before we do anything with re-connectivity.
          try { Thread.sleep(DELAY_NEW_CONNECTION_AFTER_NET_ERROR); } catch (InterruptedException e) { }
        }
      } // end while

      if (trace != null) trace.exit(getClass());
    }
    private void triggerCheckToServeNow() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "triggerCheckToServeNow()");
      synchronized (triggerMonitor) {
        if (trace != null) trace.data(10, "calling notifyAll() on the waiting job monitor");
        triggeredMonitor = true;
        triggerMonitor.notifyAll();
      }
      if (trace != null) trace.exit(getClass());
    }
  } // end class BusyConnectionScanner

  private class SleepMonitor extends ThreadTraced {
    private long stampBeforeSleep = 0;
    private SleepMonitor() {
      super("SleepMonitor");
      setDaemon(true);
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "SleepMonitor.runTraced()");
      while (!destroyed) {
        try {
          long stampNow = System.currentTimeMillis();
          boolean invalidStamp = stampBeforeSleep > stampNow;
          if (stampBeforeSleep > 0) {
            // Check if we lost time sleeping that exceeds "RECONNECTION UPDATE" interval + 1 second.
            if (stampNow - stampBeforeSleep > ServerInterfaceWorker.TIMEOUT_TO_TRIGGER_RECONNECT_UPDATE + 1000 || invalidStamp) {
              sleepDetected();
            }
          }
          stampBeforeSleep = stampNow;
          long sleepSome = ServerInterfaceWorker.TIMEOUT_TO_TRIGGER_RECONNECT_UPDATE/10 + 1000;
          Thread.sleep(sleepSome);
        } catch (Throwable t) {
        }
      }
      if (trace != null) trace.exit(getClass());
    }
    private void sleepDetected() {
      // invalidate views of fetched folders so that client reloads them on demand
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord[] folders = cache.getFolderRecords();
      for (int i=0; i<folders.length; i++) {
        Long folderId = folders[i].folderId;
        cache.markFolderViewInvalidated(folderId, true);
      }
    }
  }

  /**
  * Notifies about changes to:
  * lastLoginMessageAction,
  * mainWorker,
  */
  public interface OnStatusChangeListener {
    public void onStatusChanged();
  }
  private void notifyStatusChanged() {
    if (statusListenersL != null) {
      synchronized (statusListenersL) {
        Iterator iter = statusListenersL.iterator();
        while (iter.hasNext()) {
          OnStatusChangeListener l = (OnStatusChangeListener) iter.next();
          l.onStatusChanged();
        }
      }
    }
  }
  public void registerForStatusChanged(OnStatusChangeListener listener) {
    if (statusListenersL == null)
      statusListenersL = new HashSet();
    synchronized (statusListenersL) {
      statusListenersL.add(listener);
    }
  }
  public void unregisterForStatusChanged(OnStatusChangeListener listener) {
    if (statusListenersL != null) {
      synchronized (statusListenersL) {
        statusListenersL.remove(listener);
      }
    }
  }

//  public String getDebugInfo() {
//    StringBuffer sb = new StringBuffer();
//    sb.append("MAX_CONNECTION_RETRY_COUNT=");
//    sb.append(MAX_CONNECTION_RETRY_COUNT);
//    sb.append("\n");
//
//    sb.append("connectionRetryCount=");
//    sb.append(connectionRetryCount);
//    sb.append("\n");
//
//    sb.append("maxConnectionCount=");
//    sb.append(maxConnectionCount);
//    sb.append("\n");
//
//    sb.append("workers=");
//    sb.append(workers);
//    sb.append("\n");
//
//
//    sb.append("executionQueue=");
//    sb.append(executionQueue);
//    sb.append("\n");
//
//    sb.append("jobFifo=");
//    sb.append(jobFifo);
//    sb.append("\n");
//
//    sb.append("jobScanner=");
//    sb.append(jobScanner);
//    sb.append("\n");
//
//    sb.append("independentExecutionQueue=");
//    sb.append(independentExecutionQueue);
//    sb.append("\n");
//
//    sb.append("stampList=");
//    sb.append(stampList);
//    sb.append("\n");
//
//    sb.append("stampList2=");
//    sb.append(stampList2);
//    sb.append("\n");
//
//    sb.append("doneList=");
//    sb.append(doneList);
//    sb.append("\n");
//
//    sb.append("lastLoginMessageAction=");
//    sb.append(lastLoginMessageAction);
//    sb.append("\n");
//
//    sb.append("remoteSessionID=");
//    sb.append(remoteSessionID);
//    sb.append("\n");
//
//    sb.append("hostsAndPorts=");
//    sb.append(hostsAndPorts);
//    sb.append("\n");
//
//    sb.append("currentHostIndex=");
//    sb.append(currentHostIndex);
//    sb.append("\n");
//
//    sb.append("penalizedSocketType=");
//    sb.append(penalizedSocketType);
//    sb.append("\n");
//
//    sb.append("mainWorker=");
//    sb.append(mainWorker);
//    sb.append("\n");
//
//    sb.append("mainWorkerSubmition=");
//    sb.append(mainWorkerSubmition);
//    sb.append("\n");
//
//    sb.append("isClient=");
//    sb.append(isClient);
//    sb.append("\n");
//
//    sb.append("lastForcedWorkerStamp=");
//    sb.append(lastForcedWorkerStamp);
//    sb.append("\n");
//    sb.append("lastWorkerActivityStamp=");
//    sb.append(lastWorkerActivityStamp);
//    sb.append("\n");
//    sb.append("lastWorkerActivityResyncPending=");
//    sb.append(lastWorkerActivityResyncPending);
//    sb.append("\n");
//
//    return sb.toString();
//  }

} // end class ServerInterfaceLayer