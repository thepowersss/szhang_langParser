public class Interpreter {
    String outputError = "";
    String output = "";

    void execute(Parse node, String term) {
        if (term.equals("sequence")) {
            exec_sequence(node);
        }
        if (term.equals(("print"))) {
            exec_print(node);
        }
    }

    String execute(Parse node) {
        try {
            outputError = ""; // flush??
            output = ""; // flush??
            if (node==null) { // only null if there's a syntax error
                return "syntax error"; // no output
            } else if (node.getName().equals("syntax error")) { // parse is a syntax error
                return "syntax error"; // no output
            }
            this.execute(node, "sequence");
            return output;
        } catch (Error e) {
            //System.out.println(e);
            System.out.println(outputError);
            return output+outputError;
        }
    }

    int evaluate(Parse node) {
        if (node.getName().equals("+")) {
            return eval_add(node);
        } else if (node.getName().equals("-")) {
            return eval_sub(node);
        } else if (node.getName().equals("*")) {
            return eval_mul(node);
        } else if (node.getName().equals("/")) {
            return eval_div(node);
        } else if (node.getName().equals("int")) {
            return eval_int(node);
        } else {
            //output = "";
            //outputError = "syntax error";
            throw new AssertionError("evaluation error");
        }
    }

    int eval_int(Parse node) {
        return node.getValue();
    }

    int eval_add(Parse node) {
        int left_add = evaluate(node.children.get(0));
        int right_add = evaluate(node.children.get(1));
        return left_add + right_add;
    }

    int eval_sub(Parse node) {
        int left_sub = evaluate(node.children.get(0));
        int right_sub = evaluate(node.children.get(1));
        return left_sub - right_sub;
    }

    int eval_mul(Parse node) {
        int left_mul = evaluate(node.children.get(0));
        int right_mul = evaluate(node.children.get(1));
        return left_mul * right_mul;
    }

    int eval_div(Parse node) {
        int left_div = evaluate(node.children.get(0));
        int right_div = evaluate(node.children.get(1));
        if (right_div==0) {
            outputError = "runtime error: divide by zero";
            throw new AssertionError("runtime error: divide by zero");
        }
        return Math.floorDiv(left_div, right_div);
    }

    void exec_sequence(Parse sequence) {
        for (Parse node : sequence.children) {
            execute(node, node.getName());
        }
    }

    void exec_print(Parse node) {
        int expression = evaluate(node.children.get(0));
        System.out.println(expression);
        output+=expression+"\n";
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
