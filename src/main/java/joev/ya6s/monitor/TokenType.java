/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

/**
 * An enumeratuion of the types of tokens that can be found in a monitor
 * command string.
 */
enum TokenType {
  EQUALS,
  PERIOD,
  DECIMAL,
  HEXADECIMAL,
  ALPHABETICAL,
  ALPHANUMERIC,
  JAVA_IDENTIFIER,
  STRING,
  RELATIVE,
}
