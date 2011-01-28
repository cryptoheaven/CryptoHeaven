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

package com.CH_co.service.msg;

import java.io.*;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.*;
import com.CH_co.io.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * Protocol Message passed between client and server.
 * Structure of a message:
 * <msgCode> <protocolMsgDataCode> <protocolMsgDataSet>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Message extends Object {

  private ProtocolMsgDataSet msgDataSet;

  /** Creates new Message */
  protected Message() {
  }

  /** Creates new Message and initialize the code and data. */
  protected Message(ProtocolMsgDataSet msgDataSet) {
    this.msgDataSet = msgDataSet;
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "(ProtocolMsgDataSet msgDataSet)");
    if (trace != null) trace.args(msgDataSet);
    if (trace != null) trace.exit(Message.class);
  }

  /** @return the message data bytes without cloning */
  /** note: changed from protected to public to use in download files -- b.s.**/
  public ProtocolMsgDataSet getMsgDataSet() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "getMsgDataSet()");
    if (trace != null) trace.exit(Message.class, msgDataSet);
    return msgDataSet;
  }
  public void setMsgDataSet(ProtocolMsgDataSet set) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "setMsgDataSet(ProtocolMsgDataSet set)");
    if (trace != null) trace.args(set);
    msgDataSet = set;
    if (trace != null) trace.exit(Message.class);
  }

  private static ProtocolMsgDataSet getProtocolMsgDataSetInstance(int protocolMsgDataSetType) throws DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "getProtocolMsgDataSetInstance(int protocolMsgDataSetType)");
    if (trace != null) trace.args(protocolMsgDataSetType);

    ProtocolMsgDataSet msgDataSet = null;

    if (ProtocolMsgDataSwitch.isNull(protocolMsgDataSetType)) {
      if (trace != null) trace.data(10, "data set is empty");
    } else {
      if (trace != null) trace.data(15, "not an empty data set");
      // Convert code to class name.
      String className = ProtocolMsgDataSwitch.getClassName(protocolMsgDataSetType);
      // Code is not null, but could not fetch a non-null class name.
      if (className == null)
        throw new DataSetException("Unrecognized data set, please upgrade to newest version! ("+protocolMsgDataSetType+")");
      // Try instantiating the concrete class per suggested name.
      try {
        msgDataSet = (ProtocolMsgDataSet) Class.forName(className).newInstance();
      } catch (InstantiationException e) {
        // This should never happen, if it does, its a coding error or unsupported data set.
        if (trace != null) trace.exception(Message.class, 100, e);
      } catch (IllegalAccessException e) {
        // This should never happen, if it does, its a coding error or unsupported data set.
        if (trace != null) trace.exception(Message.class, 110, e);
      } catch (ClassNotFoundException e) {
        // This should never happen, if it does, its a coding error or unsupported data set.
        if (trace != null) trace.exception(Message.class, 120, e);
      }
      // Since the code was not null, we must have a data set, but we couldn't instantiate one.
      if (msgDataSet == null)
        throw new IllegalStateException("Unsupported data set, there was a problem instantiating " + className + " for specified code "+protocolMsgDataSetType);
    } // end else if not null

    if (trace != null) trace.exit(Message.class, msgDataSet);
    return msgDataSet;
  }


  /** 
   * Output this Message to a specified stream. 
   * @args progressMonitor is nullable if the data set does not require it.
   */
  protected void writeToStream(DataOutputStream2 out, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "writeToStream(DataOutputStream2 out, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(out, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    writeToStream(out, progressMonitor, msgDataSet, clientBuild, serverBuild);
    if (trace != null) trace.exit(Message.class);
  }
  /**
   * Output this Message to a specified stream. 
   * @args progressMonitor is nullable if the data set does not require it.
   */
  public static void writeToStream(DataOutputStream2 out, ProgMonitorI progressMonitor, ProtocolMsgDataSet msgDataSet, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "writeToStream(DataOutputStream2 out, ProgMonitor progressMonitor, ProtocolMsgDataSet msgDataSet, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(out, progressMonitor, msgDataSet);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    int protocolMsgDataSetType = ProtocolMsgDataSwitch.getCode(msgDataSet);
    if (trace != null) trace.data(20, "type", protocolMsgDataSetType);

    if (msgDataSet != null && ProtocolMsgDataSwitch.isNull(protocolMsgDataSetType))
      throw new IllegalStateException("Data set is not null, but switch returns a null indicator.");

    // only one thread should be writing to a stream at a time
    synchronized(out) {
      if (trace != null) trace.data(30, "write type", protocolMsgDataSetType);
      out.writeInt(protocolMsgDataSetType);

      // communications debug
      /*
      String name = null;
      if (msgDataSet != null)
        name = Misc.getClassNameWithoutPackage(msgDataSet.getClass());
      else
        name = "Null Data Set";

      byte[] bName = name.getBytes();
      bName = ArrayUtils.fixLength(bName, 30);
      out.writeBytes(bName);
      */


      if (!ProtocolMsgDataSwitch.isNull(protocolMsgDataSetType) && msgDataSet != null) {
        String dataInfoName = msgDataSet.getDataSetInfoName();
        if (progressMonitor != null) progressMonitor.startSendData(dataInfoName);
        msgDataSet.writeToStream(out, progressMonitor, clientBuild, serverBuild);
        if (progressMonitor != null) progressMonitor.doneSendData(dataInfoName);
      }
      // DON'T flush because Message object is never written on its own.. always wrapped in MessageAction
    }
    if (trace != null) trace.exit(Message.class);
  }



  /** 
   * Initializes 'this' Message from an Input Stream. 
   * @args progressMonitor is nullable if the data set does not require it.
   */
  protected void initFromStream(DataInputStream2 in, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException, DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "initFromStream(DataInputStream2 in, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(in, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    msgDataSet = readFromStream(in, progressMonitor, clientBuild, serverBuild);
    if (trace != null) trace.exit(Message.class);
  }
  /** 
   * Initializes a Message Data Set from an Input Stream. 
   * @args progressMonitor is nullable if the data set does not require it.
   * @return reconstructed ProtocolMsgDataSet from the stream which was written by corresponding writeToStream() call.
   */
  public static ProtocolMsgDataSet readFromStream(DataInputStream2 in, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException, DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Message.class, "readFromStream(DataInputStream2 in, ProgMonitor, progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(in, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    ProtocolMsgDataSet msgDataSet = null;
    // only one thread should be reading a message at a time
    synchronized(in) {
      int protocolMsgDataSetType = in.readInt();


      // communications debug
      //in.readBytes();


      if (trace != null) trace.data(10, "type", protocolMsgDataSetType);
      // Get the specific instance of ProtocolMsgDataSet.
      msgDataSet = getProtocolMsgDataSetInstance(protocolMsgDataSetType);

      // Initialize the set from the input stream.
      if (msgDataSet != null) {
        if (trace != null) trace.data(20, "data set instance class", Misc.getClassNameWithoutPackage(msgDataSet.getClass()));

        String dataInfoName = msgDataSet.getDataSetInfoName();
        if (progressMonitor != null) progressMonitor.startReceiveData(dataInfoName);

        // Pass the client build before the date set is read from the stream, it may effect reading pattern.
        msgDataSet.initFromStream(in, progressMonitor, clientBuild, serverBuild);
        if (progressMonitor != null) progressMonitor.doneReceiveData(dataInfoName);
      }
    }

    if (trace != null) trace.exit(Message.class, msgDataSet);
    return msgDataSet;
  }

  protected void copyDataSetFromMessage(Message sourceMessage) {
    this.msgDataSet = sourceMessage.msgDataSet;
  }

  public String toString() {
    return "[Message"
      + ": msgDataSet=" + msgDataSet
      + "]";
  }
}