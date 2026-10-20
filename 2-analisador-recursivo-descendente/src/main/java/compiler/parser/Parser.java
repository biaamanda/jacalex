package compiler.parser;

import compiler.scanner.Scanner;
import compiler.scanner.Tag;
import compiler.scanner.Token;

public class Parser {
    private final Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) throws Exception {
        if (scanner == null) {
            throw new Exception("Scanner não definido! Não foi possível criar o Parser!");
        }
        this.scanner = scanner;
        token = scanner.yylex();
    }

    private String position() {
        return "Linha: " + (token.line + 1) + " e Coluna: " + (token.column + 1);
    }

    private void error(Tag tag) throws Exception {
        throw new Exception("Erro durante a análise sintática: esperava marca com tag " + tag
                + " mas recebi marca com tag " + token.tag + "\n" + position());
    }

    private void error(Tag[] tags) throws Exception {
        StringBuilder msg = new StringBuilder("Erro durante a análise sintática: esperava marcas com tags [ ");
        for (Tag t : tags) {
            msg.append(t).append(' ');
        }
        msg.append("] mas recebi marca com tag ").append(token.tag).append('\n').append(position());
        throw new Exception(msg.toString());
    }

    private void accept(Tag tag) throws Exception {
        if (token.tag == tag) {
            token = scanner.yylex();
        } else {
            error(tag);
        }
    }

    private boolean startsStatement() {
        switch (token.tag) {
            case ID:
            case IF:
            case WHILE:
            case LEFT_BRACE:
            case SEMICOLON:
                return true;
            default:
                return false;
        }
    }

    public void parse() throws Exception {
        statementList();
        if (token.tag == Tag.EOF) {
            System.out.println("Análise sintática terminada com sucesso!");
        } else {
            error(Tag.EOF);
        }
    }

    private void statementList() throws Exception {
        statement();
        while (startsStatement()) {
            statement();
        }
    }

    private void statement() throws Exception {
        switch (token.tag) {
            case ID:
                assignStmt();
                break;
            case IF:
                ifStmt();
                break;
            case WHILE:
                whileStmt();
                break;
            case LEFT_BRACE:
                blockStmt();
                break;
            case SEMICOLON:
                nullStmt();
                break;
            default:
                error(new Tag[] { Tag.ID, Tag.IF, Tag.WHILE, Tag.LEFT_BRACE, Tag.SEMICOLON });
                break;
        }
    }

    private void assignStmt() throws Exception {
        accept(Tag.ID);
        accept(Tag.ASSIGN);
        expression();
        accept(Tag.SEMICOLON);
    }

    private void ifStmt() throws Exception {
        accept(Tag.IF);
        accept(Tag.LEFT_PAR);
        expression();
        accept(Tag.RIGHT_PAR);
        accept(Tag.THEN);
        blockStmt();
        if (token.tag == Tag.ELSE) {
            accept(Tag.ELSE);
            blockStmt();
        }
    }

    private void whileStmt() throws Exception {
        accept(Tag.WHILE);
        accept(Tag.LEFT_PAR);
        expression();
        accept(Tag.RIGHT_PAR);
        blockStmt();
    }

    private void blockStmt() throws Exception {
        accept(Tag.LEFT_BRACE);
        statementList();
        accept(Tag.RIGHT_BRACE);
    }

    private void nullStmt() throws Exception {
        accept(Tag.SEMICOLON);
    }

    private void expression() throws Exception {
        simpleExpr();
        if (token.tag == Tag.REL_OPERATOR) {
            accept(Tag.REL_OPERATOR);
            simpleExpr();
        }
    }

    private void simpleExpr() throws Exception {
        term();
        while (token.tag == Tag.ADD_OPERATOR) {
            accept(Tag.ADD_OPERATOR);
            term();
        }
    }

    private void term() throws Exception {
        factor();
        while (token.tag == Tag.MUL_OPERATOR) {
            accept(Tag.MUL_OPERATOR);
            factor();
        }
    }

    private void factor() throws Exception {
        switch (token.tag) {
            case ID:
                accept(Tag.ID);
                break;
            case NUMBER:
                accept(Tag.NUMBER);
                break;
            case LEFT_PAR:
                accept(Tag.LEFT_PAR);
                expression();
                accept(Tag.RIGHT_PAR);
                break;
            default:
                error(new Tag[] { Tag.ID, Tag.NUMBER, Tag.LEFT_PAR });
                break;
        }
    }
}
