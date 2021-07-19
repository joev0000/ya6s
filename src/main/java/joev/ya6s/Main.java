package joev.ya6s;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import joev.ya6s.monitor.Command;
import joev.ya6s.monitor.Monitor;
import joev.ya6s.monitor.MonitorParser;
import joev.ya6s.monitor.ParseException;
import joev.ya6s.signals.Signal;

public class Main {
  public static void main(String[] args) throws Exception {
    final Backplane backplane = new Backplane();
    PipedInputStream uartIn = new PipedInputStream();
    PipedOutputStream toUartIn = new PipedOutputStream(uartIn);
    Monitor.ttyIn = uartIn;
    Monitor.ttyOut = System.out;

    final W65C02 cpu = new W65C02(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);

    Monitor monitor = new Monitor(backplane, cpu, System.in, System.out, toUartIn);
    if(args.length > 0) {
      Files.lines(Path.of(args[0]))
        .map(l -> l.trim())
        .filter(l -> l.length() > 0)
        .forEach(l -> {
          try {
            MonitorParser parser = new MonitorParser(new StringReader(l));
            Command command = parser.command();
            command.execute(monitor);
          }
          catch (ParseException pe) {
            System.err.format("Error parsing \"%s\": %s%n", l, pe.getMessage());
          }
        });
    }
    monitor.run();
  }
}
