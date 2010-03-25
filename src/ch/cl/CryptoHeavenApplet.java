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

package ch.cl;

import com.CH_co.util.ArrayUtils;
import com.CH_co.util.BrowserLauncher;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.Misc;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class CryptoHeavenApplet extends JApplet implements DisposableObj {

  private CryptoHeavenApplet applet;
  private JLabel jTitle;

  private String strLoaded = "CryptoHeaven Web Edition loaded";
  private String strDone = "CryptoHeaven Web Edition done.";

  /** Creates new CryptoHeavenApplet */
  public CryptoHeavenApplet() {
  }

  public void init() {
    String _strLoaded = getParameter("strLoaded");
    String _strDone = getParameter("strDone");

    if (_strLoaded == null)
      _strLoaded = getParameter("strloaded");
    if (_strDone == null)
      _strDone = getParameter("strdone");

    if (_strLoaded != null && _strLoaded.trim().length() > 0)
      strLoaded = _strLoaded;
    if (_strDone != null && _strDone.trim().length() > 0)
      strDone = _strDone;

    jTitle = new JLabel(strLoaded);
    jTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
    getContentPane().add(jTitle);
    setApplet(this);
    // pass on the program arguments
    String[] args = null;
    String argsParam = getParameter("args");
    if (argsParam != null && argsParam.length() > 0) {
      args = argsParam.split("[ ]+");
      // make sure there is no leading delimited blanks
      args = (String[]) ArrayUtils.removeLeadingElements(args, "");
    }
    CryptoHeaven.main(args);
  }

  public void destroy() {
    disposeObj();
    super.destroy();
  }

  private void setApplet(CryptoHeavenApplet app) {
    applet = app;
    Misc.setSystemExitObj(app);
    BrowserLauncher.setAppletContext(app != null ? app.getAppletContext() : null);
  }

  /**
   * Method of DisposableObj interface used to exit the applet and release resources.
   */
  public void disposeObj() {
    if (applet != null) {
      if (jTitle != null) {
        jTitle.setText(strDone);
      }
      try {
        applet.stop();
      } catch (Throwable t) {
      }
      try {
        applet.destroy();
      } catch (Throwable t) {
      }
    }
  }

}