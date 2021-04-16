package joev.ya6s;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import joev.ya6s.monitor.Monitor;
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
    monitor.run();
  }
}
