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

package com.CH_cl.util;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.File;
import java.net.URL;
import java.util.*;
import javax.sound.sampled.*;

/**
 * Playing an encoded audio file
 * Compressed formats can be handled depending on the
 * capabilities of the Java Sound implementation it is run with.
 * A-law and &mu;-law can be handled in any known Java Sound
 * implementation. Ogg vorbis, mp3 and GSM 06.10 can be handled by
 * <ulink url="http://www.tritonus.org/">Tritonus</ulink>.
 * If you want to play these formats with the Sun jdk1.3/1.4,
 * you have to install the respective plug-ins from
 * <ulink url="http://www.tritonus.org/plugins.html">Tritonus
 * Plug-ins</ulink>. Depending on the Java Sound implementation,
 * this program may or may not play unencoded files. <p>
 *
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 */

public class DecodingAudioClipPlayer {

  private static Hashtable filesPlayingHT;

  public static void pauseSeek(Object fileOrURL, int millisecondPosition, Object controllingObj) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(fileOrURL);
    if (player != null) {
      player.pauseSeek(millisecondPosition, controllingObj);
    }
  }

  public static void play(Object fileOrURL) {
    play(fileOrURL, null);
  }
  public static void play(Object fileOrURL, CallbackI callback) {
    play(fileOrURL, callback, 0);
  }
  public static synchronized void play(Object fileOrURL, CallbackI callback, int millisecondPosition) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(fileOrURL);
    if (player == null) {
      playFile(fileOrURL, callback, millisecondPosition);
    } else {
      player.play(callback, millisecondPosition);
    }
  }

  public static void pause(File file, CallbackI callback) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(file);
    if (player != null) {
      player.pause(callback);
    }
  }

  public static void seek(File file, double fractionalPosition) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(file);
    if (player != null) {
      player.seek(fractionalPosition);
    }
  }

  public static void seekPlayIfPaused(File file, int millisecondPosition, Object controllingObj) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(file);
    if (player != null) {
      player.seekPlayIfPaused(millisecondPosition, controllingObj);
    }
  }

  public static void close(File file) {
    if (filesPlayingHT == null) filesPlayingHT = new Hashtable();
    ClipControl player = (ClipControl) filesPlayingHT.get(file);
    if (player != null) {
      player.close();
    }
  }

  private static void playFile(final Object fileOrURL, final CallbackI callback, final int millisecondPosition) {
    AudioInputStream ais = null;
    Clip clip = null;
    try {
      if (fileOrURL instanceof File)
        ais = AudioSystem.getAudioInputStream((File) fileOrURL);
      else if (fileOrURL instanceof URL)
        ais = AudioSystem.getAudioInputStream((URL) fileOrURL);
      else
        throw new IllegalArgumentException("Don't know how to handle " + fileOrURL);
      if (!ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
          ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);
      AudioFormat	audioFormat = ais.getFormat();

      DataLine.Info	info = new DataLine.Info(Clip.class, audioFormat);
      clip = (Clip) AudioSystem.getLine(info);
      final Clip _clip = clip;
      clip.open(ais);
      clip.addLineListener(new LineListener() {
        public void update(LineEvent event) {
          if (_clip.isOpen()) {
            if (!_clip.isRunning() && _clip.getMicrosecondPosition() == _clip.getMicrosecondLength()) {
              //_clip.close();
              ClipControl player = (ClipControl) filesPlayingHT.get(fileOrURL);
              player.close();
//              if (player != null)
//                player.notifyClose();
//              filesPlayingHT.remove(fileOrURL);
            } else if (!_clip.isRunning()) {
              ClipControl player = (ClipControl) filesPlayingHT.get(fileOrURL);
              if (player != null)
                player.notifyPause();
            } else {
              ClipControl player = (ClipControl) filesPlayingHT.get(fileOrURL);
              if (player != null)
                player.notifyPlay();
            }
          }
        }
      });
      if (millisecondPosition > 0)
        clip.setMicrosecondPosition(millisecondPosition*1000);
      clip.start();
      // clip is already loaded so close the source stream
      ais.close();

      // Create a thread to play back the data and start it running.  It will run
      // until all the data has been played back or stopPlayer() is called.
      ClipControl player = new ClipControl(fileOrURL, clip, callback);
      filesPlayingHT.put(fileOrURL, player);
      player.start();
      player.notifyPlay();
    } catch (Throwable e) {
      try { ais.close(); } catch (Throwable t) { }
      try { clip.close(); } catch (Throwable t) { }
    }
  }

  private static class ClipControl extends Thread {
    Object fileOrURL;
    Clip clip;
    Vector callbacksV;
    Object controllingObj;
    boolean controllingObjPaused;
    public ClipControl(Object fileOrURL, Clip clip, CallbackI callback) {
      this.fileOrURL = fileOrURL;
      this.clip = clip;
      this.callbacksV = new Vector();
      if (callback != null)
        this.callbacksV.addElement(callback);
    }
    public synchronized void close() {
      notifyClose();
      filesPlayingHT.remove(fileOrURL);
      clip.stop();
      clip.flush();
      clip.close();
    }
    private void notifyClose() {
      for (int i=0; i<callbacksV.size(); i++)
        ((CallbackI) callbacksV.elementAt(i)).callback("close");
    }
    private void notifyLength() {
      for (int i=0; i<callbacksV.size(); i++)
        ((CallbackI) callbacksV.elementAt(i)).callback(new Double(clip.getMicrosecondLength()/1000));
    }
    private void notifyPause() {
      for (int i=0; i<callbacksV.size(); i++)
        ((CallbackI) callbacksV.elementAt(i)).callback("pause");
    }
    private void notifyPlay() {
      for (int i=0; i<callbacksV.size(); i++)
        ((CallbackI) callbacksV.elementAt(i)).callback("play");
    }
    private void notifyPosition() {
      for (int i=0; i<callbacksV.size(); i++) {
        ((CallbackI) callbacksV.elementAt(i)).callback(new Integer((int) (clip.getMicrosecondPosition()/1000000)));
        ((CallbackI) callbacksV.elementAt(i)).callback(new Long(clip.getMicrosecondPosition()/1000));
      }
    }
    public synchronized void pauseSeek(int millisecondPosition, Object controllingObj) {
      if (this.controllingObj == null) {
        this.controllingObj = controllingObj;
        this.controllingObjPaused = clip.isRunning();
        clip.stop();
      }
      clip.setMicrosecondPosition(millisecondPosition*1000);
      notifyPosition();
    }
    public synchronized void pause(CallbackI callback) {
      if (!callbacksV.contains(callback))
        callbacksV.addElement(callback);
      notifyPause();
      if (clip.isRunning()) {
        clip.stop();
        notifyPosition();
      }
    }
    public synchronized void play(CallbackI callback, int millisecondPosition) {
      if (!callbacksV.contains(callback))
        callbacksV.addElement(callback);
      notifyLength();
      notifyPlay();
      if (!clip.isRunning()) {
        if (millisecondPosition >= 0) {
          clip.flush();
          clip.setMicrosecondPosition(millisecondPosition*1000);
          notifyPosition();
        }
        clip.start();
      }
    }
    public synchronized void seek(double fractionalPosition) {
      clip.flush();
      clip.setMicrosecondPosition((long) (clip.getMicrosecondLength()*fractionalPosition));
      notifyPosition();
//      clip.flush();
//      clip.setMicrosecondPosition((long) (clip.getMicrosecondLength()*fractionalPosition));
//      notifyPosition();
    }
    public synchronized void seekPlayIfPaused(int millisecondPosition, Object controllingObj) {
      if (this.controllingObj != null && this.controllingObj.equals(controllingObj)) {
        this.controllingObj = null;
        if (!clip.isRunning()) {
          clip.flush();
          clip.setMicrosecondPosition(millisecondPosition*1000);
          notifyPosition();
          if (this.controllingObjPaused)
            clip.start();
        }
      }
    }
    public void run() {
      try {
        if (callbacksV != null) {
          Thread th = new ThreadTraced("ClipControl") {
            public void runTraced() {
              notifyLength();
              while (clip.isOpen()) {
                synchronized (ClipControl.this) {
                  if (clip.isRunning()) {
                    notifyPosition();
                  }
                }
                try { Thread.sleep(20); } catch (InterruptedException e) { }
              }
            }
          };
          th.setDaemon(true);
          th.start();
        }
      } catch (Throwable e) {
      }
    }//end run
  }//end inner class ClipControl


  public static void printUsageAndExit() {
    System.out.println("DecodingAudioPlayer: usage:");
    System.out.println("\tjava DecodingAudioPlayer <soundfile>");
    System.exit(1);
  }


  public static void main(String[] args) {
    if (args.length != 1) {
      printUsageAndExit();
    }
    String	strFilename = args[0];
    File file = new File (strFilename);
    play(file);
  }

}