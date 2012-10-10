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

package com.CH_co.service.engine;

import com.CH_co.cryptx.*;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.io.SpeedLimitedInputStream;
import com.CH_co.io.SpeedLimitedOutputStream;
import com.CH_co.monitor.Interruptible;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Holds session related variables like I/O streams, client and server version numbers, etc.
*
* Structure of stream chains:
*  i/o stream ->
*     speed limited and interruptible i/o stream ->
*  a)  data i/o stream2
*  b)  block cipher i/o stream ->
*        data i/o stream2
* 
* @author  Marcin Kurzawa
* @version
*/
public abstract class CommonSessionContext extends Object implements Interruptible {

  protected Socket connectedSocket;
  private InputStream in;
  private OutputStream out;
  private SpeedLimitedInputStream speedIn;
  private SpeedLimitedOutputStream speedOut;
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
    this.speedIn = new SpeedLimitedInputStream(in, inRate, globalRateHookup); // Kbps
    if (trace != null) trace.data(40, "making SpeedLimitedOutputStream");
    this.speedOut = new SpeedLimitedOutputStream(out, outRate, globalRateHookup);

    if (trace != null) trace.data(70, "getting InetAddress");
    InetAddress addr = connectedSocket.getInetAddress();
    if (trace != null) trace.data(80, "getting InetAddress");
    String hostAddress = addr.getHostAddress();
    if (trace != null) trace.data(90, "getting connected port");
    String sourceName = "" + hostAddress + ":" + connectedSocket.getPort();

    if (trace != null) trace.data(100, "wrapping in Data Input and Output streams");
    this.dataIn = new DataInputStream2(speedIn, sourceName);
    this.dataOut = new DataOutputStream2(speedOut, sourceName);

    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  private boolean closed = false;
  public synchronized void closeCommunications() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CommonSessionContext.class, "closeCommunications()");

    if (!closed) {
      closed = true;
      try { getDataOutputStream2().flush(); } catch (Throwable t) { }
      try { in.close();                     } catch (Throwable t) { }
      try { speedIn.close();                } catch (Throwable t) { }
      try { if (blockCipherIn  != null) blockCipherIn.close();  } catch (Throwable t) { }
      try { getDataInputStream2().close();  } catch (Throwable t) { }
      // Close the socket before closing output streams to help HTTP Socket
      // send a graceful disconnect action before it bails out.
      // Otherwise it would notice client streams closed before it can do final communications.
      try { connectedSocket.close();        } catch (Throwable t) { }
      try { out.close();                    } catch (Throwable t) { }
      try { speedOut.close();               } catch (Throwable t) { }
      try { if (blockCipherOut != null) blockCipherOut.close(); } catch (Throwable t) { }
      try { getDataOutputStream2().close(); } catch (Throwable t) { }
      // used to try releasing native held object on linux
      Misc.killSocket(connectedSocket);
    }

    if (trace != null) trace.exit(CommonSessionContext.class);
  }

  public long calculateRateIn() {
    long rate = 0;
    try { rate = speedIn.calculateRate(); } catch (Throwable t) { }
    return rate;
  }
  public long calculateRateOut() {
    long rate = 0;
    try { rate = speedOut.calculateRate(); } catch (Throwable t) { }
    return rate;
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
      blockCipherIn = new BlockCipherInputStream(speedIn, symmetricKeyMaterialIncoming);
      dataIn = new DataInputStream2(blockCipherIn, dataIn.getName());
      monitorIn.notifyAll();
    }
    synchronized (monitorOut) {
      blockCipherOut = new BlockCipherOutputStream(speedOut, symmetricKeyMaterialOutgoing);
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
        + ", speedIn="                          + (speedIn==null?"null":"not null")
        + ", speedOut="                         + (speedOut==null?"null":"not null")
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
    try { speedIn.interrupt();  } catch (Throwable t) { }
    try { speedOut.interrupt(); } catch (Throwable t) { }
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