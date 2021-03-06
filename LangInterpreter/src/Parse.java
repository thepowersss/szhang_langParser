import java.util.LinkedList;

public class Parse { // these are nodes

    // the name of the parse node
    private String name;
    private int integer;
    // the number of characters into the string we've parsed
    private int index;
    public LinkedList<Parse> children;
    private String varName;

    // default constructor
    Parse() {
        this.name = "default";
        this.integer = -1;
        this.index = -1;
        this.children = new LinkedList<>();
    }

    // make a copy
    Parse(Parse parse) {
        this.name = parse.name;
        this.integer = parse.integer;
        this.index = parse.index;
        this.children = new LinkedList<>();
        this.varName = parse.varName;
    }

    Parse(int value, int index) {
        this.name = "default";
        this.integer = value;
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
        this.integer = value;
        this.index = index;
        this.children = new LinkedList<>();
    }

    Parse(String name, int index, int integer, String varName) {
        this.name = name;
        this.integer = integer;
        this.index = index;
        this.children = new LinkedList<>();
        this.varName = varName;
    }

    Parse(String name, String varName, int index) {
        this.name = name;
        this.index = index;
        this.children = new LinkedList<>();
        this.varName = varName;
    }
    Parse(String name, int index, LinkedList<Parse> children) {
        this.name = name;
        this.index = index;
        this.children = children;
    }
    Parse(String name, String varName, int index, LinkedList<Parse> children) {
        this.name = name;
        this.varName = varName;
        this.index = index;
        this.children = children;
    }

    Parse (Parse parse, String new_name) {
        this.varName = parse.varName;
        this.index = parse.index;
        this.integer = parse.integer;
        this.children = parse.children;
        this.varName = new_name;
    }

    public boolean equals(Parse other) {
        return (this.name.equals(other.name)) && (this.index == other.index) && (this.integer == other.integer);
    }

    // GETS
    public String getName() { return this.name; }
    public int getInt() { return this.integer; }
    public int getIndex() { return this.index; }
    public LinkedList<Parse> getChildren() { return this.children; }
    public String varName() { return this.varName; }

    // SETS
    public void setName(String name) { this.name = name; }
    public void setValue(int value) { this.integer = value; }
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
            result = "" + this.getInt();
        } else if (this.name.equals("var")) { // print varname
            result = this.varName();
//        } else if (this.name.equals("varloc")) {
//            //result = "(varloc " + this.varName + ")";
//        } else if (this.name.equals("memloc")) {
//            result += "(memloc";
//            for (Parse child : this.children) {
//                result+= " " + child.toString();
//            }
//            result += ")";
        } else { // other nodes
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
