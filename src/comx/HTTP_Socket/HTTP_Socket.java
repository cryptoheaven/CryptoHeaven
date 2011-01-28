/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.HTTP_Socket;

import com.CH_co.trace.*;
import comx.HTTP_Common.*;

import java.io.*;
import java.net.*;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class HTTP_Socket extends Socket {

  private static Random rnd = new Random();
  private static boolean DEBUG_ON__INSERT_RANDOM_ERRORS = false;
  private static boolean DEBUG_ON__DROP_PACKETS = false;
  private static int DEBUG_INSERT_RANDOM_ERROR_FREQUENCY = 15;
  private static int DEBUG_SOCKET_CREATE_SUCCESS_RATE = 3;
  private static int DEBUG_DROP_PACKETS_FREQUENCY = 50;

  private static boolean ENABLE_SOCKET_CONNECTIONS = true;
  private static boolean ENABLE_HTTP_CONNECTIONS = true;
  private static int MIN_TIME_SOCKET_REMAKE_DELAY = 2000;
  private static int MAX_TIME_SOCKET_REMAKE_DELAY = 4000;

  private static int CONNECTION_HTTP_TIMEOUT = 15000;
  private static int CONNECTION_SOCKET_TIMEOUT = 10000;

  private static int MAX_SEND_TRIES = 30;
  private static int TIME_DELAY_AFTER_FAILED_SEND_TRY = 1000;

  private static int MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP = 50;
  private static int MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET = 1000;
  private static int MAX_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP = 10000;
  private static int MAX_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET = 15000;
  private static int TIME_TO_WAIT_WHEN_BACKLOG = 1000;
  private static double TIME_TO_WAIT_POWER = 1.25;
  private static int NUMBER_OF_CONCURRENT_COMMUNICATION_THREADS = 2;

  private static int MAX_URL_DATA_SIZE = 2*1024;
  private static int MAX_POST_DATA_SIZE = 32*1024;

  // file extensions to be used as random draw for GET and POST requests
  private static String[] fileExts = new String[] { "asp", "jsp", "php", "cgi", "pl" };

  String proxyHost;
  int proxyPort;

  String host;
  int port;

  // create send and receive buffers
  final SequenceFifo recvFifo = new SequenceFifo(); // list of DataSets in order by sequenceId
  final OrderedFifo sendFifo = new OrderedFifo(); // list of DataSets
  final DataSetCache sentCacheFifo = new DataSetCache();

  final Object connectionIdMonitor = new Object();
  int connectionId = -1;
  long sendSequenceId = -1;

  boolean connected;
  boolean closing;
  boolean closed;

  InputStream recvPipeIn_Public;
  OutputStream recvPipeOut; // <--- We will write proxy responses here

  InputStream sendPipeIn; // >--- We will read client requests from here
  OutputStream sendPipeOut_Public;

  long lastSendTimestamp;
  long lastPingMadeStamp;
  int timeToWait = 100;

  final Object socketMonitor = new Object();
  Socket socket;
  DataInputStream socketInput;
  DataOutputStream socketOutput;

  /** Creates a new instance of HTTSocket */
  public HTTP_Socket(String proxyHost, int proxyPort, String host, int port) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "HTTP_Socket(String proxyHost, int proxyPort, String host, int port)");
    if (trace != null) trace.args(proxyHost);
    if (trace != null) trace.args(proxyPort);
    if (trace != null) trace.args(host);
    if (trace != null) trace.args(port);

    if (!ENABLE_SOCKET_CONNECTIONS)
      System.out.println("Warning: HTTP_Socket has plain connectivity disabled.");

    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.host = host;
    this.port = port;
    connect();

    if (trace != null) trace.exit(HTTP_Socket.class);
  }

  private void dumpSocket() {
    synchronized (socketMonitor) {
      if (socket != null) {
        try { socketInput.close(); } catch (Throwable x) { }
        try { socketOutput.close(); } catch (Throwable x) { }
        try { socket.close(); } catch (Throwable x) { }
        socket = null;
        socketInput = null;
        socketOutput = null;
      }
    }
  }

  private void makeSocket(final String proxyHost, final int proxyPort, long timeout) {
    boolean makeOk = ENABLE_SOCKET_CONNECTIONS && (!DEBUG_ON__INSERT_RANDOM_ERRORS || rnd.nextInt(DEBUG_SOCKET_CREATE_SUCCESS_RATE) == 0);
    if (!makeOk) {
    } else
    synchronized (socketMonitor) {
      dumpSocket();
      final Socket[] socketReturnBuf = new Socket[1];
      final boolean[] socketTimeout = new boolean[1];
      Thread th = new ThreadTraced("Socket Creator") {
        public void runTraced() {
          try {
            //System.out.print("Making socket to " + proxyHost + ":" + proxyPort + "... ");
            Socket s = new Socket(proxyHost, proxyPort);
            //System.out.print(" made.");
            synchronized (socketReturnBuf) {
              if (socketTimeout[0]) {
                s.close();
              } else {
                socketReturnBuf[0] = s;
              }
            }
          } catch (Throwable t) {
          }
        }
      };
      th.setDaemon(true);
      th.start();
      //System.out.print("Joining... ");
      try { th.join(timeout); } catch (Throwable t) { }
      //System.out.print(" joined.");
      synchronized (socketReturnBuf) {
        socketTimeout[0] = true;
        socket = socketReturnBuf[0];
        try {
          socketInput = new DataInputStream(socket.getInputStream());
          socketOutput = new DataOutputStream(socket.getOutputStream());
        } catch (Throwable t) {
          dumpSocket();
        }
      }
//      if (socket != null) System.out.println("Socket MADE");
//      else System.out.println("Socket DNE");
    }
  }

  private void connect() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "connect()");

    if (!connected) {
      // create communication pipes
      getInputStream();
      getOutputStream();

      synchronized (sendFifo) {
        sendSequenceId ++;
        DataSet requestDS = new DataSet(host, port);
        sendFifo.add(requestDS, sendSequenceId);
      }

      Thread th = null;

      if (ENABLE_SOCKET_CONNECTIONS) {
        // Try to make a direct socket....
        makeSocket(proxyHost, proxyPort, CONNECTION_SOCKET_TIMEOUT);
        // make the asynchronous SOCKET sending and reciving threads
        th = new ThreadTraced(new Sending(), "HTTP_Socket-Sending");
        th.setDaemon(true);
        th.start();
        th = new ThreadTraced(new Reciving(), "HTTP_Socket-Reciving");
        th.setDaemon(true);
        th.start();
      }

      // use multiple sending/receiving threads for better HTTP concurrency
      for (int i=0; i<NUMBER_OF_CONCURRENT_COMMUNICATION_THREADS; i++) {
        // make the synchronous HTTP sending-reciving thread
        th = new ThreadTraced(new SendingAndReciving(), "HTTP_Socket-SendingAndReciving");
        th.setDaemon(true);
        th.start();
      }

      // create data translating threads to connect streams with lists
      th = new ThreadTraced(new SendConverter(), "HTTP_Socket-SendConverter");
      th.setDaemon(true);
      th.start();
      th = new ThreadTraced(new ReciveConverter(), "HTTP_Socket-ReciveConverter");
      th.setDaemon(true);
      th.start();

      synchronized (connectionIdMonitor) {
        try {
          connectionIdMonitor.wait(CONNECTION_HTTP_TIMEOUT);
        } catch (InterruptedException e) {
          if (trace != null) trace.exception(HTTP_Socket.class, 100, e);
        }
        if (!connected) {
          if (trace != null) trace.data(200, "HTTSocket: connection timeout");
          //System.out.println("HTTSocket: connection timeout, closed()");
          closed();
          throw new IOException("connection timeout");
        }
      }
    } else {
      throw new IOException("Already connected!");
    }
    if (trace != null) trace.exit(HTTP_Socket.class);
  }

  public InputStream getInputStream() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "getInputStream()");
    if (recvPipeIn_Public == null) {
      if (trace != null) trace.data(10, "creating input stream pipes");
      recvPipeOut = new LargePipedOutputStream();
      recvPipeIn_Public = new LargePipedInputStream((LargePipedOutputStream) recvPipeOut, MAX_POST_DATA_SIZE);
    }
    if (trace != null) trace.exit(HTTP_Socket.class, recvPipeIn_Public);
    return recvPipeIn_Public;
  }

  public OutputStream getOutputStream() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "getOutputStream()");
    if (sendPipeOut_Public == null) {
      if (trace != null) trace.data(10, "creating output stream pipes");
      sendPipeOut_Public = new LargePipedOutputStream();
      sendPipeIn = new LargePipedInputStream((LargePipedOutputStream) sendPipeOut_Public, MAX_POST_DATA_SIZE);
    }
    if (trace != null) trace.exit(HTTP_Socket.class, sendPipeOut_Public);
    return sendPipeOut_Public;
  }

  public void close() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "close()");
    // closing client input pipe would immediatelly shut down our SendingConverter causing closed() to be called before we get a change to sendDisconnect()...
    // ommit closing this, it will be donw in the close() method.
    //try { sendPipeOut_Public.close(); } catch (Throwable t) { t.printStackTrace(); }
    try { recvPipeIn_Public.close(); } catch (Throwable t) { t.printStackTrace(); }
    if (!closed && !closing) {
      sendDisconnect();
    } else {
      String verb = closed ? "closed" : "closing";
      String errorMsg = "Already " + verb + "!";
      throw new IOException(errorMsg);
    }
    if (trace != null) trace.exit(HTTP_Socket.class);
  }

  public void closed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "closed()");
    closed = true;
    try { sendPipeOut_Public.close(); } catch (Throwable t) { }
    try { recvPipeIn_Public.close(); } catch (Throwable t) { }
    try { sendPipeIn.close(); } catch (Throwable t) { }
    try { recvPipeOut.close(); } catch (Throwable t) { }
    try { sendFifo.clear(); } catch (Throwable t) { }
    try { recvFifo.clear(); } catch (Throwable t) { }
    try { sentCacheFifo.clear(); } catch (Throwable t) { }
    if (trace != null) trace.exit(HTTP_Socket.class);
  }

  private void sendDisconnect() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "sendDisconnect()");
    if (!closed && !closing) {
      closing = true;
      // wake up threads waiting or slowed down
      synchronized (recvFifo) {
        recvFifo.notifyAll();
      }
      synchronized (sendFifo) {
        sendFifo.notifyAll();
      }
      synchronized (sendFifo) {
        sendSequenceId ++;
        DataSet requestDS = new DataSet(connectionId, sendSequenceId);
        //URL u = new URL("http", proxyHost, proxyPort, makeRandomPrefix()+requestDS.toURLEncoded());
        sendFifo.add(requestDS, sendSequenceId);
      }
      sendPipeOut_Public.close();
      //recvPipeIn_Public.close();
    } else {
      throw new IOException("Already closed!");
    }
    if (trace != null) trace.exit(HTTP_Socket.class);
  }


  private class ReciveConverter implements Runnable {
    private ReciveConverter() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReciveConverter.class, "ReciveConverter()");
      if (trace != null) trace.exit(ReciveConverter.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReciveConverter.class, "ReciveConverter.run()");
      try {
        boolean anyOut = false;
        while (!closed) {
          //System.out.println("ReciveConverter:run():while()");
          DataSet recvDS = null;
          synchronized (recvFifo) {
            recvDS = (DataSet) recvFifo.remove();
            if (recvDS == null) {
              if (recvFifo.size() > 0) {
                //System.out.println("recvDS is null but recvFifo.size()="+recvFifo.size()+" first id = " +((DataSet) recvFifo.peek()).sequenceId);
              }
              if (anyOut) {
                recvPipeOut.flush();
                anyOut = false;
              }
              try { recvFifo.wait(5000); } catch (InterruptedException e) { }
              // try again after waiting...
              recvDS = (DataSet) recvFifo.remove();
            }
          }
          if (recvDS != null) {
            //System.out.println("recive: ds = " + ds);
            switch (recvDS.action) {
              case DataSet.ACTION_CONNECT_RP :
                //xxx System.out.println("ACTION_CONNECT_RP");
                synchronized (connectionIdMonitor) {
                  connected = true;
                  connectionId = recvDS.connectionId;
                  connectionIdMonitor.notifyAll();
                }
                break;
              case DataSet.ACTION_DISCONNECT :
                //System.out.println("ACTION_DISCONNECT");
                closed();
                break;
              case DataSet.ACTION_SEND_RECV :
              case DataSet.ACTION_SEND_RECV_ASYNCH :
                //xxx System.out.println("ACTION_SEND_RECV");
                if (recvDS.data != null && recvDS.data.length > 0) {
                  try {
                    anyOut = true;
                    //System.out.println("recive: piping data : " + new String(ds.data));
                    recvPipeOut.write(recvDS.data);
                  } catch (IOException e) {
                    if (trace != null) trace.exception(ReciveConverter.class, 100, e);
                  }
                } else {
                  //System.out.println("recive: piping data EMPTY");
                }
                break;
              case DataSet.ACTION_NOT_CONNECTED :
                //System.out.println("ACTION_NOT_CONNECTED");
                closed();
                break;
              default :
                //System.out.println("*** default ***");
                break;
            } // end switch
          }
        } // end while()
      } catch (Throwable t) {
        if (trace != null) trace.exception(ReciveConverter.class, 200, t);
      }
      // No ReciveConverter thread so essentially the socket is CLOSED!
      if (trace != null) trace.data(300, "ReciveConverter completed, closed()");
      closed();
      // wake-up the SendingAndReciving thread...
      synchronized (recvFifo) {
        recvFifo.notifyAll();
      }
      if (trace != null) trace.exit(ReciveConverter.class);
    } // end run()
  } // end class ReciveConverter


  private class Sending implements Runnable {
    private Sending() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sending.class, "Sending()");
      if (trace != null) trace.exit(Sending.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sending.class, "Sending.run()");
      try {
        while (!closed) {
          // slow down streams if there is a backlog
          synchronized (recvFifo) {
            while (!closed && !closing && recvFifo.isAvailable(3))
              try { recvFifo.wait(1000); } catch (InterruptedException e) { }
          } // end synchronized slow down
          DataSet sendDS = null;
          synchronized (sendFifo) {
            if (socket != null) {
              sendDS = (DataSet) sendFifo.remove();
              if (sendDS == null) {
                if (sendFifo.size() > 0) {
                  //System.out.println("Sending: sendDS is null but sendFifo.size()="+sendFifo.size()+" first id = " +((DataSet) sendFifo.peek()).sequenceId);
                }
                try { sendFifo.wait(timeToWait); } catch (InterruptedException e) { }
                // After we wake-up from waiting for data to send, see if any data became available...
                sendDS = (DataSet) sendFifo.remove();
              }
            }
          }
          if (DEBUG_ON__DROP_PACKETS && rnd.nextInt(DEBUG_DROP_PACKETS_FREQUENCY) == 0) {
            // packet dropped
            System.out.println("request packet dropped");
            sendDS = null;
          }
          if (sendDS != null) {
            // make a cache of data before we try to send it
            sentCacheFifo.add(sendDS);
          }
          if (socket == null) {
            int socketRemakeDelay = MIN_TIME_SOCKET_REMAKE_DELAY + rnd.nextInt(MAX_TIME_SOCKET_REMAKE_DELAY - MIN_TIME_SOCKET_REMAKE_DELAY);
            try { Thread.sleep(socketRemakeDelay); } catch (InterruptedException e) { }
            makeSocket(proxyHost, proxyPort, CONNECTION_SOCKET_TIMEOUT);
          }
          try {
            if (sendDS != null) {
              boolean hadSomethingToSay = adjustWaitTime_PreSend(sendDS, false);
              //System.out.println("hadSomethingToSay = " + hadSomethingToSay);
              // Adjust retry watermarks.
              DataSetCache.setDSWatermarks(sendDS, recvFifo);
              DataOutputStream dOut = socketOutput;
              boolean insertError = DEBUG_ON__INSERT_RANDOM_ERRORS && rnd.nextInt(DEBUG_INSERT_RANDOM_ERROR_FREQUENCY) == 0;
              {
                if (insertError) {
                  System.out.println("~~~~~~~~~~ inserting error Sending 1 ~~~~~~~~~~~");
                  throw new IOException("broken");
                }
              }
              lastSendTimestamp = System.currentTimeMillis();
              if (sendDS.action == DataSet.ACTION_PING) {
                //System.out.println("SOCKET send " + sendDS.sequenceId + " ping");
              } else if (sendDS.action == DataSet.ACTION_PONG) {
                //System.out.println(" ..................................................... sending PONG " + sendDS.sequenceId);
              } else {
                //System.out.println("SOCKET send " + sendDS.sequenceId);
              }
              String header = "SOCKET2"+DataSet.CRLF;
              dOut.write(header.getBytes());
              {
                if (insertError) {
                  System.out.println("~~~~~~~~~~ inserting error Sending 2 ~~~~~~~~~~~");
                  throw new IOException("broken");
                }
              }
              byte[] bytes = sendDS.toByteArray();
              dOut.writeInt(bytes.length);
              dOut.write(bytes);
              dOut.writeBytes(DataSet.CRLF);
              dOut.flush();
              adjustWaitTime_PostSend(null, hadSomethingToSay, false, false);
              lastSendTimestamp = System.currentTimeMillis();
            }
          } catch (Throwable e) {
            dumpSocket();
            sendDS.tryNumber ++;
            //System.out.println("New SOCKET tryNumber = "+sendDS.tryNumber);
            if (sendDS.tryNumber > MAX_SEND_TRIES) {
              if (trace != null) trace.exception(SendingAndReciving.class, 300, e);
              closed();
            } else {
              // wait set penalty time after failure
              try {
                Thread.sleep(TIME_DELAY_AFTER_FAILED_SEND_TRY);
              } catch (InterruptedException e3) {
              }
              // push back request... note that they don't need to be in sequence... server will order them anyway
              synchronized (sendFifo) {
                //System.out.print("SOCK Push back");
                sendFifo.add(sendDS, sendDS.sequenceId); // cached copy will be dumpted later when delivery confirms
                //System.out.println(" ... pushed.id " + sendDS.sequenceId);
              }
            }
          }
        } // end while ()
      } catch (Throwable t) {
        if (trace != null) trace.exception(Sending.class, 400, t);
      }
      // No Sending thread so essentially the socket is CLOSED!
      if (trace != null) trace.data(500, "Sending completed, closed()");
      closed();
      // wake-up SendConverter if it is waiting on too large send queue
      synchronized (sendFifo) {
        sendFifo.notifyAll();
      }
      synchronized (recvFifo) {
        recvFifo.notifyAll();
      }
      if (trace != null) trace.exit(Sending.class);
    } // end run()
  } // end class Sending


  private class Reciving implements Runnable {
    final static String CRLF = "\r\n";
    private Reciving() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Reciving.class, "Reciving()");
      if (trace != null) trace.exit(Reciving.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Reciving.class, "Reciving.run()");
      try {
        while (!closed) {
          // slow down streams if there is a backlog
          synchronized (recvFifo) {
            while (!closed && !closing && recvFifo.isAvailable(3))
              try { recvFifo.wait(1000); } catch (InterruptedException e) { }
          } // end synchronized slow down
          try {
            if (socket != null) {
              DataInputStream dataIn = socketInput;
              int byteLength  = dataIn.readInt();
              byte[] bytes = new byte[byteLength];
              int countRead = 0;
              boolean insertError = DEBUG_ON__INSERT_RANDOM_ERRORS && rnd.nextInt(DEBUG_INSERT_RANDOM_ERROR_FREQUENCY) == 0;
              {
                if (insertError) {
                  System.out.println("~~~~~~~~~~ inserting error Reciving ~~~~~~~~~~~");
                  throw new IOException("broken");
                }
              }
              while ((countRead += dataIn.read(bytes, countRead, byteLength - countRead)) < byteLength) { }
              DataSet replyDS = DataSet.toDataSet(bytes);
              //System.out.println("recived " + replyDS.getActionName());
              if (replyDS.isClientResponseRequired() && connected) {
                synchronized (sendFifo) {
                  if (true || sendFifo.size() == 0) {
                    sendSequenceId ++;
                    DataSet requestDS = new DataSet(DataSet.ACTION_PONG, connectionId, sendSequenceId, null);
                    sendFifo.add(requestDS, sendSequenceId);
                    //System.out.println(" ..................................................... making PONG " + sendSequenceId);
                  }
                }
              }
              if (DEBUG_ON__DROP_PACKETS && rnd.nextInt(DEBUG_DROP_PACKETS_FREQUENCY) == 0) {
                // packet dropped
                System.out.println("reply packet dropped");
              } else {
                enqueueReplyDS(replyDS);
              }
            } else {
              try { Thread.sleep(50); } catch (Throwable t) { }
            }
          } catch (Throwable t) {
            dumpSocket();
          }
        } // end while ()
      } catch (Throwable t) {
        if (trace != null) trace.exception(Reciving.class, 400, t);
      }
      // No Reciving thread so essentially the socket is CLOSED!
      if (trace != null) trace.data(500, "Reciving completed, closed()");
      closed();
      // wake-up SendConverter if it is waiting on too large send queue
      synchronized (sendFifo) {
        sendFifo.notifyAll();
      }
      synchronized (recvFifo) {
        recvFifo.notifyAll();
      }
      if (trace != null) trace.exit(Reciving.class);
    } // end run()
  } // end class Reciving


  private class SendingAndReciving implements Runnable {
    private SendingAndReciving() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendingAndReciving.class, "SendingAndReciving()");
      if (trace != null) trace.exit(SendingAndReciving.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendingAndReciving.class, "SendingAndReciving.run()");
      try {
        while (!closed) {
          // slow down streams if there is a backlog
          synchronized (recvFifo) {
            while (!closed && !closing && recvFifo.isAvailable(3)) {
              if (trace != null) trace.data(10, "slow down streams due to backlog");
              try { recvFifo.wait(1000); } catch (InterruptedException e) { }
            }
          } // end synchronized slow down
          //System.out.println("SendingAndReciving:run():while()");
          DataSet sendDS = null;
          if (trace != null) trace.info(20, "synchronize on sendFifo");
          synchronized (sendFifo) {
            if (socket == null) {
              if (trace != null) trace.info(25, "socket is null so send with this HTTP thread");
              if (ENABLE_HTTP_CONNECTIONS)
                sendDS = (DataSet) sendFifo.remove();
              if (sendDS == null) {
                if (trace != null) trace.info(30, "no DataSet available so wait for it");
                if (sendFifo.size() > 0) {
                  if (trace != null) trace.info(31, "sendDS is null but sendFifo.size()="+sendFifo.size()+" first id = " +((DataSet) sendFifo.peek()).sequenceId);
                  //System.out.println("sendDS is null but sendFifo.size()="+sendFifo.size()+" first id = " +((DataSet) sendFifo.peek()).sequenceId);
                }
                try { sendFifo.wait(timeToWait); } catch (InterruptedException e) { }
                if (trace != null) trace.info(35, "thread woke up from waiting for DataSet");
                // After we wake-up from waiting for data to send, see if any data became available...
                if (socket == null && ENABLE_HTTP_CONNECTIONS)
                  sendDS = (DataSet) sendFifo.remove();
                if (sendDS == null) {
                  if (trace != null) trace.info(36, "woke up and no DataSet available");
                } else {
                  if (trace != null) trace.info(37, "woke up and DataSet is available");
                }
              } else {
                if (trace != null) trace.info(40, "DataSet is available");
              }
            } else {
              if (trace != null) trace.info(50, "socket NOT null so DO NOT send with this HTTP thread");
              try { sendFifo.wait(timeToWait); } catch (InterruptedException e) { }
              if (trace != null) trace.info(51, "thread woke up from waiting on sendFifo");
            }
          }
          try {
            //System.out.println("in try");
            if (sendDS != null) {
              // make a cache of data before we try to send it
              sentCacheFifo.add(sendDS);
              if (trace != null) trace.info(60, "has sendDS");
              //System.out.println("has sendDS");
              // mark timestamp before sending and after reading reply to avoid excessive ping-pong
              lastSendTimestamp = System.currentTimeMillis();
              //DataSet sendDS = DataSet.toDataSet(url.getFile().substring(url.getFile().indexOf('=')+1));
              if (sendDS.action != DataSet.ACTION_SEND_RECV || sendDS.data != null) {
                if (trace != null) trace.info(70, "openning DS", sendDS);
                //System.out.println("opening DS="+sendDS);
              }
              if (sendDS.action == DataSet.ACTION_PING) {
                if (trace != null) trace.info(80, "HTTP send " + sendDS.sequenceId + " ping-pong");
                //System.out.println("HTTP send " + sendDS.sequenceId + " ping-pong");
              } else {
                if (trace != null) trace.info(85, "HTTP send " + sendDS.sequenceId);
                //System.out.println("HTTP send " + sendDS.sequenceId);
              }
              boolean hadSomethingToSay = adjustWaitTime_PreSend(sendDS, true);
              // Adjust retry watermarks.
              DataSetCache.setDSWatermarks(sendDS, recvFifo);
              // choose method of sending data based on data size
              boolean doPOST = sendDS.data != null && sendDS.data.length > MAX_URL_DATA_SIZE;
              if (trace != null) trace.info(90, "doPOST", doPOST);
              //System.out.println("doPOST "+ doPOST);
              boolean insertError = DEBUG_ON__INSERT_RANDOM_ERRORS && rnd.nextInt(DEBUG_INSERT_RANDOM_ERROR_FREQUENCY) == 0;
              {
                if (insertError) {
                  System.out.println("~~~~~~~~~~ inserting error SendRecv out ~~~~~~~~~~~");
                  throw new IOException("broken");
                }
              }
              URL url = null;
              url = new URL("http", proxyHost, proxyPort, doPOST ? makeRandomPOSTPrefix() : makeRandomGETPrefix()+sendDS.toURLEncoded());
              URLConnection urlConn = null;
              if (trace != null) trace.info(100, "url", url);
              //System.out.println("url "+url);
              if (doPOST) {
                if (trace != null) trace.info(100, "" + new Date() + " POST " + sendDS.sequenceId + " " + sendDS.data.length);
                //System.out.println("" + new Date() + " POST " + sendDS.sequenceId + " " + sendDS.data.length);
                String boundary = MultiPartFormOutputStream.createBoundary();
                urlConn = MultiPartFormOutputStream.createConnection(url);
                urlConn.setRequestProperty("Accept", "*/*");
                urlConn.setRequestProperty("Content-Type", MultiPartFormOutputStream.getContentType(boundary));
                // set some other request headers...
                urlConn.setRequestProperty("Connection", "Keep-Alive");
                urlConn.setRequestProperty("Cache-Control", "no-cache");
                // no need to connect cuz getOutputStream() does it
                MultiPartFormOutputStream out = new MultiPartFormOutputStream(urlConn.getOutputStream(), boundary);
                // write a text field element
                out.writeField(makeRandomVarname(6), new String(sendDS.toBASE64()));
                out.close();
              } else {
                if (trace != null) trace.info(110, "" + new Date() + " GET " + sendDS.sequenceId + " " + (sendDS.data != null ? sendDS.data.length : 0));
                //System.out.println("" + new Date() + " GET " + sendDS.sequenceId + " " + (sendDS.data != null ? sendDS.data.length : 0));
                urlConn = url.openConnection();
                urlConn.setUseCaches(false);
              }
              if (trace != null) trace.info(120, (((HttpURLConnection) urlConn).usingProxy() ? "Using" : "NOT using") + " proxy");
              //System.out.println((((HttpURLConnection) urlConn).usingProxy() ? "Using" : "NOT using") + " proxy");
              Object content = urlConn.getContent();
              InputStream in = null;
              if (content instanceof InputStream)
                in = (InputStream) content;
              {
                if (insertError) {
                  System.out.println("~~~~~~~~~~ inserting error SendRecv in ~~~~~~~~~~~");
                  throw new IOException("broken");
                }
              }
              if (trace != null) trace.info(130, "content", content);
              //System.out.println("content="+content);
              StringBuffer sb = new StringBuffer();
              int ch = -1;
              while ((ch = (char) in.read()) != ' ') sb.append((char) ch);
              int length = Integer.parseInt(sb.toString());
              if (trace != null) trace.info(140, "length", length);
              //System.out.println("len="+length);
              byte[] bytes = null;
              try {
                bytes = new byte[length];
              } catch (Throwable t) {
                if (trace != null) trace.exception(SendingAndReciving.class, 100, t);
              }
              if (trace != null) trace.info(141, "reading bytes");
              int countRead = in.read(bytes);
              if (trace != null) trace.info(142, "initial part read of length", countRead);
              if (countRead < length) {
                // Only perform another read if not all of it was read
                // This is necessary because if no more bytes next read would STALL!
                while ((countRead += in.read(bytes, countRead, length - countRead)) < length) { }
              }
              String page = null;
              try {
                page = new String(bytes);
              } catch (Throwable t) {
                if (trace != null) trace.exception(SendingAndReciving.class, 200, t);
              }
              if (trace != null) trace.info(150, "page read");
              //System.out.println("page read");
              in.close();
              if (trace != null) trace.info(151, "page", page);
              //System.out.println("page="+page);

              DataSet replyDS = DataSet.toDataSet(page.toCharArray());
              if (trace != null) trace.info(152, "replyDS constructed");
              //System.out.println("replyDS constructed");
              adjustWaitTime_PostSend(replyDS, hadSomethingToSay, true, false);
              if (replyDS.isContentOrControlPacket() && connected) {
                synchronized (sendFifo) {
                  if (sendFifo.size() == 0) {
                    sendSequenceId ++;
                    DataSet requestDS = new DataSet(DataSet.ACTION_PING, connectionId, sendSequenceId, null);
                    sendFifo.add(requestDS, sendSequenceId);
                  }
                }
              }
              enqueueReplyDS(replyDS);
              // mark timestamp before sending and after reading reply to avoid excessive ping-pong
              lastSendTimestamp = System.currentTimeMillis();
            }
            else if (connected && !closing && !closed && Math.abs(System.currentTimeMillis() - lastSendTimestamp) > timeToWait) {
              if (Math.abs(System.currentTimeMillis() - lastPingMadeStamp) > timeToWait) {
                long timeWaited = Math.abs(System.currentTimeMillis() - lastSendTimestamp);
                if (trace != null) trace.info(160, "Ping-Pong  " + this + " " +timeWaited+":"+timeToWait);
                //System.out.println("Ping-Pong  " + this + " " +timeWaited+":"+timeToWait);
                if (trace != null) trace.info(161, "timeToWait="+timeToWait+", but time waited="+(Math.abs(System.currentTimeMillis() - lastSendTimestamp)));
                //System.out.println("timeToWait="+timeToWait+", but time waited="+(Math.abs(System.currentTimeMillis() - lastSendTimestamp)));
                // PERIODIC ping-pong stuff....
                // send a BLANK request for return data
                synchronized (sendFifo) {
                  if (sendFifo.size() == 0) {
                    lastPingMadeStamp = System.currentTimeMillis();
                    if (trace != null) trace.info(170, new Date()+" make ping-pong");
                    //System.out.println(new Date()+" make ping-pong");
                    sendSequenceId ++;
                    DataSet requestDS = new DataSet(DataSet.ACTION_PING, connectionId, sendSequenceId, null);
                    sendFifo.add(requestDS, sendSequenceId);
                  }
                }
              }
            }
          } catch (Throwable e) {
            sendDS.tryNumber ++;
            if (trace != null) trace.data(180, "New HTTP tryNumber = "+sendDS.tryNumber);
            //System.out.println("New HTTP tryNumber = "+sendDS.tryNumber);
            if (sendDS.tryNumber > MAX_SEND_TRIES) {
              if (trace != null) trace.exception(SendingAndReciving.class, 300, e);
              closed();
            } else {
              // wait set penalty time after failure
              try {
                Thread.sleep(TIME_DELAY_AFTER_FAILED_SEND_TRY);
              } catch (InterruptedException e3) {
              }
              // push back request... note that they don't need to be in sequence... server will order them anyway
              synchronized (sendFifo) {
                if (trace != null) trace.info(190, "HTTP Push back");
                //System.out.print("HTTP Push back");
                sendFifo.add(sendDS, sendDS.sequenceId); // cached copy will be dumpted later when delivery confirms
                if (trace != null) trace.info(191, " ... pushed.");
                //System.out.println(" ... pushed.");
              }
            }
          }
        } // end while()
      } catch (Throwable t) {
        if (trace != null) trace.exception(SendingAndReciving.class, 400, t);
      }
      // No SendingAndReciving thread so essentially the socket is CLOSED!
      if (trace != null) trace.data(500, "SendingAndReciving completed, closed()");
      closed();
      // wake-up SendConverter if it is waiting on too large send queue
      synchronized (sendFifo) {
        sendFifo.notifyAll();
      }
      synchronized (recvFifo) {
        recvFifo.notifyAll();
      }
      if (trace != null) trace.exit(SendingAndReciving.class);
    } // end run()
  } // end class SendingAndReciving


  private class SendConverter implements Runnable {
    private SendConverter() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendConverter.class, "SendConverter()");
      if (trace != null) trace.exit(SendConverter.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendConverter.class, "SendConverter.run()");
      try {
        while (!closed && !closing) {
          //System.out.println("SendConverter:run():while()");
          synchronized (connectionIdMonitor) {
            if (!connected) {
              // wait a little to get connected
              //System.out.println("SendConverter:run:wait");
              try {
                connectionIdMonitor.wait(1000);
              } catch (Throwable t) {
              }
              //System.out.println("SendConverter:run:awake");
            }
          }
          //System.out.println("SendConverter:run:connected="+connected);
          if (connected) {
            try {
              // slow down requests if backlog...
              if (sendFifo.size() > 0 || recvFifo.size() > 0) {
                Thread.yield();
              }
              int b = sendPipeIn.read();
              if (b >= 0) {
                int available = sendPipeIn.available();
                if (available + 1 > MAX_POST_DATA_SIZE)
                  available = MAX_POST_DATA_SIZE - 1;
                int byteLength = available+1; // 1 single byte 'b' was already read
                byte[] bytes = null;
                try {
                  bytes = new byte[byteLength];
                  bytes[0] = (byte) b;
                } catch (Throwable t) {
                  if (trace != null) trace.exception(SendConverter.class, 100, t);
                  throw new IOException("Could not alocate sufficient number of bytes.");
                }
                int countRead = 1; // single byte was already read
                while ((countRead += sendPipeIn.read(bytes, countRead, byteLength - countRead)) < byteLength) { }
                synchronized (sendFifo) {
                  // slow down if too many in the send queue
                  while (sendFifo.size() > 1 && !closed && !closing) {
                    try { sendFifo.wait(1000); } catch (InterruptedException e) { }
                  }
                  if (!closed) {
                    sendSequenceId ++;
                    DataSet requestDS = new DataSet(DataSet.ACTION_SEND_RECV, connectionId, sendSequenceId, bytes);
                    sendFifo.add(requestDS, sendSequenceId);
                  }
                }
              } else {
                if (trace != null) trace.data(150, "EOF in SendConverter - break");
                break;
              }
            } catch (IOException e) {
              if (trace != null) trace.exception(SendConverter.class, 200, e);
              try {
                sendDisconnect();
              } catch (IOException e2) {
                if (trace != null) trace.exception(SendConverter.class, 300, e2);
              }
            }
          }
        } // end while()
      } catch (Throwable t) {
        if (trace != null) trace.exception(SendConverter.class, 400, t);
      }
      if (trace != null) trace.data(500, "SendConverter completed, closed()");
      closed();
      synchronized (sendFifo) {
        sendFifo.notifyAll();
      }
      if (trace != null) trace.exit(SendConverter.class);
    } // end run()
  } // end class SendConverter


  /**
   * @return true if data set has content.
   */
  private boolean adjustWaitTime_PreSend(DataSet sendDS, boolean isHTTP) {
    boolean hadSomethingToSay = false;
    if (sendDS.isContentOrControlPacket()) {
      hadSomethingToSay = true;
      timeToWait = isHTTP ? MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP : MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET;
    } else {
      //System.out.println(new Date()+" send ping-pong");
    }
    return hadSomethingToSay;
  }

  private void adjustWaitTime_PostSend(DataSet replyDS, boolean hadSomethingToSay, boolean isHTTP, boolean suppressExponent) {
    if (hadSomethingToSay) {
      timeToWait = isHTTP ? MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP : MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET;
      //System.out.println("TimeToWait " + this + " " + timeToWait + " saying");
    } else if (replyDS != null && replyDS.isContentOrControlPacket()) {
      timeToWait = isHTTP ? MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP : MIN_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET;
      //System.out.println("TimeToWait " + this + " " + timeToWait + " reply action or not empty");
    } else {
      // if any backlog then wait short time so we retry sooner...
      synchronized (recvFifo) {
        if (recvFifo.hasBacklog()) {
          int timeToWait1 = (int) Math.pow(timeToWait, TIME_TO_WAIT_POWER);
          int timeToWait2 = TIME_TO_WAIT_WHEN_BACKLOG;
          timeToWait = Math.min(timeToWait1, timeToWait2);
          //System.out.println("TimeToWait " + this + " " + timeToWait + " backlog");
        } else if (!suppressExponent) {
          //System.out.print("timeToWait " + timeToWait);
          timeToWait = (int) Math.pow(timeToWait, TIME_TO_WAIT_POWER);
          //System.out.println(" new timeToWait " + timeToWait);
          timeToWait = Math.min(timeToWait, isHTTP ? MAX_TIME_TO_WAIT_TO_REQUEST_REPLY__HTTP : MAX_TIME_TO_WAIT_TO_REQUEST_REPLY__SOCKET);
          //System.out.println("TimeToWait " + this + " " + timeToWait);
        }
      }
    }
  }

  private void enqueueReplyDS(DataSet replyDS) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "enqueueReplyDS(DataSet replyDS)");
    if (replyDS.action == DataSet.ACTION_NOT_CONNECTED) {
      closed();
    } else {
      synchronized (recvFifo) {
        ArrayList chain = replyDS.chain;
        replyDS.chain = null;
        ArrayList list = new ArrayList();
        recvFifo.add(replyDS, replyDS.sequenceId);
        list.add(replyDS);
        if (chain != null) {
          for (int i=0; i<chain.size(); i++) {
            DataSet ds = (DataSet) chain.remove(0);
            recvFifo.add(ds, ds.sequenceId);
            list.add(ds);
          }
        }
        for (int i=0; i<list.size(); i++) {
          sentCacheFifo.manageCache((DataSet) list.get(i), sendFifo);
        }
      }
    }
    if (trace != null) trace.exit(HTTP_Socket.class);
  }

  /**
   * @return String to replace '/index.jsp?SESSIONID='
   */
  private static String makeRandomGETPrefix() {
    return "/" + makeRandomVarname(rnd, 6) + "." + fileExts[rnd.nextInt(fileExts.length)] + "?" + makeRandomVarname(rnd, 6) + "=";
  }
  /**
   * @return String to replace '/index.jsp'
   */
  private static String makeRandomPOSTPrefix() {
    return "/" + makeRandomVarname(rnd, 6) + "." + fileExts[rnd.nextInt(fileExts.length)];
  }

  private static String makeRandomVarname(int maxLen) {
    return makeRandomVarname(new Random(), maxLen);
  }
  private static String makeRandomVarname(Random rnd, int maxLen) {
    byte a = 'a';
    byte z = 'z';
    int varChars = rnd.nextInt(maxLen) + 1;
    byte[] varname = new byte[varChars];
    for (int i=0; i<varname.length; i++)
      varname[i] = (byte) (rnd.nextInt(z-a)+a);
    return new String(varname);
  }

  public int getPort() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "getPort()");
    int p = 0;
    if (host != null)
      p = port;
    else
      p = proxyPort;
    if (trace != null) trace.exit(HTTP_Socket.class, p);
    return p;
  }

  public InetAddress getInetAddress() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTTP_Socket.class, "getInetAddress()");
    InetAddress addr = null;
    try {
      String toHost = host != null ? host : proxyHost;
      addr = InetAddress.getByName(toHost);
    } catch (Throwable t) {
      if (trace != null) trace.exception(HTTP_Socket.class, 100, t);
    }
    if (trace != null) trace.exit(HTTP_Socket.class, addr);
    return addr;
  }

