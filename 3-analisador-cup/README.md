# 3. Analisador sintático de Jaca com CUP e JFlex

Mesmo objetivo da pasta 2 (conferir se um programa Jaca está sintaticamente correto), mas
o parser **é gerado** pelo **CUP** (LALR(1), ascendente) a partir da gramática, em vez de
escrito à mão. O scanner é gerado pelo **JFlex** e entrega `Symbol`s ao CUP.

## Arquivos

| Arquivo | Função |
| --- | --- |
| `src/main/jflex/scanner.jflex` | Scanner: expressões regulares dos terminais; devolve `Symbol` (`sym.ID`, `sym.IF`, ...). |
| `src/main/cup/parser.cup` | Gramática de Jaca e mensagem de erro sintático. O CUP gera `Parser.java` e `sym.java`. |
| `src/main/java/parser/Main.java` | Único `.java` escrito à mão: cria `Scanner` e `Parser` e chama `parse()`. |
| `pom.xml` | Maven: chama o JFlex, o CUP, compila e empacota um jar com as dependências. |
| `build.ps1` | Alternativa **sem Maven** (Windows): faz os mesmos passos e gera `build/jacacup.jar`. |
| `teste.input` | Programa válido com todos os comandos e operadores. |
| `tests/invalid-*.input` | Programas inválidos, um defeito em cada. |

`Scanner.java`, `Parser.java` e `sym.java` são **gerados** em `target/generated-sources`
e não são editados à mão.

## Diferenças para o recursivo descendente (pasta 2)

| | Recursivo descendente | CUP (LALR) |
| --- | --- | --- |
| Direção | de cima para baixo | de baixo para cima |
| Quem escreve o parser | você | o CUP, a partir do `parser.cup` |
| Repetição `{ }` | `while` no código | recursão à esquerda: `statement_list ::= statement_list statement \| statement` |
| Opcional `[ ]` | `if (token == ELSE)` | duas produções: com e sem `else` |
| Recursão à esquerda | proibida | permitida |
| Decisão | olha 1 token à frente | tabela de estados (46 estados) |

A gramática foi escrita em camadas (`expression`, `simple_expr`, `term`, `factor`); por isso
a precedência dos operadores já está na gramática e não é preciso usar `precedence`. O CUP
reporta **0 conflitos**.

## Números reais

A regra está no **scanner** (`NUMBER = [0-9]+(\.[0-9]+)?([Ee][+-]?[0-9]+)?`). Para o CUP,
`NUMBER` é só um terminal; ele não sabe se o valor é inteiro ou real.

## Como executar

```bash
mvn clean package
java -jar target/jacacup-1.0-jar-with-dependencies.jar teste.input
```

Resultado esperado:

```text
Análise léxica terminada com sucesso!
Análise sintática terminada com sucesso!
```

Sem Maven (Windows):

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -jar build\jacacup.jar teste.input
```

## Testes inválidos

```bash
java -jar target/jacacup-1.0-jar-with-dependencies.jar tests/invalid-then.input
```

| Arquivo | Resultado esperado |
| --- | --- |
| `invalid-then.input` | marca inesperada `"{"` (linha 1, coluna 12) |
| `invalid-semicolon.input` | marca inesperada fim da entrada (linha 2, coluna 1) |
| `invalid-paren.input` | marca inesperada `";"` (linha 1, coluna 11) |
| `invalid-block.input` | marca inesperada `"x"` (linha 1, coluna 15) |
| `invalid-else.input` | marca inesperada `"x"` (linha 1, coluna 24) |
| `invalid-brace.input` | marca inesperada fim da entrada (linha 2, coluna 1) |
| `invalid-empty.input` | marca inesperada fim da entrada (linha 1, coluna 1) |
| `invalid-symbol.input` | erro léxico: `símbolo ilegal <@> na linha 1, coluna 8` |

## Observação sobre o VS Code

A extensão Java compila os `.java` por conta própria e não enxerga `Parser` e `Scanner`
(gerados pelo build), o que pode gravar um `Main.class` quebrado ("Unresolved compilation
problems") em `target/classes`. Se acontecer, desligue `java.autobuild.enabled` nas
configurações do workspace e rode `mvn clean package` de novo.
