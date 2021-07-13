import javax.sound.midi.SysexMessage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.OptionalInt;

// TODO errors:
//  runtime error: member of non-object - if you try to get/set a member of something that is not an object
//  runtime error: undefined member - if you try to get/set a member that does not exist in an object
//  runtime error: math operation on functions - this should apply to classes and objects as well
//  a.b = 1 / 0; should give a div by 0 error
//  printing classes and objects should print "class" and "obj respectively
//  same equality and truthiness as with closures
//  var a.b = 1; should be invalid since you can't create new members of objects

public class Interpreter {
    String outputError;
    String output;
    Environment curr_env;
    int function_call_depth;
    Value return_value;
    static boolean isReturning; // whether or not the interpreter is currently returning a Value
    static boolean isDefiningMethod;

    Interpreter() {
        outputError = "";
        output = "";
        curr_env = new Environment();
        function_call_depth = 0;
        return_value = new Value(0);;
        isReturning = false;
        isDefiningMethod = false;
    }

    public class Value {
        String type; //
        OptionalInt Integer; // values either have an Integer
        Closure closure; // or closure if its a function
        Class Class;
        Environment environment;

        Value(int Integer) {
            this.type = "int";
            this.Integer = OptionalInt.of(Integer);
        }

        Value (Environment environment) { // FIXME ooga booga mode, make sure environments that are objects are objects
            if (environment.isObject==true) {
                this.type = "obj";
                this.environment = environment;
                this.environment.isObject = true;
            } else {
                this.type = "environment";
                this.environment = environment;
                this.environment.isObject = false;
            }
        }

        Value (Environment environment, boolean isObject) {
            this.type = "obj";
            this.environment = environment;
            this.environment.isObject = isObject;
        }

        Value(Closure closure) {
            this.type = "closure";
            this.closure = closure;
        }

        Value(Class Class) {
            this.type = "class";
            this.Class = Class;
        }

        int getInt() {
            if (Integer!=null) {
            //if (Integer.isPresent()) {
                return Integer.getAsInt();
            } else {
                throw new AssertionError("no int :(");
            }
        }

        boolean equals(Value other) {
            if (this.type.equals("int") && other.type.equals("int")) {
                return this.getInt()==other.getInt();
            }
            if (this.type.equals("closure") && other.type.equals("closure")) {
                return this.closure==other.closure; // TODO closures are equal if they have the same environment
            }
            if (this.type.equals("environment") && other.type.equals("environment")) {
                return this.environment==other.environment;
            }
            if (this.type.equals("obj") && other.type.equals("obj")) {
                return // both obj are equal if their variableMaps and prevEnv contain the same stuff
                        this.environment.variableMap.equals(other.environment.variableMap)
                        && this.environment.prevEnv.equals(other.environment.prevEnv);
            }
            return false;
        }

        public String toString() {
            if (this.type.equals("int")) {
                return "" + this.getInt();
            } else if (this.type.equals("closure")) {
                return closure.toString();
            } else if (this.type.equals("class")) {
                return Class.toString();
            } else if (this.type.equals("obj")) {
                return "obj";
            } else if (this.type.equals("environment")) {
                return environment.toString();
            } else {
                return "unknownType";
            }
        }
    }

    public class Environment {
        HashMap<String,Value> variableMap; // matches variable name with its value
        Environment prevEnv;
        boolean isObject; // allows a function to know if it is a member or just an environment
        // this allows a function/closure to know it is a member
        // if its environment is an object, then it is a member function

        Environment() {
            variableMap = new HashMap<>();
            prevEnv = null;
            isObject = false;
        }

        Environment(HashMap<String,Value> variables, Environment prevEnv) {
            this.variableMap = variables;
            this.prevEnv = prevEnv;
            this.isObject = false;
        }

        Environment(HashMap<String,Value> variables, Environment prevEnv, boolean isObject) {
            this.variableMap = variables;
            this.prevEnv = prevEnv;
            this.isObject = isObject;
        }

        void setObject(boolean isObject) {
            this.isObject = isObject;
        }

        public String toString() {
            if (isObject) {
                return "obj";
            } else {
                return "VariableMap: " + variableMap.toString() + " prevEnv: " + prevEnv;
            }
        }
    }

    public class Closure {
        Parse params; // left parse
        Parse body; // right parse
        Environment env;
        boolean isMethod;

        Closure(Parse params, Parse body, Environment env) {
            this.params = params;
            this.env = env;
            this.body = body;
            this.isMethod = false;
        }

        void setIsMethod(boolean isMethod) {
            this.isMethod = isMethod;
        }

