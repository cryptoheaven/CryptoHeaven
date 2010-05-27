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

package com.CH_gui.msgs;

/**
 * This program demonstrates the capture and subsequent playback of audio data.
 * Input data from a microphone is captured and saved in a
 * ByteArrayOutputStream object when the user clicks the Capture button.
 * Data capture stops when the user clicks the Stop button.
 * Playback begins when the user clicks the Playback button.
 * Audio file output is also supported. <p>
 *
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 */


import com.CH_cl.service.ops.DownloadUtilities;
import com.CH_co.trace.*;
import com.CH_co.util.*;
import com.CH_gui.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;

public class AudioCapturePanel extends JPanel implements DisposableObj {

  private static int BUFFER_SIZE = 256;
  //private static String GSM_ENCODING = "GSM0610";

  MsgComposeManagerI composeMngrI;

  boolean isCapturing = false;
  boolean isPausing = false;
  boolean isPlaying = false;
  boolean isWriting = false;

  ByteArrayOutputStream byteArrayOutputStream;
  AudioFormat byteArrayAudioFormat;
  File audioFile;
  AudioFormat systemAudioFormat;

  //JCheckBox jCompressCheck = null;
  JButton jCaptureBtn = null;
  JButton jPlayBtn = null;
  JButton jStopBtn = null;
  JButton jWriteBtn = null;
  JLabel jNotesLabel = null;

  String STR_RECORD = "   Record   ";
  String STR_PLAY = " Playback ";
  String STR_STOP = " Stop ";
  String STR_PAUSE = "Pause";
  String STR_RESUME = "Resume";
  String STR_ATTACH = " Attach >> ";

  Vector tempFilesToCleanupV;

