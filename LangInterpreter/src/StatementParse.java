import java.util.LinkedList;

public class StatementParse extends Parse { // internal node

    // internal parse node
    LinkedList<Parse> children;

    public StatementParse(String name, int index) {
        super(name, index);
        this.children = new LinkedList<Parse>();
    }

    StatementParse() {
        super();
        this.children = new LinkedList<>();
    }
/*
    public boolean equals(IntegerParse other) {
        return (this.getName().equals(other.getName()))
                && (this.getIndex() == other.getIndex());
    }
*/
    public String toString() {
        String expression_result = "(" + this.getName();
        for (Parse child : this.children) {
            expression_result += " " + child.toString();
        }
        expression_result += ")";
        return expression_result;
    }

    public LinkedList<Parse> getChildren() {
        return this.children;
    }
}
