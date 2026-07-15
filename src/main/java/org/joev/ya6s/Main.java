/* Copyright (C) 2021-2026 Joseph Vigneau */

package org.joev.ya6s;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joev.ya6s.Clock;
import org.joev.ya6s.monitor.Command;
import org.joev.ya6s.monitor.Monitor;
import org.joev.ya6s.monitor.MonitorParser;
import org.joev.ya6s.monitor.ParseException;
import org.joev.ya6s.signals.Signal;
import org.joev.ya6s.signals.Bus;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
  public static void main(String[] args) throws Exception {

    final Backplane backplane = new Backplane();
    PipedInputStream uartIn = new PipedInputStream();
    PipedOutputStream toUartIn = new PipedOutputStream(uartIn);
    Monitor.ttyIn = uartIn;
    Monitor.ttyOut = System.out;

    final W65C02S cpu = new W65C02S(backplane);
    final Clock clock = new Clock(backplane.clock());
    final Signal resb = cpu.resb();
    resb.value(true);
    backplane.be().value(true);
    Terminal terminal = TerminalBuilder.builder()
        .system(true)
        .build();
    Monitor monitor = new Monitor(backplane, clock, cpu, terminal, toUartIn);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        monitor.close();
      }
      catch (IOException e) {
        // best-effort restore on shutdown
      }
    }));
    if(args.length > 0) {
      Files.lines(Path.of(args[0]))
        .map(String::trim)
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

  /**
   * This class cannot be instantiated.
   */
  private Main() {
    // private default constructor
  }
}
