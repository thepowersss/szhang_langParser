import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Parser {

    static Parse FAIL = new Parse(0, -1);

    public Parse parse(String str, String term) {
        return this.parse(str, 0, term);
    }

    public Parse parse(String str, int index, String term) {
        if (index >= str.length()) {
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
        } else if (term.equals("expression")) { //will be reworked later to be either arithmetic or var or whatever
            return this.parse_add_sub_expression(str, index);
        } else if (term.equals("var_declaration")) {
            return this.parse_var_declaration(str, index);
        } else if (term.equals("var_assignment")) {
            return this.parse_var_assignment(str, index);
        } else if (term.equals("var_location")) {
            return this.parse_var_location(str, index);
        } else if (term.equals("expression_statement")) {
            return this.parse_expression_statement(str, index);
        } else if (term.equals("identifier")) {
            return this.parse_identifier(str, index);
        } else if (term.equals("identifier_first_char")) {
            return this.parse_identifier_first_char(str, index);
        } else if (term.equals("identifier_char")) {
            return this.parse_identifier_char(str, index);
        } else if (term.equals("comment")) {
            return this.parse_comment(str, index);
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

    private Parse parse_var_assignment(String str, int index) {
        return null;
    }

    private Parse parse_var_declaration(String str, int index) {
        return null;
    }

    private Parse parse_var_location(String str, int index) {
        return null;
    }

    private Parse parse_expression_statement(String str, int index) {
        return null;
    }

    private Parse parse_identifier(String str, int index) {
        return null;
    }

    private Parse parse_identifier_first_char(String str, int index) {
        return null;
    }

    private Parse parse_identifier_char(String str, int index) {
        return null;
    }

    private Parse parse_comment(String str, int index) {
        return null;
    }

    private Parse parse_print(String str, int index) {
        if (str.startsWith("print")) {
            index += 5;
            // check for req_space (which can be newline)
            Parse parse = this.parse(str, index, "req_space");
            if (!parse.equals(Parser.FAIL)) {
                index = parse.getIndex();
                //System.out.println("here");
            }
            else {
                throw new AssertionError("syntax error");
                //return Parser.FAIL;
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
        Parse parse = this.parse(str, index, "print");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "expression");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
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

    private Parse parse_opt_space(String str, int index) {
        // opt_space = BLANK*;
        // basically, parse 0 or more spaces
        // does the same as original parse_space
        while (index < str.length()) {
            if (str.charAt(index) == ' ' || str.charAt(index) == '\n') {
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
        if (str.charAt(index) != ' ') {
            return Parser.FAIL;
        }
        // Parse 1 or more spaces
        while (index < str.length()) {
            if (str.charAt(index) == ' ' || str.charAt(index) == '\n') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("req_space", index);
    }


    private Parse parse_operand(String str, int index) { //check if integer or parenthesis
        // operand = parenthesized_expression | integer;
        Parse parse = this.parse(str, index, "integer");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "parenthesis");
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
        return new Parse("int",Integer.parseInt(parsed), index);
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
        Parse left_parse = parse(str, index, "operand");
        if (left_parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        index = left_parse.getIndex(); // if not fail, add result and index
        Parse parent = left_parse; //declare parent
        Parse parse = new Parse(); // declare parse to fail test
        while (index < str.length() && !parse.equals(Parser.FAIL)) {
            if (str.charAt(index) != '*' && str.charAt(index) != '/') {  // parse *|/ and if not then fail
                parse = Parser.FAIL;
                break;
            }
            Parse right_parse = parse(str, left_parse.getIndex() + 1, "operand");  // parse next operand; index+1 for "*|/"
            if (right_parse == Parser.FAIL) {  // if operand was fail break
                parse = Parser.FAIL;
                break;
            }
            if (str.charAt(index) == '*') { // if the operation was mult *
                parent = new Parse("*", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  // set left parse to parent
            }
            if (str.charAt(index) == '/') { // if the operation was divide /
                parent = new Parse("/", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  //set left parse to parent
            }
            index = right_parse.getIndex();  //set index to right parse index
            if (parent.equals(new Parse())) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
            }
        }
        return parent;
    }

    private Parse parse_add_sub_expression(String str, int index) {
        // add_sub_expression = mul_div_expression ( opt_space add_sub_operator opt_space mul_div_expression )*;
        // add_sub_expression = operand ( opt_space add_sub_operator opt_space operand )*
        Parse space_parse = this.parse(str, index, "opt_space"); //parse spaces before operand and add to index
        if (!space_parse.equals(Parser.FAIL)) {
            index = space_parse.getIndex();
        }
        Parse left_parse = parse(str, index, "mul|div");  // parses the mult expression (if no expression returns int
        if (left_parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        index = left_parse.getIndex();
        Parse parent = left_parse; //declare parent //IS PARENT LEFT PARSE??? FIXME
        Parse parse = new Parse("temp", -3); // declare parse to fail test

        while (index < str.length() && !parse.equals(Parser.FAIL)){
            if (str.charAt(index) != '-' && str.charAt(index) != '+'){  //parse +|- and if not then fail
                parse = Parser.FAIL;
                break;
            }
            // parses the mult expression (if no expression returns int); jumps + 1 because of the "+/-"
            Parse right_parse = parse(str, left_parse.getIndex() + 1, "mul|div");  // use left parse index (parent)
            if (right_parse == Parser.FAIL) {  // if operand was fail break
                parse = Parser.FAIL;
                break;
            }
            if (str.charAt(index) == '+') { // if the operation was addition +
                parent = new Parse("+", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);  // FIXME add the left parse before the right parse
                left_parse = parent;  //set left parse to parent
            }

            if (str.charAt(index) == '-') { // if the operation was subtraction -
                parent = new Parse("-", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  //set left parse to parent
            }
            index = right_parse.getIndex();  //set index to right parse index
            /*
            if (parent.equals(new Parse("temp", -2))) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
            }*/
        }
        return parent; // return the root level parent
    }

}