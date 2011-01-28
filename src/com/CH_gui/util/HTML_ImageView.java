/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
package com.CH_gui.util;

import com.CH_co.trace.Trace;
import com.CH_co.util.ImageNums;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.net.*;
import java.util.Dictionary;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
public class HTML_ImageView extends View implements ImageObserver, MouseListener, MouseMotionListener {

  // --- Attribute Values ------------------------------------------
  public static final String TOP = "top",  TEXTTOP = "texttop",  MIDDLE = "middle",  ABSMIDDLE = "absmiddle",  CENTER = "center",  BOTTOM = "bottom";

  // --- constants and static stuff --------------------------------
  private static Icon sPendingImageIcon;
  private static Icon sMissingImageIcon;
  private static ImageIcon sResizeDragSEImageIcon;
  private static int fResizeBoxHeight = 5, fResizeBoxWidth = 5;
  private static final boolean DEBUG = false;

  //$ move this someplace public
  static final String IMAGE_CACHE_PROPERTY = "imageCache";

  // Height/width to use before we know the real size:
  private static final int DEFAULT_WIDTH = 24;
  private static final int DEFAULT_HEIGHT = 24;
  private static final int DEFAULT_BORDER = 1;
  private static final int DEFAULT_EDIT_BORDER = 1;

  // --- member variables ------------------------------------------------
  private AttributeSet attr;
  private Element fElement;
  private Image fImage;
  private int fHeight,  fWidth;
  private Container fContainer;
  private Rectangle fBounds;
  private Component fComponent;
  private Point fGrowBase;        // base of drag while growing image
  /** Set to true, while the receiver is locked, to indicate the reciever
   * is loading the image. This is used in imageUpdate. */
  private boolean loading;


