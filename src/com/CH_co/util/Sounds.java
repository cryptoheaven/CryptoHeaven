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

import java.applet.*;
import java.net.URL;
import javax.sound.sampled.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Sounds extends Object {

  public static boolean DEBUG__SUPPRESS_ALL_SOUNDS = false;

  public static final String SOUND_ENABLEMENT_PROPERTY = "audioEnabled";

  // used for sound suppression by for eg: TestAlive
  private static boolean soundSuppressed = false;

  public static final int DIALOG_ERROR;
  public static final int DIALOG_WARN;
  public static final int DIALOG_INFO;
  public static final int DIALOG_QUESTION;

  public static final int UPDATE_CLIP;
  public static final int TRANSFER_DONE;
  public static final int ONLINE;
  public static final int YOU_WERE_AUTHORIZED;
  public static final int WINDOW_POPUP;
  public static final int RING_BELL;
  //public static final int RING_OUT;

  private static final String clipNames[];
  private static AudioClip[] audioClips;


  static {
    int i = 0;
    clipNames = new String[11];
    audioClips = new AudioClip[clipNames.length];

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

    YOU_WERE_AUTHORIZED = i;
    clipNames[i] = "youWereAuthorized.wav"; i++;

    WINDOW_POPUP = i;
    clipNames[i] = "windowPopup.wav"; i++;

    RING_BELL = i;
    clipNames[i] = "ringBell.wav"; i++;

//    RING_OUT = i;
//    clipNames[i] = "ringOut.wav"; i++;
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

  private static SingleTokenArbiter singleTokenArbiter = new SingleTokenArbiter();
  private static Object singleSoundKey = new Object();

  public static void playAsynchronous(int audioClipIndex) {
    if (!soundSuppressed) {
      boolean isSoundEnabled = Boolean.valueOf(GlobalProperties.getProperty(SOUND_ENABLEMENT_PROPERTY, "true")).booleanValue();
      if (isSoundEnabled) {
        ClipPlayer player = new ClipPlayer(audioClipIndex);
        player.setDaemon(true);
        player.start();
      }
    }
  }
  private static class ClipPlayer extends Thread {
    private int clipIndex;

    private ClipPlayer(int audioClipIndex) {
      super("ClipPlayer");
      this.clipIndex = audioClipIndex;
    }
    public void run() {
      if (!DEBUG__SUPPRESS_ALL_SOUNDS) {
        boolean isError = false;
        try {
          URL sourceURL = URLs.getResourceURL("sounds/" + clipNames[clipIndex]);
          AudioInputStream ais = AudioSystem.getAudioInputStream(sourceURL);
          if (!ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
              ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);
          AudioFormat	audioFormat = ais.getFormat();
          DataLine.Info	info = new DataLine.Info(Clip.class, audioFormat);
          final Clip clip = (Clip) AudioSystem.getLine(info);
          clip.open(ais);
          clip.addLineListener(new LineListener() {
            public void update(LineEvent event) {
              if (clip.isOpen() && !clip.isRunning() && clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
                clip.close();
              }
            }
          });
          clip.start();
        } catch (Throwable th) {
          isError = true;
        }
        if (isError) {
          Object singleSoundToken = new Object();
          if (singleTokenArbiter.putToken(singleSoundKey, singleSoundToken)) {
            try {
              if (audioClips[clipIndex] == null) {
                audioClips[clipIndex] = Applet.newAudioClip(URLs.getResourceURL("sounds/" + clipNames[clipIndex]));
              }
              audioClips[clipIndex].play();
              Thread.sleep(2000);
              audioClips[clipIndex].stop();
              Thread.sleep(500);
            } catch (Throwable t) {
            }
            singleTokenArbiter.removeToken(singleSoundKey, singleSoundToken);
          } // end if putToken()
        }
      } // end if no debug
    } // end run
  } // end private class ClipPlayer

} // end class Sounds