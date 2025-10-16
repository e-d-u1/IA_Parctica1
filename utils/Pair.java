package utils;

public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getKey() { return first; }
    public B getValue() { return second; }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
