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

package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientActionSwitch;
import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.InterruptMessageAction;
import com.CH_cl.service.actions.sys.SysANullAction;
import com.CH_co.cryptx.RSAPublicKey;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.DefaultProgMonitor;
import com.CH_co.monitor.Interruptible;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.monitor.ProgMonitorPool;
import com.CH_co.queue.FifoWriterI;
import com.CH_co.queue.PriorityFifoReaderI;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.MessageActionNameSwitch;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.PingPong_Cm;
import com.CH_co.service.msg.dataSets.obj.Obj_EncSet_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.Usr_LoginSecSess_Rq;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_co.util.Misc;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details: Two inner classes: ReaderThread and WriteProcessor
*
*
* Wire
* | |         |----------------------|
* | |         | ClientSessionContext |
* | |         |----------------------|
* | |
* | |                _______
* | |              /        \                 ===| Reply
* | |------------->| Reader  |---------------->  |FifoWriterI
* | |              \________/                 ===|
* | |
* | |                _______
* | |              /        \                  |=== Request
* | |<-------------| Writer  |<----------------|    FifoReaderI
* | |              \________/                  |===
* | |
* | |
*
*
* <b>$Revision: 1.44 $</b>
* @author  Marcin Kurzawa
* @version
*/
public final class ServerInterfaceWorker extends Object implements Interruptible {

  private static final boolean DEBUG_ON__REQUEST_PACKET_DROP_ENABLED = false;
  private static final boolean DEBUG_ON__REPLY_PACKET_DROP_ENABLED = false;
  private static final int DEBUG__REQUEST_PACKET_DROP_FREQUENCY = 10;
  private static final int DEBUG__REPLY_PACKET_DROP_FREQUENCY = 10;

  public static final long TIMEOUT_TO_TRIGGER_RECONNECT_UPDATE = 1000 * 30; // 30 seconds
  public static final long PING_PONG_INTERVAL = 1000 * 60 * 1; // 1 minute
  private static final long PING_PONG_STREAK_COUNT_BEFORE_CONNECTION_BREAK = 1; // zero for no pinging and exit after first ping delay

  /** Worker's manager */
  private WorkerManagerI workerManager;
  /** Where all replies are submitted */
  private FifoWriterI replyFifoWriterI;
  /** Where all requests are taken from */
  private final PriorityFifoReaderI requestPriorityFifoReaderI;
  /** Holder of streams and general IO stuff */
  private ClientSessionContext sessionContext;
  /** Thread that constantly reads from the wire and converts bytes to messages and places them on replyFifoWriterI. */
  private ReaderThread reader;
  /** Thread that constantly writes messages to the wire as they become available. */
  private WriterThread writer;

  // All actions before they are sent, we hash them into a table so we can properly interrupt them
  // and release any waiting processes in an event of a failure in the writer or reader process.
  // The key is message stamp.
  private final HashMap outgoingInterruptableActionsHM;

  /** Login message action that should be used for login. */
  private MessageAction loginMsgAction;

  /** Temporary holder for login message attempts. */
  private MessageAction attemptLoginMessageAction;
  /** Flag which is set when Worker is quitting. */
  private boolean finished = false;
  private static int workerCount = 0;

  private int busyCount = 0;

  private boolean isPersistentWorker = false;

  /**
  * Monitor completion of HEAVY jobs so that requests for heavy jobs are sent one at a time and replies read
  * before the same worker can send a request for a heavy job.
  */
  private final Object readerDoneMonitor = new Object();

  private long creationTimestamp;

