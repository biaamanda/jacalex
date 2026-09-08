package compiler;

import compiler.scanner.Scanner;
import compiler.scanner.Tag;
import compiler.scanner.Token;

public class Main {

    public static void main(String[] args) {
        java.io.Reader reader = null;
        if (args.length == 0) {
            reader = new java.io.InputStreamReader(System.in);
        } else {
            if (args.length != 1) {
                System.out.println("Uso : java -jar jacalex <nome do arquivo>");
            } else {
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(args[0]);
                    reader = new java.io.InputStreamReader(stream);
                } catch (java.io.FileNotFoundException e) {
                    System.out.println("Arquivo não encontrado : \"" + args[0] + "\"");
                } catch (Exception e) {
                    System.out.println("Exceção de arquivo: " + e);
                }
            }
        }
        try {
            // Cria o analisador léxico
            Scanner scanner = new Scanner(reader);
            // Testar aqui no programa principal
            Token token;
            token = scanner.yylex();
            while (token.tag != Tag.EOF) {
                System.out.println("Token: " + token);
                token = scanner.yylex();
            }
        } catch (Exception e) {
            System.out.println("Exceção: " + e.getMessage());
        }

    }
}