  public AudioCapturePanel(MsgComposeManagerI composeMngrI) {//constructor
    this.composeMngrI = composeMngrI;

    jCaptureBtn = new JMyButton(STR_RECORD);
    jPlayBtn = new JMyButton(STR_PLAY);
    jStopBtn = new JMyButton(STR_STOP);
    jWriteBtn = new JMyButton(STR_ATTACH);
    jNotesLabel = new JMyLabel();

    jPlayBtn.setEnabled(false);
    jStopBtn.setEnabled(false);
    jWriteBtn.setVisible(false);

    //jCompressCheck = new JMyCheckBox("Compress Audio");
    jCaptureBtn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));
    jPlayBtn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));
    jStopBtn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));
    jWriteBtn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));

    setLayout(new FlowLayout(FlowLayout.LEFT));
    //if (jCompressCheck != null) add(jCompressCheck);
    add(jCaptureBtn);
    add(jPlayBtn);
    add(jStopBtn);
    add(jNotesLabel);
    add(jWriteBtn);

    //Register anonymous listeners
    //add(compressBtn);
    jCaptureBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Capture input data from the microphone until the Stop button is clicked.
        if (!isCapturing) {
          captureAudio();
        } else {
          isPausing = !isPausing;
        }
        setEnabledButtons();
      }//end actionPerformed
    });
    jStopBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Terminate capture and playback
        isCapturing = false;
        isPlaying = false;
        isPausing = false;
        setEnabledButtons();
      }//end actionPerformed
    });
    jPlayBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Play back all of the data that was saved during capture.
        if (!isPlaying) {
          playAudio();
        } else {
          isPausing = !isPausing;
        }
        setEnabledButtons();
      }//end actionPerformed
    });//end addActionListener()
    jWriteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Write data to file
        if (!isWriting) {
          attach();
        }
      }//end actionPerformed
    });//end addActionListener()
  }//end constructor

  public boolean anyCapturedAndNotAttached() {
    return byteArrayOutputStream != null;
  }

  public void attach() {
    File file = writeAudioToFile();
    if (file != null && composeMngrI != null)
      composeMngrI.addAttachment(file);
    setEnabledButtons();
  }

  private void setEnabledButtons() {
    // fix the component sizes so they don't change when their "text" changes
    jCaptureBtn.setSize(jCaptureBtn.getSize());
    jCaptureBtn.setPreferredSize(jCaptureBtn.getSize());
    jPlayBtn.setSize(jPlayBtn.getSize());
    jPlayBtn.setPreferredSize(jPlayBtn.getSize());
    jStopBtn.setSize(jStopBtn.getSize());
    jStopBtn.setPreferredSize(jStopBtn.getSize());

    jCaptureBtn.setEnabled(!isPlaying);
    jCaptureBtn.setText(!isCapturing ? STR_RECORD : (isPausing ? STR_RESUME : STR_PAUSE));
    jPlayBtn.setEnabled(!isCapturing && ((byteArrayOutputStream != null && byteArrayOutputStream.size() > 0) || audioFile != null));
    jPlayBtn.setText(!isPlaying ? STR_PLAY : (isPausing ? STR_RESUME : STR_PAUSE));
    jStopBtn.setEnabled(isCapturing || isPlaying || isPausing);
    boolean enableWrite = !isCapturing && !isWriting && byteArrayOutputStream != null && byteArrayOutputStream.size() > 0;
    jWriteBtn.setEnabled(enableWrite);
    jWriteBtn.setVisible(enableWrite);
  }

  //This method captures audio input from a microphone and saves it in a ByteArrayOutputStream object.
  private void captureAudio() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AudioCapturePanel.class, "captureAudio()");
    isCapturing = true;
    isPlaying = false;
    isPausing = false;
    try {
      // discard the previous buffer
      byteArrayOutputStream = null;
      // Get everything set up for capture
      if (systemAudioFormat == null)
        systemAudioFormat = getAudioFormat();
      DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, systemAudioFormat);
      TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
      targetLine.open(systemAudioFormat);
      targetLine.start();
      //System.out.println("Capture Line "+dataLineInfo);

      //Create a thread to capture the microphone data and start it running.  It will run until the Stop button is clicked.
      new CaptureThread(targetLine).start();
    } catch (Throwable e) {
      if (trace != null) trace.exception(AudioCapturePanel.class, 100, e);
      isCapturing = false;
      jNotesLabel.setText(e.getMessage());
    }//end catch
    if (trace != null) trace.exit(AudioCapturePanel.class);
  }//end captureAudio method

  //This method plays back the audio data that has been saved in the ByteArrayOutputStream
  private void playAudio() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AudioCapturePanel.class, "playAudio()");
    isPlaying = true;
    isPausing = false;
    isCapturing = false;
    try {
      //Get everything set up for playback.
      //Get the previously-saved data into a byte array object.
      AudioInputStream ais = null;
      if (byteArrayOutputStream != null) {
        byte[] audioData = byteArrayOutputStream.toByteArray();
        //Get an input stream on the
        // byte array containing the data
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        ais = new AudioInputStream(byteArrayInputStream, byteArrayAudioFormat, audioData.length / byteArrayAudioFormat.getFrameSize());
        //System.out.println("byte array "+ais.getFormat());
      } else if (audioFile != null) {
        ais = AudioSystem.getAudioInputStream(audioFile);
        //System.out.println("sound file "+ais.getFormat());
      }
      if (!ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
        ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);
        //System.out.println("transcoding to "+ais.getFormat());
      }

      DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, systemAudioFormat);
      SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
      sourceLine.open(systemAudioFormat);
      sourceLine.start();

      // Create a thread to play back the data and start it running.  It will run
      // until all the data has been played back or Stop button is clicked.
      new PlayThread(ais, sourceLine).start();
    } catch (Throwable e) {
      if (trace != null) trace.exception(AudioCapturePanel.class, 100, e);
      isPlaying = false;
      if (byteArrayOutputStream != null)
        byteArrayOutputStream = null;
      else if (audioFile != null)
        audioFile = null;
      jNotesLabel.setText(e.getMessage());
    }//end catch
    if (trace != null) trace.exit(AudioCapturePanel.class);
  }//end playAudio

  private File writeAudioToFile() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AudioCapturePanel.class, "writeAudioToFile()");
    isWriting = true;
    try {
      audioFile = null;
      //Get everything set up for playback.
      //Get the previously-saved data into a byte array object.
      byte[] audioData = byteArrayOutputStream.toByteArray();
      int secondsWorthOfData = getSecondsWorthOfData();
      byteArrayOutputStream = null;
      //Get an input stream on the byte array containing the data
      InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
      AudioInputStream ais = new AudioInputStream(byteArrayInputStream, byteArrayAudioFormat, audioData.length / byteArrayAudioFormat.getFrameSize());
      AudioFileFormat.Type fileType = null;

//      if (byteArrayAudioFormat.getEncoding().equals(new AudioFormat.Encoding(GSM_ENCODING))) {
//        fileType = new AudioFileFormat.Type("GSM", "gsm");
//        //System.out.println("write GSM stream");
//      } else if (jCompressCheck != null && jCompressCheck.isSelected()) {
//        fileType = new AudioFileFormat.Type("GSM", "gsm");
//        AudioFormat.Encoding targetEncoding = new AudioFormat.Encoding(GSM_ENCODING);
//        ais = AudioSystem.getAudioInputStream(targetEncoding, ais);
//        //System.out.println("write transcribed to GSM stream");
//      } else {
        fileType = AudioFileFormat.Type.WAVE;
        //System.out.println("write PCM stream");
//      }

      String prefix = FileLauncher.getVoicemailPrefix();
      String suffix = "."+fileType.getExtension();
      File tempDir = DownloadUtilities.getDefaultTempDir();
      audioFile = new File(tempDir, prefix+Misc.getFormattedDateFileStr(new Date())+"-"+secondsWorthOfData+"s"+suffix);
      if (tempFilesToCleanupV == null) tempFilesToCleanupV = new Vector();
      tempFilesToCleanupV.addElement(audioFile);

      new WriteThread(ais, fileType, audioFile).start();
    } catch (Exception e) {
      if (trace != null) trace.exception(AudioCapturePanel.class, 100, e);
      isWriting = false;
      jNotesLabel.setText(e.getMessage());
    }//end catch
    if (trace != null) trace.exit(AudioCapturePanel.class, audioFile);
    return audioFile;
  }

  //This method creates and returns an AudioFormat object for a given set of format parameters.  If these
  // parameters don't work well for you, try some of the other allowable parameter values, which
  // are shown in comments following the declarations.
  private AudioFormat getAudioFormat() {
    float sampleRate = 8000.0F; //8000,11025,16000,22050,44100
    int sampleSizeInBits = 16; //8,16
    int channels = 1; //1,2
    boolean signed = true; //true,false
    boolean bigEndian = false; //true,false
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }//end getAudioFormat
//===================================//

  private int getSecondsWorthOfData() {
    int secondWorthOfBytes = (int) (byteArrayAudioFormat.getFrameRate() * byteArrayAudioFormat.getFrameSize());
    int secondsWorthOfData = byteArrayOutputStream.size() / secondWorthOfBytes + 1;
    return secondsWorthOfData;
  }

