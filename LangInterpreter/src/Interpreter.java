public class Interpreter {
    static String output;
    String input;
    boolean isReturning = false;

    void execute(Parse node, String term) {
        if (term.equals("sequence")) {
            exec_sequence(node);
        }
        if (term.equals("+")) {
            exec_addition(node);
        }
    }

    String execute(Parse node) {
        try {
            this.execute(node, "sequence");
            return output;
        } catch (Error e) {
            System.out.println(e);
            return output;
        }
    }

    void execute_program(Parse root) {
        for (Parse node : root.children) {
            if (this.isReturning) {
                break;
            }
            this.execute(node);
        }
    }

    void exec_sequence(Parse sequence) {
        for (Parse node : sequence.children) {
            execute(node, node.getName());
        }
    }

    void exec_addition(Parse addition) {
        output += addition.getValue();
    }
    /*
    void exec(Parse node) {

    }
    Integer eval(Parse node) { // will later become Value instead of Integer

    }
    */
    // void exec_* ...
    // Integer eval_* ...
}
