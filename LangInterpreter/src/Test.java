public class Test {
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

    private static void test_parse_error(Parser parser, String str, String term, Error error) {
        try { // if Error is not found
            System.out.println("[PASS] no error found for \'" + str + "\', outputted \'" +parser.parse(str, 0, term) + "\', term: " + term + "\'");
        } catch(Error e) {
            if (error.toString().equals(e.toString()) && error.getClass().equals(e.getClass())) { //FIXME check if different types of error?
                System.out.println("[PASS ERROR] expected: \'" + e.toString() + "\' for \'" + str + "\', term: \'" + term + "\'");
            }
            else {
                System.out.println("[FAIL ERROR] expected: \'" + error.toString() + "\' for \'" + str + "\'\n but got: \'" + e.toString() + "\', term: " + term + "\'");
            }
        }
    }

    private static void test_parse(Parser parser, String str, String term, String expected) {
        Parse parse = parser.parse(str, 0, term);

        if (!parse.toString().equals(expected)) {
            System.out.println("[FAIL] expected: \'" + expected + "\' for \'" + str + "\'\n but got: \'" + parse.toString() + "\', term: " + term + "\'");
        }
        else {
            System.out.println("[PASS] expected: \'" + expected + "\' for \'" + str + "\', term: \'" + term + "\'");
        }
    }

    public static void test() {
        Parser parser = new Parser();

        //System.out.println(parser.parse(" 2 + 2 * 2 ", "add|sub").toString());
        //System.out.println(parser.parse("2*2", "mul|div").toString());
        //System.out.println(parser.parse("2", "mul|div").toString());
        //System.out.println(parser.parse("2", "sequence").toString());
        //System.out.println(parser.parse(" 2 + 2 * 2 ", "sequence").toString());
        //System.out.println(parser.parse("print 2 + 2 * 2 ;", "sequence").toString());
        //System.out.println(parser.parse("print 2+2*2;", "sequence").toString());
        //System.out.println(parser.parse("print2+2*2;", "sequence").toString()); //should throw error
        //System.out.println(parser.parse("print 2 + 2 * 2 ", "sequence").toString());

        test_parse(parser, "print 2;", "sequence", "(sequence (print 2))");
        test_parse(parser, "2;", "sequence", "(sequence 2)");
        test_parse_error(parser, "2", "sequence", new AssertionError("syntax error; missing semicolon"));
        test_parse(parser, "print (2+3);", "sequence", "(sequence (print (+ 2 3)))");
        //test_parse(parser, "print (2+5);", "sequence", "(sequence (print (+ 2 3)))"); //test testing for fail test
        test_parse_error(parser, "print 2", "sequence", new AssertionError("syntax error; missing semicolon"));
        test_parse_error(parser, "print 2;", "sequence", new AssertionError("syntax error"));
        test_parse(parser,"print 5;# print 7\nprint 8;", "sequence", "(sequence (print 5) (print 8))");
        test_parse(parser,"print\n#whatever print 54\n27;", "sequence", "(sequence (print 27))");

        //System.out.println("All testcases passed!");
    }

    public static void main(String[] args) {
        test();
    }
}