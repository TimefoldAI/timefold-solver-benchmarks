package ai.timefold.solver.benchmarks.micro.factorial;

public class Level<T> {
    private final T value;

    public Level(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
