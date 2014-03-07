/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.sys;

import com.CH_co.service.engine.CommonSessionContext;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Description:
 *
 *
 * Details:
 *
 *
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface SysALoginImplementationI {

  public void processReply(Obj_List_Co reply, CommonSessionContext sessionContext) throws InvalidKeyException, NoSuchAlgorithmException;

}