  /**
  * Creates new ServerInterfaceWorker
  * @param connectedSocket is the socket through which this client worker will communicate with the server.
  * @param workerManager notifications between other workers
  * @param replyFifoWriterI Where the replies should be placed.
  * @param requestPriorityFifoReaderI Where the requests for sending are taken from.
  * @param loginMsgAction Message to be used for login.
  */
  public ServerInterfaceWorker(Socket connectedSocket, WorkerManagerI workerManager,
        RequestSubmitterI requestSubmitter, FifoWriterI replyFifoWriterI,
        PriorityFifoReaderI requestPriorityFifoReaderI,
        MessageAction loginMsgAction) throws IOException
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "()");
    this.creationTimestamp = System.currentTimeMillis();
    if (workerManager.isClientMode())
      this.sessionContext = new ClientSessionContext(connectedSocket, requestSubmitter);
    else
      this.sessionContext = new ClientSessionContext(connectedSocket, requestSubmitter, false);
    this.workerManager = workerManager;
    this.replyFifoWriterI = replyFifoWriterI;
    this.requestPriorityFifoReaderI = requestPriorityFifoReaderI;
    this.loginMsgAction = loginMsgAction;

    this.outgoingInterruptableActionsHM = new HashMap();

    this.reader = new ReaderThread("Worker Reader " + workerCount);
    reader.setDaemon(true);
    reader.start();

    this.writer = new WriterThread("Worker Writer " + workerCount);
    writer.setDaemon(true);
    writer.start();

    workerCount ++;
    workerCount %= Integer.MAX_VALUE-1;

    // If manager was destroyed while we were connecting sockets or 
    // creating worker, then destroy this worker right away.
    if (workerManager.isDestroyed())
      destroyWorker();

    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }

  protected long calculateRate() {
    return sessionContext.calculateRate();
  }

  private void markPersistent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "markPersistent()");
    isPersistentWorker = true;
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }

  protected boolean isPersistent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "isPersistent()");
    boolean rc = isPersistentWorker;
    if (trace != null) trace.exit(ServerInterfaceWorker.class, rc);
    return rc;
  }

  protected long getSocketCreationStamp() {
    return creationTimestamp;
  }
  protected Class getSocketType() {
    return sessionContext.getSocket().getClass();
  }

  private void setBusy(boolean state) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "setBusy(boolean state)");
    if (trace != null) trace.args(state);
    busyCount += state ? 1 : -1;
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }

  public boolean isBusy() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "isBusy()");
    boolean rc = false;
    rc = busyCount > 0;
    if (trace != null) trace.exit(ServerInterfaceWorker.class, rc);
    return rc;
  }

  public boolean isMainWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "isMainWorker()");
    boolean rc = workerManager.isMainWorker(this);
    if (trace != null) trace.exit(ServerInterfaceWorker.class, rc);
    return rc;
  }

  private boolean finishedReading = false;
  private synchronized void finishReading(boolean cleanLogout) {
    finishReading(cleanLogout, false);
  }
  private synchronized void finishReading(boolean cleanLogout, boolean suppressConnectionTypePenalization) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "finishReading(boolean cleanLogout, boolean suppressConnectionTypePenalization)");
    if (trace != null) trace.args(cleanLogout);
    if (trace != null) trace.args(suppressConnectionTypePenalization);
    if (finishedReading == false) {
      finishedReading = true;
      finished = true;
      workerManager.workerDone(this, cleanLogout, suppressConnectionTypePenalization);
      // signal the writer that reader is quitting
      writer.interrupt();
      // if both READER and WRITER are done, destory the worker
      if (finishedWriting) {
        destroyWorker();
      }
    }
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }

  private boolean finishedWriting = false;
  private synchronized void finishWriting(boolean cleanLogout) {
    finishWriting(cleanLogout, false);
  }
  private synchronized void finishWriting(boolean cleanLogout, boolean suppressConnectionTypePenalization) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "finishWriting(boolean cleanLogout, boolean suppressConnectionTypePenalization)");
    if (trace != null) trace.args(cleanLogout);
    if (trace != null) trace.args(suppressConnectionTypePenalization);
    if (finishedWriting == false) {
      finishedWriting = true;
      finished = true;
      workerManager.workerDone(this, cleanLogout, suppressConnectionTypePenalization);
      // signal the reader that writer is quitting
      reader.interrupt();
      // if both READER and WRITER are done, destory the worker
      if (finishedReading) {
        destroyWorker();
      }
    }
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }


  /**
  * Interruptable interface method.
  */
  public void interrupt() {
    sessionContext.interrupt();
  }


  private boolean destroyed = false;
  public synchronized void destroyWorker() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "destroyWorker()");
    if (!destroyed) {
      destroyed = true;
      try { finishReading(false); } catch (Throwable t) {
        if (trace != null) trace.exception(ServerInterfaceWorker.class, 100, t);
      }
      try { finishWriting(false); } catch (Throwable t) {
        if (trace != null) trace.exception(ServerInterfaceWorker.class, 200, t);
      }
      try { sessionContext.closeCommunications(); } catch (Throwable t) {
        if (trace != null) trace.exception(ServerInterfaceWorker.class, 300, t);
      }
    }
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }

  protected void finalize() throws Throwable {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ServerInterfaceWorker.class, "finalize()");
    if (trace != null) trace.data(10, this);
    try {
      destroyWorker();
      attemptLoginMessageAction = null;
      replyFifoWriterI = null;
      reader = null;
      writer = null;
    } catch (Throwable t) {
      if (trace != null) trace.exception(ServerInterfaceWorker.class, 100, t);
    }
    super.finalize();
    if (trace != null) trace.exit(ServerInterfaceWorker.class);
  }



  //==========================================================================
  //==========================================================================

  private class ReaderThread extends ThreadTraced {
    private ReaderThread(String threadName) {
      super(threadName);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReaderThread.class, "ReaderThread(String threadName)");
      if (trace != null) trace.args(threadName);
      if (trace != null) trace.exit(ReaderThread.class);
    }

    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReaderThread.class, "ReaderThread.runTraced()");

      boolean cleanBreak = false;
      ClientMessageAction msgAction = null;
      int prevMsgActionCode = 0;
      int msgActionCode = 0;
      long msgActionStamp = 0;

      // re-login try
      try {
        // for all incoming messages
        while (!finished) {

          if (trace != null) trace.data(20, "ReaderThread entered while(!finished) loop.");

          // Reads the incoming request and converts it into an appropriate action object
          // filled with the Message received.  Also set the context for lifetime of that action.
          DataInputStream2 dataIn = sessionContext.getDataInputStream2();

          // read the start byte from stream so we know that something is coming our way
          try {
            msgActionCode = ClientMessageAction.readActionCodeFromStream(dataIn);
          } catch (Throwable t) {
            if (trace != null) trace.exception(ReaderThread.class, 30, t);
            throw new IllegalStateException("Error reading Action Code, prevMsgActionCode="+prevMsgActionCode, t);
          }

          // check the validity of the action code
          String name = ClientActionSwitch.switchCodeToActionName(msgActionCode);
          if (name == null) {
            throw new IllegalStateException("Action code is not valid, connection broken. msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode);
          }

          boolean suppressBusy = JobFifo.getJobType(msgActionCode) == JobFifo.JOB_TYPE_SEAMLESS;
          if (!suppressBusy) {
            setBusy(true);
          }

          synchronized (readerDoneMonitor) {
            msgActionStamp = ClientMessageAction.readActionStampFromStream(dataIn);
            // Set interruptable executor of this action
            ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(msgActionStamp);
            // Don't want dumping monitor here on the client side, server side would be ok.
            if (ProgMonitorPool.isDummy(progressMonitor) && !Misc.isAllGUIsuppressed()) {
              if (!workerManager.isDestroyed() && !workerManager.isDestroying()) {
                progressMonitor = new DefaultProgMonitor(false);
                ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionStamp);
              }
            }
            progressMonitor.setInterrupt(ServerInterfaceWorker.this);

            msgAction = ClientMessageAction.readActionFromStream(dataIn, sessionContext, msgActionCode, msgActionStamp);

            // response speed monitoring
            long startTime = msgAction.getStampTime();
            long endTime = System.currentTimeMillis();
            long ms = endTime-startTime;
            if (startTime != 0 && ms < 100*1000L && ms >= 0) { // sometimes stamps don't represent real time so this would be missleading
              if (trace != null) trace.data(50, "Round trip time for " + MessageActionNameSwitch.getActionInfoName(msgAction.getActionCode()) + " took " + (endTime-startTime)+" ms. ");
              //System.out.println(""+(endTime-startTime)+" ms. " + MessageActionNameSwitch.getActionInfoName(msgAction.getActionCode()));
            }

            progressMonitor.setCancellable(msgAction);

            // Since the reply has been read, there is no communication error and we can remove the interruptable message.
            synchronized (outgoingInterruptableActionsHM) {
              outgoingInterruptableActionsHM.remove(new Stamp(msgActionStamp));
            }

            // we must ignore idle restart here due to possibly incoming notifications which are treated as IDLE messages
            //synchronized (idleStartDateMonitor) {
            // idleStartDate = new Date();
            //}


            processIncomingMsgAction(msgAction, dataIn); // dataIn only for synchronization of LOGIN, not for reading data

            // Reset interruptable executor of this action
            progressMonitor.setInterrupt(null);

            // Release possibly waiting Writer for completion of reading a reply.
            // Some heavy jobs have NON-heavy replies, so release regardless of a reply type.
            readerDoneMonitor.notify();
          }


          if (!suppressBusy) {
            setBusy(false);
          }

          // If this was logout or connection timeout, do some cleanup
          if (msgActionCode == CommandCodes.USR_A_LOGOUT ||
              msgActionCode == CommandCodes.SYS_A_CONNECTION_TIMEOUT
            )
          {
            cleanBreak = true;
            break;
          }

          prevMsgActionCode = msgActionCode;
          msgAction = null;
          msgActionCode = 0;
          msgActionStamp = 0;
        } // end while

        // catch re-login try
      } catch (IOException ioX) {
        if (trace != null) trace.exception(ReaderThread.class, 100, ioX);
        if (!workerManager.isClientMode()) System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode+", exception:\n"+Misc.getStack(ioX));
        // These should never happen, if so -- coding error.
      } catch (IllegalAccessException aX) {
        if (trace != null) trace.exception(ReaderThread.class, 110, aX);
        if (!workerManager.isClientMode()) System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode+", exception:\n"+Misc.getStack(aX));
      } catch (InstantiationException iX) {
        if (trace != null) trace.exception(ReaderThread.class, 120, iX);
        if (!workerManager.isClientMode()) System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode+", exception:\n"+Misc.getStack(iX));
      } catch (ClassNotFoundException cX) {
        if (trace != null) trace.exception(ReaderThread.class, 130, cX);
        if (!workerManager.isClientMode()) System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode+", exception:\n"+Misc.getStack(cX));
        // Finally catch all throwable runtime exceptions.
      } catch (Throwable t) {
        if (trace != null) trace.exception(ReaderThread.class, 200, t);
        if (!workerManager.isClientMode()) System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode+", exception:\n"+Misc.getStack(t));
      }

      if (msgActionCode != 0) {
        ProgMonitorI pm = ProgMonitorPool.getProgMonitor(msgActionStamp);
        if (pm != null)
          pm.jobKilled();
      }

      if (cleanBreak) {
        finishReading(msgAction.getActionCode() == CommandCodes.USR_A_LOGOUT, msgAction.getActionCode() == CommandCodes.SYS_A_CONNECTION_TIMEOUT);
      } else {
        destroyWorker();

        // We need to wake-up any submitting threads should this transaction die.
        // All interruptable actions which did not see a reply must be processed as interrupted.
        synchronized (outgoingInterruptableActionsHM) {
          try {
            Iterator iter = outgoingInterruptableActionsHM.values().iterator();
            while (iter.hasNext()) {
              MessageAction mAction = (MessageAction) iter.next();
              InterruptMessageAction interruptAction = new InterruptMessageAction(mAction);
              interruptAction.setClientContext(sessionContext);
              if (!workerManager.isDestroyed()) // prevent adding interrupt jobs if we were destroyed and are cleaning up...
                replyFifoWriterI.add(interruptAction);
            }
          } catch (Throwable t) {
            // catch all to allow the rest of the cleanup
          }
          outgoingInterruptableActionsHM.clear();
        }
      }

      // Release worker incase exceptions caused the previous release to be skipped.
      synchronized (readerDoneMonitor) {
        readerDoneMonitor.notify();
      }

      if (trace != null) trace.data(300, "SIL Reader exits, server-mode="+(!workerManager.isClientMode())+", cleanBreak="+cleanBreak+", msgActionCode="+msgActionCode+", prevMsgActionCode="+prevMsgActionCode);
      if (!cleanBreak && !workerManager.isClientMode()) {
        System.out.println("SIL Reader server-mode " +sessionContext.getSocketHostPort()+ " exit! cleanBreak=" + cleanBreak + ", msgActionCode=" + msgActionCode+", prevMsgActionCode="+prevMsgActionCode);
      }

      if (trace != null) trace.exit(ReaderThread.class);
    } // end run


    private void processIncomingMsgAction(ClientMessageAction msgAction, DataInputStream2 dataIn) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReaderThread.class, "processIncomingMsgAction(ClientMessageAction msgAction)");

      int msgActionCode = msgAction.getActionCode() ;

      if (msgActionCode == CommandCodes.SYS_A_NOTIFY) {
        markPersistent();
      }

      // monitor last activity Stamp -- skip null actions that maybe caused by connection errors
      if (!msgAction.getClass().equals(SysANullAction.class))
        workerManager.markLastWorkerActivityStamp();

      // All synchronized request-reply actions must go here.
      // If streams are about to change due to securing streams, then wait.
      // if Login action then wait till the streams are secured or waiting threads released, else no one will notify us hence wait would be forever!
      if (msgActionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION ||
          msgActionCode == CommandCodes.USR_E_HANDLE_PASSWORD_COMBO_DNE ||
          msgActionCode == CommandCodes.USR_E_USER_LOCKED_OUT ||
          msgActionCode == CommandCodes.USR_E_LOGIN_FAILED ||
          msgActionCode == CommandCodes.SYSNET_A_LOGIN ||
          msgActionCode == CommandCodes.SYSNET_A_LOGIN_FAILED ||
          msgActionCode == CommandCodes.SYS_A_LOGIN ||
          msgActionCode == CommandCodes.SYS_E_LOGIN ||
          msgActionCode == CommandCodes.SYS_A_VERSION ||
          msgActionCode == CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE)
      {

        if (trace != null) trace.data(30, "LOGIN SEQUENCE");
        if (trace != null) trace.data(32, "msgActionCode", msgActionCode);
        boolean loginSuccessful = msgActionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION ||
                                  msgActionCode == CommandCodes.SYSNET_A_LOGIN ||
                                  msgActionCode == CommandCodes.SYS_A_LOGIN;

        // If login was successful, then remember the login MessageAction.
        // We can do that because the writer is blocked until the streames become secured or they are unblocked by an login error action.
        if (loginSuccessful) {
          if (trace != null) trace.data(40, "Login Successful, got ", msgActionCode);
          if (trace != null) trace.data(41, "Remembering the last login action for re-login purposes.");

          // Clear the need to send private key for subsequent logins;
          // this minimizes overhead for other loging as the key is already in the cache.
          ProtocolMsgDataSet dataSet = attemptLoginMessageAction.getMsgDataSet();
          if (dataSet instanceof Usr_LoginSecSess_Rq) {
            Usr_LoginSecSess_Rq request = (Usr_LoginSecSess_Rq) dataSet;
            request.sendPrivKey = false;
          }
          workerManager.setLoginMsgAction(attemptLoginMessageAction);

          loginMsgAction = attemptLoginMessageAction;
          if (trace != null) trace.data(42, "attemptLoginMessageAction=" + attemptLoginMessageAction);
          // set the remote session ID if its a remote engine login
          if (msgActionCode == CommandCodes.SYSNET_A_LOGIN) {
            workerManager.setRemoteSessionID((Long) ((Obj_List_Co)msgAction.getMsgDataSet()).objs[3]);
          }
        }
        synchronized (dataIn) {
          // Write the incoming Message Action to the queue.
          replyFifoWriterI.add(msgAction);
          // case of requested "Recycle" running the action does nothing, recycle the reader/writer here
          if (msgActionCode == CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE) {
            // If disconnecting a NOTIFY session, then request another one before old one disconnects
            if (isMainWorker())
              workerManager.submitAndReturn(new MessageAction(CommandCodes.SYS_Q_NOTIFY));
            // Give a change for user to complete login and claim another main session (if required)
            // with another Worker to prevent "puk-puk-puk" for its contacts
            try { Thread.sleep(10000); } catch (InterruptedException x) { }
            finishReading(false);
            sessionContext.releaseLoginStreamers();
          }
          if (msgActionCode != CommandCodes.USR_A_RECYCLE_SESSION_SEQUENCE) {
            try {
              if (trace != null) trace.data(50, "Wait till stream is secured or threads released.");
              dataIn.wait(20000);
            } catch (InterruptedException e) {
            }
            if (trace != null) trace.data(55, "Woke up from waiting for secured streams or unlock upon login failure.");
            workerManager.workerLoginComplete(ServerInterfaceWorker.this, loginSuccessful);
          }
        } // end synchronized
      } // end if LOGIN aciton or LOGIN error
      else {
        if (trace != null) trace.data(60, "Not a LOGIN related action, just submit the message.");
        if (DEBUG_ON__REPLY_PACKET_DROP_ENABLED && new Random().nextInt(DEBUG__REPLY_PACKET_DROP_FREQUENCY) == 0) {
          System.out.println("reply dropped");
        } else {
          replyFifoWriterI.add(msgAction);
        }
      }

      if (trace != null) trace.exit(ReaderThread.class);
    } // end processIncomingMsgAction()

  } // end inner class ReaderThread

  //==========================================================================
  //==========================================================================

  private class WriterThread extends ThreadTraced {

    private WriterThread(String threadName) {
      super(threadName);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WriterThread.class, "WriterThread(String threadName)");
      if (trace != null) trace.args(threadName);
      if (trace != null) trace.exit(WriterThread.class);
    }

    private MessageAction nextMessage() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WriterThread.class, "nextMessage()");

      MessageAction msgAction = null;

      if (requestPriorityFifoReaderI.size() > 0) {
        // NOTE: highest priority is 0
        if (isMainWorker() && workerManager.getMaxHeavyWorkerCount() > 0) {
          msgAction = (MessageAction) requestPriorityFifoReaderI.remove(JobFifo.MAIN_WORKER_REAL_TIME_PRIORITY, JobFifo.MAIN_WORKER_LOW_PRIORITY);
        } else {
          msgAction = (MessageAction) requestPriorityFifoReaderI.remove();
        }
      }

      if (DEBUG_ON__REQUEST_PACKET_DROP_ENABLED && new Random().nextInt(DEBUG__REQUEST_PACKET_DROP_FREQUENCY) == 0) {
        System.out.println("request dropped");
        msgAction = null;
      }

      if (msgAction != null) {
        long msgActionStamp = msgAction.getStamp();
        int msgActionCode = msgAction.getActionCode();
        ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(msgActionStamp);
        if (ProgMonitorPool.isDummy(progressMonitor) && !Misc.isAllGUIsuppressed()) {
          if (!workerManager.isDestroyed() && !workerManager.isDestroying()) {
            progressMonitor = new DefaultProgMonitor(!DefaultProgMonitor.isSuppressProgressDialog(msgActionCode));
            ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionStamp);
          }
        }
        progressMonitor.dequeue(msgActionCode, msgActionStamp);
      }

      if (trace != null) trace.exit(WriterThread.class, msgAction);
      return msgAction;
    }


    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WriterThread.class, "WriterThread.runTraced()");

      boolean cleanLogout = false;
      MessageAction msgAction = loginMsgAction;
      int pingPongStreakCount = 0;

      try {
        // for all outgoing messages
        while (!finished) {

          int round = 0;
          long idleStartDate = System.currentTimeMillis();

          // grab next message action from the queue
          while (msgAction == null && !finished) {

            round ++;

            synchronized (requestPriorityFifoReaderI) {

              msgAction = nextMessage();

              // if we grabbed something we don't like.. put it back;
              if (msgAction != null) {
                if (trace != null) trace.data(10, "message is available");
                // if we pick up a NOTIFY request, but no login, dump the NOTIFY
                if (loginMsgAction == null && msgAction.getActionCode() == CommandCodes.SYS_Q_NOTIFY) {
                  if (trace != null) trace.data(11, "Dumping NOTIFY message, no prior login!");
                  // can't do NOTIFY without login, just dump it, another one will come soon.
                  ProgMonitorI pm = ProgMonitorPool.getProgMonitor(msgAction.getStamp());
                  if (pm != null)
                    pm.jobKilled();
                  workerManager.claimMainWorker(null);
                  msgAction = null;
                } else {
                  if (trace != null) trace.data(12, "to send message with code ", msgAction.getActionCode());
                }
              }
              // if we have nothing to act-on, we will wait.
              else {
                if (trace != null) trace.data(20, "no message available, lets wait");

                // Check if we need to wait before doing ping-pong.
                long currentDate = System.currentTimeMillis();
                if (currentDate - idleStartDate < PING_PONG_INTERVAL) {
                  if (trace != null) trace.data(25, "wait for requests in the queue");
                  // we must idle waiting for requests in the queue
                  try {
                    // calculate the remainder of time to the ping-pong interval
                    long pingPongRemainder = PING_PONG_INTERVAL - (currentDate - idleStartDate);
                    pingPongRemainder = Math.max(1L, pingPongRemainder); // always positive
                    pingPongRemainder = Math.min(PING_PONG_INTERVAL, pingPongRemainder); // PING_PONG_INTERVAL is maximum incase clock changed
                    requestPriorityFifoReaderI.wait(pingPongRemainder + 100); // 100ms extra
                  } catch (InterruptedException e) { }
                }

                // If the computer was suspended, the sleep would be much longer than asked for... 
                // Check time passed to see if we need to ping-pong again to keep connection alive, 
                // or to recheck the condition of our connection after extended sleeps.
                currentDate = System.currentTimeMillis();
                if (currentDate - idleStartDate >= PING_PONG_INTERVAL) {
                  if (trace != null) trace.data(21, "already waiting a while, ping-pong interval reached");
                  if (isMainWorker() || pingPongStreakCount < PING_PONG_STREAK_COUNT_BEFORE_CONNECTION_BREAK) {
                    msgAction = new MessageAction(CommandCodes.SYS_Q_PING, new PingPong_Cm());
                  } else {
                    // not main workers after sometime should quit, cleanly
                    if (!workerManager.isClientMode()) System.out.println("SIL Writer server-mode " +sessionContext.getSocketHostPort()+ " chose not to send ping!  Finishing! isMainWorker()=" + isMainWorker() + ", pingPongStreakCount=" + pingPongStreakCount);
                    finished = true;
                    cleanLogout = true;
                  }
                }
              }

              // count consecutive ping-pongs
              if (msgAction != null) {
                if (msgAction.getActionCode() == CommandCodes.SYS_Q_PING) {
                  pingPongStreakCount ++;
                } else {
                  pingPongStreakCount = 0;
                }
              }

            } // end synchronized
          } // end while

          if (trace != null) trace.data(30, "end while, finished = ", finished);

          // if interrupted then we are done.
          if (finished) {
            if (trace != null) trace.data(31, "finished so break");
            break;
          }

          long msgActionStamp = msgAction.getStamp();
          int msgActionCode = msgAction.getActionCode();


          boolean suppressBusy = JobFifo.getJobType(msgActionCode) == JobFifo.JOB_TYPE_SEAMLESS;
          if (!suppressBusy) {
            setBusy(true);
          }

          ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(msgActionStamp);
          // if no progress monitor, assign a default one... this case could be useful for just created PING request
          if (ProgMonitorPool.isDummy(progressMonitor) && !Misc.isAllGUIsuppressed()) {
            if (!workerManager.isDestroyed() && !workerManager.isDestroying()) {
              progressMonitor = new DefaultProgMonitor(!DefaultProgMonitor.isSuppressProgressDialog(msgActionCode));
              ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionStamp);
            }
          }

          // Set interruptable executor of this action
          progressMonitor.setInterrupt(ServerInterfaceWorker.this);
          // Set cancellable action (connecting interrupt with action)
          progressMonitor.setCancellable(msgAction);

          // Remember every action that is sent, so when worker dies, all actions can be properly interrupted.
          synchronized (outgoingInterruptableActionsHM) {
            outgoingInterruptableActionsHM.put(new Stamp(msgActionStamp), msgAction);
          }

          if (JobFifo.getJobType(msgAction) == JobFifo.JOB_TYPE_HEAVY ||
              JobFifo.isJobComputationallyIntensive(msgActionCode)
              // No need to synchronize on Login since this is done in
              // processOutgoingMsgAction() and would cause a deadlock here.
              )
          {
            synchronized (readerDoneMonitor) {
              processOutgoingMsgAction(msgAction);
              // wait for reader to finish processing reply before we can continue to the next request
              try {
                readerDoneMonitor.wait();
              } catch (InterruptedException e) {
                if (trace != null) trace.data(80, "Reader thread has woken up the writer...");
              }
            }
          }
          else {
            processOutgoingMsgAction(msgAction);
          }


          if (!suppressBusy) {
            setBusy(false);
          }

          // Reset interruptable executor of this action
          progressMonitor.setInterrupt(null);

          // If this was logout, do some cleanup
          if (msgActionCode == CommandCodes.USR_Q_LOGOUT) {
            cleanLogout = true;
            break;
          }

          msgAction = null;
        } // end while (!finished)


      // If there was a communication problem why the request was not send, try
      // pushing it back to the queue for resend.
      } catch (SocketException e) {
        if (trace != null) trace.exception(WriterThread.class, 100, e);
        if (!workerManager.isClientMode()) System.out.println("SIL Writer server-mode " +sessionContext.getSocketHostPort()+ " exception:\n"+Misc.getStack(e));
      } catch (IOException e) {
        if (trace != null) trace.exception(WriterThread.class, 110, e);
        if (!workerManager.isClientMode()) System.out.println("SIL Writer server-mode " +sessionContext.getSocketHostPort()+ " exception:\n"+Misc.getStack(e));
      } catch (Throwable t) {
        if (trace != null) trace.exception(WriterThread.class, 120, t);
        if (!workerManager.isClientMode()) System.out.println("SIL Writer server-mode " +sessionContext.getSocketHostPort()+ " exception:\n"+Misc.getStack(t));
      }

      try {
        if (!cleanLogout && msgAction != null) {
          if (!msgAction.isCancelled()) {
            if (JobFifo.isJobForRetry(msgAction)) {
              // Get job's progress monitor
              ProgMonitorI pm = ProgMonitorPool.getProgMonitor(msgAction.getStamp());
              // If PM was not cancelled (ie: got reply of storage exceeded)
              // PM may be null if it was already destroyed.
              if (pm == null || (!pm.isCancelled() && !pm.isJobKilled())) {
                msgAction.markSendTry();
                workerManager.pushbackRequest(msgAction);

                if (pm != null)
                  pm.jobForRetry();
                // Since we will retry this action, it is not yet done and should not be interrupted.
                synchronized (outgoingInterruptableActionsHM) {
                  outgoingInterruptableActionsHM.remove(new Stamp(msgAction.getStamp()));
                }
                // Don't allow it to be killed so pretend nothing happend and it will be retried.
                msgAction = null;
              } // end if PM not cancelled
            }
          }
        }
      } catch (Throwable t) {
        // catch all to allow the rest of the cleanup to continue
        t.printStackTrace();
      }

      // Wait a little for the pipes to clear so that all incoming messages (if any)
      // get a chance to be read.
      try {
        Thread.sleep(2000);
      } catch (InterruptedException x) { }

      // If not pushedback, then kill it, pushing back would set it to null...
      if (msgAction != null) {
        ProgMonitorI pm = ProgMonitorPool.getProgMonitor(msgAction.getStamp());
        if (pm != null)
          pm.jobKilled();
      }

      if (cleanLogout)
        finishWriting(cleanLogout);
      else {
        destroyWorker();
        // We need to wake-up the submitting threads should this worker die.
        // All not completed actions should be entered for reply-processing as interrupted actions.
        // Reader thread completes the actions by taking them out of the hash table by their stamp.
        // Those action cannot be retried because we don't know if they failed or not, we sent them but no replies yet.
        synchronized (outgoingInterruptableActionsHM) {
          try {
            Iterator iter = outgoingInterruptableActionsHM.values().iterator();
            while (iter.hasNext()) {
              MessageAction mAction = (MessageAction) iter.next();
              replyFifoWriterI.add(new InterruptMessageAction(mAction));
            }
          } catch (Throwable t) {
            // catch all to allow the rest of the cleanup to continue
            t.printStackTrace();
          }
          outgoingInterruptableActionsHM.clear();
        }
      }

      if (trace != null) trace.data(300, "SIL Writer exits, server-mode="+(!workerManager.isClientMode())+", cleanLogout="+cleanLogout);
      if (!cleanLogout && !workerManager.isClientMode()) {
        System.out.println("SIL Writer server-mode " +sessionContext.getSocketHostPort()+ " exit! cleanLogout=" + cleanLogout);
      }

      if (trace != null) trace.exit(WriterThread.class);
    } // end run;


    /** Start processing next object from the queue */
    public void processOutgoingMsgAction(MessageAction msgAction) throws IOException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WriterThread.class, "processOutgoingMsgAction(MessageAction msgAction)");
      if (trace != null) trace.args(msgAction);

      DataOutputStream2 dataOut = sessionContext.getDataOutputStream2();

      int msgActionCode = msgAction.getActionCode();

      // All synchronized request-reply actions must go here.
      // If streams are about to change due to securing streams, then wait
      // if Login action then wait till the streams are secured or waiting threads released, else no one will notify us hence wait would be forever!
      // if this is a login action, remember it for the future.
      if (msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION ||
          msgActionCode == CommandCodes.SYSENG_Q_LOGIN ||
          msgActionCode == CommandCodes.SYS_Q_LOGIN ||
          msgActionCode == CommandCodes.SYS_Q_VERSION ||
          msgActionCode == CommandCodes.USR_Q_LOGOUT ||
          msgActionCode == CommandCodes.USR_Q_RECYCLE_SESSION_SEQUENCE)
      {
        if (msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION) {
          if (trace != null) trace.data(10, "USR_Q_LOGIN_SECURE_SESSION");
        } else if (msgActionCode == CommandCodes.SYSENG_Q_LOGIN) {
          if (trace != null) trace.data(11, "SYSENG_Q_LOGIN");
        } else if (msgActionCode == CommandCodes.SYS_Q_LOGIN) {
          if (trace != null) trace.data(12, "SYS_Q_LOGIN");
        } else if (msgActionCode == CommandCodes.SYS_Q_VERSION) {
          if (trace != null) trace.data(13, "SYS_Q_VERSION");
        } else if (msgActionCode == CommandCodes.USR_Q_RECYCLE_SESSION_SEQUENCE) {
          if (trace != null) trace.data(14, "USR_Q_RECYCLE_SESSION_SEQUENCE");
        }

        if (msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION ||
            msgActionCode == CommandCodes.SYS_Q_LOGIN) {
          if (trace != null) trace.data(21, "Synchronizing key fetch...");
          synchronized (dataOut) {
            // This key only used to conceal public key when sent from server to client...
            // key is already public so it doesn't matter much but at least login can be anonimized against sniffers
            // which could otherwise examine the public key to see who is logging in... keep in mind that successful request of public key
            // does not equal successful login (or link the key to any particular user) because user may have bad password
            // which could generate identical partial hash so public key is dispensed but login fails due to incorrect password.
            sessionContext.generateKeyPairIfDoesntExist(512);
            Obj_List_Co dataSet = new Obj_List_Co(new Object[] { sessionContext.getKeyPairToReceiveWith().getPublicKey().objectToBytes() });
            new MessageAction(CommandCodes.SYS_Q_GET_TEMP_PUB_KEY, dataSet).writeToStream(dataOut, GlobalProperties.PROGRAM_BUILD_NUMBER, sessionContext.serverBuild);
            long waitStart = System.currentTimeMillis();
            try {
              // When login is successful or fails with UsrALoginFailed, thread will be unblocked immediately.
              // Login request may unexpectadly fail with general IO error, or succeed, lets keep a timeout.
              if (trace != null) trace.data(22, "Wait till key is received or threads released.");
              dataOut.flush(); // flush the synchronized requests to make sure they go out ASAP
              dataOut.wait(25000); // wait for 25 sec MAXIMUM
            } catch (InterruptedException e) {
              if (trace != null) trace.data(23, "We got an Interrupt Exception -- this is OK, quit waiting now.");
            }
            long waitEnd = System.currentTimeMillis();
            long waitDiff = waitEnd - waitStart;
            if (trace != null) trace.data(24, "Woke up from waiting for key or unlock upon recipt. Wait time was "+waitDiff+" ms and key for sending is "+sessionContext.getPublicKeyToSendWith());
          } // end synchronized
          if (trace != null) trace.data(25, "Synchronizing key fetch... done.");
        }

        if (msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION ||
            msgActionCode == CommandCodes.SYSENG_Q_LOGIN ||
            msgActionCode == CommandCodes.SYS_Q_LOGIN)
        {
          attemptLoginMessageAction = msgAction;
        }

        synchronized (dataOut) {
          if (msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION ||
              msgActionCode == CommandCodes.SYS_Q_LOGIN)
          {
            ProtocolMsgDataSet tempDataSet = msgAction.getMsgDataSet();
            ProtocolMsgDataSet loginDataSet = null;
            Obj_EncSet_Co encDataSet = null;
            if (tempDataSet instanceof Obj_EncSet_Co) {
              loginDataSet = ((Obj_EncSet_Co) tempDataSet).dataSet;
              encDataSet = (Obj_EncSet_Co) tempDataSet;
            } else {
              loginDataSet = tempDataSet;
              encDataSet = new Obj_EncSet_Co(loginDataSet);
            }
            synchronized (loginDataSet) {
              // set the session ID
              if (loginDataSet instanceof Usr_LoginSecSess_Rq)
                ((Usr_LoginSecSess_Rq) loginDataSet).sessionId = ClientSessionContext.SESSION_ID;
              // make sure the current session context key is used for this login and not the key from another session!
              RSAPublicKey publicKeyToSendWith = sessionContext.getPublicKeyToSendWith();
              if (publicKeyToSendWith != null && encDataSet != null) {
                if (trace != null) trace.data(27, "will write request login set as enc");
                msgAction.setMsgDataSet(encDataSet);
                encDataSet.setPublicKeyToSendWith(publicKeyToSendWith);
              } else {
                if (trace != null) trace.data(28, "will write request login set as plain");
                msgAction.setMsgDataSet(loginDataSet);
              }
              msgAction.writeToStream(dataOut, GlobalProperties.PROGRAM_BUILD_NUMBER, sessionContext.serverBuild);
            }
          } else {
            msgAction.writeToStream(dataOut, GlobalProperties.PROGRAM_BUILD_NUMBER, sessionContext.serverBuild);
          }
          try {
            // When login is successful or fails with UsrALoginFailed, thread will be unblocked immediately.
            // Login request may unexpectadly fail with general IO error, or succeed, lets keep a timeout.
            if (trace != null) trace.data(30, "Wait till stream is secured or threads released.");
            dataOut.flush(); // flush the synchronized requests to make sure they go out ASAP
            dataOut.wait(120000); // wait for 2 minutes MAXIMUM
          } catch (InterruptedException e) {
            if (trace != null) trace.data(45, "We got an Interrupt Exception -- this is OK, quit waiting now.");
          }
          if (trace != null) trace.data(50, "Woke up from waiting for secured streams or unlock upon login failure.");
        } // end synchronized

      } // end if synchronized request-reply action

      else {
        if (msgActionCode == CommandCodes.SYS_Q_NOTIFY) {
          workerManager.claimMainWorker(ServerInterfaceWorker.this);
          Thread.currentThread().setName(Thread.currentThread().getName() + " (Main Worker) ");
        }

        // If not a LOGIN action, just write out the message, no streams will change due to this action.
        msgAction.writeToStream(dataOut, sessionContext.clientBuild, sessionContext.serverBuild);
      }

      if (trace != null) trace.exit(WriterThread.class);
    } // end processOutgoingMsgAction()

  } // end inner class WriterThread,

} // end class ServerInterfaceWorker