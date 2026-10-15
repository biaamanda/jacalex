package parser;

import java_cup.runtime.*;

%%

%class Scanner
%cup
%unicode
%line
%column
%yylexthrow Exception
%eofval{
    return symbol(sym.EOF);
%eofval}

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

%eof{
    System.out.println("Análise léxica terminada com sucesso!");
%eof}

ID = [A-Za-z_][A-Za-z_0-9]*
NUMBER = [0-9]+(\.[0-9]+)?([Ee][+-]?[0-9]+)?
ADD_OPERATOR = "+"|"-"
MUL_OPERATOR = "*"|"/"|"%"
REL_OPERATOR = "=="|"!="|"<"|">"|"<="|">="
SPACE = [ \t\f\r\n]+

%%

{SPACE}         { }

"if"            { return symbol(sym.IF, yytext()); }
"then"          { return symbol(sym.THEN, yytext()); }
"else"          { return symbol(sym.ELSE, yytext()); }
"while"         { return symbol(sym.WHILE, yytext()); }

{ID}            { return symbol(sym.ID, yytext()); }
{NUMBER}        { return symbol(sym.NUMBER, yytext()); }
{ADD_OPERATOR}  { return symbol(sym.ADD_OPERATOR, yytext()); }
{MUL_OPERATOR}  { return symbol(sym.MUL_OPERATOR, yytext()); }
{REL_OPERATOR}  { return symbol(sym.REL_OPERATOR, yytext()); }

"="             { return symbol(sym.ASSIGN, yytext()); }
"("             { return symbol(sym.LPAREN, yytext()); }
")"             { return symbol(sym.RPAREN, yytext()); }
"{"             { return symbol(sym.LBRACE, yytext()); }
"}"             { return symbol(sym.RBRACE, yytext()); }
";"             { return symbol(sym.SEMI, yytext()); }

[^]             { throw new Exception("Scanner: símbolo ilegal <" + yytext()
                      + "> na linha " + (yyline + 1) + ", coluna " + (yycolumn + 1)); }
