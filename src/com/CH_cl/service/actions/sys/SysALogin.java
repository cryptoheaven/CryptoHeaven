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

package com.CH_cl.service.actions.sys;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SysALogin extends ClientMessageAction {

  /** Creates new SysALogin */
  public SysALogin() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysALogin.class, "SysALogin()");
    if (trace != null) trace.exit(SysALogin.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysALogin.class, "runAction(Connection)");

    // reply syntax:
    // <sessionKeys> <serverVersion> <serverRelease> <serverBuild>

    Obj_List_Co reply = (Obj_List_Co) getMsgDataSet();
    //FetchedDataCache cache = getFetchedDataCache();

    try {
      SysALoginImplementationI sysALogin = (SysALoginImplementationI) Class.forName("com.CH_gui_admin.SysALoginImplementation").newInstance();
      sysALogin.processReply(reply, getCommonContext());
    } catch (InstantiationException ex) {
      throw new SecurityException("Could not instantiate an implementation.");
    } catch (IllegalAccessException ex) {
      throw new SecurityException("Could not access an implementation.");
    } catch (ClassNotFoundException ex) {
      throw new SecurityException("Could not find an implementation.");
    } catch (InvalidKeyException e) {
      if (trace != null) trace.exception(SysALogin.class, 50, e);
      throw new SecurityException("Could not instantiate a SymmetricBulkCipher or failed while securing streams.");
    } catch (NoSuchAlgorithmException e) {
      if (trace != null) trace.exception(SysALogin.class, 60, e);
      throw new SecurityException("Could not instantiate a default AsymmetricBlockCipher");
    }

    // all ok
    getClientContext().login(true);

    if (trace != null) trace.exit(SysALogin.class, null);
    return null;
  }

}