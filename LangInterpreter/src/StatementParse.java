import java.util.LinkedList;

public class StatementParse extends Parse {
    // the value of the parse
    LinkedList<Parse> children;

    public StatementParse(String name, int index) {
        super(name, index);
        this.children = new LinkedList<Parse>();
    }

    public boolean equals(IntegerParse other) {
        return (this.getName() == other.getName())
                && (this.getIndex() == other.getIndex());
    }

    public String toString() {
        return "Parse(" + this.value + ", " + this.getIndex() + ")";
    }

    public int getValue() {
        return this.value;
    }
}
