package com.thecout.lox.Parser;

import com.thecout.lox.Token;

public class ParserError extends Exception{
    static void error(Token token, String message) {
        System.out.printf("%d %s\n", token.line, message);
    }

    ParserError(Token token, String message) {
        super(String.format("%d %s\n", token.line, message));
        System.out.printf("%d %s\n", token.line, message);
    }
}
