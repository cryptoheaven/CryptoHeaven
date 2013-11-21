/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import com.CH_gui.gui.MyHTMLEditor;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.text.JTextComponent;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* Class Description: Wrapper for optional spell checker module.
* 
* @author  Marcin Kurzawa
*/
public class SpellCheckerWrapper {

  public static String PROPERTY__MAX_AVAIL_LANGS = "MaxAvailLangs";
  private static boolean isSpellerAvailable = false;

  static {
    // Report if we have the Spell Checking module available.
    try {
      Class.forName("comx.tig.en.SingleTigerSession");
      Class.forName("comx.Tiger.gui.TigerBkgChecker");
      isSpellerAvailable = true;
    } catch (Throwable t) {
      System.out.println("Spell checking module is not available.");
    }
  }

  public static JDialog buildOptionsDialog(Frame parent) {
    //JTigerOptionsDialog.buildDialog(parent);
    JDialog dialog = null;
    if (isSpellerAvailable) {
      try {
        dialog = (JDialog) Class.forName("comx.Tiger.gui.JTigerOptionsDialog").getMethod("buildOptionsDialog_reflection", new Class[] { Frame.class }).invoke(null, new Object[] { parent });
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("buildOptionsDialog: "+Misc.objToStr(dialog));
    return dialog;
  }

  public static JDialog buildUserDialog(Frame parent) {
    //JTigerUserDialog.buildDialog(parent);
    JDialog dialog = null;
    if (isSpellerAvailable) {
      try {
        dialog = (JDialog) Class.forName("comx.Tiger.gui.JTigerUserDialog").getMethod("buildUserDialog_reflection", new Class[] { Frame.class }).invoke(null, new Object[] { parent });
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("buildUserDialog: "+Misc.objToStr(dialog));
    return dialog;
  }

  public static JDialog buildCheckDialog(Frame parent, JTextComponent textComp) {
    //JDialog dialog = JTigerCheckDialog.buildDialog(parent, textComp);
    JDialog dialog = null;
    if (isSpellerAvailable) {
      try {
        dialog = (JDialog) Class.forName("comx.Tiger.gui.JTigerCheckDialog").getMethod("buildCheckDialog_reflection", new Class[] { Frame.class, JTextComponent.class }).invoke(null, new Object[] { parent, textComp });
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("buildCheckDialog: "+Misc.objToStr(dialog));
    return dialog;
  }

  public static int countLanguageLexicons() {
    int count = 0;
    if (isSpellerAvailable) {
      try {
        count = ((Integer) Class.forName("comx.tig.en.SingleTigerSession").getMethod("countLanguageLexicons_reflection", null).invoke(null, null)).intValue();
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("countLanguageLexicons: "+Misc.objToStr(""+count));
    //comx.tig.en.SingleTigerSession.countLanguageLexicons()
    return count;
  }
  
  public static int countUserLexicons() {
    int count = 0;
    if (isSpellerAvailable) {
      try {
        Object session = Class.forName("comx.tig.en.SingleTigerSession").getMethod("getSingleInstance_reflection", null).invoke(null, null);
        count = ((Integer) session.getClass().getMethod("countUserLexicons_reflection", null).invoke(session, null)).intValue();
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("countUserLexicons: "+Misc.objToStr(""+count));
    return count;
    //TigerPropSession speller = SingleTigerSession.getSingleInstance();
    //int count = speller.countUserLexicons();
  }

  public static String[] getAvailableLanguages() {
    String[] langs = null;
    if (isSpellerAvailable) {
      try {
        langs = (String[]) Class.forName("comx.tig.en.SingleTigerSession").getMethod("getAvailableLanguages_reflection", null).invoke(null, null);
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("getAvailableLanguages: "+Misc.objToStr(langs));
    return langs;
  }
 
  public static SpellCheckerI getSpellChecker() {
    SpellCheckerI speller = null;
    if (isSpellerAvailable) {
      try {
        speller = (SpellCheckerI) Class.forName("comx.Tiger.gui.TigerBkgChecker").getMethod("createNewChecker_reflection", null).invoke(null, null);
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("getSpellChecker: "+Misc.objToStr(speller));
    return speller;
  }
  
  public static MouseAdapter newSpellCheckerMouseAdapter(MyHTMLEditor editor) {
    MouseAdapter adapter = null;
    if (isSpellerAvailable) {
      try {
        adapter = (MouseAdapter) Class.forName("comx.Tiger.gui.TigerMouseAdapter").getMethod("createNewAdapter_reflection", new Class[] { Object.class }).invoke(null, new Object[] { editor });
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("newSpellCheckerMouseAdapter: "+Misc.objToStr(adapter));
    return adapter;
  }
  
  public static Properties getProperties() {
    Properties props = null;
    if (isSpellerAvailable) {
      try {
        Object o = Class.forName("comx.tig.en.SingleTigerSession").getMethod("getSingleInstance_reflection", null).invoke(null, null);
        props = (Properties) o.getClass().getMethod("getProperties_reflection", null).invoke(o, null);
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
//    System.out.println("getProperties: "+Misc.objToStr(props));
    return props;
  }
  
  public static void setOptionsFromProperties(Properties props) {
    if (isSpellerAvailable) {
      try {
        Object o = Class.forName("comx.tig.en.SingleTigerSession").getMethod("getSingleInstance_reflection", null).invoke(null, null);
        o.getClass().getMethod("setOptionsFromProperties_reflection", new Class[] { Properties.class, String.class }).invoke(o, new Object[] { props, null });
      } catch (Exception e) {
  //      e.printStackTrace();
      }
    }
    //comx.Tiger.gui.TigerPropSession tigerSession = comx.tig.en.SingleTigerSession.getSingleInstance();
    //tigerSession.setOptionsFromProperties(userSettingsRecord.spellingProps, null);
  }
}