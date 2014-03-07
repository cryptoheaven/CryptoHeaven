/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.applet.*;
import java.net.URL;
import java.util.Hashtable;
import javax.sound.sampled.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class SoundsPlayerImpl implements SoundsPlayerI {

  private static Hashtable audioClipsHT = new Hashtable();

  private static SingleTokenArbiter singleTokenArbiter = new SingleTokenArbiter();
  private static Object singleSoundKey = new Object();

  /**
   * No-args constructor for the factory.
   */
  public SoundsPlayerImpl() {
  }

  public void play(int audioClipIndex) {
    ClipPlayer player = new ClipPlayer(audioClipIndex);
    player.setDaemon(true);
    player.start();
  }

  private class ClipPlayer extends ThreadTraced {
    private int clipIndex;

    private ClipPlayer(int audioClipIndex) {
      super("ClipPlayer");
      this.clipIndex = audioClipIndex;
    }
    public void runTraced() {

        boolean isError = false;
        URL sourceURL = null;
        try {
          sourceURL = Sounds.getClip(clipIndex);
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
              Integer clipI = new Integer(clipIndex);
              AudioClip audioClip = (AudioClip) audioClipsHT.get(clipI);
              if (audioClip == null) {
                audioClip = Applet.newAudioClip(sourceURL);
                audioClipsHT.put(clipI, audioClip);
              }
              audioClip.play();
              Thread.sleep(2000);
              audioClip.stop();
              Thread.sleep(500);
            } catch (Throwable t) {
            }
            singleTokenArbiter.removeToken(singleSoundKey, singleSoundToken);
          } // end if putToken()
        }
    } // end run
  } // end private class ClipPlayer

}