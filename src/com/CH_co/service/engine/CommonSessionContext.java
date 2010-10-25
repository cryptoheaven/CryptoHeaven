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

package com.CH_co.service.engine;

import java.net.*;
import java.io.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.monitor.Interruptible;
import com.CH_co.io.*;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Holds session related variables like I/O streams, client and server version numbers, etc.
 *
 * Structure of stream chains:
 *  i/o stream ->
 *     speed limited i/o stream ->
 *         interruptible i/o stream ->
 *             buffered i/o stream ->
 *  a)
 *                 data i/o stream2
 *  b)
 *                 block cipher i/o stream ->
 *                     data i/o stream2
 * @author  Marcin Kurzawa
 * @version
 */
public abstract class CommonSessionContext extends Object implements Interruptible {

  private static final int BUFFER_SIZE = 1024*8;
  
  protected Socket connectedSocket;
  private InputStream in;
  private OutputStream out;
  private InputStream interIn;
  private OutputStream interOut;
  private DataInputStream2 dataIn;
  private DataOutputStream2 dataOut;
  private BlockCipherInputStream blockCipherIn;
  private BlockCipherOutputStream blockCipherOut;

  private BASymmetricKey symmetricKeyMaterialOutgoing;
  private BASymmetricKey symmetricKeyMaterialIncoming;
  private boolean loginComplete;
  private boolean streamsSecured;
  private boolean isClient;

  public float clientVersion = 1.0f;
  public short clientRelease = 0;
  public short clientBuild = 0;
  public short serverBuild = 0;
  public Long lastReportedPingMS;

  private RSAKeyPair keyPairToReceiveWith;
  private RSAPublicKey publicKeyToSendWith;

