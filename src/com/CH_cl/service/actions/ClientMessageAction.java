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

package com.CH_cl.service.actions;

import java.io.IOException;
import com.CH_co.trace.Trace;

import com.CH_co.io.DataInputStream2;
import com.CH_co.util.Misc;

import com.CH_co.service.engine.CommonSessionContext;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class ClientMessageAction extends MessageAction {

  public boolean isGUIsuppressed = false;

  /** The ServerInterfaceLayer through which this message arrived. */
  private ServerInterfaceLayer serverInterfaceLayer;


  /** Creates new ClientMessageAction */
  public ClientMessageAction() {
  }

  /** Creates new ClientMessageAction that encapsulates another MessageAction */
  protected ClientMessageAction(MessageAction encapsulatedAction) {
    super(encapsulatedAction.getActionCode(), encapsulatedAction.getMsgDataSet(), encapsulatedAction);
  }

  // Normal return code must be overwritten by all actions.
  // Exception code is here for remainder of implementation only.
  protected int normalReturnCode = CommandCodes.SYS_E_EXCEPTION;


  /** 
   * Creates an Instance of ServerMessageAction based on the incoming message code and initializes it. 
   * Sets session context for the lifetime of this action.
   * Thread is held here until full message becomes available.
   * @param code Action Code
   * @param stamp Unique action stamp.
   */
  public static ClientMessageAction readActionFromStream( DataInputStream2 dataIn, 
                                                          ClientSessionContext sessionContext, int code, long stamp)
                                         throws IOException, DataSetException, 
                                                IllegalAccessException, 
                                                InstantiationException,
                                                ClassNotFoundException 
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ClientMessageAction.class, "readActionFromStream(DataInputStream2, ClientSessionContext, int code, long stamp)");
    if (trace != null) trace.args(code);
    if (trace != null) trace.args(stamp);

    String actionClassName = ClientActionSwitch.switchCodeToActionName(code);
    if (trace != null) trace.data(20, "actionClassName=" + Misc.getClassNameWithoutPackage(actionClassName));
    if (actionClassName == null) {
      throw new IllegalStateException("Action code unknown!");
    }

    //ClientMessageAction action = (ClientMessageAction) Class.forName(Misc.getPackageName(ClientMessageAction.class) + "." + actionClassName).newInstance();
    ClientMessageAction action = (ClientMessageAction) Class.forName(actionClassName).newInstance();
    if (trace != null) trace.data(30, "action instantiated");

    // The actionCode integer is already consumed.  Pass it separately.
    // Thread is held here until the rest of the message becomes available.
    action.initFromStream(dataIn, code, stamp, sessionContext.clientBuild, sessionContext.serverBuild);
    action.setClientContext(sessionContext);
    // Decrypt data set if applicable
    ProtocolMsgDataSet dataSet = action.getMsgDataSet();
    if (dataSet instanceof Obj_EncSet_Co) {
      Obj_EncSet_Co encDataSet = (Obj_EncSet_Co) dataSet;
      encDataSet.decrypt(sessionContext.getKeyPairToReceiveWith().getPrivateKey(), sessionContext.clientBuild, sessionContext.serverBuild);
      action.setMsgDataSet(encDataSet.dataSet);
    }

    if (trace != null) trace.exit(ClientMessageAction.class);
    return action;
  }


  /** 
   * The action handler performs all actions related to the received action message,
   * and optionally returns a reply action.  If there is no reply, null is returned.
   */
  public abstract MessageAction runAction();


  /** 
   * Initialize the Message Action with the context of the communications through which it came. 
   * This method is automatically called when action is read from the stream.
   */
  public void setClientContext(ClientSessionContext sessionContext) {
    setCommonContext(sessionContext);
  }

  protected ClientSessionContext getClientContext() {
    CommonSessionContext context = getCommonContext();
    ClientSessionContext clientContext = null;
    if (context instanceof ClientSessionContext)
      clientContext = (ClientSessionContext) context;
    return clientContext;
  }

  /** 
   * Shortcut for getServerInterfaceLayer().getFetchedDataCache() 
   */
  protected FetchedDataCache getFetchedDataCache() {
    return getServerInterfaceLayer().getFetchedDataCache();
  }

  /** 
   * Initialize the Message Action with the ServerInterfaceLayer which can be notified with exceptional circumstances. 
   */
  public void setServerInterfaceLayer(ServerInterfaceLayer serverInterfaceLayer) {
    this.serverInterfaceLayer = serverInterfaceLayer;
  }
  public ServerInterfaceLayer getServerInterfaceLayer() {
    return serverInterfaceLayer;
  }

}