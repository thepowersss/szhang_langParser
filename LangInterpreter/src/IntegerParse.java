public class IntegerParse extends Parse { // leaf nodes

    // the value of the parse
    private int value;

    public IntegerParse(int value, int index) {
        super("int", index);
        this.value = value;
    }

    /*
    public boolean equals(IntegerParse other) {
        return (this.value == other.value)
                && (this.getIndex() == other.getIndex());
    }
    */

    public String toString() {
        return String.valueOf(this.value);
    }

    public int getValue() {
        return this.value;
    }
}
