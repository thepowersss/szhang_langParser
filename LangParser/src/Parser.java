import java.util.ArrayList;
import java.util.HashMap;

public class Parser {

    static Parse FAIL = new Parse(0, -1);

    public Parse parse(String str, String term) {
        return this.parse(str, 0, term);
    }

    private Parse parse(String str, int index, String term) {
        if (index >= str.length()) {
            return Parser.FAIL;
        }
        if (term.equals("integer")) {
            return this.parse_integer(str, index);
        } else if (term.equals("addition")) {
            return this.parse_addition_expression(str, index);
        } else if (term.equals("subtraction")) {
            return this.parse_subtraction_expression(str, index);
        } else if (term.equals("add|sub")) {
            return this.parse_add_sub_expression(str, index);
        } else if (term.equals("operand")) {
            return this.parse_operand(str, index);
        } else if (term.equals("spaces")) {
            return this.parse_spaces(str, index);
        } else if (term.equals("parenthesis")) {
            return this.parse_parenthesis(str, index);
        } else {
            throw new AssertionError("Unexpected term " + term);
        }
    }

    private Parse parse_opt_spaces(String str, int index) {
        // opt_space = BLANK*;
        return null;
    }

    private Parse parse_spaces(String str, int index) {
        // req_space = BLANK+
        String parsed = "";
        while (index < str.length() && str.charAt(index) == ' ') {
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")) {
            return Parser.FAIL;
        }
        return new Parse(0 ,index); // value doesn't matter because it's never looked at
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

    private Parse parse_parenthesis(String str, int index) {
        // parenthesized_expression = "(" opt_space add_sub_expression opt_space ")";
        if (str.charAt(index) != '(') {
            return Parser.FAIL;
        }

        Parse parse = this.parse(str, index,"spaces"); // parse spaces before and add to index
        if (parse != Parser.FAIL) {
            index = parse.getIndex();
        }

        parse = this.parse(str, index + 1, "addition");
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        if (str.charAt(parse.getIndex()) != ')') {
            return Parser.FAIL;
        }
        return new Parse(parse.getValue(), parse.getIndex() + 1);
    }

    private Parse parse_addition_expression(String str, int index) {
        // add_expression = operand ( opt_space "+" opt_space )*;
        Parse parse = this.parse(str, index,"spaces"); // parse spaces before and add to index
        if (parse != Parser.FAIL) {
            index = parse.getIndex();
        }
        int result = 0;
        parse = this.parse(str, index,"operand");
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        result = parse.getValue();
        index = parse.getIndex();
        while (index < str.length() && !parse.equals(Parser.FAIL)) {
            if (str.charAt(index) != '+') {
                parse = Parser.FAIL;
                break;
            }
            parse = this.parse(str, parse.getIndex() + 1, "operand");
            if (parse.equals(Parser.FAIL)) {
                parse = Parser.FAIL;
                break;
            }
            result += parse.getValue();
            index = parse.getIndex();
        }
        return new Parse(result, index);
    }

    private Parse parse_subtraction_expression(String str, int index) {
        // sub_expression = operand ( opt_space "-" opt_space )*;
        Parse parse = this.parse(str, index,"spaces"); // parse spaces before and add to index
        if (parse != Parser.FAIL) {
            index = parse.getIndex();
        }
        parse = this.parse(str, index, "operand");
        int result = 0;
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        result = parse.getValue();
        index = parse.getIndex();
        while (index < str.length() && !parse.equals(Parser.FAIL)) {
            if (str.charAt(index) != '-') {
                parse = Parser.FAIL;
                break;
            }
            parse = this.parse(str, parse.getIndex() + 1, "operand");
            if (parse.equals(Parser.FAIL)) {
                parse = Parser.FAIL;
                break;
            }
            result -= parse.getValue();
            index = parse.getIndex();
        }
        return new Parse(result, index);
    }

    private Parse parse_integer(String str, int index) {
        // integer = ( DIGIT )+;
        Parse parse = parse(str, index, "spaces"); // check spaces
        if (parse != Parser.FAIL) { // if parse of spaces was successful, add to index
            index = parse.getIndex();
        }
        String parsed = "";
        while ((index < str.length()) && Character.isDigit(str.charAt(index))) {
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")) {
            return Parser.FAIL;
        }
        parse = this.parse(str, index, "spaces");
        if (!parse.equals(Parser.FAIL)) {
            index = parse.getIndex();
        }
        return new Parse(Integer.parseInt(parsed), index);
    }

    private Parse parse_mul_div_expression(String str, int index) {
        // mul_div_expression = operand ( opt_space mul_div_operator opt_space operand )*;
        return null;
    }

    private Parse parse_add_sub_expression(String str, int index) {
        // add_sub_expression = mul_div_expression ( opt_space add_sub_operator opt_space mul_div_expression )*;
        // add_sub_expression = operand ( opt_space add_sub_operator opt_space operand )*
        Parse parse = parse(str, index, "add_sub_operand");
        /*
        while (index < str.length() && !parse.equals(Parser.FAIL)) {

        }*/
        return null;
    }

    private Parse parse_add_sub_operand(String str, int index) {
        return null;
    }

    private static void test(Parser parser, String str, String term, Parse expected) {
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

    public static void test() {
        Parser parser = new Parser();
        // integer tests
        test(parser, "b", "integer", Parser.FAIL);
        test(parser, "", "integer", Parser.FAIL);
        test(parser, "0", "integer", new Parse(0, 1));
        test(parser, "2021", "integer", new Parse(2021, 4));

        // addition tests
        test(parser, "", "addition", Parser.FAIL);
        test(parser, "b", "addition", Parser.FAIL);

        test(parser, "3-", "addition", new Parse(3, 1));
        test(parser, "3++", "addition", new Parse(3, 1));
        test(parser, "1+1-", "addition", new Parse(2, 3));
        test(parser, "1+1+-", "addition", new Parse(2, 3));

        test(parser, "3+4", "addition", new Parse(7, 3));
        test(parser, "2020+2021", "addition", new Parse(4041, 9));
        test(parser, "0+0", "addition", new Parse(0, 3));
        test(parser, "0+0+0+0+0", "addition", new Parse(0, 9));
        test(parser, "42+0", "addition", new Parse(42, 4));
        test(parser, "0+42", "addition", new Parse(42, 4));
        //test(parser, "0-42", "addition", Parser.FAIL);

        // spaces test (no trailing or leading spaces)
        // required space and optional space should be separate functions
        test(parser, "  ", "spaces", new Parse(0, 2));
        test(parser, "1 + 1", "addition", new Parse(2, 5));
        test(parser, "1   + 1", "addition", new Parse(2, 7));
        test(parser, " 1 + 1 ", "addition", new Parse(2, 7));
        test(parser, "3 - 4", "subtraction", new Parse(-1, 5));

        // subtraction tests
        test(parser, "3-4", "subtraction", new Parse(-1, 3));
        test(parser, "3-4+", "subtraction", new Parse(-1, 3));
        test(parser, "0-0", "subtraction", new Parse(0, 3));
        test(parser, "0-5", "subtraction", new Parse(-5, 3));
        test(parser, "4-3-2-1", "subtraction", new Parse(-2, 7));

        // parenthesis tests
        test(parser, "(0)", "parenthesis", new Parse(0, 3));
        test(parser, "(0+0)", "parenthesis", new Parse(0, 5));
        test(parser, "(1+2)", "parenthesis", new Parse(3, 5));
        test(parser, "(1+2+3)", "parenthesis", new Parse(6, 7));
        test(parser, "4+(1+2+3)", "addition", new Parse(10, 9));
        test(parser, "(1+2+3)+5", "addition", new Parse(11, 9));
        test(parser, "4+(1+2+3)+5", "addition", new Parse(15, 11));
        test(parser, "3+4+(5+6)+9", "addition", new Parse(27, 11));

        // end-to-end test
        test(parser, "(3+4)+((2+3)+0+(1+2+3))+9", "addition", new Parse(27, 25));
        test(parser, "1+1+b", "addition", new Parse(2, 3));

        System.out.println("All testcases passed!");
    }

    public static void main(String[] args) {
        test();
    }

}