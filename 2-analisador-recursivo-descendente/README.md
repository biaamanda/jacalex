# 2. Analisador sintático descendente recursivo de Jaca (Parte II)

Confere se um programa Jaca está sintaticamente correto. Reaproveita o scanner da Parte I
(JFlex) e acrescenta a classe `Parser`, **escrita à mão**. Não interpreta nada: só informa se
o programa é válido.

## Arquivos

| Arquivo | Função |
| --- | --- |
| `src/main/java/compiler/parser/Parser.java` | O analisador sintático. Cada regra da gramática é um método. |
| `src/main/java/compiler/Main.java` | Cria o `Scanner`, o `Parser` e chama `parse()`. |
| `src/main/jflex/scanner.jflex` e `src/main/java/compiler/scanner/` | Scanner da Parte I, sem alterações. |
| `tests/` | Testes léxicos (`valid-test`, `invalid-test`) e sintáticos (`syntax-*`). |

## Como o Parser funciona

Há uma variável `token` com o token atual (lookahead de 1). O método `accept(tag)` confere se
o token atual é o esperado e avança; senão, gera erro. Cada regra da gramática vira um método:

| Regra | Método | Como trata `{ }` e `[ ]` |
| --- | --- | --- |
| `statement_list = statement, {statement}` | `statementList()` | `while` enquanto o token puder iniciar um comando |
| `statement` | `statement()` | `switch` no token atual: `ID`, `IF`, `WHILE`, `{` ou `;` |
| `if_stmt` | `ifStmt()` | `if (token == ELSE)` para a parte opcional |
| `expression = simple_expr, [REL, simple_expr]` | `expression()` | `if` para a parte opcional |
| `simple_expr`, `term` | `simpleExpr()`, `term()` | `while` para `ADD_OPERATOR` e `MUL_OPERATOR` |
| `factor` | `factor()` | `switch`: `ID`, `NUMBER` ou `(` expressão `)` |

A precedência dos operadores vem da própria gramática em camadas: `*` `/` `%` ficam em
`term`, abaixo de `+` `-` em `simple_expr`, que fica abaixo do relacional em `expression`.

## Como executar

```bash
mvn clean package
java -jar target/jacalex.jar tests/syntax-valid.input
```

Resultado esperado:

```text
Análise léxica terminada com sucesso!
Análise sintática terminada com sucesso!
```

## Testes sintáticos

| Arquivo | Defeito | Erro esperado |
| --- | --- | --- |
| `syntax-valid.input` | nenhum | sucesso |
| `syntax-invalid-semicolon.input` | falta `;` | esperava `SEMICOLON`, recebeu `EOF` (linha 2, coluna 1) |
| `syntax-invalid-then.input` | falta `then` | esperava `THEN`, recebeu `LEFT_BRACE` (linha 1, coluna 12) |
| `syntax-invalid-paren.input` | parêntese não fechado | esperava `RIGHT_PAR`, recebeu `SEMICOLON` (linha 1, coluna 11) |
| `syntax-invalid-block.input` | `while` sem chaves | esperava `LEFT_BRACE`, recebeu `ID` (linha 1, coluna 15) |
| `syntax-invalid-else.input` | `else` sem chaves | esperava `LEFT_BRACE`, recebeu `ID` (linha 1, coluna 24) |
| `syntax-invalid-brace.input` | bloco não fechado | esperava `RIGHT_BRACE`, recebeu `EOF` |
| `syntax-invalid-empty.input` | arquivo vazio | esperava o início de um comando, recebeu `EOF` |

O parser **para no primeiro erro** e informa a marca esperada, a recebida, a linha e a coluna.
