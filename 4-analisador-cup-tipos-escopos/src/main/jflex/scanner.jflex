package interpreter;

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

ID = [A-Za-z_][A-Za-z_0-9]*
NUMBER = [0-9]+(\.[0-9]+)?([Ee][+-]?[0-9]+)?
STRING = \"[^\"]+\"
SPACE = [ \t\f\r\n]+

%%

{SPACE}         { }

"print"         { return symbol(sym.PRINT, yytext()); }
"sin"           { return symbol(sym.SIN, yytext()); }
"cos"           { return symbol(sym.COS, yytext()); }
"PI"            { return symbol(sym.PI, yytext()); }

{ID}            { return symbol(sym.ID, yytext()); }
{NUMBER}        { return symbol(sym.NUMBER, Double.valueOf(yytext())); }
{STRING}        { return symbol(sym.STRING, yytext().substring(1, yylength() - 1)); }

"**"            { return symbol(sym.POWER, yytext()); }
"+"             { return symbol(sym.PLUS, yytext()); }
"-"             { return symbol(sym.MINUS, yytext()); }
"*"             { return symbol(sym.TIMES, yytext()); }
"/"             { return symbol(sym.DIVIDE, yytext()); }
"%"             { return symbol(sym.MOD, yytext()); }
"="             { return symbol(sym.ASSIGN, yytext()); }
"("             { return symbol(sym.LPAREN, yytext()); }
")"             { return symbol(sym.RPAREN, yytext()); }
"{"             { return symbol(sym.LBRACE, yytext()); }
"}"             { return symbol(sym.RBRACE, yytext()); }
";"             { return symbol(sym.SEMI, yytext()); }

[^]             { throw new Exception("Scanner: símbolo ilegal <" + yytext()
                      + "> na linha " + (yyline + 1) + ", coluna " + (yycolumn + 1)); }
