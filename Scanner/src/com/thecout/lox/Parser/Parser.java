package com.thecout.lox.Parser;


import com.thecout.lox.Parser.Expr.*;
import com.thecout.lox.Parser.Stmts.*;
import com.thecout.lox.Token;
import com.thecout.lox.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.thecout.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            }catch (ParserError e){
                e.printStackTrace();
                break;
            }
        }

        return statements;
    }

    private Expr expression() throws ParserError {
        if (check(SEMICOLON)){
            return null;
        }
        return assignment();
    }

    private Stmt declaration() throws ParserError {
        if (match(FUN)) return function();
        if (match(VAR)) return varDeclaration();

        return statement();
    }

    private Stmt statement() throws ParserError {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() throws ParserError {
        consume(LEFT_PAREN, "left paren expected");
        Expr expr = null;
        int idx = current;

        try{
            consume(VAR, "var expected");
            varDeclaration();
            idx = current;
        }catch (Exception e){
            current = idx;
            try{
                expressionStatement();
                idx = current;
            }catch (Exception e2){
                throw error(peek(), "Syntax error");
            };
        };


        expr = expression();
        idx = current;

        consume(SEMICOLON, "semicolon expected");

        expression();

        consume(RIGHT_PAREN, "right paren expected");

        Stmt body = statement();

        return new While(expr, body);
    }

    private Stmt ifStatement() throws ParserError {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() throws ParserError {
        Expr expr = expression();
        consume(SEMICOLON, "semicolon expected");
        return new Print(expr);
    }

    private Stmt returnStatement() throws ParserError {
        Expr expr = expression();
        consume(SEMICOLON, "semicolon expected");
        return new Return(expr);
    }

    private Stmt varDeclaration() throws ParserError {
        Token name = consume(IDENTIFIER, "identifier expected");
        Expr expr = null;
        if (match(EQUAL))
            expr = expression();
        consume(SEMICOLON, "semicolon expected");
        return  new Var(name, expr);
    }

    private Stmt whileStatement() throws ParserError {
        consume(LEFT_PAREN, "left paren expected");

        Expr condition = expression();

        consume(RIGHT_PAREN, "right paren expected");

        Stmt body = statement();

        return new While(condition, body);
    }

    private Stmt expressionStatement() throws ParserError {
        Expr expr = expression();
        consume(SEMICOLON, "semicolon expected");

        return new Expression(expr);
    }

    private Function function() throws ParserError {
        Token name = consume(IDENTIFIER, "Identifier Expected");
        consume(LEFT_PAREN, "left paren expected");

        List<Token> parameters = new ArrayList<>();

        while (peek().type == IDENTIFIER){
            parameters.add(advance());
            if (check(COMMA)) {
                advance();
            }else {
                break;
            }
        }
        consume(RIGHT_PAREN, "right paren expected");
        consume(LEFT_BRACE, "left paren expected");

        List<Stmt> block = block();
        return  new Function(name, parameters, block);
    }


    private List<Stmt> block() throws ParserError {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(RIGHT_BRACE)){
            stmts.add(statement());
        }
        consume(RIGHT_BRACE, "right brace expected block()");

        return stmts;
    }

    private Expr assignment() throws ParserError {
        Expr expr = null;
        Token name = null;
        int idx = current;

        try {
            name = consume(IDENTIFIER, "identifier expected 187");
            consume(EQUAL, "equal expected");
            expr = assignment();
        }catch (Exception e){
            name = null;
            current = idx;
            expr = or();
        };
        return new Assign(name, expr);
    }

    private Expr or() throws ParserError {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() throws ParserError {
        Expr expr = equality();

        while (match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() throws ParserError {
        Expr expr = comparison();

        while (match(BANG_EQUAL) || match(EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() throws ParserError {
        Expr expr = addition();

        while (match(GREATER) || match(GREATER_EQUAL) || match(LESS) || match(LESS_EQUAL)){
            Token operator = previous();
            Expr right = addition();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() throws ParserError {
        Expr expr = multiplication();

        while (match(MINUS) || match(PLUS)){
            Token operator = previous();
            Expr right = multiplication();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() throws ParserError {
        Expr expr = unary();

        while (match(SLASH) || match(STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() throws ParserError {
        Token operator = null;
        Expr expr = null;

        int idx = current;
        try {
            if(check(BANG)) operator = advance();
            if(check(MINUS)) operator = advance();
            if(operator == null){
                throw error(peek(), "minus or ! expected");
            };
            expr = unary();
        }catch (Exception e){
            current = idx;
            expr = call();
        }

        return new Unary(operator, expr);
    }

    private Expr call() throws ParserError {
        Expr primary = primary();
        List<Expr> args = new ArrayList<>();

        if (!match(LEFT_PAREN)) {
            return new Call(primary, args);
        }

        if(match(RIGHT_PAREN)) {
            return new Call(primary, args);
        }

        while (true){
            args.add(expression());
            if (check(COMMA)) {
                advance();
            }else {
                break;
            }
        }
        consume(RIGHT_PAREN, "right paren expected");

        return new Call(primary, args);
    }

    private Expr primary() throws ParserError {
        if (check(TRUE)) return new Literal(advance().literal);
        if (check(FALSE)) return new Literal(advance().literal);
        if (check(NIL)) return new Literal(advance().literal);
        if (check(NUMBER)) return new Literal(advance().literal);
        if (check(STRING)) return new Literal(advance().literal);
        if (check(IDENTIFIER)) return new Variable(advance());
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')'");
            return expr;
        }
        throw error(peek(), "Syntax Error");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) throws ParserError {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().type == tokenType;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParserError error(Token token, String message) {
        //ParserError.error(token, message);
        return new ParserError(token, message);
    }


}
