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

    private static void test_parse_error(Parser parser, String str, String expected, Error error) {
        try { // if Error is not found
            //System.out.println("[PASS] no error found for \'" + str + "\', outputted \'" +parser.parse(str, 0, term) + "\', term: " + term + "\'");
            test_parse(parser, str, expected);
        } catch(Error e) {
            if (error.toString().equals(e.toString()) && error.getClass().equals(e.getClass())) { //FIXME check if different types of error?
                System.out.println("[PASS ERROR] expected: \'" + e.toString() + "\' for \'" + str + "\'");
            }
            else {
                System.out.println("[FAIL ERROR] expected: \'" + error.toString() + "\' for \'" + str + "\'\n but got: \'" + e.toString() + "\'");
            }
        }
    }

    private static void test_parse(Parser parser, String str, String expected) {
        Parse parse = parser.parse(str);
        if (parse == null) {
            if (expected.equals("null")) {
                System.out.println("[PASS] got null for \'" + str + "\'");
                return;
            }
            System.out.println("[FAIL] got null for \'" + str + "\'");
            return;
        }
        if (!parse.toString().equals(expected)) {
            System.out.println("[FAIL] expected: \'" + expected + "\' for \'" + str + "\'\n but got: \'" + parse.toString() + "\'");
        }
        else {
            System.out.println("[PASS] expected: \'" + expected + "\' for \'" + str + "\'");
        }
    }

    public static void test() {
        Parser parser = new Parser();
/*
        test_parse(parser, "print 1 + 1 ;", "(sequence (print (+ 1 1)))");
        test_parse(parser, "print 1 * 1 ;", "(sequence (print (* 1 1)))");
        test_parse(parser, " print 4 * 2 + 3 ; ", "(sequence (print (+ (* 4 2) 3)))");
        test_parse(parser, "2;", "(sequence 2)");
        test_parse(parser, "var1 + var2;", "(sequence (+ (lookup var1) (lookup var2)))");
        test_parse(parser, "var1 * var2;", "(sequence (* (lookup var1) (lookup var2)))");
        test_parse(parser, "2", "null");
        //test_parse_error(parser, "2", "", new AssertionError("syntax error"));
        test_parse(parser, "print (2*3);", "(sequence (print (* 2 3)))");
        //test_parse(parser, "print (2+5);", "(sequence (print (+ 2 3)))"); //test testing for fail test
        test_parse(parser, "print 2", "null");
        test_parse(parser,"  print 5;# print 7\nprint 8;", "(sequence (print 5) (print 8))");
        test_parse(parser," print\n#whatever print 54\n27;", "(sequence (print 27))");
        test_parse(parser, "var test = 2;", "(sequence (declare test 2))");
        test_parse(parser,"var test = 2+3;", "(sequence (declare test (+ 2 3)))");
        test_parse(parser, "var print = 2;","null");

        test_parse(parser,"var test = 2+3; print test;",
                "(sequence (declare test (+ 2 3)) (print (lookup test)))");
        test_parse(parser,"# tests to make sure you cannot declare the same variable twice\n" +
                        "var test = 2+3; var test = 1;",
                "(sequence (declare test (+ 2 3)) (declare test 1))");
        test_parse(parser, "# tests to make sure assignment is working\nvar test = 2+3; test = 1; print test;",
                "(sequence (declare test (+ 2 3)) (assign (varloc test) 1) (print (lookup test)))");

        test_parse(parser, "var num1 = 3; var num2 = 2; print num1 * num2;",
                "(sequence (declare num1 3) (declare num2 2) (print (* (lookup num1) (lookup num2))))");
        test_parse(parser, "var num = 3; num = num = num; print num;", "null");
        test_parse(parser, "# no ;\na = 3", "null");
        */
        test_parse(parser,
                "# testing for correct error when a variable is not initalized\n" +
                "var num = ;\n" +
                "print num;\n",
                "null");


    }

    public static void main(String[] args) {
        test();
    }
}