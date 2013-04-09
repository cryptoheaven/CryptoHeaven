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
package com.CH_cl.util;

import com.CH_cl.service.engine.ServerInterfaceLayer;

/**
* <b>Copyright</b> &copy; 2001-2013 <a
* href="http://www.CryptoHeaven.com/DevelopmentTeam/"> CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
* 
* Class Description:
* 
* Holder for SIL separated from handline code that is JRE 1.5+
*
* Class Details:
* 
*
* <b>$Revision: 1.2 $</b>
*
* @author Marcin Kurzawa
* @version
*/
public class MyUncaughtExceptionHandlerSIL {

  private static ServerInterfaceLayer SIL;

  public static void setSIL(ServerInterfaceLayer theSIL) {
    SIL = theSIL;
  }

  protected static ServerInterfaceLayer getSIL() {
    return SIL;
  }

}