/*
 * Utils.java
 *
 * Created on July 24, 2002, 4:15 PM
 */

package comx.Jaguar.gui;

import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  marcin
 * @version
 */
public class Utils {

  private static final JTextComponent.KeyBinding[] defaultBindings = {
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), DefaultEditorKit.pasteAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), DefaultEditorKit.cutAction),
  };

  /** 
   * load default bindings
   */
  public static void initKeyBindings(JTextComponent c) {
    Keymap k = c.getKeymap();
    JTextComponent.loadKeymap(k, defaultBindings, c.getActions());
  }

  public static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
    '\r', '\f', '.', ',', ':', '-', '(', ')', '[', ']', '{',
    '}', '<', '>', '/', '|', '\\', '\'', '\"'};

  public static boolean isSeparator(char ch) {
    for (int k=0; k<WORD_SEPARATORS.length; k++)
      if (ch == WORD_SEPARATORS[k])
        return true;
    return false;
  }


  public static String colorToHex(Color color) {
    String s = "#";
    String s1 = Integer.toHexString(color.getRed());
    if(s1.length() > 2) {
      s1 = s1.substring(0, 2);
    } else {
      if(s1.length() < 2)
        s = s + "0" + s1;
      else
        s = s + s1;
    }
    s1 = Integer.toHexString(color.getGreen());
    if(s1.length() > 2) {
      s1 = s1.substring(0, 2);
    } else {
      if(s1.length() < 2)
        s = s + "0" + s1;
      else
        s = s + s1;
    }
    s1 = Integer.toHexString(color.getBlue());
    if(s1.length() > 2) {
      s1 = s1.substring(0, 2);
    } else {
      if(s1.length() < 2)
        s = s + "0" + s1;
      else
        s = s + s1;
    }
    return s;
  }
}