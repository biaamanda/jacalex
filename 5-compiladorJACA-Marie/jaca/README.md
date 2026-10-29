# 5. Compilador Jaca para Marie (Parte I)

Traduz programas **Jaca** para **assembly do MARIE**, o processador didático de Null & Lobur
(arquitetura de 16 bits com um acumulador). A saída pode ser colada e executada no simulador
oficial: <https://marie.js.org/>.

Parte do interpretador-base (JFlex + CUP + AST + Visitor) e, como o enunciado pede,
**troca apenas o visitante**: sai o `Interpreter`, entra o `Compiler`. Scanner, gramática e
nós da AST continuam iguais. O interpretador original está no histórico do Git (commit
`7f56e3f`, "add JACA Marie").

```text
programa Jaca ──▶ Scanner (JFlex) ──▶ Parser (CUP) ──▶ AST ──▶ Compiler (Visitor) ──▶ assembly Marie
```

## O que mudou em relação ao interpretador-base

| Arquivo | Mudança |
| --- | --- |
| `parser/ast/Interpreter.java` | **Removido** |
| `parser/ast/Compiler.java` | **Novo**: o Visitor que gera o assembly |
| `parser/Main.java` | Chama o `Compiler` e imprime (ou grava) o assembly; sem REPL |
| `cup/parser.cup` | Só acrescentei um contador de erros de sintaxe (`hasErrors()`), porque o CUP se recupera de erros e um compilador não deve gerar código nesse caso |
| `jflex/scanner.jflex`, nós da AST, `ASTVisitor`, `Value` | Sem alteração |
| `pom.xml` | Só o `artifactId`: `jacacc` virou `jacamarie` (o jar passa a se chamar `jacamarie-1.0-jar-with-dependencies.jar`) |
| `teste.input` | Adaptado para inteiros (o original usava `3.5` e `6.02`, que o MARIE não representa) |
| `tests/`, `tools/` | Novos: testes com saída esperada e um simulador mínimo do MARIE |
| `README.pdf` | Não foi alterado e **ainda descreve o interpretador** original |

## Como compilar e usar

```bash
mvn clean package
java -jar target/jacamarie-1.0-jar-with-dependencies.jar programa.input            # imprime o assembly
java -jar target/jacamarie-1.0-jar-with-dependencies.jar programa.input saida.mas  # grava em arquivo
```

Depois, abra <https://marie.js.org/>, cole o assembly no editor, clique em **Assemble** e em
**Run**. Escolha **Decimal** no formato do *Output log*. Para programas com `read`, preencha o
painel **Inputs** (formato Decimal) antes de executar.

## Exemplo

```text
i = 1;
while (i <= 3) {
    print(i);
    i = i + 1;
}
```

vira (saída `1 2 3`):

```text
            Load CstP1
            Store Vi
Lab1,       Load Vi
            Subt CstP3
            Skipcond 800
            Jump Lab3
            Jump Lab2
Lab3,       Load Vi
            Output
            Load Vi
            Add CstP1
            Store Vi
            Jump Lab1
Lab2,       Halt
Vi,         DEC 0
CstP1,      DEC 1
CstP3,      DEC 3
```

## Como a tradução funciona

O MARIE é uma máquina de acumulador (AC) sem registradores de uso geral, sem pilha e **sem
instruções de multiplicar, dividir ou calcular resto**. Por isso:

**Valores.** Todo valor é um inteiro de 16 bits com sinal (−32768 a 32767). Booleanos são `0`
(falso) e `1` (verdadeiro).

**Variáveis e constantes.** Cada variável Jaca vira uma palavra de memória `V<nome>` (`DEC 0`).
Não há valores imediatos no MARIE, então cada constante vira uma palavra `CstP<n>` ou
`CstN<n>`. Nomes internos nunca começam com `V`, para não colidir com variáveis do usuário.

**Expressões.** O resultado fica sempre no AC. Para `a op b`:

1. se `b` é variável ou constante, usa-se direto da memória (`Subt Vb`);
2. senão, `b` é calculado e guardado num temporário `Tmp<n>` (a profundidade da expressão
   decide quantos temporários existem);
3. `a` é calculado no AC e aplica-se a operação (`Add`, `Subt`).

**Comandos:**

