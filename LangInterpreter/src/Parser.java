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
        } else if (term.equals("sequence")) {
            return this.parse_sequence(str, index);
        } else if (term.equals("statement")) {
            return this.parse_statement(str, index);
        } else if (term.equals("print")) {
            return this.parse_print(str, index);
        } else if (term.equals("expression")) {
            return this.parse_add_sub_expression(str, index);
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
        } else if (term.equals("call_member_expression")) {
            return this.parse_call_member_expression(str,index);
        }
        /* // legacy functions
        } else if (term.equals("spaces")) {
            return this.parse_spaces(str, index);
        else if (term.equals("addition")) {
            return this.parse_addition_expression(str, index);
        } else if (term.equals("subtraction")) {
            return this.parse_subtraction_expression(str, index);
         */
        else {
            throw new AssertionError("Unexpected term " + term);
        }
    }

    private Parse parse_call_member_expression(String str, int index) { // functions?
        // parse operand

        // opt_space
        return null;
    }

    private Parse parse_assignment_statement(String str, int index) { // TODO: needs testing
        // assignment_statement = location opt_space "=" opt_space expression opt_space ";";
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
                throw new AssertionError("syntax error");
            }

            // opt_space
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }

            // check if reached end of string to avoid index out of bound error if no semicolon
            if (index >= str.length()) {
                throw new AssertionError("syntax error");
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

    private Parse parse_declaration_statement(String str, int index) { // TODO: needs testing
        // declaration_statement = "var" req_space assignment_statement;

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

    private Parse parse_location(String str, int index) { //TODO: needs testing
        // location = identifier;

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

    private Parse parse_identifier(String str, int index) { // TODO: needs testing
        // identifier = identifier_first_char ( identifier_char )*;

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
            //char[] breakSymbols = {' ', ';'};
            if (Character.isLetterOrDigit(character) || character == '_') {
                ret_str += character;
                index++;
            }
            else if (character == ' '){ // stop parsing variable name/identifier if there's a space
                break;
            }
            else if (character == ';') {
                break;
            }
            else { // fail the identifier parse if there's an illegal symbol
                return Parser.FAIL;
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
        // expression_statement = expression opt_space ";";
        // expression = add_sub_expression;

        // parse expression
        Parse parse = this.parse(str, index, "add|sub");
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
            throw new AssertionError("syntax error");
        }
        if (str.charAt(index) == ';') {
            //return new Parse("int", parse.getIndex()+1, exp.getValue());
            exp.setIndex(exp.getIndex()+1); // add 1 to index for semicolon
            return exp;
        }

        return Parser.FAIL;
    }

    private Parse parse_comment(String str, int index) {
        // comment = "#" ( PRINT )* NEWLINE;
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
        // print_statement = "print" req_space expression opt_space ";";

        // parsing for word "print"
        if (str.startsWith("print", index)) {
            index += 5;
            // check for req_space (which can be newline)
            Parse parse = this.parse(str, index, "req_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }
            else { //req_space failed
                throw new AssertionError("syntax error");
            }

            // parse expression
            parse = this.parse(str, index, "expression");
            Parse exp = parse;
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();

            }

            // opt_space
            parse = this.parse(str, index, "opt_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
            }

            // semicolon
            if (index >= str.length()) { // missing semicolon
                throw new AssertionError("syntax error");
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

    private Parse parse_statement(String str, int index) {
        // statement = declaration_statement | assignment_statement | print_statement | expression_statement

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

        // print statement
        parse = this.parse(str, index, "print"); // print = parse_print
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // expression statement
        parse = this.parse(str, index, "expression_statement"); // expression => parse_expression_statement
        //parse = this.parse(str, index, "expression"); // RETIRED
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }

        // none of the above
        return Parser.FAIL;
    }

    private Parse parse_sequence(String str, int index) {
        // sequence = opt_space ( statement opt_space )*;
        // opt_space
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        // ( statement opt_space )*
        // create sequence node, add the parse to its children
        Parse sequence = new Parse("sequence", 0);
        while (index < str.length()) { // while statements can be parsed, add them into the node's children
            parse = this.parse(str, index, "statement");
            if (parse.equals(Parser.FAIL)) {
                return Parser.FAIL;
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

    private Parse parse_opt_space(String str, int index) {
        // opt_space = BLANK*;
        // basically, parse 0 or more spaces
        // does the same as original parse_space
        while (index < str.length()) {
            if (str.charAt(index) == '#') { // parse comments
                Parse parse = this.parse(str, index, "comment");
                if (!parse.equals(Parser.FAIL)) {
                    index = parse.getIndex();
                    //return new Parse(str, parse.getIndex());
                }
            }
            else if (str.charAt(index) == ' ' || str.charAt(index) == '\n') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("opt_space", index);
    }

    private Parse parse_req_space(String str, int index) {
        // req_space = BLANK+
        // parse optional space, if length is not greater or equal to one, return fail

        // if the length of the parse is not greater than 1, then its fail
        if ((str.charAt(index) != ' ' && str.charAt(index) != '\n')) {
            return Parser.FAIL;
        }
        // Parse 1 or more spaces
        while (index < str.length()) {
            if (str.charAt(index) == '#') { // parse comments
                Parse parse = this.parse(str, index, "comment");
                if (!parse.equals(Parser.FAIL)) {
                    return new Parse(str, parse.getIndex()); //str may need to be changed to '#'?
                }
            }
            if (str.charAt(index) == ' ' || str.charAt(index) == '\n') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("req_space", index);
    }


    private Parse parse_operand(String str, int index) {
        // operand = parenthesized_expression | identifier | integer;
        Parse parse = this.parse(str, index, "parenthesis");
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
        // integer = ( DIGIT )+;
        Parse parse = parse(str, index, "opt_space"); // check spaces before
        if (!parse.equals(Parser.FAIL)) { // if parse of spaces was successful, add to index
            index = parse.getIndex();
        }

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
        // parenthesized_expression = "(" opt_space add_sub_expression opt_space ")";

        Parse space_parse = this.parse(str, index,"opt_space"); // checks for spaces at start of paren and adds to index
        if (space_parse != Parser.FAIL) {
            index = space_parse.getIndex();
        }

        if (str.charAt(index) != '(') {
            return Parser.FAIL;
        }
        Parse parse = this.parse(str, index + 1, "add|sub");
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        if (str.charAt(parse.getIndex()) != ')') {
            return Parser.FAIL;
        }
        space_parse = this.parse(str, parse.getIndex() + 1, "opt_space"); // checks for spaces at end of parenthesis and adds to index
        if (space_parse != Parser.FAIL) {
            parse.setIndex(space_parse.getIndex());
            return parse;
        }
        parse.setIndex(parse.getIndex() + 1);
        // add one to index to account for close parent \ return statement parse
        return parse;
    }

    private Parse parse_mul_div_expression(String str, int index) {
        // mul_div_expression = operand ( opt_space mul_div_operator opt_space operand )*;

        Parse space_parse = this.parse(str, index, "opt_space"); //parse spaces before operand and add to index
        if (!space_parse.equals(Parser.FAIL)) {
            index = space_parse.getIndex();
        }

        // parse left operand
        Parse left_parse = this.parse(str, index, "operand");
        if (left_parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        // if the operand was a variable
        else if (left_parse.getName().equals("lookup")) {
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
            Parse right_parse = this.parse(str, index, "operand");
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
        }
        parent.setIndex(index);
        return parent;
    }

    private Parse parse_add_sub_expression(String str, int index) {
        // add_sub_expression = mul_div_expression ( opt_space add_sub_operator opt_space mul_div_expression )*; TODO

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
        }
        parent.setIndex(index);
        return parent; // return the root level parent
    }

}