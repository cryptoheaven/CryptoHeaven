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

package com.CH_co.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

import com.CH_co.trace.Trace;
import com.CH_guiLib.util.HTML_utils;

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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class HTML_ClickablePane extends JTextPane implements URLLauncher {

  public static final String PROTOCOL_HTTP = "http";
  public static final String PROTOCOL_MAIL = "mailto";

  /*
  private boolean fontChangeInProgress = false;
  private SingleTokenArbiter arbiter = new SingleTokenArbiter();
  private DocumentListener documentListener;
   */

  private static Hashtable registeredLunchersHT;
  private Hashtable registeredLocalLunchersHT;
  private Hashtable registeredLocalLuncherActionsHT;
  private String oldToolTip;

  private static URL defaultBase;

  private Component rendererContainer;

  public HTML_ClickablePane(HTMLEditorKit editorKit) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "HTML_ClickablePane()");
    setContentType("text/html");
    if (editorKit != null)
      setEditorKit(editorKit);
    setText("<html><body face="+HTML_utils.DEFAULT_FONTS_QUOTED+"> </body></html>");
    init(true);
    if (trace != null) trace.exit(HTML_ClickablePane.class);
  }
  /** Creates new HTML_ClickablePane */
  private HTML_ClickablePane(URL startURL, HTMLEditorKit editorKit) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "HTML_ClickablePane()");
    if (trace != null) trace.args(startURL);
    setContentType("text/html");
    if (editorKit != null)
      setEditorKit(editorKit);
    if (startURL != null) {
      setPage(startURL);
    }
    init(false);
    if (trace != null) trace.exit(HTML_ClickablePane.class);
  }
  /** Creates new HTML_ClickablePane */
  public HTML_ClickablePane(String htmlText) {
    this(htmlText, null);
  }
  public HTML_ClickablePane(String htmlText, HTMLEditorKit editorKit) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "HTML_ClickablePane()");
    if (trace != null) trace.args(htmlText);
    setContentType("text/html");
    if (editorKit != null)
      setEditorKit(editorKit);
    if (htmlText.startsWith("<html>") && htmlText.endsWith("</html>")) {
      htmlText = htmlText.substring(6, htmlText.length()-7);
    }
    setText("<html><body face="+HTML_utils.DEFAULT_FONTS_QUOTED+">" + htmlText + "</body></html>");
    init(true);
    if (trace != null) trace.exit(HTML_ClickablePane.class);
  }


  /**
   * To fetch a HTML_ClickablePane from a URL, use this method specifying maximum time you are willing to wait...
   * @param url is the address of page to be loaded.
   * @param maxWaitMillis Maximum number of milliseconds caller is willing to wait for the url to load, 0 for unlimited
   * @return loaded HTML_ClickablePane or null if load failed or takes too long
   */
  public static HTML_ClickablePane createNewAndLoaded(final URL url, int maxWaitMillis) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "createNewAndLoaded(final URL url, int maxWaitMillis)");
    if (trace != null) trace.args(url);
    if (trace != null) trace.args(maxWaitMillis);
    HTML_ClickablePane pane = null;
    final Object[] returnBuffer = new Object[1];
    Thread fetcher = new Thread("HTML_ClickablePane URL fetcher 1") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
        HTML_ClickablePane returnPane;
        try {
          returnPane = new HTML_ClickablePane(url, null);
          returnPane.setBorder(new EmptyBorder(0, 0, 0, 0));
          returnPane.setBackground(Color.white);
          returnBuffer[0] = returnPane;
        } catch (Throwable t) {
        }
        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    fetcher.start();
    try {
      fetcher.join(maxWaitMillis);
    } catch (InterruptedException e) {
    }
    pane = (HTML_ClickablePane) returnBuffer[0];
    if (trace != null) trace.exit(HTML_ClickablePane.class, pane);
    return pane;
  }

  /**
   * To fetch a HTML_ClickablePane from a URL, use this method to immediately
   * return a component that will become loaded in a background thread.
   * @param url is the address of page to be loaded.
   * @return empty or currently loading HTML_ClickablePane
   */
  public static HTML_ClickablePane createNewAndLoading(final URL url) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "createNewAndLoading(final URL url)");
    if (trace != null) trace.args(url);
    HTML_ClickablePane pane = null;
    try {
      pane = new HTML_ClickablePane((URL) null, null);
    } catch (IOException e) {
    }
    final HTML_ClickablePane returnPane = pane;
    if (returnPane != null) {
      returnPane.setBorder(new EmptyBorder(0, 0, 0, 0));
      returnPane.setBackground(Color.white);
      Thread fetcher = new Thread("HTML_ClickablePane URL fetcher 2") {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
          try {
            returnPane.setPage(url);
          } catch (Throwable t) {
          }
          if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
          if (trace != null) trace.exit(getClass());
          if (trace != null) trace.clear();
        }
      };
      fetcher.setDaemon(true);
      fetcher.start();
    }
    if (trace != null) trace.exit(HTML_ClickablePane.class, returnPane);
    return returnPane;
  }

  /**
   * Initialize the pane and set base optionally.
   */
  private void init(boolean setBase) {
    setEditable(false);
    addHyperlinkListener(new HyperListener());
    // set document base to work within a JAR
    if (setBase) {
      setBaseToDefault((HTMLDocument) getDocument());
    }
    /*
    documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        initFont(e);
      }
      public void insertUpdate(DocumentEvent e) {
        initFont(e);
      }
      public void removeUpdate(DocumentEvent e) {
        //initFont(e);
      }
      private void initFont(DocumentEvent e) {
        if (!fontChangeInProgress) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              HTML_ClickablePane.this.initFont();
            }
          });
        }
      }
    };
     */
    initFont();
  }

  /**
   * Initialize default fonts.
   */
  public void initFont() {
    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
    ((HTMLDocument) getDocument()).getStyleSheet().addRule(bodyRule);
    /*
    Object token = new Object();
    if (arbiter.putToken(arbiter, token)) {
      fontChangeInProgress = true;
      getDocument().removeDocumentListener(documentListener);
      try {
        EditorKit ek = getEditorKit();
        if (ek instanceof HTMLEditorKit) {
          HTMLEditorKit hek = (HTMLEditorKit) ek;
          MutableAttributeSet set = hek.getInputAttributes();
          Document doc = getDocument();
          if (doc instanceof HTMLDocument) {
            HTMLDocument hdoc = (HTMLDocument) doc;
            synchronized (getTreeLock()) {
              StyleConstants.setFontFamily(set, MiscGui.getDefaultFontName());
              StyleConstants.setFontSize(set, 12);
              hdoc.setCharacterAttributes(0, hdoc.getLength(), set, true);
            }
          }
        }
      } catch (Throwable t) {
      }
      getDocument().addDocumentListener(documentListener);
      fontChangeInProgress = false;
      arbiter.removeToken(arbiter, token);
    }
     */
  }

  public Component getRendererContainer() {
    return rendererContainer;
  }
  public void setRendererContainer(Component rendererContainer) {
    this.rendererContainer = rendererContainer;
  }
  /**
   * Launcher is invoked with URL when key matches URL type upon hyper link click.
   */
  public static void setRegisteredGlobalLauncher(String protocol, URLLauncher launcher) {
    if (registeredLunchersHT == null) registeredLunchersHT = new Hashtable();
    registeredLunchersHT.put(protocol.toLowerCase(), launcher);
  }
  public void setRegisteredLocalLauncher(String protocol, URLLauncher launcher) {
    if (registeredLocalLunchersHT == null) registeredLocalLunchersHT = new Hashtable();
    registeredLocalLunchersHT.put(protocol.toLowerCase(), launcher);
  }
  public void setRegisteredLocalLauncher(URLLauncher launcher, String actionPath) {
    if (registeredLocalLuncherActionsHT == null) registeredLocalLuncherActionsHT = new Hashtable();
    registeredLocalLuncherActionsHT.put(actionPath.toLowerCase(), launcher);
  }

  private URLLauncher getRegisteredLauncher(URL url) {
    URLLauncher launcher = null;
    String query = url.getQuery();
    if (query != null && query.indexOf("target=this") >= 0) {
      launcher = this;
    } else if (query != null && query.indexOf("target=new") >= 0) {
      launcher = null; // null launcher means that BrowserLauncher will try system specific invokation
    } else {
      String protocol = url.getProtocol();
      if (launcher == null && registeredLocalLunchersHT != null) {
        launcher = (URLLauncher) registeredLocalLunchersHT.get(protocol.toLowerCase());
      }
      if (launcher == null && registeredLunchersHT != null) {
        launcher = (URLLauncher) registeredLunchersHT.get(protocol.toLowerCase());
      }
      if (launcher == null && registeredLocalLuncherActionsHT != null && url.getHost().toLowerCase().equals("localhost")) {
        String path = url.getPath();
        if (path.startsWith("\\") || path.startsWith("/"))
          path = path.substring(1);
        int endRootDir = path.indexOf('\\');
        if (endRootDir == -1) endRootDir = path.indexOf('/');
        if (endRootDir > 0)
          path = path.substring(0, endRootDir);
        launcher = (URLLauncher) registeredLocalLuncherActionsHT.get(path.toLowerCase());
      }
    }
    return launcher;
  }

  private void newClick(final URL url) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ClickablePane.class, "newClick(URL url)");
    if (trace != null) trace.args(url);
    Thread launcher = new Thread("Click Launcher") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
        try {
          URLLauncher launcher = getRegisteredLauncher(url);
          if (launcher != null) {
            launcher.openURL(url, HTML_ClickablePane.this);
          } else {
            BrowserLauncher.openURL(url.toExternalForm());
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(HTML_ClickablePane.class, 100, t);
        }
        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    launcher.setDaemon(true);
    launcher.start();
    if (trace != null) trace.exit(HTML_ClickablePane.class);
  }

  public void openURL(URL url, Component invoker) {
    try {
      setPage(url);
    } catch (IOException e) {
    }
  }

  public void paint(Graphics g) {
    try { // lets do super.paint(g) in try-catch to avoid some BoxView exception
      MiscGui.setPaintPrefs(g);
      super.paint(g);
    } catch (Throwable t) {
    }
  }

  public static URL getDefaultBase() {
    URL base = null;
    if (defaultBase != null) {
      base = defaultBase;
    } else {
      try {
        URL url = URLs.getResourceURL("License.txt");
        String path = url.getPath().substring(0, url.getPath().lastIndexOf("/")+1);	// will work from inside jar
        base = new URL(url.getProtocol(),url.getHost(),url.getPort(), path);
        defaultBase = base;
      } catch (Throwable t) { }
    }
    return base;
  }

  /**
   * Set default base of the document.
   */
  public static void setBaseToDefault(HTMLDocument doc) {
    try {
      URL base = getDefaultBase();
      doc.setBase(base);
    } catch (Throwable t) { }
  }

  public void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
  }

  private class HyperListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent e) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "hyperlinkUpdate(HyperlinkEvent e)");
      if (trace != null) trace.args(e);
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        URL url = e.getURL();
        newClick(url);
      }
      if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        oldToolTip = HTML_ClickablePane.this.getToolTipText();
        String toolTip = "" + e.getDescription();
        if (toolTip.length() > 100) {
          toolTip = toolTip.substring(0, 100) + "...";
        }
        HTML_ClickablePane.this.setToolTipText(toolTip);
      }
      if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
        HTML_ClickablePane.this.setToolTipText(oldToolTip);
      }
      if (trace != null) trace.exit(getClass());
    }
  }
