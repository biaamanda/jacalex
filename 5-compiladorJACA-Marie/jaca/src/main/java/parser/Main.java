/*
    Para executar o interpretador na linha de comando:
        java -jar target/jacacc-1.0-jar-with-dependencies.jar teste.input
    ou
        java -jar target/jacacc-1.0-jar-with-dependencies.jar
 */
package parser;

public class Main {

    // Executa o Jaca
    public static void main(String[] args) {
        if (args.length == 0) {
            // Sem argumentos de linha de comando -> executa de modo iterativo
            runREPL();
        } else {
            // Com argumento de nome de arquivo -> abre e interpreta o arquivo
            runInterpreter(args);
        }
    }

    // Executa como interpretador normal
    public static void runInterpreter(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso : java -jar jaca <nome do arquivo>");
        } else {
            try {
                java.io.FileInputStream stream = new java.io.FileInputStream(args[0]);
                java.io.Reader reader = new java.io.InputStreamReader(stream);
                // Cria um scanner (gerado pelo JFlex)
                Scanner scanner = new Scanner(reader);
                // Cria um parser (gerado pelo CUP)
                Parser parser = new Parser(scanner);
                // Executa a análise
                parser.parse();
                // System.out.println("Análise sintática terminada com sucesso!");

                // Análise semântica: recupera o nó raiz da AST que o CUP montou
                parser.ast.ASTNode root = parser.getAST();

                if (root != null) {
                    // System.out.println("\n--- Iniciando Execução do Interpretador ---");

                    // Instancia o Visitor para interpretar a AST
                    parser.ast.Interpreter interpreter = new parser.ast.Interpreter();

                    // Dispara o padrão Visitor (faz a árvore começar a rodar de cima para baixo)
                    root.accept(interpreter);

                    // System.out.println("--- Execução Finalizada com Sucesso ---\n");
                } else {
                    System.out.println("Erro: A AST não pôde ser gerada.");
                }
            } catch (java.io.FileNotFoundException e) {
                System.out.println("Arquivo não  encontrado: \"" + args[0] + "\"");
            } catch (Exception e) {
                System.out.println("Programa terminado com erro!");
            }
        }
    }

    // Executa como interpretador iterativo
    public static void runREPL() {
        System.out.println(
                "Jaca 1.0 REPL - Digite os comandos (pressione Enter duas vezes " +
                        "em uma linha vazia para executar, ou 'exit' para encerrar):");
        java.io.BufferedReader consoleReader = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        parser.ast.Interpreter interpreter = new parser.ast.Interpreter();

        StringBuilder codeBuffer = new StringBuilder();

        while (true) {
            try {
                // Altera o prompt para indicar se continua no mesmo bloco
                if (codeBuffer.length() == 0) {
                    System.out.print("> ");
                } else {
                    System.out.print("... ");
                }

                String line = consoleReader.readLine();
                if (line == null)
                    break;
                if (line.equalsIgnoreCase("exit"))
                    break;

                // CRITÉRIO DE EXECUÇÃO: Se o usuário digitar uma linha vazia
                // e já houver código no buffer, nós tentamos executar tudo.
                if (line.trim().isEmpty()) {
                    if (codeBuffer.length() > 0) {
                        // Transforma o acumulado em String e limpa o buffer para a próxima
                        String fullCode = codeBuffer.toString();
                        codeBuffer.setLength(0);
                        // Envia o bloco completo de código para o parser
                        Scanner scanner = new Scanner(new java.io.StringReader(fullCode));
                        Parser parser = new Parser(scanner);
                        parser.parse();

                        parser.ast.ASTNode root = parser.getAST();
                        if (root != null) {
                            root.accept(interpreter);
                        }
                    }
                    continue;
                }

                // Se não for uma linha vazia, acumula no buffer com uma quebra de linha
                codeBuffer.append(line).append("\n");

            } catch (Exception e) {
                System.out.println("Erro na execução: " + e.getMessage());
                codeBuffer.setLength(0); // Limpa o buffer em caso de erro para não travar
            }
        }
    }
}
