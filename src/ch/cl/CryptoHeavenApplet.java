/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package ch.cl;

import com.CH_co.util.*;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.2 $</b>
*
* @author  Marcin Kurzawa
*/
public class CryptoHeavenApplet extends JApplet implements DisposableObj, AppletTypeI {

  private CryptoHeavenApplet applet;
  private JLabel jTitle;

  private String strLoaded = "Secure Web Edition loaded";
  private String strDone = "Secure Web Edition done.";

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
    // Attach Applet exception handler
    try {
      MyUncaughtExceptionHandlerOps.attachDefaultHandler("Applet " + GlobalProperties.PROGRAM_BUILD_NUMBER);
    } catch (Throwable t) {
      // Uncaught Exception Handler is jre 1.5+ code so catch all here
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
    try {
      BrowserLauncher.setAppletContext(app != null ? app : null);
    } catch (Throwable t) {
      // catch all throwables incase this legacy class cannot be loaded and initialized
    }
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

  public void showDocument(URL url, String target) {
    getAppletContext().showDocument(url, target);
  }

}