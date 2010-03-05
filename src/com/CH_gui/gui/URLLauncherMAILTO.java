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

package com.CH_gui.gui;

import java.awt.*;
import java.net.*;

import com.CH_cl.service.records.*;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.util.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgs.MsgPanelUtils;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
public class URLLauncherMAILTO extends Object implements URLLauncher {

  public void openURL(URL url, Component invoker) {
    String emailAddress = URLDecoder.decode(url.getPath());
    // find first Parent that is RecipientProviderI
    EmailRecord sendFromEmailAccount = null;
    MsgDataProviderI msgDataProvider = null;
    Component c = invoker;
    while (c != null && !(c instanceof MsgDataProviderI))
      c = c.getParent();
    // if we found a MsgDataProviderI
    if (c != null) {
      msgDataProvider = (MsgDataProviderI) c;
      MsgDataRecord msgData = msgDataProvider.provideMsgData();
      sendFromEmailAccount = MsgPanelUtils.getOurMatchingFromEmlRec(msgData);
    }
    new MessageFrame(new EmailAddressRecord(emailAddress), sendFromEmailAccount);
  }

}