# 4. Interpretador com CUP: AST, Visitor, tipos e escopos

Interpretador de uma pequena linguagem de comandos (atribuição, `print`, expressões
aritméticas) feito com **JFlex + CUP**. O parser **não executa nada**: ele só constrói uma
**árvore sintática abstrata (AST)**, e um **Visitor** percorre a árvore para interpretá-la.
Implementa os requisitos R1 a R5 do projeto "Analisador CUP com Tipos e Escopos":

- dois tipos de dados: **número real** (`Double`) e **cadeia de caracteres** (`String`);
- operações que verificam e convertem tipos;
- **blocos** `{ }` que criam **escopos**, controlados por uma pilha de tabelas de símbolos.

> O projeto anterior ("Ações Semânticas") não estava disponível, então esta pasta traz o
> interpretador completo (variáveis, `print`, `sin`, `cos`, `PI`, `**`). A estrutura segue os
> exemplos "AST Expression Parser" e "AST Visitor".

## Como funciona

```text
texto ──▶ Scanner (JFlex) ──▶ tokens ──▶ Parser (CUP) ──▶ AST ──▶ InterpreterVisitor ──▶ saída
                                          monta os nós          percorre a árvore e executa
```

1. O **Parser** reconhece a gramática e, em cada regra, cria um nó (`new SumExpr(e1, e2)`,
   `new PrintCommand(e)`, ...). Ao reduzir `program`, a árvore inteira está pronta.
2. O **InterpreterVisitor** visita a árvore: cada nó chama `accept(visitor)`, que chama o
   `visit` específico daquele tipo de nó (padrão Visitor).
3. Como a execução só começa depois da análise completa, **um erro de sintaxe impede qualquer
   saída**; erros de tipo e de variável aparecem durante a execução.

## Estrutura

```text
src/main
├── cup/parser.cup                  gramática; monta a AST
├── jflex/scanner.jflex             tokens
└── java
    ├── interpreter/Main.java       lê o arquivo, cria Scanner e Parser
    └── ast/
        ├── Expression, Command     classes base dos nós
        ├── SumExpr, SubExpr, MulExpr, DivExpr, ModExpr, PowExpr
        ├── UMinusExpr, SinExpr, CosExpr
        ├── NumberExpr, StringExpr, PiExpr, IdExpr
        ├── AssignCommand, PrintCommand, BlockCommand, Program
        ├── ASTVisitor              interface: um visit() por tipo de nó
        ├── InterpreterVisitor      executa a AST (tipos + pilha de escopos)
        ├── Value, Type             valor com tipo (DBL ou STR)
        └── Operations              funções estáticas das operações
```

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
Assim `-2**2` é `-4`. O CUP reporta **0 conflitos**.

## Como cada requisito foi feito

**R1: string.** O scanner reconhece `"..."` (um ou mais caracteres) e devolve o texto **sem as
aspas**; o parser cria um `StringExpr`.

**R2: tipos.** Todo valor é um `Value` (campo `type` e campo `value`). `NumberExpr` e
`StringExpr` já nascem com o `Value` correspondente.

**R3: operações** (`Operations.java`, chamadas pelo `InterpreterVisitor`):

| Operação | Regra |
| --- | --- |
| `+` | número + número = número; se algum lado for cadeia, concatena (o número é convertido em texto) |
| `- * / % **`, `-` unário, `sin`, `cos` | só com números; senão: `Tipos incompatíveis para ...` |
| atribuição | guarda o `Value` como está |
| `print(e)` | imprime `>> ` seguido do valor |

**R4: blocos.** `{` e `}` são tokens; `command_block` vira um `BlockCommand`, que guarda a lista de
comandos do bloco.

**R5: escopos** (`InterpreterVisitor`). A tabela de símbolos é
`Stack<HashMap<String, Value>>`:

- a pilha nasce com **uma** tabela, o escopo global, que dura o programa inteiro;
- `visit(BlockCommand)` faz `push` de uma tabela nova, executa os comandos e faz `pop`;
- **ler** uma variável (`visit(IdExpr)`) procura do topo para a base com um `ListIterator`, sem
  desempilhar; se nenhuma tabela tiver o nome: `Variável não definida: x`;
- **atribuir** (`visit(AssignCommand)`) grava sempre na tabela do **topo** (`peek`). Se o nome já
  existia num escopo externo, a variável é *redefinida* no bloco (sombreamento) e o valor
  externo fica intacto quando o bloco termina.

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
- O `pom.xml` usa o `build-helper-maven-plugin` (como no exemplo "AST Expression Parser") para
  incluir o `Parser` e o `Scanner` gerados no caminho de fontes; assim o VS Code também os enxerga.
