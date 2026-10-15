# Analisador léxico e sintático de Jaca

## Parte II: analisador sintático

A classe `compiler.parser.Parser` implementa um analisador descendente recursivo
(LL(1)): cada regra da gramática é um método e o token atual decide o caminho.
O `Main` cria o `Scanner`, passa para o `Parser` e chama `parse()`.

```bash
java -jar target/jacalex.jar tests/syntax-valid.input
```

Saída esperada para um programa correto:

```text
Análise léxica terminada com sucesso!
Análise sintática terminada com sucesso!
```

Para um programa incorreto, o parser para no primeiro erro e informa a marca esperada, a
recebida, a linha e a coluna. Casos de erro em `tests/syntax-invalid-*.input`.

## Parte I: analisador léxico

O programa lê a entrada e exibe a classificação de cada terminal (token).
Espaços, tabulações e quebras de linha são ignorados.
O token `EOF` encerra a leitura e não é impresso pelo `Main` original.

| Terminal | Classificação (`Tag`) |
| --- | --- |
| Identificador, como `x`, `_valor1` ou `ifx` | `ID` |
| Número, como `10`, `12.5` ou `1.2e-3` | `NUMBER` |
| `if`, `then`, `else`, `while` | `IF`, `THEN`, `ELSE`, `WHILE` |
| `=` | `ASSIGN` |
| `+`, `-` | `ADD_OPERATOR` |
| `*`, `/`, `%` | `MUL_OPERATOR` |
| `==`, `!=`, `<`, `>`, `<=`, `>=` | `REL_OPERATOR` |
| `(`, `)` | `LEFT_PAR`, `RIGHT_PAR` |
| `{`, `}` | `LEFT_BRACE`, `RIGHT_BRACE` |
| `;` | `SEMICOLON` |
| Fim da entrada (marca `-1` no enunciado) | `EOF` |

As regras estão em `src/main/jflex/scanner.jflex`. O Maven usa esse arquivo
para gerar `Scanner.java`. `IdentifierToken`, `NumberToken` e `OperatorToken`
herdam de `Token` e guardam também o texto reconhecido (lexema).

A divisão usa barra comum (`/`), conforme a correção do professor.
Esta parte faz apenas a análise léxica; não verifica a estrutura dos comandos.

## Testes

O arquivo `tests/valid-test.input` contém todos os terminais válidos, incluindo os seis
operadores relacionais e números inteiros, decimais e em notação científica.

```bash
mvn clean package
java -jar target/jacalex.jar tests/valid-test.input
```

Por exemplo, `x = 10 + 2;` produz:

```text
Token: <ID, x>
Token: <ASSIGN>
Token: <NUMBER, 10>
Token: <ADD_OPERATOR, +>
Token: <NUMBER, 2>
Token: <SEMICOLON>
Análise léxica terminada com sucesso!
```

Para testar um símbolo inválido:

```bash
java -jar target/jacalex.jar tests/invalid-test.input
```

O programa para no `@` e informa o erro na linha 1, coluna 8.
Outros exemplos inválidos para testar separadamente são `!`, `&`, `\`, `.5`
e `1.`. Já `-2` gera um operador `-` seguido do número `2`.

## Instruções de instalação e execução

Para **compilar** e **criar** o **arquivo** **jar** do **programa** de modo **automático**, você precisa instalar o **programa** **Apache** **Maven**. 

- Se estiver usando **Windows**:
  - Abra https://maven.apache.org/download.cgi
  - Baixe https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip
  - Descompacte o conteúdo e a adicione ao PATH a pasta que contem o executável `mvn`
- Se estiver usando algum Linux, use o gerenciador de pacotes próprio (exemplos):
  - `sudo apt install maven` (Debian, Ubuntu, etc)
  - `dnf install maven` (Red Hat, Fedora, etc)

Para compilar o projeto, abra um terminal na pasta que tem o arquivo `pom.xml` e execute:

```bash
mvn clean compile
```

Para compilar e criar o arquivo **jar** do projeto, abra um terminal na pasta que tem o arquivo `pom.xml` e execute:

```bash
mvn clean package
```

Para executar o jar, altere para a pasta `target` do projeto e execute no terminal:

```bash
java -jar jacalex.jar # para executar o analisador lendo a expressão que você digitar
```

ou

```bash
java -jar jacalex.jar ../tests/valid-test.input # para executar o analisador abrindo arquivo de testes
```
