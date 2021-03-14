import java.util.LinkedList;

public class StatementParse extends Parse {

    // internal parse node
    LinkedList<Parse> children;

    public StatementParse(String name, int index) {
        super(name, index);
        this.children = new LinkedList<Parse>();
    }

    public boolean equals(IntegerParse other) {
        return (this.getName().equals(other.getName()))
                && (this.getIndex() == other.getIndex());
    }

    public String toString() {
        return "Parse(" + this.getName() + ", " + this.getIndex() + ")";
    }

    public LinkedList<Parse> getChildren() {
        return this.children;
    }
}
