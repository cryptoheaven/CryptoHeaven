/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.Tiger.gui;

import java.util.*;
import comx.Tiger.ssce.*;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class TigerPropSession extends PropSpellingSession {

  /** Creates new TigerPropSession */
  public TigerPropSession(Properties properties) {
    super(properties);
  }

  public int countUserLexicons() {
    FileTextLexicon[] userLexicons = getUserLexicons();
    return userLexicons != null ? userLexicons.length : 0;
  }

}