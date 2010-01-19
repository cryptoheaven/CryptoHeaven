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

package com.CH_cl.service.records;

import javax.swing.*;

import com.CH_co.util.*;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class EmailAddressRecord extends InternetAddressRecord {

  /** Creates new EmailAddressRecord */
  public EmailAddressRecord() {
    super();
  }

  /** Creates new EmailAddressRecord and auto-assign a unique id per unique address. */
  public EmailAddressRecord(String address) {
    super(address);
  }

  /**
   * @return the default icon to represent this Record type.
   */
  public Icon getIcon() {
    return Images.get(ImageNums.EMAIL_SYMBOL_SMALL);
  }

}