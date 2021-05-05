import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Parser {

    static Parse FAIL = new Parse(0, -1);

    public Parse parse(String str) {
        try {
            Parse parse = this.parse(str, 0, "sequence");
            if (str.length() != parse.getIndex()) {
                throw new AssertionError("syntax error");
            }
            return parse;
        } catch (Error e) {
            return null;
            //return new Parse("syntax error", -100);
            //throw new AssertionError("syntax error");
        }
    }

    public Parse parse(String str, String term) {
        try {
            return this.parse(str, 0, term);
        } catch (Error e) {
            return null;
        }
    }

    public Parse parse(String str, int index, String term) {
        if (index >= str.length()) { // may be just > instead of >=
            return Parser.FAIL;
        } else if (term.equals("sequence")) {
            return this.parse_sequence(str, index);
        } else if (term.equals("statement")) {
            return this.parse_statement(str, index);
        } else if (term.equals("return_statement")) {
            return this.parse_return_statement(str, index);
        } else if (term.equals("integer")) {
            return this.parse_integer(str, index);
        } else if (term.equals("add|sub")) {
            return this.parse_add_sub_expression(str, index);
        } else if (term.equals("mul|div")) {
            return this.parse_mul_div_expression(str, index);
        } else if (term.equals("operand")) {
            return this.parse_operand(str, index);
        } else if (term.equals("opt_space")) {
            return this.parse_opt_space(str, index);
        } else if (term.equals("req_space")) {
            return this.parse_req_space(str, index);
        } else if (term.equals("parenthesis")) {
            return this.parse_parenthesis(str, index);
        } else if (term.equals("print")) {
            return this.parse_print(str, index);
        } else if (term.equals("expression")) {
            return this.parse_expression(str, index);
        } else if (term.equals("declaration_statement")) {
            return this.parse_declaration_statement(str, index);
        } else if (term.equals("assignment_statement")) {
            return this.parse_assignment_statement(str, index);
        } else if (term.equals("location")) {
            return this.parse_location(str, index);
        } else if (term.equals("expression_statement")) { // RETIRED??
            return this.parse_expression_statement(str, index);
        } else if (term.equals("identifier")) {
            return this.parse_identifier(str, index);
        } else if (term.equals("comment")) {
            return this.parse_comment(str, index);
        } else if (term.equals("if_else_statement")) {
            return this.parse_if_else_statement(str, index);
        } else if (term.equals("if_statement")) {
            return this.parse_if_statement(str, index);
        } else if (term.equals("while_statement")) {
            return this.parse_while_statement(str, index);
        } else if (term.equals("or_expression")) {
            return this.parse_or_expression(str, index);
        } else if (term.equals("and_expression")) {
            return this.parse_and_expression(str, index);
        } else if (term.equals("optional_not_expression")) {
            return this.parse_optional_not_expression(str, index);
        } else if (term.equals("not_expression")) {
            return this.parse_not_expression(str, index);
        } else if (term.equals("comp_expression")) {
            return this.parse_comp_expression(str, index);
        } else if (term.equals("comp_operator")) {
            return this.parse_comp_operator(str, index);
        } else if (term.equals("call_expression")) {
            return this.parse_call_expression(str, index);
        } else if (term.equals("function_call")) {
            return this.parse_function_call(str, index);
        } else if (term.equals("arguments")) {
            return this.parse_arguments(str, index);
        } else if (term.equals("parameters")) {
            return this.parse_parameters(str, index);
        } else if (term.equals("function")) {
            return this.parse_function(str, index);
        } else {
            throw new AssertionError("Unexpected term " + term);
        }
    }

    private Parse parse_call_expression(String str, int index) {
        // TODO call_expression = operand ( opt_space function_call )*;

        // declare parent node
        Parse parent = new Parse();

        // parse operand
        Parse parse = this.parse(str, index, "operand");
        Parse lhs = parse; // left parse
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        // for all valid operands
        index = parse.getIndex();

        // loop and TREE MANIPULATION
        while (index < str.length()) {
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();

            // parse function_call
            parse = this.parse(str, index, "function_call"); // if there's a () after
            Parse rhs = parse; // right parse
            if (parse.equals(Parser.FAIL)) { // if no ()
                break; // spit out the operand parse (which is lhs)
            } else {
                index = parse.getIndex();
            }
            // do lookup for left parse
            if (lhs.getName().equals("lookup")) { // if there was a lookup
                Parse lookupParent = lhs;
                lookupParent.children.add(new Parse("var", index, 0, lhs.varName()));
                lhs = lookupParent;
            }

            parent = new Parse("call", index);
            parent.children.add(lhs);
            parent.children.add(rhs);
            lhs = parent;

        }
        // if no function call, then parent is still a default parse
        if (parent.equals(new Parse())) {
            return lhs;
        }
        return parent;
    }
    private Parse parse_function_call(String str, int index) {
        // TODO function_call = "(" opt_space arguments opt_space ")";

        // declare parent
        Parse parent = new Parse();

        // parse '('
        if (str.charAt(index) =='(') {
            index++;
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse arguments // could be empty
        parse = this.parse(str, index, "arguments");
        Parse args = parse;
        if (parse.equals(Parser.FAIL)) { // there was a syntax error
            return Parser.FAIL;
        } else { // add arguments as a child
            index = parse.getIndex();
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse ')'
        if (str.charAt(index) == ')') {
            index++;
            parent = args;
            parent.setIndex(index);
            return parent;
        } else {
            return Parser.FAIL;
        }
    }
    private Parse parse_arguments(String str, int index) {
        // TODO arguments = ( expression opt_space ( "," opt_space expression opt_space )* )?;

        // if there's no expression, charAt(index) will just be ')'

        // declare parent as an "arguments" parse
        Parse parent = new Parse();

        // parse expression
        Parse parse = this.parse(str, index, "expression");
        Parse lhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
            parent.children.add(lhs);
        } else { // if no expression, skip to end condition
            return new Parse("arguments", index);
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // loop
        while (index < str.length()) {
            // parse ','
            if (str.charAt(index) == ',') {
                index++;
            } else {
                break;
            }
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
            // parse expression
            parse = this.parse(str, index, "expression");
            Parse rhs = parse;
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
                parent.children.add(rhs);
            } else {
                return Parser.FAIL;
            }
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
        }
        // if parent is empty, then you bypassed the while loop
        parent.setIndex(index);
        parent.setName("arguments");
        return parent;
    }
    private Parse parse_function(String str, int index) {
        // TODO function = "func" opt_space "(" opt_space parameters opt_space ")" opt_space "{" opt_space program opt_space "}";

        // parse "func"
        if (str.startsWith("func", index)) {
            index += 4;
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse '('
        if (str.charAt(index) == '(') {
            index++;
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse parameters // could be empty
        parse = this.parse(str, index, "parameters");
        Parse parameters = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else { // if parameter is empty
            parameters.setName("parameters");
            parameters.setIndex(index);
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse ')'
        if (str.charAt(index) == ')') {
            index++;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse '{'
        if (str.charAt(index) == '{') {
            index++;
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse sequence
        parse = this.parse(str, index, "sequence");
        Parse body = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse '}'
        if (str.charAt(index) == '}') {
            index++;
            Parse parent = new Parse("function", index);
            parent.children.add(parameters);
            parent.children.add(body);
            return parent;
        } else {
            return Parser.FAIL;
        }
    }
    private Parse parse_parameters(String str, int index) {
        // TODO parameters = ( identifier opt_space ( "," opt_space identifier opt_space )* )?;

        // declare parent
        Parse parent = new Parse();

        // parse identifier
        Parse parse = this.parse(str, index, "identifier");
        Parse lhs = parse;
        if (lhs.getName().equals("lookup")) { // set up lookup vars
            lhs = new Parse("var", index, 0, lhs.varName());
        }
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
            parent.children.add(lhs);
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // loop
        while (index < str.length()) {
            // parse ','
            if (str.charAt(index) == ',') {
                index++;
            } else {
                break;
            }
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
            // parse identifier
            parse = this.parse(str, index, "identifier");
            Parse rhs = parse;
            if (rhs.getName().equals("lookup")) { // set up lookup vars
                rhs = new Parse("var", index, 0, rhs.varName());
            }
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
                parent.children.add(rhs);
            } else {
                return Parser.FAIL;
            }
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
        }
        parent.setIndex(index);
        parent.setName("parameters");
        return parent;
    }

    private Parse parse_sequence(String str, int index) {
        // TODO sequence = opt_space ( statement opt_space )*;
        // opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // ( statement opt_space )*
        // create sequence node, add the parse to its children
        Parse sequence = new Parse("sequence", 0);
        while (index < str.length()) { // while statements can be parsed, add them into the node's children
            parse = this.parse(str, index, "statement");
            if (parse.equals(Parser.FAIL)) {
                break;
            }
            index = parse.getIndex();
            sequence.children.add(parse);

            // opt_space
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }
        }
        sequence.setIndex(index);

        return sequence;
    }

    private Parse parse_statement(String str, int index) {
        // TODO statement = declaration_statement | assignment_statement | if_else_statement |
        //  if_statement | while_statement | return_statement | print_statement | expression_statement

        // declaration statement
        Parse parse = this.parse(str, index, "declaration_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // assignment statement
        parse = this.parse(str, index, "assignment_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // if_else statement
        parse = this.parse(str, index, "if_else_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // if statement
        parse = this.parse(str, index, "if_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // while statement
        parse = this.parse(str, index, "while_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // return statement
        parse = this.parse(str, index, "return_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // print statement
        parse = this.parse(str, index, "print");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // expression statement
        parse = this.parse(str, index, "expression_statement");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // none of the above
        return Parser.FAIL;
    }

    private Parse parse_if_else_statement(String str, int index) {
        // TODO if_else_statement = "if" opt_space "(" opt_space expression opt_space ")"
        //  opt_space "{" opt_space program opt_space "}"
        //  opt_space "else" opt_space "{" opt_space program opt_space "}";

        // declare parent node
        Parse parent;

        // parse "if"
        if (str.startsWith("if", index)) {
            // create parent node
            index += 2;
            parent = new Parse("ifelse", index); // declare parent
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '('
        if (str.startsWith("(", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse expression and save it as the left side
        parse = this.parse(str, index, "expression"); // check if expression is empty??
        Parse lhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse ')'
        if (str.startsWith(")", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '{'
        if (str.startsWith("{", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse sequence and add to the right side
        parse = this.parse(str, index, "sequence");
        Parse mid = parse; // middle parse
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else { // if sequence is empty, it should return a (sequence) node
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '}'
        if (str.startsWith("}", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse "else"
        if (str.startsWith("else", index)) {
            index += 4;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '{'
        if (str.startsWith("{", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse sequence and add to the right side
        parse = this.parse(str, index, "sequence");
        Parse rhs = parse; // right hand side parse
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else { // if sequence is empty, it should return a (sequence) node
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '}'
        if (str.startsWith("}", index)) {
            index += 1;
            parent.setIndex(index);
        } else {
            return Parser.FAIL;
        }

        // tree manipulation
        parent.children.add(lhs);
        parent.children.add(mid);
        parent.children.add(rhs);
        return parent;
    }

    private Parse parse_if_statement(String str, int index) {
        // TODO if_statement = "if" opt_space "(" opt_space expression opt_space ")"
        //  opt_space "{" opt_space program opt_space "}";

        // declare parent node
        Parse parent;

        // parse "if"
        if (str.startsWith("if", index)) {
            // create parent node
            index += 2;
            parent = new Parse("if", index); // declare parent
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '('
        if (str.startsWith("(", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse expression and save it as the left side
        parse = this.parse(str, index, "expression"); // check if expression is empty??
        Parse lhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse ')'
        if (str.startsWith(")", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '{'
        if (str.startsWith("{", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse sequence and add to the right side
        parse = this.parse(str, index, "sequence");
        Parse rhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else { // if sequence is empty, it should return a (sequence) node
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '}'
        if (str.startsWith("}", index)) {
            index += 1;
            parent.setIndex(index);
        } else {
            return Parser.FAIL;
        }

        // tree manipulation
        parent.children.add(lhs);
        parent.children.add(rhs);
        return parent;
    }

    private Parse parse_while_statement(String str, int index) {
        // TODO while_statement = "while" opt_space "(" opt_space expression opt_space ")"
        //  opt_space "{" opt_space program opt_space "}";

        // declare parent node
        Parse parent;

        // parse "while"
        if (str.startsWith("while", index)) {
            // create parent node
            index += 5;
            parent = new Parse("while", index); // declare parent
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '('
        if (str.startsWith("(", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse expression and save it as the left side
        parse = this.parse(str, index, "expression"); // check if expression is empty??
        Parse lhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse ')'
        if (str.startsWith(")", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '{'
        if (str.startsWith("{", index)) {
            index += 1;
        } else {
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse sequence and add to the right side
        parse = this.parse(str, index, "sequence");
        Parse rhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else { // if sequence is empty, it should return a (sequence) node
            return Parser.FAIL;
        }
        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        // parse '}'
        if (str.startsWith("}", index)) {
            index += 1;
            parent.setIndex(index);
        } else {
            return Parser.FAIL;
        }

        // tree manipulation
        parent.children.add(lhs);
        parent.children.add(rhs);
        return parent;
    }

    private Parse parse_return_statement(String str, int index) {
        // TODO return_statement = "ret" req_space expression opt_space ";";

        // declare parent node
        Parse parent;

        // parse "ret"
        if (str.startsWith("ret", index)) {
            index += 3;
        } else {
            return Parser.FAIL;
        }
        // parse req_space
        Parse parse = this.parse(str, index, "req_space");
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else {
            return Parser.FAIL;
        }

        // parse expression
        parse = this.parse(str, index, "expression");
        Parse exp = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        } else {
            return Parser.FAIL;
        }

        // parse opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse ';'
        if (str.charAt(index) == ';') {
            index++;
            parse.setIndex(index);
            // set parent to be return
            parent = new Parse("return", index);
            parent.children.add(exp);
            return parent;
        } else {
            return Parser.FAIL;
        }

    }

    private Parse parse_expression(String str, int index) {
        // TODO expression = or_expression;
       return this.parse(str, index, "or_expression");
    }

    private Parse parse_or_expression(String str, int index) {
        // TODO or_expression = and_expression ( opt_space or_operator opt_space and_expression )*;

        // declare parent
        Parse parent = new Parse();

        // parse and_expression
        Parse parse = this.parse(str, index, "and_expression");
        Parse lhs = parse; // left hand side parse expression
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        else { // if and_expression fails
            return Parser.FAIL;
        }

        // loop 0 or more times depending on the str length
        while (index < str.length()) {
            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();

            // parse or_operator
            if (str.startsWith("||", index)) {
                index += 2; // count the symbol
            }
            else { // no "||" found
                break;
            }

            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();

            // parse and_expression
            parse = this.parse(str, index, "and_expression");
            Parse rhs = parse; // right hand side parse expression
            if (rhs.equals(Parser.FAIL)) {// if and_expression fails, done
                break;
            }
            index = rhs.getIndex();

            // tree manipulation
            parent = new Parse("||", index);
            parent.children.add(lhs);
            parent.children.add(rhs);
            lhs = parent;
        }
        if (parent.equals(new Parse())) { // no right parse
            return lhs;
        }
        return parent;
    }

    private Parse parse_and_expression(String str, int index) {
        // TODO and_expression = optional_not_expression ( opt_space and_operator opt_space optional_not_expression )*;

        // declare parent
        Parse parent = new Parse();

        // parse optional_not_expression
        Parse parse = this.parse(str, index, "optional_not_expression");
        Parse lhs = parse; // left hand side parse expression
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        else { // if and_expression fails
            return Parser.FAIL;
        }

        // loop 0 or more times depending on the str length
        while (index < str.length()) {
            // opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();

            // and_operator
            if (str.startsWith("&&", index)) {
                index += 2; // count the symbol
            }
            else { // no "||" found
                break;
            }

            // parse opt_space
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();

            // parse optional_not_expression
            parse = this.parse(str, index, "optional_not_expression");
            Parse rhs = parse; // right hand side parse expression
            if (rhs.equals(Parser.FAIL)) {// if and_expression fails, done
                break;
            }
            index = rhs.getIndex();

            // tree manipulation
            parent = new Parse("&&", index);
            parent.children.add(lhs);
            parent.children.add(rhs);
            lhs = parent;
        }
        if (parent.equals(new Parse())) { // no right parse
            return lhs;
        }
        return parent;
    }

    private Parse parse_optional_not_expression(String str, int index) {
        // TODO optional_not_expression = comp_expression | not_expression

        // try comp expression
        Parse parse = this.parse(str, index, "comp_expression");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        // try not expression
        parse = this.parse(str, index, "not_expression");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        return Parser.FAIL;
    }

    private Parse parse_not_expression(String str, int index) {
        // TODO not_expression = "!" opt_space comp_expression;

        // parse '!'
        if (str.charAt(index) == '!') {
            index++;
        }
        else {
            return Parser.FAIL;
        }

        // parse opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // parse comp_expression
        parse = this.parse(str, index, "comp_expression");
        if (!parse.equals(Parser.FAIL)) {
            // tree manipulation
            Parse parent = new Parse("!", parse.getIndex());
            parent.children.add(parse);
            return parent;
        }
        return Parser.FAIL;
    }

    private Parse parse_comp_expression(String str, int index) {
        // TODO comp_expression = add_sub_expression ( opt_space comp_operator opt_space add_sub_expression )?;
        // '?' means 0 or 1

        // declare parent node(?)
        Parse parent = new Parse();

        // add_sub_expression left side
        Parse parse = this.parse(str, index, "add|sub");
        Parse lhs = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        else {
            return Parser.FAIL;
        }

        // opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // comp_operator
        parse = this.parse(str, index, "comp_operator");
        Parse operator_parse = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        else {
            return lhs;
        }

        // opt_space
        parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // add_sub_expression right side
        Parse rhs = this.parse(str, index, "add|sub");
        if (rhs.equals(Parser.FAIL)) { // no right side expression, then end
            return lhs;
        }
        index = rhs.getIndex();
        parent = new Parse(operator_parse.getName(), index);
        parent.children.add(lhs);
        parent.children.add(rhs);
        lhs = parent;

        if (parent.equals(new Parse())) { // to catch any slips(?)
            return lhs;
        }
        else {
            return parent;
        }
    }

    private Parse parse_comp_operator(String str, int index) {
        // one of these little buggers
        String[] comp_operators = {"==", "!=", "<=", ">=", "<", ">"};
        for (String symbol : comp_operators) {
            if (str.startsWith(symbol, index)) {
                index += symbol.length();
                return new Parse(symbol, index); // name is the type of operator
            }
        }
        return Parser.FAIL;
    }

    private Parse parse_assignment_statement(String str, int index) {
        // TODO assignment_statement = location opt_space "=" opt_space expression opt_space ";";
        // e.g. test = 2 + 2 ;

        // location parse
        Parse parse = this.parse(str, index, "location");
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        else { // parse was fail
            return Parser.FAIL;
        }

        // override varloc with the child of memloc if type memloc? memloc = member location
        Parse var_name = new Parse("var", parse.getIndex(), parse.getValue(), parse.varName());

        // opt_space parse
        parse = this.parse(str, index, "opt_space");
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }

        // find the "="
        if (str.charAt(index) == '=') {
            index++; // for finding the '='

            // opt_space parse
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }

            // expression_statement
            parse = this.parse(str, index, "expression");
            Parse expression_parse = parse;
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }
            else {
                // if expression returned Parser.FAIL, then there was no assignment
                // e.g. test = ;
                // should be an error
                return Parser.FAIL;
            }

            // opt_space
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }

            // check if reached end of string to avoid index out of bound error if no semicolon
            // e.g. print 2
            if (index >= str.length()) {
                return Parser.FAIL;
            }

            // semicolon
            if (str.charAt(index) == ';') {
                index++;
            }
            else {
                return Parser.FAIL;
            }

            // create the node
            Parse assignment_parse = new Parse("assign", index);

            // add var_name to child of varloc when assigning
            // add the location and expression parses as children
            Parse varloc = new Parse("varloc", var_name.getIndex());
            assignment_parse.children.add(varloc); // left
            varloc.children.add(var_name);
            assignment_parse.children.add(expression_parse); // right
            return assignment_parse;


        }
        return Parser.FAIL;
    }

    private Parse parse_declaration_statement(String str, int index) { //
        // TODO declaration_statement = "var" req_space assignment_statement;

        // parse "var"
        if (str.startsWith("var", index)) {
            index += 3;

            // req_space
            Parse parse = this.parse(str, index, "req_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }
            else { //req_space failed
                return Parser.FAIL;
            }

            // parse assignment_statement
            parse = this.parse(str, index, "assignment_statement");
            if (!parse.equals(Parser.FAIL)) {
                parse.setName("declare");
                // first child is varloc's varname
                Parse var_name = parse.children.get(0).children.get(0);
                // second child is the expression
                Parse expression = parse.children.get(1);
                parse.children.clear();
                parse.children.add(var_name);
                parse.children.add(expression);
                return parse;
            }
        }
        // either didn't start with "var" or assignment_statement failed
        return Parser.FAIL;
    }

    private Parse parse_location(String str, int index) {
        // TODO location = identifier;

        // if the string starting at the given index is a banned word, throw an error
        Parse parse = this.parse(str, index, "identifier");
        if (!parse.equals(Parser.FAIL)) { // identifier is parsed and passes the banned word test
            index = parse.getIndex();
            parse.setIndex(index);
            //System.out.println(parse.getName());
            return parse;
        }
        return Parser.FAIL;
    }

    private Parse parse_identifier(String str, int index) {
        // TODO identifier = identifier_first_char ( identifier_char )*;

        // identifier_first_char = ALPHA | '_';
        // loop alphabet and underscores
        char character = str.charAt(index); //looks at first character
        String ret_str = "";
        if (Character.isLetter(character) || character == '_') {
            ret_str += character;
            index++;
        } else {
            return Parser.FAIL;
        }

        // identifier_char = ALNUM | '_';
        // loop alphabet, numbers, and underscores
        while (index < str.length()) {
            character = str.charAt(index);
            if (Character.isLetterOrDigit(character) || character == '_') {
                ret_str += character;
                index++;
            }
            else { // fail the identifier parse if there's an illegal symbol
                break;
            }
        }

        // note: identifier cannot be a keyword: print, var, if, else, while, func, ret, class, int, bool, string
        String[] banned_words = {"print", "var", "if", "else", "while", "func", "ret", "class", "int", "bool", "string"};
        for (String word : banned_words) {
            if (ret_str.equals(word)) {
                return Parser.FAIL;
            }
        }

        return new Parse("lookup", index, 0,  ret_str);
    }

    private Parse parse_expression_statement(String str, int index) {
        // TODO expression_statement = expression opt_space ";";

        // parse expression
        Parse parse = this.parse(str, index, "expression");
        Parse exp = parse;
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }

        // opt_space
        parse = this.parse(str, index, "opt_space");
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }

        // check semicolon
        if (index >= str.length()) {
            return Parser.FAIL; // used to throw assertion error
        }
        if (str.charAt(index) == ';') {
            exp.setIndex(exp.getIndex()+1); // add 1 to index for semicolon
            return exp;
        }

        return Parser.FAIL;
    }

    private Parse parse_comment(String str, int index) {
        // TODO comment = "#" ( PRINT )* NEWLINE;
        // parse pound symbol
        if (str.charAt(index) != '#') {
            return Parser.FAIL;
        } else {
            index++; // move on
        }
        // parse text
        while (str.charAt(index) != '\n') {
            if (index != str.length() - 1) {
                index++;
            } else {
                break;
            }
        }
        return new Parse("#", index+1);
    }

    private Parse parse_print(String str, int index) {
        // TODO print_statement = "print" req_space expression opt_space ";";

        // parsing for word "print"
        if (str.startsWith("print", index)) {
            index += 5;
            // check for req_space (which can be newline)
            Parse parse = this.parse(str, index, "req_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }
            else { //req_space failed
                return Parser.FAIL;
            }

            // parse expression
            parse = this.parse(str, index, "expression");
            Parse exp = parse;
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            } else { // failed expression parse
                return Parser.FAIL;
            }

            // opt_space
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }

            // semicolon
            if (index >= str.length()) { // missing semicolon
                return Parser.FAIL;
                //throw new AssertionError("syntax error");
            }
            if (str.charAt(index) == ';') {
                index++;

                //create the node
                Parse print_parse = new Parse("print", index);
                print_parse.children.add(exp);
                return print_parse;
            }
        }
        return Parser.FAIL;
    }

    private Parse parse_opt_space(String str, int index) {
        // TODO opt_space = BLANK*;
        // basically, parse 0 or more spaces
        while (index < str.length()) {
            if (str.charAt(index) == '#') { // parse comments
                Parse parse = this.parse(str, index, "comment");
                if (!parse.equals(Parser.FAIL)) {
                    index = parse.getIndex();
                }
            }
            else if (str.charAt(index) == ' ' || str.charAt(index) == '\n' || str.charAt(index) == '\t') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("opt_space", index);
    }

    private Parse parse_req_space(String str, int index) {
        // TODO req_space = BLANK+
        // parse optional space, if length is not greater or equal to one, return fail

        Parse parse = this.parse(str,index,"opt_space");
        // if index == parse.getIndex(), then space had length 0
        if (index==parse.getIndex()) {
            return Parser.FAIL;
        }
        index = parse.getIndex();
        return new Parse("req_space", index);
    }


    private Parse parse_operand(String str, int index) {
        // TODO operand = parenthesized_expression | function | identifier | integer;
        Parse parse = this.parse(str, index, "parenthesis");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "function");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "identifier");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "integer");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }


        return Parser.FAIL;
    }

    private Parse parse_integer(String str, int index) {
        // TODO integer = ( DIGIT )+;
        // opt_space
        Parse parse = parse(str, index, "opt_space");
        index = parse.getIndex();

        String parsed = "";
        while ((index < str.length()) && Character.isDigit(str.charAt(index))) { // check for digits
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")) { // if no digits found, return FAIL
            return Parser.FAIL;
        }

        parse = this.parse(str, index, "opt_space"); // check spaces after
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }

        // the first parse created when typing 2;
        return new Parse("int", index, Integer.parseInt(parsed));
        //return new Parse(Integer.parseInt(parsed), index);
    }

    private Parse parse_parenthesis(String str, int index) {
        // TODO parenthesized_expression = "(" opt_space expression opt_space ")";

        // parse opt_space
        Parse parse = this.parse(str, index,"opt_space");
        index = parse.getIndex();

        // parse '('
        if (str.charAt(index) != '(') {
            return Parser.FAIL;
        } else {
            index++;
        }

        // parse expression
        parse = this.parse(str, index, "expression");
        Parse exp = parse;
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        } else {
            index = parse.getIndex();
        }

        // parse ')'
        if (str.charAt(index) != ')') {
            return Parser.FAIL;
        } else {
            index++;
        }
        // opt_space
        parse = this.parse(str, index, "opt_space"); // checks for spaces at end of parenthesis and adds to index
        index = parse.getIndex();

        exp.setIndex(index);
        return exp;
    }

    private Parse parse_mul_div_expression(String str, int index) {
        // TODO mul_div_expression = operand ( opt_space mul_div_operator opt_space operand )*;

        Parse space_parse = this.parse(str, index, "opt_space"); //parse spaces before operand and add to index
        if (!space_parse.equals(Parser.FAIL)) {
            index = space_parse.getIndex();
        }

        // parse left operand
        Parse left_parse = this.parse(str, index, "call_expression");
        if (left_parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        // if the operand was a variable
        else if (left_parse.getName().equals("lookup") && left_parse.children.isEmpty()) { // TODO fix ooga booga solution
            Parse lookupParent = left_parse;
            lookupParent.children.add(new Parse("var", index, 0, left_parse.varName()));
            left_parse = lookupParent;
        }
        // for all valid operands
        index = left_parse.getIndex(); // if not fail, add result and index

        // declare 'empty' parent
        Parse parent = new Parse();

        // parse right operands
        while (index < str.length()) {
            // opt_space
            space_parse = this.parse(str, index, "opt_space");
            index = space_parse.getIndex();

            // parse mul_div_operator
            char operator = str.charAt(index);
            if (operator == '*') { // if the operation was mult *
                parent = new Parse("*", index);
                index++;
            }
            else if (operator == '/') {
                parent = new Parse("/", index);
                index++;
            }
            else {
                break;
            }

            // parse opt_space
            space_parse = this.parse(str, index, "opt_space");
            index = space_parse.getIndex();

            // right operand
            Parse right_parse = this.parse(str, index, "call_expression");
            if (right_parse.equals(Parser.FAIL)) { // if operand is fail, break
                break;
            }
            // if the operand was a variable
            else if (right_parse.getName().equals("lookup")) {
                Parse lookupParent = right_parse;
                lookupParent.children.add(new Parse("var", index, 0, right_parse.varName()));
                right_parse = lookupParent;
            }
            // for all valid operands
            index = right_parse.getIndex(); // if not fail, add result and index

            // parse opt_space
            space_parse = this.parse(str, index, "opt_space");
            index = space_parse.getIndex();

            // tree manipulation
            parent.children.add(left_parse); // add right/left parse
            parent.children.add(right_parse);
            left_parse = parent;  // set left parse to parent
            if (parent.equals(new Parse())) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
            }
        }
        if (parent.equals(new Parse())) { // if parent is still empty
            return left_parse;  // aka there was no expression, return the left operand
        } else if (parent.children.size()==0) {// parent is not default, and it is an operator with no children
            return Parser.FAIL; // something fucky went on
        }
        parent.setIndex(index);
        return parent;
    }

    private Parse parse_add_sub_expression(String str, int index) {
        // TODO add_sub_expression = mul_div_expression ( opt_space add_sub_operator opt_space mul_div_expression )*;

        // opt_space just in case
        Parse space_parse = this.parse(str, index, "opt_space");
        index = space_parse.getIndex();

        // mul_div_expression
        Parse left_parse = this.parse(str, index, "mul|div");  // parses the mult expression (if no expression returns fail
        if (left_parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        index = left_parse.getIndex();
        Parse parent = new Parse(); // 'empty' parent

        while (index < str.length()){
            // opt_space
            space_parse = this.parse(str, index, "opt_space"); //parse spaces before operand and add to index
            index = space_parse.getIndex();

            // add_sub_operator
            char operator = str.charAt(index);
            if (operator == '+') {
                parent = new Parse("+", index);
                index++;
            }
            else if (operator == '-') {
                parent = new Parse("-", index);
                index++;
            }
            else {
                break;
            }

            // opt_space
            space_parse = parse(str, index, "opt_space");
            index = space_parse.getIndex();

            // mul_div expression
            Parse right_parse = this.parse(str, index, "mul|div");
            if (right_parse.equals(Parser.FAIL)) { // if operand is fail, break
                break;
            }

            index = right_parse.getIndex();
            parent.children.add(left_parse);
            parent.children.add(right_parse);
            left_parse = parent;

            if (parent.equals(new Parse())) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
            }
        }
        if (parent.equals(new Parse())) { // if parent is still empty
            return left_parse;  // aka there was no expression, return the left operand
        } else if (parent.children.size()==0) {// parent is not default, and it is an operator with no children
            return Parser.FAIL; // something fucky happened
        }
        parent.setIndex(index);
        return parent; // return the root level parent
    }

}