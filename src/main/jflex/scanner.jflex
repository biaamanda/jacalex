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


// Precisa definir macros de expressões regulares
ws = \s+
%%
// Precisa completar as regras léxicas


{ws}		{ /* ignorar */ }
.		        { throw new Exception("Scanner: símbolo ilegal <" +
                     yytext() + "(" + (int)(yytext().charAt(0)) + ")" + ">"); }