//  public static void main(String[] args) {
//    try {
//      final HTTP_Socket socket = new HTTP_Socket("localhost", 1500, "localhost", 6008);
//      //final HTTSocket socket = new HTTSocket("localhost", 1500, null, 0);
//      final OutputStream out = socket.getOutputStream();
//      final InputStream in = socket.getInputStream();
//      final BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
//      Thread testSend = new Thread(new Runnable() {
//        public void run() {
//          try {
//            while (true) {
//              String line = inReader.readLine();
//              if (line != null && line.length() > 0) {
//                out.write(line.getBytes());
//                out.flush();
//              } else {
//                try {
//                  Thread.sleep(100);
//                } catch (InterruptedException e) {
//                }
//              }
//            } // end while
//          } catch (IOException e) {
//            e.printStackTrace();
//          }
//        }
//      });
//
//      Thread testRecive = new Thread(new Runnable() {
//        public void run() {
//          try {
//            while (true) {
//              int b = in.read();
//              if (b >= 0) {
//                byte[] bytes = null;
//                int available = in.available();
//                if (available > 0) {
//                  if (available > 2*1024)
//                    available = 2*1024 - 1;
//                  bytes = new byte[available + 1];
//                  in.read(bytes, 1, bytes.length - 1);
//                } else {
//                  bytes = new byte[1];
//                }
//                bytes[0] = (byte) b;
//                String response = new String(bytes).trim();
//                System.out.println("> " + response + " <");
//              } else {
//                //xxx System.out.println("********** EOF in testRecive **********");
//                break;
//              }
//            }
//          } catch (Throwable t) {
//            //xxx System.out.println("Exception in testRecive... QUITTING");
//            t.printStackTrace();
//          }
//        }
//      });
//
//      testSend.setDaemon(true);
//      testRecive.setDaemon(true);
//      testSend.start();
//      testRecive.start();
//
//      testSend.join();
//      testRecive.join();
//      System.out.println("JOINED");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

}