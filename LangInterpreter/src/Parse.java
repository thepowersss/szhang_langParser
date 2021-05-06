import java.util.LinkedList;

public class Parse { // these are nodes

    // the name of the parse node
    private String name;
    private int value;
    // the number of characters into the string we've parsed
    private int index;
    public LinkedList<Parse> children;
    private String varName;

    // default constructor
    Parse() {
        this.name = "default";
        this.value = -1;
        this.index = -1;
        this.children = new LinkedList<Parse>();
    }

    // make a copy
    Parse(Parse parse) {
        this.name = parse.name;
        this.value = parse.value;
        this.index = parse.index;
        this.children = new LinkedList<Parse>();
        this.varName = parse.varName;
    }

    Parse(int value, int index) {
        this.name = "default";
        this.value = value;
        this.index = index;
        this.children = new LinkedList<>();
    }

    Parse(String name, int index) {
        this.name = name;
        this.index = index;
        this.children = new LinkedList<>();
    }

    Parse(String name, int index, int value) {
        this.name = name;
        this.value = value;
        this.index = index;
        this.children = new LinkedList<>();
    }

    Parse(String name, int index, int value, String varName) {
        this.name = name;
        this.value = value;
        this.index = index;
        this.children = new LinkedList<>();
        this.varName = varName;
    }

    public boolean equals(Parse other) {
        return (this.name.equals(other.name)) && (this.index == other.index) && (this.value == other.value);
    }

    // GETS
    public String getName() { return this.name; }
    public int getValue() { return this.value; }
    public int getIndex() { return this.index; }
    public LinkedList<Parse> getChildren() { return this.children; }
    public String varName() { return this.varName; }

    // SETS
    public void setName(String name) { this.name = name; }
    public void setValue(int value) { this.value = value; }
    public void setIndex(int index) { this.index = index; }
    public void setChildren(LinkedList<Parse> children) { this.children = children; }
    public void setVarName(String varName) { this.varName = varName; }

    public String toString() {
        // handle empty program
        if (this.name.equals("")) {
            //System.out.println("syntax error");
            return "";
        }
        String result = "";
        if (this.name.equals("int")) { // print int
            result = "" + this.getValue();
        } else if (this.name.equals("var")) { // print varname
            result = this.varName();
        }
        else { // other nodes
            result = "(" + this.getName();
            // dealing with children
            for (Parse child : this.children) {
                result += " " + child.toString();
            }
            result += ")";
        }
        return result;
    }

}
