# Analisador sintático de Jaca com CUP e JFlex

Mesmo objetivo da Parte II (analisador recursivo descendente), agora com o
gerador de analisadores ascendentes LALR **CUP** e o scanner gerado pelo **JFlex**.

## Estrutura

| Arquivo | Função |
| --- | --- |
| `src/main/jflex/scanner.jflex` | scanner: devolve `Symbol` do CUP (`sym.ID`, `sym.IF`, ...) |
| `src/main/cup/parser.cup` | gramática de Jaca; o CUP gera `Parser.java` e `sym.java` |
| `src/main/java/parser/Main.java` | cria o `Scanner`, o `Parser` e chama `parse()` |
| `teste.input` | programa válido com todos os comandos e operadores |
| `tests/invalid-*.input` | um programa inválido por arquivo |

`Scanner.java`, `Parser.java` e `sym.java` são **gerados** pelo Maven em
`target/generated-sources`; não são editados à mão.

## Diferença para o recursivo descendente

- Lá cada regra é um método escrito à mão e as repetições `{ }` viram `while`.
- Aqui a gramática é escrita em `parser.cup`; as repetições viram recursão à
  esquerda (`statement_list ::= statement_list statement | statement`), que o
  LALR trata bem, e o CUP monta a tabela de análise.
- A gramática de Jaca foi escrita em camadas (`expression`, `simple_expr`,
  `term`, `factor`), então a precedência dos operadores já está na própria
  gramática e não precisa de `precedence`.

## Compilar e executar sem Maven (Windows)

Na pasta `jaca-cup` (a que tem o `build.ps1`):

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -jar build\jacacup.jar teste.input
```

O script usa o CUP do NetBeans e o JFlex do `.m2`, gera o scanner e o parser,
compila e monta `build\jacacup.jar`. Os caminhos estão no topo do `build.ps1`.

## Compilar e executar com Maven

Com o Maven instalado, na pasta que tem o `pom.xml`:

```bash
mvn clean package
java -jar target/jacacup-1.0-jar-with-dependencies.jar teste.input
```

Saída esperada para `teste.input`:

```text
Análise léxica terminada com sucesso!
Análise sintática terminada com sucesso!
```

Para um programa inválido, o parser para no primeiro erro:

```bash
java -jar target/jacacup-1.0-jar-with-dependencies.jar tests/invalid-semicolon.input
```

```text
Exceção: Erro durante a análise sintática: marca inesperada fim da entrada
Linha: 2 e Coluna: 1
```
