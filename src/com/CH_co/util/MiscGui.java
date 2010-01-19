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

package com.CH_co.util;

import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_gui.actionGui.JActionFrame;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class MiscGui extends Object {

  // Used for gui suppression.
  private static boolean guiSuppressed = false;
  private static boolean guiSuppressedMsgDialogs = false;

  private static boolean isAntiAliasingEnabled = false;
  private static boolean isAntiAliasingCapable = false;

  private static boolean isSmallScreen = false;

  private static Hashtable fileTypeNamesHT;   // keys are file extensions
  private static Hashtable fileTypeIconsHT;   // keys are file types

  private static Hashtable cachedFileTypesHT; // keys are file extensions
  private static Hashtable cachedFileIconsHT; // keys are file extensions

  public static final String ANTIALIASING_ENABLEMENT_PROPERTY = "antialiasingEnabled";

  // This array contains arrays of extensions, divided out by type -- the type name is the first element
  private static String[][] extensionsArray = new String[][] { 
                              { FolderRecord.getFolderType(FolderRecord.FILE_FOLDER), ""+ImageNums.FLD_CLOSED16 },
                              { "Shared Folder", ""+ImageNums.FLD_CLOSED_SHARED16 },
//                              { "Ace archive", ""+ImageNums.FILE_TYPE_ACE, "ace", "c00", "c01", "c02", "c03", "c04", "c05", "c06", "c07", "c08", "c09", "c10", "c11", "c12", "c13", "c14", "c15", "c16", "c17", "c18", "c19", "c20", "c21", "c22", "c23", "c24", "c25", "c26", "c27", "c28", "c29" },
//                              { "Active Server Document", ""+ImageNums.FILE_TYPE_ASP, "asp" },
//                              { "Adobe Acrobat Document", ""+ImageNums.FILE_TYPE_PDF, "pdf" },
//                              { "Adobe Photoshop Image", ""+ImageNums.FILE_TYPE_PSD, "psd" },
//                              { "Amiga audio file", ""+ImageNums.FILE_TYPE_IFF, "iff" },
//                              { "Application Extension", ""+ImageNums.FILE_TYPE_DLL, "dll" },
//                              { "Application", ""+ImageNums.FILE_TYPE_EXE, "exe" },
//                              { "Bitmap Image", ""+ImageNums.FILE_TYPE_BMP, "bmp" },
//                              { "CLASS File", ""+ImageNums.FILE_TYPE_CLASS, "class" },
//                              { "Configuration Settings", ""+ImageNums.FILE_TYPE_INI, "ini" },
//                              { "EPS File", ""+ImageNums.FILE_TYPE_PSD, "eps" },
//                              { "Flash Movie", ""+ImageNums.FILE_TYPE_FLA, "fla" },
//                              { "GIF Image", ""+ImageNums.FILE_TYPE_GIF, "gif" },
//                              { "HTML File", ""+ImageNums.FILE_TYPE_HTML, "html", "htm" },
//                              { "ICB File", ""+ImageNums.FILE_TYPE_PSD, "icb" },
//                              { "JAR File", ""+ImageNums.FILE_TYPE_JAR, "jar" },
//                              { "Java File", ""+ImageNums.FILE_TYPE_JAVA, "java" },
//                              { "JPEG Image", ""+ImageNums.FILE_TYPE_JPG, "jpg", "jpe", "jpeg" },
//                              { "JSP File", ""+ImageNums.FILE_TYPE_JSP, "jsp" },
//                              { "M3U File", ""+ImageNums.FILE_TYPE_WAV, "m3u" },
//                              { "Microsoft Access Application", ""+ImageNums.FILE_TYPE_MDB, "mdb" },
//                              { "Microsoft Excel Worksheet", ""+ImageNums.FILE_TYPE_XLS, "xls" },
//                              { "Microsoft Office Binder", ""+ImageNums.FILE_TYPE_OBD, "obd" },
//                              { "Microsoft PowerPoint Presentation", ""+ImageNums.FILE_TYPE_PPT, "ppt" },
//                              { "Microsoft Word Document", ""+ImageNums.FILE_TYPE_DOC, "doc", "rtf" },
//                              { "MIDI Sequence", ""+ImageNums.FILE_TYPE_WAV, "mid", "midi" },
//                              { "Movie File (MPEG)", ""+ImageNums.FILE_TYPE_WAV, "mp2" },
//                              { "Movie File", ""+ImageNums.FILE_TYPE_MPEG, "mpeg", "mpg" },
//                              { "MP3 Format Sound", ""+ImageNums.FILE_TYPE_WAV, "mp3" },
//                              { "MS-DOS Application", ""+ImageNums.FILE_TYPE_EXE, "com" },
//                              { "MS-DOS Batch File", ""+ImageNums.FILE_TYPE_BAT, "bat" },
//                              { "Netscape Hypertext Document", ""+ImageNums.FILE_TYPE_SHTML, "shtml", "shtm" },
//                              { "Outlook Express Mail Message", ""+ImageNums.FILE_TYPE_EML, "eml" },
//                              { "Paint Shop Pro 6 Image", ""+ImageNums.FILE_TYPE_PSP, "psp", "rle" },
//                              { "PCX Image", ""+ImageNums.FILE_TYPE_PCX, "pcx" },
//                              { "PGP Armored File", ""+ImageNums.FILE_TYPE_ASC, "asc" },
//                              { "PGP Private Keyring", ""+ImageNums.FILE_TYPE_SKR, "skr" },
//                              { "PGP Public Keyring", ""+ImageNums.FILE_TYPE_PKR, "pkr" },
//                              { "Photo CD Image", ""+ImageNums.FILE_TYPE_PCD, "pcd" },
//                              { "PICT Image", ""+ImageNums.FILE_TYPE_PIC, "pic", "pct" },
//                              { "PNG Image", ""+ImageNums.FILE_TYPE_PNG, "png" },
//                              { "PXR File", ""+ImageNums.FILE_TYPE_PSD, "pxr" },
//                              { "QuickTime Movie", ""+ImageNums.FILE_TYPE_MOV, "mov" },
//                              { "Rar archive", ""+ImageNums.FILE_TYPE_RAR, "rar", "r00", "r01", "r02", "r03", "r04", "r05", "r06", "r07", "r08", "r09", "r10", "r11", "r12", "r13", "r14", "r15", "r16", "r17", "r18", "r19", "r20", "r21", "r22", "r23", "r24", "r25", "r26", "r27", "r28", "r29" },
//                              { "RAW File", ""+ImageNums.FILE_TYPE_PSD, "raw" },
//                              { "Shockwave Flash Object", ""+ImageNums.FILE_TYPE_SWF, "swf" },
//                              { "Text Document", ""+ImageNums.FILE_TYPE_TXT, "txt", "log" },
//                              { "TGA File", ""+ImageNums.FILE_TYPE_PSD, "tga" },
//                              { "TIF Image Document", ""+ImageNums.FILE_TYPE_TIF, "tif", "tiff" },
//                              { "VDA File", ""+ImageNums.FILE_TYPE_PSD, "vda" },
//                              { "Video Clip", ""+ImageNums.FILE_TYPE_WAV, "avi" },
//                              { "VST File", ""+ImageNums.FILE_TYPE_PSD, "vst" },
//                              { "Wave Sound", ""+ImageNums.FILE_TYPE_WAV, "wav" },
//                              { "Winamp media file", ""+ImageNums.FILE_TYPE_MP1, "mp1" },
//                              { "Windows Media Audio/Video File", ""+ImageNums.FILE_TYPE_WAV, "asf", "asx", "wma", "wmv" },
//                              { "WinZip File", ""+ImageNums.FILE_TYPE_ZIP, "zip", "tar", "gz" },
//                              { "XML Document", ""+ImageNums.FILE_TYPE_XML, "xml" },
//                              { "XSL Stylesheet", ""+ImageNums.FILE_TYPE_XSL, "xsl" },
//                              { "KOKO File", ""+ImageNums.FILE_TYPE_XSL, "koko" },
//                              { "Havostomo Winamp", ""+ImageNums.FILE_TYPE_MP1, "havo" },
                              };

  /*
  private static String defaultFontName = null;
  private static Font defaultFont = null;
   */

  static {
    //if (RenderingHints.VALUE_ANTIALIAS_DEFAULT.equals(RenderingHints.VALUE_ANTIALIAS_OFF)) {
      String version = System.getProperty("java.version");
      StringTokenizer st = new StringTokenizer(version, ".");
      int majorVersion = 0;
      int minorVersion = 0;
      if (st.hasMoreTokens()) {
        try {
          majorVersion = Integer.parseInt(st.nextToken());
        } catch (Throwable t) {
        }
      }
      if (st.hasMoreTokens()) {
        try {
          minorVersion = Integer.parseInt(st.nextToken());
        } catch (Throwable t) {
        }
      }
      isAntiAliasingCapable = majorVersion >= 2 || (majorVersion == 1 && minorVersion >= 4);
      isAntiAliasingEnabled = Boolean.valueOf(GlobalProperties.getProperty(ANTIALIASING_ENABLEMENT_PROPERTY, "true")).booleanValue();
    //}
  }

  // Setup the extention hashtables for quick lookup.
  static {
    fileTypeNamesHT = new Hashtable();
    fileTypeIconsHT = new Hashtable();
    for (int i=0; i<extensionsArray.length; i++) {
      String[] typeRow = extensionsArray[i];
      fileTypeIconsHT.put(typeRow[0], typeRow[1]);
      for (int k=2; k<typeRow.length; k++) {
        fileTypeNamesHT.put(typeRow[k].toUpperCase(), typeRow[0]);
      }
    }
    cachedFileIconsHT = new Hashtable();
    cachedFileTypesHT = new Hashtable();
  }


  /**
   * Set a flag to suppress all GUI, put it in Misc and not in MiscGUI because MiscGUI class loads some GUI components
   */
  public static void suppressAllGUI() {
    guiSuppressed = true;
  }
  public static void suppressMsgDialogsGUI(boolean suppressMsgDialogs) {
    guiSuppressedMsgDialogs = suppressMsgDialogs;
  }
  public static boolean isAllGUIsuppressed() {
    return guiSuppressed;
  }
  public static boolean isMsgDialogsGUIsuppressed() {
    return guiSuppressedMsgDialogs;
  }


  private static final JTextComponent.KeyBinding[] defaultBindings = {
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), DefaultEditorKit.pasteAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), DefaultEditorKit.cutAction),
  };

  public static void setPaintPrefs(Graphics g) {
    if (isAntiAliasingCapable && isAntiAliasingEnabled && g instanceof Graphics2D) {
      //UIManager.getLookAndFeelDefaults().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
  }
  public static void setAntiAliasingEnabled(boolean enable) {
    isAntiAliasingEnabled = enable;
  }
  public static boolean isAntiAliasingCapable() {
    return isAntiAliasingCapable;
  }

  public static void setSmallScreen(boolean isSmall) {
    isSmallScreen = isSmall;
  }
  public static boolean isSmallScreen() {
    return isSmallScreen;
  }

  public static void nudge(Component comp, boolean includeSound, boolean includeTitleRoll) {
    nudge(new Component[] { comp }, includeSound, includeTitleRoll);
  }
  public static void nudge(Component[] comps, boolean includeSound, final boolean includeTitleRoll) {
    if (includeSound)
      Sounds.playAsynchronous(Sounds.RING_BELL);
    if (comps != null && comps.length > 0) {
      for (int i=0; i<comps.length; i++) {
        Component comp = comps[i];
        if (comp != null) {
          Window w = null;
          if (comp instanceof Window) w = (Window) comp;
          else w = SwingUtilities.windowForComponent(comp);
          if (w != null) {
            if (w instanceof Frame) {
              if (((Frame)w).getState() == Frame.ICONIFIED)
                ((Frame)w).setState(Frame.NORMAL);
            }
            final Window window = w;
            Thread th = new ThreadTraced("Nudger") {
              public void runTraced() {
                if (window.isShowing()) {
                  Point p = window.getLocation();
                  double d = 0;
                  double magnitude = 4.0;
                  for (int i=0; i<30; i++) {
                    d = d+((double)i)/10.0;
                    double x = p.x+magnitude*Math.sin(2*Math.PI*d);
                    double y = p.y-magnitude*Math.cos(2*Math.PI*d);
                    window.setLocation((int) x, (int) y);
                    try { Thread.sleep(15); } catch (InterruptedException ex) { }
                  }
                  window.setLocation(p);
                }
                try { Thread.sleep(50); } catch (InterruptedException ex) { }
                if (includeTitleRoll && window instanceof JActionFrame)
                  ((JActionFrame) window).triggerVisualUpdateNotificationRoll("  *ring*");
              }
            };
            th.setDaemon(true);
            th.start();
          }
        }
      }
    }
  }

  /*
  static {
    String[] availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    String[] fontNames = new String[] { "Arial", "Verdana", "Helvetica", "SansSerif" };
    Font font = null;
    for (int i=0; i<fontNames.length; i++) {
      String fontName = fontNames[i];
      for (int k=0; k<availableFontNames.length; k++) {
        String availFontName = availableFontNames[k];
        if (fontName.equalsIgnoreCase(availFontName)) {
          defaultFontName = fontName;
          break;
        }
      }
      if (defaultFontName != null)
        break;
    }
  }
   */

  /** 
   * load default bindings
   */
  public static void initKeyBindings(JTextComponent c) {
    Keymap k = c.getKeymap();
    JTextComponent.loadKeymap(k, defaultBindings, c.getActions());
  }

  /*
  public static Font getDefaultFont() {
    if (defaultFont == null)
      defaultFont = new Font(defaultFontName, Font.PLAIN, 12);
    return defaultFont;
  }
  public static String getDefaultFontName() {
    return defaultFontName;
  }
   */
  /**
   * sets default font
   */
  public static void setDefaultFont(Component c) {
    // no-op
    //c.setFont(getDefaultFont());
  }

  public static void setPlainFont(Component c) {
    Font oldFont = c.getFont();
    if (!oldFont.isPlain())
      c.setFont(oldFont.deriveFont(Font.PLAIN));
  }

  public static void setBoldFont(Component c) {
    Font oldFont = c.getFont();
    if (!oldFont.isBold())
      c.setFont(oldFont.deriveFont(Font.BOLD));
  }


  /**
   * @return array composed of file icon, file type
   */
  public static Object[] getFileIconAndType(String fileName) {
    Object[] info = new Object[2];
    String fileType = null;
    Icon icon = null;

    // get extension
    String ext = null;
    int index = fileName.lastIndexOf('.');
    if (index > 0) {
      ext = fileName.substring(index + 1).toUpperCase();
    }
    info[1] = ext;

    // get type and icon
    if (ext == null || ext.length() == 0) {
      fileType = getFileInternalType(fileName);
      icon = getFileInternalIconForType(fileType);
    } else {
      icon = (Icon) cachedFileIconsHT.get(ext);
      fileType = (String) cachedFileTypesHT.get(ext);
      if (icon == null || fileType == null) {
        Object[] iconAndType = getSystemFileIconAndType(ext);
        icon = (Icon) iconAndType[0];
        fileType = (String) iconAndType[1];
        // if file type not found on the system, use internal file type clasification
        if (fileType == null)
          fileType = getFileInternalType(fileName);
        if (fileType != null)
          cachedFileTypesHT.put(ext, fileType);
        // if icon not found on the system, use internal icons
        if (icon == null)
          icon = getFileInternalIconForType(getFileInternalType(fileName)); // make sure to use internal file type classification when getting internal icon
        if (icon != null)
          cachedFileIconsHT.put(ext, icon);
      }
    }

    info[0] = icon;
    info[1] = fileType;

    return info;
  }

  /** From the static array of extensions and file types, figure out,
    * to which group file's extension belongs
    */
  public static String getFileInternalType(File file) {
    String fileName = null;
    String fileType = null;
    if (file.isFile()) {
      fileName = file.getName();
      fileType = getFileInternalType(fileName);
    }
    else if (file.isDirectory()) {
      fileType = FolderRecord.getFolderType(FolderRecord.FILE_FOLDER);
    }
    return fileType;
  }
  public static String getFileInternalType(String fileName) {
    String fileType = null;
    int index = fileName.lastIndexOf('.');

    /* if no extension or extension is one letter only */
    if (index == -1 || index+1 == fileName.length()) {
      fileType = "File";
    } else {
      String ext = fileName.substring(index + 1).toUpperCase();
      fileType = (String) fileTypeNamesHT.get(ext);
      if (fileType == null)
        fileType = ext + " File";
    }
    return fileType;
  }

  public static Icon getFileInternalIconForType(String fileType) {
    Icon icon = null;
    Object imageOrCode = fileTypeIconsHT.get(fileType);
    if (imageOrCode != null && imageOrCode instanceof Icon) {
      icon = (Icon) imageOrCode;
    } else if (imageOrCode == null || imageOrCode instanceof String) {
      String imageCodeS = (String) imageOrCode;
      if (imageCodeS != null && imageCodeS.length() > 0) {
        int imageCode = Integer.parseInt(imageCodeS);
        icon = Images.get(imageCode);
      }
      // If still no icon, use a default one
      if (icon == null) {
        icon = Images.get(ImageNums.FILE_TYPE_OTHER);
      }
      // cache the icon for later re-use
      fileTypeIconsHT.put(fileType, icon);
    }
    return icon;
  }

  private static Object[] getSystemFileIconAndType(String extension) {
    Icon icon = null;
    String type = null;
    File tmpFile = null;
    // add a '.' if extension does not start with one
    if (!extension.startsWith(".")) {
      extension = "." + extension;
    }
    // create tmp file
    try {
      //System.out.println("Fetching from system for extension " + extension);
      tmpFile = File.createTempFile("tempIconAndTypeExtract", extension);
      // create fileSystemView and get icon for .html files for current OS
      javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView.getFileSystemView();
      try {
        java.lang.reflect.Method m1 = fsv.getClass().getMethod("getSystemIcon", new Class[] { File.class });
        icon = (Icon) m1.invoke(fsv, new Object[] { tmpFile });
      } catch (Throwable t1) {
        //t1.printStackTrace();
      }
      try {
        java.lang.reflect.Method m2 = fsv.getClass().getMethod("getSystemTypeDescription", new Class[] { File.class });
        type = (String) m2.invoke(fsv, new Object[] { tmpFile });
        //System.out.println("system type description : " + tmpFile + " is " + type);
      } catch (Throwable t2) {
        //t2.printStackTrace();
      }
      if (type == null) {
        if (icon != null) {
          String iconName = icon.toString();
          //System.out.println("iconName : " + iconName);
          if (!iconName.startsWith("javax.swing") && !iconName.startsWith("sun.swing") && iconName.indexOf('@') < 0) {
            type = iconName;
          }
        }
      }
      //System.out.println("Fetched from system, icon="+icon+", type="+type);
    } catch (Throwable t) {
      //System.out.println(t.getMessage());
      //t.printStackTrace();
    }
    // delete temporary file
    if (tmpFile != null) tmpFile.delete();
    return new Object[] { icon, type };
  }

  public static JPanel createButtonPanel(JButton[] buttons) {
    /* Add buttons so they are placed on the bottom right corner */
    JPanel buttonPanelBL = new JPanel(new BorderLayout());
    int cols = buttons.length;
    GridLayout grid = new GridLayout(1, cols);
    grid.setHgap(5);
    JPanel buttonPanel = new JPanel(grid);
    EmptyBorder border = new EmptyBorder(5,5,5,5);
    buttonPanel.setBorder(border);

    for (int i=0; i<cols; i++) 
      if (buttons[i] != null)
        buttonPanel.add(buttons[i]);

    buttonPanelBL.add(buttonPanel, BorderLayout.EAST);
    return buttonPanelBL;
  }

  public static void setSuggestedWindowLocation(Component owner, Window child) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getSuggestedWindowLocation(Component owner, Window child)");
    if (trace != null) trace.args(owner, child);

    Dimension parentDimension;
    Point parentLocation;
    if (owner != null) {
      parentDimension = owner.getSize();
      parentLocation = owner.getLocationOnScreen();
    } else {
      parentDimension = getScreenUsableSize(0, 0, child);
      parentLocation = new Point(0, 0);
    }
    Insets parentInsets = getScreenInsets(parentLocation.x, parentLocation.y, owner);

    Dimension childDimension = child.getSize();

    // get North-West corner of centralized figures in parent's coordinate system
    int x = (parentDimension.width - childDimension.width) / 2;
    int y = (parentDimension.height - childDimension.height) / 2;

    // move the coordinates to screen coordinates
    int screenX = parentLocation.x + x;
    int screenY = parentLocation.y + y;

    Point absolutePoint = new Point(screenX+Math.abs(parentInsets.left), screenY+Math.abs(parentInsets.top));
    child.setLocation(absolutePoint);
    adjustSizeAndLocationToFitScreen(child);

    if (trace != null) trace.exit(MiscGui.class);
  }

  private static Dimension getSuggestedWindowSizeToFitScreen(Window w) {
    Point p = w.getLocation();
    Dimension d = getScreenUsableSize(p.x, p.y, w);
    Dimension s = w.getSize();
    return new Dimension(Math.min(s.width, d.width), Math.min(s.height, d.height));
  }

  public static void adjustSizeAndLocationToFitScreen(Window w) {
    Dimension windowDim_orig = w.getSize();
    Dimension windowDim = getSuggestedWindowSizeToFitScreen(w);
    if (windowDim_orig.width != windowDim.width || windowDim_orig.height != windowDim.height)
      w.setSize(windowDim);
    Point p = w.getLocation();
    w.setLocation(adjustLocationToFitScreen(p.x, p.y, w));
  }

  public static Rectangle getScreenBounds(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      screenBounds = gc.getBounds();
    } else {
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }
    return screenBounds;
  }
  public static Insets getScreenInsets(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      screenInsets = toolkit.getScreenInsets(gc);
    } else {
      screenInsets = new Insets(0, 0, 0, 0);
    }
    return screenInsets;
  }
  
  /**
   * Find screen size adjusted for insets;
   * @return
   */
  public static Dimension getScreenUsableSize(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      // If we have GraphicsConfiguration use it to get
      // screen bounds and insets
      screenInsets = toolkit.getScreenInsets(gc);
      screenBounds = gc.getBounds();
    } else {
      // If we don't have GraphicsConfiguration use primary screen
      // and empty insets
      screenInsets = new Insets(0, 0, 0, 0);
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }
    int scrWidth = screenBounds.width - Math.abs(screenInsets.left + screenInsets.right);
    int scrHeight = screenBounds.height - Math.abs(screenInsets.top + screenInsets.bottom);
    Dimension screenSize = new Dimension(scrWidth, scrHeight);
    return screenSize;
  }
  
  /**
     * Returns an point which has been adjusted to take into account of the 
     * desktop bounds, taskbar and multi-monitor configuration.
     * <p>
     * This adustment code is from JPopupMenu.adjustPopupLocationToFitScreen()
     */
  private static Point adjustLocationToFitScreen(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }

    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }

    if (gc != null) {
      // If we have GraphicsConfiguration use it to get
      // screen bounds and insets
      screenInsets = toolkit.getScreenInsets(gc);
      screenBounds = gc.getBounds();
    } else {
      // If we don't have GraphicsConfiguration use primary screen
      // and empty insets
      screenInsets = new Insets(0, 0, 0, 0);
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }

    int scrWidth = screenBounds.width - Math.abs(screenInsets.left + screenInsets.right);
    int scrHeight = screenBounds.height - Math.abs(screenInsets.top + screenInsets.bottom);

    Dimension size = invoker.getSize();

    if ((p.x + size.width) > screenBounds.x + scrWidth) {
      p.x = screenBounds.x + scrWidth - size.width;
    }

    if ((p.y + size.height) > screenBounds.y + scrHeight) {
      p.y = screenBounds.y + scrHeight - size.height;
    }

    /* Change is made to the desired (X,Y) values, when the
       Component is too tall OR too wide for the screen */
    if (p.x < screenBounds.x) {
      p.x = screenBounds.x;
    }
    if (p.y < screenBounds.y) {
      p.y = screenBounds.y;
    }

    return p;
  }

  private static Point lastSuggestedSpreadedLocation;
  public static Point getSuggestedSpreadedWindowLocation(Component child) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getSuggestedSpreadedWindowLocation(Component child)");
    if (trace != null) trace.args(child);

    Point p = child.getLocation();
    Rectangle parentBounds = getScreenBounds(p.x, p.y, child);

    Dimension childDimension = child.getSize();

    Point absolutePoint = null;
    if (lastSuggestedSpreadedLocation == null) {
      absolutePoint = new Point(parentBounds.x, parentBounds.y);
    } else {
      if (lastSuggestedSpreadedLocation.x + childDimension.width + 30 > parentBounds.width ||
          lastSuggestedSpreadedLocation.y + childDimension.height + 30 > parentBounds.height)
        absolutePoint = new Point(parentBounds.x, parentBounds.y);
      else
        absolutePoint = new Point(lastSuggestedSpreadedLocation.x + 30, lastSuggestedSpreadedLocation.y + 30);
    }
    lastSuggestedSpreadedLocation = absolutePoint;

    if (trace != null) trace.exit(MiscGui.class, absolutePoint);
    return absolutePoint;
  }

  /**
   * Removes all components from the container 2 levels down.
   * Also removes all components from given conteiner's containers.
   */
  /*
  public static void removeAllComponents(Container cont) {
    Component[] components = cont.getComponents();

    Component comp;

    for (int i=0; i<components.length; i++) {
      comp = components[i];

      if (comp != null) {
        if (comp instanceof Container) {
          removeAllComponents((Container) comp);
        }

        comp.transferFocus();
        cont.remove(comp);
      }
    }
  }
   */


  /**
   * Removes all components in the tree and their listeners.
   * Called to aid in garbage collection.
   */
  public static void removeAllComponentsAndListeners(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "removeAllComponentsAndListeners(Component c)");
    if (trace != null) trace.args(c);
    try {
      removeAllListenersRecur(c);
    } catch (Throwable t) {
      if (trace != null) trace.data(200, "small issue while removing listeners");
    }
    try {
      removeAllComponentsRecur(c);
    } catch (Throwable t) {
      if (trace != null) trace.data(300, "small issue while removing components");
    }
    if (trace != null) trace.exit(MiscGui.class);
  }

  private static void removeAllListenersRecur(Component c) {
    try {
      if (c != null) {
        // special treatment for menus
        if (c instanceof MenuElement) {
          removeAllMenuListenersRecur((MenuElement) c);
        } else {
          // Remove all listeners from parents to children order.
          removeAllListeners(c);
          // Go into containers looking for child components.
          if (c instanceof ComponentContainerI) {
            ComponentContainerI cont = (ComponentContainerI) c;
            Component[] components = cont.getPotentiallyHiddenComponents();
            removeAllListenersRecur2(components);
          }
          if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] components = cont.getComponents();
            removeAllListenersRecur2(components);
          }
        }
      } // end if (c != null)
    } catch (Throwable t) {
      // Removing listeners encountered small problem, not a big deal, continue normally.
    }
  } // end removeAllListenersRecur()
  private static void removeAllListenersRecur2(Component[] components) {
    if (components != null && components.length > 0) {
      for (int i=0; i<components.length; i++) {
        Component comp = components[i];
        if (comp != null)
          removeAllListenersRecur(comp);
      } // end for
    }
  }



  private static void removeAllComponentsRecur(Component c) {
    try {
      if (c != null) {
        // special treatment for menus elements
        if (c instanceof MenuElement) {
          removeAllMenuComponentsRecur((MenuElement) c);
        } else {
          // Go into containers looking for child components.
          if (c instanceof ComponentContainerI) {
            ComponentContainerI cont = (ComponentContainerI) c;
            Component[] components = cont.getPotentiallyHiddenComponents();
            removeAllComponentsRecur2(null, components);
          }
          if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] components = cont.getComponents();
            removeAllComponentsRecur2(cont, components);
          } 
        }
      }
    } catch (Throwable t) {
      // Removing components encountered small problem, not a big deal, continue normally.");
      // noop
    }
  } // end removeAllComponentsRecur()
  private static void removeAllComponentsRecur2(Container parent, Component[] components) {
    if (components != null && components.length > 0) {
      for (int i=0; i<components.length; i++) {
        Component comp = components[i];
        if (comp != null) {
          removeAllComponentsRecur(comp);
          // skip menu bars, they have problems later on with popup windows not showing
          if (!(comp instanceof JMenuBar)) {
            boolean componentHandled = false;
            try {
              if (comp instanceof JTextComponent) {
                componentHandled = true;
                JTextComponent textComp = (JTextComponent) comp;
                Keymap keymap = textComp.getKeymap();
                textComp.setKeymap(null);
                if (keymap != null)
                  textComp.removeKeymap(keymap.getName());
                textComp.setActionMap(null);
                textComp.setAutoscrolls(false);
                textComp.setBorder(null);
                textComp.setCaret(null);
                textComp.setDropTarget(null);
                textComp.setFont(null);
                textComp.setHighlighter(null);
                textComp.setLayout(null);
              } else if (comp instanceof AbstractButton) {
                componentHandled = true;
                AbstractButton button = (AbstractButton) comp;
                button.setAction(null);
                button.setActionCommand(null);
                button.setActionMap(null);
                button.setAutoscrolls(false);
                button.setBorder(null);
                button.setCursor(null);
                button.setDisabledIcon(null);
                button.setDisabledSelectedIcon(null);
                button.setDropTarget(null);
                button.setFont(null);
                button.setIcon(null);
                button.setInputVerifier(null);
                button.setLayout(null);
                button.setMargin(null);
                //button.setModel(null); // this caused NullPointerException when button was dissasembled but someone called .setEnabled() on it
                button.setPressedIcon(null);
                button.setRolloverIcon(null);
                button.setRolloverSelectedIcon(null);
                button.setSelectedIcon(null);
              }
            } catch (Throwable t) {
              // noop
            }

            if (componentHandled) {
              // Remove component from its parent next (children get removed first, bottom-up order)
              if (parent != null)
                parent.remove(comp);

              if (comp.isVisible())
                comp.setVisible(false);
              if (comp.hasFocus())
                comp.transferFocus();
            }
          }
        }
      } // end for
    }
    if (parent != null)
      parent.removeAll();
  }


  private static void removeAllMenuListenersRecur(MenuElement menuElement) {
    try {
      if (menuElement != null) {
        Component c = menuElement.getComponent();
        removeAllListeners(c);
        MenuElement[] subElements = menuElement.getSubElements();
        if (subElements != null) {
          for (int i=0; i<subElements.length; i++) {
            removeAllMenuListenersRecur(subElements[i]);
          }
        }
      }
    } catch (Throwable t) {
    }
  }
  private static void removeAllMenuComponentsRecur(MenuElement menuElement) {
    try {
      if (menuElement != null) {
        Component c = menuElement.getComponent();
        MenuElement[] subElements = menuElement.getSubElements();
        if (subElements != null) {
          for (int i=subElements.length-1; i>=0; i--) {
            removeAllMenuComponentsRecur(subElements[i]);
          }
        }
        if (c instanceof Container) {
          Container menuContainer = (Container) c;
          Component[] components = menuContainer.getComponents();
          if (components != null) {
            for (int i=0; i<components.length; i++) {
              Component comp = components[i];
              if (comp instanceof DisposableObj) {
                ((DisposableObj) comp).disposeObj();
              }
            }
          }
          menuContainer.removeAll();
        }
        if (c instanceof DisposableObj) {
          ((DisposableObj) c).disposeObj();
        }
      }
    } catch (Throwable t) {
      // noop
    }
  }

  private static void removeAllListeners(Component c) {
    if (c != null) {
      KeyListener[] keyListeners = (KeyListener[]) c.getListeners(KeyListener.class);
      if (keyListeners != null && keyListeners.length > 0)
        for (int i=0; i<keyListeners.length; i++)
          c.removeKeyListener(keyListeners[i]);

      MouseListener[] mouseListeners = (MouseListener[]) c.getListeners(MouseListener.class);
      if (mouseListeners != null && mouseListeners.length > 0)
        for (int i=0; i<mouseListeners.length; i++)
          c.removeMouseListener(mouseListeners[i]);

      MouseMotionListener[] mouseMotionListeners = (MouseMotionListener[]) c.getListeners(MouseMotionListener.class);
      if (mouseMotionListeners != null && mouseMotionListeners.length > 0)
        for (int i=0; i<mouseMotionListeners.length; i++)
          c.removeMouseMotionListener(mouseMotionListeners[i]);

      ComponentListener[] componentListeners = (ComponentListener[]) c.getListeners(ComponentListener.class);
      if (componentListeners != null && componentListeners.length > 0)
        for (int i=0; i<componentListeners.length; i++)
          c.removeComponentListener(componentListeners[i]);

      FocusListener[] focusListeners = (FocusListener[]) c.getListeners(FocusListener.class);
      if (focusListeners != null && focusListeners.length > 0)
        for (int i=0; i<focusListeners.length; i++)
          c.removeFocusListener(focusListeners[i]);

      HierarchyBoundsListener[] hierarchyBoundsListeners = (HierarchyBoundsListener[]) c.getListeners(HierarchyBoundsListener.class);
      if (hierarchyBoundsListeners != null && hierarchyBoundsListeners.length > 0)
        for (int i=0; i<hierarchyBoundsListeners.length; i++)
          c.removeHierarchyBoundsListener(hierarchyBoundsListeners[i]);

      HierarchyListener[] hierarchyListeners = (HierarchyListener[]) c.getListeners(HierarchyListener.class);
      if (hierarchyListeners != null && hierarchyListeners.length > 0)
        for (int i=0; i<hierarchyListeners.length; i++)
          c.removeHierarchyListener(hierarchyListeners[i]);

      InputMethodListener[] inputMethodListeners = (InputMethodListener[]) c.getListeners(InputMethodListener.class);
      if (inputMethodListeners != null && inputMethodListeners.length > 0)
        for (int i=0; i<inputMethodListeners.length; i++)
          c.removeInputMethodListener(inputMethodListeners[i]);

      PropertyChangeListener[] propertyChangeListeners = (PropertyChangeListener[]) c.getListeners(PropertyChangeListener.class);
      if (propertyChangeListeners != null && propertyChangeListeners.length > 0)
        for (int i=0; i<propertyChangeListeners.length; i++)
          c.removePropertyChangeListener(propertyChangeListeners[i]);

      propertyChangeListeners = c.getPropertyChangeListeners();
      if (propertyChangeListeners != null && propertyChangeListeners.length > 0)
        for (int i=0; i<propertyChangeListeners.length; i++)
          c.removePropertyChangeListener(propertyChangeListeners[i]);

      if (c instanceof Container) {
        Container container = (Container) c;
        ContainerListener[] containerListeners = (ContainerListener[]) container.getListeners(ContainerListener.class);
        if (containerListeners != null && containerListeners.length > 0)
          for (int i=0; i<containerListeners.length; i++)
            container.removeContainerListener(containerListeners[i]);
      }

      if (c instanceof PropertyDrivenItem) {
        PropertyDrivenItem propertyDrivenItem = (PropertyDrivenItem) c;
        propertyDrivenItem.removePropertyChangeListener();
        propertyDrivenItem.setAction(null);
      }

      if (c instanceof AbstractButton) {
        AbstractButton a = (AbstractButton) c;

        ActionListener[] actionListeners = (ActionListener[]) a.getListeners(ActionListener.class);
        if (actionListeners != null && actionListeners.length > 0)
          for (int i=0; i<actionListeners.length; i++)
            a.removeActionListener(actionListeners[i]);

        ChangeListener[] changeListeners = (ChangeListener[]) a.getListeners(ChangeListener.class);
        if (changeListeners != null && changeListeners.length > 0)
          for (int i=0; i<changeListeners.length; i++)
            a.removeChangeListener(changeListeners[i]);

        ItemListener[] itemListeners = (ItemListener[]) a.getListeners(ItemListener.class);
        if (itemListeners != null && itemListeners.length > 0)
          for (int i=0; i<itemListeners.length; i++)
            a.removeItemListener(itemListeners[i]);
      }

      if (c instanceof JComboBox) {
        JComboBox cb = (JComboBox) c;

        ActionListener[] actionListeners = (ActionListener[]) cb.getListeners(ActionListener.class);
        if (actionListeners != null && actionListeners.length > 0)
          for (int i=0; i<actionListeners.length; i++)
            cb.removeActionListener(actionListeners[i]);

        ItemListener[] itemListeners = (ItemListener[]) cb.getListeners(ItemListener.class);
        if (itemListeners != null && itemListeners.length > 0)
          for (int i=0; i<itemListeners.length; i++)
            cb.removeItemListener(itemListeners[i]);
      }

      if (c instanceof JComponent) {
        JComponent jComponent = (JComponent) c;

        AncestorListener[] ancestorListeners = (AncestorListener[]) jComponent.getListeners(AncestorListener.class);
        if (ancestorListeners != null && ancestorListeners.length > 0)
          for (int i=0; i<ancestorListeners.length; i++)
            jComponent.removeAncestorListener(ancestorListeners[i]);

        if (c instanceof AbstractButton) {
          AbstractButton button = (AbstractButton) c;
          ActionListener[] actionListeners = (ActionListener[]) button.getListeners(ActionListener.class);
          if (actionListeners != null && actionListeners.length > 0)
            for (int i=0; i<actionListeners.length; i++)
              button.removeActionListener(actionListeners[i]);

          ChangeListener[] changeListeners = (ChangeListener[]) button.getListeners(ChangeListener.class);
          if (changeListeners != null && changeListeners.length > 0)
            for (int i=0; i<changeListeners.length; i++)
              button.removeChangeListener(changeListeners[i]);

          ItemListener[] itemListeners = (ItemListener[]) button.getListeners(ItemListener.class);
          if (itemListeners != null && itemListeners.length > 0)
            for (int i=0; i<itemListeners.length; i++)
              button.removeItemListener(itemListeners[i]);
        }

        else if (c instanceof JComboBox) {
          JComboBox comboBox = (JComboBox) c;
          ActionListener[] actionListeners = (ActionListener[]) comboBox.getListeners(ActionListener.class);
          if (actionListeners != null && actionListeners.length > 0)
            for (int i=0; i<actionListeners.length; i++)
              comboBox.removeActionListener(actionListeners[i]);

          ItemListener[] itemListeners = (ItemListener[]) comboBox.getListeners(ItemListener.class);
          if (itemListeners != null && itemListeners.length > 0)
            for (int i=0; i<itemListeners.length; i++)
              comboBox.removeItemListener(itemListeners[i]);
        }

        else if (c instanceof JTextComponent) {
          JTextComponent textComp = (JTextComponent) c;
          textComp.getKeymap().removeBindings();

          if (textComp instanceof JTextField) {
            JTextField textField = (JTextField) textComp;
            ActionListener[] actionListeners = (ActionListener[]) textField.getListeners(ActionListener.class);
            if (actionListeners != null && actionListeners.length > 0)
              for (int i=0; i<actionListeners.length; i++)
                textField.removeActionListener(actionListeners[i]);
          }

          CaretListener[] caretListeners = (CaretListener[]) textComp.getListeners(CaretListener.class);
          if (caretListeners != null && caretListeners.length > 0) 
            for (int i=0; i<caretListeners.length; i++)
              textComp.removeCaretListener(caretListeners[i]);

          Document doc = textComp.getDocument();
          if (doc != null && doc instanceof AbstractDocument) {
            AbstractDocument aDoc = (AbstractDocument) doc;
            DocumentListener[] docListeners = (DocumentListener[]) aDoc.getListeners(DocumentListener.class);
            if (docListeners != null && docListeners.length > 0)
              for (int i=0; i<docListeners.length; i++)
                aDoc.removeDocumentListener(docListeners[i]);

            UndoableEditListener[] undoListeners = (UndoableEditListener[]) aDoc.getListeners(UndoableEditListener.class);
            if (undoListeners != null && undoListeners.length > 0)
              for (int i=0; i<undoListeners.length; i++)
                aDoc.removeUndoableEditListener(undoListeners[i]);
          }
        }

        else if (c instanceof JTable) {
          JTable table = (JTable) c;
          removeAllListeners(table.getSelectionModel());
          removeAllListeners(table.getModel());

          /*
          if (table instanceof JSortedTable) {
            JSortedTable sTable = (JSortedTable) table;
            removeAllListeners(sTable.getRawModel());

            TableModel tableModel = sTable.getModel();
            if (tableModel instanceof TableMap) {
              TableMap tMap = (TableMap) tableModel;
              tMap.removeTableModelSortListeners();
            }
          }
           */
        }

        else if (c instanceof JList) {
          JList list = (JList) c;
          removeAllListeners(list.getSelectionModel());
          ListModel listModel = list.getModel();
          if (listModel != null && listModel instanceof AbstractListModel) {
            AbstractListModel aListModel = (AbstractListModel) listModel;
            ListDataListener[] listeners = (ListDataListener[]) aListModel.getListeners(ListDataListener.class);
            if (listeners != null && listeners.length > 0)
              for (int i=0; i<listeners.length; i++)
                aListModel.removeListDataListener(listeners[i]);
          }
        }

      } // end if JComponent

      if (c instanceof DisposableObj) {
        DisposableObj disposableObj = (DisposableObj) c;
        disposableObj.disposeObj();
      }
      /*
      if (c instanceof ActionProducerI) {
        ActionProducerI actionProducer = (ActionProducerI) c;
        actionProducer.clearActions();
      }
       */

    } // end if != null
  } // end removeAllListeners()

  private static void removeAllListeners(ListSelectionModel listSelectionModel) {
    if (listSelectionModel != null && listSelectionModel instanceof DefaultListSelectionModel) {
      DefaultListSelectionModel dListSelectionModel = (DefaultListSelectionModel) listSelectionModel;
      ListSelectionListener[] listeners = (ListSelectionListener[]) dListSelectionModel.getListeners(ListSelectionListener.class);
      if (listeners != null && listeners.length > 0)
        for (int i=0; i<listeners.length; i++)
          dListSelectionModel.removeListSelectionListener(listeners[i]);
    }
  }

  private static void removeAllListeners(TableModel tableModel) {
    if (tableModel != null && tableModel instanceof AbstractTableModel) {
      AbstractTableModel aTableModel = (AbstractTableModel) tableModel;
      TableModelListener[] listeners = (TableModelListener[]) aTableModel.getListeners(TableModelListener.class);
      if (listeners != null && listeners.length > 0)
        for (int i=0; i<listeners.length; i++)
          aTableModel.removeTableModelListener(listeners[i]);
    }
  }


  public static void storeVisualsSavable(Component c) {
    boolean traverseContainers = true;
    if (c instanceof VisualsSavable) {
      VisualsSavable v = (VisualsSavable) c;
      String vs = v.getVisuals();
      if (vs != null) {
        String name = getVisualsKeyName(v);
        if (name != null)
          GlobalProperties.setProperty(name, vs);
      }
      traverseContainers = v.isVisuallyTraversable();
    }
    if (traverseContainers && c instanceof Container) {
      Container cont = (Container) c;
      Component[] children = cont.getComponents();
      for (int i=0; i<children.length; i++) {
        storeVisualsSavable(children[i]);
      }
    }
  }

  /**
   * @return key name for GlobalProperties used to store visuals data
   */
  public static String getVisualsKeyName(VisualsSavable v) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(VisualsSavable v)");
    if (trace != null) trace.args(v);
    String extension = v.getExtension();
    String name = getVisualsKeyName(v, extension);
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }

  public static String getVisualsKeyName(VisualsSavable versionedVisual, String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(VisualsSavable versionedVisual, String propertyName)");
    if (trace != null) trace.args(versionedVisual, propertyName);
    String name = null;
    String visualsClassKeyName = versionedVisual.getVisualsClassKeyName();
    if (visualsClassKeyName != null)
      name = getVisualsKeyName(visualsClassKeyName, versionedVisual.getVisualsVersion(), propertyName);
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }
  public static String getVisualsKeyName(String visualsClassKeyName, Integer visualsVersion, String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(String visualsClassKeyName, Integer visualsVersion, String propertyName)");
    if (trace != null) trace.args(visualsClassKeyName, visualsVersion, propertyName);
    String name = visualsClassKeyName;
    if (propertyName != null && propertyName.length() > 0)
      name += "_" + propertyName;
    name += "_VS";
    if (visualsVersion != null) {
      name += "_Ver" + visualsVersion;
    }
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }

  /** 
   * Gets recursively Components from all Components that descend from <code> c </code>.
   */
  private static void fillComponentsRecursively(Component c, java.util.List targetList) {
    if (c != null) {
      if (!targetList.contains(c))
        targetList.add(c);
      Component[] components = null;
      for (int i=0; i<2; i++) {
        if (i == 0 && c instanceof ComponentContainerI) {
          components = ((ComponentContainerI)c).getPotentiallyHiddenComponents();
        } else if (i == 1 && c instanceof Container) {
          components = ((Container)c).getComponents();  
        }
        if (components != null) {
          for (int k=0; k<components.length; k++) {
            Component compK = components[k];
            if (compK != null) {
              fillComponentsRecursively(compK, targetList);
            }
          }
        }
      }
    }
  }


  /** 
   * Gets recursively all Components that <code> c </code> contains
   * merges them and returns as an array of Components.
   */
  public static Component[] getComponentsRecursively(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getComponentsRecursively(Component c)");

    LinkedList targetList = new LinkedList();
    fillComponentsRecursively(c, targetList);
    Component[] componentArray = (Component[]) ArrayUtils.toArray(targetList, Component.class);
    int length = componentArray != null ? componentArray.length : 0;

    if (trace != null) trace.exit(MiscGui.class, length);
    return componentArray;
  }

  public static JSplitPane getParentSplitPane(Component c) {
    JSplitPane splitPane = null;
    Container cont = c.getParent();
    if (cont != null) {
      while (true) {
        if (cont == null)
          break;
        else if (cont instanceof JSplitPane) {
          splitPane = (JSplitPane) cont;
          break;
        }
        cont = cont.getParent();
      }
    }
    return splitPane;
  }

}