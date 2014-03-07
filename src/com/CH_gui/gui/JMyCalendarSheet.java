/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.*;
import javax.swing.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyCalendarSheet extends JPanel implements Serializable, ActionListener, FocusListener, KeyListener {

  public static final int No_Names = 0;
  public static final int Short_Names = 1;
  public static final int Long_Names = 2;
  public static final String Current_Date_Has_Changed = "Date changed";
  public static final String DATE_COMMITTED = "Date committed";
  public static final String DATE_ROLLEDBACK = "Date rolled back";
  protected Locale calendarLocale;
  protected int startOfWeekOffset;
  protected GregorianCalendar current;
  protected GregorianCalendar today;
  protected JLabel todayInText;
  protected JPanel dayColumnTitles;
  protected JLabel titles[];
  protected JButton previousMonth;
  protected JButton nextMonth;
  protected JButton buttonSet[];
  protected int titleStyle;
  protected ActionListener actionListener;
  private DateFormat formatter;
  private final String weekdays[];
  private final String shortWeekdays[];
  private static final String NEXT_MONTH = "Next month";
  private static final String PREV_MONTH = "Prev month";
  private JButton focusButton;

  public JMyCalendarSheet() {
    this(2);
  }

  public JMyCalendarSheet(int i) {
    calendarLocale = Locale.getDefault();
    startOfWeekOffset = Calendar.getInstance(TimeZone.getDefault(), calendarLocale).getFirstDayOfWeek() - 1;
    formatter = DateFormat.getDateInstance(1, calendarLocale);
    weekdays = (new DateFormatSymbols(calendarLocale)).getWeekdays();
    shortWeekdays = (new DateFormatSymbols(calendarLocale)).getShortWeekdays();
    focusButton = null;
    formatter.setTimeZone(TimeZone.getDefault());
    today = new GregorianCalendar(calendarLocale);
    current = new GregorianCalendar(calendarLocale);
    Date date = new Date();
    today.setTime(date);
    current.setTime(date);
    makeComponents();
    setLayout(new BorderLayout());
    JPanel jpanel = new JPanel();
    jpanel.setLayout(new BorderLayout());
    jpanel.add(nextMonth, "East");
    jpanel.add(previousMonth, "West");
    jpanel.add(todayInText, "Center");
    jpanel.add(dayColumnTitles, "South");
    add(jpanel, "North");
    JPanel jpanel1 = new JPanel();
    jpanel1.setLayout(new GridLayout(6, 7));
    for (int j = 0; j < buttonSet.length; j++) {
      jpanel1.add(buttonSet[j]);
    }
    add(jpanel1, "Center");
    setTitleStyle(i);
    showButtons();
  }

  public void focusGained(FocusEvent focusevent) {
  }

  public void focusLost(FocusEvent focusevent) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Window window = SwingUtilities.getWindowAncestor(JMyCalendarSheet.this);
        if (null != window) {
          Component component = window.getFocusOwner();
          if (null != component && !isAncestorOf(component))
            processActionEvent(new ActionEvent(this, 1001, "Date rolled back"));
        }
      }
    });
  }

  private void makeComponents() {
    Insets insets = new MyInsets(2, 2, 2, 2);
    nextMonth = new JMyButton(" >> ");
    previousMonth = new JMyButton(" << ");
    nextMonth.setActionCommand(NEXT_MONTH);
    previousMonth.setActionCommand(PREV_MONTH);
    nextMonth.addActionListener(this);
    previousMonth.addActionListener(this);
    nextMonth.addFocusListener(this);
    previousMonth.addFocusListener(this);
    nextMonth.setMargin(insets);
    previousMonth.setMargin(insets);
    todayInText = new JMyLabel("", 0);
    dayColumnTitles = new JPanel();
    dayColumnTitles.setLayout(new GridLayout(1, 7));
    titles = new JLabel[7];
    for (int i = 0; i < 7; i++) {
      titles[i] = new JMyLabel("", 0);
      dayColumnTitles.add(titles[i]);
    }

    buttonSet = new JButton[42];
    for (int j = 0; j < buttonSet.length; j++) {
      buttonSet[j] = new JMyButton("");
      buttonSet[j].setMargin(insets);
      buttonSet[j].setActionCommand(j + "");
      buttonSet[j].addActionListener(this);
      buttonSet[j].addFocusListener(this);
      buttonSet[j].addKeyListener(this);
    }
  }

  public void keyTyped(KeyEvent keyevent) {
  }

  public void keyPressed(KeyEvent keyevent) {
    if (keyevent.getKeyCode() == 37) {
      current.add(5, -1);
      showButtons();
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else if (keyevent.getKeyCode() == 39) {
      current.add(5, 1);
      showButtons();
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else if (keyevent.getKeyCode() == 38) {
      current.add(5, -7);
      showButtons();
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else if (keyevent.getKeyCode() == 40) {
      current.add(5, 7);
      showButtons();
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else if (keyevent.getKeyCode() == 10) {
      keyevent.consume();
      processActionEvent(new ActionEvent(this, 1001, "Date committed"));
    } else if (keyevent.getKeyCode() == 27) {
      keyevent.consume();
      processActionEvent(new ActionEvent(this, 1001, "Date rolled back"));
    }
  }

  public void keyReleased(KeyEvent keyevent) {
  }

  private int getColumn(int i) {
    return (i + 1) % 7 - 1;
  }

  private boolean selectButton(int i) {
    if (i < 0 || i > 41)
      return false;
    if (buttonSet[i].isVisible()) {
      buttonSet[i].requestFocusInWindow();
      return true;
    } else {
      return false;
    }
  }

  private void selectLastButton(int i) {
    byte byte0 = ((byte)(i < 0 ? 1 : 7));
    int j = i < 0 ? 41 : 35 + i;
    for (int k = j; k >= 0; k -= byte0) {
      if (!buttonSet[k].isVisible())
        continue;
      buttonSet[k].requestFocusInWindow();
      break;
    }
  }

  private void selectFirstButton(int i) {
    byte byte0 = ((byte)(i < 0 ? 1 : 7));
    int j = i < 0 ? 0 : i;
    for (int k = j; k < 42; k += byte0) {
      if (!buttonSet[k].isVisible())
        continue;
      buttonSet[k].requestFocusInWindow();
      break;
    }
  }

  public void setTitleStyle(int i) {
    if (i != 0 && i != 1 && i != 2)
      throw new IllegalArgumentException("Invalid title style specification.");
    titleStyle = i;
    if (titleStyle == 0) {
      dayColumnTitles.setVisible(false);
      doLayout();
    } else {
      for (int j = 0; j < 7; j++)
        titles[j].setText(getWeekdayName(j, titleStyle));
    }
    if (!dayColumnTitles.isVisible()) {
      dayColumnTitles.setVisible(true);
      doLayout();
    }
  }

  public int getTitleStyle() {
    return titleStyle;
  }

  public void setDate(Calendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Null date is not allowed.");
    } else {
      current.setTime(calendar.getTime());
      showButtons();
      return;
    }
  }

  public Calendar getDate() {
    return current;
  }

  protected void showButtons() {
    todayInText.setText(formatter.format(current.getTime()));
    GregorianCalendar gregoriancalendar = new GregorianCalendar();
    gregoriancalendar.set(current.get(1), current.get(2), 1);
    int i = 0;
    int j = current.get(5) - 1;
    int k = getDaysInMonth(current);
    int l = gregoriancalendar.get(7) - startOfWeekOffset;
    for (i = 0; i < l - 1; i++) {
      buttonSet[i].setVisible(false);
      buttonSet[i].setText("");
    }

    for (int i1 = 0; i1 < k; i1++) {
      buttonSet[i].setText("" + (i1 + 1));
      if (!buttonSet[i].isVisible())
        buttonSet[i].setVisible(true);
      if (i1 == j) {
        buttonSet[i].requestFocusInWindow();
        focusButton = buttonSet[i];
      }
      i++;
    }

    for (; i < buttonSet.length; i++)
      buttonSet[i].setVisible(false);
  }

  public void actionPerformed(ActionEvent actionevent) {
    JButton jbutton = (JButton)actionevent.getSource();
    if (NEXT_MONTH.equals(jbutton.getActionCommand())) {
      current.add(2, 1);
      showButtons();
      todayInText.setText(formatter.format(current.getTime()));
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else if (PREV_MONTH.equals(jbutton.getActionCommand())) {
      current.add(2, -1);
      showButtons();
      todayInText.setText(formatter.format(current.getTime()));
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
    } else {
      current.set(5, Integer.parseInt(jbutton.getText()));
      todayInText.setText(formatter.format(current.getTime()));
      showButtons();
      processActionEvent(new ActionEvent(this, 1001, "Date changed"));
      processActionEvent(new ActionEvent(this, 1001, "Date committed"));
    }
  }

  public void addActionListener(ActionListener actionlistener) {
    actionListener = AWTEventMulticaster.add(actionListener, actionlistener);
  }

  public void removeActionListener(ActionListener actionlistener) {
    actionListener = AWTEventMulticaster.remove(actionListener, actionlistener);
  }

  protected void processActionEvent(ActionEvent actionevent) {
    if (actionListener != null)
      actionListener.actionPerformed(actionevent);
  }

  public String getWeekdayName(int i, int j) {
    int k = (i + startOfWeekOffset) % 7 + 1;
    if (j == 2)
      return weekdays[k];
    else
      return shortWeekdays[k];
  }

  public int getDaysInMonth(GregorianCalendar gregoriancalendar) {
    byte byte0 = 0;
    switch (gregoriancalendar.get(2)) {
      case 0: // '\0'
      case 2: // '\002'
      case 4: // '\004'
      case 6: // '\006'
      case 7: // '\007'
      case 9: // '\t'
      case 11: // '\013'
        byte0 = 31;
        break;

      case 3: // '\003'
      case 5: // '\005'
      case 8: // '\b'
      case 10: // '\n'
        byte0 = 30;
        break;

      case 1: // '\001'
        byte0 = ((byte)(gregoriancalendar.isLeapYear(gregoriancalendar.get(1)) ? 29 : 28));
        break;
    }
    return byte0;
  }

  public void requestFocus() {
    if (focusButton != null)
      focusButton.requestFocusInWindow();
  }



  public static void main(String[] args) {
    final JMyCalendarSheet p = new JMyCalendarSheet(1);
    JFrame f = new JFrame();
    f.getContentPane().add(p);
    f.pack();
    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      public void windowDeactivated(WindowEvent e) {
        System.out.println();
      }
    });
  }


}