| Jaca | Marie |
| --- | --- |
| `x = e;` | calcula `e` no AC; `Store Vx` |
| `print(e);` | calcula `e`; `Output` |
| `read(x);` | `Input`; `Store Vx` |
| `if (c) then {A} else {B}` | testa `c`, salta para o `else` se falso; `A`; `Jump fim`; `B` |
| `while (c) {A}` | `topo:` testa `c`, salta para o fim se falso; `A`; `Jump topo` |

**Comparações.** `a < b` calcula `a - b` e usa `Skipcond`: `000` (AC < 0), `400` (AC = 0) e
`800` (AC > 0). Como `Skipcond` só pula a próxima instrução, `!=`, `<=` e `>=` usam um rótulo
auxiliar. Quando a comparação é usada como valor (`x = a < b;`), o resultado `1` ou `0` é
materializado no AC.

**Sub-rotinas.** `*`, `/` e `%` são chamados com `JnS` e usam as palavras `MulA/MulB` e
`DivA/DivB`:

- **Multiplicação**: soma `|a|` por `|b|` vezes e corrige o sinal no fim.
- **Divisão e resto**: subtrai `|b|` de `|a|` até não poder; o quociente trunca para zero e o
  resto tem o sinal do dividendo, como no Java. Divisão por zero **interrompe o programa**
  (`Halt`).

As sub-rotinas só entram no assembly se o programa usar `*`, `/` ou `%`.

**Rótulos.** Vários rótulos sobre a mesma instrução (fim de `if` dentro de `while`, por
exemplo) são unificados, porque no MARIE um rótulo precisa estar numa instrução.

## Diferenças em relação ao interpretador (limitações)

| Interpretador | Compilador para Marie |
| --- | --- |
| Números `Double` | Só **inteiros** de 16 bits; `3.5` é erro de compilação |
| `7 / 2` = `3.5` | `7 / 2` = `3` (divisão inteira, truncando para zero) |
| `print` de booleano mostra `true`/`false` | Mostra `1`/`0` |
| `read` aceita número ou `true`/`false` | `read` só lê inteiros |
| Tipos checados em tempo de execução | Tipos (`INT`/`BOOL`) checados em **tempo de compilação** |
| Variável pode mudar de tipo | Uma variável tem **um só tipo**; trocar de tipo é erro |
| Variável não inicializada falha ao executar | Usar uma variável **antes de ela receber valor** (na ordem do texto) é erro |
| Estouro de número: sem limite | Estouro dá a volta em 16 bits, como no hardware |

Condições de `if` e `while` precisam ser booleanas (uma comparação, ou uma variável
booleana). `if (x)` com `x` numérico é erro de compilação.

## Testes

Cada `tests/*.input` tem um `tests/*.expected` com as entradas e a saída esperada. Para rodar
todos (precisa do Python e de `mvn clean package` antes):

```bash
py tools/run_tests.py
```

O script compila cada programa, simula o assembly com `tools/marie_sim.py` (um simulador
mínimo do MARIE) e compara com o esperado.

| Teste | O que cobre |
| --- | --- |
| `soma` | `read`, `+`, `-`, `*` |
| `aritmetica` | todos os operadores, sinais, precedência, temporários aninhados |
| `relacionais` | as 6 comparações como valor |
| `condicional` | `if`, `else`, `if` aninhado, 6 comparações |
| `laco` | `while`: soma 1..10, fatorial, MDC com `%`, laços aninhados |
| `booleanos` | variáveis booleanas, `==` entre booleanos, `while` com flag |
| `leitura` | `read` e contagem regressiva |
| `constantes` | −32768, 32767, `--x`, `x - -3` |
| `vazio` | programa só com `;` |
| `invalid-*` | real, fora de 16 bits, variável sem valor, condição numérica, tipos misturados, mudança de tipo, erro de sintaxe, símbolo ilegal |

**Verificação no simulador oficial.** Além do simulador local, os assemblies de `aritmetica` e de
um programa que junta `relacionais`, `condicional`, `booleanos` e `laco` foram executados em
<https://marie.js.org/> e produziram exatamente a saída esperada. O mesmo simulador confirmou
que rótulos com `_` são aceitos, que rótulos diferem entre maiúsculas e minúsculas e que
`Input` funciona.
