import java.util.HashMap;
import java.util.LinkedList;
import java.util.OptionalInt;

public class Interpreter {
    String outputError = "";
    String output = "";
    Environment curr_env;

    public class Value {
        String type; //
        OptionalInt Integer; // values either have an Integer
        Closure closure; // or closure if its a function

        Value(int Integer) {
            this.type = "int";
            this.Integer = OptionalInt.of(Integer);
            this.closure = null;
        }

        Value(Closure closure) {
            this.type = "closure";
            this.Integer = OptionalInt.empty();
            this.closure = closure;
        }

        int getInt() {
            if (Integer.isPresent()) {
                return Integer.getAsInt();
            } else {
                throw new AssertionError("fuck you! integer not present");
            }
        }

        boolean equals(Value other) {
            if (this.type.equals("int") && other.type.equals("int")) {
                return this.getInt()==other.getInt();
            }
            if (this.type.equals("closure") && other.type.equals("closure")) {
                return this.closure==other.closure;
            }
            return false;
        }

        public String toString() {
            if (this.type.equals("int")) {
                return "" + this.getInt();
            } else if (this.type.equals("closure")) {
                return closure.toString();
            } else {
                return "unknownType";
            }
        }
    }

    public class Environment {
        HashMap<String,Value> variableMap;
        Environment prevEnv;

        Environment() {
            variableMap = new HashMap<>();
            prevEnv = null;
        }

        Environment(HashMap<String,Value> variables, Environment prevEnv) {
            this.variableMap = variables;
            this.prevEnv = prevEnv;
        }
    }

    public class Closure {
        Parse params; // left parse
        Parse body; // right parse
        Environment env;

        Closure(Parse params, Parse body, Environment env) {
            this.params = params;
            this.env = env;
            this.body = body;
        }

        public String toString() {
            return "closure";
        }
    }

    void pushEnv() { // push a new environment onto the stack
        curr_env = new Environment(new HashMap<>(), curr_env);
    }

    void popEnv() { // pop the topmost environment, discarding it
        curr_env = curr_env.prevEnv;
    }

    String execute(Parse node) {
        try {
            outputError = ""; // flush??
            output = ""; // flush??
            curr_env = new Environment(); // flush??
            if (node==null) { // only null if there's a syntax error
                return "syntax error"; // no output
            }
            this.execute(node, "sequence");
            return output+outputError;
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
        } else if (term.equals("return")) {
            exec_return(node);
        } else {
            evaluate(node);
        }
    }