//Inner class to capture data from microphone
  class CaptureThread extends ThreadTraced {
    //An arbitrary-size temporary holding buffer
    byte buff[] = new byte[BUFFER_SIZE];
    TargetDataLine targetLine;
    public CaptureThread(TargetDataLine targetLine) {
      super("Audio Capture Thread");
      this.targetLine = targetLine;
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "CaptureThread.run()");
      AudioInputStream ais = null;
      try {
        AudioFormat.Encoding targetEncoding = null;
//        if (jCompressCheck != null && jCompressCheck.isSelected()) {
//          targetEncoding = new AudioFormat.Encoding(GSM_ENCODING);
//        } else {
          targetEncoding = AudioFormat.Encoding.ULAW;
//        }
        ais = new AudioInputStream(targetLine);
        ais = AudioSystem.getAudioInputStream(targetEncoding, ais);

        boolean anySignal = false;
        HashSet anySignalHS = new HashSet();
        long cntTotal = 0;
        byteArrayAudioFormat = ais.getFormat();
        //System.out.println("Capturing in format " + byteArrayAudioFormat);
        //Loop until stopCapture is set by another thread that services the Stop button.
        while (isCapturing) {
          //Read data from the internal buffer of the data line.
          int cnt = ais.read(buff, 0, buff.length);
          //if (cnt > 0) for (int i=0; i<cnt; i++) System.out.print(buff[i] + " ");
          if (cnt > 0) {
            cntTotal += cnt;
            if (!anySignal) {
              for (int i=0; i<cnt; i++) {
                Byte b = new Byte(buff[i]);
                anySignalHS.add(b);
              }
              anySignal = anySignalHS.size() >= 8;
            }
            if (anySignal) {
              if (!isPausing) {
                //Save data in output stream object.
                if (byteArrayOutputStream == null)
                  byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(buff, 0, cnt);
                int secondsWorthOfData = getSecondsWorthOfData();
                jNotesLabel.setText("Recording length: " + Misc.getFormattedTime(secondsWorthOfData));
              }
            } else {
              jNotesLabel.setText("No microphone signal detected.");
              // Stop capturing if no microphone signal
              if (cntTotal > 10000) isCapturing = false;
            }
            //System.out.print(""+cnt+" "+anySignal+" ");
          }//end if
        }//end while

      } catch (Throwable e) {
        if (trace != null) trace.exception(getClass(), 100, e);
        jNotesLabel.setText(e.getMessage());
      } finally {
        try { byteArrayOutputStream.close(); } catch (Throwable t) { }
        try { ais.close(); } catch (Throwable t) { }
        try { targetLine.close(); } catch (Throwable t) { }
      }
      isCapturing = false;
      setEnabledButtons();

      if (trace != null) trace.exit(getClass());
    }//end run
  }//end inner class CaptureThread
