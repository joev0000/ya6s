/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import java.util.function.Predicate;

import joev.ya6s.W65C02S;

public class NotExpression implements Predicate<W65C02S> {
  private final Predicate<W65C02S> cpuPredicate;
  public NotExpression(Predicate<W65C02S> cpuPredicate) {
    this.cpuPredicate = cpuPredicate;
  }

  @Override
  public boolean test(W65C02S cpu) {
    return !cpuPredicate.test(cpu);
  }

  @Override
  public String toString() {
    return String.format("NOT (%s)", cpuPredicate);
  }
}

