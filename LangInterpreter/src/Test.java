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
            if (expected.equals("syntax error")) {
                System.out.println("[PASS] got syntax error for \'" + str + "\'");
                return;
            }
            System.out.println("[FAIL] got syntax error for \'" + str + "\'");
            return;
        }
        if (!parse.toString().equals(expected)) {
            System.out.println("[FAIL] expected: \'" + expected + "\' for \'" + str + "\'\n but got: \'" + parse + "\'");
        }
        else {
            System.out.println("[PASS] expected: \'" + expected + "\' for \'" + str + "\'");
        }
    }

    private static void test_interpreter(Parser parser, Interpreter interpreter, String input) {
        Parse tree = parser.parse(input);
        System.out.println("----PROGRAM:\n"+input); // print the program string
        System.out.println("S-EXP:\n"+tree); // print tree s-exp

        System.out.println("OUTPUT:");
        // print the output of the execution
        String output = interpreter.execute(tree);
        System.out.println("OUTPUTSTRING:\n"+output);
    }

    public static void test() {
        Parser parser = new Parser();
        Interpreter interpreter = new Interpreter();

        System.out.println("-------------VARIABLE PARSES-------------");
        test_parse(parser, "print ;", "syntax error");
        test_parse(parser, "print 3+;", "syntax error");
        test_parse(parser, "print 1 + 1 ;", "(sequence (print (+ 1 1)))");
        test_parse(parser, "print 1 * 1 ;", "(sequence (print (* 1 1)))");
        test_parse(parser, " print 1 + 2 * 3 - 4 / 5 ; ", "(sequence (print (- (+ 1 (* 2 3)) (/ 4 5))))");
        test_parse(parser, " print 4 * 2 + 3 ; ", "(sequence (print (+ (* 4 2) 3)))");
        test_parse(parser, "2;", "(sequence 2)");
        test_parse(parser, "var1 + var2;", "(sequence (+ (lookup var1) (lookup var2)))");
        test_parse(parser, "var1 * var2;", "(sequence (* (lookup var1) (lookup var2)))");
        test_parse(parser, "2", "syntax error");
        //test_parse_error(parser, "2", "", new AssertionError("syntax error"));
        test_parse(parser, "print (2*3);", "(sequence (print (* 2 3)))");
        //test_parse(parser, "print (2+5);", "(sequence (print (+ 2 3)))"); //test testing for fail test
        test_parse(parser, "print 2", "syntax error");
        test_parse(parser,"  print 5;# print 7\nprint 8;", "(sequence (print 5) (print 8))");
        test_parse(parser," print\n#whatever print 54\n27;", "(sequence (print 27))");
        test_parse(parser, "var test = 2;", "(sequence (declare test 2))");
        test_parse(parser,"var test = 2+3;", "(sequence (declare test (+ 2 3)))");
        test_parse(parser, "var print = 2;","syntax error");

        test_parse(parser,"var test = 2+3; print test;",
                "(sequence (declare test (+ 2 3)) (print (lookup test)))");
        test_parse(parser,"# tests to make sure you cannot declare the same variable twice\n" +
                        "var test = 2+3; var test = 1;",
                "(sequence (declare test (+ 2 3)) (declare test 1))");
        test_parse(parser, "# tests to make sure assignment is working\nvar test = 2+3; test = 1; print test;",
                "(sequence (declare test (+ 2 3)) (assign (varloc test) 1) (print (lookup test)))");

        test_parse(parser, "var num1 = 3; var num2 = 2; print num1 * num2;",
                "(sequence (declare num1 3) (declare num2 2) (print (* (lookup num1) (lookup num2))))");
        test_parse(parser, "var num = 3; num = num = num; print num;", "syntax error");

        test_parse(parser, "# no ;\na = 3", "syntax error");

        // sus test
        test_parse(parser, "print7;", "(sequence (lookup print7))");

        // same but smaller so its easier to debug
        test_parse(parser,
                "# \n" +
                        "# .\n" +
                        "\n" +
                        "var counter# \n" +
                        "=# \n" +
                        "0# \n" +
                        ";\n" +
                        "print counter;\n",
                "(sequence (declare counter 0) (print (lookup counter)))");

        test_parse(parser, "# wrong keyword\n" +
                        "var var = 1;\n" +
                        "print var;",
                "syntax error");

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

        test_parse(parser, "print 3; var b = c;", "(sequence (print 3) (declare b (lookup c)))");

        test_parse(parser, "# \n" +
                "var a = 2;\n" +
                "var b = 4;\n" +
                "print a*b;\n", "(sequence (declare a 2) (declare b 4) (print (* (lookup a) (lookup b))))");

        // fibonacci vars test // tricky b/c original file had \t, and \t doesn't render in here
        test_parse(parser, "# math expression with multiple variables\n" +
                "\n" +
                "var a = 1;\n" +
                "var b = 2;\n" +
                "var c = 3;\n" +
                "var d = 4;\n" +
                "var e = 5;\n" +
                "print (a+b+c+d+e)*e; #75\n\n", "(sequence (declare a 1) (declare b 2) (declare c 3) (declare d 4) (declare e 5) (print (* (+ (+ (+ (+ (lookup a) (lookup b)) (lookup c)) (lookup d)) (lookup e)) (lookup e))))");

        test_parse(parser, "# weirdly formatted on purpose\nvar\t\ndistance\n=\n\t0-4;print\tdistance;\n", "(sequence (declare distance (- 0 4)) (print (lookup distance)))");

        test_parse(parser, "# weirdly formatted on purpose\n", "(sequence)");
        test_parse(parser, "# weirdly# howdy #\nprint 2;#\tprint 2;", "(sequence (print 2))");
        test_parse(parser, "# making sure your space parses parse tabs \t\n\tvar\tb\t=\t2\t;\tprint\tb\t;\t", "(sequence (declare b 2) (print (lookup b)))");

        test_parse(parser, "# checking your parenthesized variable parse!!\n" +
                "var d = 0;\n" +
                "d;\n" +
                "(d);", "(sequence (declare d 0) (lookup d) (lookup d))");

        System.out.println("\n---------------------------------CONTROL FLOW PARSES-----------------------------------");
        // test less than
        test_parse(parser, " print 1 > 3 ; ", "(sequence (print (> 1 3)))");

        // test and, or, not
        test_parse(parser, " print 1 && 1 ; print 2||2 ; 3; !4; ", "(sequence (print (&& 1 1)) (print (|| 2 2)) 3 (! 4))");

        // test 2
        test_parse(parser, " print !2<=3&&4+5; 6 ==   3; 2 >=  4; ", "(sequence (print (&& (! (<= 2 3)) (+ 4 5))) (== 6 3) (>= 2 4))");

        // closure test
        test_parse(parser, "var a = 1; if (a) {var a = 2; print a;} print a;","(sequence (declare a 1) (if (lookup a) (sequence (declare a 2) (print (lookup a)))) (print (lookup a)))");

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
        test_parse(parser, " if ( 4 > 5 ) {}}", "syntax error");

        // test if_else statement
        test_parse(parser, " if ( 4 > 5 ) {} else {} ", "(sequence (ifelse (> 4 5) (sequence) (sequence)))");

        test_parse(parser, "while (var a = 0) { print 1; }\n", "syntax error");

        // -------------FUNCTIONS
        System.out.println("\n----------------------FUNCTION TESTCASES---------------------------");

        test_parse(parser, "a();", "(sequence (call (lookup a) (arguments)))");
        test_parse(parser, "a(b);", "(sequence (call (lookup a) (arguments (lookup b))))");
        test_parse(parser, "a(b());", "(sequence (call (lookup a) (arguments (call (lookup b) (arguments)))))");
        test_parse(parser, "a(b, c, d);", "(sequence (call (lookup a) (arguments (lookup b) (lookup c) (lookup d))))");
        test_parse(parser, "var a = func () {};", "(sequence (declare a (function (parameters) (sequence))))");
        test_parse(parser, "var a = func (c) {print c;};", "(sequence (declare a (function (parameters c) (sequence (print (lookup c))))))");
        test_parse(parser, "var a = func (d, c, e) {print c;};", "(sequence (declare a (function (parameters d c e) (sequence (print (lookup c))))))");

        // end-to-end test from Ben
        test_parse(parser,
                "var outer = func(a){\n" +
                "        var inner = func(a){\n" +
                "                ret a;\n" +
                "        };\n" +
                "        ret inner(a);\n" +
                "};\n" +
                "while(outer(0)){\n" +
                "        print 5;\n" +
                "}\n" +
                "print 2;\n",
                "(sequence (declare outer (function (parameters a) (sequence (declare inner (function (parameters a) (sequence (return (lookup a))))) (return (call (lookup inner) (arguments (lookup a))))))) (while (call (lookup outer) (arguments 0)) (sequence (print 5))) (print 2))");

        // test ret
        test_parse(parser, "var outer = func(a){ ret a ; } ;", "(sequence (declare outer (function (parameters a) (sequence (return (lookup a))))))");

        // comma test
        test_parse(parser, "var a = func(a, b,) {};\n" +
                "print a(1, 2,);", "syntax error");

        test_parse(parser, "print (3 - (4/2);", "syntax error");

        System.out.println("\n---------------------------------INTERPRETER TESTS-----------------------------------");

        test_interpreter(parser, interpreter, "print ;");
        test_interpreter(parser, interpreter, "print 2+3;");
        test_interpreter(parser, interpreter, "print 2-3;");
        test_interpreter(parser, interpreter, "print 2*3;");
        test_interpreter(parser, interpreter, "print 3/2;");

        test_interpreter(parser, interpreter, "print 3+2;print 3/0;");
        test_interpreter(parser, interpreter, "print 3+2;print 3/(2-2);");

        test_interpreter(parser, interpreter, "var a = 1;");
        test_interpreter(parser, interpreter, "var a = 1; print a;");
        test_interpreter(parser, interpreter, "var a = 1; a = 2; print a;");
        test_interpreter(parser, interpreter, "var num = 3; num = num = num; print num;\n");
        test_interpreter(parser, interpreter, "print7;");
        test_interpreter(parser, interpreter, "if (2) {print 2;}");
        test_interpreter(parser, interpreter, "if (0) {print 0;}");
        test_interpreter(parser, interpreter, "if (1) {print 0;} else {print 3;}");
        test_interpreter(parser, interpreter, "var a = 1; while (a) {print 5; a = 0;}");
    }

    public static void main(String[] args) {
        test();
    }
}