package com.thecout.lox;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.thecout.lox.TokenType.EOF;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private boolean isComment = false;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanLine(String line, int lineNumber) {
        List<Token> returnToken = new ArrayList<>();

        String literal = "";
        Token token;
        Token newToken = null;
        char[] chars = line.toCharArray();

        for (int i = 0; i < chars.length; i++) {

            literal += chars[i];
            token = newToken;
            newToken = getToken(literal, lineNumber);

            if(token != null && token.type == TokenType.NUMBER && newToken == null && chars[i] == '.'){
                literal += chars[++i];
                newToken = getToken(literal, lineNumber);
            }

            if(newToken == null && token != null){
                returnToken.add(token);
                literal = "";

                if(chars[i] != ' ')
                    i--;
            }

            if(i == chars.length - 1 && newToken != null){
                returnToken.add(newToken);
            }

        }

        isComment = false;

        return returnToken;
    }

    private Token getToken(String literal, int lineNum){

        Token token;

        // Comment
        if (Objects.equals(literal, "//")){
            isComment = true;
            return new Token(TokenType.COMMENT, literal, literal, lineNum);
        }

        // TokenTypes
        token = getTokenType(literal, lineNum);
        if (token != null)
            return token;

        // Literals
        if(literal.matches("\\d+([.]\\d+)?"))
            return new Token(TokenType.NUMBER, literal, Double.parseDouble(literal), lineNum);

        if(literal.matches("\".*\""))
            return new Token(TokenType.STRING, literal, literal.substring(1, literal.length()-1), lineNum);

        if(literal.matches("([a-z]|_)([a-z]|[A-Z]|_|\\d)*"))
            return new Token(TokenType.IDENTIFIER, literal, literal, lineNum);

        return null;
    }

    private Token getTokenType(String literal, int lineNum){
        return switch (literal) {
            // Single-character tokens.
            case "(" -> new Token(TokenType.LEFT_PAREN, literal, literal, lineNum);
            case ")" -> new Token(TokenType.RIGHT_PAREN, literal, literal, lineNum);
            case "{" -> new Token(TokenType.LEFT_BRACE, literal, literal, lineNum);
            case "}" -> new Token(TokenType.RIGHT_BRACE, literal, literal, lineNum);
            case "," -> new Token(TokenType.COMMA, literal, literal, lineNum);
            case "." -> new Token(TokenType.DOT, literal, literal, lineNum);
            case "-" -> new Token(TokenType.MINUS, literal, literal, lineNum);
            case "+" -> new Token(TokenType.PLUS, literal, literal, lineNum);
            case ";" -> new Token(TokenType.SEMICOLON, literal, literal, lineNum);
            case "/" -> new Token(TokenType.SLASH, literal, literal, lineNum);
            case "*" -> new Token(TokenType.STAR, literal, literal, lineNum);

            // One or two character tokens.
            case "!" -> new Token(TokenType.BANG, literal, literal, lineNum);
            case "!=" -> new Token(TokenType.BANG_EQUAL, literal, literal, lineNum);
            case "=" -> new Token(TokenType.EQUAL, literal, literal, lineNum);
            case "==" -> new Token(TokenType.EQUAL_EQUAL, literal, literal, lineNum);
            case "<" -> new Token(TokenType.GREATER, literal, literal, lineNum);
            case "<=" -> new Token(TokenType.GREATER_EQUAL, literal, literal, lineNum);
            case ">" -> new Token(TokenType.LESS, literal, literal, lineNum);
            case ">=" -> new Token(TokenType.LESS_EQUAL, literal, literal, lineNum);

            // Keywords.
            case "and" -> new Token(TokenType.AND, literal, literal, lineNum);
            case "else" -> new Token(TokenType.ELSE, literal, literal, lineNum);
            case "false" -> new Token(TokenType.FALSE, literal, literal, lineNum);
            case "fun" -> new Token(TokenType.FUN, literal, literal, lineNum);
            case "for" -> new Token(TokenType.FOR, literal, literal, lineNum);
            case "if" -> new Token(TokenType.IF, literal, literal, lineNum);
            case "nil" -> new Token(TokenType.NIL, literal, literal, lineNum);
            case "or" -> new Token(TokenType.OR, literal, literal, lineNum);
            case "print" -> new Token(TokenType.PRINT, literal, literal, lineNum);
            case "return" -> new Token(TokenType.RETURN, literal, literal, lineNum);
            case "true" -> new Token(TokenType.TRUE, literal, literal, lineNum);
            case "var" -> new Token(TokenType.VAR, literal, literal, lineNum);
            case "while" -> new Token(TokenType.WHILE, literal, literal, lineNum);

            default -> null;
        };
    }

    public List<Token> scan() {
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            tokens.addAll(scanLine(removeWhitespace(lines[i]), i));
        }
        tokens.add(new Token(EOF, "", "", lines.length));
        return tokens;
    }

    private String removeWhitespace(String line){
        return line.trim().replaceAll("\\s+", " ");
    }
}
