# Jaca: analisadores léxico e sintáticos

Projeto da disciplina ECM253 (Linguagens Formais, Autômatos e Compiladores).
**Jaca** (*Just Another Compiler Attempt*) é uma linguagem simples com atribuição,
`if/then/else`, `while`, blocos `{ }`, comando vazio `;` e expressões aritméticas
e relacionais.

Cada pasta é um projeto Maven independente, com o seu próprio `README.md`.

| Pasta | Analisador | Técnica | Saída para um programa correto |
| --- | --- | --- | --- |
| [`1-analisador-lexico`](1-analisador-lexico) | Léxico (Parte I) | JFlex | lista os tokens reconhecidos |
| [`2-analisador-recursivo-descendente`](2-analisador-recursivo-descendente) | Sintático (Parte II) | descendente recursivo, escrito à mão | "Análise sintática terminada com sucesso!" |
| [`3-analisador-cup`](3-analisador-cup) | Sintático (CUP) | LALR(1), gerado pelo CUP | "Análise sintática terminada com sucesso!" |
| [`4-analisador-cup-tipos-escopos`](4-analisador-cup-tipos-escopos) | Interpretador (CUP) | AST + Visitor, tipos e escopos | executa o programa e imprime os `print` |
| [`5-compiladorJACA-Marie/jaca`](5-compiladorJACA-Marie/jaca) | Compilador (CUP) | AST + Visitor que gera código | assembly do MARIE |

## Como as partes se relacionam

```text
texto do programa ──▶ analisador léxico ──▶ tokens ──▶ analisador sintático ──▶ correto / erro
                      (Parte I, JFlex)                  (Parte II ou CUP)
```

- A **Parte I** só quebra o texto em tokens (`ID`, `NUMBER`, `IF`, ...).
- A **Parte II** reaproveita esse scanner e acrescenta o `Parser`, que confere se os
  tokens formam um programa válido pela gramática.
- A pasta **CUP** faz o mesmo trabalho da Parte II, mas o parser é gerado
  automaticamente a partir da gramática, em vez de escrito à mão.
- A pasta **4** usa outra linguagem (calculadora com `print`, variáveis, cadeias e blocos) e vai
  além de validar: executa o programa, com verificação de tipos e escopos de variáveis.
- A pasta **5** volta a Jaca: em vez de interpretar o programa, o `Compiler` (um Visitor sobre a
  AST) o **traduz para assembly do MARIE**, um processador didático de 16 bits.

A Parte I também está preservada na tag `parte-1` do repositório.

## Requisitos

- JDK 17 ou superior
- Apache Maven 3.9 ou superior (`mvn -version`)

## Como executar qualquer parte

Entre na pasta da parte desejada, compile e execute (os comandos exatos estão no README
de cada pasta):

```bash
cd 2-analisador-recursivo-descendente
mvn clean package
java -jar target/jacalex.jar tests/syntax-valid.input
```

## A linguagem Jaca

```text
program        = statement_list, EOF;
statement_list = statement, {statement};
statement      = assign_stmt | if_stmt | while_stmt | block_stmt | null_stmt;
assign_stmt    = ID, "=", expression, ";";
if_stmt        = "if", "(", expression, ")", "then", block_stmt, ["else", block_stmt];
while_stmt     = "while", "(", expression, ")", block_stmt;
block_stmt     = "{", statement_list, "}";
null_stmt      = ";";
expression     = simple_expr, [REL_OPERATOR, simple_expr];
simple_expr    = term, {ADD_OPERATOR, term};
term           = factor, {MUL_OPERATOR, factor};
factor         = ID | NUMBER | "(", expression, ")";
```

Terminais: `ID = [A-Za-z_][A-Za-z_0-9]*`, `NUMBER = [0-9]+(\.[0-9]+)?([Ee][+-]?[0-9]+)?`,
`ADD_OPERATOR = [+-]`, `MUL_OPERATOR = [*/%]`, `REL_OPERATOR = (==|!=|<|>|<=|>=)`.
