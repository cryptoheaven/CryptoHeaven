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

package com.CH_cl.service.engine;

import java.util.*;
import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;

import com.CH_co.trace.*;
import com.CH_co.monitor.*;
import com.CH_co.queue.*;
import com.CH_co.util.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;

import comx.HTTP_Socket.*;

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

  private static final boolean DEBUG_ON__SUPPERSS_RETRIES = false;

  private static final String PROPERTY_LAST_ENGINE_HOST = "lastEngineHost";
  private static final String PROPERTY_LAST_ENGINE_PORT = "lastEnginePort";

  // For every 3 additional non-heavy jobs waiting, create additional connection.
  private static final int FOR_EVERY_N_NON_HEAVY_JOBS_CREATE_CONNECTION = 5; // used to have 3 here

  // Delay between retrying establishing a new connection after connectivity broke.
  private static final int DELAY_NEW_CONNECTION_AFTER_NET_ERROR = 15 * 1000; // 15 sec

  // By default lets retry connections periodically indefinitly;
  private int MAX_CONNECTION_RETRY_COUNT = -1; // -1 for unlimited
  private int connectionRetryCount = 0;

  // main connection never transfer heavy jobs (files) unless they are not so big
  public static final long DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION = 50 * 1024;

  // The maximum number of connections to the server that we may establish.
  public static final int DEFAULT_MAX_CONNECTION_COUNT = 3;

  private int maxConnectionCount = DEFAULT_MAX_CONNECTION_COUNT;
  public static final String PROPERTY_NAME_MAX_CONNECTION_COUNT = "ServerInterfaceLayer" + "_maxConnCount";

  /** ArrayList of connection workers. */
  private final ArrayList workers = new ArrayList();
  /** All the ready messages go through this queue.
      No jobs are being run by that queue, they are handled to the submitting threads to be run. */
  private QueueMM1 executionQueue;
  /** Job Queue */
  private JobFifo jobFifo;
  /** Waiting Jobs Scanner to relieve the waiting jobs for extensive periods of time,
      and to pickup brand new jobs that came to the queue. */
  private WaitingJobsScanner jobScanner;
  /** The jobs that no one is waiting for are executed by the Independent queue.
      This queue ensures that jobs are executed in order that they arrive, and frees up the
      ExecutionQueue so that its not blocked while independent jobs are being run. */
  private QueueMM1 independentExecutionQueue;

  /** List to put the waiting stamps. */
  private final ArrayList stampList = new ArrayList();
  /** Secondary list to put the waiting stamps as a signal for execution thread to start its work. */
  private final ArrayList stampList2 = new ArrayList();
  /** List to put the fetched Client Message Actions for the waiting stamps. */
  private final ArrayList doneList = new ArrayList();

  /** Last successful login message. */
  private MessageAction lastLoginMessageAction;
  private Long remoteSessionID;

  /** Server host address and port number */
  private Object[][] hostsAndPorts;
  private int currentHostIndex;

  /** When a worker fails, remember its type to try to delay next one of the same type being created.
   * Re-connection mechanizm spawns multiple threads trying to connect to different hosts/ports with
   * possibly different connection protocols. We will delay the type that failed last to give better chance
   * for other to succeed first before this one connects again.  This should help to fight providers
   * deteriorating and breaking certain connection types.
   * Static variable because it is a property of our Internet provider connectivity, so must be global for all SILs.
   */
  private static Class penalizedSocketType;
  private static final int DELAY_PENALIZED_CONNECTION_TYPE = 5000;
  // always delay protocoled sockets just a tiny bit to allow plain Socket some advantage of being first to connect...
  private static final int DELAY_PROTOCOLED_CONNECTION  = 1500;

  /**
   * Main Worker should send Ping-Pong to retain a persistant connection.
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

  private BurstableMonitor burstableMonitorWorkerCreatings;
  private BurstableMonitor burstableMonitorWorkerCreationTrials;
  private BurstableMonitor burstableMonitorWorkersExceptions;

  /**
   * Creates new ServerInterfaceLayer
   * @param connectedSocket through which communication will take place
   */
  public ServerInterfaceLayer(Object[][] hostsAndPorts, boolean isClient) {
    this(hostsAndPorts, null, null, isClient);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ServerInterfaceLayer(Object[][] hostsAndPorts, boolean isClient)");
    if (trace != null) trace.args(hostsAndPorts);
    if (trace != null) trace.args(isClient);
    if (trace != null) trace.exit(ServerInterfaceLayer.class, this);
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
    this.executionQueue = new QueueMM1("Execution Queue", new QueueExecutionFunction());

    // Create the job queue
    this.jobFifo = new JobFifo();

    // Create the independent execution queue
    independentExecutor = independentExecutor != null ? independentExecutor : new IndependentClientQueueExecutionFunction(this);
    this.independentExecutionQueue = new QueueMM1("Independent Exec Queue", independentExecutor);

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

    if (trace != null) trace.exit(ServerInterfaceLayer.class, this);
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
   * @return the Fetched Data Cach storage where all data is to be cached.
   * There should be only one instance of this cach in the program runtime.
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

    if (!destroyed || msgAction.getActionCode() == CommandCodes.USR_Q_LOGOUT) {
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
      submitAndReturn(msgAction, 0, 0, null, null, null);
    else
      submitAndReturnNow(msgAction);
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  public void submitAndReturn(MessageAction msgAction, long timeout, int maxRetries) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturn(MessageAction msgAction, long timeout, int maxRetries)");
    submitAndReturn(msgAction, timeout, maxRetries, null, null, null);
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
    submitAndReturn(msgAction, timeout, 0, null, afterJob, timeoutJob);
  }
  public void submitAndReturn(final MessageAction msgAction, final long timeout, final Runnable replyReceivedJob, final Runnable afterJob, final Runnable timeoutJob) {
    submitAndReturn(msgAction, timeout, 0, replyReceivedJob, afterJob, timeoutJob);
  }
  public void submitAndReturn(final MessageAction msgAction, final long timeout, final int maxRetries, final Runnable replyReceivedJob, final Runnable afterJob, final Runnable timeoutJob) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndReturn(MessageAction msgAction, long timeout, int maxRetries, final Runnable replyReceivedJob, Runnable afterJob, Runnable timeoutJob)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);
    if (trace != null) trace.args(maxRetries);
    if (trace != null) trace.args(afterJob, timeoutJob);

    Thread th = new ThreadTraced("Job-Submitter-and-After-Job-Runner") {
      public void runTraced() {
        Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(getClass(), "submitAndReturn.runTraced()");
        try {
          boolean noTimeout = submitAndWait(msgAction, timeout, maxRetries, replyReceivedJob);
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
      synchronized (workers) {
        ensureEnoughFreeWorkers(forceAdditionalConnection);
        triggerCheckForMainWorker();
      }
      connectionRetryCount = 0;
    } catch (RuntimeException e) {
      // If we don't destroy server or its not already destroyed, the exception will be re-thrown...
      if (!destroyed) {
        connectionRetryCount ++;
        if (MAX_CONNECTION_RETRY_COUNT > -1 && connectionRetryCount > MAX_CONNECTION_RETRY_COUNT) {
          if (trace != null) trace.data(10, "SIL: DESTROY, connectionRetryCount", connectionRetryCount);
          destroyServer();
        } else { // do some stuff, then re-throw...
          if (trace != null) trace.data(20, "SIL: RETRY, connectionRetryCount", connectionRetryCount);
          // Rest after worker creating failure.
          // Stall the current thread for some 3 seconds on repetitive exception
          if (burstableMonitorWorkersExceptions == null)
            burstableMonitorWorkersExceptions = new BurstableMonitor(2, 3000, 0);
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
                if (destroyed) {
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

  private void warnIfOnAWTthread() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "warnIfOnAWTthread()");
    Thread th = Thread.currentThread();
    ThreadGroup thGroup = th.getThreadGroup();
    String thName = th.getName();
    String thGroupName = null;
    if (thGroup != null)
      thGroupName = thGroup.getName();
    if (thName.indexOf("AWT") >= 0) {
      String messageText = "This Warning is displayed only to users with ID < 100\n\nAWT Thread " + thName + " (group " + thGroupName + ") at \n\n" + Misc.getStack(new Exception("Blocking of AWT Thread detected!"));
      String title = "Warning: Network request using AWT Thread";
      if (trace != null) trace.info(100, messageText);
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Long uId = cache.getMyUserId();
      if (uId != null && uId.longValue() < 100) {
        NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, title, messageText);
        System.out.println(title);
        System.out.println(messageText);
        System.out.println();
      }
    }
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

    if (!destroyed || msgAction.getActionCode() == CommandCodes.USR_Q_LOGOUT) {
      warnIfOnAWTthread();

      Stamp lStamp = new Stamp(msgAction.getStamp());

      synchronized (stampList) {
        // register a stamp so that executor returns the reply message to the done list
        if (stampList.contains(lStamp)) {
          if (trace != null) trace.data(5, "stampList already contains " + lStamp);
          System.out.println("stampList already contains " + lStamp);
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
              System.out.println("stampList2 already contains " + lStamp);
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
            if (trace != null) trace.data(72, "timeout - clear any open progress dialogs");
            ProgMonitorPool.getProgMonitor(msgAction.getStamp()).jobKilled();
          }
        } // end synchronized (stampList)
      } // end synchronized

      // copy original Interrupts to the reply
      if (replyMsg != null) {
        replyMsg.setInterruptsFrom(msgAction);
      }
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class, replyMsg);
    return replyMsg;
  } // end submitAndFetchReply()

  public ClientMessageAction submitAndFetchReply(MessageAction msgAction, long timeout, int maxRetries) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "submitAndFetchReply(MessageAction, long timeout, int maxRetries)");
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(timeout);
    if (trace != null) trace.args(maxRetries);
    if (DEBUG_ON__SUPPERSS_RETRIES)
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
   * @return true if reply was recived in time, false when timeout reached.
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
        // TO-DO: running action here, will mess up the Default Progress Monitor 6 basic setps
        // unless DefaultProgMonitor is not used for those special actions that may have reply-requests!

        // Don't start a new DefaultReplyRunner thread here (because it recursively used ServerInterfaceLayer)
        // Just execute the action synchronously (in a MIN PRIORIY THREAD) while the user waits.
        final MessageAction[] returnBufferMsgAction = new MessageAction[1];
        Thread th = new ThreadTraced("Reply Runner") {
          public void runTraced() {
            returnBufferMsgAction[0] = DefaultReplyRunner.runAction(replyMsg);
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
    return maxConnectionCount - 1;
  }
  public int getMaxConnectionCount() {
    return maxConnectionCount;
  }
  public void setMaxConnectionCount(int maxConnections) {
    maxConnectionCount = maxConnections;
  }

  private void ensureEnoughFreeWorkers(boolean forceAdditionalConnection) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ensureEnoughFreeWorkers(boolean forceAdditionalConnection)");
    if (trace != null) trace.args(forceAdditionalConnection);

    if (workers.size() >= getMaxConnectionCount() || destroyed) {
      if (trace != null) trace.exit(ServerInterfaceLayer.class);
      return;
    }

    synchronized (workers) {
      // count free workers
      int countFreeAllWorkers = 0;
      int countFreeHeavyWorkers = 0;
      int countAllWorkers = workers.size();

      if (trace != null) trace.data(10, "for all workers, size=", countAllWorkers);
      for (int i=0; i<countAllWorkers; i++) {
        ServerInterfaceWorker worker = (ServerInterfaceWorker) workers.get(i);
        if (!worker.isBusy())
          countFreeAllWorkers ++;
        if (!isMainWorker(worker)) {
          countFreeHeavyWorkers += worker.isBusy() ? 0 : 1;
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
        countWorkersToCreate += (countAllJobs - countLargeFileJobs)/FOR_EVERY_N_NON_HEAVY_JOBS_CREATE_CONNECTION;
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
      }

      // limit number of all workers to the preset maximum
      if (countWorkersToCreate + countAllWorkers > getMaxConnectionCount()) {
        countWorkersToCreate = getMaxConnectionCount() - countAllWorkers;
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
        if (trace != null) trace.data(60, "increasing countWorkersToCreate from 0 to 1 so that persistant connection can be established");
        countWorkersToCreate = 1;
      }

      createWorkers(countWorkersToCreate);
    } // end synchronized

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  } // end ensureEnoughFreeWorkers


  /**
   * Make sure that at least one additional worker aside from the persistant connection
   * worker exists.  Useful as a preparation for file upload to establish a connection
   * prior to having the transaction ready to minimize the delay.
   */
  public void ensureAtLeastOneAdditionalWorker_SpawnThread() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "ensureAtLeastOneAdditionalWorker()");
    Thread th = new ThreadTraced("Additional Worker Ensurer") {
      public void runTraced() {
        Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(getClass(), "ServerInterfaceLayer.ensureAtLeastOneAdditionalWorker_SpawnThread.runTraced()");
        synchronized (workers) {
          int countAllWorkers = workers.size();
          if (trace != null) trace.data(10, "countAllWorkers", countAllWorkers);
          if (countAllWorkers < 2 && countAllWorkers < getMaxConnectionCount())
            createWorkers(1);
        }
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
    if (!destroyed) {
      synchronized (workers) {
        int countAllWorkers = workers.size();
        if (trace != null) trace.data(10, "numberOfWorkersToCreate", numberOfWorkersToCreate);
        // Create the calculated number of additional/new workers to
        // ensure that enough workers are free to handle the job queue.
        for (int i=0; i<numberOfWorkersToCreate; i++) {
          // Control throughput of worker creation as a safety mechanism for server connectivity
          if (burstableMonitorWorkerCreatings == null)
            burstableMonitorWorkerCreatings = new BurstableMonitor(2, 5000, 100); // max 2 workers in 5 seconds each at least 100ms apart
          burstableMonitorWorkerCreatings.passThrough();

          ServerInterfaceWorker worker = null;
          // roll through the valid hosts to attempt to create a connection
          int maxWorkerTrials = hostsAndPorts.length;
          for (int workerTrial=0; workerTrial<maxWorkerTrials; workerTrial++) {
            // Control throughput of trials as a safety mechanism for server connectivity
            if (burstableMonitorWorkerCreationTrials == null)
              burstableMonitorWorkerCreationTrials = new BurstableMonitor(10, 5000, 100); // max 10 trials in 5 seconds each at least 100ms apart
            burstableMonitorWorkerCreationTrials.passThrough();

            int[] hostIndexesToTry = null;
            int hostIndexCompletedFirst = -1;
            try {
              String hostToTry = (String) hostsAndPorts[currentHostIndex][0];

              // If encountered 'protocoled' host, simultaneously try other non-protocoled host too.
              int alternateHostIndex = -1;
              if (hostToTry.indexOf("://") >= 0) {
                // find next non-protocoled host/port
                if (trace != null) trace.data(20, "find next non-protocoled host/port");
                for (int k=0; k<maxWorkerTrials; k++) {
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
                for (int k=0; k<maxWorkerTrials; k++) {
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
              Throwable[][] errorBuffers = new Throwable[hostIndexesToTry.length][1];

              for (int k=0; k<hostIndexesToTry.length; k++) {
                ths[k] = createSocket_Threaded((String) hostsAndPorts[hostIndexesToTry[k]][0],
                                                ((Integer) hostsAndPorts[hostIndexesToTry[k]][1]).intValue(),
                                                socketBuffers[k], errorBuffers[k]);
              }
              // Find the first one joined
              // We are interested in first successful or first failure iff all failed.
              int joinedIndexFirst = -1;
              while (isAnyNonNULL(ths)) {
                int joinedIndex = joinAny(ths); // join the first available thread that has completed
                if (joinedIndexFirst == -1)
                  joinedIndexFirst = joinedIndex;
                if (errorBuffers[joinedIndex][0] != null) {
                  ths[joinedIndex] = null;
                } else {
                  joinedIndexFirst = joinedIndex;
                  break;
                }
              }

              hostIndexCompletedFirst = hostIndexesToTry[joinedIndexFirst];
              if (trace != null) trace.data(50, "hostIndexCompletedFirst", hostIndexCompletedFirst);

              if (errorBuffers[joinedIndexFirst][0] != null)
                throw errorBuffers[joinedIndexFirst][0];

              Socket socket = socketBuffers[joinedIndexFirst][0];
              if (trace != null) trace.data(60, "socket", socket);
              if (socket != null) {
                if (trace != null) trace.data(61, "socketType", socket.getClass());
              }

              // Clear other sockets that might have been created too
              // remove socket that we are using so it doesn't get cleaned up here
              socketBuffers[joinedIndexFirst][0] = null;
              Thread cleanupOtherSockets = new Thread("Cleanup Other Sockets") {
                public void run() {
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
                if (lastLoginMessageAction != null) {
                  if (trace != null) trace.data(30, "restamp the auto-login message");
                  lastLoginMessageAction = new MessageAction(lastLoginMessageAction.getActionCode(), lastLoginMessageAction.getMsgDataSet());
                } else {
                  if (trace != null) trace.data(31, "no auto-login message to restamp");
                }
                worker = new ServerInterfaceWorker(socket, this, this,
                                                   getReplyFifoWriterI(),
                                                   getRequestPriorityFifoReaderI(),
                                                   lastLoginMessageAction);
                if (trace != null) trace.data(40, "ServerInterfaceWorker instantiated");
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
              // increment host index for next trial
              currentHostIndex = (currentHostIndex + 1) % hostsAndPorts.length;
              if (trace != null) trace.exception(ServerInterfaceLayer.class, 70, t);
              String errMsg = "<html>Error occurred while trying to connect to the "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" Data Server at " + hostsAndPorts[hostIndexCompletedFirst][0] + " on port " + hostsAndPorts[hostIndexCompletedFirst][1] + ".  "
                + "Please verify your computer network and/or modem cables are plugged-in and your computer is currently connected to the Internet.  When you have established and verified your Internet connectivity, please try connecting to "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" again.  "
                + "If the problem persists please visit <a href=\""+URLs.get(URLs.CONNECTIVITY_PAGE)+"\">"+URLs.get(URLs.CONNECTIVITY_PAGE)+"</a> for help. <p>";
              // Only at the last round process the exception, else ignore and try another host.
              if (workerTrial+1 == maxWorkerTrials) {

                if (t instanceof UnknownHostException) {
                  throw new SILConnectionException(errMsg + "Specified host is unknown or cannot be resolved.  The error message is: <br>" + t.getMessage());
                }
                else if (t instanceof IOException) {
                  // TO-DO: decide if this is ok, analyze the case of 1 worker active (main worker)
                  // should this mean System.exit if we have ZERO workers???
                  // Maybe we should retry if we have at least the main worker!
                  // throw new error if we have less than 2 workers (1 main, 1 heavy)
                  if (countAllWorkers == 0) {
                    if (trace != null) trace.data(80, "Not enough workers to ignore connection problem!");
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
            workers.add(worker);
            // destroyed worker could be replaced by new one which should now be updating connection counts
            if (!destroyed) Stats.setConnections(workers.size(), getWorkerCounts());
          }
        } // end for
      } // end synchronized
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
  private Thread createSocket_Threaded(final String hostName, final int portNumber, final Socket[] socketBuffer, final Throwable[] errorBuffer) {
    Thread th = new ThreadTraced("Socket Creator") {
      public void runTraced() {
        try {
          Socket socket = createSocket(hostName, portNumber);
          socketBuffer[0] = socket;
        } catch (Throwable t) {
          errorBuffer[0] = t;
        }
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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "createSocket(String hostName, int portNumber, MessageAction loginMsgAction)");
    if (trace != null) trace.args(hostName);
    if (trace != null) trace.args(portNumber);

    Socket socket = null;

    if (!destroyed) {
      // Create the workers for serving connecitons
      // This could throw exceptions if connection to server is not established!
      final Socket[] socketBuffer = new Socket[1];
      final Exception[] exceptionBuffer = new Exception[1];

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
                } else {
                  if (trace != null) trace.data(21, "sleep DELAY_PROTOCOLED_CONNECTION", DELAY_PROTOCOLED_CONNECTION);
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
        socketConnector.join(12000);
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
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
    return socket;
  }

  private PriorityFifoReaderI getRequestPriorityFifoReaderI() {
    return jobFifo;
  }

  private FifoWriterI getReplyFifoWriterI() {
    return executionQueue.getFifoWriterI();
  }




  private boolean destroyed = false;
  public boolean isDestroyed() {
    return destroyed;
  }

  /** Destroys this object and invalidates its state releasing all resources. */
  public synchronized void destroyServer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "destroy()");

    if (destroyed) {
      if (trace != null) trace.exit(ServerInterfaceLayer.class);
      return;
    }
    destroyed = true;

    try {
      if (loginCompletionNotifier != null)
        loginCompletionNotifier.serverDestroyed(this);
    } catch (Throwable t) {
    }

    try {
      disconnectAndClear();
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
      executionQueue.getFifoWriterI().clear();
      executionQueue.kill();
      executionQueue = null;
    } catch (Throwable t) {
    }
    if (trace != null) trace.data(32, "execution queue killed");

    // kill the independentExecutionQueue
    if (trace != null) trace.data(33, "killing independent execution queue");
    try {
      independentExecutionQueue.getFifoWriterI().clear();
      independentExecutionQueue.kill();
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
      lastLoginMessageAction = null;
      try {
        submitAndWait(new MessageAction(CommandCodes.USR_Q_LOGOUT), 3000);
      } catch (Throwable t) {
      }
    }
    hostsAndPorts = null;
    try {
      logoutWorkers();
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
  public void logoutWorkers() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "logoutWorkers()");
    // kill all workers
    if (trace != null) trace.data(10, "killing all workers");

    synchronized (workers) {
      int size = workers.size();
      for (int i=0; i<size; i++) {
        ServerInterfaceWorker worker = (ServerInterfaceWorker) workers.get(i);
        try {
          worker.destroyWorker();
        } catch (Throwable t) {
        }
      }
      workers.clear();
      // destroyed worker could be replaced by new one which should now be updating connection counts
      if (!destroyed) Stats.setConnections(workers.size(), getWorkerCounts());
    }

    if (trace != null) trace.data(20, "workers killed");
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
   * Sets the last successful login action, called by workers after successful login.
   */
  public synchronized void setLoginMsgAction(MessageAction loginMsgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "setLoginMsgAction(MessageAction loginMsgAction)");
    if (trace != null) trace.args(loginMsgAction);
    lastLoginMessageAction = loginMsgAction;
    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }
  public synchronized boolean isLastLoginMsgActionSet() {
    return lastLoginMessageAction != null;
  }
  public boolean isLoggedIn() {
    boolean rc = false;
    if (isLastLoginMsgActionSet())
      if (getFetchedDataCache().getMyUserId() != null)
        rc = true;
    return rc;
  }
  public synchronized ProtocolMsgDataSet getLoginMsgDataSet() {
    if (lastLoginMessageAction != null) {
      ProtocolMsgDataSet tempDataSet = lastLoginMessageAction.getMsgDataSet();
      ProtocolMsgDataSet loginDataSet = null;
      if (tempDataSet instanceof Obj_EncSet_Co) {
        loginDataSet = ((Obj_EncSet_Co) tempDataSet).dataSet;
      } else {
        loginDataSet = tempDataSet;
      }
      return loginDataSet;
    }
    return null;
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

    boolean workerRemoved = false;

    synchronized (workers) {
      workerRemoved = workers.remove(worker);
      // destroyed worker could be replaced by new one which should now be updating connection counts
      if (!destroyed) Stats.setConnections(workers.size(), getWorkerCounts());
    }

    if (workerRemoved) {
      // penalize connection type if it broke and lasted for short time
      if (!suppressConnectionTypePenalization) {
        Class workerSocketType = worker.getSocketType();
        if (!cleanLogout && (System.currentTimeMillis()-worker.getSocketCreationStamp()) < 1000L * 60L * 3L) { // 3 minutes
          if (trace != null) trace.data(10, "assigning penalizedSocketType", workerSocketType);
          penalizedSocketType = workerSocketType;
        } else if (workerSocketType.equals(penalizedSocketType)) {
          // if clean logout or connection failure after long time of being alive, clear out penalized Socket type
          if (trace != null) trace.data(11, "clearing penalizedSocketType");
          penalizedSocketType = null;
        } else {
          if (trace != null) trace.data(12, "penalizedSocketType unchanged and equals", penalizedSocketType);
        }
      }

      // if not loging out, then maybe we need to create more workers if one died.
      if (!cleanLogout) {
        if (workers.size() == 0) {
          lastForcedWorkerStamp = new Date();
        }
      }
      // If a MAIN WORKER quits, we need to designate another main worker.
      synchronized (mainWorkerMonitor) {
        if (isMainWorker(worker)) {
          mainWorker = null;
          // Try to assign a main worker job to another worker.
          triggerCheckForMainWorker();
        }
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

    synchronized (mainWorkerMonitor) {
      if (trace != null) trace.data(10, worker != null ? "Claiming new main worker." : "Main worker won't be claimed.");
      if (mainWorker == null) {
        if (trace != null) trace.data(20, "Main worker submission cycle done.");
        mainWorkerSubmition = false;
        // since workers submitting NOTIFY requests claim Main Persistant status, this cycle cannot break when there is a comm failure
        mainWorker = worker;
      }
      // Allow multiple main workers because this can happen briefly when main worker connection is being recycled
//      else
//        throw new IllegalStateException("Main Worker already claimed!");
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }


  /**
   * Causes that a Main Worker is designated or a submition is in progress.
   * If necessary, submits a Message Action to start Main Worker designation.
   * That message upon being picked up by some worker will grant a Main Worker
   * status when claimMainWorker() is called by that worker.
   */
  private void triggerCheckForMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "triggerCheckForMainWorker()");

    if (!destroyed) {
      boolean submitNow = false;
      synchronized (mainWorkerMonitor) {
        if (mainWorker == null && mainWorkerSubmition == false) {
          mainWorkerSubmition = true;
          submitNow = true;
        }
      } // end synchronized

      if (trace != null) trace.data(10, "submitNow", submitNow);
      if (submitNow) {
        // Whichever worker picks it up, it should claim Main Worker and send it to the server to register for notifications.
        // Quietly add a NOTIFY message without having aditional workers created to serve it -- do not user submitAndReturn.
        // This message is only meant for already existing workers, otherwise we would have an infinite loop.
        MessageAction msgActionNotify = new MessageAction(CommandCodes.SYS_Q_NOTIFY);
        enqueueProgMonitor(msgActionNotify);
        jobFifo.addJob(msgActionNotify);
        jobScanner.triggerCheckToServeNow();
      }
    }

    if (trace != null) trace.exit(ServerInterfaceLayer.class);
  }

  /**
   * @return true if the specified worker is registered as the Main Worker.
   */
  public boolean isMainWorker(ServerInterfaceWorker worker) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "isMainWorker(ServerInterfaceWorker worker)");
    if (trace != null) trace.args(worker);

    boolean rc;
    synchronized (mainWorkerMonitor) {
      rc = worker != null && mainWorker == worker;
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
    boolean rc;
    synchronized (mainWorkerMonitor) {
      rc = mainWorker != null;
    }
    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  /**
   * @return true if there is a main worker which is persistant;
   * Main Worker becomes persistant when it retrieves a NOTIFY reply from the server allowing it to become persistant.
   */
  public boolean hasPersistantMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceLayer.class, "hasPersistantMainWorker()");
    ServerInterfaceWorker mWorker = mainWorker;
    boolean rc = mWorker != null ? mWorker.isPersistant() : false;
    if (trace != null) trace.exit(ServerInterfaceLayer.class, rc);
    return rc;
  }

  /**
   * @return maximum number of workers this manager can have
   */
  public int getMaxWorkerCount() {
    return getMaxConnectionCount();
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
      if (!destroyed) Stats.setConnections(workers.size(), getWorkerCounts());
    }
    // Notify of successful login.
    if (loginSuccessful && loginCompletionNotifier != null)
      loginCompletionNotifier.loginComplete(this);
    // Each successful login warants check for Main Worker... incase we are doing some kind of auto-re-login due to connection drop etc.
    if (loginSuccessful)
      triggerCheckForMainWorker();
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
    public void processQueuedObject(Object obj) {
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
            System.out.println("Waited for submitter prepared-ready state over 10s, escaping!");
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
            if (trace != null) trace.data(60, "no one waiting for this reply, run it.");
            // no one waiting for this reply, run it.
            // Running independent jobs by independent queue frees up the execution queue while
            // those jobs are being run, plus it ensures the FCFS order of independent jobs.
            if (!destroyed) {
              synchronized (ServerInterfaceLayer.this) {
                independentExecutionQueue.getFifoWriterI().add(nextMsgAction);
              }
            }
          }
        }
      } catch (Throwable t) {
        // execution of server reply went wrong -- critical error.
        if (trace != null) trace.exception(QueueExecutionFunction.class, 100, t);
      }

      if (trace != null) trace.exit(QueueExecutionFunction.class);
    } // end processQueuedObject
  } // end inner class QueueExecutionFunction



  private class WaitingJobsScanner extends ThreadTraced {
    private Object lastScanHeadJob = null;
    private boolean triggeredMonitor = false;
    private final Object triggerMonitor = new Object();
    public WaitingJobsScanner() {
      super("Waiting Jobs Scanner");
      setDaemon(true);
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "WaitingJobsScanner.runTraced()");

      int delay = 5000;
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
              }
              delay = 5000;
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
              if (hasPersistantMainWorker()) {
                ensureEnoughAllWorkersExist(forceAdditionalConnection);
                delay = 15000;
              }
            }
            lastScanHeadJob = headJob;
          } // end no immediate jobs
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
          // If client with no prior login encounters a connection exception, it has to exit.
          if (isClient && lastLoginMessageAction == null && t instanceof SILConnectionException) {
            destroyServer();
            NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "Error", t.getMessage());
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
        if (trace != null) trace.data(10, "notifying to serve now");
        triggeredMonitor = true;
        triggerMonitor.notifyAll();
      }
      if (trace != null) trace.exit(getClass());
    }
  } // end class BusyConnectionScanner

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