package joev.ya6s.smartline;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static joev.ya6s.smartline.Termios.*;

class Smartline implements AutoCloseable {
  private final InputStream in;
  private final OutputStream out;
  private Termios.termios origTermios = null;
  private final List<String> history = new ArrayList<>();

  public Smartline(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;

    if(in.equals(System.in)) {
      Termios.termios termios = new Termios.termios();
      origTermios = new Termios.termios();
      Termios.instance.tcgetattr(0, origTermios);
      Termios.instance.tcgetattr(0, termios);
      termios.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
      termios.c_oflag &= ~(OPOST);
      termios.c_cflag |=  (CS8);
      termios.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
      termios.c_cc[VMIN] = 0;
      termios.c_cc[VTIME] = 1;
      Termios.instance.tcsetattr(0, 0, termios);
    }
  }

  private enum State {
    START,
    ESCAPE,
    CSI
  }

  public String readLine(String prompt) throws IOException {
    StringBuilder sb = new StringBuilder();
    State state = State.START;
    boolean done = false;
    int cursor = 0;
    int historyCursor = history.size() - 1;
    System.out.print(prompt);
    while(!done) {
      int c = read();
      if(c != -1) {
        switch(state) {
          case START:
            switch(c) {
              case '\r': done = true; break;
              case 0x7F:
                if(cursor > 0) {
                  sb.deleteCharAt(--cursor);
                  // CUB, DCH
                  System.out.format("%c%c%c%c", 0x9B, 0x44, 0x9B, 0x50);
                }
                break;
              case 0x1B: state = State.ESCAPE; break;
              case 0x9B: state = State.CSI; break;
              default:
                if(cursor > sb.length()) {
                  sb.append((char)c);
                }
                else {
                  System.out.format("%c%c", 0x9B, '@');
                  sb.insert(cursor, (char)c);
                }
                System.out.print((char)c);
                cursor++;
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
            switch(c) {
              case 'A': // CUP
                if(historyCursor >= 0) {
                  if(cursor > 0) {
                    out.write(String.format("\u009B%dD", cursor).getBytes());
                  }
                  sb = new StringBuilder(history.get(historyCursor));
                  cursor = sb.length();
                  out.write(String.format("\u009BK%s", sb.toString()).getBytes());
                  if(historyCursor > 0) {
                    historyCursor--;
                  }
                }
                state = State.START;
                break;
              case 'B': // CUD
                if(historyCursor < history.size() - 1) {
                  historyCursor++;
                  if(cursor > 0) {
                    out.write(String.format("\u009B%dD", cursor).getBytes());
                  }
                  sb = new StringBuilder(history.get(historyCursor));
                  cursor = sb.length();
                  out.write(String.format("\u009BK%s", sb.toString()).getBytes());
                }
                state = State.START;
                break;
              case 'C': // CUF
                if(cursor < sb.length()) {
                  out.write("\u009BC".getBytes());
                  cursor++;
                }
                state = State.START;
                break;
              case 'D': // CUB
                if(cursor > 0) {
                  out.write("\u009BD".getBytes());
                  cursor--;
                }
                state = State.START;
                break;
            }
        }
      }
    }
    addHistory(sb.toString());
    return sb.toString();
  }

  private void addHistory(String string) {
    history.add(string);
    if(history.size() > 1000) {
      history.remove(0);
    }
  }

  public int read() throws IOException {
    return in.read();
  }

  public void close() {
    if(origTermios != null) {
      Termios.instance.tcsetattr(1, 0, origTermios);
    }
  }

  public static void main(String[] args) throws Exception {
    try(Smartline sl = new Smartline(System.in, System.out)) {
      String s;
      while(!(s = sl.readLine(">>> ")).equals("exit")) {
        System.out.format("\r\nI read: %s [ ", s);
        byte[] b = s.getBytes();
        for(int i = 0 ; i < b.length; i++) {
          System.out.format("%02X ", b[i]);
        }
        System.out.print("]\r\n");
      }
      System.out.print("\r\n");
    }
  }
}
