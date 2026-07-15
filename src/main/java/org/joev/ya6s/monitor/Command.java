/* Copyright (C) 2021-2026 Joseph Vigneau */

package org.joev.ya6s.monitor;

/**
 * A Monitor command that can be executed.
 */
public interface Command {
  /**
   * Execute the command with the given Backplane and CPU.
   *
   * @param monitor the Monitor which will execute this command.
   * @return a suggested next Command, or null if there is no suggestion.
   */
  Command execute(Monitor monitor);
}