        public String toString() {
            return "closure";
        }
    }

    public class Class {
        Parse body; // parse of 'class' body, which contains all the 'declare' children // not needed
        Environment env; // env contains all member variables

        Class(Parse body, Environment env) {
            this.body = body;
            this.env = env;
        }

        public String toString() {
            return "class";
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
        } else if (node.getName().equals("class")) {
            return eval_class(node);
        } else if (node.getName().equals("member")) {
            return eval_member(node);
            // varloc and memloc not needed because they're only called in assignment (and they return env, not value)
//        } else if (node.getName().equals("varloc")) {
//            return eval_varloc(node);
//        } else if (node.getName().equals("memloc")) {
//            return eval_memloc(node);
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
        Closure function_closure = new Closure(parameters, body, saved_env);

        // if isDefiningMethod (which means we're in a class), then this function is a method
        if (isDefiningMethod) {
            function_closure.setIsMethod(true);
        }

        // return as a Value
        return new Value(function_closure);
    }

    Value eval_call(Parse node) {
        // TODO if the called value is a class, the return value becomes the environment instead
        //  when creating objects, must check if the function is a member
        //  if so, inject the object as the first argument

        // var a = func(b) {ret b;};
        // a(2);
        // (call (lookup a) (arguments 2))
        //      call
        // lookup  arguments
        //   a          2
        // a is mapped to its function in its env

        // var c = class{};
        // b();
        // (call (lookup c) (arguments))

        // c is mapped to its class in its env

        function_call_depth++;
        Value curr_closure = evaluate((node).children.get(0)); // evaluate lookup (left child)

        // check if calling a non-function
        if (curr_closure.type.equals("int")) {
            outputError = "runtime error: calling a non-function\n";
            throw new AssertionError("runtime error: calling a non-function");
        }

        // save if looked-up variable was a class
        else if (curr_closure.type.equals("class")) {
            if (node.children.get(1).children.size() != 0) { // if arguments node has children (classes have no args)
                //System.out.println("error 700");
                outputError = "runtime error: argument mismatch\n";
                throw new AssertionError("runtime error: argument mismatch");
            }

            // if curr_closure is a class AND the node has a right child (arguments), then we have a();
            if (node.children.getLast().getName().equals("arguments")) {
                //System.out.println("reached arguments block");
                // therefore, curr_closure becomes an object/environment, which used to be a class
                Environment obj = new Environment(curr_closure.Class.env.variableMap, curr_closure.Class.env.prevEnv,true);
                this.function_call_depth--;
                // calling an object returns the object
                return new Value(obj, true);
            }

            Environment saved_env = this.curr_env; // save curr_env
            this.curr_env = curr_closure.Class.env; // get class's environment
            pushEnv(); // push new environment to the stack
            //this.curr_env.setObject(true);// TODO set isObj??

            for (Parse child : curr_closure.Class.body.children) {// execute body of declare statements
                execute(child,"declare"); // executing each declare statement
                // declares to this.curr_env's variable map
            }

            // 1. store the pushed environment
            Environment retEnv = this.curr_env;
            // 2. reset to old environment and decrease function call depth
            this.curr_env = saved_env;
            this.function_call_depth--;
            // 3. return pushed environment
            return new Value(retEnv); // see who gets pissed off

            //return new Value(new Object(curr_closure.Class.body,this.curr_env,//parent class));
        }

        // everything below applies to function calls only
        else if (curr_closure.type.equals("closure")) {
            // save and evaluate arguments
            LinkedList<Parse> arguments = node.children.get(1).children; // save arguments (right child)
            LinkedList<Value> evaluated_args = new LinkedList<>();

            if (curr_closure.closure.isMethod) { // if closure inside a class, it is a Method
                // first param is 'this', the object instance
                // add the closure.env as a value to evaluated args
                evaluated_args.add(new Value(curr_closure.closure.env)); // this is 'this'
            }

            // evaluate the arguments and add to evaluated_args list
            for (Parse args : arguments) {
                evaluated_args.add(evaluate(args));
            }

            // check if params match args
            if ((curr_closure.closure.params.children.size() != evaluated_args.size())) {
                //System.out.println("error 900");
                outputError = "runtime error: argument mismatch\n";
                throw new AssertionError("runtime error: argument mismatch");
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
            function_call_depth--; // decrease function call depth

            // TODO determine if retval can be not an integer?
            Value retval = this.return_value;
            return_value = new Value(0);

            isReturning = false;

            return retval; // retval is the returned int value
        }
        else { // not an int, class, or function?
            outputError = "unrecognized type called\n";
            throw new AssertionError("unrecognized type called");
        }
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
            //System.out.println(100); // FIXME REMOVEME
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
        // get the value of the variable from the environment's variable map
        // look for value in variable map, then parameters
        if (saved_env.variableMap.containsKey(var_name)) {
            //System.out.println(result_env.variableMap);
            Value result_val = saved_env.variableMap.get(var_name); // for debug purposes
//            if (result_val.type.equals("class")) {
//                System.out.println("result_valllll " + result_val.Class.env.variableMap);
//            }
            return result_val; // return the corresponding value for the variable
        } else {
            System.out.println(300); //FIXME REMOVEME
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
    }

    Environment eval_varloc(Parse node) {
        // TODO since objects are just environments, useful to define a Location class (environment, string name)
        //  (varloc ...) evaluates to a Location with that ENVIRONMENT and the name of the variable
        //  (memloc ...) evaluates to a Location with that OBJECT and the name of the member

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
            System.out.println(200); // FIXME REMOVEME
            outputError = "runtime error: undefined variable\n";
            throw new AssertionError("runtime error: undefined variable");
        }
        return saved_env;
    }

    Value eval_class(Parse node) {
        // just like closures, when a class is "called", an environment is created in which the body is run
        // in fact, having a common superclass makes eval_call much simpler
        // suggested class name: Callable
        pushEnv();
        this.curr_env.setObject(true); // this is now an obj
        isDefiningMethod = true;
        Environment class_env = this.curr_env; // save class env
        for (Parse child : node.children) {
            this.execute(child, "declare"); // body of class is all declare
        }
        Value callable = new Value(new Class(node, class_env));
        isDefiningMethod = false;
        popEnv();
        return callable;
    }

    Environment eval_memloc(Parse node) {
        // TODO return location of the member
        // a.b = 3;
        // (assign (memloc (varloc a) b) 3)
        //    memloc
        // varloc   b
        //    a

        // a.b -> a.env.var_map{b:b_env}
        // we want b_env

        Parse varloc_parse = node.children.get(0); // varloc (contains the instance) // e.g. a
        Environment varloc_env = eval_varloc(varloc_parse); // evaluate varloc to get its environment
        String member_name_key = node.children.get(1).varName(); // get the key, which is member name (rhs of node) // e.g. b

        // TODO DE-SPAGHETTIFY ENVIRONMENT/OBJ VS CLASS
        if (varloc_env.variableMap.get(varloc_parse.children.get(0).varName()).type.equals("environment")
        || varloc_env.variableMap.get(varloc_parse.children.get(0).varName()).type.equals("obj")) {
            Environment class_instance_env = varloc_env.variableMap.get(varloc_parse.children.get(0).varName()).environment;

            // check if member_name_key exists in the env's variable map. if not, then give runtime error
            if (!class_instance_env.variableMap.containsKey(member_name_key)) {
                //System.out.println(400); // FIXME REMOVEME
                outputError = "runtime error: undefined variable\n";
                throw new AssertionError("runtime error: undefined variable");
            }
            return class_instance_env;
        } else if (varloc_env.variableMap.get(varloc_parse.children.get(0).varName()).type.equals("class")) {
            Class class_instance = varloc_env.variableMap.get(varloc_parse.children.get(0).varName()).Class; // a's class

            // check if member_name_key exists in the class's variable map. if not, then give runtime error
            if (!class_instance.env.variableMap.containsKey(member_name_key)) {
                //System.out.println(400); // FIXME REMOVEME
                outputError = "runtime error: undefined variable\n";
                throw new AssertionError("runtime error: undefined variable");
            }

            // for debugging purposes only
            //Value member_env_val = class_instance.env.variableMap.get(member_name_key); // get the current value of member env value
            //System.out.println("current member env val " + member_env_val);

            //System.out.println("return class_instance env");
            return class_instance.env;
        } else {
            throw new AssertionError("error 800");
        }
    }

    Value eval_member(Parse node) {
        // TODO return member's Value

        // a.b;
        // (member (lookup a) b)
        //      member
        // lookup      b
        //   a
        // print a.b; should throw runtime error: member of non-object

        // a().b;
        // (member (call (lookup a) (arguments)) b)
        //              member
        //         call         b
        //   lookup   arguments
        //     a

        Parse lhs = node.children.get(0);
        Parse rhs = node.children.get(1); // member var name stored here

        Value class_instance = evaluate(lhs);// evaluate lhs and store as class instance

        // print a.b; should throw runtime error: member of non-object
        // since only valid use of a.b is in assignment, which uses memloc, not member
        if (class_instance.type.equals("class")) {
            outputError = "runtime error: member of non-object\n";
            throw new AssertionError("runtime error: member of non-object");
        }

        //if (class_instance.type.equals("obj")) {
        return class_instance.environment.variableMap.get(rhs.varName());

    }

    Value eval_add(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        // if not adding ints, then fail
        if (!(lhs.type.equals("int") && rhs.type.equals("int"))) { // you can only add ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        return new Value(lhs.getInt() + rhs.getInt());
    }

    Value eval_sub(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!(lhs.type.equals("int") && rhs.type.equals("int"))) { // you can only sub ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        return new Value(lhs.getInt() - rhs.getInt());
    }

    Value eval_mul(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!(lhs.type.equals("int") && rhs.type.equals("int"))) { // you can only mul ints
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        } // isPresent check not necessary because values can only either be ints or closures
        return new Value(lhs.getInt() * rhs.getInt());
    }

    Value eval_div(Parse node) {
        Value lhs = evaluate(node.children.get(0));
        Value rhs = evaluate(node.children.get(1));
        if (!(lhs.type.equals("int") && rhs.type.equals("int"))) { // you can only div ints
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
        if (lhs.type.equals("closure") || lhs.type.equals("environment")) {
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
        if (!lhs.type.equals(rhs.type)) { //if you try to compare different types
            outputError = "runtime error: math operation on functions\n";
            throw new AssertionError("runtime error: math operation on functions");
        }
        if (!lhs.type.equals("int")) { // if left isn't an int, then right isn't an int either
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
        if (!lhs.type.equals("int")) { // if left isn't an int, then right isn't an int either
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
        if (!lhs.type.equals("int")) { // if left isn't an int, then right isn't an int either
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
        if (!lhs.type.equals("int")) { // if left isn't an int, then right isn't an int either
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
        if (lhs.type.equals("closure") || lhs.type.equals("environment") || lhs.getInt() != 0) {
            return new Value(1);
        }
        Value rhs = evaluate(node.children.get(1));
        if (rhs.type.equals("closure") || rhs.type.equals("environment") || rhs.getInt() != 0) { // lang is a lazy language
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
        System.out.println(expression.toString());
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

        while ((evaluate(node.children.get(0)).type.equals("closure")
                || evaluate(node.children.get(0)).getInt() != 0)
                && !isReturning) { //check if condition is true AND stop if returning
            pushEnv();
            execute(node.children.get(1), "sequence");
            popEnv();
        }
    }

    void exec_assign(Parse node) {
        // re-assign an existing variable

        // possibility 1: varloc
        // a = 2;
        // (assign (varloc a) 2)
        //        assign
        // varloc         2
        //   a

        // possibility 2: memloc
        // a.b = 5;
        // (assign (memloc (varloc a) b) 5)
        //            assign
        //       memloc     5
        //  varloc    b
        //    a

    // possibility 1: varloc
        Parse lhs = node.children.get(0);
        if (lhs.getName().equals("varloc")) {
            Parse varloc = lhs; // get the varloc parse node
            Value val_new = evaluate(node.children.get(1)); // get new value to assign
            String var_name = varloc.children.get(0).varName(); // get the variable name

            // shift environment to the variable's location
            Environment result_env = eval_varloc(varloc); // also checks if variable already exists

            // assign new value
            result_env.variableMap.put(var_name, val_new); //put replaces old value

    // possibility 2: memloc
        } else if (lhs.getName().equals("memloc")) {
            //System.out.println("memloc time");
            // memloc only triggers on assignment on a member

            Parse memloc = lhs; // get the memloc parse node

            Parse varloc = memloc.children.get(0); // lhs
            String member_name = memloc.children.get(1).varName(); // rhs

            Environment instance_location = eval_memloc(node.children.get(0)); // get the memloc environment

            String instance_name = varloc.children.get(0).varName(); // get the variable name
            Value val_new = evaluate(node.children.get(1)); // get new value to assign

            // for debugging purposes
//            Value class_instance_value = instance_location.variableMap.get(member_name); // get the old class instance value to be overwritten by assignment
//
//            System.out.println("old class instance value " + class_instance_value);
//            System.out.println("instance name " + instance_name);
//            System.out.println("member name " + member_name);
//            System.out.println("val_new " + val_new);

            // shift environment to the instance's location
            Environment result_env = instance_location; // also checks if variable already exists

            // assign new value
            result_env.variableMap.put(member_name, val_new); //put can replace

            //this.curr_env = result_env;
        }
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
