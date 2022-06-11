/* Copyright (C) 2021, 2022 Joseph Vigneau */

module joev.ya6s {
  requires com.sun.jna;

  exports joev.ya6s;
  exports joev.ya6s.signals;
  exports joev.ya6s.smartline to com.sun.jna;
}
