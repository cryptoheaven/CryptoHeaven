/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.HTTP_Socket;

import java.io.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class LargePipedOutputStream extends OutputStream {

  /* REMIND: identification of the read and write sides needs to be
     more sophisticated.  Either using thread groups (but what about
     pipes within a thread?) or using finalization (but it may be a
     long time until the next GC). */
  private LargePipedInputStream sink;

  /**
   * Creates a piped output stream connected to the specified piped
   * input stream. Data bytes written to this stream will then be
   * available as input from <code>snk</code>.
   *
   * @param      snk   The piped input stream to connect to.
   * @exception  IOException  if an I/O error occurs.
   */
  public LargePipedOutputStream(LargePipedInputStream snk) throws IOException {
    connect(snk);
  }

  /**
   * Creates a piped output stream that is not yet connected to a
   * piped input stream. It must be connected to a piped input stream,
   * either by the receiver or the sender, before being used.
   *
   * @see     LargePipedInputStream#connect(LargePipedOutputStream)
   * @see     LargePipedOutputStream#connect(LargePipedInputStream)
   */
  public LargePipedOutputStream() {
  }

  /**
   * Connects this piped output stream to a receiver. If this object
   * is already connected to some other piped input stream, an
   * <code>IOException</code> is thrown.
   * <p>
   * If <code>snk</code> is an unconnected piped input stream and
   * <code>src</code> is an unconnected piped output stream, they may
   * be connected by either the call:
   * <blockquote><pre>
   * src.connect(snk)</pre></blockquote>
   * or the call:
   * <blockquote><pre>
   * snk.connect(src)</pre></blockquote>
   * The two calls have the same effect.
   *
   * @param      snk   the piped input stream to connect to.
   * @exception  IOException  if an I/O error occurs.
   */
  public void connect(LargePipedInputStream snk) throws IOException {
    if (snk == null) {
      throw new NullPointerException();
    } else {
      synchronized (snk) {
        if (sink != null || snk.connected) {
          throw new IOException("Already connected");
        }
        sink = snk;
        snk.in = -1;
        snk.out = 0;
        snk.connected = true;
      }
    }
  }

  /**
   * Writes the specified <code>byte</code> to the piped output stream.
   * If a thread was reading data bytes from the connected piped input
   * stream, but the thread is no longer alive, then an
   * <code>IOException</code> is thrown.
   * <p>
   * Implements the <code>write</code> method of <code>OutputStream</code>.
   *
   * @param      b   the <code>byte</code> to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public void write(int b) throws IOException {
    if (sink == null) {
      throw new IOException("Pipe not connected");
    }
    sink.receive(b);
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array
   * starting at offset <code>off</code> to this piped output stream.
   * If a thread was reading data bytes from the connected piped input
   * stream, but the thread is no longer alive, then an
   * <code>IOException</code> is thrown.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   */
  public void write(byte b[], int off, int len) throws IOException {
    if (sink == null) {
      throw new IOException("Pipe not connected");
    } else if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    sink.receive(b, off, len);
  }

  /**
   * Flushes this output stream and forces any buffered output bytes
   * to be written out.
   * This will notify any readers that bytes are waiting in the pipe.
   *
   * @exception IOException if an I/O error occurs.
   */
  public void flush() throws IOException {
    if (sink != null) {
      synchronized (sink) {
        sink.notifyAll();
      }
    }
  }

  /**
   * Closes this piped output stream and releases any system resources
   * associated with this stream. This stream may no longer be used for
   * writing bytes.
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public void close() throws IOException {
    if (sink != null) {
      sink.receivedLast();
    }
  }
}