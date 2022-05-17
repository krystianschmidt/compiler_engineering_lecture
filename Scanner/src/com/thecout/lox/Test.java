package com.thecout.lox;

public class Test {
    public static void main(String[] args) {
        String program = """
            // Kommentar
            ( ) { } , . - + ; / * ! != = == < <= > >=
            fun printSum(a,b) {
            print a+b;
            }
            print 25+60;
            """;

        Scanner scan = new Scanner(program);
        for (Token token :
                scan.scan()) {
            System.out.println(token);
        }
    }
}
