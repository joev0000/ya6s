package joev.ya6s;

/**
 * A record that represents a single machine cycle.
 *
 * @param vpb the negative logic value of the Vector Pull signal.
 * @param mlb the negative logic value of the Memory Lock signal.
 * @param sync the value of the Sync signal.
 * @param address the Register whose value will be used to set the address bus.
 * @param data the Register whose value will be used to set the data bus.
 * @param rwb the value of the Read / Not Write signal.
 */
public record Cycle(boolean vpb, boolean mlb, boolean sync,
    Address address, Data data, boolean rwb) {
}
