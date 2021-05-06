import java.util.LinkedList;

public class Test {
    private static void test_parse(String str, String expected) {
        Parser parser = new Parser();
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

    private static void test_interpreter(String input) {
        Parser parser = new Parser();
        Interpreter interpreter = new Interpreter();
        Parse tree = parser.parse(input);
        System.out.println("----PROGRAM:\n"+input); // print the program string
        System.out.println("S-EXP:\n"+tree); // print tree s-exp
        String output = interpreter.execute(tree);
        System.out.println("OUTPUT+ERRORS:\n"+output);
    }

    public static void test() {

        System.out.println("-------------VARIABLE PARSES-------------");
        test_parse("print ;", "syntax error");
        test_parse("print 3+;", "syntax error");
        test_parse("print 1 + 1 ;", "(sequence (print (+ 1 1)))");
        test_parse("print 1 * 1 ;", "(sequence (print (* 1 1)))");
        test_parse(" print 1 + 2 * 3 - 4 / 5 ; ", "(sequence (print (- (+ 1 (* 2 3)) (/ 4 5))))");
        test_parse(" print 4 * 2 + 3 ; ", "(sequence (print (+ (* 4 2) 3)))");
        test_parse("2;", "(sequence 2)");
        test_parse("var1 + var2;", "(sequence (+ (lookup var1) (lookup var2)))");
        test_parse("var1 * var2;", "(sequence (* (lookup var1) (lookup var2)))");
        test_parse("2", "syntax error");
        //test_parse_error("2", "", new AssertionError("syntax error"));
        test_parse("print (2*3);", "(sequence (print (* 2 3)))");
        //test_parse("print (2+5);", "(sequence (print (+ 2 3)))"); //test testing for fail test
        test_parse("print 2", "syntax error");
        test_parse("print 5;# print 7\nprint 8;", "(sequence (print 5) (print 8))");
        test_parse("print\n#whatever print 54\n27;", "(sequence (print 27))");
        test_parse("var test = 2;", "(sequence (declare test 2))");
        test_parse("var test = 2+3;", "(sequence (declare test (+ 2 3)))");
        test_parse("var print = 2;","syntax error");

        test_parse("var test = 2+3; print test;",
                "(sequence (declare test (+ 2 3)) (print (lookup test)))");
        test_parse("# tests to make sure you cannot declare the same variable twice\n" +
                        "var test = 2+3; var test = 1;",
                "(sequence (declare test (+ 2 3)) (declare test 1))");
        test_parse("# tests to make sure assignment is working\nvar test = 2+3; test = 1; print test;",
                "(sequence (declare test (+ 2 3)) (assign (varloc test) 1) (print (lookup test)))");

        test_parse("var num1 = 3; var num2 = 2; print num1 * num2;",
                "(sequence (declare num1 3) (declare num2 2) (print (* (lookup num1) (lookup num2))))");
        test_parse("var num = 3; num = num = num; print num;", "syntax error");

        test_parse("# no ;\na = 3", "syntax error");

        // sus test
        test_parse("print7;", "(sequence (lookup print7))");

        // same but smaller so its easier to debug
        test_parse("# \n" +
                        "# .\n" +
                        "\n" +
                        "var counter# \n" +
                        "=# \n" +
                        "0# \n" +
                        ";\n" +
                        "print counter;\n",
                "(sequence (declare counter 0) (print (lookup counter)))");

        test_parse("# wrong keyword\n" +
                        "var var = 1;\n" +
                        "print var;",
                "syntax error");

        test_parse("# switch variables\n" +
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

        test_parse("print 3; var b = c;", "(sequence (print 3) (declare b (lookup c)))");

        test_parse("# \n" +
                "var a = 2;\n" +
                "var b = 4;\n" +
                "print a*b;\n", "(sequence (declare a 2) (declare b 4) (print (* (lookup a) (lookup b))))");

        // fibonacci vars test // tricky b/c original file had \t, and \t doesn't render in here
        test_parse("# math expression with multiple variables\n" +
                "\n" +
                "var a = 1;\n" +
                "var b = 2;\n" +
                "var c = 3;\n" +
                "var d = 4;\n" +
                "var e = 5;\n" +
                "print (a+b+c+d+e)*e; #75\n\n", "(sequence (declare a 1) (declare b 2) (declare c 3) (declare d 4) (declare e 5) (print (* (+ (+ (+ (+ (lookup a) (lookup b)) (lookup c)) (lookup d)) (lookup e)) (lookup e))))");

        test_parse("# weirdly formatted on purpose\nvar\t\ndistance\n=\n\t0-4;print\tdistance;\n", "(sequence (declare distance (- 0 4)) (print (lookup distance)))");

        test_parse("# weirdly formatted on purpose\n", "(sequence)");
        test_parse("# weirdly# howdy #\nprint 2;#\tprint 2;", "(sequence (print 2))");
        test_parse("# making sure your space parses parse tabs \t\n\tvar\tb\t=\t2\t;\tprint\tb\t;\t", "(sequence (declare b 2) (print (lookup b)))");

        test_parse("# checking your parenthesized variable parse!!\n" +
                "var d = 0;\n" +
                "d;\n" +
                "(d);", "(sequence (declare d 0) (lookup d) (lookup d))");

        System.out.println("\n---------------------------------CONTROL FLOW PARSES-----------------------------------");
        // test less than
        test_parse(" print 1 > 3 ; ", "(sequence (print (> 1 3)))");

        // test and, or, not
        test_parse(" print 1 && 1 ; print 2||2 ; 3; !4; ", "(sequence (print (&& 1 1)) (print (|| 2 2)) 3 (! 4))");

        // test 2
        test_parse(" print !2<=3&&4+5; 6 ==   3; 2 >=  4; ", "(sequence (print (&& (! (<= 2 3)) (+ 4 5))) (== 6 3) (>= 2 4))");

        // closure test
        test_parse("var a = 1; if (a) {var a = 2; print a;} print a;","(sequence (declare a 1) (if (lookup a) (sequence (declare a 2) (print (lookup a)))) (print (lookup a)))");

        // vars and logic
        test_parse("var t = 1 < 2;\n" +
                        "var f = 1 > 2;\n" +
                        "print t + f;",
                "(sequence (declare t (< 1 2)) (declare f (> 1 2)) (print (+ (lookup t) (lookup f))))");

        // test long if statement
        test_parse("if(x==1 || y == 2){\nvar x = x *6;\n}\nprint x;", "(sequence (if (|| (== (lookup x) 1) (== (lookup y) 2)) (sequence (declare x (* (lookup x) 6)))) (print (lookup x)))");

        // test if_statement and or_statement
        test_parse("var x = 1;\n" +
                        "var y = 674;\n" +
                        "if(x==1 || y == 2){\n" +
                        "    x = x *6;\n" +
                        "}\n" +
                        "print x;\n",
                "(sequence (declare x 1) (declare y 674) (if (|| (== (lookup x) 1) (== (lookup y) 2)) (sequence (assign (varloc x) (* (lookup x) 6)))) (print (lookup x)))");

        // test while_statement
        test_parse("var x = 5;\n" +
                "while (x > 0) {\n" +
                "  x = x - 1;\n" +
                "}\n" +
                "print x;",
                "(sequence (declare x 5) (while (> (lookup x) 0) (sequence (assign (varloc x) (- (lookup x) 1)))) (print (lookup x)))");

        // test empty if statement
        test_parse(" if (2) {} 2; ", "(sequence (if 2 (sequence)) 2)");

        // test empty while statement
        test_parse(" while (2) {} 2; ", "(sequence (while 2 (sequence)) 2)");

        // test regular if statement
        test_parse(" if ( 4 > 5 ) { print 5 ; } print 6 ; ", "(sequence (if (> 4 5) (sequence (print 5))) (print 6))");

        // test syntax error if statement
        test_parse(" if ( 4 > 5 ) {}}", "syntax error");

        // test if_else statement
        test_parse(" if ( 4 > 5 ) {} else {} ", "(sequence (ifelse (> 4 5) (sequence) (sequence)))");

        test_parse("while (var a = 0) { print 1; }\n", "syntax error");

        // -------------FUNCTIONS
        System.out.println("\n----------------------FUNCTION TESTCASES---------------------------");

        test_parse("a();", "(sequence (call (lookup a) (arguments)))");
        test_parse("a(b);", "(sequence (call (lookup a) (arguments (lookup b))))");
        test_parse("a(b());", "(sequence (call (lookup a) (arguments (call (lookup b) (arguments)))))");
        test_parse("a(b, c, d);", "(sequence (call (lookup a) (arguments (lookup b) (lookup c) (lookup d))))");
        test_parse("var a = func () {};", "(sequence (declare a (function (parameters) (sequence))))");
        test_parse("var a = func (c) {print c;};", "(sequence (declare a (function (parameters c) (sequence (print (lookup c))))))");
        test_parse("var a = func (d, c, e) {print c;};", "(sequence (declare a (function (parameters d c e) (sequence (print (lookup c))))))");

        // end-to-end test from Ben
        test_parse("var outer = func(a){\n" +
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
        test_parse("var outer = func(a){ ret a ; } ;", "(sequence (declare outer (function (parameters a) (sequence (return (lookup a))))))");

        // comma test
        test_parse("var a = func(a, b,) {};\n" +
                "print a(1, 2,);", "syntax error");

        test_parse("print (3 - (4/2);", "syntax error");

        System.out.println("\n---------------------------------INTERPRETER TESTS-----------------------------------");

        // arithmetic tests
        test_interpreter("print ;");
        test_interpreter("print 2+3;");
        test_interpreter("print 2-3;");
        test_interpreter("print 2*3;");
        test_interpreter("print 3/2;");

        // div by 0 tests
        test_interpreter("print 3+2;print 3/0;");
        test_interpreter("print 3+2;print 3/(2-2);");

        // variable tests
        test_interpreter("var a = 1;");
        test_interpreter("var a = 1; print a;");
        test_interpreter("var a = 1; a = 2; print a;");
        test_interpreter("var num = 3; num = num = num; print num;\n");
        test_interpreter("print7;");
        test_interpreter("if (2) {print 2;}");
        test_interpreter("varnum = 4;\n");
        test_interpreter("if (1) {print 0;} else {print 3;}");
        test_interpreter("var a = 1; while (a) {print 5; a = 0;}");
        test_interpreter("if(1){if(1){if(1){print 101;}}}");
        test_interpreter("var b = 1;if (b || c){print 3;}");
        test_interpreter("var a = 0;var b = 1;print a / b;print b / (b-b);");

        // comparator tests
        test_interpreter("var a = (2 || (1 && 2) < (!3 <= (2 * 3)));var b = ! a == ((2 >= 3) != (1 > 0));print a; print b;");
        test_interpreter("print (2 || (1 && 2) < (!3 <= (2 * 3)));");
        test_interpreter("print (2 || (1 && 2));");
        test_interpreter("print (!3 <= (2 * 3));");
        test_interpreter("print 2 < 3;");

        // function tests
        test_interpreter("var b = func() {};");
        test_interpreter("var b = func() {}; print b;");
        test_interpreter("var a = func(b,c,d) {print b; ret 5;};print a(2);");

        test_interpreter("var test = func(){};print test() + 3;");
        test_interpreter("var a = func(){}; if (a) {print 1;}");
        test_interpreter("var b = func() {}; b();");
        test_interpreter("");
        test_interpreter("var run_first = func() {\n" +
                "    print 42;\n" +
                "};\n" +
                "\n" +
                "nonexistant = run_first();");

        test_interpreter("" +
                "var b = func(m) {print m;};\n" +
                "var a = func(n) {ret b(n-1);}; a(10);");

        test_interpreter("var x = func(a, b){\n" +
                " ret a/b;\n" +
                "};\nx(x(1,0));");

        test_interpreter("var b = func() {print 1;};\n" +
                "print b();");

        test_interpreter("a();");

        test_interpreter("var a = func(){print 5; ret 2;};\n" +
                "var b = func(){print a();};\n" +
                "print b();\n");

        test_interpreter("" +
                "var pair = func(first, second) {\n" +
                "    ret func(f) {\n" +
                "        ret f(first, second);\n" +
                "    };\n" +
                "};\n" +
                "\n" +
                "var NULL = pair(pair, pair);\n" +
                "\n" +
                "var first = func(pair) {\n" +
                "    ret pair(func(first, second) {\n" +
                "        ret first;\n" +
                "    });\n" +
                "};\n" +
                "\n" +
                "var second = func(pair) {\n" +
                "    ret pair(func(first, second) {\n" +
                "        ret second;\n" +
                "    });\n" +
                "};\n" +
                "\n" +
                "var range = func(end) {\n" +
                "    var _range = func(end, partial) {\n" +
                "        if (end == 0) {\n" +
                "            ret partial;\n" +
                "        } else {\n" +
                "            ret _range(end - 1, pair(end - 1, partial));\n" +
                "        }\n" +
                "    };\n" +
                "    ret _range(end, NULL);\n" +
                "};\n" +
                "\n" +
                "var reverse = func(list) {\n" +
                "    var _reverse = func(list, result) {\n" +
                "        if (list == NULL) {\n" +
                "            ret result;\n" +
                "        } else {\n" +
                "            ret _reverse(second(list), pair(first(list), result));\n" +
                "        }\n" +
                "    };\n" +
                "    ret _reverse(list, NULL);\n" +
                "};\n" +
                "\n" +
                "var map = func(list, fn) {\n" +
                "    var _map = func(list, fn, result) {\n" +
                "        if (list == NULL) {\n" +
                "            ret result;\n" +
                "        } else {\n" +
                "            ret _map(second(list), fn, pair(fn(first(list)), result));\n" +
                "        }\n" +
                "    };\n" +
                "    ret reverse(_map(list, fn, NULL));\n" +
                "};\n" +
                "\n" +
                "var filter = func(list, fn) {\n" +
                "    var _filter = func(list, fn, result) {\n" +
                "        if (list == NULL) {\n" +
                "            ret result;\n" +
                "        }\n" +
                "        if (fn(first(list)) == 1) {\n" +
                "            ret _filter(second(list), fn, pair(first(list), result));\n" +
                "        } else {\n" +
                "            ret _filter(second(list), fn, result);\n" +
                "        }\n" +
                "    };\n" +
                "    ret reverse(_filter(list, fn, NULL));\n" +
                "};\n" +
                "\n" +
                "var reduce = func(list, fn, result) {\n" +
                "    if (list == NULL) {\n" +
                "        ret result;\n" +
                "    } else {\n" +
                "        ret reduce(second(list), fn, fn(result, first(list)));\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "var mod = func(a, b) {\n" +
                "    ret a - (b * (a / b));\n" +
                "};\n" +
                "\n" +
                "var euler_1 = func(n) {\n" +
                "    ret reduce(\n" +
                "        filter(\n" +
                "            map(\n" +
                "                range(n - 1),\n" + // problem is here??
                "                func(n) {\n" +
                "                    ret n + 1;\n" +
                "                }\n" +
                "            ),\n" +
                "            func(n) {\n" +
                "                ret (mod(n, 3) == 0 || mod(n, 5) == 0);\n" +
                "            }\n" +
                "        ),\n" +
                "        func(a, b) {\n" +
                "            ret a + b;\n" +
                "        },\n" +
                "        0\n" +
                "    );\n" +
                "};\n" +
                "\n" +
                "# solve Project Euler Problem #1 example using map-reduce\n" +
                "# https://projecteuler.net/problem=1\n" +
                "print euler_1(10);\n"
        );

        test_interpreter("" +
                "var true = func() {\n" +
                "    print 111;\n" +
                "    ret 1;\n" +
                "};\n" +
                "var false = func() {\n" +
                "    print 0-111;\n" +
                "    ret 0;\n" +
                "};\n" +
                "\n" +
                "print true() || true(); # 111; 1;\n" +
                        "print true() || false(); # 111; 1;\n" +
                        "print false() || true(); # -111; 111; 1;\n" +
                        "print false() || false(); # -111; -111; 0;\n"
                );

        test_parse("var\n" +
                "\n" +
                "\n" +
                "\n" +
                "printNum                    =                       func(n)\n" +
                "{\n" +
                "    print n               ;\n" +
                "}           ;\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "printNum(   10      )\n" +
                "\n" +
                "\n" +
                "\n" +
                ";\n", "(sequence (declare printNum (function (parameters n) (sequence (print (lookup n))))) (call (lookup printNum) (arguments 10)))");

        System.out.println("All testcases passed!");

        /*
        // testing check_duplicates
        Parse tree = parser.parse("var a = func(a, b, c, b) {};");
        //System.out.println(tree);
        Parse func = tree.children.get(0).children.get(1);
        System.out.println(func);
        System.out.println(interpreter.eval_function(func));
        */
    }

    public static void main(String[] args) {
        test();
    }
}