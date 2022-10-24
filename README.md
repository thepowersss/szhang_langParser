# szhang_langParser
for COMP 337

## UNSTABLE (CLASS IMPLEMENTATION)

This repo contains code for lang (an invented toy language by Professor Justin Li), a strongly and partially typed programming language with lexical scoping and closures. This branch supports arithmetic, variables, control flow, loops, functions, recursion, and type-checking. For the stable branch that does NOT support classes, go to /stable

How to run/play with lang:
1. navigate to LangInterpreter/src/
2. on command line, run the command **java Test "your lang code here"** IMPORTANT you MUST use quotations. example: **java Test "var a = class { var num = 2;}; print a().num;"** should output "2"
3. to run on debug mode, run the command **java Test "your lang code here" debug**
4. command line will output the result of your code

testcases from https://github.com/justinnhli/lang-test-cases