  // --- Construction ----------------------------------------------
  /**
   * Creates a new view that represents an IMG element.
   *
   * @param elem the element to create a view for
   */
  public HTML_ImageView(Element elem) {
    super(elem);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "HTML_ImageView(Element elem)");
    initialize(elem);
    StyleSheet sheet = getStyleSheet();
    attr = sheet.getViewAttributes(this);
    if (trace != null) trace.exit(HTML_ImageView.class);
  }

  private void initialize(Element elem) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "initialize(Element elem)");
    synchronized (this) {
      loading = true;
      fWidth = fHeight = 0;
    }
    int width = 0;
    int height = 0;
    boolean customWidth = false;
    boolean customHeight = false;
    try {
      fElement = elem;

      if (isURL()) {
        URL srcURL = getSourceURL();
        if (DEBUG) {
          System.out.println("URL src = "+srcURL);
        }
        if (srcURL != null) {
          fImage = Toolkit.getDefaultToolkit().createImage(srcURL);
        }
      } else {
        /******** Code to load from relative path *************/
        String src = (String) fElement.getAttributes().getAttribute(HTML.Attribute.SRC);
        if (DEBUG) {
          System.out.println("non-URL src = "+src);
        }
        ImageIcon imageIcon = Images.get(src);
        if (imageIcon != null) {
          fImage = imageIcon.getImage();
        } else {
          URL context = ((HTMLDocument) getDocument()).getBase();
          URL srcURL = null;
          try {
            srcURL = new URL(context, src);
            Dictionary cache = (Dictionary) getDocument().getProperty(IMAGE_CACHE_PROPERTY);
            if (cache != null) {
              fImage = (Image) cache.get(srcURL);
            }
            if (fImage == null) {
              fImage = Toolkit.getDefaultToolkit().createImage(srcURL);
              if (fImage != null) {
                try {
                  waitForImage();
                  if (cache != null) {
                    cache.put(srcURL, fImage);
                  }
                } catch (InterruptedException e) {
                  if (trace != null) trace.exception(HTML_ImageView.class, 100, e);
                  fImage = null;
                }
              }
            }
          } catch (MalformedURLException e) {
            if (trace != null) trace.exception(HTML_ImageView.class, 200, e);
          }
        }
      /******************************************************/
      }

      // Get height/width from params or image or defaults:
      height = getIntAttr(HTML.Attribute.HEIGHT, -1);
      customHeight = (height > 0);
      if (!customHeight && fImage != null) {
        height = fImage.getHeight(this);
      }
      if (height <= 0) {
        height = DEFAULT_HEIGHT;
      }

      width = getIntAttr(HTML.Attribute.WIDTH, -1);
      customWidth = (width > 0);
      if (!customWidth && fImage != null) {
        width = fImage.getWidth(this);
      }
      if (width <= 0) {
        width = DEFAULT_WIDTH;
      }

      // Make sure the image starts loading:
      if (fImage != null) {
        if (customWidth && customHeight) {
          Toolkit.getDefaultToolkit().prepareImage(fImage, height, width, this);
        } else {
          Toolkit.getDefaultToolkit().prepareImage(fImage, -1, -1, this);
        }
      }

    /********************************************************
    // Rob took this out. Changed scope of src.
    if( DEBUG ) {
    if( fImage != null )
    System.out.println("ImageInfo: new on "+src+
    " ("+fWidth+"x"+fHeight+")");
    else
    System.out.println("ImageInfo: couldn't get image at "+
    src);
    if(isLink())
    System.out.println("           It's a link! Border = "+
    getBorder());
    //((AbstractDocument.AbstractElement)elem).dump(System.out,4);
    }
     ********************************************************/
    } finally {
      synchronized (this) {
        loading = false;
        if (customWidth || fWidth == 0) {
          fWidth = width;
        }
        if (customHeight || fHeight == 0) {
          fHeight = height;
        }
      }
    }
    if (trace != null) trace.exit(HTML_ImageView.class);
  }

  /** Determines if path is in the form of a URL */
  private boolean isURL() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "isURL()");
    String src = (String) fElement.getAttributes().getAttribute(HTML.Attribute.SRC);
    boolean b = src.toLowerCase().startsWith("file") || src.toLowerCase().startsWith("http");
    if (trace != null) trace.exit(HTML_ImageView.class, b);
    return b;
  }

  /** Added this guy to make sure an image is loaded - ie no broken 
  images. So far its used only for images loaded off the disk (non-URL). 
  It seems to work marvelously. By the way, it does the same thing as
  MediaTracker, but you dont need to know the component its being 
  rendered on. Rob */
  private void waitForImage() throws InterruptedException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "waitForImage()");
    int w = fImage.getWidth(this);
    int h = fImage.getHeight(this);

    long sleepTotal = 0;
    while (true) {
      int flags = Toolkit.getDefaultToolkit().checkImage(fImage, w, h, this);
      if (((flags & ERROR) != 0) || ((flags & ABORT) != 0)) {
        throw new InterruptedException();
      } else if ((flags & (ALLBITS | FRAMEBITS)) != 0) {
        break;
      }
      Thread.sleep(10);
      sleepTotal += 10;
      if (sleepTotal % 1000 == 0) {
        if (trace != null) trace.data(10, "sleepTotal", sleepTotal/1000 + " seconds");
      }
    }
    if (trace != null) trace.exit(HTML_ImageView.class);
  }

  /**
   * Fetches the attributes to use when rendering.  This is
   * implemented to multiplex the attributes specified in the
   * model with a StyleSheet.
   */
  public AttributeSet getAttributes() {
    return attr;
  }

  /** Is this image within a link? */
  boolean isLink() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "isLink()");
    boolean isLink = false;
    //! It would be nice to cache this but in an editor it can change
    // See if I have an HREF attribute courtesy of the enclosing A tag:
    AttributeSet anchorAttr = (AttributeSet) fElement.getAttributes().getAttribute(HTML.Tag.A);
    if (anchorAttr != null) {
      isLink = anchorAttr.isDefined(HTML.Attribute.HREF);
    }
    if (trace != null) trace.exit(HTML_ImageView.class, isLink);
    return isLink;
  }

  /** Returns the size of the border to use. */
  int getBorder() {
    int value = getIntAttr(HTML.Attribute.BORDER, isLink() ? DEFAULT_BORDER : 0);
    return value;
  }

  /** Returns the amount of extra space to add along an axis. */
  int getSpace(int axis) {
    int value = getIntAttr(axis == X_AXIS ? HTML.Attribute.HSPACE : HTML.Attribute.VSPACE, 0);
    return value;
  }

  /** Returns the border's color, or null if this is not a link. */
  Color getBorderColor() {
    StyledDocument doc = (StyledDocument) getDocument();
    Color color = doc.getForeground(getAttributes());
    return color;
  }

  /** Returns the image's vertical alignment. */
  float getVerticalAlignment() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "getVerticalAlignment()");
    float value = 1.0f; // default alignment is bottom
    String align = (String) fElement.getAttributes().getAttribute(HTML.Attribute.ALIGN);
    if (align != null) {
      align = align.toLowerCase();
      if (align.equals(TOP) || align.equals(TEXTTOP)) {
        value = 0.0f;
      } else if (align.equals(this.CENTER) || align.equals(MIDDLE) || align.equals(ABSMIDDLE)) {
        value = 0.5f;
      }
    }
    if (trace != null) trace.exit(HTML_ImageView.class, value);
    return value;
  }

  boolean hasPixels(ImageObserver obs) {
    boolean b = fImage != null && fImage.getHeight(obs) > 0 && fImage.getWidth(obs) > 0;
    return b;
  }

  /** Return a URL for the image source, 
  or null if it could not be determined. */
  private URL getSourceURL() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "getSourceURL()");
    URL url = null;
    String src = (String) fElement.getAttributes().getAttribute(HTML.Attribute.SRC);
    if (src == null) {
      url = null;
    } else {
      URL reference = ((HTMLDocument) getDocument()).getBase();
      try {
        url = new URL(reference, src);
      } catch (MalformedURLException e) {
        if (trace != null) trace.exception(HTML_ImageView.class, 100, e);
        url = null;
      }
    }
    if (trace != null) trace.exit(HTML_ImageView.class, url);
    return url;
  }

  /** Look up an integer-valued attribute. <b>Not</b> recursive. */
  private int getIntAttr(HTML.Attribute name, int deflt) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "getIntAttr(HTML.Attribute name, int deflt)");
    if (trace != null) trace.args(name);
    if (trace != null) trace.args(deflt);
    int attrib = 0;
    AttributeSet attr = fElement.getAttributes();
    if (attr.isDefined(name)) {		// does not check parents!
      int i;
      String val = (String) attr.getAttribute(name);
      if (val == null) {
        i = deflt;
      } else {
        try {
          i = Math.max(0, Integer.parseInt(val));
        } catch (NumberFormatException x) {
          if (trace != null) trace.exception(HTML_ImageView.class, 100, x);
          i = deflt;
        }
      }
      attrib = i;
    } else {
      attrib = deflt;
    }
    if (trace != null) trace.exit(HTML_ImageView.class, attrib);
    return attrib;
  }

  /**
   * Establishes the parent view for this view.
   * Seize this moment to cache the AWT Container I'm in.
   */
  public void setParent(View parent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "setParent(View parent)");
    if (trace != null) trace.args(parent);
    super.setParent(parent);
    fContainer = parent != null ? getContainer() : null;
    if (parent == null && fComponent != null) {
      Container cnt = fComponent.getParent();
      if (cnt != null)
        cnt.remove(fComponent);
      fComponent = null;
    }
    if (trace != null) trace.exit(HTML_ImageView.class);
  }

  /** My attributes may have changed. */
  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    if (DEBUG) {
      System.out.println("ImageView: changedUpdate begin...");
    }
    super.changedUpdate(e, a, f);
    float align = getVerticalAlignment();

    int height = fHeight;
    int width = fWidth;

    initialize(getElement());

    boolean hChanged = fHeight != height;
    boolean wChanged = fWidth != width;
    if (hChanged || wChanged || getVerticalAlignment() != align) {
      if (DEBUG) {
        System.out.println("ImageView: calling preferenceChanged");
      }
      getParent().preferenceChanged(this, hChanged, wChanged);
    }
    if (DEBUG) {
      System.out.println("ImageView: changedUpdate end; valign=" + getVerticalAlignment());
    }
  }


  // --- Painting --------------------------------------------------------
  /**
   * Paints the image.
   *
   * @param g the rendering surface to use
   * @param a the allocated region to render into
   * @see View#paint
   */
  public void paint(Graphics g, Shape a) {
    Color oldColor = g.getColor();
    fBounds = a.getBounds();
    int border = getBorder();
    int x = fBounds.x + border + getSpace(X_AXIS);
    int y = fBounds.y + border + getSpace(Y_AXIS);
    int width = fWidth;
    int height = fHeight;
    int sel = getSelectionState();

    // Make sure my Component is in the right place:
    if (fComponent == null) {
      fComponent = new Component() { };
      fComponent.addMouseListener(this);
      fComponent.addMouseMotionListener(this);
      // change this to customize the mouse pointer
      fComponent.setCursor(Cursor.getDefaultCursor());
      fContainer.add(fComponent);

      JTextComponent textComp = (JTextComponent) fContainer;
      textComp.addCaretListener(new CaretListener() {
        private int lastSelectionState = -1;
        public void caretUpdate(CaretEvent e) {
          int selectionState = getSelectionState();
          if (selectionState != lastSelectionState) {
            lastSelectionState = selectionState;
            repaint(0);
          }
        }
      });

    }
    fComponent.setBounds(x, y, width, height);

    // If no pixels yet, draw gray outline and icon:
    if (!hasPixels(this)) {
      g.setColor(Color.lightGray);
      g.drawRect(x, y, width - 1, height - 1);
      g.setColor(oldColor);
      loadIcons();
      Icon icon = fImage == null ? sMissingImageIcon : sPendingImageIcon;
      if (icon != null) {
        icon.paintIcon(getContainer(), g, x, y);
      }
    }

    // Draw image:
    if (fImage != null) {
      //g.drawImage(fImage, x, y, width, height, this);
    // Use the following instead of g.drawImage when
    // BufferedImageGraphics2D.setXORMode is fixed (4158822).

    //  Use Xor mode when selected/highlighted.
    //! Could darken image instead, but it would be more expensive.

      if( sel > 0 )
        g.setXORMode(Color.lightGray);
      g.drawImage(fImage, x, y, width, height, this);
      if( sel > 0 )
        g.setPaintMode();
    }

    // If selected exactly, we need a black border & grow-box:
    Color bc = getBorderColor();
    // Prepare for the grow-edit box
    if (sel == 2) {
        border = DEFAULT_EDIT_BORDER;
        bc = null;
        g.setColor(Color.lightGray);
    }

    // Draw border:
    if (border > 0) {
      if (bc != null) {
        g.setColor(bc);
      }
      // Draw a thick rectangle:
      for (int i = 0; i < border; i++) {
        //g.drawRect(x - i, y - i, width - 1 + i + i, height - 1 + i + i);
        g.drawLine(x+i, y+i, x+i + (width - 1 - i - i), y+i);
        g.drawLine(x+i, y+i + (height - 1 - i - i), x+i + (width - 1 - i - i) - fResizeBoxWidth-1, y+i + (height - 1 - i - i));
        g.drawLine(x+i, y+i, x+i, y+i + (height - 1 - i - i));
        g.drawLine(x+i + (width - 1 - i - i), y+i, x+i + (width - 1 - i - i), y+i + (height - 1 - i - i) - fResizeBoxHeight-1);
      }
      g.setColor(oldColor);
    }

    // Draw grow-edit icon or square
    if (sel == 2) {
      loadIcons();
      if (sResizeDragSEImageIcon != null) {
        g.drawImage(sResizeDragSEImageIcon.getImage(), x + width - fResizeBoxWidth, y + height - fResizeBoxHeight, fResizeBoxWidth, fResizeBoxHeight, this);
      } else {
        g.fillRect(x + width - fResizeBoxWidth, y + height - fResizeBoxHeight, fResizeBoxWidth, fResizeBoxHeight);
      }
    }
  }

  /** Request that this view be repainted.
  Assumes the view is still at its last-drawn location. */
  protected void repaint(long delay) {
    if (fContainer != null && fBounds != null) {
      fContainer.repaint(delay, fBounds.x, fBounds.y, fBounds.width, fBounds.height);
    }
  }

  /** Determines whether the image is selected, and if it's the only thing selected.
  @return  0 if not selected, 1 if selected, 2 if exclusively selected.
  "Exclusive" selection is only returned when editable. */
  protected int getSelectionState() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "getSelectionState()");
    int state = 0;
    int p0 = fElement.getStartOffset();
    int p1 = fElement.getEndOffset();
    if (fContainer instanceof JTextComponent) {
      JTextComponent textComp = (JTextComponent) fContainer;
      int start = textComp.getSelectionStart();
      int end = textComp.getSelectionEnd();
      if (start <= p0 && end >= p1) {
        if (start == p0 && end == p1 && isEditable()) {
          state = 2;
        } else {
          state = 1;
        }
      }
    }
    if (trace != null) trace.exit(HTML_ImageView.class, state);
    return state;
  }

  protected boolean isEditable() {
    boolean b =fContainer instanceof JEditorPane && ((JEditorPane) fContainer).isEditable();
    return b;
  }

  /** Returns the text editor's highlight color. */
  protected Color getHighlightColor() {
    JTextComponent textComp = (JTextComponent) fContainer;
    Color color = textComp.getSelectionColor();
    return color;
  }

  // --- Progressive display ---------------------------------------------
  // This can come on any thread. If we are in the process of reloading
  // the image and determining our state (loading == true) we don't fire
  // preference changed, or repaint, we just reset the fWidth/fHeight as
  // necessary and return. This is ok as we know when loading finishes
  // it will pick up the new height/width, if necessary.
  public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
    boolean update = false;

    if (fImage == null || fImage != img) {
      update = false;
    } else {

      // Bail out if there was an error:
      if ((flags & (ABORT | ERROR)) != 0) {
        fImage = null;
        repaint(0);
        update = false;
      } else {

        // Resize image if necessary:
        short changed = 0;
        if ((flags & ImageObserver.HEIGHT) != 0) {
          if (!getElement().getAttributes().isDefined(HTML.Attribute.HEIGHT)) {
            changed |= 1;
          }
        }
        if ((flags & ImageObserver.WIDTH) != 0) {
          if (!getElement().getAttributes().isDefined(HTML.Attribute.WIDTH)) {
            changed |= 2;
          }
        }
        synchronized (this) {
          if ((changed & 1) == 1) {
            fWidth = width;
          }
          if ((changed & 2) == 2) {
            fHeight = height;
          }
          if (loading) {
            // No need to resize or repaint, still in the process of
            // loading.
            update = true;
          }
        }
        if (!update) {
          if (changed != 0) {
            // May need to resize myself, asynchronously:
            if (DEBUG) {
              System.out.println("ImageView: resized to " + fWidth + "x" + fHeight);
            }

            Document doc = getDocument();
            try {
              if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readLock();
              }
              preferenceChanged(this, true, true);
            } finally {
              if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
              }
            }

            update = true;
          }

          if (!update) {

            // Repaint when done or when new pixels arrive:
            if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
              repaint(0);
            } else if ((flags & SOMEBITS) != 0) {
              if (sIsInc) {
                repaint(sIncRate);
              }
            }
          }
        }
      }
    }

    update = ((flags & ALLBITS) == 0);

    return update;
  }
  /*        
  /**
   * Static properties for incremental drawing.
   * Swiped from Component.java
   * @see #imageUpdate
   */
  private static boolean sIsInc = true;
  private static int sIncRate = 100;

  // --- Layout ----------------------------------------------------------
  /**
   * Determines the preferred span for this view along an
   * axis.
   *
   * @param axis may be either X_AXIS or Y_AXIS
   * @returns  the span the view would like to be rendered into.
   *           Typically the view is told to render into the span
   *           that is returned, although there is no guarantee.  
   *           The parent may choose to resize or break the view.
   */
  public float getPreferredSpan(int axis) {
    float span = 0.0f;
    int extra = 2 * (getBorder() + getSpace(axis));
    switch (axis) {
      case View.X_AXIS:
        span = fWidth + extra;
        break;
      case View.Y_AXIS:
        span = fHeight + extra;
        break;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
    return span;
  }

  /**
   * Determines the desired alignment for this view along an
   * axis.  This is implemented to give the alignment to the
   * bottom of the icon along the y axis, and the default
   * along the x axis.
   *
   * @param axis may be either X_AXIS or Y_AXIS
   * @returns the desired alignment.  This should be a value
   *   between 0.0 and 1.0 where 0 indicates alignment at the
   *   origin and 1.0 indicates alignment to the full span
   *   away from the origin.  An alignment of 0.5 would be the
   *   center of the view.
   */
  public float getAlignment(int axis) {
    float align = 0.0f;
    switch (axis) {
      case View.Y_AXIS:
        align = getVerticalAlignment();
        break;
      default:
        align = super.getAlignment(axis);
    }
    return align;
  }

  /**
   * Provides a mapping from the document model coordinate space
   * to the coordinate space of the view mapped to it.
   *
   * @param pos the position to convert
   * @param a the allocated region to render into
   * @return the bounding box of the given position
   * @exception BadLocationException  if the given position does not represent a
   *   valid location in the associated document
   * @see View#modelToView
   */
  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    Shape shape = null;
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    if ((pos >= p0) && (pos <= p1)) {
      Rectangle r = a.getBounds();
      if (pos == p1) {
        r.x += r.width;
      }
      r.width = 0;
      shape = r;
    }
    return shape;
  }

  /**
   * Provides a mapping from the view coordinate space to the logical
   * coordinate space of the model.
   *
   * @param x the X coordinate
   * @param y the Y coordinate
   * @param a the allocated region to render into
   * @return the location within the model that best represents the
   *  given point of view
   * @see View#viewToModel
   */
  public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
    int offset = 0;
    Rectangle alloc = (Rectangle) a;
    if (x < alloc.x + alloc.width) {
      bias[0] = Position.Bias.Forward;
      offset = getStartOffset();
    } else {
      bias[0] = Position.Bias.Backward;
      offset = getEndOffset();
    }
    return offset;
  }

  /**
   * Set the size of the view. (Ignored.)
   *
   * @param width the width
   * @param height the height
   */
  public void setSize(float width, float height) {
  // Ignore this -- image size is determined by the tag attrs and
  // the image itself, not the surrounding layout!
  }

  /** Change the size of this image. This alters the HEIGHT and WIDTH
  attributes of the Element and causes a re-layout. */
  protected void resize(int width, int height) {
    if (width == fWidth && height == fHeight) {
      // no-op
    } else {
      fWidth = width;
      fHeight = height;
      // Replace attributes in document:
      MutableAttributeSet attr = new SimpleAttributeSet();
      attr.addAttribute(HTML.Attribute.WIDTH, Integer.toString(width));
      attr.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(height));
      ((StyledDocument) getDocument()).setCharacterAttributes(fElement.getStartOffset(), fElement.getEndOffset(), attr, false);
    }
  }

  // --- Mouse event handling --------------------------------------------
  /** Select or grow image when clicked. */
  public void mousePressed(MouseEvent e) {
    Dimension size = fComponent.getSize();
    if (e.getX() >= size.width - fResizeBoxWidth && e.getY() >= size.height - fResizeBoxHeight && getSelectionState() == 2) {
      // Click in selected grow-box:
      if (DEBUG) {
        System.out.println("ImageView: grow!!! Size=" + fWidth + "x" + fHeight);
      }
      Point loc = fComponent.getLocationOnScreen();
      fGrowBase = new Point(loc.x + e.getX() - fWidth, loc.y + e.getY() - fHeight);
    } else {
      // Else select image:
      fGrowBase = null;
      JTextComponent comp = (JTextComponent) fContainer;
      int start = fElement.getStartOffset();
      int end = fElement.getEndOffset();
      int mark = comp.getCaret().getMark();
      int dot = comp.getCaret().getDot();
      if (e.isShiftDown()) {
        // extend selection if shift key down:
        if (mark <= start) {
          comp.moveCaretPosition(end);
        } else {
          comp.moveCaretPosition(start);
        }
      } else {
        // just select image, without shift:
        if (mark != start) {
          comp.setCaretPosition(start);
        }
        if (dot != end) {
          comp.moveCaretPosition(end);
        }
      }
    }
  }

  /** Resize image if initial click was in grow-box: */
  public void mouseDragged(MouseEvent e) {
    if (fGrowBase != null) {
      Point loc = fComponent.getLocationOnScreen();
      int width = Math.max(2, loc.x + e.getX() - fGrowBase.x);
      int height = Math.max(2, loc.y + e.getY() - fGrowBase.y);
      if (e.isShiftDown() && fImage != null) {
        // Make sure size is proportional to actual image size:
        float imgWidth = fImage.getWidth(this);
        float imgHeight = fImage.getHeight(this);
        if (imgWidth > 0 && imgHeight > 0) {
          float prop = imgHeight / imgWidth;
          float pwidth = height / prop;
          float pheight = width * prop;
          if (pwidth > width) {
            width = (int) pwidth;
          } else {
            height = (int) pheight;
          }
        }
      }
      resize(width, height);
    }
  }

  public void mouseReleased(MouseEvent e) {
    fGrowBase = null;
  //! Should post some command to make the action undo-able
  }

  /** On double-click, open image properties dialog. */
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2) {
    //$ IMPLEMENT
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  private void loadIcons() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_ImageView.class, "loadIcons()");
    try {
      if (sPendingImageIcon == null) {
        sPendingImageIcon = Images.get(ImageNums.HTML_IMAGE_DELAYED);
      }
      if (sMissingImageIcon == null) {
        sMissingImageIcon = Images.get(ImageNums.HTML_IMAGE_FAILED);
      }
      if (sResizeDragSEImageIcon == null) {
        sResizeDragSEImageIcon = Images.get(ImageNums.RESIZE_DRAG_SE);
        fResizeBoxHeight = sResizeDragSEImageIcon.getIconHeight();
        fResizeBoxWidth = sResizeDragSEImageIcon.getIconWidth();
      }
    } catch (Exception x) {
      if (trace != null) trace.exception(HTML_ImageView.class, 100, x);
      System.err.println("ImageView: Couldn't load image icons");
    }
    if (trace != null) trace.exit(HTML_ImageView.class);
  }

  protected StyleSheet getStyleSheet() {
    HTMLDocument doc = (HTMLDocument) getDocument();
    StyleSheet ss = doc.getStyleSheet();
    return ss;
  }

}