  /** Creates new CommonSessionContext, used by ClientSessionContext */
  public CommonSessionContext(Socket connectedSocket, long inRate, long outRate, boolean globalRateHookup) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "CommonSessionContext(Socket connectedSocket, long inRate, long outRate, boolean globalRateHookup)");
    if (trace != null) trace.args(connectedSocket);
    if (trace != null) trace.args(inRate);
    if (trace != null) trace.args(outRate);
    if (trace != null) trace.args(globalRateHookup);

    this.connectedSocket = connectedSocket;
    this.isClient = true;
    initCommunications(connectedSocket, inRate, outRate, globalRateHookup);
    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  /** Creates new CommonSessionContext */
  public CommonSessionContext(Socket connectedSocket) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "CommonSessionContext(Socket connectedSocket)");
    if (trace != null) trace.args(connectedSocket);

    this.connectedSocket = connectedSocket;
    this.isClient = false;
    initCommunications(connectedSocket, 0, 0, true);
    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  public void setSoTimeout(int timeout) throws SocketException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "setSoTimeout(int timeout)");
    if (trace != null) trace.args(timeout);
    connectedSocket.setSoTimeout(timeout);
    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  private synchronized void initCommunications(Socket connectedSocket, long inRate, long outRate, boolean globalRateHookup) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "initCommunications(Socket, boolean isClient, long inRate, long outRate, boolean globalRateHookup)");
    if (trace != null) trace.args(connectedSocket);
    if (trace != null) trace.args(isClient);
    if (trace != null) trace.args(inRate);
    if (trace != null) trace.args(outRate);
    if (trace != null) trace.args(globalRateHookup);

    // no need to buffer up the socket streams as they already use internal buffering
    if (trace != null) trace.data(10, "getting input stream");
    this.in = connectedSocket.getInputStream();
    if (trace != null) trace.data(20, "getting output stream");
    this.out = connectedSocket.getOutputStream();

    /*
    try {
      int num = com.CH_co.cryptx.Rnd.getSecureRandom().nextInt();
      num = Math.abs(num);
      this.in = new com.tools.TeeInputStream(in, "tee_" + num + ".out");
      this.out = new com.tools.TeeOutputStream(out, (com.tools.TeeInputStream) in);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    */

    // making throughput measuring and speed limited streems
    if (trace != null) trace.data(30, "making SpeedLimitedInputStream");
    this.in = new SpeedLimitedInputStream(in, inRate, globalRateHookup); // Kbps
    if (trace != null) trace.data(40, "making SpeedLimitedOutputStream");
    this.out = new SpeedLimitedOutputStream(out, outRate, globalRateHookup);

    if (isClient) {
      // Interruptible streams for progress monitor CANCEL action
      if (trace != null) trace.data(50, "client mode : making InterruptibleInputStream");
      this.interIn = new InterruptibleInputStream(in);
      if (trace != null) trace.data(60, "client mode : making InterruptibleOutputStream");
      this.interOut = new InterruptibleOutputStream(out);
    } else {
      this.interIn = in;
      this.interOut = out;
    }

    if (trace != null) trace.data(70, "getting InetAddress");
    InetAddress addr = connectedSocket.getInetAddress();
    if (trace != null) trace.data(80, "getting InetAddress");
    String hostAddress = addr.getHostAddress();
    if (trace != null) trace.data(90, "getting connected port");
    String sourceName = "" + hostAddress + ":" + connectedSocket.getPort();

    // buffer up the communications streams to take load off the speed limiting stream computations...
    if (trace != null) trace.data(100, "wrapping in Data Input and Output streams, and buffering up");
    this.dataIn = new DataInputStream2(new BufferedInputStream(interIn, BUFFER_SIZE), sourceName);
    this.dataOut = new DataOutputStream2(new BufferedOutputStream(interOut, BUFFER_SIZE), sourceName);

    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  private boolean closed = false;
  public synchronized void closeCommunications() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "closeCommunications()");

    if (!closed) {
      closed = true;
      try { getDataOutputStream2().flush(); } catch (Throwable t) { }
      try { in.close();                     } catch (Throwable t) { }
      try { interIn.close();                } catch (Throwable t) { }
      try { if (blockCipherIn  != null) blockCipherIn.close();  } catch (Throwable t) { }
      try { getDataInputStream2().close();  } catch (Throwable t) { }
      // Close the socket before closing output streams to help HTTP Socket
      // send a graceful disconnect action before it bails out.
      // Otherwise it would notice client streams closed before it can do final communications.
      try { connectedSocket.close();            } catch (Throwable t) { }
      try { out.close();                    } catch (Throwable t) { }
      try { interOut.close();               } catch (Throwable t) { }
      try { if (blockCipherOut != null) blockCipherOut.close(); } catch (Throwable t) { }
      try { getDataOutputStream2().close(); } catch (Throwable t) { }
      // used to try releasing native held object on linux
      Misc.killSocket(connectedSocket);
    }

    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  public long calculateRateIn() {
    return (in instanceof SpeedLimitedInputStream) ? ((SpeedLimitedInputStream) in).calculateRate() : 0;
  }
  public long calculateRateOut() {
    return (out instanceof SpeedLimitedOutputStream) ? ((SpeedLimitedOutputStream) out).calculateRate() : 0;
  }
  public long calculateRate() {
    return calculateRateIn() + calculateRateOut();
  }

  public synchronized void setKeyMaterial(BASymmetricKey keyMaterialOutgoing, BASymmetricKey keyMaterialIncoming) {
    this.symmetricKeyMaterialOutgoing = keyMaterialOutgoing;
    this.symmetricKeyMaterialIncoming = keyMaterialIncoming;
  }

  public boolean                        isLoggedIn()            { return loginComplete;   }
  public Socket                         getSocket()             { return connectedSocket; }
  public String                         getSocketHostPort()     { return connectedSocket.getInetAddress().getHostAddress() + ":" + connectedSocket.getPort(); }
  public synchronized DataInputStream2  getDataInputStream2()   { return dataIn;          }
  public synchronized DataOutputStream2 getDataOutputStream2()  { return dataOut;         }


  /** Secures the input/output Data streams with current key material.
   *  Notifies waiters on the DataInputStream2 that is has changed through dataIn.notifyAll();
   *  Releases all threads that are waiting on DataInputStream2 and DataOutputStream2 for completion of login sequence.
   */
  public synchronized void secureStreams() throws InvalidKeyException, NoSuchAlgorithmException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "secureStreams()");
    if (streamsSecured)
      throw new IllegalStateException("Streams already secured!");
    streamsSecured = true;
    Object monitorIn = dataIn;
    Object monitorOut = dataOut;
    synchronized (monitorIn) {
      // buffer up the communications streams to take load off the speed limiting streams computations...
      blockCipherIn = new BlockCipherInputStream(new BufferedInputStream(interIn, BUFFER_SIZE), symmetricKeyMaterialIncoming);
      dataIn = new DataInputStream2(blockCipherIn, dataIn.getName());
      monitorIn.notifyAll();
    }
    synchronized (monitorOut) {
      // buffer up the communications streams to take load off the speed limiting streams computations...
      blockCipherOut = new BlockCipherOutputStream(new BufferedOutputStream(interOut, BUFFER_SIZE), symmetricKeyMaterialOutgoing);
      dataOut = new DataOutputStream2(blockCipherOut, dataOut.getName());
      monitorOut.notifyAll();
    }
    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  public synchronized boolean isSecured() {
    return streamsSecured;
  }

  public synchronized long resetTransferedAllByteCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "resetTransferedAllByteCount()");
    long rc = resetTransferedInByteCount() + resetTransferedOutByteCount();
    if (trace != null) trace.exit(CommonSessionContext.class, rc);
    return rc;
  }

  private synchronized long resetTransferedInByteCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "resetTransferedInByteCount()");
    long rc = 0;
    if (blockCipherIn != null)
      rc = blockCipherIn.resetByteCounter();
    if (trace != null) trace.exit(CommonSessionContext.class, rc);
    return rc;
  }

  private synchronized long resetTransferedOutByteCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "resetTransferedOutByteCount()");
    long rc = 0;
    if (blockCipherOut != null)
      rc = blockCipherOut.resetByteCounter();
    if (trace != null) trace.exit(CommonSessionContext.class, rc);
    return rc;
  }

  public synchronized long getTransferedAllByteCounter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "getTransferedAllByteCounter()");
    long rc = 0;
    if (blockCipherIn != null)
      rc = blockCipherIn.getByteCounter();
    if (blockCipherOut != null)
      rc += blockCipherOut.getByteCounter();
    if (trace != null) trace.exit(CommonSessionContext.class, rc);
    return rc;
  }

  public synchronized void login(boolean bAction) {
    loginComplete = bAction;
    // additional cleanup on logout
    if (bAction == false) {
      symmetricKeyMaterialOutgoing = null;
      symmetricKeyMaterialIncoming = null;
    }
  }

  public String toString() {
    return "[CommonSessionContext "
        + ": connectedSocket="                  + connectedSocket
        + ", in="                               + (in==null?"null":"not null")
        + ", out="                              + (out==null?"null":"not null")
        + ", interIn="                          + (interIn==null?"null":"not null")
        + ", interOut="                         + (interOut==null?"null":"not null")
        + ", blockCipherIn="                    + (blockCipherIn==null?"null":"not null")
        + ", blockCipherOut="                   + (blockCipherOut==null?"null":"not null")
        + ", dataIn="                           + (dataIn==null?"null":"not null")
        + ", dataOut="                          + (dataOut==null?"null":"not null")
        + ", symmetricKeyMaterialOutgoing is "  + (symmetricKeyMaterialOutgoing==null?"null":"not null")
        + ", symmetricKeyMaterialIncoming is "  + (symmetricKeyMaterialIncoming==null?"null":"not null")
        + ", loginComplete="                    + loginComplete
        + ", clientVersion="                    + clientVersion
        + ", clientRelease="                    + clientRelease
        + ", clientBuild="                      + clientBuild
        + ", serverBuild="                      + serverBuild
        + ", lastReportedPingMS="               + lastReportedPingMS
        + "]";
  }


  protected void finalize() throws Throwable {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "finalize()");
    try {
      closeCommunications();
      super.finalize();
    } catch (Throwable t) {
      if (trace != null) trace.exception(CommonSessionContext.class, 100, t);
      if (trace != null) trace.exit(CommonSessionContext.class);
      throw t;
    }
    if (trace != null) trace.exit(CommonSessionContext.class);
  }


  /**
   * Interruptible interface method.
   */
  public synchronized void interrupt() {
    if (interIn instanceof InterruptibleInputStream)
      ((InterruptibleInputStream)interIn).interrupt();
    if (interOut instanceof InterruptibleOutputStream)
      ((InterruptibleOutputStream)interOut).interrupt();
  }

  public synchronized void generateKeyPairIfDoesntExist(int strength) {
    if (keyPairToReceiveWith == null) {
      keyPairToReceiveWith = RSAKeyPairGenerator.generateKeyPair(strength);
    }
  }

  public synchronized RSAKeyPair getKeyPairToReceiveWith() {
    return keyPairToReceiveWith;
  }

  public synchronized RSAPublicKey getPublicKeyToSendWith() {
    return publicKeyToSendWith;
  }

  public synchronized void setKeyPairToReceiveWith(RSAKeyPair keyPair) {
    keyPairToReceiveWith = keyPair;
  }

  public synchronized void setPublicKeyToSendWith(RSAPublicKey publicKey) {
    publicKeyToSendWith = publicKey;
  }

}