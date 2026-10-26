### Instruções

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
java -jar jacalex.jar ../teste.input # para executar o analisador abrindo arquivo de testes
```



