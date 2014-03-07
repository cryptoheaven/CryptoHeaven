/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.http.HTTP_Common;

import java.util.Iterator;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class DataSetCache extends Object {

  private OrderedFifo cache;

  private long sequenceIdAvailable = -1;
  private long sequenceIdProcessed = -1;

  public DataSetCache() {
    cache = new OrderedFifo();
  }

  /**
   * Adds the DataSet and its entire chain, one-by-one, each one separately with its own sequence ID
   * @param ds
   */
  public synchronized void add(DataSet ds) {
    if (ds != null) {
      cache.add(ds, ds.sequenceId);
      if (ds.chain != null) {
        for (int i=0; i<ds.chain.size(); i++) {
          DataSet chainDS = (DataSet) ds.chain.get(i);
          cache.add(chainDS, chainDS.sequenceId);
        }
      }
    }
  }

  public synchronized void clear() {
    sequenceIdAvailable = -1;
    sequenceIdProcessed = -1;
    cache.clear();
  }

  public synchronized void manageCache(DataSet recivedDS, OrderedFifo sendFifo) {
    // remove confirmed received DataSets
    removeConfirmed(recivedDS.responseSequenceIdProcessed);
    // if retry capable packet
    if (recivedDS.responseSequenceIdAvailable != -1 && recivedDS.responseSequenceIdProcessed != -1) {
      moveRetryPacketsToSend(sendFifo, recivedDS.responseSequenceIdAvailable, recivedDS.responseSequenceIdProcessed);
//      if (cache.size() > 0) {
//        synchronized (this) {
//          StringBuffer sb = new StringBuffer("manageCache: size ");
//          sb.append(cache.size() + " : ");
//          java.util.Iterator iter = cache.iterator();
//          while (iter.hasNext())
//            sb.append(((DataSet) iter.next()).sequenceId + " ");
//          System.out.println(sb.toString());
//        }
//      }
    } // end if retry capable packet
  }

  private synchronized void moveRetryPacketsToSend(OrderedFifo sendFifo, long newSequenceIdAvailable, long newSequenceIdProcessed) {
    if (sequenceIdAvailable != newSequenceIdAvailable ||
        sequenceIdProcessed != newSequenceIdProcessed)
    {
      // mark watermarks to be checked next time... if next time they are unchanged then send retry packets
      sequenceIdProcessed = newSequenceIdProcessed;
      sequenceIdAvailable = newSequenceIdAvailable;
    } else {
      // reset hi and low watermarks;
      sequenceIdProcessed = -1;
      sequenceIdAvailable = -1;
      // move old cached responses to send queue to be resent
      // If complain of a backlog in the client...
      moveToSend(sendFifo, newSequenceIdAvailable);
    }
  }

  private synchronized void moveToSend(OrderedFifo sendFifo, long hiIdToExclude) {
    boolean isDebug = false;
    if (hiIdToExclude > -1) {
      StringBuffer sb = null;
      if (isDebug)
        sb = new StringBuffer();
      while (cache.size() > 0 && ((DataSet) cache.peek()).sequenceId < hiIdToExclude) {
        DataSet cachedDS = (DataSet) cache.remove();
        if (isDebug) {
          if (sb.length() == 0)
            sb.append(" ++ PUSH BACK ");
          sb.append(cachedDS.sequenceId + " ");
        }
        sendFifo.add(cachedDS, cachedDS.sequenceId);
      }
      if (isDebug && sb.length() > 0)
        System.out.println(sb.toString());
    }
  }

  private synchronized void removeConfirmed(long processedId) {
    if (processedId > -1) {
      //System.out.println("confirmed "+processedId);
      while (cache.size() > 0 && ((DataSet) cache.peek()).sequenceId <= processedId) {
        DataSet ds = (DataSet) cache.remove();
      }
    }
  }

  public synchronized int size() {
    return cache.size();
  }

  public synchronized int sizeBytes() {
    Iterator iter = cache.iterator();
    int totalSize = 0;
    while (iter.hasNext()) {
      DataSet dataSet = (DataSet) iter.next();
      if (dataSet.data != null)
        totalSize += dataSet.data.length;
    }
    return totalSize;
  }

  public static void setDSWatermarks(DataSet sendDS, SequenceFifo recvFifo) {
    sendDS.responseSequenceIdProcessed = recvFifo.getLastRemovedSequence();
    if (sendDS.batchId % DataSet.RETRY_PACKET_BATCH_SIZE == 0) {
      DataSet peekDS = (DataSet) recvFifo.peek();
      sendDS.responseSequenceIdAvailable = peekDS != null ? peekDS.sequenceId : -1;
    }
    //System.out.println("                  watermarks " + sendDS.responseSequenceIdProcessed + " " + sendDS.responseSequenceIdAvailable + " " + Thread.currentThread().getName());
  }

  public synchronized void trimToCount(int maxCount) {
    while (size() > maxCount)
      cache.remove();
  }

  public synchronized void trimToSize(int maxBytes) {
    while (sizeBytes() > maxBytes)
      cache.remove();
  }

}