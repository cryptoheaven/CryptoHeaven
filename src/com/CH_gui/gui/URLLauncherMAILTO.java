/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.service.records.Record;
import com.CH_gui.frame.MessageFrame;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.util.URLLauncher;
import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.6 $</b>
*
* @author  Marcin Kurzawa
*/
public class URLLauncherMAILTO extends Object implements URLLauncher {

  public void openURL(URL url, Component invoker) {
    Record initialRecipient = null;
    String path = url.getPath();
    // this URL maybe HTML encoded - decode it now
    try {
      path = URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException x) {
    }
    // additionally convert all &amp; to &
    path = path.replace("&amp;", "&");
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
    if (path != null && path.length() > 0) {
      Long userid = null;
      try {
        userid = Long.valueOf(path);
        // use my contact list only, not the reciprocal contacts
        initialRecipient = CacheUsrUtils.convertUserIdToFamiliarUser(userid, true, false);
      } catch (Throwable t) {
      }
      if (initialRecipient == null)
        initialRecipient = new EmailAddressRecord(path);
    }
    new MessageFrame(initialRecipient, sendFromEmailAccount);
  }

}