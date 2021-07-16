package joev.ya6s.expression;

import java.util.function.Predicate;

import joev.ya6s.W65C02;

public class NotExpression implements Predicate<W65C02> {
  private final Predicate<W65C02> cpuPredicate;
  public NotExpression(Predicate<W65C02> cpuPredicate) {
    this.cpuPredicate = cpuPredicate;
  }

  @Override
  public boolean test(W65C02 cpu) {
    return !cpuPredicate.test(cpu);
  }

  @Override
  public String toString() {
    return String.format("NOT (%s)", cpuPredicate);
  }
}

