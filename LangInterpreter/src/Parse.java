import sun.awt.image.ImageWatched;

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

    Parse(String name, int value, int index) {
        this.name = name;
        this.value = value;
        this.index = index;
        this.children = new LinkedList<>();
    }

    Parse(String name, int index, LinkedList<Parse> children) {
        this.name = name;
        this.index = index;
        this.children = children;
    }

    Parse(String name, int value, int index, LinkedList<Parse> children) {
        this.name = name;
        this.value = value;
        this.index = index;
        this.children = children;
    }

    Parse(String name, int value, int index, LinkedList<Parse> children, String varName) {
        this.name = name;
        this.value = value;
        this.index = index;
        this.children = children;
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
    public void setValue(int value) { this.value = value; }
    public void setIndex(int index) { this.index = index; }
    public void setChildren(LinkedList<Parse> children) { this.children = children; }
    public void setVarName(String varName) { this.varName = varName; }

    /*
    public String toString() {
        return "Parse(" + this.name + ", " + this.index + ")";
    }
    */


    public String toString() {
        String result = "";
        if (!this.name.equals("int")) { //if this is not a value
            result = "(" + this.getName();
        }
        else {
            result = "" + this.getValue();
        }
        for (Parse child : this.children) {
            result += " " + child.toString();
        }
        if (!this.name.equals("int")) { //if this is not a value
            result += ")";
        }
        return result;
    }

}
