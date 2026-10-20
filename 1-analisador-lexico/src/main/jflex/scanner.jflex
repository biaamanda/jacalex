/**
 * Analisador léxico para a linguagem Jaca
 */
package compiler.scanner;

%%

%public
%class Scanner
%unicode
%type Token

%yylexthrow Exception

%eofval{
    return new Token(Tag.EOF, yyline, yycolumn);
%eofval}

%line
%column

%eof{
    System.out.println("Análise léxica terminada com sucesso!");
%eof}

// Padrões dos terminais definidos na gramática de Jaca.
ID = [A-Za-z_][A-Za-z_0-9]*
NUMBER = [0-9]+(\.[0-9]+)?([Ee][+-]?[0-9]+)?
ADD_OPERATOR = "+"|"-"
MUL_OPERATOR = "*"|"/"|"%"
REL_OPERATOR = "=="|"!="|"<"|">"|"<="|">="
SPACE = [ \t\f\r\n]+

%%

{SPACE}         { /* Ignorar espaços em branco. */ }

// Palavras reservadas vêm antes de ID.
"if"            { return new Token(Tag.IF, yyline, yycolumn); }
"then"          { return new Token(Tag.THEN, yyline, yycolumn); }
"else"          { return new Token(Tag.ELSE, yyline, yycolumn); }
"while"         { return new Token(Tag.WHILE, yyline, yycolumn); }

{ID}            { return new IdentifierToken(yytext(), yyline, yycolumn); }
{NUMBER}        { return new NumberToken(yytext(), yyline, yycolumn); }
{ADD_OPERATOR}  { return new OperatorToken(Tag.ADD_OPERATOR, yytext(), yyline, yycolumn); }
{MUL_OPERATOR}  { return new OperatorToken(Tag.MUL_OPERATOR, yytext(), yyline, yycolumn); }
{REL_OPERATOR}  { return new OperatorToken(Tag.REL_OPERATOR, yytext(), yyline, yycolumn); }

"="             { return new Token(Tag.ASSIGN, yyline, yycolumn); }
"("             { return new Token(Tag.LEFT_PAR, yyline, yycolumn); }
")"             { return new Token(Tag.RIGHT_PAR, yyline, yycolumn); }
"{"             { return new Token(Tag.LEFT_BRACE, yyline, yycolumn); }
"}"             { return new Token(Tag.RIGHT_BRACE, yyline, yycolumn); }
";"             { return new Token(Tag.SEMICOLON, yyline, yycolumn); }

[^]             { throw new Exception("Scanner: símbolo ilegal <" + yytext()
                      + "> na linha " + (yyline + 1) + ", coluna " + (yycolumn + 1)); }
