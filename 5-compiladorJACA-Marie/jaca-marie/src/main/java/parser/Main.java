/*
    Para compilar um programa Jaca para assembly de Marie:
        java -jar target/jacamarie-1.0-jar-with-dependencies.jar programa.input
    ou, gravando o assembly em um arquivo:
        java -jar target/jacamarie-1.0-jar-with-dependencies.jar programa.input saida.mas
 */
package parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import parser.ast.ASTNode;
import parser.ast.Compiler;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Uso : java -jar jacamarie <programa.input> [saida.mas]");
            System.exit(1);
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8)) {
            // Análise léxica e sintática: o CUP monta a AST
            Scanner scanner = new Scanner(reader);
            Parser parser = new Parser(scanner);
            parser.parse();
            if (parser.hasErrors()) {
                System.err.println("Compilação abortada: o programa tem erros de sintaxe.");
                System.exit(1);
            }

            ASTNode root = parser.getAST();
            if (root == null) {
                System.err.println("Erro: a AST não pôde ser gerada.");
                System.exit(1);
            }

            // Geração de código: o Visitor Compiler percorre a AST e produz o assembly
            String assembly = new Compiler().compile(root);

            if (args.length == 2) {
                Files.writeString(Path.of(args[1]), assembly, StandardCharsets.UTF_8);
                System.out.println("Assembly gravado em " + args[1]);
            } else {
                System.out.print(assembly);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Arquivo não encontrado: \"" + args[0] + "\"");
            System.exit(1);
        } catch (Compiler.CompileException e) {
            System.err.println("Erro de compilação: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Compilação abortada: " + e.getMessage());
            System.exit(1);
        }
    }
}
