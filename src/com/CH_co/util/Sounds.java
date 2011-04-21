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

package com.CH_co.util;

import com.CH_co.trace.ThreadTraced;
import java.net.URL;

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
  private static final Object dispatchMonitor = new Object();

  public static final int DIALOG_ERROR;
  public static final int DIALOG_WARN;
  public static final int DIALOG_INFO;
  public static final int DIALOG_QUESTION;

  public static final int UPDATE_CLIP;
  public static final int UPDATE_CLIP_CHAT;
  public static final int TRANSFER_DONE;
  public static final int ONLINE;
  public static final int OFFLINE;
  public static final int YOU_WERE_AUTHORIZED;
  public static final int WINDOW_POPUP;
  public static final int RING_BELL;
  //public static final int RING_OUT;

  private static final String clipNames[];
  private static final long clipPlayStamps[];
  private static long MIN_CLIP_TIME_APART = 999;


  static {
    int i = 0;
    clipNames = new String[13];
    clipPlayStamps = new long[13];

    DIALOG_ERROR = i;
    clipNames[i] = "dialog-error.wav"; i++;

    DIALOG_WARN = i;
    clipNames[i] = "dialog-notify.wav"; i++;

    DIALOG_INFO = i;
    clipNames[i] = "dialog-notify.wav"; i++;

    DIALOG_QUESTION = i;
    clipNames[i] = "dialog-notify.wav"; i++;

    UPDATE_CLIP = i;
    clipNames[i] = "updateClip.wav"; i++;

    UPDATE_CLIP_CHAT = i;
    clipNames[i] = "updateClipChat.wav"; i++;

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

  public static void playAsynchronous(final int audioClipIndex) {
    if (!DEBUG__SUPPRESS_ALL_SOUNDS && !soundSuppressed && soundsPlayerImpl != null) {
      boolean isSoundEnabled = Boolean.valueOf(GlobalProperties.getProperty(SOUND_ENABLEMENT_PROPERTY, "true")).booleanValue();
      if (isSoundEnabled) {
        Thread th = new ThreadTraced("Asynch Sound Dispatcher") {
          public void runTraced() {
            try {
              // Pospone Chat Update clip as it maybe overwritten by Window-Popup clip
              if (audioClipIndex == UPDATE_CLIP_CHAT) {
                try { Thread.sleep(100); } catch (InterruptedException e) { }
              }
              // synchronize to avoid dispatching multiple clip players at the same time will possibly the same clip
              synchronized (dispatchMonitor) {
                long now = System.currentTimeMillis();
                long lastPlayed = clipPlayStamps[audioClipIndex];
                long expired = lastPlayed + MIN_CLIP_TIME_APART;
                if (expired < now || lastPlayed > now) {
                  clipPlayStamps[audioClipIndex] = now;
                  // if window-slide-popup, also mark this time for chat-update-clip as we don't want them to overlap and slider sound should take presedence
                  if (audioClipIndex == WINDOW_POPUP)
                    clipPlayStamps[UPDATE_CLIP_CHAT] = now;
                  // if any update clip, then mark time for all other update clips to prevent sound overlaps
                  if (audioClipIndex == UPDATE_CLIP || audioClipIndex == UPDATE_CLIP_CHAT) {
                    clipPlayStamps[UPDATE_CLIP] = now;
                    clipPlayStamps[UPDATE_CLIP_CHAT] = now;
                  }
                  SoundsPlayerI soundsPlayer = (SoundsPlayerI) soundsPlayerImpl.newInstance();
                  soundsPlayer.play(audioClipIndex);
                }
              }
            } catch (Throwable t) {
            }
          }
        };
        th.setDaemon(true);
        th.start();
      }
    }
  }

} // end class Sounds