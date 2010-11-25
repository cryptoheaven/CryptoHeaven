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

import java.net.URL;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Sounds extends Object {

  public static boolean DEBUG__SUPPRESS_ALL_SOUNDS = false;

  public static final String SOUND_ENABLEMENT_PROPERTY = "audioEnabled";

  // used for sound suppression by for eg: TestAlive
  private static boolean soundSuppressed = false;

  private static Class soundsPlayerImpl;

  public static final int DIALOG_ERROR;
  public static final int DIALOG_WARN;
  public static final int DIALOG_INFO;
  public static final int DIALOG_QUESTION;

  public static final int UPDATE_CLIP;
  public static final int TRANSFER_DONE;
  public static final int ONLINE;
  public static final int OFFLINE;
  public static final int YOU_WERE_AUTHORIZED;
  public static final int WINDOW_POPUP;
  public static final int RING_BELL;
  //public static final int RING_OUT;

  private static final String clipNames[];
  private static final long clipPlayStamps[];
  private static long MIN_CLIP_TIME_APART = 1000;


  static {
    int i = 0;
    clipNames = new String[12];
    clipPlayStamps = new long[12];

    DIALOG_ERROR = i;
    clipNames[i] = "errorDialog.wav"; i++;

    DIALOG_WARN = i;
    clipNames[i] = "warnDialog.wav"; i++;

    DIALOG_INFO = i;
    clipNames[i] = "infoDialog.wav"; i++;

    DIALOG_QUESTION = i;
    clipNames[i] = "questionDialog.wav"; i++;

    UPDATE_CLIP = i;
    clipNames[i] = "updateClip.wav"; i++;

    TRANSFER_DONE = i;
    clipNames[i] = "transferDone.wav"; i++;

    ONLINE = i;
    clipNames[i] = "online.wav"; i++;

    OFFLINE  = i;
    clipNames[i] = "offline.wav"; i++;

    YOU_WERE_AUTHORIZED = i;
    clipNames[i] = "youWereAuthorized.wav"; i++;

    WINDOW_POPUP = i;
    clipNames[i] = "windowPopup.wav"; i++;

    RING_BELL = i;
    clipNames[i] = "ringBell.wav"; i++;

  }

  public static void setImpl(Class implSoundsPlayerI) {
    soundsPlayerImpl = implSoundsPlayerI;
  }

  public static URL getClip(int audioClipIndex) {
    return URLs.getResourceURL("sounds/" + clipNames[audioClipIndex]);
  }

  /**
   * Set a flag to suppress all Sound
   */
  public static void suppressAllSound() {
    soundSuppressed = true;
  }
  public static boolean isAllSoundsuppressed() {
    return soundSuppressed;
  }

  public static void playAsynchronous(int audioClipIndex) {
    if (!DEBUG__SUPPRESS_ALL_SOUNDS && !soundSuppressed && soundsPlayerImpl != null) {
      boolean isSoundEnabled = Boolean.valueOf(GlobalProperties.getProperty(SOUND_ENABLEMENT_PROPERTY, "true")).booleanValue();
      if (isSoundEnabled) {
        try {
          long now = System.currentTimeMillis();
          long lastPlayed = clipPlayStamps[audioClipIndex];
          long expired = lastPlayed + MIN_CLIP_TIME_APART;
          if (expired < now || lastPlayed > now) {
            clipPlayStamps[audioClipIndex] = now;
            SoundsPlayerI soundsPlayer = (SoundsPlayerI) soundsPlayerImpl.newInstance();
            soundsPlayer.play(audioClipIndex);
          }
        } catch (Throwable t) {
        }
      }
    }
  }

} // end class Sounds