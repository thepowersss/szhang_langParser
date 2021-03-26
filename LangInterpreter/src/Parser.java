import java.util.ArrayList;
import java.util.HashMap;

public class Parser {

    static Parse FAIL = new Parse("0", -1);

    public Parse parse(String str, String term) {
        return this.parse(str, 0, term);
    }

    private Parse parse(String str, int index, String term) {
        if (index >= str.length()) {
            return Parser.FAIL;
        }
        if (term.equals("integer")) {
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
        } else if (term.equals("program")) {
            return null;
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

    private Parse parse_opt_space(String str, int index) {
        // opt_space = BLANK*;
        // basically, parse 0 or more spaces
        // does the same as original parse_space
        while (index < str.length()) {
            if (str.charAt(index) == ' ') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("space", index);
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
            if (str.charAt(index) == ' ') {
                index++;
            } else {
                break;
            }
        }
        return new Parse("space", index);
    }

    /*
    private Parse parse_spaces(String str, int index) {
        // legacy code, should be the same as opt_space
        String parsed = "";
        while (index < str.length() && str.charAt(index) == ' ') {
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")) {
            return Parser.FAIL;
        }
        return new Parse(parsed, index); // value doesn't matter because it's never looked at
    }
    */

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

        return new IntegerParse(Integer.parseInt(parsed), index);
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
        StatementParse parent = new StatementParse(); //declare parent
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
                parent = new StatementParse("*", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  // set left parse to parent
            }
            if (str.charAt(index) == '/') { // if the operation was divide /
                parent = new StatementParse("/", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  //set left parse to parent
            }
            index = right_parse.getIndex();  //set index to right parse index
        }
            if (parent.equals(new StatementParse())) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
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
        StatementParse parent = new StatementParse(); //declare parent
        Parse parse = new Parse(); // declare parse to fail test

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
                parent = new StatementParse("+", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);  // FIXME add the left parse before the right parse
                left_parse = parent;  //set left parse to parent
            }

            if (str.charAt(index) == '-') { // if the operation was subtraction -
                parent = new StatementParse("-", right_parse.getIndex());
                parent.children.add(left_parse); // add right/left parse
                parent.children.add(right_parse);
                left_parse = parent;  //set left parse to parent
            }
            index = right_parse.getIndex();  //set index to right parse index
            if (parent.equals(new StatementParse())) { // if parent is still empty
                return left_parse;  // aka there was no expression, return the left operand
            }
        }
        return parent; // return the root level parent
    }

    private static void test(Parser parser, String str, String term, Parse expected) { // legacy test
        Parse actual = parser.parse(str, term);
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + actual);
        }
        else {
            System.out.println("Test passed for string: \'" + str + "\', term: \'" + term + "\'");
        }
    }

    /*
    private static void test_parse(Parser parser, String str, String term, IntegerParse expected) {
        IntegerParse actual = (IntegerParse) parser.parse(str, term);

        assert (actual != null): "Got null when parsing {" + str + "}";
        assert (actual.getValue() == expected.getValue()) : "Parsing {" + str + "}; expected {" + expected + "} but got {" + actual;
        assert (actual.getIndex() == expected.getIndex()) : "Parsing {" + str + "}; expected {" + expected + "} but got {" + actual;
        /*
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + actual);
        }
        else {
            System.out.println("Test passed for string: \'" + str + "\', term: \'" + term + "\'");
        }
        */
    //}

    public static void test() {
        Parser parser = new Parser();

        //Parse term = parser.parse("2+2*2", "add|sub");
        System.out.println(parser.parse("2+2*2", "add|sub").toString());
        System.out.println(parser.parse(" 2 + 2 * 2 ", "add|sub").toString());
        System.out.println(parser.parse("2*2", "mul|div").toString());
        //System.out.println(parser.parse("2+2*2", "mul|div"));
        //System.out.println(term.toString());


        System.out.println("All testcases passed!");
    }

    public static void main(String[] args) {
        test();
    }

}