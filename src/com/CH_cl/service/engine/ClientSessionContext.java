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

import java.net.Socket;
import java.io.IOException;

import com.CH_co.cryptx.*;
import com.CH_co.io.SpeedLimiter;
import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_co.service.engine.CommonSessionContext;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ClientSessionContext extends CommonSessionContext {

  // This will be a global identification number for my user id for all of my workers.
  // The goal here is to provide the server a distinction between application instances
  // that may be connected under the same user id.
  public static final long SESSION_ID = Rnd.getSecureRandom().nextLong();

  // The RequestSubmitterI who's READER has read the message to recreate it from stream.
  // Submitting subsequent messages to this RequestSubmitterI, will cause them to be written
  // through the same writer/reader pair that this message went through.
  private RequestSubmitterI requestSubmitter;

  /**
   * Creates new ClientSessionContext WITH rate control for CLIENT side use.
   */
  public ClientSessionContext(Socket connectedSocket, RequestSubmitterI requestSubmitter) throws IOException {
    super(connectedSocket, SpeedLimiter.connInRate, SpeedLimiter.connOutRate, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ClientSessionContext.class, "ClientSessionContext(Socket connectedSocket, RequestSubmitterI requestSubmitter)");
    if (trace != null) trace.args(connectedSocket, requestSubmitter);
    this.requestSubmitter = requestSubmitter;
    this.clientVersion = GlobalProperties.PROGRAM_VERSION;
    this.clientRelease = GlobalProperties.PROGRAM_RELEASE;
    this.clientBuild = GlobalProperties.PROGRAM_BUILD_NUMBER;
    if (trace != null) trace.exit(ClientSessionContext.class);
  }
  /**
   * Creates new ClientSessionContext without rate control for SERVER side use
   */
  public ClientSessionContext(Socket connectedSocket, RequestSubmitterI requestSubmitter, boolean isClientRateControlled) throws IOException {
    super(connectedSocket);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ClientSessionContext.class, "ClientSessionContext(Socket connectedSocket, RequestSubmitterI requestSubmitter, boolean isClientRateControlled)");
    if (trace != null) trace.args(connectedSocket, requestSubmitter);
    if (trace != null) trace.args(isClientRateControlled);
    if (isClientRateControlled) throw new IllegalArgumentException("For client side use different constructor!");
    this.requestSubmitter = requestSubmitter;
    this.clientVersion = GlobalProperties.PROGRAM_VERSION;
    this.clientRelease = GlobalProperties.PROGRAM_RELEASE;
    this.clientBuild = GlobalProperties.PROGRAM_BUILD_NUMBER;
    if (trace != null) trace.exit(ClientSessionContext.class);
  }


  /**
   * Releases all threads that are waiting on DataInputStream2 and DataOutputStream2 for completion of login sequence.
   */
  public void releaseLoginStreamers() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ClientSessionContext.class, "releaseLoginStreamers()");
    Object monitorIn = getDataInputStream2();
    Object monitorOut = getDataOutputStream2();
    synchronized (monitorIn)  { monitorIn.notifyAll();  }
    synchronized (monitorOut) { monitorOut.notifyAll(); }
    if (trace != null) trace.exit(ClientSessionContext.class);
  }

  /**
   * @return this message's request pipe.
   */
  public RequestSubmitterI getRequestSubmitter() {
    return requestSubmitter;
  }

}