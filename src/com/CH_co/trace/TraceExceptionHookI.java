/**
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/
package com.CH_co.trace;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* @author  Marcin Kurzawa
*/
public interface TraceExceptionHookI {

  public void traceExceptionHook(Class c, int tracePoint, Throwable t);

}