//===================================//
//Inner class to play back the data that was saved.
  class PlayThread extends ThreadTraced {
    byte buff[] = new byte[BUFFER_SIZE];
    AudioInputStream ais;
    SourceDataLine line;
    public PlayThread(AudioInputStream ais, SourceDataLine line) {
      super("Audio Play Thread");
      this.ais = ais;
      this.line = line;
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "PlayThread.run()");
      try {
        int cnt;
        //Keep looping until the input read method returns -1 for empty stream.
        while ((cnt = ais.read(buff, 0, buff.length)) != -1 && isPlaying) {
          if (cnt > 0) {
            //Write data to the internal buffer of the data line where it will be delivered to the speaker.
            line.write(buff, 0, cnt);
          }//end if
          while (isPausing) {
            Thread.sleep(10);
          }
        }//end while
        //Block and wait for internal buffer of the data line to empty.
        line.drain();
      } catch (Throwable e) {
        if (trace != null) trace.exception(getClass(), 100, e);
        jNotesLabel.setText(e.getMessage());
      } finally {
        try { ais.close(); } catch (Throwable t) { }
        try { line.close(); } catch (Throwable t) { }
      }
      isPlaying = false;
      setEnabledButtons();
      if (trace != null) trace.exit(getClass());
    }//end run
  }//end inner class PlayThread
//===================================//
//Inner class to write captured data to file
  class WriteThread extends ThreadTraced {
    AudioInputStream ais;
    AudioFileFormat.Type fileType;
    File file;
    public WriteThread(AudioInputStream ais, AudioFileFormat.Type fileType, File file) {
      super("Audio Write Thread");
      this.ais = ais;
      this.fileType = fileType;
      this.file = file;
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "WriteThread.run()");
      try {
        AudioSystem.write(ais, fileType, file);
      } catch (Throwable e) {
        if (trace != null) trace.exception(getClass(), 100, e);
        jNotesLabel.setText(e.getMessage());
      } finally {
        try { ais.close(); } catch (Throwable t) { }
      }
      isWriting = false;
      setEnabledButtons();
      if (trace != null) trace.exit(getClass());
    }//end run
  }//end inner class PlayThread
//===================================//

  private void cleanupTempFiles() {
    if (tempFilesToCleanupV != null) {
      while (tempFilesToCleanupV.size() > 0) {
        File tempFile = (File) tempFilesToCleanupV.remove(tempFilesToCleanupV.size()-1);
        boolean cleaned = false;
        try { cleaned = CleanupAgent.wipeOrDelete(tempFile); } catch (Throwable t) { }
        if (!cleaned && tempFile.exists()) {
          GlobalProperties.addTempFileToCleanup(tempFile);
        }
      }
    }
  }

  public void disposeObj() {
    // Stop any potentially active threads...
    isCapturing = false;
    isPausing = false;
    isPlaying = false;
    isWriting = false;
    cleanupTempFiles();
  }

  public static void main(String args[]) {
    try {
      JFrame frame = new JFrame("Capture/Playback/Write Test");
      frame.getContentPane().add(new AudioCapturePanel(null));
      frame.getContentPane().setLayout(new FlowLayout());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }//end main

  protected void finalize() throws Throwable {
    cleanupTempFiles();
    super.finalize();
  }
}//end outer class AudioCapturePanel.java