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

package comx.Jaguar.gui;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MyJaguar extends Jaguar {

  /** Creates new MyJaguar */
  public MyJaguar() {
    super(true, false, true, true);
  }

  public static String formatShortCode(String str) {
    String rc = null;
    if (str != null && str.length() == 4) {
      java.util.StringTokenizer st = new java.util.StringTokenizer(str, "hdcs", true);
      String v1 = null;
      String s1 = null;
      String v2 = null;
      String s2 = null;
      if (st.hasMoreTokens()) v1 = st.nextToken().toUpperCase();
      if (st.hasMoreTokens()) s1 = st.nextToken().toLowerCase();
      if (st.hasMoreTokens()) v2 = st.nextToken().toUpperCase();
      if (st.hasMoreTokens()) s2 = st.nextToken().toLowerCase();
      if (v1 != null && s1 != null && v2 != null && s2 != null && !st.hasMoreTokens() && !(v1+s1).equals(v2+s2)) {
        String cardValues = "  23456789TJQKA";
        int vv1 = cardValues.indexOf(v1.charAt(0));
        int vv2 = cardValues.indexOf(v2.charAt(0));
        if (vv1 >= 2 && vv2 >= 2) {
          if (vv1 < vv2) {
            String _tv = v1;
            String _ts = s1;
            v1 = v2;
            s1 = s2;
            v2 = _tv;
            s2 = _ts;
          }
          switch (s1.charAt(0)) {
            case 'h':
              s1 = "<font size=+1 color=red>" + v1 + "&#9829" + "</font>";
              break;
            case 'd':
              s1 = "<font size=+1 color=red>" + v1 + "&#9830" + "</font>";
              break;
            case 'c':
              s1 = "<font size=+1 color=black>" + v1 + "&#9827" + "</font>";
              break;
            case 's':
              s1 = "<font size=+1 color=black>" + v1 + "&#9824" + "</font>";
              break;
          }
          switch (s2.charAt(0)) {
            case 'h':
              s2 = "<font size=+1 color=red>" + v2 + "&#9829" + "</font>";
              break;
            case 'd':
              s2 = "<font size=+1 color=red>" + v2 + "&#9830" + "</font>";
              break;
            case 'c':
              s2 = "<font size=+1 color=black>" + v2 + "&#9827" + "</font>";
              break;
            case 's':
              s2 = "<font size=+1 color=black>" + v2 + "&#9824" + "</font>";
              break;
          }
          rc = s1 + " " + s2;
        }
      }
    }
    return rc;
  }

}