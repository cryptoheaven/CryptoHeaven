/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.print;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.*;

import com.CH_gui.gui.*;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.11 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class PrintPreview extends JDialog {

  protected static PageFormat pageFormat;
  protected static PrinterJob printerJob;

  protected int m_wPage;
  protected int m_hPage;
  protected Printable m_target;
  protected JComboBox m_cbScale;
  private PreviewContainer m_preview;
  //protected int numOfPages;

  private static String PRINT_JOB_NAME = URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" printing job";
  private static int printJobNumber = 1;

  public PrintPreview(Printable target, String title, Dialog parent) {
    super(parent, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PrintPreview.class, "PrintPreview(Printable target, String title, Dialog parent)");
    initialize(target, parent);
    if (trace != null) trace.exit(PrintPreview.class);
  }
  public PrintPreview(Printable target, String title, Frame parent) {
    super(parent, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PrintPreview.class, "PrintPreview(Printable target, String title, Frame parent)");
    initialize(target, parent);
    if (trace != null) trace.exit(PrintPreview.class);
  }

  private void initialize(Printable target, Component parent) {
    setSize(695, 485);
    if (parent != null) 
      com.CH_gui.util.MiscGui.setSuggestedWindowLocation(parent, this);

    m_target = target;

    if (printerJob == null) {
      printerJob = PrinterJob.getPrinterJob();
      printerJob.setJobName(PRINT_JOB_NAME+(printJobNumber<=1?"":" "+printJobNumber));
    }

    if (pageFormat == null) {
      pageFormat = printerJob.defaultPage();
      // set margines to 0.5" 
      Paper paper = pageFormat.getPaper();
      paper.setImageableArea(36, 36, paper.getWidth()-72, paper.getHeight()-72);
      pageFormat.setPaper(paper);
    }

    // validate new margines
    printerJob.validatePage(pageFormat);

    JToolBar tb = new JToolBar(SwingConstants.HORIZONTAL);
    tb.setFloatable(false);

    JButton bt = new JMyButton("Print");
    ActionListener lst = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Thread th = new ThreadTraced("Print Thread") {
          public void runTraced() {
            try {
              // Use default printer, no dialog
              setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
              printDialog();
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
              dispose();
            } catch (PrinterException ex) {
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
              ex.printStackTrace();
              System.err.println("Printing error: "+ex.toString());
            }
          }
        };
        th.setDaemon(true);
        th.start();
      }
    };
    bt.addActionListener(lst);
    tb.add(bt);

    bt = new JMyButton("Page Setup");
    lst = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Thread th = new ThreadTraced("Page Setup Thread") {
          public void runTraced() {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (printerJob == null) {
              printerJob = PrinterJob.getPrinterJob();
              printerJob.setJobName(PRINT_JOB_NAME+(printJobNumber<=1?"":" "+printJobNumber));
              //printerJob.setPageable(new PageableDoc());
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            pageFormat = printerJob.pageDialog(pageFormat);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            initLayoutPreview(m_target, m_preview, pageFormat);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        };
        th.setDaemon(true);
        th.start();
      }
    };
    bt.addActionListener(lst);
    tb.add(bt);

    bt = new JMyButton("Close");
    lst = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    };
    bt.addActionListener(lst);
    tb.add(bt);

    String[] scales = { "10 %", "25 %", "50 %", "100 %" };
    m_cbScale = new JMyComboBox(scales);
    m_cbScale.setSelectedIndex(3);
    lst = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Thread th = new ThreadTraced("Print Scale Thread") {
          public void runTraced() {
            int scale = getSelectedScale();
            if (scale > 0) {
              int w = (int)(m_wPage*scale/100);
              int h = (int)(m_hPage*scale/100);
              Component[] comps = m_preview.getComponents();
              for (int k=0; k<comps.length; k++) {
                if (!(comps[k] instanceof PagePreview))
                  continue;
                PagePreview pp = (PagePreview)comps[k];
                pp.setScaledSize(w, h);
              }
              m_preview.doLayout();
              m_preview.getParent().getParent().validate();
            }
          }
        };
        th.setDaemon(true);
        th.start();
      }
    };
    m_cbScale.addActionListener(lst);
    m_cbScale.setMaximumSize(m_cbScale.getPreferredSize());
    m_cbScale.setEditable(true);

    tb.addSeparator();
    tb.add(m_cbScale);

    getContentPane().add(tb, BorderLayout.NORTH);

    // check that java version is at least 1.4
    String verStr = System.getProperty("java.version");
    int jvmMajor = 0;
    int jvmMinor = 0;
    java.util.StringTokenizer st = new java.util.StringTokenizer(verStr, ". _");
    if (st.hasMoreTokens()) jvmMajor = Integer.parseInt(st.nextToken());
    if (st.hasMoreTokens()) jvmMinor = Integer.parseInt(st.nextToken());
    boolean isJvmVersionOk = jvmMajor > 1 || (jvmMajor == 1 && (jvmMinor >= 4));

    if (isJvmVersionOk) {
      m_preview = new PreviewContainer();
      initLayoutPreview(m_target, m_preview, pageFormat);
      JScrollPane ps = new JScrollPane(m_preview);
      getContentPane().add(ps, BorderLayout.CENTER);
    }

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);

    // print available in premium accounts only
    UserRecord myUserRec = FetchedDataCache.getSingleInstance().getUserRecord();
    if (myUserRec.isDemoAccount()) {
      String urlStr = "\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + myUserRec.userId + "\"";
      String htmlText = "<html>Print functionality is not available for demo accounts.  To upgrade your user account to a Premium Account click here <a href="+urlStr+">"+URLs.get(URLs.SIGNUP_PAGE)+"</a>. <p>Thank You.</html>";
      MessageDialog.showWarningDialog(parent, htmlText, "Account Incapable", true);
      dispose();
    } else if (!isJvmVersionOk) {
      String htmlText = "<html>Print functionality is not available in systems running Java(TM) Runtime Environment v1.3 and earlier.  Please install the latest version with Java(TM) Runtime Environment v1.4 or later, or email support@"+URLs.getElements(URLs.DOMAIN_MAIL)[0]+" for additional instructions.";
      MessageDialog.showWarningDialog(parent, htmlText, "Software Incapable", true);
      dispose();
    } else if (m_target instanceof DocumentRenderer) {
      if (((DocumentRenderer) m_target).scaledDownWarning)
        com.CH_gui.util.MessageDialog.showInfoDialog(this, "Original document formatting is wider than page size.  Document was scaled down to fit on the page.  You may consider reducing margins or changing page orientation.", "Document scaled", false);
    }
  }

  private void initLayoutPreview(Printable target, PreviewContainer preview, PageFormat pageFormat) {
    preview.removeAll();
    if (pageFormat.getHeight()==0 || pageFormat.getWidth()==0) {
      System.err.println("Unable to determine default page size");
      return;
    }
    m_wPage = (int)(pageFormat.getWidth());
    m_hPage = (int)(pageFormat.getHeight());
    int scale = getSelectedScale();
    int w = (int)(m_wPage*scale/100);
    int h = (int)(m_hPage*scale/100);
    int pageIndex = 0;

    try {
      while (true) {
        BufferedImage img = new BufferedImage(m_wPage,m_hPage, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, m_wPage, m_hPage);
        if (target.print(g, pageFormat, pageIndex) != Printable.PAGE_EXISTS)
          break;
        PagePreview pp = new PagePreview(w, h, img);
        preview.add(pp);
        pageIndex++;
        // don't print more than 100 pages to prevent OutOfMemoryErrors
        if (pageIndex >= 100)
          break;
      }
    } catch (PrinterException e) {
      e.printStackTrace();
      System.err.println("Printing error: "+e.toString());
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      System.err.println("Printing error: "+e.toString());
    }
    preview.revalidate();
    preview.repaint();
    //numOfPages = pageIndex;
    //System.out.println("numOfPages="+numOfPages);
  }

  private int getSelectedScale() {
    String str = m_cbScale.getSelectedItem().toString();
    if (str.endsWith("%"))
      str = str.substring(0, str.length()-1);
    str = str.trim();
    int scale = 25;
    try { scale = Integer.parseInt(str); }
    catch (NumberFormatException ex) { return -1; }
    return scale;
  }

  /**
   * A private method, printDialog(), displays the print dialog and initiates
   * printing in response to user input.
   */
  private void printDialog() throws PrinterException {
    if (printerJob == null) {
      printerJob = PrinterJob.getPrinterJob();
      printerJob.setJobName(PRINT_JOB_NAME+(printJobNumber<=1?"":" "+printJobNumber));
      //printerJob.setPageable(new PageableDoc());
    }
    printerJob.setPrintable(m_target, pageFormat);
    if (printerJob.printDialog()) {
      printerJob.print();
      printJobNumber ++;
    }
  }


  private static class PreviewContainer extends JPanel {

    protected int H_GAP = 16;
    protected int V_GAP = 10;

    public Dimension getPreferredSize() {
      int n = getComponentCount();
      if (n == 0)
        return new Dimension(H_GAP, V_GAP);
      Component comp = getComponent(0);
      Dimension dc = comp.getPreferredSize();
      int w = dc.width;
      int h = dc.height;
      Dimension dp = getParent().getSize();
      int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
      int nRow = n/nCol;
      if (nRow*nCol < n)
        nRow++;
      int ww = nCol*(w+H_GAP) + H_GAP;
      int hh = nRow*(h+V_GAP) + V_GAP;
      Insets ins = getInsets();
      return new Dimension(ww+ins.left+ins.right, hh+ins.top+ins.bottom);
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void doLayout() {
      Insets ins = getInsets();
      int x = ins.left + H_GAP;
      int y = ins.top + V_GAP;
      int n = getComponentCount();
      if (n == 0)
        return;
      Component comp = getComponent(0);
      Dimension dc = comp.getPreferredSize();
      int w = dc.width;
      int h = dc.height;
      Dimension dp = getParent().getSize();
      int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
      int nRow = n/nCol;
      if (nRow*nCol < n)
        nRow++;
      int index = 0;
      for (int k = 0; k<nRow; k++) {
        for (int m = 0; m<nCol; m++) {
          if (index >= n)
            return;
          comp = getComponent(index++);
          comp.setBounds(x, y, w, h);
          x += w+H_GAP;
        }
        y += h+V_GAP;
        x = ins.left + H_GAP;
      }
    }
  } // end class PreviewContainer


  private static class PagePreview extends JPanel {

    protected int m_w;
    protected int m_h;
    protected Image m_source;
    protected Image m_img;

    public PagePreview(int w, int h, Image source) {
      m_w = w;
      m_h = h;
      m_source= source;
      m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
      m_img.flush();
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
    }

    public void setScaledSize(int w, int h) {
      m_w = w;
      m_h = h;
      m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
      repaint();
    }

    public Dimension getPreferredSize() {
      Insets ins = getInsets();
      return new Dimension(m_w+ins.left+ins.right, m_h+ins.top+ins.bottom);
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      g.drawImage(m_img, 0, 0, this);
      paintBorder(g);
    }
  } // end class PagePreview

  /*
  private class PageableDoc implements Pageable {
    public int getNumberOfPages() {
      return numOfPages;
    }
    public PageFormat getPageFormat(int pageIndex) {
      return pageFormat;
    }
    public Printable getPrintable(int pageIndex) {
      return m_target;
    }
  } // end class PageableDoc
   */

}