    Value evaluate(Parse node) {
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
        } else if (node.getName().equals("call")) {
            return eval_call(node);
        } else {
            //output = "";
            outputError = "evaluation error\n";
            throw new AssertionError("evaluation error");
        }
    }

    boolean check_duplicates(LinkedList<Parse> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (i != j
                        && list.get(i).varName()
                        .equals(list.get(j).varName())) {
                    return true;
                }
            }
        }
        return false;
    }

    Value eval_int(Parse node) {
        return new Value(node.getValue());
    }

    Closure eval_function(Parse node) {
        // TODO eval_function can be called under declare and assign
        //  in other words, this is only called when defining or assigning a function
        //  printing a function should give "closure"
        // (function (parameters b c) (sequence (print 1)))
        //        function
        // parameters    sequence
        // a       b      print 1
/*
        Environment curr_env = this.curr_env;
        LinkedList<Parse> parameters = node.children.get(0).children; // capture params into a list
        // check for duplicate params
        boolean contains_duplicate_parameters = check_duplicates(parameters);
        if (contains_duplicate_parameters) {
            outputError = "runtime error: duplicate parameter\n";
            throw new AssertionError("error");
        }
        // load a string array with all the param names
        String[] param_array = new String[parameters.size()];
        for (int i = 0; i < param_array.length; i++) {
            param_array[i] = parameters.get(i).varName();
        }
        Closure function_closure = new Closure(node, curr_env, param_array);
        return function_closure;

 */
        return null;
    }

    Value eval_call(Parse node) {
        return null;
    }

    Value eval_lookup(Parse node) {
        // get variable name
        String var_name = node.varName();
        // check the right environment
        Environment env = curr_env;
        Environment result_env = null;
        while (result_env==null && env!=null) {
            if (env.variableMap.containsKey(var_name)) {
                result_env = env;
                break;
            }
            env = env.prevEnv;
        }
        if (env==null) {
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
        // get the value of the variable from the environment's variable map
        return result_env.variableMap.get(var_name);
    }

    Environment eval_varloc(Parse node) {
        // (varloc a)
        String var_name = node.children.get(0).varName(); // get var_name
        Environment env = curr_env;
        Environment result_env = null;
        try {
            while (env!= null) {
                if (env.variableMap.containsKey(var_name)) {
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

    Value eval_add(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.closure!=null || rhs.closure!=null) { //if you try to add functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        return new Value(lhs.getInt() + rhs.getInt());
    }

    Value eval_sub(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.closure!=null || rhs.closure!=null) { //if you try to add functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        return new Value(lhs.getInt() - rhs.getInt());
    }

    Value eval_mul(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.closure!=null || rhs.closure!=null) { //if you try to add functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        return new Value(lhs.getInt() * rhs.getInt());
    }

    Value eval_div(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.closure!=null || rhs.closure!=null) { //if you try to add functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        if (rhs.getInt()==0) {
            outputError = "runtime error: divide by zero\n";
            throw new AssertionError("runtime error: divide by zero");
        }
        return new Value(Math.floorDiv(lhs.getInt(), rhs.getInt()));
    }

    Value eval_equals(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.equals(rhs)) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_notEquals(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!lhs.equals(rhs)) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_not(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        if (lhs.type.equals("closure")) {
            return new Value(0);
        }
        if (lhs.getInt()==0) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_lessThanOrEquals(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!lhs.type.equals(rhs.type)) { //if you try to compare functions to ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.type.equals("closure")) { // left and right are both closures
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.getInt() <= rhs.getInt()) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_moreThanOrEquals(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!lhs.type.equals(rhs.type)) { //if you try to compare functions to ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.type.equals("closure")) { // left and right are both closures
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.getInt() >= rhs.getInt()) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_lessThan(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!lhs.type.equals(rhs.type)) { //if you try to compare functions to ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.type.equals("closure")) { // left and right are both closures
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.getInt() < rhs.getInt()) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_greaterThan(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!lhs.type.equals(rhs.type)) { //if you try to compare functions to ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.type.equals("closure")) { // left and right are both closures
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (lhs.getInt() > rhs.getInt()) {
            return new Value(1);
        }
        return new Value(0);
    }

    Value eval_and(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(0));
        // left is only false if its equal to 0
        if (lhs.getInt() == 0) { // closures are truthy
            return new Value(0);
        } else if (evaluate(node.children.get(1)).getInt() != 0 && lhs.getInt()==rhs.getInt()) { // lang is a lazy language
            return new Value(1);
        } else {
            return new Value(0);
        }
    }

    Value eval_or(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        // left is only true if its either a closure or equal to 1
        if (lhs.type.equals("closure") || lhs.getInt() != 0) {
            return new Value(1);
        } else if (evaluate(node.children.get(1)).getInt() != 0) { // lang is a lazy language
            return new Value(1);
        } else {
            return new Value(0);
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
        Value expression = evaluate(node.children.get(0));
        System.out.println(expression.toString());
        output+=expression+"\n";
    }

    void exec_return(Parse node) {

    }

    void exec_if(Parse node) {
        // (if 1 (sequence))
        //       if
        //  1       sequence
        Value condition = evaluate(node.children.get(0));
        if (condition.getInt() != 0 || condition.type.equals("closure")) { // if condition is true
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_ifelse(Parse node) {
        // (ifelse 1 (sequence) (sequence))
        //          ifelse
        //  1     sequence    sequence
        Value condition = evaluate(node.children.get(0));
        if (condition.getInt() != 0 || condition.type.equals("closure")) { // if condition is true
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

        while (evaluate(node.children.get(0)).type.equals("closure") || evaluate(node.children.get(0)).getInt() != 0) {
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
        Value val_new = evaluate(node.children.get(1));
        // get the varloc
        Parse varloc = node.children.get(0);
        // get the variable name
        String var_name = varloc.children.get(0).varName();
        // perform lookup, shift environment we're looking at
        Environment result_env = eval_varloc(varloc);

        // set new value
        result_env.variableMap.put(var_name, val_new);
    }

    void exec_declare(Parse node) {
        // (declare a 1)
        //     node
        //   a      1

        // get the var_name to be declared
        String var_name = node.children.get(0).varName(); // get var_name

        // evaluate the value to be assigned
        // could either be int or Closure (if its a function)
        if (node.children.get(1).getName().equals("function")) {
            Closure var_value;
        } //else {
            Value var_value = evaluate(node.children.get(1)); // get the value
        //}

        // check if the variable is already in the environment
        //System.out.println(outerEnv.variableMap);
        if (curr_env.variableMap.containsKey(var_name)) {
            outputError = "runtime error: variable already defined\n";
            throw new AssertionError("runtime error: variable already defined\n");
        }
        // add the variable to the environment map
        this.curr_env.variableMap.put(var_name,var_value);
    }
}
