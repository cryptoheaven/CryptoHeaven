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

import com.CH_co.queue.*;
import com.CH_co.trace.Trace;

import com.CH_cl.service.actions.ClientMessageAction;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class IndependentClientQueueExecutionFunction extends Object implements ProcessingFunctionI {

  private ServerInterfaceLayer serverInterfaceLayer;

  /** Creates new IndependentClientQueueExecutionFunction */
  public IndependentClientQueueExecutionFunction(ServerInterfaceLayer serverInterfaceLayer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IndependentClientQueueExecutionFunction.class, "IndependentClientQueueExecutionFunction(ServerInterfaceLayer serverInterfaceLayer)");
    this.serverInterfaceLayer = serverInterfaceLayer;
    if (trace != null) trace.exit(IndependentClientQueueExecutionFunction.class);
  }

  /* =======================================================
  Methods from ProcessingFunctionI for the 'executionQueue'
  ========================================================= */
  /** start processing cached objects */
  public void processQueuedObject(Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IndependentClientQueueExecutionFunction.class, "processQueuedObject(Object)");
    if (trace != null) trace.args(obj);

    try {
      ClientMessageAction nextMsgAction = (ClientMessageAction) obj;
      DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, nextMsgAction);
    } catch (Throwable t) {
      // execution of server reply went wrong -- critical error.
      
      if (trace != null) trace.exception(IndependentClientQueueExecutionFunction.class, 100, t);
    }

    if (trace != null) trace.exit(IndependentClientQueueExecutionFunction.class);
  }

}