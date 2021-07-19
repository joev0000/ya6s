package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * Command to read out data from the Backplane.
 */
public class ReadCommand implements Command {
  /**
   * A mapping of byte to terminal-friendly chars to be used in the READ
   * command. Uses a simple mapping of the Unicode Latin-1 Supplimental
   * to bytes 128 through 255.
   */
  public final static char[] ascii = {
    '.', '.', '.', '.', '.', '.', '.', '.',  '.', '.', '.', '.', '.', '.', '.', '.',
    '.', '.', '.', '.', '.', '.', '.', '.',  '.', '.', '.', '.', '.', '.', '.', '.',
    ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
    '0', '1', '2', '3', '4', '5', '6', '7',  '8', '9', ':', ';', '<', '=', '>', '?',
    '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G',  'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',  'X', 'Y', 'Z', '[', '\\',']', '^', '_',
    '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g',  'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
    'p', 'q', 'r', 's', 't', 'u', 'v', 'w',  'x', 'y', 'z', '{', '|', '}', '~', '.',
    '.', '.', '.', '.', '.', '.', '.', '.',  '.', '.', '.', '.', '.', '.', '.', '.',
    '.', '.', '.', '.', '.', '.', '.', '.',  '.', '.', '.', '.', '.', '.', '.', '.',
    '.', '¡', '¢', '£', '¤', '¥', '¦', '§',  '¨', '©', 'ª', '«', '¬', '.', '®', '¯',
    '°', '±', '²', '³', '´', 'µ', '¶', '·',  '¸', '¹', 'º', '»', '¼', '½', '¾', '¿',
    'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç',  'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï',
    'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', '×',  'Ø', 'Ù', 'Ú', 'Û', 'Ü', 'Ý', 'Þ', 'ß',
    'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç',  'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï',
    'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö', '÷',  'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ'
  };

  private short start;
  private short end;

  /**
   * Create a Read command with the given start location, and an end location
   * that is 255 bytes beyond.
   *
   * @param start the start location
   */
  public ReadCommand(short start) {
    this.start = start;
    this.end =   (short)(start + 255);
  }

  /**
   * Create a Read command with the given start and end location.
   *
   * @param start the start location.
   * @param end the end location.
   */
  public ReadCommand(short start, short end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Execute the read command.
   *
   * @param monitor the Monitor which this command will execute against.
   * @return a suggested next Command.
   */
  @Override
  public Command execute(Monitor monitor) {
    Backplane backplane = monitor.backplane();
    W65C02 cpu = monitor.cpu();

    Signal rdy = cpu.rdy();
    Signal clock = backplane.clock();
    Signal rwb = backplane.rwb();
    Bus address = backplane.address();
    Bus data = backplane.data();

    short alignedStart = (short)(start & 0xFFF0);
    short alignedEnd = (short)(end | 0x000F);


    boolean oldRdy = rdy.value();
    rdy.value(false);
    System.out.println("       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF");
    short loc = alignedStart;
    byte[] line = new byte[16];
    while(loc < alignedEnd) {
      System.out.format("%04X: ", loc);
      for(int i = 0; i < 16; i++) {
        clock.value(false);
        address.value(loc++);
        rwb.value(true);
        clock.value(true);
        line[i] = (byte)data.value();
      }
      for(int i = 0; i < 8; i++) {
        System.out.format("%02X ", line[i]);
      }
      System.out.print(" ");
      for(int i = 8; i < 16; i++) {
        System.out.format("%02X ", line[i]);
      }
      System.out.print(" |");
      for(int i = 0; i < 8; i++) {
        System.out.print(ascii[line[i] & 0xFF]);
      }
      System.out.print(" ");
      for(int i = 8; i < 16; i++) {
        System.out.print(ascii[line[i] & 0xFF]);
      }
      System.out.println("|");
    }
    rdy.value(oldRdy);
    return null;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return "read {start} {end}"
   */
  @Override
  public String toString() {
    return String.format("read %04X %04X", start, end);
  }
}
