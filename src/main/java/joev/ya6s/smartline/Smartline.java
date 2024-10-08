/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.smartline;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static joev.ya6s.smartline.Termios.*;


/**
 * Smartline is a raw-mode command line helper tool, providing lightweight
 * interactive editing of commands.
 */
public final class Smartline implements AutoCloseable {
  private static final byte[] VT_CSI = new byte[] { 0x1B, 0x5B };
  private static final byte[] VT_CUB = new byte[] { 0x1B, 0x5B, 0x44 };
  private static final byte[] VT_CUF = new byte[] { 0x1B, 0x5B, 0x43 };
  private static final byte[] VT_DCH = new byte[] { 0x1B, 0x5B, 0x50 };
  private static final byte[] VT_EL  = new byte[] { 0x1B, 0x5B, 0x4B };
  private static final byte[] VT_ICH = new byte[] { 0x1B, 0x5B, 0x40 };

  private final InputStream in;
  private final PrintStream out;
  private Termios.termios origTermios = null;
  private final List<String> history = new ArrayList<>();

  /**
   * Create a new Smartline with the given input and output streams.
   *
   * TODO: Make this non-smart when using an Inputstream that's not System.in
   *
   * @param in the InputStream, if System.in, will activate Smartine editing.
   * @param out the OutputStream
   */
  public Smartline(InputStream in, OutputStream out) {
    this.in = in;
    this.out = (out instanceof PrintStream) ? (PrintStream)out : new PrintStream(out, true, UTF_8);

    if(in.equals(System.in)) {
      Termios.termios termios = new Termios.termios();
      origTermios = new Termios.termios();
      Termios.instance.tcgetattr(0, origTermios);
      Termios.instance.tcgetattr(0, termios);
      termios.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
      termios.c_cflag |=  (CS8);
      termios.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
      termios.c_cc[VMIN] = 0;
      termios.c_cc[VTIME] = 0;
      Termios.instance.tcsetattr(0, TCSAFLUSH, termios);

      Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }
  }

  /**
   * The states of the internal state machine to parse ECMA-48 sequences
   */
  private enum State {
    START,
    ESCAPE,
    CSI
  }

  /**
   * Read a line. Uses ECMA-48 sequences to allow in-line editing and history.
   *
   * @param prompt the prompt to display before reading.
   * @return a line of input
   * @throws IOException if anything goes wrong.
   */
  public String readLine(String prompt) throws IOException {
    StringBuilder sb = new StringBuilder();
    Termios.termios termios = new Termios.termios();
    Termios.instance.tcgetattr(0, termios);
    termios.c_cc[VMIN] = 1;
    termios.c_cc[VTIME] = 0;
    Termios.instance.tcsetattr(0, TCSAFLUSH, termios);
    State state = State.START;
    boolean done = false;
    int cursor = 0;
    int historyCursor = history.size() - 1;
    out.print(prompt);
    while(!done) {
      int c = read();
      if(c != -1) {
        switch(state) {
          case START:
            switch(c) {
              case '\r': done = true; break;
              case 0x08: case 0x7F:
                if(cursor > 0) {
                  sb.deleteCharAt(--cursor);
                  out.write(VT_CUB);
                  out.write(VT_DCH);
                }
                break;
              case 0x1B: state = State.ESCAPE; break;
              case 0x9B: state = State.CSI; break;
              default:
                if(c > 0x1f) { // TODO: use control keys for editing?
                  if(cursor > sb.length()) {
                    sb.append((char)c);
                  }
                  else {
                    out.write(VT_ICH);
                    sb.insert(cursor, (char)c);
                  }
                  out.print((char)c);
                  cursor++;
                }
            }
            break;
          case ESCAPE:
            if(c == '[') {
              state = State.CSI;
            }
            else {
              state = State.START;
            }
            break;
          case CSI:
            switch (c) {
              case 'A' -> { // CUP
                if (historyCursor >= 0) {
                  if (cursor > 0) {
                    // CUB
                    out.write(VT_CSI);
                    out.write(Integer.toString(cursor).getBytes(UTF_8));
                    out.write('D');
                  }
                  sb = new StringBuilder(history.get(historyCursor));
                  cursor = sb.length();
                  // EL
                  out.write(VT_EL);
                  out.write(sb.toString().getBytes(UTF_8));
                  if (historyCursor > 0) {
                    historyCursor--;
                  }
                }
                state = State.START;
              }
              case 'B' -> { // CUD
                if (historyCursor < history.size() - 1) {
                  historyCursor++;
                  if (cursor > 0) {
                    // CUB
                    out.write(VT_CSI);
                    out.write(Integer.toString(cursor).getBytes(UTF_8));
                    out.write('D');
                  }
                  sb = new StringBuilder(history.get(historyCursor));
                  cursor = sb.length();
                  // EL
                  out.write(VT_EL);
                  out.write(sb.toString().getBytes(UTF_8));
                }
                state = State.START;
              }
              case 'C' -> { // CUF
                if (cursor < sb.length()) {
                  out.write(VT_CUF);
                  cursor++;
                }
                state = State.START;
              }
              case 'D' -> { // CUB
                if (cursor > 0) {
                  // CUB
                  out.write(VT_CUB);
                  cursor--;
                }
                state = State.START;
              }
              default -> {
                state = State.START;
              }
            }
        }
      }
    }
    addHistory(sb.toString());
    out.write('\n');
    Termios.instance.tcgetattr(0, termios);
    termios.c_cc[VMIN] = 0;
    termios.c_cc[VTIME] = 0;
    Termios.instance.tcsetattr(0, TCSAFLUSH, termios);
    return sb.toString();
  }

  /**
   * Add a line to history, drop the oldest one if more than
   * 1000 lines have been added.
   */
  private void addHistory(String string) {
    history.add(string);
    if(history.size() > 1000) {
      history.remove(0);
    }
  }

  /**
   * Attempt to read a single byte.
   *
   * @return a byte, or -1 if none is available.
   */
  public int read() throws IOException {
    return in.read();
  }

  /**
   * Close the Smartline, reverting the terminal to normal mode.
   */
  public void close() {
    if(origTermios != null) {
      Termios.instance.tcsetattr(1, 0, origTermios);
    }
  }

  /**
   * A small driver program to exercise Smartline.
   */
  public static void main(String[] args) throws Exception {
    try(Smartline sl = new Smartline(System.in, System.out)) {
      String s;
      while(!(s = sl.readLine(">>> ")).equals("exit")) {
        System.out.format("I read: %s [ ", s);
        byte[] b = s.getBytes(UTF_8);
        for (byte value : b) {
          System.out.format("%02X ", value);
        }
        System.out.println("]");
      }
    }
  }
}
