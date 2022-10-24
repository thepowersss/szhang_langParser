import java.util.HashMap;
import java.util.LinkedList;
import java.util.OptionalInt;

/*
    This file contains the interpreter, which interprets parsed lang code
    lang is a strongly and partially typed programming language with lexical scoping and closures
    supports arithmetic, variables, control flow, loops, functions, recursion, and type-checking
 */
public class Interpreter {
    String outputError;
    String output;
    Environment curr_env;
    int function_call_depth;
    Value return_value;
    static boolean isReturning; // whether or not the interpreter is currently returning a Value

    Interpreter() {
        outputError = "";
        output = "";
        curr_env = new Environment();
        function_call_depth = 0;
        return_value = new Value(0);;
        isReturning = false;
    }

    public class Value {
        String type; //
        OptionalInt Integer; // values either have an Integer
        Closure closure; // or closure if it's a function

        Value(int Integer) {
            this.type = "int";
            this.Integer = OptionalInt.of(Integer);
            this.closure = null;
        }

        Value(Closure closure) {
            this.type = "closure";
            this.Integer = OptionalInt.of(0);
            this.closure = closure;
        }

        int getInt() {
            if (Integer.isPresent()) {
                return Integer.getAsInt();
            } else {
                throw new AssertionError("integer not present");
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
        HashMap<String,Value> variableMap; // matches variable name with its value
        Environment prevEnv;

        Environment() {
            variableMap = new HashMap<>();
            prevEnv = null;
        }

        Environment(HashMap<String,Value> variables, Environment prevEnv) {
            this.variableMap = variables;
            this.prevEnv = prevEnv;
        }

        public String toString() {
            return "VariableMap: " + variableMap.toString() + " prevEnv: " + prevEnv;
            //return "VariableMap: " + variableMap.toString() + " parameters: "+ parameters.toString()+" prevEnv: " + prevEnv;
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
        this.curr_env = new Environment(new HashMap<>(), this.curr_env);
    }

    void popEnv() { // pop the topmost environment, discarding it
        this.curr_env = this.curr_env.prevEnv;
    }

    String execute(Parse node) {
        try { // flushing
            outputError = "";
            output = "";
            curr_env = new Environment();
            function_call_depth = 0;
            return_value = new Value(0);;
            isReturning = false;

            // check if Parser detected a syntax error
            if (node==null) { // only null if there's a syntax error
                return "syntax error"; // no output
            }
            // empty program
            if (node.equals(new Parse("", -1))) {
                return "";
            }

            // begin executing
            this.execute(node, "sequence");
            return output+outputError;
        } catch (Error e) {
            //System.out.println(e);
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
        } else if (node.getName().equals("function")) {
            return eval_function(node);
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
        return new Value(node.getInt());
    }

    Value eval_function(Parse node) {
        // eval_function can be called under declare and assign
        //  in other words, this is only called when defining or assigning a function

        // (function (parameters b c) (sequence (print 1)))
        //        function
        // parameters    sequence
        // a       b      print

        Environment saved_env = this.curr_env; // copy current env
        Parse parameters = node.children.get(0); // save params
        Parse body = node.children.get(1); // save body

        // load params into a linkedlist
        LinkedList<Parse> paramList = parameters.children;
        // check for duplicate params
        if (check_duplicates(paramList)) {
            outputError = "runtime error: duplicate parameter\n";
            throw new AssertionError("runtime error: duplicate parameter");
        }
        // closure is new Closure(Parse params, Parse body, Environment env);
        // closure belongs to Value
        Closure function_closure = new Closure(parameters, body, saved_env);
        return new Value(function_closure);
    }

    Value eval_call(Parse node) {
        // a(2);
        // (call (lookup a) (arguments 2))
        //      call
        // lookup  arguments
        //   a          2

        function_call_depth++;
        Value curr_closure = evaluate((node).children.get(0)); // perform lookup on function definition
        if (!curr_closure.type.equals("closure")) {
            outputError = "runtime error: calling a non-function\n";
            throw new AssertionError("runtime error: calling a non-function");
        }

        // save and evaluate arguments
        LinkedList<Parse> arguments = node.children.get(1).children; // save arguments
        LinkedList<Value> evaluated_args = new LinkedList<>();
        // check if params match args
        if (curr_closure.closure.params.children.size() != arguments.size()) {
            outputError = "runtime error: argument mismatch\n";
            throw new AssertionError("runtime error: argument mismatch");
        }
        // evaluate the arguments
        for (Parse args : arguments) {
            evaluated_args.add(evaluate(args));
        }
        Environment saved_env = this.curr_env; // make a copy of curr_env
        this.curr_env = curr_closure.closure.env; // set current environment to closure env
        pushEnv(); // push a new env onto stack

        // add arguments to this.curr_env's variable map, matching with closure's params
        for (int i = 0; i < curr_closure.closure.params.children.size(); i++) {
            // get current param
            Parse curr_param = curr_closure.closure.params.children.get(i);
            String curr_param_name = curr_param.varName();
            // add it to the parameters of the curr_env
            this.curr_env.variableMap.put(curr_param_name, evaluated_args.get(i));
        }

        Parse closure_body = curr_closure.closure.body; // save function body
        execute(closure_body, "sequence"); // execute function body

        popEnv(); //pop environment
        this.curr_env = saved_env; // return to original environment

        Value retval = this.return_value;
        return_value = new Value(0);
        function_call_depth--;
        isReturning = false;
        return retval;
    }

    Value eval_lookup(Parse node) {
        // returns the value of a variable
        // (lookup a)
        //   lookup
        //     a

        // get variable name (the lookup and its child both share the same varName)
        String var_name = node.children.get(0).varName();
        // check environments until you find it in the dictionary
        Environment saved_env = this.curr_env; // make a copy of curr_env
        while (saved_env!=null) { // parameters are in the env stack
            if (saved_env.variableMap.containsKey(var_name)) {
                break;
            }
            saved_env = saved_env.prevEnv;
        }
        if (saved_env==null) {
            outputError = "runtime error: undefined variable\n";
            //System.out.println(100);
            throw new AssertionError("runtime error: undefined variable");
        }
        // get the value of the variable from the environment's variable map
        // look for value in variable map, then parameters
        if (saved_env.variableMap.containsKey(var_name)) {
            Value result_val = saved_env.variableMap.get(var_name); // for debug purposes
            return result_val; // return the corresponding value for the variable
        } else {
            //System.out.println(300);
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
    }

    Environment eval_varloc(Parse node) {
        // only called when assigning
        // returns the environment the variable is located in
        // (varloc a)
        //   varloc
        //     a
        String var_name = node.children.get(0).varName(); // get var_name
        Environment saved_env = this.curr_env;
        while (saved_env!= null) {
            if (saved_env.variableMap.containsKey(var_name)) { // search
                break;
            }
            saved_env = saved_env.prevEnv;
        }
        // check to see if this variable exists
        if (saved_env == null) {
            outputError = "runtime error: undefined variable\n";
            //System.out.println(200);
            throw new AssertionError("runtime error: undefined variable");
        }
        return saved_env;
    }

    Value eval_add(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if ((lhs.type.equals("closure") || rhs.type.equals("closure"))) { //if you try to add functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        return new Value(lhs.getInt() + rhs.getInt());
    }

    Value eval_sub(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.type.equals("closure") || rhs.type.equals("closure")) { //if you try to subtract functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        return new Value(lhs.getInt() - rhs.getInt());
    }

    Value eval_mul(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.type.equals("closure") || rhs.type.equals("closure")) { //if you try to mul functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        return new Value(lhs.getInt() * rhs.getInt());
    }

    Value eval_div(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (lhs.type.equals("closure") || rhs.type.equals("closure")) { //if you try to div functions
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
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
        //Value rhs = evaluate(node.children.get(0));
        // left is only false if its equal to 0
        if (lhs.type.equals("int") && lhs.getInt() == 0) { // closures are truthy
            return new Value(0);
        }
        Value rhs = evaluate(node.children.get(1));
        if ((rhs.type.equals("int") && rhs.getInt() != 0)
                || (lhs.getInt()==rhs.getInt())) { // lang is a lazy language
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
        }
        Value rhs = evaluate(node.children.get(1));
        if (rhs.type.equals("closure") || rhs.getInt() != 0) { // lang is a lazy language
            return new Value(1);
        } else {
            return new Value(0);
        }
    }

    void exec_sequence(Parse sequence) {
        for (Parse node : sequence.children) {
            if (isReturning) { // stop reading if function is returning
                break;
            }
            execute(node, node.getName());
        }
    }

    void exec_print(Parse node) {
        // print ( expression )
        // the expression is an evaluation (could be either arithmetic or a lookup)
        Value expression = evaluate(node.children.get(0));
        //System.out.println(expression.toString());
        output+=expression+"\n";
    }

    void exec_return(Parse node) {
        // (return 5)
        //   return
        //     5

        if (this.function_call_depth <= 0) { // the only place we care about function call depth
            outputError = "runtime error: returning outside function\n";
            throw new AssertionError("runtime error: returning outside function");
        }
        this.return_value = evaluate(node.children.get(0));
        isReturning = true;
        //return_value = null;
    }

    void exec_if(Parse node) {
        // control flow statement to execute a sequence if original condition is met,
        // do nothing if not met
        // (if condition (sequence))
        //            if
        //  condition     sequence
        Value condition = evaluate(node.children.get(0));
        if (condition.getInt() != 0 || condition.type.equals("closure")) { // if condition is true
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_ifelse(Parse node) {
        // control flow statement to execute first sequence if original condition is met,
        // or the second sequence if original condition is not met
        // (ifelse 1 (sequence) (sequence))
        //          ifelse
        //  1     sequence    sequence
        Value condition = evaluate(node.children.get(0));
        if (condition.getInt() != 0 // condition must be true
                || condition.type.equals("closure")) { // if condition is true, execute first sequence
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        } else { // if condition is false, execute second sequence
            pushEnv();
            execute(node.children.get(2), "sequence");
            popEnv();
        }
    }

    void exec_while(Parse node) {
        // execute body sequence while sequence condition is met, stop if returning
        // (while 1 (sequence))
        //     while
        //  1     sequence

        Value condition = evaluate(node.children.get(0));
        while ((condition.type.equals("closure") // condition must be a closure
                || condition.getInt() != 0) // condition must be true
                && !isReturning) { // stop if returning
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_assign(Parse node) {
        // re-assign an existing variable
        // (assign (varloc a) 2)
        //        assign
        // (varloc a)     2

        Parse varloc = node.children.get(0); // get the varloc
        Value val_new = evaluate(node.children.get(1)); // get new value to assign
        String var_name = varloc.children.get(0).varName(); // get the variable name
        // shift environment to the variable's location
        Environment result_env = eval_varloc(varloc); // also checks if variable already exists

        // assign new value
        result_env.variableMap.put(var_name, val_new); //put can replace
    }

    void exec_declare(Parse node) {
        // (declare a 1)
        //     declare
        //   a        1

        // get the var_name to be declared
        String var_name = node.children.get(0).varName(); // get var_name
        // evaluate the value to be assigned
        Value var_value = evaluate(node.children.get(1)); // get the value

        // check if the variable is already in the environment
        if (curr_env.variableMap.containsKey(var_name)) {
            outputError = "runtime error: variable already defined\n";
            throw new AssertionError("runtime error: variable already defined\n");
        }
        // put the variable in the environment map
        this.curr_env.variableMap.put(var_name,var_value);
    }
}
