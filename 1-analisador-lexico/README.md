# 1. Analisador léxico de Jaca (Parte I)

Lê um programa Jaca e mostra a classificação de cada terminal (token). Espaços, tabulações
e quebras de linha são ignorados. **Não verifica a estrutura dos comandos**: isso é papel
do analisador sintático (pastas 2 e 3).

## Arquivos

| Arquivo | Função |
| --- | --- |
| `src/main/jflex/scanner.jflex` | Regras léxicas (expressões regulares e ações). O Maven usa o JFlex para gerar o `Scanner.java`. |
| `src/main/java/compiler/scanner/Tag.java` | Constantes das marcas (`ID`, `NUMBER`, `IF`, ...). |
| `src/main/java/compiler/scanner/Token.java` | Token básico: marca, linha e coluna. |
| `IdentifierToken`, `NumberToken`, `OperatorToken` | Herdam de `Token` e guardam também o texto reconhecido (lexema). |
| `src/main/java/compiler/Main.java` | Lê o arquivo e imprime os tokens até o fim da entrada. |
| `tests/` | Programas de teste. |
| `ECM253-Atividade-...-Parte-I.md` | Enunciado da atividade. |

## Terminais reconhecidos

| Terminal | Tag |
| --- | --- |
| Identificador, como `x`, `_valor1`, `ifx` | `ID` |
| Número, como `10`, `12.5`, `1.2e-3` | `NUMBER` |
| `if`, `then`, `else`, `while` | `IF`, `THEN`, `ELSE`, `WHILE` |
| `=` | `ASSIGN` |
| `+`, `-` | `ADD_OPERATOR` |
| `*`, `/`, `%` | `MUL_OPERATOR` |
| `==`, `!=`, `<`, `>`, `<=`, `>=` | `REL_OPERATOR` |
| `(`, `)` | `LEFT_PAR`, `RIGHT_PAR` |
| `{`, `}` | `LEFT_BRACE`, `RIGHT_BRACE` |
| `;` | `SEMICOLON` |
| fim da entrada | `EOF` |

As palavras reservadas vêm antes de `ID` no `.jflex`, por isso `if` é `IF`, mas `ifx` é `ID`.

## Como executar

```bash
mvn clean package
java -jar target/jacalex.jar tests/valid-test.input
```

Saída para `x = 10 + 2;`:

```text
Token: <ID, x>
Token: <ASSIGN>
Token: <NUMBER, 10>
Token: <ADD_OPERATOR, +>
Token: <NUMBER, 2>
Token: <SEMICOLON>
Análise léxica terminada com sucesso!
```

## Testes

| Arquivo | O que testa | Resultado |
| --- | --- | --- |
| `tests/valid-test.input` | todos os terminais válidos, inclusive os 6 relacionais e números inteiros, decimais e científicos | lista de tokens e "terminada com sucesso" |
| `tests/invalid-test.input` | símbolo `@` | `símbolo ilegal <@> na linha 1, coluna 8` |
| `tests/original-test.input` | texto da gramática (não é Jaca) | erro léxico no primeiro símbolo ilegal |

Também são rejeitados `!`, `&`, `\`, `.5` e `1.`. Já `-2` gera o operador `-` seguido de `2`.
