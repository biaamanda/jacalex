# 4. Interpretador com CUP: tipos e escopos

Interpretador de uma pequena linguagem de comandos (atribuição, `print`, expressões
aritméticas) feito com **JFlex + CUP**, com ações semânticas executadas durante a análise.
Implementa os requisitos R1 a R5 do projeto "Analisador CUP com Tipos e Escopos":

- dois tipos de dados: **número real** (`Double`) e **cadeia de caracteres** (`String`);
- operações que verificam e convertem tipos;
- **blocos** `{ }` que criam **escopos**, controlados por uma pilha de tabelas de símbolos.

> O projeto anterior ("Analisador CUP com Ações Semânticas") não estava disponível, então
> esta pasta traz o interpretador completo: variáveis, `print`, `sin`, `cos`, `PI` e `**`
> também foram implementados aqui.

## Gramática

```text
program            ::= command_list
command_list       ::= command_list command_part | command_part
command_part       ::= command ';' | command_block
command_block      ::= '{' command_list '}'
command            ::= assignment_command | print_command
assignment_command ::= id '=' expr
print_command      ::= 'print' '(' expr ')'
expr ::= expr '+' expr | expr '-' expr | expr '*' expr | expr '/' expr
       | expr '%' expr | expr '**' expr | '-' expr | '(' expr ')'
       | number | string | id | 'sin' '(' expr ')' | 'cos' '(' expr ')' | 'PI'
```

Precedência (da menor para a maior): `+ -`, `* / %`, `-` unário, `**` (associativo à direita).
Assim `-2**2` é `-(2**2)`. O CUP reporta **0 conflitos**.

## Arquivos

| Arquivo | Função | Requisito |
| --- | --- | --- |
| `src/main/jflex/scanner.jflex` | Tokens: `number` (devolve `Double`), `string` (devolve o texto **sem as aspas**), `{`, `}`, `**`, palavras reservadas. | R1, R4 |
| `src/main/java/parser/Type.java` | Enumeração `DBL` e `STR`. | R2 |
| `src/main/java/parser/Value.java` | Valor com tipo: campo `type` e campo `value` (`Object`), com construtores para `Double` e `String`. | R2 |
| `src/main/java/parser/Operations.java` | Funções estáticas das operações: verificam os tipos, convertem e devolvem um `Value`. | R3 |
| `src/main/cup/parser.cup` | Gramática e ações semânticas; a pilha de escopos fica na seção `action code`. | R2, R4, R5 |
| `src/main/java/parser/Main.java` | Abre o arquivo (UTF-8), cria `Scanner` e `Parser` e chama `parse()`. | |

## Como cada requisito foi feito

**R1/R2: tipos.** As expressões do CUP são do tipo `Value`. Um número vira
`new Value(Double)` e uma cadeia vira `new Value(String)`.

**R3: operações** (`Operations.java`):

| Operação | Regra |
| --- | --- |
| `+` | número + número = número; se algum lado for cadeia, concatena (o número é convertido em texto) |
| `- * / % **`, `-` unário, `sin`, `cos` | só com números; senão: `Tipos incompatíveis para ...` |
| atribuição | sem mudança: guarda o `Value` como está |
| `print(e)` | imprime `>> ` seguido do valor |

**R4/R5: blocos e escopos.** A tabela de símbolos é
`Stack<HashMap<String, Value>>` (em `action code` do `parser.cup`):

- a pilha começa com **uma** tabela, o escopo global, que dura todo o programa;
- `{` faz `push` de uma tabela nova (ação no meio da regra, logo após o `{`);
- `}` faz `pop`, descartando as variáveis daquele bloco;
- **ler** uma variável procura do topo para a base (`ListIterator`, sem desempilhar); se
  nenhuma tabela tiver o nome, erro: `Variável não definida: x`;
- **atribuir** grava sempre na tabela do **topo** (`peek`). Se o nome já existia num escopo
  externo, a variável é *redefinida* no bloco (sombreamento) e o valor externo fica
  intacto quando o bloco termina.

## Como executar

```bash
mvn clean package
java -jar target/tiposescopos-1.0-jar-with-dependencies.jar teste.input
```

## Testes válidos

| Arquivo | Saída esperada |
| --- | --- |
| `tests/strings.input` | `>> Olá mundo`, `>> Olá  Marco`, `>> O valor de 4.0 ao quadrado é 16.0` |
| `tests/operacoes.input` | `>> 3.0 + 2.0 = 5.0`, ..., `>> Calculando: 1.0` |
| `tests/escopos.input` | `>> Teste de blocos`, `>> 2.0`, `>> 3.0` |
| `tests/escopos-sombra.input` | `>> 3.0` (dentro do bloco) e `>> 2.0` (depois do bloco) |
| `teste.input` | os três primeiros em sequência |

## Testes inválidos

| Arquivo | Defeito | Erro |
| --- | --- | --- |
| `invalid-undefined.input` | `print(a);` sem `a` | `Variável não definida: a` |
| `invalid-type-subtract.input` | `"abc" - 1` | `Tipos incompatíveis para subtração: STR e DBL` |
| `invalid-type-negate.input` | `-"abc"` | `Tipo incompatível para negação: STR` |
| `invalid-scope-exit.input` | usa `b` depois do bloco que a criou | `Variável não definida: b` |
| `invalid-syntax-semicolon.input` | falta `;` | marca inesperada fim da entrada |
| `invalid-syntax-brace.input` | bloco não fechado | marca inesperada fim da entrada |
| `invalid-symbol.input` | `@` | `símbolo ilegal <@> na linha 1, coluna 7` |
| `invalid-string.input` | aspas sem fechar | `símbolo ilegal <"> na linha 1, coluna 5` |

## Observações e decisões

- O `print` usa o prefixo `>> `, como na saída de exemplo do enunciado.
- Números são impressos como `Double` do Java (`3.0`, não `3`).
- A `string` segue a definição do enunciado: **um ou mais** caracteres entre aspas, então
  `""` (vazia) não é aceita. Não há sequências de escape.
- Um bloco precisa ter ao menos um comando (`{ }` vazio é erro de sintaxe), conforme a gramática.
- O exemplo do enunciado usa `printf(...)`; o comando da gramática é `print`.
