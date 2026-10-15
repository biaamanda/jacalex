package compiler.parser;

import compiler.scanner.Scanner;
import compiler.scanner.Tag;
import compiler.scanner.Token;

/**
 * Analisador sintático descendente recursivo para a linguagem Jaca.
 *
 * Cada regra da gramática vira um método. Como a gramática é LL(1), basta
 * olhar o token atual (lookahead de 1) para decidir qual caminho seguir.
 */
public class Parser {
    // referência a um scanner criado separadamente pelo JFlex
    private final Scanner scanner;
    // token atualmente obtido pelo Scanner (o lookahead)
    private Token token;

    public Parser(Scanner scanner) throws Exception {
        // se não tem um scanner, então não é possível continuar
        if (scanner == null) {
            throw new Exception("Scanner não definido! Não foi possível criar o Parser!");
        }
        this.scanner = scanner;
        // obtém o primeiro token
        token = scanner.yylex();
    }

    // linha e coluna do scanner começam em 0; para o usuário, começam em 1
    private String position() {
        return "Linha: " + (token.line + 1) + " e Coluna: " + (token.column + 1);
    }

    // erro quando só uma marca era aceitável
    private void error(Tag tag) throws Exception {
        throw new Exception("Erro durante a análise sintática: esperava marca com tag " + tag
                + " mas recebi marca com tag " + token.tag + "\n" + position());
    }

    // erro quando várias marcas eram aceitáveis
    private void error(Tag[] tags) throws Exception {
        StringBuilder msg = new StringBuilder("Erro durante a análise sintática: esperava marcas com tags [ ");
        for (Tag t : tags) {
            msg.append(t).append(' ');
        }
        msg.append("] mas recebi marca com tag ").append(token.tag).append('\n').append(position());
        throw new Exception(msg.toString());
    }

    // verifica a marca atual e avança na entrada
    private void accept(Tag tag) throws Exception {
        if (token.tag == tag) {
            token = scanner.yylex();
        } else {
            error(tag);
        }
    }

    // FIRST(statement): marcas com as quais um comando pode começar
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

    // inicia a análise sintática
    public void parse() throws Exception {
        // REGRA: program = statement_list, EOF;
        statementList();
        // depois de statement_list só pode restar o fim da entrada
        if (token.tag == Tag.EOF) {
            System.out.println("Análise sintática terminada com sucesso!");
        } else {
            error(Tag.EOF);
        }
    }

    // REGRA: statement_list = statement, { statement };
    private void statementList() throws Exception {
        statement();
        while (startsStatement()) {
            statement();
        }
    }

    // REGRA: statement = assign_stmt | if_stmt | while_stmt | block_stmt | null_stmt;
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

    // REGRA: assign_stmt = ID, "=", expression, ";";
    private void assignStmt() throws Exception {
        accept(Tag.ID);
        accept(Tag.ASSIGN);
        expression();
        accept(Tag.SEMICOLON);
    }

    // REGRA: if_stmt = "if", "(", expression, ")", "then", block_stmt, [ "else", block_stmt ];
    private void ifStmt() throws Exception {
        accept(Tag.IF);
        accept(Tag.LEFT_PAR);
        expression();
        accept(Tag.RIGHT_PAR);
        accept(Tag.THEN);
        blockStmt();
        // parte opcional: só entra se o próximo token for "else"
        if (token.tag == Tag.ELSE) {
            accept(Tag.ELSE);
            blockStmt();
        }
    }

    // REGRA: while_stmt = "while", "(", expression, ")", block_stmt;
    private void whileStmt() throws Exception {
        accept(Tag.WHILE);
        accept(Tag.LEFT_PAR);
        expression();
        accept(Tag.RIGHT_PAR);
        blockStmt();
    }

    // REGRA: block_stmt = "{", statement_list, "}";
    private void blockStmt() throws Exception {
        accept(Tag.LEFT_BRACE);
        statementList();
        accept(Tag.RIGHT_BRACE);
    }

    // REGRA: null_stmt = ";";
    private void nullStmt() throws Exception {
        accept(Tag.SEMICOLON);
    }

    // REGRA: expression = simple_expr, [ REL_OPERATOR, simple_expr ];
    private void expression() throws Exception {
        simpleExpr();
        if (token.tag == Tag.REL_OPERATOR) {
            accept(Tag.REL_OPERATOR);
            simpleExpr();
        }
    }

    // REGRA: simple_expr = term, { ADD_OPERATOR, term };
    private void simpleExpr() throws Exception {
        term();
        while (token.tag == Tag.ADD_OPERATOR) {
            accept(Tag.ADD_OPERATOR);
            term();
        }
    }

    // REGRA: term = factor, { MUL_OPERATOR, factor };
    private void term() throws Exception {
        factor();
        while (token.tag == Tag.MUL_OPERATOR) {
            accept(Tag.MUL_OPERATOR);
            factor();
        }
    }

    // REGRA: factor = ID | NUMBER | "(", expression, ")";
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
