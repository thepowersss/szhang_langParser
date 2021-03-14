public class IntegerParse extends Parse {

    // the value of the parse
    private int value;

    public IntegerParse(int value, int index) {
        super("int", index);
        this.value = value;
    }

    public boolean equals(IntegerParse other) {
        return (this.value == other.value)
                && (this.getIndex() == other.getIndex());
    }

    public String toString() {
        return "Parse(" + this.value + ", " + this.getIndex() + ")";
    }

    public int getValue() {
        return this.value;
    }
}
