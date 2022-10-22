/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.Map;

import joev.ya6s.expression.Constant;
import joev.ya6s.expression.Register;
import joev.ya6s.expression.RelationalExpression;

import org.junit.jupiter.api.Test;

public class MonitorParserTests {
  protected Command parse(String s) throws ParseException {
    MonitorParser parser = new MonitorParser(new StringReader(s));
    return parser.command();
  }

  @Test
  public void bad() {
    assertThrows(ParseException.class, () -> parse("bad"));
  }

  @Test
  public void exit() throws ParseException {
    assertEquals(parse("exit"), ExitCommand.instance());
  }

  @Test
  public void reset() throws ParseException {
    assertEquals(parse("reset"), ResetCommand.instance());
  }

  @Test
  public void cont() throws ParseException {
    assertEquals(parse("cont"), ContinueCommand.instance());
  }

  @Test
  public void step() throws ParseException {
    assertEquals(parse("step"), StepCommand.instance());
  }

  @Test
  public void profileOn() throws ParseException {
    assertEquals(parse("profile on"), ProfileOnCommand.instance());
  }

  @Test
  public void profileOff() throws ParseException {
    assertEquals(parse("profile off"), ProfileOffCommand.instance());
  }

  @Test
  public void profileReset() throws ParseException {
    assertEquals(parse("profile reset"), ProfileResetCommand.instance());
  }

  @Test
  public void profileShowAll() throws ParseException {
    assertEquals(parse("profile show"), new ProfileShowCommand(Integer.MAX_VALUE));
  }

  @Test
  public void profileShowLines() throws ParseException {
    assertEquals(parse("profile show 23"), new ProfileShowCommand(23));
  }

  @Test
  public void readStartAndEnd() throws ParseException {
    assertEquals(parse("read 1bbb 1ccc"), new ReadCommand((short)0x1bbb, (short)0x1ccc));
  }

  @Test
  public void readStartOnly() throws ParseException {
    assertEquals(parse("read 1000"), new ReadCommand((short)0x1000));
  }

  @Test
  public void disassembleAddressOnly() throws ParseException {
    assertEquals(parse("disassemble f000"), new DisassembleCommand((short)0xf000, 10));
  }

  @Test
  public void disassembleAddressAndCount() throws ParseException {
    assertEquals(parse("disassemble f000 23"), new DisassembleCommand((short)0xf000, 23));
  }

  @Test
  public void write() throws ParseException {
    assertEquals(parse("write 2000 11 ff 22 ee"), new WriteCommand((short)0x2000, new byte[] { 0x11, (byte)0xff, 0x22, (byte)0xee }));
  }

  @Test
  public void attach() throws ParseException {
    assertEquals(parse("attach foo.bar.Baz name1=\"value1!!!\" name2=abcd"),
        new AttachCommand("foo.bar.Baz", Map.of("name1", "value1!!!", "name2", "abcd")));
  }

  @Test
  public void breakpointList() throws ParseException {
    assertEquals(parse("breakpoint list"), BreakpointListCommand.instance());
  }

  @Test
  public void breakpointRemove() throws ParseException {
    assertEquals(parse("breakpoint remove 3"), new BreakpointRemoveCommand(3));
  }

  @Test
  public void breakpointAt() throws ParseException {
    assertEquals(parse("break at 4223"),
        new BreakpointWhenCommand(new RelationalExpression(
            RelationalExpression.Op.EQUALS,
            Register.PC,
            new Constant(0x4223))));
  }

  @Test
  public void breakpointWhenGTE() throws ParseException {
    assertEquals(parse("break when x >= 13"),
        new BreakpointWhenCommand(new RelationalExpression(
            RelationalExpression.Op.GREATER_THAN_OR_EQUALS,
            Register.X,
            new Constant(0x13))));
  }
}
