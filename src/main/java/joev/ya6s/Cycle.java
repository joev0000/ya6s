package joev.ya6s;

public record Cycle(boolean vpb, boolean mlb, boolean sync,
    Register address, Register data, boolean rwb) {
}
