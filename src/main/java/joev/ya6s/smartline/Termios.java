package joev.ya6s.smartline;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

public interface Termios extends Library {
  Termios instance = Native.load("c", Termios.class);

  @FieldOrder({"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed", "c_ospeed"})
  public static class termios extends Structure {
    public static final int NCCS = 32;
    public int c_iflag; // tcflag_t
    public int c_oflag; // tcflag_t
    public int c_cflag; // tcflag_t
    public int c_lflag; // tcglag_t
    public byte c_line; // cc_t
    public byte[] c_cc = new byte[NCCS]; // cc_t
    public int c_ispeed; // speed_t
    public int c_ospeed; // speed_t
  }

  // c_iflag bits
  public static final int IGNBRK = 0000001;
  public static final int BRKINT = 0000002;
  public static final int IGNPAR = 0000004;
  public static final int PARMRK = 0000010;
  public static final int INPCK  = 0000020;
  public static final int ISTRIP = 0000040;
  public static final int INLCR  = 0000100;
  public static final int IGNCR  = 0000200;
  public static final int ICRNL  = 0000400;
  public static final int IUCLC  = 0001000;
  public static final int IXON   = 0002000;
  public static final int IXANY  = 0004000;
  public static final int IXOFF  = 0010000;
  public static final int IMAXBEL= 0020000;
  public static final int IUTF8  = 0040000;

  // c_oflag bits
  public static final int OPOST  = 0000001;
  public static final int OLCUC  = 0000002;
  public static final int ONLCR  = 0000004;
  public static final int OCRNL  = 0000010;
  public static final int ONOCR  = 0000020;
  public static final int ONLRET = 0000040;
  public static final int OFILL  = 0000100;
  public static final int OFDEL  = 0000200;

  // c_lflag bits
  public static final int ISIG   = 0000001;
  public static final int ICANON = 0000002;
  public static final int XCASE  = 0000004;
  public static final int ECHO   = 0000010;
  public static final int ECHOE  = 0000020;
  public static final int ECHOK  = 0000040;
  public static final int ECHONL = 0000100;
  public static final int NOFLSH = 0000200;
  public static final int TOSTOP = 0000400;
  public static final int IEXTEN = 0100000;

  // c_cc characters
  public static final int VINTR = 0;
  public static final int VQUIT = 1;
  public static final int VERASE = 2;
  public static final int VKILL = 3;
  public static final int VEOF = 4;
  public static final int VTIME = 5;
  public static final int VMIN = 6;
  public static final int VSWTC = 7;
  public static final int VSTART = 8;
  public static final int VSTOP = 9;
  public static final int VSUSP = 10;
  public static final int VEOL = 11;
  public static final int VREPRINT = 12;
  public static final int VDISCARD = 13;
  public static final int VWERASE = 14;
  public static final int VLNEXT = 15;
  public static final int VEOL2 = 16;

  // c_cflags
  public static final int CSIZE   = 0000060;
  public static final int   CS5   = 0000000;
  public static final int   CS6   = 0000020;
  public static final int   CS7   = 0000040;
  public static final int   CS8   = 0000060;
  public static final int CSTOPB  = 0000100;
  public static final int CREAD   = 0000200;
  public static final int PARENB  = 0000400;
  public static final int PARODD  = 0001000;
  public static final int HUPCL   = 0002000;
  public static final int CLOCAL  = 0004000;

  public static final int TCSANOW = 0;
  public static final int TCSADRAIN = 1;
  public static final int TCSAFLUSH = 2;

  public int tcgetattr(int filedes, termios termios_p);
  public int tcsetattr(int filedes, int optional_actions, termios termios_p);

  public int read(int filedes, int[] buf, int nbyte);
}
