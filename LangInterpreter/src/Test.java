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

        System.out.println("-------------VARIABLE PARSES-------------");
        test_parse(parser, "print 1 + 1 ;", "(sequence (print (+ 1 1)))");
        test_parse(parser, "print 1 * 1 ;", "(sequence (print (* 1 1)))");
        test_parse(parser, " print 1 + 2 * 3 - 4 / 5 ; ", "(sequence (print (- (+ 1 (* 2 3)) (/ 4 5))))");
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

        test_parse(parser,
                "# testing for correct error when a variable is not initalized\n" +
                "var num = ;\n" +
                "print num;",
                "null");
        test_parse(parser, "# wrong keyword\n" +
                        "var var = 1;\n" +
                        "print var;",
                "null");

        test_parse(parser, "# switch variables\n" +
                        "var a = 2;\n" +
                        "print a;        #2\n" +
                        "\n" +
                        "var b = 3;\n" +
                        "print b; #3\n" +
                        "\n" +
                        "var temp = b;\n" +
                        "print temp; #3\n" +
                        "\n" +
                        "b = a;\n" +
                        "print b; #2\n" +
                        "\n" +
                        "a = temp;\n" +
                        "print a; #3\n" +
                        "print temp; #3\n",
                "(sequence (declare a 2) (print (lookup a)) (declare b 3) (print (lookup b)) (declare temp (lookup b)) (print (lookup temp)) (assign (varloc b) (lookup a)) (print (lookup b)) (assign (varloc a) (lookup temp)) (print (lookup a)) (print (lookup temp)))");


        System.out.println("\n------------CONTROL FLOW PARSES-------------");
        // test less than
        test_parse(parser, " print 1 > 3 ; ", "(sequence (print (> 1 3)))");

        // test and, or, not
        test_parse(parser, " print 1 && 1 ; print 2||2 ; 3; !4; ", "(sequence (print (&& 1 1)) (print (|| 2 2)) 3 (! 4))");

        // test 2
        test_parse(parser, " print !2<=3&&4+5; 6 ==   3; 2 >=  4; ", "(sequence (print (&& (! (<= 2 3)) (+ 4 5))) (== 6 3) (>= 2 4))");

        // vars and logic
        test_parse(parser, "var t = 1 < 2;\n" +
                        "var f = 1 > 2;\n" +
                        "print t + f;",
                "(sequence (declare t (< 1 2)) (declare f (> 1 2)) (print (+ (lookup t) (lookup f))))");

        // test long if statement
        test_parse(parser, "if(x==1 || y == 2){\nvar x = x *6;\n}\nprint x;", "(sequence (if (|| (== (lookup x) 1) (== (lookup y) 2)) (sequence (declare x (* (lookup x) 6)))) (print (lookup x)))");

        // test if_statement and or_statement
        test_parse(parser, "var x = 1;\n" +
                        "var y = 674;\n" +
                        "if(x==1 || y == 2){\n" +
                        "    x = x *6;\n" +
                        "}\n" +
                        "print x;\n",
                "(sequence (declare x 1) (declare y 674) (if (|| (== (lookup x) 1) (== (lookup y) 2)) (sequence (assign (varloc x) (* (lookup x) 6)))) (print (lookup x)))");

        // test while_statement
        test_parse(parser, "var x = 5;\n" +
                "while (x > 0) {\n" +
                "  x = x - 1;\n" +
                "}\n" +
                "print x;",
                "(sequence (declare x 5) (while (> (lookup x) 0) (sequence (assign (varloc x) (- (lookup x) 1)))) (print (lookup x)))");

        // test empty if statement
        test_parse(parser, " if (2) {} 2; ", "(sequence (if 2 (sequence)) 2)");

        // test empty while statement
        test_parse(parser, " while (2) {} 2; ", "(sequence (while 2 (sequence)) 2)");

        // test regular if statement
        test_parse(parser, " if ( 4 > 5 ) { print 5 ; } print 6 ; ", "(sequence (if (> 4 5) (sequence (print 5))) (print 6))");

        // test syntax error if statement
        test_parse(parser, " if ( 4 > 5 ) {}}", "null");

        // test if_else statement
        test_parse(parser, " if ( 4 > 5 ) {} else {} ", "(sequence (ifelse (> 4 5) (sequence) (sequence)))");


    }

    public static void main(String[] args) {
        test();
    }
}