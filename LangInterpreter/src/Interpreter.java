import java.util.HashMap;

public class Interpreter {
    String outputError = "";
    String output = "";
    Environment outerEnv;

    Interpreter() {
        this.output = "";
        this.outerEnv = new Environment();
    }

    void pushEnv() { // push a new environment onto the stack
        outerEnv = new Environment(new HashMap<>(), outerEnv);
    }

    void popEnv() { // pop the topmost environment, discarding it
        outerEnv = outerEnv.prevEnv;
    }

    String execute(Parse node) {
        try {
            outputError = ""; // flush??
            output = ""; // flush??
            outerEnv = new Environment(); // flush??
            if (node==null) { // only null if there's a syntax error
                return "syntax error"; // no output
            //} else if (node.getName().equals("syntax error")) { // parse is a syntax error
                //return "syntax error"; // no output
            }
            this.execute(node, "sequence");
            return output;
        } catch (Error e) {
            //System.out.println(e);
            //System.out.println(outputError);
            return output+outputError;
        }
    }

    void execute(Parse node, String term) {
        if (term.equals("sequence")) {
            exec_sequence(node);
        } else if (term.equals(("print"))) {
            exec_print(node);
        } else if (term.equals("assign")) {
            exec_assign(node);
        } else if (term.equals("declare")) {
            exec_declare(node);
        } else if (term.equals("if")) {
            exec_if(node);
        } else if (term.equals("ifelse")) {
            exec_ifelse(node);
        } else if (term.equals("while")) {
            exec_while(node);
        } else {
            evaluate(node);
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
        } else if (node.getName().equals("==")) {
            return eval_equals(node);
        } else if (node.getName().equals("!=")) {
            return eval_notEquals(node);
        } else if (node.getName().equals("!")) {
            return eval_not(node);
        } else if (node.getName().equals("<=")) {
            return eval_lessThanOrEquals(node);
        } else if (node.getName().equals(">=")) {
            return eval_moreThanOrEquals(node);
        } else if (node.getName().equals("<")) {
            return eval_lessThan(node);
        } else if (node.getName().equals(">")) {
            return eval_greaterThan(node);
        } else if (node.getName().equals("&&")) {
            return eval_and(node);
        } else if (node.getName().equals("||")) {
            return eval_or(node);
        } else if (node.getName().equals("lookup")) {
            return eval_lookup(node);
        } else if (node.getName().equals("int")) {
            return eval_int(node);
        } else {
            //output = "";
            outputError = "evaluation error\n";
            throw new AssertionError("evaluation error");
        }
    }

    int eval_int(Parse node) {
        return node.getValue();
    }

    int eval_lookup(Parse node) {
        // get variable name
        String var_name = node.varName();
        // check the right environment
        Environment env = outerEnv;
        Environment result_env = null;
        while (result_env==null && env!=null) {
            if (env.variables.containsKey(var_name)) {
                result_env = env;
                break;
            }
            env = env.prevEnv;
        }
        if (env==null) {
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
         /*
        // if var_name isn't in the environment, it's not defined
        if (!outerEnv.variables.containsKey(var_name)) {
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }*/

        // get the value of the variable from the environment's variable map
        return result_env.variables.get(var_name);
    }

    Environment eval_varloc(Parse node) {
        // (varloc a)
        String var_name = node.children.get(0).varName(); // get var_name
        Environment env = outerEnv;
        Environment result_env = null;
        try {
            while (env!= null) {
                if (env.variables.containsKey(var_name)) {
                    result_env = env;
                    break;
                }
                env = env.prevEnv;
            }
            // check to see if this variable exists??
            if (result_env == null) {
                outputError = "runtime error: undefined variable\n";
                throw new AssertionError("error");
            }
            return result_env;
        } catch (Error e) {
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }

    }

    int eval_add(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        return lhs + rhs;
    }

    int eval_sub(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        return lhs - rhs;
    }

    int eval_mul(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        return lhs * rhs;
    }

    int eval_div(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (rhs==0) {
            outputError = "runtime error: divide by zero\n";
            throw new AssertionError("runtime error: divide by zero");
        }
        return Math.floorDiv(lhs, rhs);
    }

    int eval_equals(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs == rhs) {
            return 1;
        }
        return 0;
    }

    int eval_notEquals(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs != rhs) {
            return 1;
        }
        return 0;
    }

    int eval_not(Parse node) {
        int lhs = evaluate(node.children.get(0));
        if (lhs==0) {
            return 1;
        }
        return 0;
    }

    int eval_lessThanOrEquals(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs <= rhs) {
            return 1;
        }
        return 0;
    }

    int eval_moreThanOrEquals(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs >= rhs) {
            return 1;
        }
        return 0;
    }

    int eval_lessThan(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs < rhs) {
            return 1;
        }
        return 0;
    }

    int eval_greaterThan(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs > rhs) {
            return 1;
        }
        return 0;
    }

    int eval_and(Parse node) {
        int lhs = evaluate(node.children.get(0));
        int rhs = evaluate(node.children.get(1));
        if (lhs !=0 && rhs !=0) {
            return 1;
        }
        return 0;
    }

    int eval_or(Parse node) {
        int lhs = evaluate(node.children.get(0));
        if (lhs != 0) {
            return 1;
        } else if (evaluate(node.children.get(1)) != 0) { // lang is a 'lazy' language != 0){
            return 1;
        } else {
            return 0;
        }
    }

    void exec_sequence(Parse sequence) {
        for (Parse node : sequence.children) {
            execute(node, node.getName());
        }
    }

    void exec_print(Parse node) {
        // print ( expression )
        // the expression is an evaluation (could be either arithmetic or a lookup)
        int expression = evaluate(node.children.get(0));
        System.out.println(expression);
        output+=expression+"\n";
    }

    void exec_if(Parse node) {
        // (if 1 (sequence))
        //       if
        //  1       sequence
        int condition = evaluate(node.children.get(0));
        if (condition != 0) { // if condition is true
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_ifelse(Parse node) {
        // (ifelse 1 (sequence) (sequence))
        //          ifelse
        //  1     sequence    sequence
        int condition = evaluate(node.children.get(0));
        if (condition != 0) { // if condition is true
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        } else {
            pushEnv();
            execute(node.children.get(2), "sequence");
            popEnv();
        }
    }

    void exec_while(Parse node) {
        // (while 1 (sequence))
        //     while
        //  1     sequence

        while (evaluate(node.children.get(0)) != 0) {
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_assign(Parse node) {
        // (assign (varloc a) 2)
        //        assign
        // (varloc a)     2

        // get new value to assign
        int val_new = evaluate(node.children.get(1));
        // get the varloc
        Parse varloc = node.children.get(0);
        // get the variable name
        String var_name = varloc.children.get(0).varName();
        // perform lookup, shift environment we're looking at
        Environment result_env = eval_varloc(varloc);

        // set new value
        result_env.variables.put(var_name, val_new);
    }

    void exec_declare(Parse node) {
        // (declare a 1)
        //     node
        //   a      1

        // get the var_name to be declared
        String var_name = node.children.get(0).varName(); // get var_name
        // evaluate the value to be assigned
        int var_value = evaluate(node.children.get(1)); // get the value

        // check if the variable is already in the environment
        //System.out.println(outerEnv.variables);
        if (outerEnv.variables.containsKey(var_name)) {
            outputError = "runtime error: variable already defined\n";
            throw new AssertionError("runtime error: variable already defined\n");
        }
        // add the variable to the environment map
        this.outerEnv.variables.put(var_name,var_value);
    }
}
