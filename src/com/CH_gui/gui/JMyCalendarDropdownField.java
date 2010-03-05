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

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.CH_co.util.*;
import com.CH_co.gui.*;
import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyCalendarDropdownField extends JPanel implements ActionListener, FocusListener, PropertyChangeListener {

  public static final int YYYYMMDD = 1;
  public static final int YYYY_MM_DD = 2;
  public static final int DEFAULT = 3;
  public static final int TOP = 0;
  public static final int BOTTOM = 1;

  JButton button;
  JTextField field;
  JMyCalendarSheet calendar;
  //DateFormat df;
  int dateFormat;
  SimpleDateFormat formatter;
  JLayeredPane layeredPane;
  int position;
  String prevDate;
  boolean isNullable;
  String[] optionNames;
  int[][] options;
  boolean useOptionNameText;

  // options
  // -2 chooser
  // -1 never
  // 0 now
  // >0 number of days from now

  /**
   * @param i temporarity disabled
   * @param j text field date format style 1=yyyMMdd, 2=yyyy-MM-dd, 3=platform default
   * @param k popup weekday Title Style 0=no name, 1=short, 2=long
   * @param defaultDate default value in the text field
   * @param optionNames if null show three dot icon, else drop down options
   * @param options predefined number of days corresponding to string "optionNames" and some default actions
   */
  public JMyCalendarDropdownField(int i, int j, int k, boolean nullable, Date defaultDate, String[] _optionNames, int[][] _options, boolean _useOptionNameText) {
    this(i, j, k, nullable, defaultDate, _optionNames, _options, _useOptionNameText, new JMyDropdownIcon());
  }
  public JMyCalendarDropdownField(int i, int j, int k, boolean nullable, Date defaultDate, String[] _optionNames, int[][] _options, boolean _useOptionNameText, Icon icon) {
    dateFormat = 3;
    layeredPane = null;
    position = 1;
    prevDate = "";
    dateFormat = j;
    isNullable = nullable;
    optionNames = _optionNames;
    options = _options;
    useOptionNameText = _useOptionNameText;
    setLayout(new BorderLayout());
    if (optionNames == null) {
      if (icon == null)
        icon = new JMyDotsIcon();
      button = new JMyButton(icon);
      //button.setBorder(new EmptyBorder(0,0,0,0));
      //button.setBorder(UIManager.getBorder("Button.border"));
      button.setActionCommand("showcal");
      button.addActionListener(this);
    } else {
      if (icon == null)
        icon = new JMyDropdownIcon();
      button = new JMyButton(icon);
      //button.setIcon(Images.get(ImageNums.ARROW_DROP_DOWN_5_3));
      //button.setAlignmentX(JButton.CENTER_ALIGNMENT);
      //button.setBorder(new EmptyBorder(0, 0, 0, 0));
      //button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      button.setPreferredSize(new Dimension(11, 21));
      button.setMinimumSize(new Dimension(11, 21));
      ActionListener popupAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Object[] objects = optionNames;
          if (objects != null && objects.length > 0) {
            JPopupMenu popup = new JMyPopupMenu("Options");
            final JMenuItem[] menuItems = new JMenuItem[objects.length];
            for (int i=0; i<menuItems.length; i++) {
              menuItems[i] = new JMenuItem();
              menuItems[i].setText(""+objects[i]);
              //menuItems[i].setIcon(ListRenderer.getRenderedIcon(objects[i]));
              menuItems[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  int index = 0;
                  JMenuItem source = (JMenuItem) e.getSource();
                  for (int k=0; k<menuItems.length; k++) {
                    if (source.equals(menuItems[k])) {
                      pressedMenuItem(k);
                      index = k;
                      break;
                    }
                  }
                  //setSelectedIndex(index);
                  //fireActionPerformed(e);
                }
              });
              popup.add(menuItems[i]);
            }
            popup.pack();
            // wrong-popup-location
            // Point point = MiscGui.getSuggestedPopupLocation(JMyCalendarDropdownField.this, popup);
            // popup.show(JMyCalendarDropdownField.this, point.x, point.y);
            popup.show(JMyCalendarDropdownField.this, 0, JMyCalendarDropdownField.this.getSize().height);
          }
        }
      };
      button.addActionListener(popupAction);
    }
    calendar = new JMyCalendarSheet(k);
    calendar.addActionListener(this);
    calendar.setVisible(false);
    calendar.setBorder(new LineBorder(Color.black, 2));
    Dimension dimension = calendar.getPreferredSize();
    calendar.setSize(new Dimension((int)((double)dimension.width * 1.1000000000000001D), dimension.height));
    formatter = sdFormatter(j);
    //df = DateFormat.getDateInstance(i);
    //df.setTimeZone(TimeZone.getDefault());
    if (!isNullable && defaultDate == null)
      defaultDate = new Date();
    String defaultDateStr = defaultDate != null ? formatter.format(defaultDate) : "";
    field = new JMyTextField(defaultDateStr, 12);
    field.setMinimumSize(new Dimension(140, 16));
    field.setBorder(new EmptyBorder(0,0,0,0));
    field.addFocusListener(this);
    button.addKeyListener(calendar);
    add(field, BorderLayout.CENTER);
    add(button, BorderLayout.EAST);
    Dimension buttonDimension = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    button.setPreferredSize(buttonDimension);
    button.setSize(buttonDimension);
    addPropertyChangeListener("labeledBy", this);
    addPropertyChangeListener("UAKey", this);
    getAccessibleContext().addPropertyChangeListener(this);
    setBorder();
  }

  /**
   * @param i temporarity disabled
   * @param j text field date format style 1=yyyMMdd, 2=yyyy-MM-dd, 3=platform default
   * @param k popup weekday Title Style 0=no name, 1=short, 2=long
   */
  public JMyCalendarDropdownField(int i, int j, int k) {
    this(i, j, k, false, new Date(), null, null, false);
  }

  public JMyCalendarDropdownField(int i, int j) {
    this(i, j, 0);
  }

  private void pressedMenuItem(int k) {
    int unit = options[k][0];
    int num = options[k][1];
    if (unit == 0 && num == -1) {
      field.setText("");
    } else if (unit == 0 && num == -2) {
      actionPerformed(new ActionEvent(this, 0, "showcal"));
    } else {
      Calendar cal = Calendar.getInstance();
      cal.add(unit, num);
      calendar.setDate(cal);
      if (useOptionNameText)
        field.setText(optionNames[k]);
      else
        field.setText(formatter.format(cal.getTime()));
    }
  }

  public void setBorder() {
    //javax.swing.border.Border border = new LineBorder(java.awt.Color.black, 1); //AssistManager.getBorder("TextField.border");
    javax.swing.border.Border border = UIManager.getBorder("TextField.border");
    //javax.swing.border.Border border = new javax.swing.border.EmptyBorder(0,0,0,0);
    if (!border.equals(getBorder())) {
      setBorder(border);
      repaint();
    }
  }

  public JMyCalendarDropdownField(int i) {
    this(i, 3);
  }

  public void setPosition(int i) {
    position = i;
  }

  public SimpleDateFormat sdFormatter(int i) {
    SimpleDateFormat simpledateformat;
    switch (i) {
      case 1: // '\001'
        simpledateformat = new SimpleDateFormat("yyyyMMdd");
        break;

      case 2: // '\002'
        simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
        break;

      default:
        //java.util.Locale locale = AssistManager.getPreferredLocale();
        java.util.Locale locale = Locale.getDefault();
        simpledateformat = (SimpleDateFormat)DateFormat.getDateInstance(1, locale);
        break;
    }
    return simpledateformat;
  }

  public void setEnabled(boolean flag) {
    button.setEnabled(flag);
    field.setEnabled(flag);
  }

  public JButton getButton() {
    return button;
  }

  public JTextField getTextField() {
    return field;
  }

  public String getText() {
    return field.getText();
  }

  public void setText(String s) {
    field.setText(s);
  }

  public Date getDate() {
    if (isNullable && field.getText().trim().length() == 0) {
      return null;
    } else {
      return calendar.getDate().getTime();
    }
  }

  /**
   * Private access due to nullable nature of the field.
   */
  private Calendar getCalendar() {
    return calendar.getDate();
  }

  public void setDate(Date date) {
    if (isNullable && date == null) {
      field.setText("");
    } else {
      //GregorianCalendar gregoriancalendar = new GregorianCalendar(AssistManager.getPreferredLocale());
      GregorianCalendar gregoriancalendar = new GregorianCalendar(Locale.getDefault());
      gregoriancalendar.setTime(date);
      calendar.setDate(gregoriancalendar);
      field.setText(formatter.format(calendar.getDate().getTime()));
    }
  }

  public void setDate(GregorianCalendar gregoriancalendar) {
    if (isNullable && gregoriancalendar == null) {
      field.setText("");
    } else {
      calendar.setDate(gregoriancalendar);
      field.setText(formatter.format(calendar.getDate().getTime()));
    }
  }

  public void actionPerformed(ActionEvent actionevent) {
    if (actionevent.getActionCommand() == "showcal") {
      if (calendar.isVisible())
        unPop();
      else
        pop();
    } else
      if (actionevent.getActionCommand() == "Date rolled back") {
        unPop();
        button.requestFocus();
      } else
        if (actionevent.getActionCommand() == "Date committed") {
          field.setText(formatter.format(calendar.getDate().getTime()));
          unPop();
          button.requestFocus();
        }
  }

  public void pop() {
    if (layeredPane == null)
      layeredPane = getParentLayeredPane();
    if (!calendar.isVisible()) {
      Point point = getLocation();
      Dimension dimension = getSize();
      Container container = getParent();
      Point point1;
      if (position == 0)
        point1 = SwingUtilities.convertPoint(container, point.x, point.y - calendar.getHeight(), layeredPane);
      else
        point1 = SwingUtilities.convertPoint(container, point.x, point.y + dimension.height, layeredPane);
      calendar.setLocation(point1);
      calendar.setVisible(true);
      layeredPane.add(calendar, JLayeredPane.POPUP_LAYER);
      layeredPane.moveToFront(calendar);
    }
    calendar.requestFocus();
  }

  public void unPop() {
    if (layeredPane != null && calendar.isVisible()) {
      calendar.setVisible(false);
      layeredPane.remove(calendar);
      layeredPane.repaint();
    }
  }

  private JLayeredPane getParentLayeredPane() {
    Container container = getParent();
    JLayeredPane jlayeredpane = null;
    for (Container container1 = container; container1 != null && jlayeredpane == null; container1 = container1.getParent())
      if (container1 instanceof RootPaneContainer)
        jlayeredpane = ((RootPaneContainer) container1).getLayeredPane();
      else
        if (container1 instanceof JLayeredPane)
          jlayeredpane = (JLayeredPane)container1;
    return jlayeredpane;
  }

  public Dimension getCalendarDropDownSize() {
    return calendar.getPreferredSize();
  }

  public void propertyChange(PropertyChangeEvent propertychangeevent) {
    String propName = propertychangeevent.getPropertyName();
    if (propertychangeevent.getSource() == this) {
      if (propName.equals("labeledBy")) {
        Object obj = propertychangeevent.getNewValue();
        if (obj != null && (obj instanceof JLabel)) {
          JLabel jlabel = (JLabel)obj;
          button.getAccessibleContext().setAccessibleName(jlabel.getText());
          field.getAccessibleContext().setAccessibleName(jlabel.getText());
        }
      } else
        if (propName.equals("UAKey")) {
          Object obj1 = propertychangeevent.getNewValue();
          field.putClientProperty("UAKey", obj1);
          button.putClientProperty("UAKey", obj1);
        }
    } else
      if (propertychangeevent.getSource() == getAccessibleContext() && propName.equals("AccessibleName")) {
        String s1 = (String)propertychangeevent.getNewValue();
        field.getAccessibleContext().setAccessibleName(s1);
        button.getAccessibleContext().setAccessibleName(s1);
      }
  }

  public void focusGained(FocusEvent focusevent) {
    prevDate = field.getText();
  }

  public void focusLost(FocusEvent focusevent) {
    String fieldStr = field.getText().trim();
    if (!fieldStr.equals(prevDate)) {
      if (isNullable && fieldStr.length() == 0) {
        // no-op
      } else {
        boolean flag = formatter.isLenient();
        formatter.setLenient(false);
        try {
          Date date = formatter.parse(fieldStr);
          GregorianCalendar gregoriancalendar = new GregorianCalendar();
          gregoriancalendar.setTime(date);
          calendar.setDate(gregoriancalendar);
        } catch (ParseException parseexception) {
          field.setText(prevDate);
          // see if it one of the optionNames
          if (useOptionNameText && optionNames != null) {
            for (int i=0; i<optionNames.length; i++) {
              if (optionNames[i].equalsIgnoreCase(fieldStr)) {
                pressedMenuItem(i);
                break;
              }
            }
          }
        }
        formatter.setLenient(flag);
      }
    }
  }


  public static void main(String[] args) {
    JPanel panel = new JPanel(new BorderLayout());
    //final JMyCalendarDropdownField p = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1);
    final JMyCalendarDropdownField p = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1, true, null, 
        new String[] { "Never", "One Day", "One Week", "Two Weeks", "One Month", "Custom..." }, 
        new int[][] { { 0, -1 },
                      { Calendar.DAY_OF_MONTH, 1 },
                      { Calendar.WEEK_OF_YEAR, 1 },
                      { Calendar.WEEK_OF_YEAR, 2 },
                      { Calendar.MONTH, 1 },
                      { 0, -2 } }, true, null);
    JButton ok = new JButton("Print");
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.out.println("calendar="+p.getDate());
      }
    });
    panel.add(p, "North");
    panel.add(new JLabel(), "Center");
    panel.add(ok, "South");
    JFrame f = new JFrame();
    f.getContentPane().add(panel);
    f.pack();
    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      public void windowDeactivated(WindowEvent e) {
        System.out.println();
        System.out.println("calendar="+p.getDate());
      }
      public void windowClosed(WindowEvent e) {
        System.exit(0);
      }
    });
  }

}