package com.thecout.lox;


public enum TokenType {
    // Single-character tokens.
    // ( ) { } , . - + ; / *
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    // ! != = == < <= > >=
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Keywords.
    AND, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, TRUE, VAR, WHILE,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // End of file //
    EOF, COMMENT
}