//
//  /**
//   * We need a Graphics object to trick the layout to do its work properly.
//   */
//  private final Graphics2D previewGraphics;
//
//  /**
//   * A panel with a BorderLayout. The center will be occupied by a no text
//   * label. The PAGE_START area will by used by a preview label.
//   */
//  private final JPanel previewPanel;
//
//  /**
//   * The preferred width. If less than 1, no preferred width is taken into
//   * account.
//   */
//  private int preferredWidthLimit = -1;
//
//  /**
//   * flag to avoid recursive calls to limited size computation
//   */
//  private boolean computingLimitedSize = false;
//
//  /**
//   * Initialize global variables
//   */
//  {
//    previewGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
//    previewPanel = new JPanel(new BorderLayout());
//  }
//
//  private Dimension computeLimitedSize(int widthLimit) {
//    previewPanel.removeAll();
//    previewPanel.add(this, BorderLayout.PAGE_START);
//    previewPanel.add(new JLabel());
//    
//    Dimension initialPreferred = previewPanel.getPreferredSize();
//    previewPanel.setSize(widthLimit, 2 * initialPreferred.height + initialPreferred.height * initialPreferred.width / widthLimit);
//
//    previewPanel.getLayout().layoutContainer(previewPanel);
//    previewPanel.paint(previewGraphics);
//
//    previewPanel.getLayout().layoutContainer(previewPanel);
//
//    return getSize();
//  }
//
//  /**
//   * If preferredWidthLimit is set to a value > 0, a size is computed with a
//   * width that is lees or equal to the preferred width and that is sufficient
//   * to contain the renderer's text. If preferredWidthLimit less than 1, the
//   * normal preferred size is returned.
//   */
//  public final Dimension getPreferredSize() {
//    Dimension preferred = super.getPreferredSize();
////    if (!computingLimitedSize && preferredWidthLimit > 0 && preferredWidthLimit < preferred.width) {
////      computingLimitedSize = true;
////      try {
////        preferred = computeLimitedSize(preferredWidthLimit);
////      } catch (Throwable t) {
////      }
////      computingLimitedSize = false;
////    }
//    return preferred;
//  }
//  public void setPreferredWidthLimit(int prefWidthLimit) {
//    preferredWidthLimit = prefWidthLimit;
//  }

  /**
   * Testing of HTML renderer on certain pages.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: <program> [<html-file-name>|<url>] [URL|file]");
      System.exit(-1);
    }
    try {
      JFrame f = new JFrame("Test of HTML renderer on page " + args[0]);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      boolean isURL = true;
      if (args.length >= 2) {
        if (args[1].equalsIgnoreCase("URL") || args[1].equalsIgnoreCase("file"))
          isURL = args[1].equalsIgnoreCase("URL");
      }
      if (isURL) {
        URL url = ClassLoader.getSystemResource(args[0]);
        if (url == null)
          url = new URL(args[0]);
        f.getContentPane().add(new JScrollPane(new HTML_ClickablePane(url, null)), "Center");
      } else {
        FileInputStream fin = new FileInputStream(args[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b = 0;
        while ((b = fin.read()) >= 0) {
          bout.write(b);
        }
        bout.flush();
        fin.close();
        f.getContentPane().add(new JScrollPane(new HTML_ClickablePane(new String(bout.toByteArray()))), "Center");
      }
      f.setSize(300, 300);
      f.setVisible(true);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(-2);
    }
  }

}