public class Parse {
    // the name of the parse node
    private String name;
    // the number of characters into the string we've parsed
    private int index;

    public Parse(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public boolean equals(Parse other) {
        return (this.name.equals(other.name))
                && (this.index == other.index);
    }

    public String toString() {
        return "Parse(" + this.name + ", " + this.index + ")";
    }

    public String getName() {
        return this.name;
    }

    public int getIndex() {
        return this.index;
    }
}
